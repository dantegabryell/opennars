package nars.nal.nal7;


import nars.NARSeed;
import nars.nal.ScriptNALTest;
import nars.nar.Classic;
import nars.nar.Curve;
import nars.nar.Default;
import nars.nar.DefaultMicro;
import org.junit.runners.Parameterized;

import java.util.Collection;

import static nars.io.in.LibraryInput.getParams;

public class NAL7ScriptTests extends ScriptNALTest {

    public NAL7ScriptTests(NARSeed b, String input) {
        super(b, input);
    }

    @Parameterized.Parameters(name= "{1} {0}")
    public static Collection configurations() {
        return getParams(new String[]{"test7"},
                new Default(),
                new Default().setInternalExperience(null),
                new Classic(),
                new Curve(),
                new DefaultMicro()
        );
    }

    public int getMaxCycles() { return 800; }


}

