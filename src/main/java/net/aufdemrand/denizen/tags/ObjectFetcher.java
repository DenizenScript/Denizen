package net.aufdemrand.denizen.tags;

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

    private static Map<String, Class> objects = new HashMap<String, Class>();

    public static void _initialize() throws IOException, ClassNotFoundException {
        objects.clear();

        for (Class dClass : fetchable_objects)
            for (Method method : dClass.getMethods())
                if (method.isAnnotationPresent(net.aufdemrand.denizen.objects.ObjectFetcher.class)) {
                    String[] identifiers = method.getAnnotation(net.aufdemrand.denizen.objects.ObjectFetcher.class).value().split(",");
                    for (String identifer : identifiers)
                        objects.put(identifer.trim().toLowerCase(), dClass);
                }

        dB.echoApproval("Loaded the Object Fetcher! Valid object types: " + objects.keySet().toString());
    }

    private static ArrayList<Class> fetchable_objects = new ArrayList<Class>();

    public static void registerWithObjectFetcher(Class dObject) {
        fetchable_objects.add(dObject);
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

}
