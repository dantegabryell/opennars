package nars.nal.nal6;


import nars.NARSeed;
import nars.nal.ScriptNALTest;
import nars.nar.Classic;
import nars.nar.Curve;
import nars.nar.Default;
import org.junit.runners.Parameterized;

import java.util.Collection;

import static nars.io.in.LibraryInput.getParams;

public class NAL6ScriptTests extends ScriptNALTest {

    public NAL6ScriptTests(NARSeed b, String input) {
        super(b, input);
    }

    @Parameterized.Parameters(name= "{1} {0}")
    public static Collection configurations() {
        return getParams(new String[]{"test6"},
                new Default(),
                new Default().setInternalExperience(null),
                new Default().setInternalExperience(null).level(6),
                new Classic().setInternalExperience(null),
                new Curve());
    }

    public int getMaxCycles() { return 850; }


}

