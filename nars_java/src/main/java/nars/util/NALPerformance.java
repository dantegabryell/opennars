/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nars.util;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import nars.core.NAR;
import nars.core.Parameters;
import nars.io.TextInput;
import nars.io.condition.OutputCondition;

/**
 *
 * @author me
 */
public class NALPerformance implements Runnable {
    
    static public int similarsToSave = 5;       

    double score;
    private boolean success;
    private boolean error;
    public final List<OutputCondition> expects = new ArrayList();
    private final NAR nar;
    private final String input;
    private final int cycles;

    public NALPerformance(NAR n, String input, int cycles) {
        this.nar = n;
        this.input = input;
        this.cycles = cycles;
    }

    @Override
    public void run() {
        try {
            
            List<OutputCondition> extractedExpects = OutputCondition.getConditions(nar, input, similarsToSave);
            
            for (OutputCondition e1 : extractedExpects) {
                expects.add(e1);
            }
            
            nar.addInput(new TextInput(input));
            
            nar.run(cycles);
            
        } catch (Throwable e) {
            System.err.println(e);
            if (Parameters.DEBUG) {
                e.printStackTrace();
            }
            error = true;
        }
        success = expects.size() > 0 && (!error);
        for (OutputCondition e : expects) {
            if (!e.succeeded) {
                success = false;
            }
        }
        score = Double.POSITIVE_INFINITY;
        if (success) {
            long lastSuccess = -1;
            for (OutputCondition e : expects) {
                if (e.getTrueTime() != -1) {
                    if (lastSuccess < e.getTrueTime()) {
                        lastSuccess = e.getTrueTime();
                    }
                }
            }
            if (lastSuccess != -1) {
                //score = 1.0 + 1.0 / (1+lastSuccess);
                score = lastSuccess;
            }
        }
    }

    public double getScore() {
        return score;
    }

    public boolean getSuccess() {
        return success;
    }

    public List<OutputCondition> getExpects() {
        return expects;
    }

    private long getCycleTime() {
        return nar.memory.getCycleTime();
    }

    public void printResults(PrintStream p) {
        p.println(" @" + getCycleTime());
        for (OutputCondition e : expects) {
            p.println("  " + e);
        }
    }
    
}