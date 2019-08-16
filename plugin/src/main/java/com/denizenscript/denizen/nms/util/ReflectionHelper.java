package com.denizenscript.denizen.nms.util;

import com.denizenscript.denizencore.utilities.debugging.Debug;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public class ReflectionHelper {

    private static final Map<Class, Map<String, Field>> cachedFields = new HashMap<>();

    private static final Map<Class, Map<String, MethodHandle>> cachedFieldSetters = new HashMap<>();

    public static <T> T getFieldValue(Class clazz, String fieldName, Object object) {
        Map<String, Field> cache = getFields(clazz);
        try {
            Field field = cache.get(fieldName);
            if (field == null) {
                return null;
            }
            cache.put(fieldName, field);
            return (T) field.get(object);
        }
        catch (Exception ex) {
            Debug.echoError(ex);
            return null;
        }
    }

    public static void setFieldValue(Class clazz, String fieldName, Object object, Object value) {
    }

    public static Map<String, Field> getFields(Class clazz) {
        Map<String, Field> fields = cachedFields.get(clazz);
        if (fields != null) {
            return fields;
        }
        fields = new HashMap<>();
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            fields.put(field.getName(), field);
        }
        cachedFields.put(clazz, fields);
        return fields;
    }

    public static Method getMethod(Class<?> clazz, String method, Class<?>... params) {
        Method f = null;
        try {
            f = clazz.getDeclaredMethod(method, params);
            f.setAccessible(true);
        }
        catch (Exception ex) {
            Debug.echoError(ex);
        }
        return f;
    }

    public static MethodHandle getMethodHandle(Class<?> clazz, String method, Class<?>... params) {
        try {
            return LOOKUP.unreflect(getMethod(clazz, method, params));
        }
        catch (Exception ex) {
            Debug.echoError(ex);
        }
        return null;
    }

    public static MethodHandle getFinalSetter(Class<?> clazz, String field) {
        Map<String, MethodHandle> map = cachedFieldSetters.get(clazz);
        if (map == null) {
            map = new HashMap<>();
            cachedFieldSetters.put(clazz, map);
        }
        MethodHandle result = map.get(field);
        if (result != null) {
            return result;
        }
        Field f = getFields(clazz).get(field);
        if (f == null) {
            return null;
        }
        int mod = f.getModifiers();
        try {
            if (MODIFIERS_FIELD == null) {
                validateUnsafe();
                boolean isStatic = Modifier.isStatic(mod);
                long offset = (long) (isStatic ? UNSAFE_STATIC_FIELD_OFFSET.invoke(f) : UNSAFE_FIELD_OFFSET.invoke(f));
                result = isStatic ? MethodHandles.insertArguments(UNSAFE_PUT_OBJECT, 0, clazz, offset)
                        : MethodHandles.insertArguments(UNSAFE_PUT_OBJECT, 1, offset);
            }
            else {
                if (Modifier.isFinal(mod)) {
                    MODIFIERS_FIELD.setInt(f, mod & ~Modifier.FINAL);
                }
                result = LOOKUP.unreflectSetter(f);
            }
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
            return null;
        }
        if (result == null) {
            return null;
        }
        cachedFieldSetters.get(clazz).put(field, result);
        return result;
    }

    private static void validateUnsafe() {
        if (UNSAFE == null) {
            try {
                UNSAFE = getFields(Class.forName("sun.misc.Unsafe")).get("theUnsafe");
            }
            catch (Exception ex) {
                Debug.echoError(ex);
            }
            UNSAFE_STATIC_FIELD_OFFSET = getMethodHandle(UNSAFE.getClass(), "staticFieldOffset", Field.class).bindTo(UNSAFE);
            UNSAFE_FIELD_OFFSET = getMethodHandle(UNSAFE.getClass(), "objectFieldOffset", Field.class).bindTo(UNSAFE);
            UNSAFE_PUT_OBJECT = getMethodHandle(UNSAFE.getClass(), "putObject", Object.class, long.class, Object.class).bindTo(UNSAFE);
        }
    }

    private static MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    private static Field MODIFIERS_FIELD = getFields(Field.class).get("modifiers");
    private static Object UNSAFE;
    private static MethodHandle UNSAFE_FIELD_OFFSET;
    private static MethodHandle UNSAFE_PUT_OBJECT;
    private static MethodHandle UNSAFE_STATIC_FIELD_OFFSET;
}
