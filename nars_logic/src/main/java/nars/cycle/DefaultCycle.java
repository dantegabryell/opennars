package nars.cycle;

import nars.Global;
import nars.Memory;
import nars.bag.Bag;
import nars.concept.Concept;
import nars.link.TaskLink;
import nars.process.ConceptProcess;
import nars.process.TaskProcess;
import nars.task.Sentence;
import nars.task.Task;
import nars.task.TaskComparator;
import nars.term.Compound;
import nars.term.Term;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/**
 * The original deterministic memory cycle implementation that is currently used as a standard
 * for development and testing.
 */
public class DefaultCycle extends SequentialCycle {

    /**
     * New tasks with novel composed terms, for delayed and selective processing
     */
    public final Bag<Sentence<Compound>, Task<Compound>> novelTasks;

    /* ---------- Short-term workspace for a single cycle ------- */
    /**
     * List of new tasks accumulated in one cycle, to be processed in the next
     * cycle
     */
    protected TreeSet<Task> newTasks;
    protected List<Task> newTasksTemp = new ArrayList();
    protected boolean executingNewTasks = false;



    public DefaultCycle(Bag<Term, Concept> concepts, Bag<Sentence<Compound>, Task<Compound>> novelTasks) {
        super(concepts);


        this.novelTasks = novelTasks;


    }


    @Override
    public void reset(Memory m, boolean delete) {
        super.reset(m, delete);

        newTasksTemp.clear();

        if (delete) {
            newTasks.clear();
            newTasks = null;
            novelTasks.delete();
        }
        else {
            //this.newTasks = new ConcurrentSkipListSet(new TaskComparator(memory.param.getDerivationDuplicationMode()));
            if (newTasks == null)
                newTasks = new TreeSet(new TaskComparator(memory.param.getInputMerging()));
            else
                newTasks.clear();

            novelTasks.clear();
        }


    }

    @Override
    public boolean addTask(Task t) {
        if (executingNewTasks) {
            return newTasksTemp.add(t); //buffer it
        }
        else {
            return newTasks.add(t); //add it directly to the newtasks set
        }
    }


    /**
     * An atomic working cycle of the system: process new Tasks, then fire a
     * concept
     **/
    @Override
    public synchronized void cycle() {

        //inputs
        memory.perceiveNext(memory.param.inputsMaxPerCycle.get());


        //all new tasks
        int numNewTasks = newTasks.size();
        if (numNewTasks > 0)
            runNewTasks();


        //1 novel tasks if numNewTasks empty
        if (newTasks.isEmpty()) {
            int numNovelTasks = 1;
            for (int i = 0; i < numNovelTasks; i++) {
                Runnable novel = nextNovelTask();
                if (novel != null) novel.run();
                else
                    break;
            }
        }


        //1 concept if (memory.newTasks.isEmpty())*/
        int conceptsToFire = newTasks.isEmpty() ? memory.param.conceptsFiredPerCycle.get() : 0;

        float tasklinkForgetDurations = memory.param.taskLinkForgetDurations.floatValue();
        float conceptForgetDurations = memory.param.conceptForgetDurations.floatValue();
        for (int i = 0; i < conceptsToFire; i++) {
            ConceptProcess f = nextTaskLink(nextConceptToProcess(conceptForgetDurations), tasklinkForgetDurations);
            if (f != null) {
                f.run();
            }
        }

        concepts.forgetNext(
                memory.param.conceptForgetDurations,
                memory.random.nextFloat() * Global.CONCEPT_FORGETTING_EXTRA_DEPTH,
                memory);

        memory.runNextTasks();
        run.clear();

    }

    private void runNewTasks() {


        {

            for (int n = newTasks.size()-1;  n >= 0; n--) {
                Task highest = newTasks.pollLast();
                if (highest == null) break;

                executingNewTasks = true;
                run(highest);
                executingNewTasks = false;

                commitNewTasks();
            }

        }


    }

    private void commitNewTasks() {
        //add the generated tasks back to newTasks
        int ns = newTasksTemp.size();
        if (ns > 0) {
            newTasks.addAll( newTasksTemp );
            newTasksTemp.clear();
        }
    }

    /** returns whether the task was run */
    protected boolean run(Task task) {
        final Sentence sentence = task.sentence;

        //memory.emotion.busy(task);

        if (task.isInput() || !(sentence.isJudgment())
                || (memory.concept(sentence.getTerm()) != null)
                ) {

            //it is a question/quest or a judgment for a concept which exists:
            return TaskProcess.run(memory, task)!=null;

        } else {
            //it is a judgment or goal which would create a new concept:


            final Sentence s = sentence;

            //if (s.isJudgment() || s.isGoal()) {

            final double exp = s.truth.getExpectation();
            if (exp > memory.param.conceptCreationExpectation.floatValue()) {//Global.DEFAULT_CREATION_EXPECTATION) {

                // new concept formation
                Task overflow = novelTasks.put(task);

                if (overflow != null) {
                    if (overflow == task) {
                        memory.removed(task, "Ignored");
                        return false;
                    } else {
                        memory.removed(overflow, "Displaced novel task");
                    }
                }

                memory.logic.TASK_ADD_NOVEL.hit();
                return true;

            } else {
                memory.removed(task, "Neglected");
            }
            //}
        }
        return false;
    }


    private ConceptProcess nextTaskLink(final Concept concept, float taskLinkForgetDurations) {
        if (concept == null) return null;


        TaskLink taskLink = concept.getTaskLinks().forgetNext(taskLinkForgetDurations, memory);
        if (taskLink!=null)
            return newConceptProcess(concept, taskLink);
        else {
            return null;
        }

    }


    /**
     * Select a novel task to process.
     */
    protected Runnable nextNovelTask() {
        if (novelTasks.isEmpty()) return null;

        final Task task = novelTasks.pop();

        return TaskProcess.run(memory, task);
    }





}
      /*
    //private final Cycle loop = new Cycle();
    class Cycle {
        public final AtomicInteger threads = new AtomicInteger();
        private final int numThreads;

        public Cycle() {
            this(Parameters.THREADS);
        }

        public Cycle(int threads) {
            this.numThreads = threads;
            this.threads.set(threads);

        }

        int t(int threads) {
            if (threads == 1) return 1;
            else {
                return threads;
            }
        }




        public int newTasksPriority() {
            return memory.newTasks.size();
        }

        public int novelTasksPriority() {
            if (memory.getNewTasks().isEmpty()) {
                return t(numThreads);
            } else {
                return 0;
            }
        }

        public int conceptsPriority() {
            if (memory.getNewTasks().isEmpty()) {
                return memory.param.conceptsFiredPerCycle.get();
            } else {
                return 0;
            }
        }


    }

    */
