/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */

package nars.farg.workspace;

import nars.core.EventEmitter.EventObserver;
import nars.core.Events.CycleEnd;
import nars.core.NAR;
import nars.entity.BudgetValue;
import nars.farg.coderack.Breaker;
import nars.farg.coderack.Codelet;
import nars.farg.coderack.Coderack;
import nars.farg.slipnet.SlipNode;

/**
 *
 * @author patrick.hammer
 */
public class Workspace {

    public double tenperature=0.0;
    public NAR nar;
    public Workspace(NAR nar) {
        this.nar=nar;
        Workspace ws=this;
        codelets=new Coderack(FARGParameters.codelet_level,FARGParameters.max_codelets);
        codelets.putIn(new Breaker(new BudgetValue(0.9f,0.9f,0.5f),nar.memory,new int[]{1,2,3}));
        nar.on(CycleEnd.class, new EventObserver() { 

            @Override
            public void event(Class event, Object[] args) {
                Codelet cod=codelets.takeNext();
                cod.run(ws);
                codelets.putIn(cod);
                tenperature=calc_temperature();
            }
            
        });
    }

    public double calc_temperature() {
        double s=0.0f;
        int n=0;
        for(SlipNode node : nar.memory.concepts) {
            if(!node.desires.isEmpty()) {
                s+=node.getPriority()*node.desires.get(0).truth.getExpectation();
            }
            n++;
        }
        return s/((double) n);
        //return nar.memory.emotion.happy();
    }
        
    
    Coderack codelets;
}