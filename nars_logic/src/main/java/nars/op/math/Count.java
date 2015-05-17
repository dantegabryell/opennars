/*
 * Copyright (C) 2014 peiwang
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

package nars.op.math;

import nars.nal.nal3.SetExt;
import nars.nal.nal3.SetInt;
import nars.nal.nal8.TermFunction;
import nars.nal.term.Atom;
import nars.nal.term.Compound;
import nars.nal.term.Term;
import nars.op.mental.Mental;

/**
 * Count the number of elements in a set
 * 

'INVALID
(^count,a)!
(^count,a,b)!
(^count,a,#b)!

'VALID: 
(^count,[a,b],#b)!

 * 
 */
public class Count extends TermFunction<Integer> implements Mental {

    public Count() {
        super("^count");
    }

    final static String requireMessage = "Requires 1 SetExt or SetInt argument";
    
    final static Term counted = Atom.get("counted");




    @Override
    public Integer function(Term[] x) {
        Term content = x[0];
        if (!(content instanceof SetExt) && !(content instanceof SetInt)) {
            throw new RuntimeException(requireMessage);
        }       
        
        return ((Compound) content).length();
    }

    
}
