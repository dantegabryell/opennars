package ca.nengo.ui.test;

import ca.nengo.math.impl.GaussianPDF;
import ca.nengo.model.Node;
import ca.nengo.model.SimulationException;
import ca.nengo.model.StructuralException;
import ca.nengo.model.Units;
import ca.nengo.model.impl.NetworkImpl;
import ca.nengo.model.impl.NoiseFactory;
import ca.nengo.neural.neuron.impl.LIFSpikeGenerator;
import ca.nengo.neural.neuron.impl.LinearSynapticIntegrator;
import ca.nengo.neural.neuron.impl.SpikingNeuron;
import ca.nengo.ui.Nengrow;
import ca.nengo.ui.lib.world.WorldObject;
import ca.nengo.ui.model.node.UINetwork;
import ca.nengo.ui.model.plot.LinePlot;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class TestPlotNode extends Nengrow {
    public static final float RESOLUTION_SEC = .001f;

    //https://github.com/nengo/nengo_1.4/blob/master/simulator-ui/docs/simplenodes.rst

    public static Node newPlotNodeDemo() throws StructuralException {
        NetworkImpl network = new NetworkImpl();

        SpikingNeuron sn = new SpikingNeuron(
                new LinearSynapticIntegrator(RESOLUTION_SEC/100f, Units.ACU),
                new  LIFSpikeGenerator(.001f, .02f, .002f), 1, 0.1f, "A");

        network.addNode(sn.setNoise(NoiseFactory.makeRandomNoise(15000f, new GaussianPDF())));

        network.addNode(new LinePlot("Activity"));

        return network;
    }


    @Override
    public void init() throws Exception {



        UINetwork networkUI = (UINetwork) addNodeModel(newPlotNodeDemo());
        networkUI.doubleClicked();

        new Timer(25, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    float dt = getSimulationDT();
                    networkUI.getModel().run(time, time + dt);
                    time += dt;
                } catch (SimulationException e1) {
                    e1.printStackTrace();
                }
                //cycle();
            }
        }).start();

    }

    float time = 0;


    public void cycle() {


        for (WorldObject x : this.getNengoWorld().getChildren()) {
                    //System.out.println( x.getChildren() );
            //x.run(time, time+1);
        }
    }

    public static void main(String[] args) {
        new TestPlotNode();
    }


}
