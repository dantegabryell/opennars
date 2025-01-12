package nars.op.app;

import nars.Events;
import nars.Global;
import nars.Memory;
import nars.NAR;
import nars.event.NARReaction;
import nars.nal.nal7.TemporalRules;
import nars.process.TaskProcess;
import nars.task.Sentence;
import nars.task.Task;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;

import static nars.nal.nal7.TemporalRules.containsMentalOperator;
import static nars.term.Terms.equalSubTermsInRespectToImageAndProduct;

/**
 * Short-term memory Event Induction.  Empties task buffer when plugin is (re)started.
 */
public class STMInduction extends NARReaction {

    public final Deque<Task> stm;
    int stmSize;
    //public static STMInduction I=null;

    public STMInduction(NAR nar) {
        super(nar);
        this.stmSize = 1;
        stm = Global.THREADS == 1 ? new ArrayDeque() : new ConcurrentLinkedDeque<>();
        //I=this; //hm there needs to be a way to query plugins from the NAR/NAL object like in 1.6.x, TODO find out
    }

    @Override
    public Class[] getEvents() {
        return new Class[]{TaskProcess.class, Events.ResetStart.class};
    }


    @Override
    public void event(Class event, Object[] args) {
        if (event == TaskProcess.class) {
            Task t = (Task) args[0];
            TaskProcess n = (TaskProcess) args[1];
            inductionOnSucceedingEvents(t, n, false);
        }
        else if (event == Events.ResetStart.class) {
            stm.clear();
        }
    }

    public static boolean isInputOrTriggeredOperation(final Task newEvent, Memory mem) {
        if (newEvent.isInput()) return true;
        if (containsMentalOperator(newEvent)) return true;
        if (newEvent.getCause()!=null) return true;
        return false;
    }

    public int getStmSize() {
        return stmSize;
    }

    public boolean inductionOnSucceedingEvents(final Task currentTask, TaskProcess nal, boolean anticipation) {

        stmSize = nal.memory.param.shortTermMemoryHistory.get();

        if (currentTask == null || (!currentTask.isTemporalInductable() && !anticipation)) { //todo refine, add directbool in task
            return false;
        }


        if (currentTask.sentence.isEternal() || (!isInputOrTriggeredOperation(currentTask, nal.memory) && !anticipation)) {
            return false;
        }

        //new one happened and duration is already over, so add as negative task
        nal.emit(Events.InduceSucceedingEvent.class, currentTask, nal);

        //final long now = nal.memory.time();


        Iterator<Task> ss = stm.iterator();

        int numToRemoveFromBeginning = stm.size() - stmSize;

        while (ss.hasNext()) {

            Task stmLast = ss.next();


            if (!equalSubTermsInRespectToImageAndProduct(currentTask.sentence.getTerm(), stmLast.sentence.getTerm())) {
                continue;
            }


            if (numToRemoveFromBeginning > 0) {
                ss.remove();
            }
        }


        //iterate on a copy because temporalInduction seems like it sometimes calls itself recursively and this will cause a concurrent modification exception otherwise
        Task[] stmCopy = stm.toArray(new Task[stm.size()]);

        for (Task previousTask : stmCopy) {




            //nal.setCurrentTask(currentTask);

            Sentence previousBelief = previousTask.sentence;
            nal.setCurrentBelief(previousTask);

            Sentence currentBelief = currentTask.sentence;

            //if(currentTask.getPriority()>Parameters.TEMPORAL_INDUCTION_MIN_PRIORITY)
            TemporalRules.temporalInduction(currentBelief, previousBelief,
                    //nal.newStamp(currentTask.sentence, previousTask.sentence),
                    nal);
        }

        ////for this heuristic, only use input events & task effects of operations
        ////if(currentTask.getPriority()>Parameters.TEMPORAL_INDUCTION_MIN_PRIORITY) {
        //stmLast = currentTask;
        ////}
        while (stm.size() > stmSize) {
            stm.removeFirst();
        }
        stm.add(currentTask);

        return true;
    }

}
