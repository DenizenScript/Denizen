package net.aufdemrand.denizen.utilities;

import org.bukkit.Bukkit;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * ReflectionUtil v1.1
 * <p/>
 * You are welcome to use it, modify it and redistribute it under the condition to not claim this class as your own
 *
 * @author DarkBlade12
 */
public abstract class ReflectionUtil {
    private static final Map<Class<?>, Class<?>> CORRESPONDING_TYPES = new HashMap<Class<?>, Class<?>>();

    static {
        CORRESPONDING_TYPES.put(Byte.class, byte.class);
        CORRESPONDING_TYPES.put(Short.class, short.class);
        CORRESPONDING_TYPES.put(Integer.class, int.class);
        CORRESPONDING_TYPES.put(Long.class, long.class);
        CORRESPONDING_TYPES.put(Character.class, char.class);
        CORRESPONDING_TYPES.put(Float.class, float.class);
        CORRESPONDING_TYPES.put(Double.class, double.class);
        CORRESPONDING_TYPES.put(Boolean.class, boolean.class);
    }

    public enum DynamicPackage {
        MINECRAFT_SERVER {
            @Override
            public String toString() {
                return "net.minecraft.server." + Bukkit.getServer().getClass().getPackage().getName().substring(23, 30);
            }
        },
        CRAFTBUKKIT {
            @Override
            public String toString() {
                return Bukkit.getServer().getClass().getPackage().getName();
            }
        }
    }

    public static class FieldEntry {
        String key;
        Object value;

        public FieldEntry(String key, Object value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return this.key;
        }

        public Object getValue() {
            return this.value;
        }
    }

    private static Class<?> getPrimitiveType(Class<?> clazz) {
        return CORRESPONDING_TYPES.containsKey(clazz) ? CORRESPONDING_TYPES.get(clazz) : clazz;
    }

    private static Class<?>[] toPrimitiveTypeArray(Object[] objects) {
        int a = objects != null ? objects.length : 0;
        Class<?>[] types = new Class<?>[a];
        for (int i = 0; i < a; i++)
            types[i] = getPrimitiveType(objects[i].getClass());
        return types;
    }

    private static Class<?>[] toPrimitiveTypeArray(Class<?>[] classes) {
        int a = classes != null ? classes.length : 0;
        Class<?>[] types = new Class<?>[a];
        for (int i = 0; i < a; i++)
            types[i] = getPrimitiveType(classes[i]);
        return types;
    }

    private static boolean equalsTypeArray(Class<?>[] a, Class<?>[] o) {
        if (a.length != o.length)
            return false;
        for (int i = 0; i < a.length; i++)
            if (!a[i].equals(o[i]) && !a[i].isAssignableFrom(o[i]))
                return false;
        return true;
    }

    public static Class<?> getClass(String name, DynamicPackage pack, String subPackage) throws Exception {
        return Class.forName(pack + (subPackage != null && subPackage.length() > 0 ? "." + subPackage : "") + "." + name);
    }

    public static Class<?> getClass(String name, DynamicPackage pack) throws Exception {
        return getClass(name, pack, null);
    }

    public static Constructor<?> getConstructor(Class<?> clazz, Class<?>... paramTypes) {
        Class<?>[] t = toPrimitiveTypeArray(paramTypes);
        for (Constructor<?> c : clazz.getConstructors()) {
            Class<?>[] types = toPrimitiveTypeArray(c.getParameterTypes());
            if (equalsTypeArray(types, t))
                return c;
        }
        return null;
    }

    public static Object newInstance(Class<?> clazz, Object... args) throws Exception {
        return getConstructor(clazz, toPrimitiveTypeArray(args)).newInstance(args);
    }

    public static Object newInstance(String name, DynamicPackage pack, String subPackage, Object... args) throws Exception {
        return newInstance(getClass(name, pack, subPackage), args);
    }

    public static Object newInstance(String name, DynamicPackage pack, Object... args) throws Exception {
        return newInstance(getClass(name, pack, null), args);
    }

    public static Method getMethod(String name, Class<?> clazz, Class<?>... paramTypes) {
        Class<?>[] t = toPrimitiveTypeArray(paramTypes);
        for (Method m : clazz.getMethods()) {
            Class<?>[] types = toPrimitiveTypeArray(m.getParameterTypes());
            if (m.getName().equals(name) && equalsTypeArray(types, t))
                return m;
        }
        return null;
    }

    public static Object invokeMethod(String name, Class<?> clazz, Object obj, Object... args) throws Exception {
        return getMethod(name, clazz, toPrimitiveTypeArray(args)).invoke(obj, args);
    }

    public static Field getField(String name, Class<?> clazz) throws Exception {
        return clazz.getDeclaredField(name);
    }

    public static Object getValue(String name, Object obj) throws Exception {
        Field f = getField(name, obj.getClass());
        f.setAccessible(true);
        return f.get(obj);
    }

    public static void setValue(Object obj, FieldEntry entry) throws Exception {
        Field f = getField(entry.getKey(), obj.getClass());
        f.setAccessible(true);
        f.set(obj, entry.getValue());
    }

    public static void setValues(Object obj, FieldEntry... entrys) throws Exception {
        for (FieldEntry f : entrys)
            setValue(obj, f);
    }
}
