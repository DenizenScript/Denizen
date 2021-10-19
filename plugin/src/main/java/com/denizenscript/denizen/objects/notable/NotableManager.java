package com.denizenscript.denizen.objects.notable;

import com.denizenscript.denizencore.DenizenCore;
import com.denizenscript.denizencore.utilities.YamlConfiguration;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizencore.flags.FlaggableObject;
import com.denizenscript.denizencore.flags.SavableMapFlagTracker;
import com.denizenscript.denizencore.objects.ObjectFetcher;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.notable.Notable;
import com.denizenscript.denizencore.objects.notable.Note;
import com.denizenscript.denizencore.tags.core.EscapeTagBase;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.text.StringHolder;

import java.io.File;
import java.lang.reflect.Method;
import java.util.*;

public class NotableManager {

    public static HashMap<String, Notable> nameToObject = new HashMap<>();
    public static HashMap<Notable, String> objectToName = new HashMap<>();
    public static HashMap<Class, HashSet<Notable>> notesByType = new HashMap<>();

    public static boolean isSaved(Notable object) {
        return objectToName.containsKey(object);
    }

    public static boolean isExactSavedObject(Notable object) {
        String id = objectToName.get(object);
        if (id == null) {
            return false;
        }
        return nameToObject.get(id) == object;
    }

    public static Notable getSavedObject(String id) {
        return nameToObject.get(CoreUtilities.toLowerCase(id));
    }

    public static String getSavedId(Notable object) {
        return objectToName.get(object);
    }

    public static void saveAs(Notable object, String id) {
        if (object == null) {
            return;
        }
        id = CoreUtilities.toLowerCase(id);
        Notable noted = nameToObject.get(id);
        if (noted != null) {
            noted.forget();
        }
        nameToObject.put(id, object);
        objectToName.put(object, id);
        notesByType.get(object.getClass()).add(object);
    }

    public static Notable remove(String id) {
        id = CoreUtilities.toLowerCase(id);
        Notable obj = nameToObject.get(id);
        if (obj == null) {
            return null;
        }
        nameToObject.remove(id);
        objectToName.remove(obj);
        notesByType.get(obj.getClass()).remove(obj);
        return obj;
    }

    public static void remove(Notable obj) {
        String id = objectToName.get(obj);
        nameToObject.remove(id);
        objectToName.remove(obj);
        notesByType.get(obj.getClass()).remove(obj);
    }

    public static <T extends Notable> Set<T> getAllType(Class<T> type) {
        return (Set<T>) notesByType.get(type);
    }

    private static void loadFromConfig() {
        nameToObject.clear();
        for (Set set : notesByType.values()) {
            set.clear();
        }
        objectToName.clear();
        for (StringHolder key : getSaveConfig().getKeys(false)) {
            Class<? extends ObjectTag> clazz = namesToTypes.get(key.str);
            YamlConfiguration section = getSaveConfig().getConfigurationSection(key.str);
            if (section == null) {
                continue;
            }
            for (StringHolder noteNameHolder : section.getKeys(false)) {
                String noteName = noteNameHolder.str;
                String note = EscapeTagBase.unEscape(noteName.replace("DOT", "."));
                String objText;
                String flagText = null;
                Object rawPart = section.get(noteName);
                if (rawPart instanceof YamlConfiguration || rawPart instanceof Map) {
                    objText = section.getConfigurationSection(noteName).getString("object");
                    flagText = section.getConfigurationSection(noteName).getString("flags");
                }
                else {
                    objText = section.getString(noteName);
                }
                Notable obj = (Notable) ObjectFetcher.getObjectFrom(clazz, objText, CoreUtilities.errorButNoDebugContext);
                if (obj != null) {
                    obj.makeUnique(note);
                    if (flagText != null && obj instanceof FlaggableObject) {
                        ((FlaggableObject) getSavedObject(note)).reapplyTracker(new SavableMapFlagTracker(flagText));
                    }
                }
                else {
                    Debug.echoError("Note '" + note + "' failed to load!");
                }
            }
        }
    }

    private static void saveToConfig() {
        YamlConfiguration saveConfig = getSaveConfig();
        for (StringHolder key : saveConfig.getKeys(false)) {
            saveConfig.set(key.str, null);
        }
        for (Map.Entry<String, Notable> note : nameToObject.entrySet()) {
            try {
                saveConfig.set(typesToNames.get(getClass(note.getValue())) + "." + EscapeTagBase.escape(CoreUtilities.toLowerCase(note.getKey())), note.getValue().getSaveObject());
            }
            catch (Exception e) {
                Debug.echoError("Notable '" + note.getKey() + "' failed to save!");
                Debug.echoError(e);
            }
        }
    }

    private static <T extends Notable> Class<T> getClass(Notable note) {
        for (Class clazz : typesToNames.keySet()) {
            if (clazz.isInstance(note)) {
                return clazz;
            }
        }
        return null;
    }

    private static YamlConfiguration saveConfig = null;
    private static String saveFilePath = null;

    public static void reload() {
        if (saveFilePath == null) {
            saveFilePath = new File(DenizenCore.getImplementation().getDataFolder(), "notables.yml").getPath();
        }
        saveConfig = YamlConfiguration.load(CoreUtilities.journallingLoadFile(saveFilePath));
        loadFromConfig();
    }

    public static YamlConfiguration getSaveConfig() {
        if (saveConfig == null) {
            reload();
        }
        return saveConfig;
    }

    public static void save() {
        if (saveConfig == null || saveFilePath == null) {
            return;
        }
        saveToConfig();
        if (nameToObject.isEmpty()) {
            if (new File(saveFilePath).exists()) {
                new File(saveFilePath).delete();
            }
        }
        else {
            CoreUtilities.journallingFileSave(saveFilePath, saveConfig.saveToString(false));
        }
    }

    ///////////////////
    // Note Annotation Handler
    ///////////////////

    public static Map<Class, String> typesToNames = new HashMap<>();
    public static Map<String, Class> namesToTypes = new HashMap<>();

    public static void registerWithNotableManager(Class notable) {
        for (Method method : notable.getMethods()) {
            if (method.isAnnotationPresent(Note.class)) {
                String note = method.getAnnotation(Note.class).value();
                typesToNames.put(notable, note);
                namesToTypes.put(note, notable);
                notesByType.put(notable, new HashSet<>());
            }
        }
    }
}
