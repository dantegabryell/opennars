/*
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
package nars.main;

import nars.NAR;
import nars.io.out.TextOutput;
import nars.nar.Default.CommandLineNARBuilder;

import java.io.File;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Scanner;


/**
 * Run Reasoner
 * <p>
 Runs a NAR with addInput. useful for command line or batch functionality; 
 * <p>
 * Manage the internal working thread. Communicate with Reasoner only.
 */
public class NARRun {

    private final NAR nar;

    private boolean logging;
    private PrintStream out = System.out;
    private final static boolean dumpLastState = true;
    int maxTime = 0;


    boolean inputPerLine = true;

    
    /**
     * The entry point of the standalone application.
     * <p>
     * @param args optional argument used : one addInput file
     */
    public static void main(String args[]) {
                
        NARRun nars = new NARRun(new NAR(new CommandLineNARBuilder(args)));
        nars.run(args);
        
        // TODO only if single finish ( no reset in between )
        if (dumpLastState) {
            System.out.println("\n==== Dump Last State ====\n"
                    + nars.nar.toString());
        }
    }

    public NARRun(NAR n) {
        this.nar = n;
    }

    /**
     * non-static equivalent to {@link #main(String[])} : finish to completion from
 an addInput file
     */
    public void run(String args[]) {

        
        if (args.length > 0) {
            try {
                nar.input(new File(args[0]));
            } catch (Exception ex) {
                System.err.println("NARRun.init: " + ex);
            }
        }
        else {
            Scanner sc = new Scanner(System.in);

            if (inputPerLine) {
                new Thread(() -> {
                    readPerLine(sc);
                }).start();
            }
            else {
                readEntirely(sc);
            }
        }

        TextOutput output = new TextOutput(nar, new PrintWriter(out, true));
        output.setShowErrors(true);
        output.setShowInput(true);


        run();
    }

    protected void readPerLine(Scanner sc) {
        while (sc.hasNextLine()) {
            String l = sc.nextLine().trim();
            if (l.isEmpty()) continue;
            try {
                nar.input(l);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected void readEntirely(Scanner sc) {
        String entire = "";
        while (sc.hasNext()) {
            String l = sc.next();
            entire += l;
        }

        nar.input(entire);
    }

    protected void run() {
        while (true) {
            if (logging)
                log("NARSBatch.run():"
                        + " step " + nar.time());

            nar.frame(1);


            if (logging)
                log("NARSBatch.run(): after tick"
                        + " step " + nar.time());

            if (maxTime > 0) {
                if ((nar.memory.perception.isEmpty()) || nar.time() == maxTime) {
                    break;
                }
            }
        }


    }

    public void setPrintStream(PrintStream out) {
        this.out = out;
    }

    private void log(String mess) {
        if (logging) {
            System.out.println("/ " + mess);
        }
    }
    
    
    
    
    


}
