package nars.meter;

import nars.Global;
import nars.NAR;
import nars.io.in.LibraryInput;
import nars.nar.Default;

public class Derivations1  {



    public static void main(String[] args) {

        Global.DEBUG = true;

        Derivations d = new Derivations(false, false);


        for (int seed = 0; seed < 4; seed++) {
            for (String s : LibraryInput.getPaths("test2")) {
                NAR n = new TestNAR(new Default().setInternalExperience(null).level(3));
                n.memory.randomSeed(seed);
                d.record(n);
                n.input(LibraryInput.getExample(s));
                n.runWhileNewInput(200);
            }
        }

        d.print(System.out);
    }
}
