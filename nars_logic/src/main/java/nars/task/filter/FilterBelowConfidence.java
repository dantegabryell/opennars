package nars.task.filter;

import nars.process.NAL;
import nars.task.Sentence;
import nars.task.Task;
import nars.task.TaskSeed;
import nars.truth.Truth;

/**
* Created by me on 2/9/15.
*/
public class FilterBelowConfidence implements DerivationFilter {

    @Override public String reject(NAL nal, TaskSeed task, boolean solution, boolean revised, boolean single, Sentence currentBelief, Task currentTask) {
        Truth t = task.getTruth();
        if (t != null) {
            float conf = t.getConfidence();
            if (conf < nal.memory.param.confidenceThreshold.get()) {
                //no confidence - we can delete the wrongs out that way.
                return "Insufficient confidence";
            }
        }
        return null;
    }
}
