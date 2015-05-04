/*
 * Sample.java
 *
 * Copyright (C) 2008  Pei Wang
 *
 * This file is part of Open-NARS.
 *
 * Open-NARS is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * Open-NARS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Open-NARS.  If not, see <http://www.gnu.org/licenses/>.
 */
package nars.grid2d.operator;

import nars.grid2d.TestChamber;
import nars.nal.Task;
import nars.nal.term.Term;
import nars.nal.nal8.Operation;
import nars.nal.nal8.Operator;

import java.util.List;

/**
 *  A class used as a template for Operator definition.
 * TODO: memory.registerOperator(new Goto("^goto"));
 */
public class Activate extends Operator {

    TestChamber chamb;
    public Activate(TestChamber chamb, String name) {
        super(name);
        this.chamb=chamb;
    }

    @Override
    protected List<Task> execute(Operation operation, Term[] args) {
        //Operation content = (Operation) task.getContent();
        //Operator op = content.getOperator();
         
        TestChamber.executed=true;
        System.out.println("Executed: " + this);
        for (Term t : args) {
            System.out.println(" --- " + t);
            TestChamber.operateObj(t.toString(), "activate");
            break;
        }
        
        
       // if(nars.grid2d.Grid2DSpace.world_used) {
            //ok lets start pathfinding tool
            //nars.grid2d.Grid2DSpace.pathFindAndGoto(arg);
       // }
        
       
        
        return null;
    }

}
