package nars.meter;

import nars.Events;
import nars.NAR;
import nars.event.NARReaction;
import nars.util.meter.Meter;
import nars.util.meter.SignalData;
import nars.util.meter.TemporalMetrics;
import nars.util.meter.func.FirstOrderDifference;

import java.util.ArrayList;
import java.util.List;

public class NARMetrics extends NARReaction {

    public final TemporalMetrics<Object> metrics;
    private final NAR nar;

    public NARMetrics(NAR n, int historySize) {
        super(n, true, Events.FrameEnd.class);

        this.nar = n;

        metrics = new TemporalMetrics(historySize);

        metrics.addMeters(n.memory.emotion);

        if (n.memory.resource!=null)
            metrics.addMeters(n.memory.resource);


        //metrics.addMeter(new BasicStatistics(metrics, n.memory.resource.FRAME_DURATION.id(), 16));
        if (n.memory.resource!=null)
            metrics.addMeter(new FirstOrderDifference(metrics, n.memory.resource.CYCLE_RAM_USED.id()));

        metrics.addMeters(n.memory.logic);
        n.memory.logic.setActive(isActive());
    }

    @Override
    public void setActive(boolean b) {
        super.setActive(b);
        if (nar!=null)
            nar.memory.logic.setActive(b);
    }

    @Override
    public void event(Class event, Object[] args) {
        if (event == Events.FrameEnd.class) {
            metrics.update((double)nar.time());
        }

    }

    public void addMeter(Meter m) {
        metrics.addMeter(m);
    }

    public TemporalMetrics<Object> getMetrics() {
        return metrics;
    }

    public SignalData[] getCharts(String... names) {
        List<SignalData> l = new ArrayList(names.length);
        for (String n : names) {
            SignalData t = metrics.newSignalData(n);
            if (t!=null)
                l.add(t);
        }
        return l.toArray(new SignalData[l.size()]);
    }

    public List<SignalData> getCharts() {
        return metrics.getSignalDatas();
    }
}
