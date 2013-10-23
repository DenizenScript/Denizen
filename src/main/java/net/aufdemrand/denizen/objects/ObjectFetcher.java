package net.aufdemrand.denizen.objects;

import net.aufdemrand.denizen.objects.Fetchable;
import net.aufdemrand.denizen.objects.notable.NotableManager;
import net.aufdemrand.denizen.utilities.debugging.dB;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
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

    public static void _initialize() throws IOException, ClassNotFoundException, NoSuchMethodException {

        // Initialize the ObjectFetcher
        ObjectFetcher.registerWithObjectFetcher(dItem.class);      // i@
        ObjectFetcher.registerWithObjectFetcher(dCuboid.class);    // cu@
        ObjectFetcher.registerWithObjectFetcher(dEntity.class);    // e@
        ObjectFetcher.registerWithObjectFetcher(dInventory.class); // in@
        ObjectFetcher.registerWithObjectFetcher(dColor.class);     // co@
        ObjectFetcher.registerWithObjectFetcher(dList.class);      // li@/fl@
        ObjectFetcher.registerWithObjectFetcher(dLocation.class);  // l@
        ObjectFetcher.registerWithObjectFetcher(dMaterial.class);  // m@
        ObjectFetcher.registerWithObjectFetcher(dNPC.class);       // n@
        ObjectFetcher.registerWithObjectFetcher(dPlayer.class);    // p@
        ObjectFetcher.registerWithObjectFetcher(dScript.class);    // s@
        ObjectFetcher.registerWithObjectFetcher(dWorld.class);     // w@
        ObjectFetcher.registerWithObjectFetcher(Element.class);    // el@
        ObjectFetcher.registerWithObjectFetcher(Duration.class);   // d@
        ObjectFetcher.registerWithObjectFetcher(dChunk.class);     // ch@

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
            e.printStackTrace();
        }

        return false;

    }

    public static dObject getObjectFrom(Class<? extends dObject> dClass, String value) {
        try {
            return (dObject) valueof.get(dClass).invoke(null, value);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
