package net.aufdemrand.denizen.utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.interfaces.dExternal;
import net.aufdemrand.denizen.utilities.debugging.dB;

import org.abstractmeta.toolbox.compilation.compiler.JavaSourceCompiler;
import org.abstractmeta.toolbox.compilation.compiler.impl.JavaSourceCompilerImpl;
import org.bukkit.ChatColor;

public class RuntimeCompiler {

    Denizen denizen;

    public RuntimeCompiler(Denizen denizen) {
        this.denizen = denizen;
    }

    @SuppressWarnings("unchecked")
    public void loader() {

        List<String> dependencies = new ArrayList<String>();
        dB.echoDebug("Loading external dependencies for run-time compiler.");
        try {
            File file = new File(denizen.getDataFolder() + File.separator + "externals" + File.separator + "dependencies");
            File[] files = file.listFiles();
            if (files.length > 0){
                for (File f : files){
                    String fileName = f.getName();
                    if (fileName.substring(fileName.lastIndexOf('.') + 1).equalsIgnoreCase("JAR")) {
                        dependencies.add(f.getPath());
                        dB.echoDebug("Loaded  " + f.getName());
                    }
                }
            }   
        } catch (Exception error) { dB.log("No dependencies to load."); }

        try {
            File file = new File(denizen.getDataFolder() + File.separator + "externals");
            File[] files = file.listFiles();
            if (files.length > 0){
                for (File f : files){
                    String fileName = f.getName();

                    if (fileName.substring(fileName.lastIndexOf('.') + 1).equalsIgnoreCase("JAVA") && !fileName.startsWith(".")) {
                        dB.echoDebug("Processing '" + fileName + "'... ");

                        JavaSourceCompiler javaSourceCompiler = new JavaSourceCompilerImpl();
                        JavaSourceCompiler.CompilationUnit compilationUnit = javaSourceCompiler.createCompilationUnit();
                        if (!dependencies.isEmpty()) compilationUnit.addClassPathEntries(dependencies);

                        try {
                            compilationUnit.addJavaSource(fileName.replace(".java", ""), readFile(f.getAbsolutePath()));
                            ClassLoader classLoader = javaSourceCompiler.compile(compilationUnit);
                            Class<dExternal> load = (Class<dExternal>) classLoader.loadClass(fileName.replace(".java", ""));
                            dExternal loadedClass = load.newInstance();
                            loadedClass.load();
                        } catch (Exception e) {
                            if (e instanceof IllegalStateException)
                                dB.echoError("No JDK found! External .java files will not be loaded.");
                            else {
                                dB.echoError(ChatColor.RED + "Woah! Error compiling " + fileName + "!");
                                e.printStackTrace();
                            }
                        }
                    } 
                }
                dB.echoApproval("All externals loaded!");
            } else dB.echoError("Woah! No externals in /plugins/Denizen/externals/.../ to load!");  
        } catch (Exception error) { /* No externals */ }
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