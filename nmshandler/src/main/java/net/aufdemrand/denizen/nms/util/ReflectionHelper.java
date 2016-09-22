package net.aufdemrand.denizen.nms.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public class ReflectionHelper {

    private static final Map<Class, Map<String, Field>> cachedFields = new HashMap<Class, Map<String, Field>>();

    public static <T> T getFieldValue(Class clazz, String fieldName, Object object) {
        Map<String, Field> cache;
        if (cachedFields.containsKey(clazz)) {
            cache = cachedFields.get(clazz);
        }
        else {
            cache = new HashMap<String, Field>();
            cachedFields.put(clazz, cache);
        }
        Field field;
        try {
            if (cache.containsKey(fieldName)) {
                field = cache.get(fieldName);
            }
            else {
                field = clazz.getDeclaredField(fieldName);
            }
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            cache.put(fieldName, field);
            return (T) field.get(object);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void setFieldValue(Class clazz, String fieldName, Object object, Object value) {
        Map<String, Field> cache;
        if (cachedFields.containsKey(clazz)) {
            cache = cachedFields.get(clazz);
        }
        else {
            cache = new HashMap<String, Field>();
            cachedFields.put(clazz, cache);
        }
        Field field;
        try {
            if (cache.containsKey(fieldName)) {
                field = cache.get(fieldName);
            }
            else {
                field = clazz.getDeclaredField(fieldName);
            }
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            int mod = field.getModifiers();
            if (Modifier.isFinal(mod)) {
                setFieldValue(Field.class, "modifiers", field, mod & ~Modifier.FINAL);
            }
            cache.put(fieldName, field);
            field.set(object, value);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Map<String, Field> getFields(Class clazz) {
        if (cachedFields.containsKey(clazz)) {
            return cachedFields.get(clazz);
        }
        Map<String, Field> fields = new HashMap<String, Field>();
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            fields.put(field.getName(), field);
        }
        cachedFields.put(clazz, fields);
        return fields;
    }
}
