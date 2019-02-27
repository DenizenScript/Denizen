package net.aufdemrand.denizen.utilities.debugging;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.Settings;
import net.aufdemrand.denizen.flags.FlagManager;
import net.aufdemrand.denizencore.events.OldEventManager;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.objects.dScript;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.CommandExecuter;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.scripts.queues.ScriptQueue;
import net.aufdemrand.denizencore.tags.TagContext;
import net.aufdemrand.denizencore.tags.TagManager;
import net.aufdemrand.denizencore.utilities.debugging.Debuggable;
import net.aufdemrand.denizencore.utilities.debugging.dB.DebugElement;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Preferred method of outputting debugger information with Denizen and
 * denizen-related plugins.
 * <p/>
 * Attempts to unify the style of reporting information to the Console and
 * player with the use of color, headers, footers, and formatting.
 * <p/>
 * <p/>
 * Example, this code:
 * <p/>
 * dB.echoDebug(DebugElement.Header, "Sample debug information");
 * dB.echoDebug("This is an example of a piece of debug information. Parts and pieces " +
 * "of an entire debug sequence may be in completely different classes, so making " +
 * "a unified way to output to the console can make a world of difference with " +
 * "debugging and usability.");
 * dB.echoDebug(DebugElement.Spacer);
 * dB.echoDebug("Here are some examples of a few different ways to log with the logger.");
 * dB.echoApproval("Notable events can nicely show success or approval.");
 * dB.echoError("Your users will be able to easily distinguish problems.");
 * dB.info("...and important pieces of information can be easily spotted.");
 * dB.echoDebug(DebugElement.Footer);
 * <p/>
 * <p/>
 * will produce this output (with color):
 * <p/>
 * 16:05:05 [INFO] +- Sample debug information ------+
 * 16:05:05 [INFO] This is an example of a piece of debug information. Parts
 * and pieces of an entire debug sequence may be in completely
 * different classes, so making a unified way to output to the
 * console can make a world of difference with debugging and
 * usability.
 * 16:05:05 [INFO]
 * 16:05:05 [INFO] Here are some examples of a few different ways to log with the
 * logger.
 * 16:05:05 [INFO]  OKAY! Notable events can nicely show success or approval.
 * 16:05:05 [INFO]  ERROR! Your users will be able to easily distinguish problems.
 * 16:05:05 [INFO] +> ...and important pieces of information can easily be spotted.
 * 16:05:05 [INFO] +---------------------+
 */
public class dB {

    public static boolean showDebug = Settings.showDebug();
    public static boolean showStackTraces = true;
    public static boolean showColor = true;
    public static boolean debugOverride = false;
    public static boolean showSources = false;

    public static List<String> filter = new ArrayList<String>();

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


    // <--[language]
    // @Name 'show_command_reports' player flag
    // @Group Useful flags

    // @Description
    // Giving a player the flag 'show_command_reports' will tell the Denizen dBugger to output
    // command reports to the player involved with the ScriptEntry. This can be useful for
    // script debugging, though it not an all-inclusive view of debugging information.
    //
    // To turn on and turn off the flag, just use:
    // <code>
    // /ex flag <player> show_command_reports
    // /ex flag <player> show_command_reports:!
    // </code>
    //
    // -->

    /**
     * Used by Commands to report how the supplied arguments were parsed.
     * Should be supplied a concatenated String with aH.debugObject() or dObject.debug() of all
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
        echo("<Y>+> <G>Executing '<Y>" + name + "<G>': "
                + trimMessage(report), caller);

        if (caller instanceof ScriptEntry) {
            if (((BukkitScriptEntryData) ((ScriptEntry) caller).entryData).hasPlayer()) {
                if (FlagManager.playerHasFlag(((BukkitScriptEntryData) ((ScriptEntry) caller).entryData)
                        .getPlayer(), "show_command_reports")) {
                    String message = "<Y>+> <G>Executing '<Y>" + name + "<G>': "
                            + trimMessage(report);

                    ((BukkitScriptEntryData) ((ScriptEntry) caller).entryData).getPlayer().getPlayerEntity()
                            .sendRawMessage(message.replace("<Y>", ChatColor.YELLOW.toString())
                                    .replace("<G>", ChatColor.DARK_GRAY.toString())
                                    .replace("<A>", ChatColor.AQUA.toString())
                                    .replace("<R>", ChatColor.DARK_RED.toString())
                                    .replace("<W>", ChatColor.WHITE.toString()));
                }
            }
        }
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
                sb.append(ChatColor.LIGHT_PURPLE).append("+- ").append(string).append(" ---------+");
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
        if (net.aufdemrand.denizencore.utilities.debugging.dB.verbose && caller != null) {
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
        ConsoleSender.sendMessage(ChatColor.LIGHT_PURPLE + " " + ChatColor.GREEN + "OKAY! "
                + ChatColor.WHITE + message);
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
    //
    // @Determine
    // "CANCELLED" to stop the error from showing in the console.
    // -->
    public static void echoError(String message) {
        echoError(null, message);
    }

    public static void echoError(ScriptQueue source, String message) {
        if (source == null) {
            source = CommandExecuter.currentQueue;
        }
        dScript script = null;
        if (source != null && source.getEntries().size() > 0 && source.getEntries().get(0).getScript() != null) {
            script = source.getEntries().get(0).getScript();
        }
        else if (source != null && source.getLastEntryExecuted() != null && source.getLastEntryExecuted().getScript() != null) {
            script = source.getLastEntryExecuted().getScript();
        }
        if (throwErrorEvent) {
            throwErrorEvent = false;
            Map<String, dObject> context = new HashMap<String, dObject>();
            context.put("message", new Element(message));
            if (source != null) {
                context.put("queue", source);
            }
            if (script != null) {
                context.put("script", script);
            }
            List<String> events = new ArrayList<String>();
            events.add("script generates error");
            if (script != null) {
                events.add(script.identifySimple() + " generates error");
            }
            ScriptEntry entry = (source != null ? source.getLastEntryExecuted() : null);
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
        ConsoleSender.sendMessage(ChatColor.LIGHT_PURPLE + " " + ChatColor.RED + "ERROR" +
                (script != null ? " in script '" + script.getName() + "'" : "")
                + (source != null ? " in queue '" + source.id + "'" : "") + "! "
                + ChatColor.WHITE + message);
        if (net.aufdemrand.denizencore.utilities.debugging.dB.verbose && depthCorrectError == 0) {
            depthCorrectError++;
            try {
                throw new RuntimeException("Verbose info for above error");
            }
            catch (Throwable e) {
                echoError(source, e);
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
            source = CommandExecuter.currentQueue;
        }
        if (throwErrorEvent) {
            throwErrorEvent = false;
            Map<String, dObject> context = new HashMap<String, dObject>();
            Throwable thrown = ex;
            if (ex.getCause() != null) {
                thrown = ex.getCause();
            }
            context.put("message", new Element(thrown.getMessage()));
            context.put("type", new Element(thrown.getClass().getSimpleName()));
            context.put("queue", source);
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
            dB.echoError(source, "Exception! Enable '/denizen debug -s' for the nitty-gritty.");
        }
        else {
            dB.echoError(source, "Internal exception was thrown!");
            StringBuilder errorMesage = new StringBuilder();
            String prefix = ConsoleSender.dateFormat.format(new Date()) + " [SEVERE] ";
            boolean first = true;
            while (ex != null) {
                errorMesage.append(prefix + (first ? "" : "Caused by: ") + ex.toString() + "\n");
                for (StackTraceElement ste : ex.getStackTrace()) {
                    errorMesage.append(prefix + ste.toString() + "\n");
                }
                if (ex.getCause() == ex) {
                    break;
                }
                ex = ex.getCause();
                first = false;
            }
            dScript script = null;
            if (source != null && source.getEntries().size() > 0 && source.getEntries().get(0).getScript() != null) {
                script = source.getEntries().get(0).getScript();
            }
            ConsoleSender.sendMessage(ChatColor.LIGHT_PURPLE + " " + ChatColor.RED + "ERROR" +
                    (script != null ? " in script '" + script.getName() + "'" : "")
                    + (source != null ? " in queue '" + source.id + "'" : "") + "! "
                    + ChatColor.WHITE + errorMesage.toString(), false);
        }
        throwErrorEvent = wasThrown;
    }

    private static final Map<Class<?>, String> classNameCache = new WeakHashMap<Class<?>, String>();

    private static class SecurityManagerTrick extends SecurityManager {
        @Override
        @SuppressWarnings("rawtypes")
        protected Class[] getClassContext() {
            return super.getClassContext();
        }
    }

    public static void log(String message) {
        if (!showDebug) {
            return;
        }
        Class[] classes = new SecurityManagerTrick().getClassContext();
        Class caller = classes.length > 2 ? classes[2] : dB.class;
        String callerName = classNameCache.get(caller);
        if (callerName == null) {
            classNameCache.put(caller, callerName = caller.getSimpleName());
        }
        callerName = callerName.length() > 16 ? callerName.substring(0, 12) + "..." : callerName;
        ConsoleSender.sendMessage(ChatColor.YELLOW + "+> ["
                + callerName + "] "
                + ChatColor.WHITE + trimMessage(message));
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

        ConsoleSender.sendMessage(sb.toString());
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
        boolean should_send = true;

        // Attempt to see if the debug should even be sent by checking the
        // script container's 'debug' node.
        if (caller != null) {
            try {

                if (filter.isEmpty()) {
                    should_send = caller.shouldDebug();
                }
                else {
                    should_send = false;
                    for (String criteria : filter) {
                        if (caller.shouldFilter(criteria)) {
                            should_send = true;
                            break;
                        }
                    }
                }

            }
            catch (Exception e) {
                // Had a problem determining whether it should debug, assume true.
                should_send = true;
            }
        }
        return should_send;
    }

    // Handles checking whether the provided debuggable should submit to the debugger
    private static void echo(String string, Debuggable caller) {
        if (shouldDebug(caller)) {
            if (showSources && caller != null) {
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
                ConsoleSender.sendMessage(ChatColor.DARK_GRAY + "[Src:" + ChatColor.GRAY + callerId + ChatColor.DARK_GRAY + "]" + ChatColor.WHITE + string);
            }
            else {
                ConsoleSender.sendMessage(string);
            }
        }
    }


    /**
     * ConsoleSender sends dScript debugging information to the logger
     * will attempt to intelligently wrap any debug information that is more
     * than one line. This is used by the dB static methods which do some
     * additional formatting.
     */
    private static class ConsoleSender {

        // Bukkit CommandSender sends color nicely to the logger, so we'll use that.
        static CommandSender commandSender = null;
        public static SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        static boolean skipFooter = false;

        // Use this method for sending a message
        public static void sendMessage(String string) {
            sendMessage(string, true);
        }
        public static void sendMessage(String string, boolean reformat) {
            if (commandSender == null) {
                commandSender = Bukkit.getServer().getConsoleSender();
            }

            // These colors are used a lot in the debugging of commands/etc, so having a few shortcuts is nicer
            // than having a bunch of ChatColor.XXXX
            string = TagManager.cleanOutputFully(string
                    .replace("<Y>", ChatColor.YELLOW.toString())
                    .replace("<G>", ChatColor.DARK_GRAY.toString())
                    .replace("<A>", ChatColor.AQUA.toString())
                    .replace("<R>", ChatColor.DARK_RED.toString())
                    .replace("<W>", ChatColor.WHITE.toString()));

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
                for (String word : words) { // # of total chars * # of lines - timestamp
                    int strippedLength = ChatColor.stripColor(word).length() + 1;
                    if (length + strippedLength < width) {
                        buffer.append(word).append(" ");
                        length = length + strippedLength;
                    }
                    else {
                        // Increase # of lines to account for
                        length = strippedLength;
                        // Leave spaces to account for timestamp and indent
                        buffer.append("\n                   ").append(word).append(" ");
                    }                 // [01:02:03 INFO]:
                }
                string = buffer.toString();
            }

            // Record current buffer to the to-be-submitted buffer
            if (dB.record) {
                dB.Recording.append(URLEncoder.encode(dateFormat.format(new Date())
                        + " [INFO] " + string.replace(ChatColor.COLOR_CHAR, (char) 0x01) + "\n"));
            }

            // Send buffer to the player
            commandSender.sendMessage(showColor ? string : ChatColor.stripColor(string));
        }
    }
}
