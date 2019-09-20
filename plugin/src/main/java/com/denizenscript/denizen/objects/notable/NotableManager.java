package com.denizenscript.denizen.objects.notable;

import com.denizenscript.denizen.objects.*;
import com.denizenscript.denizen.utilities.DenizenAPI;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizencore.objects.ObjectFetcher;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.notable.Notable;
import com.denizenscript.denizencore.objects.notable.Note;
import com.denizenscript.denizencore.tags.core.EscapeTagBase;
import com.denizenscript.denizencore.utilities.CoreUtilities;
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
        registerWithNotableManager(CuboidTag.class);
        registerWithNotableManager(EllipsoidTag.class);
        registerWithNotableManager(InventoryTag.class);
        registerWithNotableManager(ItemTag.class);
        registerWithNotableManager(LocationTag.class);
    }


    public static Map<String, Notable>
            notableObjects = new ConcurrentHashMap<>();
    public static Map<String, Class>
            typeTracker = new ConcurrentHashMap<>();
    public static Map<Notable, String>
            reverseObjects = new ConcurrentHashMap<>();


    public static boolean isSaved(String id) {
        return notableObjects.containsKey(CoreUtilities.toLowerCase(id));
    }


    public static boolean isSaved(Notable object) {
        return reverseObjects.containsKey(object);
    }


    public static boolean isExactSavedObject(Notable object) {
        String id = reverseObjects.get(object);
        if (id == null) {
            return false;
        }
        // Reference equality
        return notableObjects.get(id) == object;
    }

    public static Notable getSavedObject(String id) {
        if (notableObjects.containsKey(CoreUtilities.toLowerCase(id))) {
            return notableObjects.get(CoreUtilities.toLowerCase(id));
        }
        else {
            return null;
        }
    }


    public static String getSavedId(Notable object) {
        if (reverseObjects.containsKey(object)) {
            return reverseObjects.get(object);
        }
        return null;
    }


    public static boolean isType(String id, Class type) {
        return (typeTracker.containsKey(CoreUtilities.toLowerCase(id)))
                && typeTracker.get(CoreUtilities.toLowerCase(id)) == type;
    }


    public static void saveAs(Notable object, String id) {
        if (object == null) {
            return;
        }
        notableObjects.put(CoreUtilities.toLowerCase(id), object);
        reverseObjects.put(object, CoreUtilities.toLowerCase(id));
        typeTracker.put(CoreUtilities.toLowerCase(id), object.getClass());
    }


    public static void remove(String id) {
        Notable obj = notableObjects.get(CoreUtilities.toLowerCase(id));
        notableObjects.remove(CoreUtilities.toLowerCase(id));
        reverseObjects.remove(obj);
        typeTracker.remove(CoreUtilities.toLowerCase(id));
    }

    public static void remove(Notable obj) {
        String id = reverseObjects.get(obj);
        notableObjects.remove(CoreUtilities.toLowerCase(id));
        reverseObjects.remove(obj);
        typeTracker.remove(CoreUtilities.toLowerCase(id));
    }

    public static <T extends ObjectTag> List<T> getAllType(Class<T> type) {
        List<T> objects = new ArrayList<>();
        for (Map.Entry<String, Notable> notable : notableObjects.entrySet()) {
            // dB.log(notable.toString());
            if (isType(notable.getKey(), type)) {
                objects.add((T) notable.getValue());
            }
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

            Class<? extends ObjectTag> clazz = reverse_objects.get(key);

            ConfigurationSection section = DenizenAPI.getCurrentInstance().notableManager().getNotables()
                    .getConfigurationSection(key);

            if (section == null) {
                continue;
            }

            for (String notableRaw : section.getKeys(false)) {
                String notable = EscapeTagBase.unEscape(notableRaw.replace("DOT", "."));
                Notable obj = (Notable) ObjectFetcher.getObjectFrom(clazz, section.getString(notableRaw), CoreUtilities.noDebugContext);
                if (obj != null) {
                    obj.makeUnique(notable);
                }
                else {
                    Debug.echoError("Notable '" + notable + "' failed to load!");
                }
            }

        }

    }

    /**
     * Called on by '/denizen save'.
     */
    private static void _saveNotables() {

        FileConfiguration notables = DenizenAPI.getCurrentInstance().notableManager().getNotables();
        for (String key : notables.getKeys(false)) {
            notables.set(key, null);
        }

        for (Map.Entry<String, Notable> notable : notableObjects.entrySet()) {

            try {
                notables.set(getClassId(getClass(notable.getValue())) + "." + EscapeTagBase.escape(CoreUtilities.toLowerCase(notable.getKey())),
                        notable.getValue().getSaveObject());
            }
            catch (Exception e) {
                Debug.echoError("Notable '" + notable.getKey() + "' failed to save!");
                Debug.echoError(e);
            }
        }
    }

    private static <T extends Notable> Class<T> getClass(Notable notable) {
        for (Class clazz : objects.keySet()) {
            if (clazz.isInstance(notable)) {
                return clazz;
            }
        }
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
        }
        catch (IOException ex) {
            Logger.getLogger(JavaPlugin.class.getName()).log(Level.SEVERE, "Could not save to " + notablesFile, ex);
        }
    }

    ///////////////////
    // Note Annotation Handler
    ///////////////////


    private static Map<Class, String> objects = new HashMap<>();
    private static Map<String, Class> reverse_objects = new HashMap<>();

    public static void registerWithNotableManager(Class notable) {
        for (Method method : notable.getMethods()) {
            if (method.isAnnotationPresent(Note.class)) {
                String note = method.getAnnotation(Note.class).value();
                objects.put(notable, note);
                reverse_objects.put(note, notable);
            }
        }
    }

    public static boolean canFetch(Class notable) {
        return objects.containsKey(notable);
    }

    public static String getClassId(Class notable) {
        if (canFetch(notable)) {
            return objects.get(notable);
        }
        else {
            return null;
        }
    }

    public static Map<String, Class> getReverseClassIdMap() {
        return reverse_objects;
    }
}
