package net.aufdemrand.denizen.utilities.debugging;

import net.aufdemrand.denizen.Settings;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Preferred method of outputting debugger information with Denizen and
 * denizen-related plugins.
 *
 * Attempts to unify the style of reporting information to the Console and
 * player with the use of color, headers, footers, and formatting.
 *
 *
 * Example, this code:
 *
 * dB.echoDebug(DebugElement.Header, "Sample debug information");
 * dB.echoDebug("This is an example of a piece of debug information. Parts and pieces " +
 *         "of an entire debug sequence may be in completely different classes, so making " +
 *         "a unified way to output to the console can make a world of difference with " +
 *         "debugging and usability.");
 * dB.echoDebug(DebugElement.Spacer);
 * dB.echoDebug("Here are some examples of a few different ways to log with the logger.");
 * dB.echoApproval("Notable events can nicely show success or approval.");
 * dB.echoError("Your users will be able to easily distinguish problems.");
 * dB.info("...and important pieces of information can be easily spotted.");
 * dB.echoDebug(DebugElement.Footer);
 *
 *
 * will produce this output (with color):
 *
 * 16:05:05 [INFO] +- Sample debug information ------+
 * 16:05:05 [INFO] This is an example of a piece of debug information. Parts
 *                   and pieces of an entire debug sequence may be in completely
 *                   different classes, so making a unified way to output to the
 *                   console can make a world of difference with debugging and
 *                   usability.
 * 16:05:05 [INFO]
 * 16:05:05 [INFO] Here are some examples of a few different ways to log with the
 *                      logger.
 * 16:05:05 [INFO]  OKAY! Notable events can nicely show success or approval.
 * 16:05:05 [INFO]  ERROR! Your users will be able to easily distinguish problems.
 * 16:05:05 [INFO] +> ...and important pieces of information can easily be spotted.
 * 16:05:05 [INFO] +---------------------+
 *
 *
 * @author Jeremy Schroeder
 *
 */
public class dB {

    public static boolean showDebug = Settings.ShowDebug();
    public static boolean showStackTraces = true;
    public static boolean showScriptBuilder = false;
    public static boolean showColor = true;
    public static boolean showEventsFiring = false;

    public static List<String> filter = new ArrayList<String>();

    public static boolean shouldTrim = true;
    public static int trimSize = 512;
    public static boolean record = false;
    public static StringBuilder Recording = new StringBuilder();
    public static void toggle() { showDebug = !showDebug; }


    /**
     * Can be used with echoDebug(...) to output a header, footer,
     * or a spacer.
     *
     * DebugElement.Header = +- string description ------+
     * DebugElement.Spacer =
     * DebugElement.Footer = +--------------+
     *
     * Also includes color.
     */
    public static enum DebugElement {
        Header, Footer, Spacer
    }



    ////////////
    //  Public debugging methods, toggleable by checking extra criteria as implemented
    //  by the Debuggable interface, which usually checks a ScriptContainer's 'debug' node
    //////


    /**
     * Used by Commands to report how the supplied arguments were parsed.
     * Should be supplied a concatenated String with aH.debugObject() or dObject.debug() of all
     * applicable objects used by the Command.
     *
     * @param caller
     * @param name
     * @param report
     */
    public static void report(Debuggable caller, String name, String report) {
        if (!showDebug) return;
        echo("<Y>+> <G>Executing '<Y>" + name + "<G>': "
                + trimMessage(report), caller);
    }


    // Used by the various parts of Denizen that output debuggable information
    // to help scripters see what is going on. Debugging an element is usually
    // for formatting debug information.
    public static void echoDebug(Debuggable caller, DebugElement element) {
        if (!showDebug) return;
        echoDebug(caller, element, null);
    }


    // Used by the various parts of Denizen that output debuggable information
    // to help scripters see what is going on. Debugging an element is usually
    // for formatting debug information.
    public static void echoDebug(Debuggable caller, DebugElement element, String string) {
        if (!showDebug) return;
        StringBuilder sb = new StringBuilder(24);

        switch(element) {
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
        if (!showDebug) return;
        echo(ChatColor.LIGHT_PURPLE + " " + ChatColor.WHITE + trimMessage(message), caller);
    }

    // TODO: REMOVE AFTER SENTRY STOPS CALLING THESE!
    public static void echoDebug(String message) {
        log("[External Caller] " + message);
    }
    public static void echoDebug(DebugElement de, String message) {
        echoDebug(de.toString() + message);
    }



    /////////////
    // Other public debugging methods (Always show when debugger is enabled)
    ///////


    public static void echoApproval(String message) {
        if (!showDebug) return;
        ConsoleSender.sendMessage(ChatColor.LIGHT_PURPLE + " " + ChatColor.GREEN + "OKAY! "
                + ChatColor.WHITE + message);
    }


    public static void echoError(String message) {
        if (!showDebug) return;
        ConsoleSender.sendMessage(ChatColor.LIGHT_PURPLE + " " + ChatColor.RED + "ERROR! "
                + ChatColor.WHITE + trimMessage(message));
    }


    public static void log(String message) {
        if (!showDebug) return;
        ConsoleSender.sendMessage(ChatColor.YELLOW + "+> ["
                + (sun.reflect.Reflection.getCallerClass(2).getSimpleName().length() > 16 ?
                sun.reflect.Reflection.getCallerClass(2).getSimpleName().substring(0, 12) + "..."
                : sun.reflect.Reflection.getCallerClass(2).getSimpleName()) + "] "
                + ChatColor.WHITE + trimMessage(message));
    }



    ///////////////
    // Private Helper Methods
    /////////


    // Some debug methods trim to keep super-long messages from hitting the console.
    private static String trimMessage(String message) {
        if (!shouldTrim) return message;
        if (message.length() > trimSize)
            message = message.substring(0, trimSize - 1) + "... * snip! *";
        return message;
    }


    // Handles checking whether the provided debuggable should submit to the debugger
    private static void echo(String string, Debuggable caller) {
        boolean should_send = true;

        // Attempt to see if the debug should even be sent by checking the
        // script container's 'debug' node.
        try {

            if (filter.isEmpty())
                should_send = caller.shouldDebug();

            else {
                should_send = false;
                for (String criteria : filter)
                    if (caller.shouldFilter(criteria)) {
                        should_send = true;
                        break;
                    }
            }

        } catch (Exception e) {
            // Had a problem determining whether it should debug, assume true.
            should_send = true;
        }

        if (should_send) ConsoleSender.sendMessage(string);
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
        static SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        static boolean skipFooter = false;

        // Use this method for sending a message
        public static void sendMessage(String string) {
            if (commandSender == null) commandSender = Bukkit.getServer().getConsoleSender();

            // These colors are used a lot in the debugging of commands/etc, so having a few shortcuts is nicer
            // than having a bunch of ChatColor.XXXX
            string = string
                    .replace("<Y>", ChatColor.YELLOW + "")
                    .replace("<G>", ChatColor.DARK_GRAY + "")
                    .replace("<A>", ChatColor.AQUA + "");

            // 'Hack-fix' for disallowing multiple 'footers' to print in a row
            if (string.equals(ChatColor.LIGHT_PURPLE + "+---------------------+")) {
                if (!skipFooter) skipFooter = true;
                else { return; }
            } else skipFooter = false;

            // Create buffer for wrapping debug text nicely. This is mostly needed for Windows logging.
            String[] words = string.split(" ");
            String buffer = "";
            int length = 0;
            for (String word : words) { // # of total chars * # of lines - timestamp
                if (length + ChatColor.stripColor(word).length() + 1  < Settings.ConsoleWidth()) {
                    buffer = buffer + word + " ";
                    length = length + ChatColor.stripColor(word).length() + 1;
                } else {
                    // Increase # of lines to account for
                    length = ChatColor.stripColor(word).length() + 1;
                    // Leave spaces to account for timestamp and indent
                    buffer = buffer + "\n" + "                   " + word + " ";
                }                          // 16:05:06 [INFO]
            }

            // Record current buffer to the to-be-submitted buffer
            if (dB.record) dB.Recording.append(URLEncoder.encode(dateFormat.format(new Date())
                    + " [INFO] " + buffer.replace(ChatColor.COLOR_CHAR, (char)0x01) + "\n"));

            // Send buffer to the player
            commandSender.sendMessage(showColor ? buffer : ChatColor.stripColor(buffer));
        }
    }

}
