package net.aufdemrand.denizen.objects.notable;

import net.aufdemrand.denizen.objects.dObject;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;


public class NotableManager {

    public NotableManager() {
        try { _initialize(); } catch (IOException e) {



        } catch (ClassNotFoundException e) {



        }
    }


    public static Map<String, Notable>
            notableObjects = new ConcurrentHashMap<String, Notable>();
    public static Map<String, Class>
            typeTracker    = new ConcurrentHashMap<String, Class>();
    public static Map<Notable, String>
            reverseObjects = new ConcurrentHashMap<Notable, String>();


    public static boolean isSaved(String id) {
        return notableObjects.containsKey(id.toLowerCase());
    }


    public static boolean isSaved(Notable object) {
        return reverseObjects.containsKey(object);
    }


    public static Notable getSavedObject(String id) {
        if (notableObjects.containsKey(id.toLowerCase()))
            return notableObjects.get(id.toLowerCase());
        else return null;
    }


    public static String getSavedId(Notable object) {
        if (reverseObjects.containsKey(object))
            return reverseObjects.get(object);
        return null;
    }


    public static boolean isType(String id, Class type) {
        return (typeTracker.containsKey(id.toLowerCase())) && typeTracker.get(id.toLowerCase()) == type;
    }


    public static void saveAs(Notable object, String id) {
        if (object == null) return;
        notableObjects.put(id.toLowerCase(), object);
        reverseObjects.put(object, id.toLowerCase());
        typeTracker.put(id.toLowerCase(), object.getClass());
    }


    public static void remove(String id) {
        Notable obj = notableObjects.get(id.toLowerCase());
        notableObjects.remove(id.toLowerCase());
        reverseObjects.remove(obj);
        typeTracker.remove(id.toLowerCase());
    }

    public static void remove(Notable obj) {
        String id = reverseObjects.get(obj);
        notableObjects.remove(id.toLowerCase());
        reverseObjects.remove(obj);
        typeTracker.remove(id.toLowerCase());
    }

    public static List<dObject> getAllType(Class<? extends dObject> type) {
        List<dObject> objects = new ArrayList<dObject>();
        for (Map.Entry<String, Notable> notable : notableObjects.entrySet()) {
            // dB.log(notable.toString());
            if (isType(notable.getKey(), type))
                objects.add((dObject) notable.getValue());
        }

        return objects;
    }



    /*
     * Called on '/denizen reload notables'.
     */
    private static void _recallNotables() {

        // Find each type of notable
        for (String keys : DenizenAPI.getCurrentInstance().notableManager().getNotables().getKeys(false)) {



        }

    }

    /*
     * Called on by '/denizen save'.
     */
    private static void _saveNotables() {

        for (Map.Entry<String, Notable> notable : notableObjects.entrySet()) {

            // If the object is serializable, save that info... fetching the objects back
            // will require this information
            if (notable.getValue().getSaveObject() instanceof ConfigurationSerializable)
                DenizenAPI.getCurrentInstance().notableManager().getNotables()
                        .set(((dObject) notable.getValue()).getObjectType().toLowerCase() + "." + "_serializable", true);

            DenizenAPI.getCurrentInstance().notableManager().getNotables()
                    .set(((dObject) notable.getValue()).getObjectType().toLowerCase() + "." + notable.getKey().toLowerCase(),
                            notable.getValue());
        }

    }


    /*
     * Reloads, retrieves and saves notable information from/to 'notables.yml'.
     */

    private FileConfiguration notablesSave = null;
    private File notablesFile = null;

    public void reloadNotables() {
        if (notablesFile == null) {
            notablesFile = new File(DenizenAPI.getCurrentInstance().getDataFolder(), "notables.yml");
        }
        notablesSave = YamlConfiguration.loadConfiguration(notablesFile);
        // Reload notables from notables.yml
        _recallNotables();
    }

    public FileConfiguration getNotables() {
        if (notablesSave == null) {
            reloadNotables();
        }
        return notablesSave;
    }

    public void saveNotables() {
        if (notablesSave == null || notablesFile == null) {
            return;
        }
        try {
            // Save notables to notables.yml
            _saveNotables();
            notablesSave.save(notablesFile);
        } catch (IOException ex) {
            Logger.getLogger(JavaPlugin.class.getName()).log(Level.SEVERE, "Could not save to " + notablesFile, ex);
        }
    }



    ///////////////////
    // Note Annotation Handler
    ///////////////////


    private static Map<Class, String> objects = new HashMap<Class, String>();

    public static void _initialize() throws IOException, ClassNotFoundException {
        objects.clear();

        for (Class dClass : notable_objects)
            for (Method method : dClass.getMethods())
                if (method.isAnnotationPresent(net.aufdemrand.denizen.objects.notable.Note.class)) {
                    String[] identifiers = method.getAnnotation(net.aufdemrand.denizen.objects.notable.Note.class).value().split(",");
                    for (String identifer : identifiers)
                        objects.put(dClass, identifer.trim().toLowerCase());
                }
    }

    private static ArrayList<Class> notable_objects = new ArrayList<Class>();

    public static void registerWithNotableManager(Class notable) {
        notable_objects.add(notable);
    }

    public static boolean canFetch(Class notable) {
        return objects.containsKey(notable);
    }

    public static String getClassId(Class notable) {
        if (canFetch(notable))
            return objects.get(notable).toLowerCase();
        else
            return null;
    }

}
