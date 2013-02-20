package net.aufdemrand.denizen.utilities.arguments;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.scripts.ScriptQueue;
import net.aufdemrand.denizen.scripts.ScriptRegistry;
import net.aufdemrand.denizen.scripts.commands.core.NewCommand;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.gmail.nossr50.skills.utilities.SkillType;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The dScript Argument Helper will aide you in parsing and formatting arguments from a 
 * dScript argument string (such as those found in a ScriptEntry.getArguments() method).
 *
 * @author aufdemrand
 *
 */
public class aH {

    public enum ArgumentType {
        LivingEntity, Item, Boolean, Custom, Double, Float, Integer, String, Word, Location, Script, Duration
    }

    public static String debugObj(String prefix, String value) {
        return "<G>" + prefix + "='<Y>" + value + "<G>'  ";
    }

    public static String debugUniqueObj(String prefix, String id, String value) {
        return "<G>" + prefix + "='<A>" + id + "<Y>(" + value + ")<G>'  ";
    }

    final static Pattern doublePtrn = Pattern.compile("(?:-|)(?:(?:\\d+)|)(?:(?:\\.\\d+)|)");
    final static Pattern floatPtrn = Pattern.compile("^[-+]?[0-9]+[.]?[0-9]*([eE][-+]?[0-9]+)?$");
    final static Pattern integerPtrn = Pattern.compile("(?:-|)\\d+");
    final static Pattern wordPtrn = Pattern.compile("\\w+");

    /**
     * <p>Used to determine if a argument string matches a non-valued custom argument.
     * If a dScript valued argument (such as PLAYER:NAME) is passed, this method
     * will always return false. Also supports multiple argument names, separated by a
     * comma (,) character. This method will trim() each name specified.</p>
     *
     * <b>Example use of '<tt>aH.matchesArg("NOW, LATER", arg)</tt>':</b>
     * <ol>
     * <tt>arg = "NOW"</tt> will return true.<br>
     * <tt>arg = "NEVER"</tt> will return false.<br>
     * <tt>arg = "LATER:8PM"</tt> will return false.<br>
     * <tt>arg = "LATER"</tt> will return true.
     * </ol>
     *
     * @param names the valid argument names to match
     * @param arg the dScript argument string
     * @return true if matched, false if not
     *
     */
    public static boolean matchesArg(String names, String arg) {
        if (names.split(",").length == 1) {
            if (arg.toUpperCase().equals(names.toUpperCase())) return true;
        } else {
            for (String string : names.split(","))
                if (arg.split(":")[0].equalsIgnoreCase(string.trim())) return true;
        }
        return false;
    }


    /**
     * <p>Used to match a custom argument with a value. In practice, the standard
     * arguments should be used whenever possible to keep things consistent across
     * the entire 'dScript experience'. Should you need to use custom arguments,
     * however, this method provides some support. After all, while using standard
     * arguments is nice, you should never reach. Arguments should make as much
     * sense to the user/script writer as possible.</p>
     *
     * <b>Small code example:</b>
     * <ol>
     * <tt>0 if (aH.matchesValueArg("HARDNESS", arg, ArgumentType.Word))</tt><br>
     * <tt>1     try { </tt><br>
     * <tt>2        hardness = Hardness.valueOf(aH.getStringFrom(arg).toUpperCase());</tt><br>
     * <tt>3     } catch (Exception e) { </tt><br>
     * <tt>4		dB.echoError("Invalid HARDNESS!") </tt><br>
     * <tt>5 }</tt><br>
     * </ol>
     *
     * <p>Note: Like {@link #matchesArg(String, String)}, matchesValueArg(String)
     * supports multiple argument names, separated by a comma (,) character. This method
     * will trim() each name specified.</p>
     *
     * <p>Also requires a specified ArgumentType, which will filter the type of value
     * to match to. If anything should be excepted as the value, or you plan
     * on parsing the value yourself, use ArgumentType.Custom, otherwise use an
     * an appropriate ArgumentType. See: {@link ArgumentType}.</p>
     *
     * <b>Example use of '<tt>aH.matchesValueArg("TIME", arg, ArgumentType.Integer)</tt>':</b>
     * <ol>
     * <tt>arg = "TIME:60"</tt> will return true.<br>
     * <tt>arg = "90"</tt> will return false.<br>
     * <tt>arg = "TIME:8 o'clock"</tt> will return false.<br>
     * <tt>arg = "TIME:0"</tt> will return true.
     * </ol>
     *
     * @param names the desired name variations of the argument
     * @param arg the dScript argument string
     * @param type a valid ArgumentType, used for matching values
     * @return true if matched, false otherwise
     *
     */
    public static boolean matchesValueArg(String names, String arg, ArgumentType type) {
        if (arg == null) return false;
        if (arg.split(":").length == 1) return false;

        if (names.split(",").length == 1) {
            if (!arg.split(":")[0].equalsIgnoreCase(names)) return false;

        } else {
            boolean matched = false;
            for (String string : names.split(","))
                if (arg.split(":")[0].equalsIgnoreCase(string.trim())) matched = true;
            if (!matched) return false;
        }

        arg = arg.split(":", 2)[1];
        Matcher m;

        switch (type) {
            case Word:
                m = wordPtrn.matcher(arg);
                if (m.matches()) return true;
                break;

            case Integer:
                m = integerPtrn.matcher(arg);
                if (m.matches()) return true;
                break;

            case Double:
                m = doublePtrn.matcher(arg);
                if (m.matches()) return true;
                break;

            case Float:
                m = floatPtrn.matcher(arg);
                if (m.matches()) return true;
                break;

            case Boolean:
                if (arg.equalsIgnoreCase("true")) return true;
                if (arg.equalsIgnoreCase("false")) return false;
                break;

            case Location:
                return matchesLocation("location:" + arg);

            case Script:
                return matchesLocation("script:" + arg);

            case Item:
                return matchesItem("item:" + arg);

            case LivingEntity:
                final Pattern matchesEntityPtrn =
                        Pattern.compile("(?:.+?|):((ENTITY\\.|PLAYER\\.|NPC\\.).+)|(PLAYER|NPC)", Pattern.CASE_INSENSITIVE);
                m = matchesEntityPtrn.matcher(arg);
                if (m.matches()) return true;
                break;

            case Duration:
                return matchesDuration("duration:" + arg);

            default:
                return true;
        }

        dB.echoError("While parsing '" + arg + "', Denizen has run into a problem. While the " +
                "prefix is correct, the value is not valid. Check documentation for valid value." +
                "Perhaps a replaceable Tag has failed to fill in a value?");
        return false;
    }

    /**
     * <p>Returns a boolean value from a dScript argument string. Also accounts
     * for the argument prefix being passed along, for convenience.</p>
     *
     * <b>Examples:</b>
     * <ol>
     * <tt>'TOGGLE:true'</tt> will return true.<br>
     * <tt>'WORKING:false'</tt> will return false.<br>
     * <tt>'FILL:bleh'</tt> will return false.<br>
     * <tt>'true'</tt> will return true.<br>
     * <tt>'arg'</tt> will return false.
     * </ol>
     *
     * @param arg the argument to check
     * @return true or false
     *
     */
    public static boolean getBooleanFrom(String arg) {
        if (arg.split(":").length >= 2)
            return Boolean.valueOf(arg.split(":", 2)[1]);
        else return Boolean.valueOf(arg);
    }

    /**
     * <p>Returns a primitive double from a dScript argument string. Also accounts
     * for the argument prefix being passed along, for convenience. Never returns
     * null, if not a valid double, 0D will return. If given an integer value,
     * a double representation will be returned.</p>
     *
     * <b>Examples:</b>
     * <ol>
     * <tt>'LEVEL:3.5'</tt> will return '3.5D'.<br>
     * <tt>'INT:1'</tt> will return '1D'.<br>
     * <tt>'1950'</tt> will return '1950D'.<br>
     * <tt>'-.377'</tt> will return '-0.377D'.<br>
     * </ol>
     *
     * @param arg the argument to check
     * @return a double interpretation of the argument
     *
     */
    public static double getDoubleFrom(String arg) {
        try {
            if (arg.split(":").length >= 2)
                return Double.valueOf(arg.split(":", 2)[1]);
            else return Double.valueOf(arg);
        } catch (Exception e) {
            return 0.00;
        }
    }

    /**
     * <p>Returns a Bukkit EntityType from a dScript argument string. Also accounts
     * for the argument prefix being passed along, for convenience. Though the
     * <tt>matchesEntity(...)</tt> requires an <tt>ITEM:</tt> prefix, this method
     * does not, so it can be used in a CustomValueArg.<p>
     *
     * <p>Provides a line of dB output if returning null. For getting saved entities
     * make with the 'NEW ENTITY Command', use {@link #getLivingEntityFrom(String)}</p>
     *
     * <b>Examples:</b>
     * <ol>
     * <tt>'zombie'</tt> will return 'EntityType.Zombie'.<br>
     * <tt>'monster:skeleton'</tt> will return 'EntityType.SKELETON'.<br>
     * <tt>'1983'</tt> will return 'null'.<br>
     * </ol>
     *
     * @param arg the argument to check
     * @return an EntityType or null
     *
     */
    public static EntityType getEntityFrom(String arg) {
        final Pattern matchesEntityPtrn = Pattern.compile("(?:(?:.+?:)|)(.+)", Pattern.CASE_INSENSITIVE);
        Matcher m = matchesEntityPtrn.matcher(arg);
        if (m.matches()) {
            // Match against valid EntityTypes using Bukkit enum
            for (EntityType validEntity : EntityType.values())
                if (m.group(1).equalsIgnoreCase(validEntity.name()))
                    return validEntity;
        }
        // No match
        return null;
    }

    /**
     *
     *
     */
    public static LivingEntity getLivingEntityFrom(String arg) {
        final Pattern matchesEntityPtrn = Pattern.compile("(?:(?:.+?:)|)(.+)", Pattern.CASE_INSENSITIVE);
        Matcher m = matchesEntityPtrn.matcher(arg);
        if (m.matches()) {
            if (m.group(1).toUpperCase().startsWith("ENTITY.")) {
                LivingEntity returnable = ((Denizen) Bukkit.getPluginManager().getPlugin("Denizen"))
                        .getCommandRegistry().get(NewCommand.class).getEntity(m.group(1).split("\\.")[1]);
                if (returnable != null) return returnable;
                else dB.echoError("Invalid entity! '" + m.group(1) + "' could not be found.");
            }

            else if (m.group(1).toUpperCase().startsWith("NPC.")) {
                LivingEntity returnable = CitizensAPI.getNPCRegistry().getById(Integer.valueOf(m.group(1).split("\\.")[1])).getBukkitEntity();
                if (returnable != null) return returnable;
                else dB.echoError("Invalid NPC! '" + m.group(1) + "' could not be found.");
            }

            else if (m.group(1).toUpperCase().startsWith("PLAYER.")) {
                LivingEntity returnable = getPlayerFrom(m.group(1).split("\\.")[1]);
                if (returnable != null) return returnable;
                else dB.echoError("Invalid Player! '" + m.group(1) + "' could not be found.");
            }
        }

        return null;
    }

    /**
     * <p>Returns a primitive float from a dScript argument string. Also
     * accounts for the argument prefix being passed along, for convenience.
     * Never returns null, if not a valid float, 0F will return. If given
     * an integer or double value, a float representation will be returned.</p>
     *
     * @param arg the argument to check
     * @return a float interpretation of the argument
     *
     */
    public static float getFloatFrom(String arg) {
        try {
            if (arg.split(":").length >= 2)
                return Float.valueOf(arg.split(":", 2)[1]);
            else return Float.valueOf(arg);
        } catch (Exception e) {
            return 0f;
        }
    }

    /**
     * <p>Returns a primitive int from a dScript argument string. Also accounts
     * for the argument prefix being passed along, for convenience. Never returns
     * null, if not a valid integer, 0 will return. If given a double value,
     * an integer representation will be returned.</p>
     *
     * <b>Examples:</b>
     * <ol>
     * <tt>'LEVEL:3.5'</tt> will return '3'.<br>
     * <tt>'INT:1'</tt> will return '1'.<br>
     * <tt>'1950'</tt> will return '1950'.<br>
     * <tt>'-.377'</tt> will return '0'.<br>
     * </ol>
     *
     * @param arg the argument to check
     * @return an int interpretation of the argument
     *
     */
    public static int getIntegerFrom(String arg) {
        try {
            if (arg.split(":").length >= 2)
                return Integer.valueOf(arg.split(":", 2)[1]);
            else return Integer.valueOf(arg);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * <p>Returns a Bukkit ItemStack from a dScript argument string. Also accounts
     * for the argument prefix being passed along, for convenience. Though the
     * <tt>matchesItem(...)</tt> requires an <tt>ITEM:</tt> prefix, this method
     * does not, so it could be used in a CustomValueArg.</p>
     *
     * <p>Accounts for several formats, including <tt>ItemId</tt>, <tt>ItemId:Data</tt>,
     * <tt>Material</tt>, <tt>Material:Data</tt>, and finally <tt>ITEMSTACK.item_name</tt>.</p>
     *
     * <p>Provides a line of dB output if returning null.</p>
     *
     * <b>Examples:</b>
     * <ol>
     * <tt>'ITEM:DIAMOND'</tt> will return 'new ItemStack(Material.DIAMOND)'.<br>
     * <tt>'1'</tt> will return 'new ItemStack(1)'.<br>
     * <tt>'1950'</tt> will return 'null'.<br>
     * <tt>'FLOOR:35:15'</tt> will return a new Black Wool ItemStack.<br>
     * <tt>'ITEMSTACK.enchantedItem'</tt> will return the {@link NewCommand}'s
     *     instance of 'enchantedItem', if it exists, otherwise 'null'.<br>
     * </ol>
     *
     * @param arg the argument to check
     * @return an ItemStack or null
     *
     */
    public static Item getItemFrom(String arg) {
        if (arg == null) return null;
        Item stack = Item.valueOf(arg);
        if (stack == null)
            dB.echoError("Invalid item! Failed to find a matching Bukkit ItemStack.");
        return stack;
    }

    /**
     * <p>Returns a String List from a dScript argument string. Also accounts
     * for the argument prefix being passed along, for convenience. Lists in dScript
     * use a pipe ('|') character to divide entries. This method will not trim(), so
     * whitespace is included between pipes. If no pipes are present, it is assumed
     * there is only one item, and the resulting List will have exactly 1 member.
     * If arg is null or contains no characters, and empty list is returned.</p>
     *
     * <b>Examples:</b>
     * <ol>
     * <tt>'ITEMS:DIAMOND|EMERALD'</tt> will return a new List with 2 items,
     *     'DIAMOND' and 'EMERALD'.<br>
     * <tt>'BLOCKS:COAL |STONE'</tt> will return a new List with 2 items,
     *     'COAL ' and 'STONE'. Note the extra space. <br>
     * <tt>'1'</tt> will return a new list with one item, '1'.<br>
     * <tt>''</tt> will return an empty list.<br>
     * </ol>
     *
     * @param arg the dScript argument string
     * @return a List<String> of the string, split by the '|' character
     *
     */
    public static List<String> getListFrom(String arg) {
        if (arg == null || arg.equals("")) return new ArrayList<String>();
        if (arg.split(":").length >= 2)
            return Arrays.asList(arg.split(":", 2)[1].split("\\|"));
        else return Arrays.asList(arg.split("\\|"));
    }

    /**
     * <p>Returns a Bukkit Location from a dScript argument string. Accounts for
     * the argument prefix being passed along, for convenience. Locations in
     * dScript need to be in the format '#,#,#,world_name', whereas the numbers
     * required are double or integer values of x, y and z coordinates, in that
     * order.</p>
     *
     * <p>Remember: Denizen uses 'Replaceable TAGs' (See: TagManager) to fill in
     * various types of 'Locations', such as Anchors, Notables, or even an entity's
     * current position, so no need to handle such things on their own. It is instead
     * encouraged that any type of location uses matchesLocation(...), or at the very
     * least, getLocationFrom(...) when getting a location from a CustomValueArg.</p>
     *
     * <p>Provides a line of dB output if returning null.</p>
     *
     * @param arg the dScript argument string
     * @return a Bukkit Location, or null
     *
     */
    public static Location getLocationFrom(String arg) {
        // Build location argument from dScript argument
        Location location = Location.valueOf(arg);
        // Check if location returned as null
        if (location == null)
            dB.echoError("Unable to build a location with this information! Provided: '" + arg + "'.");
        return location;
    }

    public static Script getScriptFrom(String arg) {
        // Build location argument from dScript argument
        Script script = Script.valueOf(arg);
        // Check if location returned as null
        if (script == null)
            dB.echoError("Unable to build a script with this information! Provided: '" + arg + "'.");
        return script;
    }

    /**
     * <p>Returns a Bukkit Player object from a dScript argument string. Accounts for
     * the argument prefix being passed along, for convenience. For a non-null
     * value to return, the specified player must be currently online. This method
     * does not require case-sensitivity as an additional convenience. For a similar,
     * but less powerful OfflinePlayer object, use {@link #getOfflinePlayerFrom(String)}.</p>
     *
     * <p>Provides a line of dB output if returning null.</p>
     *
     * <b>Examples:</b>
     * <ol>
     * <tt>'TARGET:player_name'</tt> will return Player object, if the player is online.
     * <tt>'aufdemrand'</tt> will return a Player object for 'aufdemrand', if online.<br>
     * <tt>''</tt> will return null.<br>
     * </ol>
     *
     * @param arg the dScript argument string
     * @return a Bukkit Player object, or null
     */
    public static Player getPlayerFrom(String arg) {
        if (arg.split(":").length >= 2)
            arg = arg.split(":", 2)[1];
        // Remove prefix if using PLAYER.playername format
        if (arg.toUpperCase().contains("PLAYER."))
            arg = arg.toUpperCase().replace("PLAYER.", "");
        for (Player player : Bukkit.getOnlinePlayers())
            if (player.getName().equalsIgnoreCase(arg)) return player;
        dB.echoError("Player '" + arg + "' is invalid, or offline.");
        return null;
    }

    /**
     * <p>Returns a NPC object from a dScript argument string. Accounts for
     * the argument prefix being passed along, for convenience.</p>
     *
     * <p>Provides a line of dB output if returning null.</p>
     *
     * <b>Examples:</b>
     * <ol>
     * <tt>'TARGETNPC:32'</tt> will return a NPC.
     * <tt>'83'</tt> will return a NPC.<br>
     * <tt>''</tt> will return null.<br>
     * <tt>'JOSH'</tt> will return null.<br>
     * </ol>
     *
     * @param arg the dScript argument string
     * @return a Citizens NPC object, or null
     */
    public static NPC getNPCFrom(String arg) {
        if (arg.split(":").length >= 2)
            arg = arg.split(":", 2)[1];
        for (NPC npc : CitizensAPI.getNPCRegistry())
            if (npc.getId() == Integer.valueOf(arg).intValue()) return npc;
        dB.echoError("NPC '" + arg + "' is invalid, or has been removed.");
        return null;
    }

    /**
     * <p>Returns a Bukkit OfflinePlayer object from a dScript argument string. Accounts for
     * the argument prefix being passed along, for convenience. For a non-null value to
     * return, the specified player must have logged on to this server at least once. If
     * this returns null, the specified player does not exist. This may also be used for
     * players currently 'online', as Bukkit's Player object extends OfflinePlayer. This
     * method does not require case-sensitivity as an additional convenience.</p>
     *
     * <p>OfflinePlayer objects are less powerful than a Player object, but contain some
     * important methods such as name, health, inventory, etc.</p>
     *
     * <p>Provides a line of dB output if returning null.</p>
     *
     * <b>Examples:</b>
     * <ol>
     * <tt>'TARGET:player_name'</tt> will return an OfflinePlayer object, if the player
     *     exists.
     * <tt>'aufdemrand'</tt> will return an OfflinePlayer object for 'aufdemrand', if
     *     the player exists.<br>
     * <tt>''</tt> will return null.<br>
     * </ol>
     *
     * @param arg the dScript argument string
     * @return a Bukkit OfflinePlayer object, or null
     *
     */
    public static OfflinePlayer getOfflinePlayerFrom(String arg) {
        if (arg.split(":").length >= 2)
            arg = arg.split(":", 2)[1];
        for (OfflinePlayer player : Bukkit.getOfflinePlayers())
            if (player.getName().equalsIgnoreCase(arg)) return player;
        dB.echoError("OfflinePlayer '" + arg + "' is invalid, or has never logged in to this server.");
        return null;
    }

    /**
     * <p>Returns a Queue from a dScript argument string. For convenience, this method
     * can accept the name of the argument. This method is useful for commands which
     * directly affect the script queues.</p>
     *
     * @param arg the dScript argument string
     * @return a QueueType, or null
     *
     */
    public static ScriptQueue getQueueFrom(String arg) {
        if (arg.split(":").length >= 2)
            return ScriptQueue._getQueue(arg.split(":")[1].toUpperCase());
        else return ScriptQueue._getQueue(arg.toUpperCase());
    }

    /**
     * <p>Returns a String value from a dScript argument string. Useful for stripping off
     * an argument prefix from a dScript argument. ie. TEXT:text will return 'text'.</p>
     *
     * <b>Examples:</b>
     * <ol>
     * <tt>'TEXT:hello there'</tt> will return 'hello there'.
     * <tt>'aufdemrand'</tt> will return 'aufdemrand'.<br>
     * </ol>
     *
     * @param arg the dScript argument string
     * @return a String, minus any argument prefix
     *
     */
    public static String getStringFrom(String arg) {
        if (arg.split(":").length >= 2 &&
                ((arg.indexOf(':') < arg.indexOf(' ') || arg.indexOf(' ') == -1)))
            return arg.split(":", 2)[1];
        else return arg;
    }

    /**
     * <p>Gets a Duration object from the dScript duration value format. Accepts a prefix in the
     * argument for convenience.</p>
     *
     * <p>Uses the regex pattern <tt>"(?:.+:|)(\\d+(?:(|\\.\\d+)))(|t|m|s|h|d)"</tt>.
     * Valid units: T=ticks, M=minutes, S=seconds, H=hour, D=day. If not specified,
     * seconds are assumed.</p>
     *
     * <b>Examples:</b>
     * <ol>
     * <tt>'60'</tt> will return '60'.<br>
     * <tt>'-1'</tt> will return '0'.<br>
     * <tt>'DURATION:1.5m'</tt> will return '90'.<br>
     * <tt>'DELAY:derp'</tt> will return null.
     * </ol>
     *
     * @param arg  the dScript argument string
     * @return  a Duration object, or null
     *
     */
    public static Duration getDurationFrom(String arg) {
        // Build location argument from dScript argument
        Duration duration = Duration.valueOf(arg);
        // Check if location returned as null
        if (duration == null)
            dB.echoError("Unable to build a duration with this information! Provided: '" + arg + "'.");
        return duration;
    }
    
    /**
     * <p>Returns an mcMMO SkillType.
     *
     * <p>Accounts for the format <tt>Skill:Name</tt>.</p>
     *
     * <p>Provides a line of dB output if returning null.</p>
     *
     * @param arg the argument to check
     * @return a SkillType or null
     *
     */
    public static SkillType getSkillTypeFrom(String arg) {
        if (arg == null) return null;
        SkillType skill = null;
        try {
            skill = SkillType.valueOf(arg);
        } catch(IllegalArgumentException e) {
            dB.echoError("Invalid skill! Failed to find a matching mcMMO SkillType.");
        }
        return skill;
    }

    /**
     * <p>Used to determine if a dScript argument string is a valid double format. Uses
     * regex to match the string arg provided.</p>
     *
     * <p>This is the same as doing <tt>arg.matches("(?:-|)(?:(?:\\d+)|)(?:(?:\\.\\d+)|)")</tt> except
     * slightly faster since Denizen uses a pre-compiled Pattern for matching.</p>
     *
     * <p>An argument prefix will likely cause this to return false. If wanting a double
     * value in a custom ValueArg format, see {@link #matchesValueArg(String, String, ArgumentType)}.</p>
     *
     * @param arg the dScript argument string
     * @return true if matched, otherwise false
     *
     */
    public static boolean matchesDouble(String arg) {
        Matcher m = doublePtrn.matcher(arg);
        return m.matches();
    }

    /**
     * <p>Used to determine if a dScript argument string is a valid duration argument. Uses
     * regex to match the string arg provided. In order to return true, the 'duration:'
     * prefix must be present along with a positive integer number. Since this argument
     * is used throughout the core members of Denizen, it is encouraged to use it whenever
     * appropriate.</p>
     *
     * TODO: Note compatibility with dScript times (using {@link #getDurationFrom(String)})
     *
     * <p>When extracting the value from a match, using {@link #getDurationFrom(String)} is
     * encouraged.</p>
     *
     * <b>Examples:</b>
     * <ol>
     * <tt>'DURATION:60'</tt> will return true.<br>
     * <tt>'DURATION:-1'</tt> will return false, with a warning.<br>
     * <tt>'DURATION:0'</tt> will return true.<br>
     * <tt>'ENTITY:ZOMBIE'</tt> will return false.
     * </ol>
     *
     * @param arg the dScript argument string
     * @return true if matched, otherwise false
     *
     */
    public static boolean matchesDuration(String arg) {
        final Pattern matchesDurationPtrn = Pattern.compile("duration:\\d+(?:\\.\\d+)?(t|m|s|h|d)?", Pattern.CASE_INSENSITIVE);
        Matcher m = matchesDurationPtrn.matcher(arg);
        if (m.matches()) return true;
        else if (arg.toUpperCase().startsWith("DURATION:"))
            dB.echoError("While parsing '" + arg + "', Denizen has run into a problem. While the " +
                    "prefix is correct, the value is not valid. 'DURATION' requires a positive integer value. " +
                    "Perhaps a replaceable Tag has failed to fill in a valid value?");
        return false;
    }

    /**
     * <p>Used to determine if a dScript argument string is a valid LivingEntity. Will check
     * against Bukkit's EntityType enum as well as {@link NewCommand}'s 'ENTITY' format,
     * <code>'ENTITY.entity_name'</code>.</p>
     *
     * <p>When extracting the value from a match, using {@link #getEntityFrom(String)} is
     * encouraged.</p>
     *
     * <b>Examples:</b>
     * <ol>
     * <tt>'ENTITY:ZOMBIE'</tt> will return true.<br>
     * <tt>'ENTITY:-1'</tt> will return false.<br>
     * <tt>'ENTITY:ENTITY.creeper_boss_17'</tt> will return true.<br>
     * </ol>
     *
     * @param arg the dScript argument string
     * @return true if matched, otherwise false
     *
     */
    public static boolean matchesEntityType(String arg) {
        final Pattern matchesEntityPtrn = Pattern.compile("entity:(.+)", Pattern.CASE_INSENSITIVE);
        Matcher m = matchesEntityPtrn.matcher(arg);
        if (m.matches()) {
            // Check against valid EntityTypes using Bukkit's EntityType enum
            for (EntityType validEntity : EntityType.values())
                if (m.group(1).equalsIgnoreCase(validEntity.name()))
                    return true;
            for (EntityType validEntity : EntityType.values())
                if (m.group(1).equalsIgnoreCase(validEntity.name()))
                    return true;
        }
        // Check for valid prefix, warn about value.
        if (arg.toUpperCase().startsWith("entity:"))
            dB.echoError("While parsing '" + arg + "', Denizen has run into a problem. While the " +
                    "prefix is correct, the value is not valid. Perhaps a replaceable Tag has failed " +
                    "to fill in a valid EntityType, or the EntityType you provided is not correct?");
        return false;
    }

    /**
     * <p>Used to determine if a dScript argument string is a valid integer format. Uses
     * regex to match the string arg provided.</p>
     *
     * <p>This is the same as doing <tt>arg.matches("(?:-|)\\d+")</tt> except
     * slightly faster since Denizen uses a pre-compiled Pattern for matching.</p>
     *
     * <p>An argument prefix will likely cause this to return false. If wanting an integer
     * value in a custom ValueArg format, see {@link #matchesValueArg(String, String, ArgumentType)} and
     * {@link ArgumentType}.</p>
     *
     * @param arg the dScript argument string
     * @return true if matched, otherwise false
     *
     */
    public static boolean matchesInteger(String arg) {
        Matcher m = integerPtrn.matcher(arg);
        return m.matches();
    }

    /**
     * <p>Used to determine if a dScript argument string is a valid Bukkit ItemStack
     * or a currently saved instance from the {@link NewCommand} using 'NEW
     * ITEMSTACK'.</p>
     *
     * <p>Accounts for several formats, including <tt>ItemId</tt>, <tt>ItemId:Data</tt>,
     * <tt>Material</tt>, <tt>Material:Data</tt>, and finally <tt>ITEMSTACK.item_name</tt>.</p>
     *
     * <b>Examples:</b>
     * <ol>
     * <tt>'ITEM:DIAMOND'</tt> will return 'true'.<br>
     * <tt>'ITEM:1'</tt> will return 'true'.<br>
     * <tt>'100:3'</tt> will return 'false'.<br>
     * <tt>'ITEM:ITEMSTACK.enchantedItem'</tt> will return 'true'.<br>
     * </ol>
     *
     * @param arg the dScript argument string
     * @return true if matches, false otherwise
     *
     */
    public static boolean matchesItem(String arg) {
        if (arg.toUpperCase().startsWith("ITEM:")) return true;
        // TODO: Other matches____ do some actual checks. Should this?
        return false;
    }

    public static boolean matchesContext(String arg) {
        if (arg.toUpperCase().startsWith("CONTEXT:")) return true;
        // TODO: Other matches____ do some actual checks. Should this?
        return false;
    }

    public static Map<String, String> getContextFrom(String arg) {
        Map<String, String> context = new HashMap<String, String>();
        List<String> contexts = aH.getListFrom(arg);
        int x = 1;
        for (String ctxt : contexts) {
            context.put(String.valueOf(x), ctxt.trim());
            x++;
        }
        return context;
    }


    /**
     * <p>Used to determine if a dScript argument string is a valid location. Uses regex
     * to match the string arg provided. In order to return true, the 'location:' prefix
     * must be present along with a value that matches the format '#,#,#,world_name',
     * whereas the numbers required are double or integer values of x, y and z
     * coordinates, in that order.</p>
     *
     * <p>When extracting the value from a match, using {@link #getLocationFrom(String)} is
     * encouraged. It is possible that getLocationFrom(...) can produce a 'null' result
     * based on the parameters of this method since there is no check for whether the
     * location actually exists.</p>
     *
     * @param arg the dScript argument string
     * @return true if matched, otherwise false
     *
     */
    public static boolean matchesLocation(String arg) {
        final Pattern locationPattern = Pattern.compile("location:(?:-|)\\d+,(?:-|)\\d+,(?:-|)\\d+,\\w+", Pattern.CASE_INSENSITIVE);
        Matcher m = locationPattern.matcher(arg);
        if (m.matches())
            return true;
        else if (arg.toUpperCase().startsWith("location:"))
            dB.echoError("While parsing '" + arg + "', Denizen has run into a problem. While the " +
                    "prefix is correct, the value is not valid. Perhaps a replaceable Tag has failed " +
                    "to fill in a valid location?");
        return false;
    }

    /**
     * <p>Used to determine if a dScript argument string is a valid quantity argument. Uses
     * regex to match the string arg provided. In order to return true, the 'qty:'
     * prefix must be present along with a valid integer number. Since this argument
     * is used throughout the core members of Denizen, it is encouraged to use it whenever
     * appropriate.</p>
     *
     * <p>When extracting the value from a match, using {@link #getIntegerFrom(String)} is
     * encouraged.</p>
     *
     * <b>Examples:</b>
     * <ol>
     * <tt>'QTY:60'</tt> will return true.<br>
     * <tt>'QTY:-1'</tt> will return true.<br>
     * <tt>'AMT:-1'</tt> will return false.<br>
     * <tt>'QTY:0'</tt> will return true.<br>
     * </ol>
     *
     * @param arg the dScript argument string
     * @return true if matched, otherwise false
     *
     */
    public static boolean matchesQuantity(String arg) {
        final Pattern matchesQuantityPtrn = Pattern.compile("qty:(?:-|)\\d+", Pattern.CASE_INSENSITIVE);
        Matcher m = matchesQuantityPtrn.matcher(arg);
        if (m.matches()) return true;
        else if (arg.toUpperCase().startsWith("qty:"))
            dB.echoError("While parsing '" + arg + "', Denizen has run into a problem. While the " +
                    "prefix is correct, the value is not valid. 'QTY' requires a an integer value. " +
                    "Perhaps a replaceable Tag has failed to fill in a valid value?");
        return false;
    }

    /**
     * <p>Used to determine if a dScript argument string is a valid queuetype argument. Uses
     * regex to match the string arg provided. In order to return true, the 'queue:' or
     * prefix must be present. Since this argument is used throughout the core
     * members of Denizen, it is encouraged to use it whenever appropriate.</p>
     *
     * <p>When extracting the value from a match, using {@link #getQueueFrom(String)} is
     * encouraged.</p>
     *
     * <b>Examples:</b>
     * <ol>
     * <tt>'QUEUE:PLAYER'</tt> will return true.<br>
     * <tt>'QUEUETYPE:NPC'</tt> will return true.<br>
     * <tt>'BLECH:NONE'</tt> will return false.<br>
     * </ol>
     *
     * @param arg the dScript argument string
     * @return true if matched, otherwise false
     *
     */
    public static boolean matchesQueue(String arg) {
        final Pattern matchesQueuePtrn = Pattern.compile("queue:(.+)", Pattern.CASE_INSENSITIVE);
        Matcher m = matchesQueuePtrn.matcher(arg);
        if (m.matches())
            return true;
        return false;
    }

    /**
     * <p>Used to determine if a dScript argument string is a valid script argument. Uses
     * regex to match the string arg provided. In order to return true, the 'script:'
     * prefix must be present along with a valid script currently loaded into Denizen.
     * Since this argument is used throughout the core members of Denizen, it is
     * encouraged to use it whenever appropriate.</p>
     *
     * <b>Examples:</b>
     * <ol>
     * <tt>'SCRIPT:A Fine Quest'</tt> will return true, provided a script with this name
     *     is loaded.<br>
     * <tt>'BOOK:Librarian Guide'</tt> will return false.<br>
     * </ol>
     *
     * @param arg the dScript argument string
     * @return true if matched, otherwise false
     *
     */
    public static boolean matchesScript(String arg) {
        final Pattern matchesScriptPtrn = Pattern.compile("script:(.+)", Pattern.CASE_INSENSITIVE);

        Matcher m = matchesScriptPtrn.matcher(arg);
        if (m.matches()) {
            if (ScriptRegistry.containsScript(m.group(1)))
                return true;
            else {
                dB.echoError("While parsing '" + arg + "', Denizen has run into a problem. This " +
                        "argument's format is correct, but Denizen couldn't locate a script " +
                        "named '" + m.group(1) + "'. Is it spelled correctly?");
            }
        }
        return false;
    }


    /**
     * <p>Used to determine if a dScript argument string is a valid toggle argument. Uses
     * regex to match the string arg provided. In order to return true, the 'toggle:'
     * prefix must be present along with a valid value of either TRUE, FALSE or TOGGLE.
     * Since this argument is used throughout the core members of Denizen, it is
     * encouraged to use it whenever appropriate.</p>
     *
     * <b>Examples:</b>
     * <ol>
     * <tt>'TOGGLE:TRUE'</tt> will return true.<br>
     * <tt>'TOGGLE:TOGGLE'</tt> will return true.<br>
     * <tt>'TOGGLE:DOWN'</tt> will return false.<br>
     * </ol>
     *
     * @param arg the dScript argument string
     * @return true if matched, otherwise false
     *
     */
    public static boolean matchesToggle(String arg) {
        final Pattern matchesTogglePtrn = Pattern.compile("toggle:(?:(?:true)|(?:false)|(?:toggle))", Pattern.CASE_INSENSITIVE);
        Matcher m = matchesTogglePtrn.matcher(arg);
        if (m.matches()) return true;
        else if (arg.toUpperCase().startsWith("toggle:"))
            dB.echoError("While parsing '" + arg + "', Denizen has run into a problem. While the " +
                    "prefix is correct, the value is not valid. 'TOGGLE' requires a value of TRUE, FALSE, or TOGGLE. " +
                    "Perhaps a replaceable Tag has failed to fill in a valid value?");
        return false;
    }

    final private static Pattern regex = Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'");

    /**
     * Builds an arguments array, recognizing items in quotes as a single item
     *
     * @param stringArgs  the line of arguments that need split
     * @return  an array of arguments
     *
     */
    public static String[] buildArgs(String stringArgs) {
        if (stringArgs == null) return null;
        List<String> matchList = new ArrayList<String>();
        Matcher regexMatcher = regex.matcher(stringArgs);
        while (regexMatcher.find()) {
            if (regexMatcher.group(1) != null)
                matchList.add(regexMatcher.group(1));
            else if (regexMatcher.group(2) != null)
                matchList.add(regexMatcher.group(2));
            else
                matchList.add(regexMatcher.group());
        }

        if (dB.showScriptBuilder)
            dB.echoDebug(ChatColor.GRAY + "Args: " + Arrays.toString(matchList.toArray()));

        String[] split = new String[matchList.size()];
        matchList.toArray(split);
        return split;
    }


}
