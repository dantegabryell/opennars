package nars;

import nars.Events.FrameEnd;
import nars.Events.FrameStart;
import nars.budget.BudgetFunctions;
import nars.concept.Concept;
import nars.concept.ConceptBuilder;
import nars.io.TextPerception;
import nars.io.in.FileInput;
import nars.io.in.Input;
import nars.io.in.ReaderInput;
import nars.io.in.TextInput;
import nars.nal.nal7.Tense;
import nars.nal.nal8.ImmediateOperation;
import nars.nal.nal8.Operator;
import nars.narsese.InvalidInputException;
import nars.narsese.NarseseParser;
import nars.process.TaskProcess;
import nars.task.Task;
import nars.task.TaskSeed;
import nars.task.stamp.Stamp;
import nars.term.Atom;
import nars.term.Compound;
import nars.term.Term;
import nars.truth.DefaultTruth;
import nars.truth.Truth;
import nars.util.event.EventEmitter;
import nars.util.event.Reaction;
import objenome.Container;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;


/**
 * Non-Axiomatic Reasoner
 * <p>
 * Instances of this represent a reasoner connected to a Memory, and set of Input and Output channels.
 * <p>
 * All state is contained within Memory.  A NAR is responsible for managing I/O channels and executing
 * memory operations.  It executesa series sof cycles in two possible modes:
 * * step mode - controlled by an outside system, such as during debugging or testing
 * * thread mode - runs in a pausable closed-loop at a specific maximum framerate.
 */
public class NAR extends Container implements Runnable {

    /**
     * The information about the version and date of the project.
     */
    public static final String VERSION = "Open-NARS v1.7.0";

    /**
     * The project web sites.
     */
    public static final String WEBSITE =
            " Open-NARS website:  http://code.google.com/p/open-nars/ \n"
                    + "      NARS website:  http://sites.google.com/site/narswang/ \n" +
                    "    Github website:  http://github.com/opennars/ \n" +
                    "    IRC:  http://webchat.freenode.net/?channels=nars \n";
    public final NarseseParser narsese;
    public final TextPerception textPerception;


    /**
     * The name of the reasoner
     */
    protected String name;
    /**
     * The memory of the reasoner
     */
    public final Memory memory;
    public final Param param;


    public void think(int delay) {
        memory.think(delay);
    }

    public NAR input(ImmediateOperation o) {
        input(o.newTask());
        return this;
    }


    /**
     * Flag for running continuously
     */
    private boolean running = false;

    private boolean threadYield = false;

    private int cyclesPerFrame = 1; //how many memory cycles to execute in one NAR cycle

    /**
     * memory activity enabled
     */
    private boolean enabled = true;


    protected NAR(final Memory m) {
        super();
        this.memory = m;
        this.param = m.param;

        the(NAR.class, this);

        this.narsese = NarseseParser.newParser(this);
        this.textPerception = new TextPerception(this, narsese);
    }

    /**
     * Reset the system with an empty memory and reset clock.  Event handlers
     * will remain attached but enabled plugins will have been deactivated and
     * reactivated, a signal for them to empty their state (if necessary).
     */
    public void reset() {
        memory.reset(true, false);
    }

    /**
     * Resets and deletes the entire system
     */
    public void delete() {
        memory.delete();
    }


    public Input input(final File input) throws IOException {
        return input(new FileInput(textPerception, input));
    }

    /**
     * this needs tested to see if it still works, an asynchronous input interface is probably better
     */
    @Deprecated
    public Input input(final InputStream input) {
        return input(new ReaderInput(textPerception, input));
    }

    /**
     * inputs a task, only if the parsed text is valid; returns null if invalid
     */
    public Task inputTask(final String taskText) {
        try {
            Task t = task(taskText);
            input(t);
            return t;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * parses and forms a Task from a string but doesnt input it
     */
    public Task task(String taskText) {
        Task t = narsese.parseTask(taskText);

        long now = time();
        if (!t.sentence.isEternal()) {
            t.getSentence().setTime(now, now + t.sentence.getOccurrenceTime());
        } else {
            t.getSentence().setTime(now, Stamp.ETERNAL);
        }
        return t;
    }

    public <T extends Compound> TaskSeed<T> task(T t) {
        return memory.newTask(t);
    }

    public TextInput input(final String text) {
        //final TextInput i = new TextInput(new TextPerception(this, narsese, narseseParser), text);
        final TextInput i = new TextInput(textPerception, text);
        //final TextInput i = new TextInput.CachingTextInput(textPerception, text);
        input(i);
        return i;
    }

    public NAR input(final String taskText, final float frequency, final float confidence) throws InvalidInputException {
        return input(-1, -1, taskText, frequency, confidence);
    }

    public <S extends Term, T extends S> T term(final String t) throws InvalidInputException {
        return narsese.parseTermNormalized(t);
    }

    public Concept concept(final Term term) {
        return memory.concept(term);
    }

    /**
     * gets a concept if it exists, or returns null if it does not
     */
    public Concept concept(final String conceptTerm) throws InvalidInputException {
        return concept((Term) narsese.parseTermNormalized(conceptTerm));
    }


    public Task goal(final String goalTerm, final float freq, final float conf) {
        return goal(Global.DEFAULT_GOAL_PRIORITY, Global.DEFAULT_GOAL_DURABILITY, goalTerm, freq, conf);
    }


    public Task ask(String termString) throws InvalidInputException {
        //TODO remove '?' if it is attached at end
        return ask(termString, Symbols.QUESTION);
    }

    public Task quest(String questString) throws InvalidInputException {
        return ask(questString, Symbols.QUEST);
    }

    public Task goal(float pri, float dur, String goalTerm, float freq, float conf) throws InvalidInputException {
        final Task t;
        final Truth tv;
        input(
                t = new Task(

                        narsese.parseCompoundNormalized(goalTerm),
                        Symbols.GOAL,
                        tv = new DefaultTruth(freq, conf),


                        pri,
                        dur, BudgetFunctions.truthToQuality(tv)

                )
        );
        return t;
    }

    public Task believe(float priority, String termString, long when, float freq, float conf) throws InvalidInputException {
        return believe(priority, Global.DEFAULT_JUDGMENT_DURABILITY, termString, when, freq, conf);
    }

    public Task believe(float priority, Compound term, long when, float freq, float conf) throws InvalidInputException {
        return believe(priority, Global.DEFAULT_JUDGMENT_DURABILITY, term, when, freq, conf);
    }

    public Task believe(String termString, Tense tense, float freq, float conf) throws InvalidInputException {
        return believe(Global.DEFAULT_JUDGMENT_PRIORITY, Global.DEFAULT_JUDGMENT_DURABILITY, term(termString), tense, freq, conf);
    }

    public Task believe(Compound term, float freq, float conf) throws InvalidInputException {
        return believe(Global.DEFAULT_JUDGMENT_PRIORITY, Global.DEFAULT_JUDGMENT_DURABILITY, term, Tense.Eternal, freq, conf);
    }

    public Task believe(String termString, float freq, float conf) throws InvalidInputException {
        return believe((Compound) term(termString), freq, conf);
    }

    public Task believe(String termString, float conf) throws InvalidInputException {
        return believe(termString, 1.0f, conf);
    }

    public Task believe(String termString) throws InvalidInputException {
        return believe(termString, 1.0f, Global.DEFAULT_JUDGMENT_CONFIDENCE);
    }

    public Task believe(Compound term) throws InvalidInputException {
        return believe(term, 1.0f, Global.DEFAULT_JUDGMENT_CONFIDENCE);
    }

    public Task believe(float pri, float dur, Compound beliefTerm, Tense tense, float freq, float conf) throws InvalidInputException {
        return believe(pri, dur, beliefTerm, Stamp.getOccurrenceTime(time(), tense, memory.duration()), freq, conf);
    }

    public Task believe(float pri, float dur, String beliefTerm, long occurrenceTime, float freq, float conf) throws InvalidInputException {
        return believe(pri, dur, (Compound) term(beliefTerm), occurrenceTime, freq, conf);
    }

    public Task believe(float pri, float dur, Compound belief, long occurrenceTime, float freq, float conf) throws InvalidInputException {

        final Truth tv;

        Task t = new Task(belief,
                Symbols.JUDGMENT,
                tv = new DefaultTruth(freq, conf),
                pri, dur, BudgetFunctions.truthToQuality(tv));
        t.setOccurrenceTime(occurrenceTime);

        input(t);

        return t;
    }

    public Task ask(String termString, char questionOrQuest) throws InvalidInputException {


        //TODO use input method like believe uses which avoids creation of redundant Budget instance

        final Task t;
        input(
                t = new Task(
                        narsese.parseCompoundNormalized(termString),
                        questionOrQuest,
                        null,
                        Global.DEFAULT_QUESTION_PRIORITY,
                        Global.DEFAULT_QUESTION_DURABILITY,
                        1)
        );

        return t;

        //ex: return new Answered(this, t);

    }

    public NAR input(float priority, float durability, final String taskText, float frequency, float confidence) throws InvalidInputException {

        narsese.parse(taskText, t -> {
            if (frequency != -1)
                t.sentence.truth.setFrequency(frequency);
            if (confidence != -1)
                t.sentence.truth.setConfidence(confidence);
            if (priority != -1)
                t.setPriority(priority);
            if (durability != -1)
                t.setDurability(durability);

            input(t);
        });

        return this;
    }

    protected void preprocessInput(Task t) {
        if (t.getCreationTime() == Stamp.TIMELESS)
            t.setCreationTime(time());
        if (t.getEvidentialSet() == null)
            t.setEvidentialSet(memory.newStampSerial());
    }

    /** input a task via perception buffers */
    public NAR input(final Task t) {
        preprocessInput(t);
        input((Input) t);
        return this;
    }

    public NAR inputDirect(final TaskSeed t) {
        return inputDirect(t.get());
    }

    /** input a task via direct TaskProcessing */
    public NAR inputDirect(final Task t) {
        preprocessInput(t);
        TaskProcess.run(this, t);
        return this;
    }


    /**
     * attach event handler to one or more event (classes)
     */
    public EventEmitter.Registrations on(Reaction<Class> o, Class... c) {
        return memory.event.on(o, c);
    }


    public EventEmitter.Registrations on(Class<? extends Reaction<Class>> c) {
        Reaction<Class> v = the(c);
        the(c, v); //register singleton
        return on(v);
    }

    public EventEmitter.Registrations on(Reaction<Term> o, Term... c) {
        return memory.exe.on(o, c);
    }

    public EventEmitter.Registrations on(Operator o) {
        Term a = o.getTerm();
        EventEmitter.Registrations reg = on(o, a);
        o.setEnabled(this, true);
        return reg;
    }

    /**
     * activate a concept builder
     */
    public void on(ConceptBuilder c) {
        memory.on(c);
    }

    /**
     * deactivate a concept builder
     */
    public void off(ConceptBuilder c) {
        memory.off(c);
    }

    @Deprecated
    public int getCyclesPerFrame() {
        return cyclesPerFrame;
    }

    @Deprecated
    public void setCyclesPerFrame(int cyclesPerFrame) {
        this.cyclesPerFrame = cyclesPerFrame;
    }


    /**
     * Adds an input channel for input from an external sense / sensor.
     * Will remain added until it closes or it is explicitly removed.
     */
    public Input input(final Input ii) {
        memory.perception.accept(ii);
        return ii;
    }

//    /** Explicitly removes an input channel and notifies it, via Input.finished(true) that is has been removed */
//    public Input removeInput(Input channel) {
//        inputChannels.remove(channel);
//        channel.finished(true);
//        return channel;
//    }

//
//    /** add and enable a plugin or operate */
//    public OperatorRegistration on(IOperator p) {
//        if (p instanceof Operator) {
//            memory.operatorAdd((Operator) p);
//        }
//        OperatorRegistration ps = new OperatorRegistration(p);
//        plugins.add(ps);
//        return ps;
//    }
//
//    /** disable and remove a plugin or operate; use the PluginState instance returned by on(plugin) to .off() it */
//    protected void off(OperatorRegistration ps) {
//        if (plugins.remove(ps)) {
//            IOperator p = ps.IOperator;
//            if (p instanceof Operator) {
//                memory.operatorRemove((Operator) p);
//            }
//            ps.setEnabled(false);
//        }
//    }
//
//    public List<OperatorRegistration> getPlugins() {
//        return Collections.unmodifiableList(plugins);
//    }


    @Deprecated
    public void start(final long minCyclePeriodMS, int cyclesPerFrame) {
        throw new RuntimeException("WARNING: this threading model is not safe and deprecated");

//        if (isRunning()) stop();
//        this.minFramePeriodMS = minCyclePeriodMS;
//        this.cyclesPerFrame = cyclesPerFrame;
//        running = true;
//        if (thread == null) {
//            thread = new Thread(this, this.toString() + "_reasoner");
//            thread.start();
//        }
    }

    /**
     * Repeatedly execute NARS working cycle in a new thread with Iterative timing.
     *
     * @param minCyclePeriodMS minimum cycle period (milliseconds).
     */
    @Deprecated
    public void start(final long minCyclePeriodMS) {
        start(minCyclePeriodMS, getCyclesPerFrame());
    }


    public EventEmitter event() {
        return memory.event;
    }


    /**
     * Exits an iteration loop if running
     */
    public void stop() {
        running = false;
        enabled = false;
    }

    /**
     * steps 1 frame forward. cyclesPerFrame determines how many cycles this frame consists of
     */
    public void frame() {
        frame(1);
    }

    /**
     * Runs multiple frames, unless already running (then it return -1).
     *
     * @return total time in seconds elapsed in realtime
     */
    public synchronized void frame(final int frames) {

        final boolean wasRunning = running;

        running = true;

        final int cpf = cyclesPerFrame;
        for (int f = 0; (f < frames) && running; f++) {
            frameCycles(cpf);
        }

        running = wasRunning;

    }

    /**
     * Execute a minimum number of cycles, allowing additional cycles (less than maxCycles) for finishing any pending inputs
     *
     * @param maxCycles max cycles, or -1 to allow any number of additional cycles until input finishes
     */
    public NAR runWhileNewInput(long minCycles, long maxCycles) {


        if (maxCycles <= 0) return this;
        if (minCycles > maxCycles)
            throw new RuntimeException("minCycles " + minCycles + " required <= maxCycles " + maxCycles);

        running = true;

        long cycleStart = time();
        do {
            frame(1);

            long now = time();

            long elapsed = now - cycleStart;

            if (elapsed >= minCycles)
                running = (!memory.perception.isEmpty()) &&
                        (elapsed < maxCycles);
        }
        while (running);

        return this;
    }

    /**
     * Execute a fixed number of cycles, then finish any remaining walking steps.
     */
    public NAR runWhileNewInput(long extraCycles) {
        //TODO see if this entire method can be implemented as run(0, cycles);

        if (extraCycles <= 0) return this;

        running = true;
        enabled = true;

        //clear existing input

        long cycleStart = time();
        do {
            frame(1);
        }
        while ((!memory.perception.isEmpty()) && running && enabled);

        long cyclesCompleted = time() - cycleStart;

        //queue additional cycles, 
        extraCycles -= cyclesCompleted;
        if (extraCycles > 0)
            memory.think(extraCycles);

        //finish all remaining cycles
        while (!memory.perceiving() && running && enabled) {
            frame(1);
        }

        running = false;

        return this;
    }


    /**
     * Run until stopped, at full speed
     */
    public void run() {
        runAtRate(0);
    }

    /**
     * Runs until stopped, at a given delay period between frames (0= no delay). Main loop
     */
    public void runAtRate(long minFramePeriodMS) {
        //TODO use DescriptiveStatistics to track history of frametimes to slow down (to decrease speed rate away from desired) or speed up (to reach desired framerate).  current method is too nervous, it should use a rolling average

        running = true;

        while (running) {


            long start = System.currentTimeMillis();

            frame(1); //in seconds

            long frameTimeMS = System.currentTimeMillis() - start;

            if (minFramePeriodMS > 0) {

                double remainingTime = (minFramePeriodMS - frameTimeMS) / 1.0E3;
                if (remainingTime > 0) {
                    try {
                        Thread.sleep(minFramePeriodMS);
                    } catch (InterruptedException ee) {
                        //error(ee);
                    }
                } else if (remainingTime < 0) {
                    minFramePeriodMS++;
                    System.err.println("Expected framerate not achieved: " + remainingTime + "ms too slow; incresing frame period to " + minFramePeriodMS + "ms");
                }
            } else if (threadYield) {
                Thread.yield();
            }
        }
    }


    /**
     * returns the configured NAL level
     */
    public int nal() {
        return memory.nal();
    }


    public void emit(final Class c) {
        memory.emit(c);
    }

    public void emit(final Class c, final Object... o) {
        memory.emit(c, o);
    }


    protected void error(Throwable e) {
        memory.error(e);
    }


    /**
     * enable/disable all I/O and memory processing. CycleStart and CycleStop
     * events will continue to be generated, allowing the memory to be used as a
     * clock tick while disabled.
     */
    public void enable(boolean e) {
        this.enabled = e;
    }

    public boolean isEnabled() {
        return enabled;
    }


    /**
     * executes one complete memory cycle (if not disabled)
     */
    protected void cycle(final boolean newFrame) {
        if (isEnabled()) {
            memory.cycle();
        }
    }

    /**
     * A frame, consisting of one or more NAR memory cycles
     */
    protected void frameCycles(final int cycles) {

        if (memory.resource!=null)
            memory.resource.FRAME_DURATION.start();

        memory.clock.preFrame(memory);

        emit(FrameStart.class);

        //try {
        for (int i = 0; i < cycles; i++)
            cycle(i == 0);
        /*}
        catch (Throwable e) {
            Throwable c = e.getCause();
            if (c == null) c = e;
            error(c);
        }*/

        emit(FrameEnd.class);

    }

    @Override
    public String toString() {
        return "NAR[" + memory.toString() + "]";
    }

    /**
     * Get the current time from the clock Called in {@link nars.logic.entity.stamp.Stamp}
     *
     * @return The current time
     */
    public long time() {
        return memory.time();
    }


    public boolean isRunning() {
        return running;
    }


    /**
     * When b is true, NAR will call Thread.yield each run() iteration that minCyclePeriodMS==0 (no delay).
     * This is for improving program responsiveness when NAR is run with no delay.
     */
    public void setThreadYield(boolean b) {
        this.threadYield = b;
    }


    /**
     * create a NAR given the class of a Build.  its default constructor will be used
     */
    public static NAR build(Class<? extends NARSeed> g) {
        try {
            return new NAR(g.newInstance());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * normal way to construct a NAR, using a particular Build instance
     */
    public NAR(NARSeed b) {
        this(b.newMemory(b, b.getLogicPolicy()));
        b.init(this);
    }

    /**
     * returns the Atom for the given string. since the atom is unique to itself it can be considered 'the' the
     */
    public Atom atom(final String s) {
        return memory.the(s);
    }

    public void runWhileNewInput() {
        runWhileNewInput(0);
    }


//    private void debugTime() {
//        //if (running || stepsQueued > 0 || !finishedInputs) {
//            System.out.println("// doTick: "
//                    //+ "walkingSteps " + stepsQueued
//                    + ", clock " + time());
//
//            System.out.flush();
//        //}
//    }

}