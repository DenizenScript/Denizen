package com.denizenscript.denizen.utilities.debugging;

import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.utilities.implementation.DenizenCoreImplementation;
import com.denizenscript.denizen.utilities.Settings;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.events.OldEventManager;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.QueueTag;
import com.denizenscript.denizencore.objects.core.ScriptTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.CommandExecutor;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.scripts.queues.ScriptQueue;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.tags.TagManager;
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
    public static StringBuilder Recording = new StringBuilder();

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

    // <--[event]
    // @Events
    // script generates error
    //
    // @Regex ^on script generates error$
    //
    // @Triggers when a script generates an error.
    // @Context
    // <context.message> returns the error message.
    // <context.queue> returns the queue that caused the error, if any.
    // <context.script> returns the script that caused the error, if any.
    // <context.line> returns the line number within the script file that caused the error, if any.
    //
    // @Determine
    // "CANCELLED" to stop the error from showing in the console.
    // -->
    public static void echoError(String message) {
        echoError(null, message);
    }

    public static void echoError(ScriptQueue sourceQueue, String message) {
        echoError(sourceQueue, message, true);
    }

    public static void echoError(ScriptQueue sourceQueue, String message, boolean reformat) {
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
            Map<String, ObjectTag> context = new HashMap<>();
            context.put("message", new ElementTag(message));
            if (sourceQueue != null) {
                context.put("queue", new QueueTag(sourceQueue));
            }
            if (sourceScript != null) {
                context.put("script", sourceScript);
            }
            if (sourceEntry != null) {
                context.put("line", new ElementTag(sourceEntry.internal.lineNumber));
            }
            List<String> events = new ArrayList<>();
            events.add("script generates error");
            if (sourceScript != null) {
                events.add(sourceScript.identifySimple() + " generates error");
            }
            ScriptEntry entry = (sourceQueue != null ? sourceQueue.getLastEntryExecuted() : null);
            List<String> Determinations = OldEventManager.doEvents(events,
                    entry != null ? entry.entryData : new BukkitScriptEntryData(null, null), context, true);
            throwErrorEvent = true;
            for (String Determination : Determinations) {
                if (Determination.equalsIgnoreCase("CANCELLED")) {
                    return;
                }
            }
        }
        if (!showDebug) {
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
                fullMessage.append(" with player '").append(ChatColor.AQUA).append(data.getPlayer().debuggable()).append(ChatColor.RED).append("'");
            }
            if (data.hasNPC()) {
                fullMessage.append(" with NPC '").append(ChatColor.AQUA).append(data.getNPC().debuggable()).append(ChatColor.RED).append("'");
            }
        }
        fullMessage.append("!\n").append(ChatColor.GRAY).append("     Error Message: ").append(ChatColor.WHITE).append(message);
        if (sourceScript != null && !sourceScript.getContainer().shouldDebug()) {
            fullMessage.append(ChatColor.GRAY).append(" ... ").append(ChatColor.RED).append("Enable debug on the script for more information.");
        }
        finalOutputDebugText(fullMessage.toString(), sourceQueue, reformat);
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

    // <--[event]
    // @Events
    // server generates exception
    //
    // @Regex ^on script generates exception$
    //
    // @Triggers when an exception occurs on the server.
    // @Context
    // <context.message> returns the Exception message.
    // <context.full_trace> returns the full exception trace+message output details.
    // <context.type> returns the type of the error. (EG, NullPointerException).
    // <context.queue> returns the queue that caused the exception, if any.
    //
    // @Determine
    // "CANCELLED" to stop the exception from showing in the console.
    // -->
    public static void echoError(Throwable ex) {
        echoError(null, ex);
    }

    public static void echoError(ScriptQueue source, Throwable ex) {
        if (source == null) {
            source = CommandExecutor.currentQueue;
        }
        String errorMessage = getFullExceptionMessage(ex);
        if (throwErrorEvent) {
            throwErrorEvent = false;
            Map<String, ObjectTag> context = new HashMap<>();
            Throwable thrown = ex;
            if (ex.getCause() != null) {
                thrown = ex.getCause();
            }
            context.put("message", new ElementTag(thrown.getMessage()));
            context.put("full_trace", new ElementTag(errorMessage));
            context.put("type", new ElementTag(thrown.getClass().getSimpleName()));
            if (source != null) {
                context.put("queue", new QueueTag(source));
            }
            ScriptEntry entry = (source != null ? source.getLastEntryExecuted() : null);
            List<String> Determinations = OldEventManager.doEvents(Arrays.asList("server generates exception"),
                    entry == null ? new BukkitScriptEntryData(null, null) : entry.entryData, context);
            throwErrorEvent = true;
            for (String Determination : Determinations) {
                if (Determination.equalsIgnoreCase("CANCELLED")) {
                    return;
                }
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
            echoError(source, errorMessage, false);
        }
        throwErrorEvent = wasThrown;
    }

    public static String getFullExceptionMessage(Throwable ex) {
        StringBuilder errorMessage = new StringBuilder();
        errorMessage.append("Internal exception was thrown!\n");
        String prefix = ChatColor.GRAY + "[Error Continued] " + ChatColor.WHITE;
        boolean first = true;
        while (ex != null) {
            errorMessage.append(prefix);
            if (!first) {
                errorMessage.append("Caused by: ");
            }
            errorMessage.append(ex.toString()).append("\n");
            for (StackTraceElement ste : ex.getStackTrace()) {
                errorMessage.append(prefix).append(ste.toString()).append("\n");
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
        finalOutputDebugText(ChatColor.YELLOW + "+> ["
                + callerName + "] "
                + ChatColor.WHITE + trimMessage(message), null);
    }

    public static void log(DebugElement element, String string) {
        if (!showDebug) {
            return;
        }
        StringBuilder sb = new StringBuilder(24);

        switch (element) {
            case Footer:
                sb.append(ChatColor.LIGHT_PURPLE).append("+---------------------+");
                break;

            case Header:
                sb.append(ChatColor.LIGHT_PURPLE).append("+- ").append(string).append(" ---------+");
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
                .replace("<GR>", ChatColor.GREEN.toString())
                .replace("<A>", ChatColor.AQUA.toString())
                .replace("<R>", ChatColor.DARK_RED.toString())
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
                Debug.Recording.append(URLEncoder.encode(dateFormat.format(new Date())
                        + " [INFO] " + string.replace(ChatColor.COLOR_CHAR, (char) 0x01) + "\n"));
            }

            string = Settings.debugPrefix() + string;

            // Send buffer to the player
            commandSender.sendMessage(showColor ? string : ChatColor.stripColor(string));
        }
    }
}
