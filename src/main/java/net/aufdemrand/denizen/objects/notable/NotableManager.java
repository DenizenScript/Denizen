package net.aufdemrand.denizen.objects.notable;

import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
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
        registerWithNotableManager(dCuboid.class);
        registerWithNotableManager(dEllipsoid.class);
        registerWithNotableManager(dInventory.class);
        registerWithNotableManager(dItem.class);
        registerWithNotableManager(dLocation.class);
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

    public static <T extends dObject> List<T> getAllType(Class<T> type) {
        List<T> objects = new ArrayList<T>();
        for (Map.Entry<String, Notable> notable : notableObjects.entrySet()) {
            // dB.log(notable.toString());
            if (isType(notable.getKey(), type))
                objects.add((T) notable.getValue());
        }

        return objects;
    }

    /**
     * Called on '/denizen reload notables'.
     */
    private static void _recallNotables() {

        notableObjects.clear();
        typeTracker.clear();
        reverseObjects.clear();

        // Find each type of notable
        for (String key : DenizenAPI.getCurrentInstance().notableManager().getNotables().getKeys(false)) {

            Class<? extends dObject> clazz = reverse_objects.get(key);

            ConfigurationSection section = DenizenAPI.getCurrentInstance().notableManager().getNotables()
                    .getConfigurationSection(key);

            if (section == null)
                continue;

            for (String notable : section.getKeys(false)) {
                Notable obj = (Notable) ObjectFetcher.getObjectFrom(clazz, section.getString(notable));
                if (obj != null) {
                    obj.makeUnique(notable.replace("DOT", "."));
                }
                else {
                    dB.echoError("Notable '" + section.getString(notable).replace("DOT", ".") + "' failed to load!");
                }
            }

        }

    }

    /**
     * Called on by '/denizen save'.
     */
    private static void _saveNotables() {

        FileConfiguration notables = DenizenAPI.getCurrentInstance().notableManager().getNotables();
        for (String key: notables.getKeys(false)) {
            notables.set(key, null);
        }

        for (Map.Entry<String, Notable> notable : notableObjects.entrySet()) {

            // If the object is serializable, save that info... fetching the objects back
            // will require this information TODO: make this do something?..
      //      if (notable.getValue().getSaveObject() instanceof ConfigurationSerializable)
      //          DenizenAPI.getCurrentInstance().notableManager().getNotables()
      //                  .set(getClassId(notable.getValue().getClass()) + "." + "_serializable", true);

            notables.set(getClassId(getClass(notable.getValue())) + "." + notable.getKey().toLowerCase().replace(".", "DOT"),
                    notable.getValue().getSaveObject());
        }

    }

    private static <T extends Notable> Class<T> getClass(Notable notable) {
        for (Class clazz : objects.keySet())
            if (clazz.isInstance(notable))
                return clazz;
        return null;
    }

    private FileConfiguration notablesSave = null;
    private File notablesFile = null;

    /**
     * Reloads, retrieves and saves notable information from/to 'notables.yml'.
     */
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
    private static Map<String, Class> reverse_objects = new HashMap<String, Class>();

    public static void registerWithNotableManager(Class notable) {
        for (Method method : notable.getMethods())
            if (method.isAnnotationPresent(Note.class)) {
                String note = method.getAnnotation(Note.class).value();
                objects.put(notable, note);
                reverse_objects.put(note, notable);
            }
    }

    public static boolean canFetch(Class notable) {
        return objects.containsKey(notable);
    }

    public static String getClassId(Class notable) {
        if (canFetch(notable))
            return objects.get(notable);
        else
            return null;
    }

    public static Map<String, Class> getReverseClassIdMap() {
        return reverse_objects;
    }
}
