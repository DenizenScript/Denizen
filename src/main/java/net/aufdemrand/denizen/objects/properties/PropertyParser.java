package net.aufdemrand.denizen.objects.properties;

import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.objects.dObject;
import net.aufdemrand.denizen.utilities.debugging.dB;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class PropertyParser {

    public static String NONE = "";

    // Keep track of which Property belongs to which dObject
    static Map<Class<? extends dObject>, List<Class>> properties
            = new HashMap<Class<? extends dObject>, List<Class>>();

    // Keep track of the static 'describes' and 'getFrom' methods for each Property
    static Map<Class, Method> describes = new WeakHashMap<Class, Method>();
    static Map<Class, Method> getFrom = new WeakHashMap<Class, Method>();


    public PropertyParser() {
        properties.clear();
        describes.clear();
        getFrom.clear();

        // register core dEntity properties
        registerProperty(EntityProfession.class, dEntity.class);
        registerProperty(EntityAge.class, dEntity.class);
        registerProperty(EntityFramed.class, dEntity.class);
        registerProperty(EntityInfected.class, dEntity.class);

        // register core dItem properties
        registerProperty(ItemColor.class, dItem.class);

    }

    public void registerProperty(Class property, Class<? extends dObject> object) {
        // Add property to the dObject's Properties list
        List<Class> prop_list;

        // Get current properties list, or make a new one
        if (properties.containsKey(object))
            prop_list = properties.get(object);
        else prop_list = new ArrayList<Class>();

        // Add this property to the list
        prop_list.add(property);

        // Put the list back into the Map
        properties.put(object, prop_list);

        // Cache methods used for fetching new properties
        try {
            describes.put(property, property.getMethod("describes", dObject.class));
            getFrom.put(property, property.getMethod("getFrom", dObject.class));

        } catch (NoSuchMethodException e) {
            dB.echoError("Unable to register property '" + property.getSimpleName() + "'!");
        }

    }

    public static String getPropertiesString(dObject object) {
        StringBuilder prop_string = new StringBuilder();

        // Iterate through each property associated with the dObject type, invoke 'describes'
        // and if 'true', add property string from the property to the prop_string.
        try {
            for (Class property : properties.get(object.getClass())) {
                if ((Boolean) describes.get(property).invoke(null, object))
                    prop_string.append(((Property) getFrom.get(property).invoke(null, object)).getPropertyString());
            }

        } catch (IllegalAccessException e) {
            dB.echoError(e);
        } catch (InvocationTargetException e) {
            dB.echoError(e);
        }

        // Return the list of properties
        return prop_string.toString();
    }

    public static List<Property> getProperties(dObject object) {
        List<Property> props = new ArrayList<Property>();

        try {
            for (Class property : properties.get(object.getClass())) {
                if ((Boolean) describes.get(property).invoke(null, object))
                    props.add((Property) getFrom.get(property).invoke(null, object));
            }

        } catch (IllegalAccessException e) {
            dB.echoError(e);
        } catch (InvocationTargetException e) {
            dB.echoError(e);
        }

        return props;

    }



}
