package nars.concept;

import nars.Global;
import nars.Memory;
import nars.process.NAL;
import nars.task.Sentence;
import nars.task.Task;
import nars.term.Compound;
import nars.truth.Truth;

import java.util.List;

import static nars.nal.UtilityFunctions.or;
import static nars.nal.nal1.LocalRules.revisible;
import static nars.nal.nal1.LocalRules.tryRevision;

/**
 * Stores beliefs ranked in a sorted ArrayList, with strongest beliefs at lowest indexes (first iterated)
 */
public class ArrayListBeliefTable extends ArrayListTaskTable implements BeliefTable {

    public ArrayListBeliefTable(int cap) {
        super(cap);
    }


    @Override public Task top(final boolean eternal, final boolean temporal) {
        if (isEmpty()) {
            return null;
        }
        else if (eternal && temporal) {
            return get(0);
        }
        else if (eternal ^ temporal) {
            final int n = size();
            for (int i = 0; i < n; i++) {
                Task t = get(i);
                if (eternal && t.isEternal()) return t;
                if (temporal && !t.isEternal()) return t;
            }
        }

        return null;
    }



    @Override public Task top(Ranker r) {

        float s = Float.MIN_VALUE;
        Task b = null;

        final int n = size();
        for (int i = 0; i < n; i++) {
            Task t = get(i);

            float x = r.rank(t, s);
            if (Float.isFinite(x) && (x > s)) {
                s = x;
                b = t;
            }
        }

        return b;
    }

    /**
     * Select a belief to interact with the given task in logic
     * <p>
     * get the first qualified one
     * <p>
     * only called in RuleTables.rule
     *
     * @param now the current time, or Stamp.TIMELESS to disable projection
     * @param task The selected task
     * @return The selected isBelief
     */
//    @Override
//    public Task match(final Task task, long now) {
//        if (isEmpty()) return null;
//
//        long occurrenceTime = task.getOccurrenceTime();
//
//        final int b = size();
//
//        if (task.isEternal()) {
//            Task eternal = top(true, false);
//
//        }
//        else {
//
//        }
//
//        for (final Task belief : this) {
//
//            //if (task.sentence.isEternal() && belief.isEternal()) return belief;
//
//
//            return belief;
//        }
//
//
//        Task projectedBelief = belief.projectTask(occurrenceTime, now);
//
//        //TODO detect this condition before constructing Task
//        if (projectedBelief.getOccurrenceTime()!=belief.getOccurrenceTime()) {
//            //belief = nal.derive(projectedBelief); // return the first satisfying belief
//            return projectedBelief;
//        }
//
//        return null;
//    }


    @Override
    public Task project(Task t, long now) {
        Task closest = top(new BeliefConfidenceAndCurrentTime(now));
        if (closest == null) return null;
        return closest.projectTask(t.getOccurrenceTime(), now);
    }

    @Override
    public Task add(Task input, Ranker r, Concept c) {

        final Memory memory = c.getMemory();


        long now = memory.time();

        if (isEmpty()) {
            add(input);
            return input;
        }
        else {

            if (r == null) {
                //just return the top item if no ranker is provided
                return top();
            }

            float rankInput = r.rank(input);    // for the new isBelief


            int i;

            final int siz = size();

            boolean atCapacity = (cap == siz);

            for (i = 0; i < siz; i++) {
                Task existing = get(i);

                float existingRank = r.rank(existing, rankInput);

                if (Float.isFinite(existingRank) && rankInput >= existingRank) {
                    //truth and stamp:
                    if (input.equivalentTo(existing, false, false, true, true, false)) {
                        return existing;
                    } else {


                        if (atCapacity) {
                            Task removed = remove(siz - 1);
                            memory.removed(removed, "Displaced");
                        }


                        add(i, input);

                        return input;
                    }
                }
            }
        }

//        if (size()!=preSize)
//            c.onTableUpdated(goalOrJudgment.getPunctuation(), preSize);
//
//        if (removed != null) {
//            if (removed == goalOrJudgment) return false;
//
//            m.emit(eventRemove, this, removed.sentence, goalOrJudgment.sentence);
//
//            if (preSize != table.size()) {
//                m.emit(eventAdd, this, goalOrJudgment.sentence);
//            }
//        }

        //the new task was not added, so remove it
        memory.removed(input, "Unbelievable/Undesirable");
        return null;
    }

        //TODO provide a projected belief
//
//
//
//        //first create a projected
//
//
//        /*if (input.sentence == belief.sentence) {
//            return false;
//        }*/
//
//        if (belief.sentence.equalStamp(input.sentence, true, false, true)) {
////                if (task.getParentTask() != null && task.getParentTask().sentence.isJudgment()) {
////                    //task.budget.decPriority(0);    // duplicated task
////                }   // else: activated belief
//
//            getMemory().removed(belief, "Duplicated");
//            return false;
//        } else if (revisible(belief.sentence, input.sentence)) {
//            //final long now = getMemory().time();
//
////                if (nal.setTheNewStamp( //temporarily removed
////                /*
////                if (equalBases(first.getBase(), second.getBase())) {
////                return null;  // do not merge identical bases
////                }
////                 */
////                //        if (first.baseLength() > second.baseLength()) {
////                new Stamp(newStamp, oldStamp, memory.time()) // keep the order for projection
////                //        } else {
////                //            return new Stamp(second, first, time);
////                //        }
////                ) != null) {
//
//            //TaskSeed projectedBelief = input.projection(nal.memory, now, task.getOccurrenceTime());
//
//
//            //Task r = input.projection(nal.memory, now, newBelief.getOccurrenceTime());
//
//            //Truth r = input.projection(now, newBelief.getOccurrenceTime());
//                /*
//                if (projectedBelief.getOccurrenceTime()!=input.getOccurrenceTime()) {
//                }
//                */
//
//
//
//            Task revised = tryRevision(belief, input, false, nal);
//            if (revised != null) {
//                belief = revised;
//                nal.setCurrentBelief(revised);
//            }
//
//        }
//

//        if (!addToTable(belief, getBeliefs(), getMemory().param.conceptBeliefsMax.get(), Events.ConceptBeliefAdd.class, Events.ConceptBeliefRemove.class)) {
//            //wasnt added to table
//            getMemory().removed(belief, "Insufficient Rank"); //irrelevant
//            return false;
//        }
//    }

//    @Override
//    public Task addGoal(Task goal, Concept c) {
//        if (goal.equalStamp(input, true, true, false)) {
//            return false; // duplicate
//        }
//
//        if (revisible(goal.sentence, oldGoal)) {
//
//            //nal.setTheNewStamp(newStamp, oldStamp, memory.time());
//
//
//            //Truth projectedTruth = oldGoal.projection(now, task.getOccurrenceTime());
//                /*if (projectedGoal!=null)*/
//            {
//                // if (goal.after(oldGoal, nal.memory.param.duration.get())) { //no need to project the old goal, it will be projected if selected anyway now
//                // nal.singlePremiseTask(projectedGoal, task.budget);
//                //return;
//                // }
//                //nal.setCurrentBelief(projectedGoal);
//
//                Task revisedTask = tryRevision(goal, oldGoalT, false, nal);
//                if (revisedTask != null) { // it is revised, so there is a new task for which this function will be called
//                    goal = revisedTask;
//                    //return true; // with higher/lower desire
//                } //it is not allowed to go on directly due to decision making https://groups.google.com/forum/#!topic/open-nars/lQD0no2ovx4
//
//                //nal.setCurrentBelief(revisedTask);
//            }
//        }
//    }


    /**
     * Determine the rank of a judgment by its quality and originality (stamp
     * baseLength), called from Concept
     *
     * @param s The judgment to be ranked
     * @return The rank of the judgment, according to truth value only
     */
    /*public float rank(final Task s, final long now) {
        return rankBeliefConfidenceTime(s, now);
    }*/

    public static class BeliefConfidenceAndCurrentTime implements Ranker {
        public final long now;

        public BeliefConfidenceAndCurrentTime(long now) {
            this.now = now;
        }

        @Override
        public float rank(Task t, float bestToBeat) {
            float c = t.getTruth().getConfidence();
            if (!t.isEternal()) {
                float dur = t.getDuration();
                float durationsToNow = Math.abs(t.getOccurrenceTime() - now) / dur;

                float ageFactor = 1.0f / (1.0f + durationsToNow * Global.rankDecayPerTimeDuration);
                c *= ageFactor;
            }
            return c;        }
    }


    public static float rankBeliefConfidence(final Sentence judg) {
        return judg.getTruth().getConfidence();
    }

    public static float rankBeliefOriginal(final Sentence judg) {
        final float confidence = judg.truth.getConfidence();
        final float originality = judg.getOriginality();
        return or(confidence, originality);
    }




//    boolean addToTable(final Task goalOrJudgment, final List<Task> table, final int max, final Class eventAdd, final Class eventRemove, Concept c) {
//        int preSize = table.size();
//
//        final Memory m = c.getMemory();
//
//        Task removed = addToTable(goalOrJudgment, table, max, c);
//
//        if (size()!=preSize)
//            c.onTableUpdated(goalOrJudgment.getPunctuation(), preSize);
//
//        if (removed != null) {
//            if (removed == goalOrJudgment) return false;
//
//            m.emit(eventRemove, this, removed.sentence, goalOrJudgment.sentence);
//
//            if (preSize != table.size()) {
//                m.emit(eventAdd, this, goalOrJudgment.sentence);
//            }
//        }
//
//        return true;
//    }



}
