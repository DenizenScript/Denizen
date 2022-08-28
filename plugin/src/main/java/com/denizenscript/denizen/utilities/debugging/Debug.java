package com.denizenscript.denizen.utilities.debugging;

import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.utilities.debugging.Debuggable;

@Deprecated
public class Debug {
    @Deprecated
    public static void report(Debuggable caller, String name, String report) {
        com.denizenscript.denizencore.utilities.debugging.Debug.report(caller, name, report);
    }
    @Deprecated
    public static void report(Debuggable caller, String name, Object... values) {
        com.denizenscript.denizencore.utilities.debugging.Debug.report(caller, name, values);
    }
    @Deprecated
    public static void echoDebug(Debuggable caller, com.denizenscript.denizencore.utilities.debugging.Debug.DebugElement element) {
        com.denizenscript.denizencore.utilities.debugging.Debug.echoDebug(caller, element);
    }
    @Deprecated
    public static void echoDebug(Debuggable caller, com.denizenscript.denizencore.utilities.debugging.Debug.DebugElement element, String string) {
        com.denizenscript.denizencore.utilities.debugging.Debug.echoDebug(caller, element, string);
    }
    @Deprecated
    public static void echoDebug(Debuggable caller, String message) {
        com.denizenscript.denizencore.utilities.debugging.Debug.echoDebug(caller, message);
    }
    @Deprecated
    public static void echoApproval(String message) {
        com.denizenscript.denizencore.utilities.debugging.Debug.echoApproval(message);
    }
    @Deprecated
    public static void echoError(String message) {
        com.denizenscript.denizencore.utilities.debugging.Debug.echoError(message);
    }
    @Deprecated
    public static void echoError(ScriptEntry source, String message) {
        com.denizenscript.denizencore.utilities.debugging.Debug.echoError(source, message);
    }
    @Deprecated
    public static void echoError(Throwable ex) {
        com.denizenscript.denizencore.utilities.debugging.Debug.echoError(ex);
    }
    @Deprecated
    public static void echoError(ScriptEntry source, Throwable ex) {
        com.denizenscript.denizencore.utilities.debugging.Debug.echoError(source, ex);
    }
    @Deprecated
    public static void log(String message) {
        com.denizenscript.denizencore.utilities.debugging.Debug.log("<OUTDATED-PLUGIN>", message);
    }
    @Deprecated
    public static void log(String caller, String message) {
        com.denizenscript.denizencore.utilities.debugging.Debug.log(caller, message);
    }
    @Deprecated
    public static void log(com.denizenscript.denizencore.utilities.debugging.Debug.DebugElement element, String message) {
        com.denizenscript.denizencore.utilities.debugging.Debug.log(element, message);
    }
    @Deprecated
    public static boolean shouldDebug(Debuggable caller) {
        return com.denizenscript.denizencore.utilities.debugging.Debug.shouldDebug(caller);
    }
}
