/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.core;

import nars.NAR;
import nars.io.qa.Answered;
import nars.nar.Default;
import nars.narsese.InvalidInputException;
import nars.task.Sentence;
import nars.task.Task;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static nars.nal.nal7.Tense.Eternal;
import static org.junit.Assert.assertTrue;

/**
 * @author me
 */
public class NALQueryTest {


    @Test
    public void testQuery2() throws InvalidInputException {
        testQueryAnswered(16, 0);
    }

    @Test
    public void testQuery1() throws InvalidInputException {
        testQueryAnswered(0, 32);
    }


    public void testQueryAnswered(int cyclesBeforeQuestion, int cyclesAfterQuestion) throws InvalidInputException {

        final AtomicBoolean b = new AtomicBoolean(false);

        String question = cyclesBeforeQuestion == 0 ?
                "<a --> b>" /* unknown solution to be derived */ :
                "<b --> a>" /* existing solution, to test finding existing solutions */;

        NAR n = new NAR(new Default().setInternalExperience(null));
        //new TextOutput(n, System.out);

        n.believe("<a <-> b>", Eternal, 1.0f, 0.5f);

        n.believe("<b --> a>", Eternal, 1.0f, 0.5f);

        n.runWhileNewInput(cyclesBeforeQuestion);

        new Answered(n, question) {

            @Override
            public void onSolution(Sentence belief) {
                //System.out.println("solution: " + belief);
                b.set(true);
                off();
            }

            @Override
            public void onChildSolution(Task child, Sentence belief) {
                //System.out.println("  child: " +
                //child + " solution: " + belief);
            }
        };

        n.runWhileNewInput(cyclesAfterQuestion);

        assertTrue(b.get());

    }
}
