package net.aufdemrand.denizen.utilities;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.interfaces.dExternal;
import org.abstractmeta.toolbox.compilation.compiler.JavaSourceCompiler;
import org.abstractmeta.toolbox.compilation.compiler.impl.JavaSourceCompilerImpl;
import org.bukkit.ChatColor;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class RuntimeCompiler {

    Denizen denizen;

    public RuntimeCompiler(Denizen denizen) {
        this.denizen = denizen;
        this.externalsFolder = new File(denizen.getDataFolder() + File.separator + "externals");
    }

    private final File externalsFolder;
    private final static File pluginsFolder = new File("plugins");

    private final static FilenameFilter jarFilter = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
            return (name.toLowerCase().endsWith(".jar"));
        }
    };

    private final static FilenameFilter javaFilter = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
            return (name.toLowerCase().endsWith(".java"));
        }
    };

    public static ArrayList<dExternal> loadedExternals = new ArrayList<dExternal>();
    List<String> dependencies;

    @SuppressWarnings("unchecked")
    public void loader() {

        dB.log("Loading external dependencies for run-time compiler.");
        dependencies = new ArrayList<String>();
        try {
            File file = new File(denizen.getDataFolder() + File.separator + "externals" + File.separator + "dependencies");
            for (File f : file.listFiles(jarFilter)) {
                dependencies.add(f.getPath());
                dB.log("Loaded  " + f.getName());
            }
        }
        catch (Exception error) {
            dB.log("No dependencies to load or error loading dependencies: " + error.getMessage());
        }
        dB.log("Loading plugins as dependencies for run-time compiler.");
        for (File f : pluginsFolder.listFiles(jarFilter)) {
            dependencies.add(f.getPath());
            dB.log("Loaded  " + f.getName());
        }
        dB.log("Attempting to add CraftBukkit as dependency for run-time compiler.");
        try {
            File file = new File(".");
            for (File f : file.listFiles(jarFilter)) {
                dependencies.add(f.getPath());
                dB.log("Loaded  " + f.getName());
            }
        }
        catch (Exception error) {
            dB.log("Could not find CraftBukkit jar or error reading it: " + error.getMessage());
        }

        if (!externalsFolder.exists() || externalsFolder.list().length == 0) {
            return;
        }
        try {
            File file = new File(denizen.getDataFolder() + File.separator + "externals");
            File[] files = file.listFiles(javaFilter);
            if (files != null && files.length > 0) {
                for (File f : files) {
                    String fileName = f.getName();

                    dB.log("Processing '" + fileName + "'... ");

                    JavaSourceCompiler javaSourceCompiler = new JavaSourceCompilerImpl();
                    JavaSourceCompiler.CompilationUnit compilationUnit = javaSourceCompiler.createCompilationUnit();
                    if (!dependencies.isEmpty()) {
                        compilationUnit.addClassPathEntries(dependencies);
                    }

                    try {
                        compilationUnit.addJavaSource(fileName.replace(".java", ""), readFile(f.getAbsolutePath()));
                        ClassLoader classLoader = javaSourceCompiler.compile(compilationUnit);
                        Class<dExternal> load = (Class<dExternal>) classLoader.loadClass(fileName.replace(".java", ""));
                        dExternal loadedClass = load.newInstance();
                        loadedClass.load();
                        loadedExternals.add(loadedClass);
                    }
                    catch (Exception e) {
                        if (e instanceof IllegalStateException) {
                            dB.echoError("No JDK found! External .java files will not be loaded.");
                            dB.echoError(e);
                        }
                        else {
                            dB.echoError(ChatColor.RED + "Error compiling " + fileName + "!");
                            dB.echoError(e);
                        }
                    }
                }
                dB.echoApproval("All externals loaded!");
            }
        }
        catch (Exception error) {
            dB.echoError(error);
        }
    }

    public int i = 0;

    public void runString(String input) {
        try {
            i++;
            JavaSourceCompiler javaSourceCompiler = new JavaSourceCompilerImpl();
            JavaSourceCompiler.CompilationUnit compilationUnit = javaSourceCompiler.createCompilationUnit();
            if (!dependencies.isEmpty()) {
                compilationUnit.addClassPathEntries(dependencies);
            }
            String complete = "import net.aufdemrand.denizen.utilities.debugging.dB;\npublic class CodeTester" + i +
                    " extends net.aufdemrand.denizen.utilities.dExternalRunnable {\n"
                    + "public void unload() {}\npublic void load() {}\n"
                    + "public void run() {\n" + input + "\n}\n}";
            compilationUnit.addJavaSource("CodeTester" + i, complete);
            ClassLoader classLoader = javaSourceCompiler.compile(compilationUnit);
            Class<? extends dExternalRunnable> load = (Class<? extends dExternalRunnable>) classLoader.loadClass("CodeTester" + i);
            dExternalRunnable loadedClass = load.newInstance();
            loadedClass.load();
            loadedClass.run();
            loadedClass.unload();
        }
        catch (Exception e) {
            dB.echoError(e);
        }
    }

    public void reload() {
        for (dExternal external : loadedExternals) {
            external.unload();
        }
        loadedExternals.clear();
        loader();
    }

    private String readFile(String file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = null;
        StringBuilder stringBuilder = new StringBuilder();
        String ls = System.getProperty("line.separator");
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
            stringBuilder.append(ls);
        }
        reader.close();
        return stringBuilder.toString();
    }
}
