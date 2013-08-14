package net.aufdemrand.denizen.utilities.debugging;

import net.aufdemrand.denizen.Settings;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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

    static ConsoleSender cs = new ConsoleSender();
    
    public static boolean debugMode = Settings.ShowDebug();
    public static boolean showStackTraces = true;
    public static boolean showScriptBuilder = false;
    public static boolean showColor = true;

    /**
     * Can be used with echoDebug(...) to output a header, footer, 
     * or a spacer.
     * 
     * DebugElement.Header = +- string description ------+
     * DebugElement.Spacer = 
     * DebugElement.Footer = +--------------+
     *    
     * Also includes color.
     *
     */
    public static enum DebugElement {
        Header,    Footer, Spacer
    }

    public static void report(String name, String report) {
        dB.echoDebug("<Y>+> <G>Executing '<Y>" + name + "<G>': " + trimMessage(report));
    }
    
    /**
     * ConsoleSender sends dScript debugging information to the logger
     * will attempt to intelligently wrap any debug information that is more
     * than one line. This is used by the dB static methods which do some
     * additional formatting.
     * 
     * Example:
     * 
     * 16:05:05 [INFO] +- Executing command: ENGRAVE/aufdemrand ------+
     * 16:05:05 [INFO]  ...set TYPE: 'TARGET:fullwall'
     * 16:05:05 [INFO]  Engraving 'DIAMOND_CHESTPLATE' with an inscription of
                         'Mastaba'.
     * 16:05:05 [INFO] +---------------------+
     * 16:05:05 [INFO] +- Executing command: SUPERZAP ------+
     * 16:05:05 [INFO]  ERROR! SUPERZAP is an invalid dScript command! Are you
                          sure the command loaded?
     * 16:05:05 [INFO] +---------------------+
     * 
     */
    private static class ConsoleSender {
        // Bukkit CommandSender sends color nicely to the logger.
        static CommandSender commandSender = null;
        static boolean skipFooter = false;
        public static void sendMessage(String string) {
            if (commandSender == null) commandSender = Bukkit.getServer().getConsoleSender(); 

            // These colors are used a lot in the debugging of commands/etc, so having a few shortcuts is nicer
            // than having a bunch of ChatColor.XXXX
            string = string.replace("<Y>", ChatColor.YELLOW + "").replace("<G>", ChatColor.DARK_GRAY + "")
                    .replace("<A>", ChatColor.AQUA + "");

            // 'Hack-fix' for disallowing multiple 'footers' to print in a row
            if (string.equals(ChatColor.LIGHT_PURPLE + "+---------------------+")) {
                if (!skipFooter) skipFooter = true;
                else { return; }
            } else skipFooter = false;

            // Create buffer for wrapping debug text nicely.
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
            // Send buffer to the player
            if (showColor)
                commandSender.sendMessage(buffer);
            else commandSender.sendMessage(ChatColor.stripColor(buffer));
        }
    }
    

    // TODO: Clean up messages.
    // Plan is to have all core debugging dialog nicely organized here. Outside plugins which use the
    // debugger can use this as well, but the main goal would be to ensure that all 'output' is uniform.
    public enum Messages {
        
        ERROR_NO_NPCID ("No 'NPCID:#' argument."),
        ERROR_NO_PLAYER ("No 'PLAYER:player_name' argument."),
        ERROR_NO_TEXT("No 'TEXT' argument."),
        ERROR_NO_PLAYER_IN_SCRIPTENTRY("No Player object in the ScriptEntry. Use 'PLAYER:player_name' argument."),
        ERROR_INVALID_ANCHOR("Missing 'TEXT' argument."),
        ERROR_INVALID_NOTABLE("Missing 'TEXT' argument."),
        ERROR_MISSING_LOCATION("Missing 'LOCATION'."),
        ERROR_NO_SCRIPT("Missing 'SCRIPT' argument."),
        ERROR_MISSING_OTHER("Missing '%s' argument."),
        ERROR_LOTS_OF_ARGUMENTS("Too many arguments. Are there misplaced quotes?"),
        ERROR_UNKNOWN_ARGUMENT("Unknown argument: '%s' Check spelling and syntax."),
        DEBUG_SET_TEXT("...set TEXT: '%s'"),
        DEBUG_TOGGLE("...TOGGLE: '%s'"), 
        DEBUG_SET_DURATION("...set DURATION: '%s'"), 
        DEBUG_SET_GLOBAL("...set GLOBAL."), 
        DEBUG_SET_SCRIPT("...set SCRIPT: '%s'"), 
        DEBUG_SET_QUANTITY("...set QUANTITY: '%s'"), 
        DEBUG_SET_LOCATION("...set LOCATION: '%s'"),
        DEBUG_SET_TYPE("...set TYPE: '%s'"),
        DEBUG_SET_RANGE("...set RANGE: '%s'"),
        DEBUG_SET_ITEM("...set ITEM: '%s'"),
        DEBUG_SET_NAME("...set NAME: '%s'"),
        DEBUG_SET_RADIUS("...set RADIUS: '%s'"),
        DEBUG_SET_COOLDOWN("...set COOLDOWN: '%s'"),
        DEBUG_SET_COMMAND("...set COMMAND: '%s'"),
        DEBUG_SET_FLAG_ACTION("...set FLAG ACTION: '%s'"),
        DEBUG_SET_FLAG_TYPE("...set FLAG TYPE: '%s'"), 
        ERROR_INVALID_ENTITY("Invalid entity!"),  
        ERROR_INVALID_ITEM("Invalid item!"), 
        ERROR_PLAYER_NPCS_ONLY("NPC must be Human (Player)!"), 
        DEBUG_SET_STEP("...set STEP: '%s'"), 
        DEBUG_RANDOMLY_SELECTED_STEP("...randomly selected step '%s'"), 
        ERROR_CANCELLING_DELAYED_TASK("Unable to cancel previously delayed task!"), 
        DEBUG_RUNNING_DELAYED_TASK(ChatColor.YELLOW + "// DELAYED // Running delayed task '%s'"), 
        DEBUG_SETTING_DELAYED_TASK(ChatColor.YELLOW + "// DELAYED // Setting delayed task '%s'"), 
        ERROR_NO_WORLD("No valid world specified!"), 
        ERROR_INVALID_WORLD("Invalid world!"); 
        
        @Override
        public String toString() {
            return error;
        }
        
        private String error;
        
        private Messages(String error) {
            this.error = error;
        }
    }

    /*
     *  Communicate with the Logger
     */

    public static void echoDebug(DebugElement element) {
        echoDebug(element, null);
    }
    
    public static void echoDebug(Messages message, String arg) {
        if (!debugMode) return;
        ConsoleSender.sendMessage(ChatColor.LIGHT_PURPLE + " " + ChatColor.WHITE + String.format(message.toString(), arg));
    }
    
    public static void echoDebug(DebugElement element, String string) {
        if (!debugMode) return;
        if (element == DebugElement.Footer) 
            ConsoleSender.sendMessage(ChatColor.LIGHT_PURPLE + "+---------------------+");
        else if (element == DebugElement.Header)
            ConsoleSender.sendMessage(ChatColor.LIGHT_PURPLE + "+- " + string + " ------+");
        else if (element == DebugElement.Spacer)
            ConsoleSender.sendMessage(ChatColor.LIGHT_PURPLE + "");
    }
    
    public static void echoDebug(String message, String arg) {
        if (!debugMode) return;
        ConsoleSender.sendMessage(ChatColor.LIGHT_PURPLE + " " + ChatColor.WHITE + String.format(message, arg));
    }

    public static void echoDebug(String message) {
        if (!debugMode) return;
        ConsoleSender.sendMessage(ChatColor.LIGHT_PURPLE + " " + ChatColor.WHITE + message);
    }

    public static void echoApproval(String message, String arg) {
        if (!debugMode) return;
        ConsoleSender.sendMessage(ChatColor.LIGHT_PURPLE + " " + ChatColor.GREEN + "OKAY! " + ChatColor.WHITE + String.format(message, arg));
    }

    public static void echoApproval(String message) {
        if (!debugMode) return;
        ConsoleSender.sendMessage(ChatColor.LIGHT_PURPLE + " " + ChatColor.GREEN + "OKAY! " + ChatColor.WHITE + message);
    }
    
    public static void echoError(String message) {
        if (!debugMode) return;
        ConsoleSender.sendMessage(ChatColor.LIGHT_PURPLE + " " + ChatColor.RED + "ERROR! " + ChatColor.WHITE + trimMessage(message));
    }

    public static void echoError(Messages message) {
        if (!debugMode) return;
        ConsoleSender.sendMessage(ChatColor.LIGHT_PURPLE + " " + ChatColor.RED + "ERROR! " + ChatColor.WHITE + trimMessage(message.toString()));
    }

    public static void echoError(Messages message, String arg) {
        if (!debugMode) return;
        ConsoleSender.sendMessage(ChatColor.LIGHT_PURPLE + " " + ChatColor.GREEN + "OKAY! " + ChatColor.WHITE + String.format(message.toString(), arg));
    }
    
    public static void echoError(String message, String arg) {
        if (!debugMode) return;
        ConsoleSender.sendMessage(ChatColor.LIGHT_PURPLE + " " + ChatColor.RED + "ERROR! " + ChatColor.WHITE + String.format(message, arg));
    }

    @SuppressWarnings("restriction")
    public static void log(String message, String arg) {
        if (!debugMode) return;
        ConsoleSender.sendMessage(ChatColor.YELLOW + "+> ["
                + (sun.reflect.Reflection.getCallerClass(2).getSimpleName().length() > 16 ?
                sun.reflect.Reflection.getCallerClass(2).getSimpleName().substring(0, 12) + "..."
                : sun.reflect.Reflection.getCallerClass(2).getSimpleName()) + "] "
                + ChatColor.WHITE + String.format(trimMessage(message), arg));
    }

    @SuppressWarnings("restriction")
    public static void log(Messages message, String arg) {
        if (!debugMode) return;
        ConsoleSender.sendMessage(ChatColor.YELLOW + "+> ["
                + (sun.reflect.Reflection.getCallerClass(2).getSimpleName().length() > 16 ?
                sun.reflect.Reflection.getCallerClass(2).getSimpleName().substring(0, 12) + "..."
                : sun.reflect.Reflection.getCallerClass(2).getSimpleName()) + "] "
                + ChatColor.WHITE + String.format(message.toString(), arg));
    }
    
    @SuppressWarnings("restriction")
    public static void log(String message) {
        if (!debugMode) return;
        ConsoleSender.sendMessage(ChatColor.YELLOW + "+> ["
                + (sun.reflect.Reflection.getCallerClass(2).getSimpleName().length() > 16 ?
                sun.reflect.Reflection.getCallerClass(2).getSimpleName().substring(0, 12) + "..."
                : sun.reflect.Reflection.getCallerClass(2).getSimpleName()) + "] " + ChatColor.WHITE + trimMessage(message));
    }
    
    /* 
     * These methods do NOT require DebugMode to be enabled 
     */

    public static void notify(Player player, String message, String arg) {
        player.sendMessage(ChatColor.YELLOW + "+> " + ChatColor.WHITE + String.format(message, arg));
    }

    public static void notify(Player player, String message) {
        player.sendMessage(ChatColor.YELLOW + "+> " + ChatColor.WHITE + ChatColor.WHITE + message);
    }

    public static void toggle() {
        debugMode = !debugMode;
    }
    
    private static String trimMessage(String message) {
        if (message.length() > 256)
            message = message.substring(0, 255) + "... *snip*";
        return message;
    }

}