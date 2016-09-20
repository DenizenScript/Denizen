package net.aufdemrand.denizen.nms.util;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

public class DenizenAtomicIntegerFieldUpdater<T> extends AtomicIntegerFieldUpdater<T> {
    private static final Unsafe unsafe;

    static {
        Unsafe theUnsafe = null;
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            theUnsafe = (Unsafe) field.get(null);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        unsafe = theUnsafe;
    }

    private final long offset;

    public DenizenAtomicIntegerFieldUpdater(Field field) {
        field.setAccessible(true);
        int modifiers = field.getModifiers();

        Class fieldt = field.getType();
        if (fieldt != int.class) {
            throw new IllegalArgumentException("Must be integer type");
        }

        if (!Modifier.isVolatile(modifiers)) {
            throw new IllegalArgumentException("Must be volatile type");
        }

        offset = unsafe.objectFieldOffset(field);
    }

    public boolean compareAndSet(T obj, int expect, int update) {
        return unsafe.compareAndSwapInt(obj, offset, expect, update);
    }

    public boolean weakCompareAndSet(T obj, int expect, int update) {
        return unsafe.compareAndSwapInt(obj, offset, expect, update);
    }

    public void set(T obj, int newValue) {
        unsafe.putIntVolatile(obj, offset, newValue);
    }

    public void lazySet(T obj, int newValue) {
        unsafe.putOrderedInt(obj, offset, newValue);
    }

    public final int get(T obj) {
        return unsafe.getIntVolatile(obj, offset);
    }
}
