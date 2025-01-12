/*
 * Copyright (C) 2014 me
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package nars.util;

import nars.NAR;
import nars.io.out.TextOutput;
import nars.io.signal.Number1DInput;
import nars.nar.Curve;
import nars.nar.Default;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author me
 */
public class Number1DInputTest {



    public static double[] randomArray(int size, double scale, double min) {
        double[] d = new double[size];
        for (int i = 0; i < size; i++) {
            d[i] = Math.random() * scale + min;
        }
        return d;
    }

    public static double[] pulse(int size, int center) {
        double[] d = new double[size];
        d[center] = 1.0;
        return d;
    }    
    
    //@Test
    public void test1() throws Exception {
        int N = 4;
        
        double[] x = randomArray(N, 1.0, 0);

        NAR n = new NAR(new Curve(true).
                setActiveConcepts(32367));
                
        (n.param).conceptsFiredPerCycle.set(1024);
        
        
        Number1DInput v = new Number1DInput(n, "a", x, 4);
        //n.addInput(v);
        
        
        
        new TextOutput(n, System.out);
        
        for (int i = 0; i < 100; i++) {
            v.next(randomArray(N, 1.0, 0));
            //v.next(pulse(N,i%N));            
            n.runWhileNewInput(10);
        }
        
        n.runWhileNewInput(10000000);
        
        
        //v.close();
                
        
        //n.finish(256);
        
        Assert.assertTrue(true);        
    }
    
    //@Test
    public void test2() throws Exception {
        int N = 4;
        int resolution = 4;
        
        double[] x = randomArray(N, 1.0, 0);

        NAR n = new NAR(new Default());
        
        //new TextOutput(n, System.out);
        
        //Number1DInput v = new Number1DInput("a", x, resolution);
        //n.addInput(v);
        
        n.frame(1);
        
        for (int i = 0; i < 10; i++) {
            //v.next(randomArray(N, 1.0, 0));
            //v.next(pulse(N,i%N));            
        }
        
        
        n.runWhileNewInput(12);
        //v.close();
        
        Assert.assertTrue(true);        
    }    
    
    @Test 
    public void nulltest() { }
    
    public static void main(String[] args) throws Exception {
        new Number1DInputTest().test1();
        //new Number1DInputTest().test2();
    }
}
