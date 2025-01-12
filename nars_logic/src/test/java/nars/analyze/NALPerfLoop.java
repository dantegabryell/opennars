package nars.analyze;

import nars.Global;
import nars.NAR;
import nars.io.in.LibraryInput;
import nars.nal.NALTest;
import nars.nar.Default;

import java.util.Collection;

import static nars.analyze.experimental.NALStressMeasure.perfNAL;

/**
 * Runs NALTestPerf continuously, for profiling
 * TODO keep up to date with the new test script layout
 */
public class NALPerfLoop {
    
    public static void main(String[] args) {
       
        int repeats = 2;
        int warmups = 1;
        int maxConcepts = 2048;
        int extraCycles = 10048;
        int randomExtraCycles = 512;
        Global.THREADS = 1;
        Global.EXIT_ON_EXCEPTION = true;

          
        NAR n = new NAR(new Default().setActiveConcepts(maxConcepts).setInternalExperience(null) );
        //NAR n = new NAR( new Neuromorphic(16).setConceptBagSize(maxConcepts) );
        //NAR n = new NAR(new Curve());
        
        //NAR n = new NAR(new Discretinuous().setConceptBagSize(maxConcepts));

        //new NARPrologMirror(n,0.75f, true).temporal(true, true);              
        
        Collection c = NALTest.params();
        c.addAll(LibraryInput.getAllExamples().values());

        while (true) {
            for (Object o : c) {
                Object x = o;
                String examplePath = (x instanceof Object[]) ? (String)(((Object[])x)[1]) : (String)x;


                Global.DEBUG = Global.DEBUG_BAG = false;
                perfNAL(n, examplePath,extraCycles+ (int)(Math.random()*randomExtraCycles),repeats,warmups,false);
            }
        }        
    }
}
