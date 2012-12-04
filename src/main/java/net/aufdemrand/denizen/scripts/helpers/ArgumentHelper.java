package net.aufdemrand.denizen.scripts.helpers;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.scripts.ScriptEngine.QueueType;
import net.aufdemrand.denizen.utilities.debugging.Debugger;

/**
 * The dScript Argument Helper will aide you in parsing and formatting arguments from a ScriptEntry.  The Argument Helper (aH)
 * object reference is included with AbstractCommand, AbstractRequirement, AbstractActivity and AbstractTrigger.
 * 
 * @author Jeremy Schroeder
 *
 */

public class ArgumentHelper {

    Debugger dB;
    Denizen denizen;

    public enum ArgumentType {
        String, Word, Integer, Double, Float, Boolean, Custom
    }

    public ArgumentHelper(Denizen denizen) {
        this.denizen = denizen;
        dB = denizen.getDebugger();
    }

    /**
     *   dScript Argument         JAVA Parse Method      JAVA Get Method       Returns       Pattern Matcher (Case_Insensitive)
     *  +------------------------+----------------------+---------------------+-------------+----------------------------------------------
     *   NPCID:#                  parsed in executer     done in executer      DenizenNPC    (matched against current NPCs)
     *   PLAYER:player_name       parsed in executer     done in executer      Player        (matched against online/offline Players)
     *   TOGGLE:true|false        matchesToggle(arg)     getBooleanFrom(arg)   boolean       trigger:(true|false)
     *   DURATION:#               matchesDuration(arg)   getIntegerFrom(arg)   int           duration:\d+ 
     *   SCRIPT:script_name       matchesScript(arg)     getStringFrom(arg)    String        script:.+ matched against loaded Scripts    
     *   LOCATION:x#,y#,z#,world  matchesLocation(arg)   getLocationFrom(arg)  Location      location:\d+,\d+,\d+,\w+
     *   QUEUE:Queue_Type         matchesQueueType(arg)  getQueueFrom(arg)     QueueType     queue:(?:player|task|denizen)
     *   QTY:#                    matchesQuantity(arg)   getQuantityFrom(arg)  int           qty:\d+ 
     *   ITEM:Material_Type(:#)   matchesItem(arg)       getItemFrom(arg)      ItemStack     item:\.+:(\d+) matched against Material_Type
     *   ITEM:#(:#)               matchesItem(arg)       getItemFrom(arg)      ItemStack     item:\d+|(\d+:\d+)    
     *   # (Integer)              matchesInteger(arg)    getIntegerFrom(arg)   int           \d+
     *   #.# (Double)             matchesDouble(arg)     getDoubleFrom(arg)    double        \d+\.\d+
     *   #.## (Float)             matchesFloat(arg)      getFloatFrom(arg)     float         ^[-+]?[0-9]+[.]?[0-9]*([eE][-+]?[0-9]+)
     *   'NPCs rule!' (String)    matchesString(arg)     getStringFrom(arg)    String        \.+
     *   single_word (Word)       matchesWord(arg)       getStringFrom(arg)    String        \w+
     *   string|string2 (List)    matchesString(arg)     getListFrom(arg)      List<String>  String.split("|")
     */

    final Pattern durationPattern = Pattern.compile("duration:(\\d+)", Pattern.CASE_INSENSITIVE);
    final Pattern scriptPattern = Pattern.compile("script:.+", Pattern.CASE_INSENSITIVE);
    final Pattern locationPattern = Pattern.compile("location:\\d+,\\d+,\\d+,\\w+", Pattern.CASE_INSENSITIVE);
    final Pattern queuetypePattern = Pattern.compile("(?:queue|queuetype):(?:player|player_task|npc)", Pattern.CASE_INSENSITIVE);
    final Pattern quantityPattern = Pattern.compile("qty:\\d+", Pattern.CASE_INSENSITIVE);
    final Pattern togglePattern = Pattern.compile("toggle:(true|false)", Pattern.CASE_INSENSITIVE);
    final Pattern materialPattern = Pattern.compile("[a-zA-Z\\x5F]+", Pattern.CASE_INSENSITIVE);
    final Pattern materialDataPattern = Pattern.compile("[a-zA-Z]+?:\\d+", Pattern.CASE_INSENSITIVE);
    final Pattern itemIdPattern = Pattern.compile("(?:(item:)|)\\d+");
    final Pattern itemIdDataPattern = Pattern.compile("(?:(item:)|)(\\d+)(:)(\\d+)");
    final Pattern integerPattern = Pattern.compile("\\d+");
    final Pattern doublePattern = Pattern.compile("\\d+(\\.\\d+|)");
    final Pattern floatPattern = Pattern.compile("^[-+]?[0-9]+[.]?[0-9]*([eE][-+]?[0-9]+)?$");
    final Pattern stringPattern = Pattern.compile("\\.+", Pattern.CASE_INSENSITIVE);
    final Pattern wordPattern = Pattern.compile("\\w+", Pattern.CASE_INSENSITIVE);
    
    /*
     * Argument Matchers
     */

    /**
     * In practice, the remaining standard arguments should be used whenever possible to keep things consistant across the entire 
     * Denizen experience. Should you need to use custom arguments, however, there is support for that as well. After all, while using 
     * standard arguments is nice, you should never reach. Arguments should make as much sense to the user as possible.
     * 
     * Small code examples:
     * 0  if (aH.matchesArg("HARD", arg))
     * 1     hardness = Hardness.HARD;
     * 
     * 0 if (aH.matchesValueArg("HARDNESS", arg, ArgumentType.Word)) 
     * 1     try { hardness = Hardness.valueOf(aH.getStringFrom(arg));}
     * 2     catch (Exception e) { dB.echoError("Invalid HARDNESS!")}
     * 
     * 
     * Methods for dealing with Custom Denizen Arguments
     * 
     * DenizenScript Argument   JAVA Parse Method                                 JAVA Get Method       Returns    
     * +------------------------+-------------------------------------------------+---------------------+---------------------------------
     * CUSTOM_ARGUMENT          matchesArg("CUSTOM_ARGUMENT", arg)                None. No value        boolean     
     * CSTM_ARG:value           MatchesValueArg("CSTM_ARG", arg, ArgumentType)    get____From(arg)      depends on get method
     * 
     * 
     * Note: ArgumentType will filter the type of value to match to. If anything should be excepted as the value, or you plan
     * on parsing the value yourself, use ArgumentType.Custom.
     * 
     * Valid ArgumentTypes: ArgumentType.String, ArgumentType.Word, ArgumentType.Integer, ArgumentType.Double, ArgumentType.Float,
     * and ArgumentType.Custom
     * 
     */

    Matcher m;
    
    public boolean matchesValueArg(String argumentName, String argument, ArgumentType type) {
        if (argument == null) return false;
        if (argument.split(":").length == 1) return false;
        if (!argument.toUpperCase().contains(argumentName.toUpperCase() + ":")) return false;
        argument = argument.split(":", 2)[1];

        switch (type) {
        case Word:
            m = wordPattern.matcher(argument);
            return m.matches();

        case Integer:
            m = integerPattern.matcher(argument);
            return m.matches();

        case Double:
            m = doublePattern.matcher(argument);
            return m.matches();

        case Float:
            m = floatPattern.matcher(argument);
            return m.matches();
            
        case Boolean:
            if (argument.equalsIgnoreCase("true")) return true;
            else return false;

        default:
            return true;
        }
    }

    public boolean matchesArg(String name, String argument) {
        if (argument.toUpperCase().equals(name.toUpperCase())) return true;
        return false;
    }

    public boolean matchesInteger(String argument) {
        m = integerPattern.matcher(argument);
        return m.matches();
    }

    public boolean matchesDouble(String argument) {
        m = doublePattern.matcher(argument);
        return m.matches();
    }

    public boolean matchesLocation(String argument) {
        m = locationPattern.matcher(argument);
        return m.matches();
    }

    public boolean matchesItem(String argument) {
        m = itemIdPattern.matcher(argument);
        if (m.matches())
            return true;
        m = itemIdDataPattern.matcher(argument);
        if (m.matches())
            return true;
        // Strip ITEM: to check against a valid Material
        if (argument.toUpperCase().startsWith("ITEM:"))
            argument = argument.substring(5);
        m = materialPattern.matcher(argument);
        if (m.matches()) {
            // Check against Materials
            for (Material mat : Material.values())
                if (mat.name().equalsIgnoreCase(argument))
                    return true;
        }
        m = materialDataPattern.matcher(argument);
        if (m.matches()) {
            if (argument.split(":").length == 2)
                argument = argument.split(":")[0];
            for (Material mat : Material.values())
                if (mat.name().equalsIgnoreCase(argument))
                    return true;
        }
        // No match!
        return false;
    }

    public boolean matchesDuration(String regex) {
        m = durationPattern.matcher(regex);
        return m.matches();
    }

    public boolean matchesToggle(String regex) {
        m = togglePattern.matcher(regex);
        return m.matches();
    }

    public boolean matchesQueueType(String regex) {
        m = queuetypePattern.matcher(regex);
        return m.matches();
    }

    public boolean matchesScript(String regex) {
        m = scriptPattern.matcher(regex);
        // Check if script exists by looking for  Script Name:
        //                                          Type: ...
        if (m.matches() && denizen.getScripts().contains(regex.toUpperCase() + ".TYPE"))
            return true;
        return false;
    }

    public boolean matchesQuantity(String regex) {
        m = quantityPattern.matcher(regex);
        return m.matches();
    }

    /*
     * Argument Extractors
     */

    public boolean getBooleanFrom(String argument) {
        if (argument.split(":").length >= 2)
            return Boolean.valueOf(argument.split(":")[1]).booleanValue();
        else return false;
    }

    public String getStringFrom(String argument) {
        if (argument.split(":").length >= 2)
            return argument.split(":")[1];
        else return argument;
    }

    public Player getPlayerFrom(String argument) {
        if (argument.split(":").length >= 2)
            return denizen.getServer().getPlayer(argument.split(":")[1]);
        else return denizen.getServer().getPlayer(argument);
    }

    public Location getLocationFrom(String argument) {
        argument = argument.split(":", 2)[1];
        String[] num = argument.split(",");
        Location location = null;
        try {
            location = new Location(Bukkit.getWorld(num[3]), Double.valueOf(num[1]), Double.valueOf(num[2]), Double.valueOf(num[0]));
        } catch (Exception e) { dB.echoError("Invalid Location!"); return null; }
        return location;
    }

    public List<String> getListFrom(String argument) {
        return Arrays.asList(argument.split("\\|"));
    }

    public QueueType getQueueFrom(String argument) {
        try { if (argument.split(":").length >= 2)
            return QueueType.valueOf(argument.split(":")[1].toUpperCase());
        else return QueueType.valueOf(argument.toUpperCase());
        } catch (Exception e) { dB.echoError("Invalid Queuetype!"); }
        return null;
    }

    public Integer getIntegerFrom(String argument) {
        try { if (argument.split(":").length >= 2)
            return Integer.valueOf(argument.split(":")[1]);
        else return Integer.valueOf(argument);
        } catch (Exception e) { return 0; }
    }

    public Float getFloatFrom(String argument) {
        try { if (argument.split(":").length >= 2)
            return Float.valueOf(argument.split(":")[1]);
        else return Float.valueOf(argument);
        } catch (Exception e) { return 0f; }
    }

    public Double getDoubleFrom(String argument) {
        try { if (argument.split(":").length >= 2)
            return Double.valueOf(argument.split(":")[1]);
        else return Double.valueOf(argument);
        } catch (Exception e) { return 0.00; }
    }

    public ItemStack getItemFrom(String thisArg) {
        m = itemIdPattern.matcher(thisArg);
        Matcher m2 = itemIdDataPattern.matcher(thisArg);
        Matcher m3 = materialPattern.matcher(thisArg);
        Matcher m4 = materialDataPattern.matcher(thisArg);
        ItemStack stack = null;
        try {
            if (m.matches())
                stack = new ItemStack(Integer.valueOf(thisArg));
            else if (m2.matches()) {
                stack = new ItemStack(Integer.valueOf(thisArg.split(":")[0]));
                stack.setDurability(Short.valueOf(thisArg.split(":")[1]));
            } else if (m3.matches()) {
                stack = new ItemStack(Material.valueOf(thisArg.toUpperCase()));
            } else if (m4.matches()) {
                stack = new ItemStack(Material.valueOf(thisArg.split(":")[0].toUpperCase()));
                stack.setDurability(Short.valueOf(thisArg.split(":")[1]));
            }
        } catch (Exception e) { dB.echoError("Invalid item!"); if (denizen.getDebugger().showStackTraces) e.printStackTrace(); }
        return stack;
    }

}
