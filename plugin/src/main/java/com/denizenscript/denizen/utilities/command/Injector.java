package com.denizenscript.denizen.utilities.command;

import com.denizenscript.denizen.utilities.debugging.dB;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class Injector {

    private final Class<?>[] argClasses;
    private final Object[] args;

    public Injector(Object... args) {
        this.args = args;
        argClasses = new Class[args.length];
        for (int i = 0; i < args.length; ++i) {
            argClasses[i] = args[i].getClass();
        }
    }

    public Object getInstance(Class<?> clazz) {
        try {
            Constructor<?> ctr = clazz.getConstructor(argClasses);
            ctr.setAccessible(true);
            return ctr.newInstance(args);
        }
        catch (NoSuchMethodException e) {
            try {
                return clazz.newInstance();
            }
            catch (Exception ex) {
                dB.echoError("Error initializing commands class " + clazz + ": ");
                dB.echoError(ex);
                return null;
            }
        }
        catch (InvocationTargetException e) {
            dB.echoError("Error initializing commands class " + clazz + ": ");
            dB.echoError(e);
            return null;
        }
        catch (InstantiationException e) {
            dB.echoError("Error initializing commands class " + clazz + ": ");
            dB.echoError(e);
            return null;
        }
        catch (IllegalAccessException e) {
            dB.echoError("Error initializing commands class " + clazz + ": ");
            dB.echoError(e);
            return null;
        }
    }
}
