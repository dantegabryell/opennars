/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.io.in;

import nars.NAR;
import nars.NARSeed;
import nars.io.TextPerception;
import nars.meter.condition.OutputCondition;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Access to library of examples/unit tests
 * TODO use getClass().getResource ?
 */
public class LibraryInput extends TextInput {

    public static final String[] directories =
            new String[] {
                    "test1", "test2", "test3", "test4", "test4/depr", "test5", "test5/depr", "test6", "test7", "test8",
                    "other",
                    "app/testchamber", "app/pattern_matching1", "app/metacat"
            };




    public static Iterable<String> getUnitTestPaths() {
        return getUnitTests().values();
        //Collection<String> v = getUnitTests().values();
        /*return Iterables.transform(v, new Function<String, String>() {

            @Override
            public String apply(String f) {
                return (String)((Object[])f)[0];
            }
            
        });*/
    }


    protected LibraryInput(TextPerception t, String path) throws IOException {
        super(t, FileInput.load(path));

    }
    
    public static LibraryInput get(NAR n, String id) throws Exception {
        if (!id.endsWith(".nal"))
            id = id + ".nal";

        String path = getExamplePath(id);
                
        return new LibraryInput(n.textPerception, path);
    }

    final static String cwd;

    static {
        Path currentRelativePath = Paths.get("");
        cwd = currentRelativePath.toAbsolutePath().toString();
    }

    public static String getExamplePath(String path) {
        if (path.startsWith("/")) return path; //dont modify, it's already absolute
        if (cwd.endsWith("nars_logic") || cwd.endsWith("nars_lab"))
            return "../nal/" + path;
        else
            return "nal/" + path;
    }
    
    public List<OutputCondition> enableConditions(NAR n, int similarResultsToSave) {
        return OutputCondition.getConditions(n, input, similarResultsToSave);
    }

    public static Map<String,String> getUnitTests() {
        return getUnitTests("test", "Examples/DecisionMaking", "Examples/ClassicalConditioning");
    }


    public static Map<String,String> getUnitTests(String... directories) {
        Map<String,String> l = new TreeMap();

        for (String dir : directories ) {

            String se = getExamplePath(dir);
            File folder = new File(se);
            File[] files = folder.listFiles();
            if (files == null) {
                System.err.println(folder.getAbsoluteFile() + " is not a directory or does not exist");
                break;
            }

            File[] ff = folder.listFiles();
            if (ff == null)
                throw new RuntimeException(se + " not found");

            for (final File file : ff) {
                if (file.isDirectory() || file.getName().equals("README.txt") || file.getName().contains(".png"))
                    continue;
                if(!("extra".equals(file.getName()))) {
                    l.put(file.getName(), file.getAbsolutePath() );
                }
            }
            
        }
        return l;
    }

    public String getSource() {
        return input;
    }

    protected static Map<String, String> examples = new HashMap(); //path -> script data

    public static String getExample(String path) {
        try {
            String existing = examples.get(path);
            if (existing!=null)
                return existing;

            existing = FileInput.load(path);

            examples.put(path, existing);
            return existing;
        } catch (Exception ex) {
            throw new RuntimeException("Example file not found: " + path + ": " + ex.toString()  + ": " + " ./=" + new File(".").getAbsolutePath());
        }
    }

    public static Collection<String> getPaths(String... directories) {
        Map<String, String> et = LibraryInput.getUnitTests(directories);
        Collection<String> t = et.values();
        return t;
    }

    public static Collection getParams(String directories, NARSeed... builds) {
        return getParams(new String[] { directories }, builds);
    }

    //@Parameterized.Parameters(name="{1} {0}")
    public static Collection getParams(String[] directories, NARSeed... builds) {
        Collection<String> t = getPaths(directories);

        Collection<Object[]> params = new ArrayList(t.size() * builds.length);
        for (String script : t) {
            for (NARSeed b : builds) {
                params.add(new Object[] { b, script });
            }
        }
        return params;
    }

    public static Map<String, String> getAllExamples() {
        return getUnitTests(directories);
    }
}
