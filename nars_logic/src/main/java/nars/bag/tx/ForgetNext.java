package nars.bag.tx;

import nars.Global;
import nars.Memory;
import nars.bag.Bag;
import nars.bag.BagTransaction;
import nars.budget.Budget;
import nars.budget.Itemized;

/**
* Applies forgetting to the next sequence of sampled bag items
*/
public class ForgetNext<K, V extends Itemized<K>> implements BagTransaction<K,V> {

    private final Bag<K, V> bag;
    private float forgetCycles;
    private Memory memory;
    public V current = null;
    private long now;


    public ForgetNext(Bag<K, V> bag) {
        this.bag = bag;
        this.forgetCycles = Float.NaN;
    }

    @Override
    public K name() {
        return null; //signals to the bag updater to use the next item
    }

    protected static boolean forgettingCouldAffectItemBudget(final long now, final Itemized v) {

        final Budget b = v.getBudget();
        final long lastForgetTime = b.getLastForgetTime();

        if (lastForgetTime == -1) {
            b.setLastForgetTime(now);
            return false;
        }

        return (lastForgetTime != now) && //there is >0 time across which forgetting would be applied
                (b.getPriority() > b.getQuality() * Global.MIN_FORGETTABLE_PRIORITY); //there is sufficient priority for forgetting to occurr
    }

    /** updates with current time, etc. call immediately before update() will be called */
    public void set(float forgetCycles, Memory memory) {
        this.forgetCycles = forgetCycles;
        this.memory = memory;
        this.now = memory.time();
    }

    @Override
    public V updateItem(final V v) {
        /*if (Parameters.DEBUG) {
            if (!Float.isFinite(forgetCycles))
                throw new RuntimeException("Invalid forgetCycles parameter; set() method was probably not called prior");
        }*/

        this.current = v;

        if (!forgettingCouldAffectItemBudget(now, v)) {
            return null; //unaffected (null means that the item's budget was not changed, so the bag knows it can avoid any reindexing it)
        }

        Memory.forget(now, v, forgetCycles, Global.MIN_FORGETTABLE_PRIORITY);

        return v;
    }

    @Override
    public V newItem() {
        return null;
    }

    @Override
    public Budget getBudget() {
        //this returns null to avoid the default budget merging;
        // budget manipulation happens entirely in this class's updateItem method
        return null;

    }

    @Override
    public Budget getBudgetRef() {
        return null;
    }

    public V getItem() {
        return null;
    }
}
