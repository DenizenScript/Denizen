package com.denizenscript.denizen.utilities.debugging;

import com.denizenscript.denizen.utilities.FormattedTextHelper;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.utilities.implementation.DenizenCoreImplementation;
import com.denizenscript.denizen.utilities.Settings;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.DenizenCore;
import com.denizenscript.denizencore.events.core.ConsoleOutputScriptEvent;
import com.denizenscript.denizencore.events.core.ScriptGeneratesErrorScriptEvent;
import com.denizenscript.denizencore.events.core.ServerGeneratesExceptionScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ScriptTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.CommandExecutor;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.scripts.queues.ScriptQueue;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.utilities.debugging.Debuggable;
import com.denizenscript.denizencore.utilities.debugging.Debug.DebugElement;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Consumer;

public class Debug {

    public static boolean showDebug = true;
    public static boolean showStackTraces = true;
    public static boolean showColor = true;
    public static boolean debugOverride = false;
    public static boolean showSources = false;

    public static boolean shouldTrim = true;
    public static boolean record = false;
    public static StringBuilder recording = new StringBuilder();

    public static void toggle() {
        showDebug = !showDebug;
    }

    ////////////
    //  Public debugging methods, toggleable by checking extra criteria as implemented
    //  by the Debuggable interface, which usually checks a ScriptContainer's 'debug' node
    //////

    public static Consumer<String> getDebugSender(Debuggable caller) {
        if (caller == null) {
            caller = CommandExecutor.currentQueue;
        }
        if (caller instanceof TagContext) {
            if (((TagContext) caller).entry != null) {
                caller = ((TagContext) caller).entry;
            }
        }
        if (caller instanceof ScriptEntry) {
            if (((ScriptEntry) caller).getResidingQueue() != null) {
                caller = ((ScriptEntry) caller).getResidingQueue();
            }
        }
        if (caller instanceof ScriptQueue) {
            return ((ScriptQueue) caller).debugOutput;
        }
        // ScriptContainer can't be traced to a queue
        return null;
    }

    public static void report(Debuggable caller, String name, Object... values) {
        if (!showDebug || !shouldDebug(caller)) {
            return;
        }
        StringBuilder output = new StringBuilder();
        for (Object obj : values) {
            if (obj == null) {
                continue;
            }
            if (obj instanceof ObjectTag) {
                output.append(((ObjectTag) obj).debug());
            }
            else {
                output.append(obj.toString());
            }
        }
        echo("<Y>+> <G>Executing '<Y>" + name + "<G>': " + trimMessage(output.toString()), caller);
    }

    /**
     * Used by Commands to report how the supplied arguments were parsed.
     * Should be supplied a concatenated String with aH.debugObject() or ObjectTag.debug() of all
     * applicable objects used by the Command.
     *
     * @param caller the object calling this debug
     * @param name   the name of the command
     * @param report all the debug information related to the command
     */
    public static void report(Debuggable caller, String name, String report) {
        if (!showDebug || !shouldDebug(caller)) {
            return;
        }
        echo("<Y>+> <G>Executing '<Y>" + name + "<G>': " + trimMessage(report), caller);
    }

    public static void echoDebug(Debuggable caller, DebugElement element) {
        if (!showDebug || !shouldDebug(caller)) {
            return;
        }
        echoDebug(caller, element, null);
    }

    // Used by the various parts of Denizen that output debuggable information
    // to help scripters see what is going on. Debugging an element is usually
    // for formatting debug information.
    public static void echoDebug(Debuggable caller, DebugElement element, String string) {
        if (!showDebug || !shouldDebug(caller)) {
            return;
        }
        StringBuilder sb = new StringBuilder(24);

        switch (element) {
            case Footer:
                sb.append(ChatColor.LIGHT_PURPLE).append("+---------------------+");
                break;

            case Header:
                sb.append(ChatColor.LIGHT_PURPLE).append("+- ").append(string).append(ChatColor.LIGHT_PURPLE).append(" ---------+");
                break;
        }

        echo(sb.toString(), caller);
    }

    // Used by the various parts of Denizen that output debuggable information
    // to help scripters see what is going on.
    public static void echoDebug(Debuggable caller, String message) {
        if (!showDebug || !shouldDebug(caller)) {
            return;
        }
        echo(ChatColor.LIGHT_PURPLE + " " + ChatColor.WHITE + trimMessage(message), caller);
        if (com.denizenscript.denizencore.utilities.debugging.Debug.verbose && caller != null) {
            echo(ChatColor.GRAY + "(Verbose) Caller = " + caller, caller);
        }
    }

    /////////////
    // Other public debugging methods (Always show when debugger is enabled)
    ///////

    /**
     * Shows an approval message (always shows, regardless of script debug mode, excluding debug fully off - use sparingly)
     * Prefixed with "OKAY! "
     *
     * @param message the message to debug
     */
    public static void echoApproval(String message) {
        if (!showDebug) {
            return;
        }
        finalOutputDebugText(ChatColor.LIGHT_PURPLE + " " + ChatColor.GREEN + "OKAY! "
                + ChatColor.WHITE + message, null);
    }

    public static void echoError(String message) {
        echoError(null, null, message, true);
    }

    public static void echoError(ScriptQueue sourceQueue, String message) {
        echoError(sourceQueue, null, message, true);
    }

    public static boolean errorDuplicatePrevention = false;

    public static void echoError(ScriptQueue sourceQueue, String addedContext, String message, boolean reformat) {
        message = cleanTextForDebugOutput(message);
        if (errorDuplicatePrevention) {
            if (!com.denizenscript.denizencore.utilities.debugging.Debug.verbose) {
                finalOutputDebugText("Error within error (??!!!! SOMETHING WENT SUPER WRONG!): " + message, sourceQueue, reformat);
            }
            return;
        }
        errorDuplicatePrevention = true;
        if (sourceQueue == null) {
            sourceQueue = CommandExecutor.currentQueue;
        }
        ScriptEntry sourceEntry = null;
        if (sourceQueue != null && sourceQueue.getLastEntryExecuted() != null) {
            sourceEntry = sourceQueue.getLastEntryExecuted();
        }
        else if (sourceQueue != null && sourceQueue.getEntries().size() > 0) {
            sourceEntry = sourceQueue.getEntries().get(0);
        }
        ScriptTag sourceScript = null;
        if (sourceEntry != null) {
            sourceScript = sourceEntry.getScript();
        }
        if (throwErrorEvent) {
            throwErrorEvent = false;
            boolean cancel = ScriptGeneratesErrorScriptEvent.instance.handle(message, sourceQueue, sourceScript, sourceEntry == null ? -1 : sourceEntry.internal.lineNumber);
            throwErrorEvent = true;
            if (cancel) {
                errorDuplicatePrevention = false;
                return;
            }
        }
        if (!showDebug) {
            errorDuplicatePrevention = false;
            return;
        }
        StringBuilder fullMessage = new StringBuilder();
        fullMessage.append(ChatColor.LIGHT_PURPLE).append(" ").append(ChatColor.RED).append("ERROR");
        if (sourceScript != null) {
            fullMessage.append(" in script '").append(ChatColor.AQUA).append(sourceScript.getName()).append(ChatColor.RED).append("'");
        }
        if (sourceQueue != null) {
            fullMessage.append(" in queue '").append(sourceQueue.debugId).append(ChatColor.RED).append("'");
        }
        if (sourceEntry != null) {
            fullMessage.append(" while executing command '").append(ChatColor.AQUA).append(sourceEntry.getCommandName()).append(ChatColor.RED).append("'");
            if (sourceScript != null) {
                fullMessage.append(" in file '").append(ChatColor.AQUA).append(sourceScript.getContainer().getRelativeFileName()).append(ChatColor.RED)
                        .append("' on line '").append(ChatColor.AQUA).append(sourceEntry.internal.lineNumber).append(ChatColor.RED).append("'");
            }
            BukkitScriptEntryData data = Utilities.getEntryData(sourceEntry);
            if (data.hasPlayer()) {
                fullMessage.append(" with player '").append(ChatColor.AQUA).append(data.getPlayer().getName()).append(ChatColor.RED).append("'");
            }
            if (data.hasNPC()) {
                fullMessage.append(" with NPC '").append(ChatColor.AQUA).append(data.getNPC().debuggable()).append(ChatColor.RED).append("'");
            }
        }
        if (addedContext != null) {
            fullMessage.append("\n     ").append(addedContext);
        }
        fullMessage.append("!\n").append(ChatColor.GRAY).append("     Error Message: ").append(ChatColor.WHITE).append(message);
        if (sourceScript != null && !sourceScript.getContainer().shouldDebug()) {
            fullMessage.append(ChatColor.GRAY).append(" ... ").append(ChatColor.RED).append("Enable debug on the script for more information.");
        }
        finalOutputDebugText(fullMessage.toString(), sourceQueue, reformat);
        errorDuplicatePrevention = false;
        if (com.denizenscript.denizencore.utilities.debugging.Debug.verbose && depthCorrectError == 0) {
            depthCorrectError++;
            try {
                throw new RuntimeException("Verbose info for above error");
            }
            catch (Throwable e) {
                echoError(sourceQueue, e);
            }
            depthCorrectError--;
        }
    }

    static long depthCorrectError = 0;

    private static boolean throwErrorEvent = true;

    public static void echoError(Throwable ex) {
        echoError(null, ex);
    }

    public static void echoError(ScriptQueue source, Throwable ex) {
        if (source == null) {
            source = CommandExecutor.currentQueue;
        }
        String errorMessage = getFullExceptionMessage(ex, true);
        if (throwErrorEvent) {
            throwErrorEvent = false;
            Throwable thrown = ex;
            while (thrown.getCause() != null) {
                thrown = thrown.getCause();
            }
            boolean cancel = ServerGeneratesExceptionScriptEvent.instance.handle(thrown, errorMessage, source);
            throwErrorEvent = true;
            if (cancel) {
                return;
            }
        }
        if (!showDebug) {
            return;
        }
        boolean wasThrown = throwErrorEvent;
        throwErrorEvent = false;
        if (!showStackTraces) {
            Debug.echoError(source, "Exception! Enable '/denizen debug -s' for the nitty-gritty.");
        }
        else {
            depthCorrectError++;
            echoError(source, null, errorMessage, false);
            depthCorrectError--;
        }
        throwErrorEvent = wasThrown;
    }

    public static String getFullExceptionMessage(Throwable ex, boolean includeBounding) {
        StringBuilder errorMessage = new StringBuilder();
        if (includeBounding) {
            errorMessage.append("Internal exception was thrown!\n");
        }
        String prefix = includeBounding ? ChatColor.GRAY + "[Error Continued] " + ChatColor.WHITE : "";
        boolean first = true;
        while (ex != null) {
            errorMessage.append(prefix);
            if (!first) {
                errorMessage.append("Caused by: ");
            }
            errorMessage.append(ex.toString()).append("\n");
            for (StackTraceElement ste : ex.getStackTrace()) {
                errorMessage.append(prefix).append("  ").append(ste.toString()).append("\n");
            }
            if (ex.getCause() == ex) {
                break;
            }
            ex = ex.getCause();
            first = false;
        }
        return errorMessage.toString();
    }

    private static final Map<Class<?>, String> classNameCache = new WeakHashMap<>();

    private static class SecurityManagerTrick extends SecurityManager {
        @Override
        @SuppressWarnings("rawtypes")
        protected Class[] getClassContext() {
            return super.getClassContext();
        }
    }

    private static boolean canGetClass = true;

    public static void log(String message) {
        if (!showDebug) {
            return;
        }
        String callerName = "<JVM-Block>";
        try {
            if (canGetClass) {
                Class[] classes = new SecurityManagerTrick().getClassContext();
                Class caller = classes.length > 2 ? classes[2] : Debug.class;
                if (caller == DenizenCoreImplementation.class) {
                    caller = classes.length > 4 ? classes[4] : Debug.class;
                }
                callerName = classNameCache.get(caller);
                if (callerName == null) {
                    classNameCache.put(caller, callerName = caller.getSimpleName());
                }
                callerName = callerName.length() > 16 ? callerName.substring(0, 12) + "..." : callerName;
            }
        }
        catch (Throwable ex) {
            canGetClass = false;
        }
        finalOutputDebugText(ChatColor.YELLOW + "+> [" + callerName + "] " + ChatColor.WHITE + trimMessage(message), null);
    }

    public static void log(String caller, String message) {
        if (!showDebug) {
            return;
        }
        finalOutputDebugText(ChatColor.YELLOW + "+> [" + caller + "] " + ChatColor.WHITE + trimMessage(message), null);
    }

    public static void log(DebugElement element, String message) {
        if (!showDebug) {
            return;
        }
        StringBuilder sb = new StringBuilder(24);

        switch (element) {
            case Footer:
                sb.append(ChatColor.LIGHT_PURPLE).append("+---------------------+");
                break;

            case Header:
                sb.append(ChatColor.LIGHT_PURPLE).append("+- ").append(message).append(" ---------+");
                break;

            default:
                break;
        }

        finalOutputDebugText(sb.toString(), null);
    }

    ///////////////
    // Private Helper Methods
    /////////

    // Some debug methods trim to keep super-long messages from hitting the console.
    private static String trimMessage(String message) {
        if (!shouldTrim) {
            return message;
        }
        int trimSize = Settings.trimLength();
        if (message.length() > trimSize) {
            message = message.substring(0, trimSize - 1) + "... * snip! *";
        }
        return message;
    }

    public static boolean shouldDebug(Debuggable caller) {
        if (debugOverride) {
            return true;
        }
        if (!showDebug) {
            return false;
        }
        if (caller != null) {
            return caller.shouldDebug();
        }
        return true;
    }

    // Handles checking whether the provided debuggable should submit to the debugger
    private static void echo(String string, Debuggable caller) {
        if (!shouldDebug(caller)) {
            return;
        }
        if (!showSources || caller == null) {
            finalOutputDebugText(string, caller);
            return;
        }
        String callerId;
        if (caller instanceof ScriptContainer) {
            callerId = "Script:" + ((ScriptContainer) caller).getName();
        }
        else if (caller instanceof ScriptEntry) {
            if (((ScriptEntry) caller).getScript() != null) {
                callerId = "Command:" + ((ScriptEntry) caller).getCommandName() + " in Script:" + ((ScriptEntry) caller).getScript().getName();
            }
            else {
                callerId = "Command:" + ((ScriptEntry) caller).getCommandName();
            }
        }
        else if (caller instanceof ScriptQueue) {
            if (((ScriptQueue) caller).script != null) {
                callerId = "Queue:" + ((ScriptQueue) caller).id + " running Script:" + ((ScriptQueue) caller).script.getName();
            }
            else {
                callerId = "Queue:" + ((ScriptQueue) caller).id;
            }
        }
        else if (caller instanceof TagContext) {
            if (((TagContext) caller).entry != null) {
                ScriptEntry sent = ((TagContext) caller).entry;
                if (sent.getScript() != null) {
                    callerId = "Tag in Command:" + sent.getCommandName() + " in Script:" + sent.getScript().getName();
                }
                else {
                    callerId = "Tag in Command:" + sent.getCommandName();
                }
            }
            else if (((TagContext) caller).script != null) {
                callerId = "Tag in Script:" + ((TagContext) caller).script.getName();
            }
            else {
                callerId = "Tag:" + caller.toString();
            }
        }
        else {
            callerId = caller.toString();
        }
        finalOutputDebugText(ChatColor.DARK_GRAY + "[Src:" + ChatColor.GRAY + callerId + ChatColor.DARK_GRAY + "]" + ChatColor.WHITE + string, caller);
    }

    static void finalOutputDebugText(String message, Debuggable caller) {
        finalOutputDebugText(message, caller, true);
    }

    public static String cleanTextForDebugOutput(String message) {
        return message
                .replace("<Y>", ChatColor.YELLOW.toString())
                .replace("<O>", ChatColor.GOLD.toString()) // 'orange'
                .replace("<G>", ChatColor.DARK_GRAY.toString())
                .replace("<LG>", ChatColor.GRAY.toString())
                .replace("<GR>", ChatColor.GREEN.toString())
                .replace("<A>", ChatColor.AQUA.toString())
                .replace("<R>", ChatColor.DARK_RED.toString())
                .replace("<LR>", ChatColor.RED.toString())
                .replace("<W>", ChatColor.WHITE.toString());
    }

    public static int outputThisTick = 0;

    static void finalOutputDebugText(String message, Debuggable caller, boolean reformat) {
        outputThisTick++;
        if (outputThisTick >= Settings.debugLimitPerTick()) {
            if (outputThisTick == Settings.debugLimitPerTick()) {
                ConsoleSender.sendMessage("... Debug rate limit per-tick hit, edit config.yml to adjust this limit...", true);
            }
            return;
        }
        // These colors are used a lot in the debugging of commands/etc, so having a few shortcuts is nicer
        // than having a bunch of ChatColor.XXXX
        message = cleanTextForDebugOutput(message);
        ConsoleSender.sendMessage(message, reformat);
        Consumer<String> additional = getDebugSender(caller);
        if (additional != null) {
            additional.accept(message);
        }
    }

    private static class ConsoleSender {

        // Bukkit CommandSender sends color nicely to the logger, so we'll use that.
        static CommandSender commandSender = null;
        public static SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        static boolean skipFooter = false;

        public static void sendMessage(String string, boolean reformat) {
            if (commandSender == null) {
                commandSender = Bukkit.getServer().getConsoleSender();
            }

            // 'Hack-fix' for disallowing multiple 'footers' to print in a row
            if (string.equals(ChatColor.LIGHT_PURPLE + "+---------------------+")) {
                if (!skipFooter) {
                    skipFooter = true;
                }
                else {
                    return;
                }
            }
            else {
                skipFooter = false;
            }
            string = string.replace('\0', ' ');

            if (reformat) {
                // Create buffer for wrapping debug text nicely. This is mostly needed for Windows logging.
                String[] words = string.split(" ");
                StringBuilder buffer = new StringBuilder();
                int length = 0;
                int width = Settings.consoleWidth();
                for (String word : words) {
                    // # of total chars * # of lines - timestamp
                    int strippedLength = ChatColor.stripColor(word).length() + 1;
                    if (length + strippedLength < width) {
                        buffer.append(word).append(" ");
                        length += strippedLength;
                    }
                    else {
                        // Increase # of lines to account for
                        length = strippedLength;
                        // Leave spaces to account for timestamp and indent
                        buffer.append("\n                   ").append(word).append(" ");
                    }                 // [01:02:03 INFO]:
                    if (word.contains("\n")) {
                        length = 0;
                    }
                }
                string = buffer.toString();
            }

            // Record current buffer to the to-be-submitted buffer
            if (Debug.record) {
                Debug.recording.append(URLEncoder.encode(dateFormat.format(new Date())
                        + " [INFO] " + string.replace(ChatColor.COLOR_CHAR, (char) 0x01) + "\n"));
            }
            string = Settings.debugPrefix() + string;
            if (DenizenCore.logInterceptor.redirected) {
                if (!DenizenCore.logInterceptor.antiLoop) {
                    DenizenCore.logInterceptor.antiLoop = true;
                    try {
                        ConsoleOutputScriptEvent event = ConsoleOutputScriptEvent.instance;
                        event.message = string;
                        event = (ConsoleOutputScriptEvent) event.fire();
                        if (event.cancelled) {
                            return;
                        }
                    }
                    finally {
                        DenizenCore.logInterceptor.antiLoop = false;
                    }
                }
            }
            if (showColor) {
                commandSender.spigot().sendMessage(FormattedTextHelper.parse(string, net.md_5.bungee.api.ChatColor.WHITE));
            }
            else {
                commandSender.sendMessage(ChatColor.stripColor(string));
            }
        }
    }
}
