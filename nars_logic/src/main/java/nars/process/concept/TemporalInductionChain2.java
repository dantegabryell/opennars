package nars.process.concept;

import nars.Global;
import nars.Memory;
import nars.Op;
import nars.concept.Concept;
import nars.link.TaskLink;
import nars.link.TermLink;
import nars.nal.nal5.Implication;
import nars.nal.nal7.TemporalRules;
import nars.process.ConceptProcess;
import nars.task.Sentence;
import nars.task.Task;
import nars.term.Term;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import static nars.term.Terms.equalSubTermsInRespectToImageAndProduct;

/**
* Patrick's new version which 'restores the special reasoning context'
* new attempt/experiment to make nars effectively track temporal coherences
*/
public class TemporalInductionChain2 extends ConceptFireTaskTerm {

    static class ConceptByOperator implements Predicate<Concept> {

        final Op[] n;

        public ConceptByOperator(Op... n) {
            this.n = n;
        }

        @Override
        public boolean test(Concept concept) {
            for (Op x : n)
                if (concept.operator() == x)
                    return true;
            return false;
        }

    }

    final InductableImplication nextInductedImplication = new InductableImplication();

    @Override
    public boolean apply(ConceptProcess f, TaskLink taskLink, TermLink termLink) {

        if (!f.nal(7)) return true;

        final Sentence belief = f.getCurrentBelief();
        if (belief == null) return true;

        if (!belief.isJudgment()) return true;

        final Concept concept = f.getCurrentConcept();

        Task task = f.getCurrentTask();

        if (task.sentence.isEternal())
            return true;

        final Memory memory = f.memory;

        final Term beliefTerm = belief.getTerm();

        if (beliefTerm instanceof Implication &&
                (beliefTerm.getTemporalOrder() == TemporalRules.ORDER_FORWARD || beliefTerm.getTemporalOrder() == TemporalRules.ORDER_CONCURRENT)) {

            final int chainSamples = Global.TEMPORAL_INDUCTION_CHAIN_SAMPLES;
            final float chainSampleSearchSize = Global.TEMPORAL_INDUCTION_CHAIN_SAMPLE_DEPTH(taskLink.getPriority());

            //prevent duplicate inductions

            nextInductedImplication.reset(task, f.memory.duration());

            for (int i = 0; i < chainSamples; i++) {

                //TODO create and use a sampleNextConcept(NALOperator.Implication) method

                Concept next = memory.cycle.nextConcept(nextInductedImplication, chainSampleSearchSize);
                if (next == null || next.equals(concept))
                    continue;

                //Implication t = (Implication) next.getTerm();


                //select a Non-eternal (temporal) belief
                Task b = nextInductedImplication.getTask();
                f.setCurrentBelief(b);

                induct(task,
                        nextInductedImplication.getPrevious(),
                        nextInductedImplication.getCurrent(), f);
            }
        }

        return true;

    }


    boolean induct(Task task, Sentence prev, Sentence current, ConceptProcess f) {

        //if(newEvent.getPriority()>Parameters.TEMPORAL_INDUCTION_MIN_PRIORITY)
        TemporalRules.temporalInduction(current, prev,
                f, task,
                false
        );
        return true;
    }


    static class InductableImplication extends ConceptByOperator {

        Set<Sentence> alreadyInducted = new HashSet();
        Task belief = null;
        private Sentence current, prev;
        private Task task;
        private int duration;

        public InductableImplication() {
            super(Op.IMPLICATION);
        }

        @Override
        public boolean test(Concept concept) {

            belief = null;

            if (super.test(concept)) {

                Task temporalBelief = concept.getBeliefs().top(false, true);
                if (temporalBelief != null) {
                    if (!temporalBelief.isEternal() && TemporalRules.isInputOrTriggeredOperation(temporalBelief)) {

                        //if it was not already inducted, then Set.add will return true
                        if (alreadyInducted.add(temporalBelief.sentence)) {

                            if(task.isTemporalInductable()) { //todo refine, add directbool in task

                                if (!equalSubTermsInRespectToImageAndProduct(current.getTerm(), prev.getTerm())) {

                                    this.belief = temporalBelief;

                                    Sentence otherBelief = temporalBelief.sentence;
                                    if (otherBelief.after(task.sentence, duration)) {
                                        current = otherBelief;
                                        prev = task.sentence;
                                    } else {
                                        current = task.sentence;
                                        prev = otherBelief;
                                    }

                                    return true;
                                }
                            }
                        }

                    }
                }
            }

            return false;
        }

        public Task getTask() {
            return belief;
        }

        /** holds the belief of the current concept that was identified as valid in the test() method, to avoid having to find it again */
        public Sentence getBelief() {
            return belief.sentence;
        }

        public void reset(Task task, int duration) {
            this.task = task;
            this.duration = duration;
            alreadyInducted.clear();
            prev = current = null;
            belief = null;
        }

        public Sentence getPrevious() {
            return prev;
        }
        public Sentence getCurrent() {
            return current;
        }
    }
}
