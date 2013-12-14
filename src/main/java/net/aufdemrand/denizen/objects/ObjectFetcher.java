package net.aufdemrand.denizen.objects;

import net.aufdemrand.denizen.utilities.debugging.dB;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;

/**
 *
 * @author Jeremy Schroeder
 *
 */

public class ObjectFetcher {

    // Keep track of each Class keyed by its 'object identifier' --> i@, e@, etc.
    private static Map<String, Class> objects = new HashMap<String, Class>();

    // Keep track of the static 'matches' and 'valueOf' methods for each dObject
    static Map<Class, Method> matches = new WeakHashMap<Class, Method>();
    static Map<Class, Method> valueof = new WeakHashMap<Class, Method>();

    public static void _initialize() throws IOException, ClassNotFoundException {

        if (fetchable_objects.isEmpty())
            return;

        Map<String, Class> adding = new HashMap<String, Class>();
        for (Class dClass : fetchable_objects)
            for (Method method : dClass.getMethods())
                if (method.isAnnotationPresent(Fetchable.class)) {
                    String[] identifiers = method.getAnnotation(Fetchable.class).value().split(",");
                    for (String identifer : identifiers)
                        adding.put(identifer.trim().toLowerCase(), dClass);
                }

        objects.putAll(adding);
        dB.echoApproval("Added objects to the ObjectFetcher " + adding.keySet().toString());
        fetchable_objects.clear();
    }

    public static void _registerCoreObjects() throws NoSuchMethodException, ClassNotFoundException, IOException {

        // Initialize the ObjectFetcher
        registerWithObjectFetcher(dItem.class);      // i@
        registerWithObjectFetcher(dCuboid.class);    // cu@
        registerWithObjectFetcher(dEntity.class);    // e@
        registerWithObjectFetcher(dInventory.class); // in@
        registerWithObjectFetcher(dColor.class);     // co@
        registerWithObjectFetcher(dList.class);      // li@/fl@
        registerWithObjectFetcher(dLocation.class);  // l@
        registerWithObjectFetcher(dMaterial.class);  // m@
        registerWithObjectFetcher(dNPC.class);       // n@
        registerWithObjectFetcher(dPlayer.class);    // p@
        registerWithObjectFetcher(dScript.class);    // s@
        registerWithObjectFetcher(dWorld.class);     // w@
        registerWithObjectFetcher(Element.class);    // el@
        registerWithObjectFetcher(Duration.class);   // d@
        registerWithObjectFetcher(dChunk.class);     // ch@

        _initialize();

    }

    private static ArrayList<Class> fetchable_objects = new ArrayList<Class>();

    public static void registerWithObjectFetcher(Class dObject) throws NoSuchMethodException {
        fetchable_objects.add(dObject);
        matches.put(dObject, dObject.getMethod("matches", String.class));
        valueof.put(dObject, dObject.getMethod("valueOf", String.class));
    }

    public static boolean canFetch(String id) {
        return objects.containsKey(id.toLowerCase());
    }

    public static Class getObjectClass(String id) {
        if (canFetch(id))
            return objects.get(id.toLowerCase());
        else
            return null;
    }

    public static boolean checkMatch(Class<? extends dObject> dClass, String value) {
        try {
            return (Boolean) matches.get(dClass).invoke(null, value);
        } catch (Exception e) {
            dB.echoError(e);
        }

        return false;

    }

    public static <T extends dObject> T getObjectFrom(Class<T> dClass, String value) {
        try {
            return (T) valueof.get(dClass).invoke(null, value);
        } catch (Exception e) {
            dB.echoError(e);
        }

        return null;
    }
}
