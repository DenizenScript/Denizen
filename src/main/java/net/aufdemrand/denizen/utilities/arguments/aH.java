package net.aufdemrand.denizen.utilities.arguments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.scripts.ScriptEngine.QueueType;
import net.aufdemrand.denizen.scripts.commands.core.NewCommand;
import net.aufdemrand.denizen.utilities.debugging.dB;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * The dScript Argument Helper will aide you in parsing and formatting arguments from a 
 * dScript argument string (such as those found in a ScriptEntry.getArguments() method).
 * 
 * <br><br>TODO: Update following information
 * 
 * <br><pre>
 * 	 dScript argument         recommended            recommended
 *   string format            JAVA parse method      JAVA get method       returns       
 *  +------------------------+----------------------+---------------------+-------------+
 *   NPCID:#                  parsed in executer     done in executer      DenizenNPC    
 *   PLAYER:player_name       parsed in executer     done in executer      Player        
 *   TOGGLE:true|false        matchesToggle(arg)     getBooleanFrom(arg)   boolean       
 *   DURATION:#               matchesDuration(arg)   getIntegerFrom(arg)   int            
 *   SCRIPT:script_name       matchesScript(arg)     getStringFrom(arg)    String            
 *   LOCATION:#y#,#,world     matchesLocation(arg)   getLocationFrom(arg)  Location      
 *   QUEUE:Queue_Type         matchesQueueType(arg)  getQueueFrom(arg)     QueueType     
 *   QTY:#                    matchesQuantity(arg)   getQuantityFrom(arg)  int            
 *   ITEM:Material_Type(:#)   matchesItem(arg)       getItemFrom(arg)      ItemStack     
 *   ITEM:#(:#)               matchesItem(arg)       getItemFrom(arg)      ItemStack         
 *   # (Integer)              matchesInteger(arg)    getIntegerFrom(arg)   int           
 *   #.# (Double)             matchesDouble(arg)     getDoubleFrom(arg)    double        
 *   #.## (Float)             matchesFloat(arg)      getFloatFrom(arg)     float         
 *   'NPCs rule!' (String)    matchesString(arg)     getStringFrom(arg)    String        
 *   single_word (Word)       matchesWord(arg)       getStringFrom(arg)    String        
 *   string|string2 (List)    matchesString(arg)     getListFrom(arg)      List<String>  
 *</pre><br>
 *
 * In practice, the standard arguments should be used whenever possible to keep things consistent across the entire 
 * Denizen experience. Should you need to use custom arguments, however, there is support for that as well. After all, while using 
 * standard arguments is nice, you should never reach. Arguments should make as much sense to the user as possible.
 * 
 * <br><br><b>
 * Small code examples:
 * </b><ol><code>
 * 0  if (aH.matchesArg("HARD", arg)) <br>
 * 1     hardness = Hardness.HARD; <br>
 *  <br>
 * 0 if (aH.matchesValueArg("HARDNESS", arg, ArgumentType.Word)) <br> 
 * 1     try { hardness = Hardness.valueOf(aH.getStringFrom(arg));} <br>
 * 2     catch (Exception e) { dB.echoError("Invalid HARDNESS!")} <br>
 * </code></ol>
 * 
 * <br><br><b>
 * Methods for dealing with Custom Denizen Arguments
 * </b><ol><code>
 * DenizenScript Argument   JAVA Parse Method                                 JAVA Get Method       Returns    
 * +------------------------+-------------------------------------------------+---------------------+---------------------------------
 * CUSTOM_ARGUMENT          matchesArg("CUSTOM_ARGUMENT", arg)                None. No value        boolean     
 * CSTM_ARG:value           MatchesValueArg("CSTM_ARG", arg, ArgumentType)    get____From(arg)      depends on get method
 * </code></ol><br>
 * 
 * Note: ArgumentType will filter the type of value to match to. If anything should be excepted as the value, or you plan
 * on parsing the value yourself, use ArgumentType.Custom.<br><br>
 * 
 * Valid ArgumentTypes: ArgumentType.String, ArgumentType.Word, ArgumentType.Integer, ArgumentType.Double, ArgumentType.Float,
 * and ArgumentType.Custom
 *
 * @author Jeremy Schroeder
 *
 */
public class aH {

	public enum ArgumentType {
		Entity, Item, Boolean, Custom, Double, Float, Integer, String, Word, Location, Script
	}

	//	Denizen denizen;

	final static Pattern doublePtrn = Pattern.compile("(?:-|)(?:(?:\\d+)|)(?:(?:\\.\\d+)|)");
	final static Pattern floatPtrn = Pattern.compile("^[-+]?[0-9]+[.]?[0-9]*([eE][-+]?[0-9]+)?$");
	final static Pattern integerPtrn = Pattern.compile("(?:-|)\\d+");
	final static Pattern stringPtrn = Pattern.compile(".+");
	final static Pattern wordPtrn = Pattern.compile("\\w+");

	/**
	 * Returns a boolean value from a dScript argument string. Also accounts
	 * for the argument prefix being passed along, for convenience.<br><br>
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
			return Boolean.valueOf(arg.split(":", 2)[1]).booleanValue();
		else return Boolean.valueOf(arg).booleanValue();
	}

	/**
	 * Returns a primitive double from a dScript argument string. Also accounts
	 * for the argument prefix being passed along, for convenience. Never returns
	 * null, if not a valid double, 0D will return. If given an integer value, 
	 * a double representation will be returned.<br><br>
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
				return Double.valueOf(arg.split(":", 2)[1]).doubleValue();
			else return Double.valueOf(arg).doubleValue();
		} catch (Exception e) { 
			return 0.00; 
		}
	}

	/**
	 * Returns a Bukkit LivingEntity from a dScript argument string. Also accounts
	 * for the argument prefix being passed along, for convenience. Though the
	 * <tt>matchesItem(...)</tt> requires an <tt>ITEM:</tt> prefix, this method
	 * does not, so it could be used in a CustomValueArg.<br><br>
	 * 
	 * Accounts for several formats, including <tt>ItemId</tt>, <tt>ItemId:Data</tt>,
	 * <tt>Material</tt>, <tt>Material:Data</tt>, and finally <tt>ITEMSTACK.item_name</tt>.<br><br>
	 * 
	 * Provides a line of dB output if returning null. Also note that Bukkit requires
	 * a LivingEntity to be spawned, so a location is also necessary unless using
	 * a stored 'ENTITY.entity_name'<br><br>
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
	 * @param spawnLocation the location to spawn the entity, not used (and may be null)
	 * 		if using a 'ENTITY.entity_name' format
	 * @return an ItemStack or null
	 * 
	 */
	public static LivingEntity getEntityFrom(String arg, Location spawnLocation) {
		final Pattern matchesEntityPtrn = Pattern.compile("(?:(?:.+?:)|)(.+)", Pattern.CASE_INSENSITIVE);
		Matcher m = matchesEntityPtrn.matcher(arg);
		if (m.matches()) {
			// Check for NEW ENTITY command format (ENTITY.entity_name)
			if (m.group(1).toUpperCase().startsWith("ENTITY.")) {
				LivingEntity returnable = ((Denizen) Bukkit.getPluginManager().getPlugin("Denizen"))
						.getCommandRegistry().get(NewCommand.class).getEntity(m.group(1));
				if (returnable != null) return returnable;
				else dB.echoError("Invalid entity! '" + m.group(1) + "' could not be found.");
			} else {
				// Location required past this point
				if (spawnLocation == null) return null;
				// Check against valid EntityTypes using Bukkit's EntityType enum
				for (EntityType validEntity : EntityType.values())
					if (m.group(1).equalsIgnoreCase(validEntity.getName()))
						spawnLocation.getWorld().spawnEntity(spawnLocation, validEntity);
			}
		}
		// No match
		return null;
	}

	/**
	 * Returns a primitive float from a dScript argument string. Also
	 * accounts for the argument prefix being passed along, for convenience. 
	 * Never returns null, if not a valid float, 0F will return. If given
	 * an integer or double value, a float representation will be returned.<br><br>
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
	 * Returns a primitive int from a dScript argument string. Also accounts
	 * for the argument prefix being passed along, for convenience. Never returns
	 * null, if not a valid integer, 0 will return. If given a double value, 
	 * an integer representation will be returned.<br><br>
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
	 * Returns a Bukkit ItemStack from a dScript argument string. Also accounts
	 * for the argument prefix being passed along, for convenience. Though the
	 * <tt>matchesItem(...)</tt> requires an <tt>ITEM:</tt> prefix, this method
	 * does not, so it could be used in a CustomValueArg.<br><br>
	 * 
	 * Accounts for several formats, including <tt>ItemId</tt>, <tt>ItemId:Data</tt>,
	 * <tt>Material</tt>, <tt>Material:Data</tt>, and finally <tt>ITEMSTACK.item_name</tt>.<br><br>
	 * 
	 * Provides a line of dB output if returning null.<br><br>
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
	public static ItemStack getItemFrom(String arg) {

		final Pattern[] getItemPtrn = {
				Pattern.compile("(?:(?:.+?:)|)(\\d+):(\\d+)"),
				Pattern.compile("(?:(?:.+?:)|)(\\d+)"),
				Pattern.compile("(?:(?:.+?:)|)([a-zA-Z\\x5F]+?):(\\d+)"),
				Pattern.compile("(?:(?:.+?:)|)([a-zA-Z\\x5F]+)"),
				Pattern.compile("(?:(?:.+?:)|)itemstack\\.(.+)", Pattern.CASE_INSENSITIVE)
		};

		Matcher[] m = new Matcher[4];

		// First check for a 'saved' item made with the NEW command.
		// These are in the 'ITEMSTACK.item_name' format.
		m[0] = getItemPtrn[4].matcher(arg);
		if (m[0].matches()) {
			ItemStack returnable = ((Denizen) Bukkit.getPluginManager().getPlugin("Denizen"))
					.getCommandRegistry().get(NewCommand.class).getItem(m[0].group(1));
			if (returnable != null) return returnable;
			else dB.echoError("Invalid item! '" + m[0].group(1) + "' could not be found.");
		}

		// Now check traditional item patterns.
		m[0] = getItemPtrn[0].matcher(arg);
		m[1] = getItemPtrn[1].matcher(arg);
		m[2] = getItemPtrn[2].matcher(arg);
		m[3] = getItemPtrn[3].matcher(arg);
		ItemStack stack = null;

		try {
			// Match 'ItemId:Data'
			if (m[0].matches()) {
				stack = new ItemStack(Integer.valueOf(m[0].group(1)));
				stack.setDurability(Short.valueOf(m[0].group(2)));
				return stack;

				// Match 'ItemId'
			} else if (m[1].matches()) {
				return new ItemStack(Integer.valueOf(m[1].group(1)));

				// Match 'Material:Data'
			} else if (m[2].matches()) {
				stack = new ItemStack(Material.valueOf(m[2].group(1).toUpperCase()));
				stack.setDurability(Short.valueOf(m[2].group(2)));
				return stack;

				// Match 'Material'
			} else if (m[3].matches()) {
				return new ItemStack(Material.valueOf(m[3].group(1).toUpperCase()));
			}

		} catch (Exception e) { 
			dB.echoError("Invalid item! Failed to find a matching Bukkit ItemStack."); 
			if (dB.showStackTraces) e.printStackTrace();
		}

		return stack;
	}

	/**
	 * Returns a List<String> from a dScript argument string. Also accounts
	 * for the argument prefix being passed along, for convenience. Lists in dScript
	 * use a pipe ('|') character to divide entries. This method will not trim(), so 
	 * whitespace is included between pipes. If no pipes are present, it is assumed 
	 * there is only one item, and the resulting List will have exactly 1 member.
	 * If arg is null or contains no characters, and empty list is returned.<br><br>
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
	 * Returns a Bukkit Location from a dScript argument string. Accounts for
	 * the argument prefix being passed along, for convenience. Locations in
	 * dScript need to be in the format '#,#,#,world_name', whereas the numbers
	 * required are double or integer values of x, y and z coordinates, in that
	 * order.<br><br>
	 * 
	 * Remember: Denizen uses 'Replaceable TAGs' (See: {@link TagManager}) to fill in
	 * various types of 'Locations', such as Anchors, Notables, or even an entity's
	 * current position, so no need to handle such things on their own. It is instead
	 * encouraged that any type of location uses matchesLocation(...), or at the very
	 * least, getLocationFrom(...) when getting a location from a CustomValueArg.<br><br>
	 * 
	 * Provides a line of dB output if returning null.<br><br>
	 * 
	 * @param arg the dScript argument string
	 * @return a Bukkit Location, or null
	 * 
	 */
	public static Location getLocationFrom(String arg) {
		arg = arg.split(":", 2)[1];
		String[] num = arg.split(",");
		Location location = null;
		try {
			location = new Location(Bukkit.getWorld(num[3]), Double.valueOf(num[0]), Double.valueOf(num[1]), Double.valueOf(num[2]));
		} catch (Exception e) { 
			dB.echoError("Unable to build a location with this information! Provided: '" + arg + "'."); 
			return null; 
		}
		return location;
	}

	/**
	 * Returns a Bukkit Player object from a dScript argument string. Accounts for
	 * the argument prefix being passed along, for convenience. For a non-null
	 * value to return, the specified player must be currently online. This method
	 * does not require case-sensitivity as an additional convenience. For a similar,
	 * but less powerful OfflinePlayer object, use getOfflinePlayerFrom(...).<br><br>
	 *
	 * Provides a line of dB output if returning null.<br><br>
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
		for (Player player : Bukkit.getOnlinePlayers())
			if (player.getName().equalsIgnoreCase(arg)) return player;
		dB.echoError("Player '" + arg + "' is invalid, or offline.");
		return null;
	}

	/**
	 * Returns a Bukkit OfflinePlayer object from a dScript argument string. Accounts for
	 * the argument prefix being passed along, for convenience. For a non-null value to 
	 * return, the specified player must have logged on to this server at least once. If
	 * this returns null, the specified player does not exist. This may also be used for
	 * players currently 'online', as Bukkit's Player object extends OfflinePlayer. This
	 * method does not require case-sensitivity as an additional convenience.<br><br>
	 * 
	 * OfflinePlayer objects are less powerful than a Player object, but contain some
	 * important methods such as name, health, inventory, etc.<br><br>
	 * 
	 * Provides a line of dB output if returning null.<br><br>
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
	public static Player getOfflinePlayerFrom(String arg) {
		if (arg.split(":").length >= 2)
			arg = arg.split(":", 2)[1];
		for (Player player : Bukkit.getOnlinePlayers())
			if (player.getName().equalsIgnoreCase(arg)) return player;
		dB.echoError("OfflinePlayer '" + arg + "' is invalid, or has never logged in to this server.");
		return null;
	}

	/**
	 * Returns a QueueType from a dScript argument string. For convenience, this method
	 * can accept the name of the argument. This method is useful for commands which
	 * directly affect the script queues.<br><br>
	 * 
	 * Valid {@link QueueType}s are 'NPC', 'PLAYER' and 'PLAYER_TASK'. Returns null if
	 * the provided argument doesn't match any of these.<br><br>
	 * 
	 * Provides a line of dB output if returning null.<br><br>
	 * 
	 * <b>Examples:</b>
	 * <ol>
	 * <tt>'QUEUE:NPC'</tt> will return 'QueueType.NPC'.
	 * <tt>'PLAYER_TASK'</tt> will return 'QueueType.PLAYER_TASK'.
	 * <tt>'aufdemrand'</tt> will return 'null'.<br>
	 * </ol>
	 * 
	 * @param arg the dScript argument string
	 * @return a QueueType, or null
	 * 
	 */
	public static QueueType getQueueFrom(String arg) {
		try { 
			if (arg.split(":").length >= 2)
				return QueueType.valueOf(arg.split(":")[1].toUpperCase());
			else return QueueType.valueOf(arg.toUpperCase());
		} catch (Exception e) { 
			dB.echoError("Invalid Queuetype!"); 
		}
		return null;
	}

	/**
	 * Returns a String value from a dScript argument string. Useful for stripping off
	 * an argument prefix from a dScript argument. ie. TEXT:text will return 'text'.<br><br>
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
	 * Used to determine if a argument string matches a non-valued custom argument.
	 * If a dScript valued argument (such as PLAYER:NAME) is passed, this method
	 * will always return false. Also supports multiple argument names, separated by a
	 * comma (,) character. This method will trim() each name specified.<br><br>
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
	 * Used to determine if a dScript argument string is a valid double format. Uses
	 * regex to match the string arg provided.<br><br>
	 * 
	 * This is the same as doing <tt>arg.matches("(?:-|)(?:(?:\\d+)|)(?:(?:\\.\\d+)|)")</tt> except
	 * slightly faster since Denizen uses a pre-compiled Pattern for matching.<br><br>
	 * 
	 * An argument prefix will likely cause this to return false. If wanting a double
	 * value in a custom ValueArg format, see {@link #matchesValueArg(String)} and 
	 * {@link ArgumentType}.
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
	 * Used to determine if a dScript argument string is a valid duration argument. Uses
	 * regex to match the string arg provided. In order to return true, the 'duration:' 
	 * prefix must be present along with a positive integer number. Since this argument
	 * is used throughout the core members of Denizen, it is encouraged to use it whenever
	 * appropriate.<br><br>
	 * 
	 * When extracting the value from a match, using {@link #getIntegerFrom(String)} is
	 * encouraged.<br><br>
	 *
	 * <b>Examples:</b>
	 * <ol>
	 * <tt>'DURATION:60'</tt> will return true.<br>
	 * <tt>'DURATION:-1'</tt> will return false.<br>
	 * <tt>'DURATION:0'</tt> will return true.<br>
	 * </ol>
	 *
	 * @param arg the dScript argument string
	 * @return true if matched, otherwise false
	 * 
	 */
	public static boolean matchesDuration(String arg) {
		final Pattern matchesDurationPtrn = Pattern.compile("duration:\\d+", Pattern.CASE_INSENSITIVE);
		Matcher m = matchesDurationPtrn.matcher(arg);
		return m.matches();
	}

	/**
	 * Used to determine if a dScript argument string is a valid LivingEntity. Will check
	 * against Bukkit's EntityType enum as well as {@link NewCommand}'s 'ENTITY' format, 
	 * <code>'ENTITY.entity_name'</code>.<br><br>
	 * 
	 * When extracting the value from a match, using {@link #getEntityFrom(String)} is
	 * encouraged.<br><br>
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
	public static boolean matchesEntity(String arg) {
		final Pattern matchesEntityPtrn = Pattern.compile("entity:(.+)", Pattern.CASE_INSENSITIVE);
		Matcher m = matchesEntityPtrn.matcher(arg);
		if (m.matches()) {
			// Check for NEW ENTITY command format (ENTITY.entity_name)
			if (m.group(1).toUpperCase().startsWith("ENTITY."))
				return true;
			else {
				// Check against valid EntityTypes using Bukkit's EntityType enum
				for (EntityType validEntity : EntityType.values())
					if (m.group(1).equalsIgnoreCase(validEntity.getName()))
						return true;
			}
		}
		// No match
		return false;
	}

	/**
	 * Used to determine if a dScript argument string is a valid integer format. Uses
	 * regex to match the string arg provided.<br><br>
	 * 
	 * This is the same as doing <tt>arg.matches("(?:-|)\\d+")</tt> except
	 * slightly faster since Denizen uses a pre-compiled Pattern for matching.<br><br>
	 * 
	 * An argument prefix will likely cause this to return false. If wanting an integer
	 * value in a custom ValueArg format, see {@link #matchesValueArg(String)} and 
	 * {@link ArgumentType}.
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
	 * Used to determine if a dScript argument string is a valid Bukkit ItemStack
	 * or a currently saved instance from the {@link NewCommand} using 'NEW
	 * ITEMSTACK'.<br><br>
	 * 
	 * Accounts for several formats, including <tt>ItemId</tt>, <tt>ItemId:Data</tt>,
	 * <tt>Material</tt>, <tt>Material:Data</tt>, and finally <tt>ITEMSTACK.item_name</tt>.<br><br>
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

		final Pattern[] matchesItemPtrn = {
				Pattern.compile("item:\\d+:\\d+", Pattern.CASE_INSENSITIVE),
				Pattern.compile("item:\\d+", Pattern.CASE_INSENSITIVE),
				Pattern.compile("item:([a-zA-Z\\x5F]+)", Pattern.CASE_INSENSITIVE),
				Pattern.compile("item:itemstack\\..+", Pattern.CASE_INSENSITIVE)
		};

		Matcher m;
		m = matchesItemPtrn[3].matcher(arg);
		if (m.matches()) return true;

		m = matchesItemPtrn[0].matcher(arg);
		if (m.matches()) return true;

		m = matchesItemPtrn[1].matcher(arg);
		if (m.matches()) return true;

		m = matchesItemPtrn[2].matcher(arg);
		if (m.matches()) {
			for (Material mat : Material.values())
				if (mat.name().equalsIgnoreCase(m.group(1)))
					return true;
		}

		// No match!
		return false;
	}

	/**
	 * Used to determine if a dScript argument string is a valid location. Uses regex
	 * to match the string arg provided. In order to return true, the 'location:' prefix
	 * must be present along with a value that matches the format '#,#,#,world_name', 
	 * whereas the numbers required are double or integer values of x, y and z 
	 * coordinates, in that order.<br><br>
	 * 
	 * When extracting the value from a match, using {@link #getLocationFrom(String)} is
	 * encouraged. It is possible that getLocationFrom(...) can produce a 'null' result
	 * based on the parameters of this method since there is no check for whether the
	 * location actually exists.<br><br>
	 * 
	 * @param arg the dScript argument string
	 * @return true if matched, otherwise false
	 * 
	 */
	public static boolean matchesLocation(String arg) {
		final Pattern locationPattern = Pattern.compile("location:(?:-|)\\d+,(?:-|)\\d+,(?:-|)\\d+,\\w+", Pattern.CASE_INSENSITIVE);
		Matcher m = locationPattern.matcher(arg);
		return m.matches();
	}

	/**
	 * Used to determine if a dScript argument string is a valid quantity argument. Uses
	 * regex to match the string arg provided. In order to return true, the 'qty:' 
	 * prefix must be present along with a valid integer number. Since this argument
	 * is used throughout the core members of Denizen, it is encouraged to use it whenever
	 * appropriate.<br><br>
	 * 
	 * When extracting the value from a match, using {@link #getIntegerFrom(String)} is
	 * encouraged.<br><br>
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
		return m.matches();
	}

	/**
	 * Used to determine if a dScript argument string is a valid queuetype argument. Uses
	 * regex to match the string arg provided. In order to return true, the 'queue:' or 
	 * 'queuetype:' prefix must be present along with a valid {@link QueueType}. Since this 
	 * argument is used throughout the core members of Denizen, it is encouraged to use it
	 * whenever appropriate.<br><br>
	 * 
	 * When extracting the value from a match, using {@link #getQueueFrom(String)} is
	 * encouraged. Valid QueueTypes are PLAYER, NPC, and PLAYER_TASK.<br><br>
	 *
	 * <b>Examples:</b>
	 * <ol>
	 * <tt>'QUEUE:PLAYER'</tt> will return true.<br>
	 * <tt>'QUEUETYPE:NPC'</tt> will return true.<br>
	 * <tt>'QUEUE:NONE'</tt> will return false.<br>
	 * </ol>
	 *
	 * @param arg the dScript argument string
	 * @return true if matched, otherwise false
	 * 
	 */
	public static boolean matchesQueueType(String arg) {
		final Pattern matchesQueuePtrn = Pattern.compile("(?:(?:queue)|(?:queuetype)):(?:(?:player)|(?:player_task)|(?:npc))", Pattern.CASE_INSENSITIVE);
		Matcher m = matchesQueuePtrn.matcher(arg);
		return m.matches();
	}

	/**
	 * Used to determine if a dScript argument string is a valid script argument. Uses
	 * regex to match the string arg provided. In order to return true, the 'script:' 
	 * prefix must be present along with a valid script currently loaded into Denizen.
	 * Since this argument is used throughout the core members of Denizen, it is 
	 * encouraged to use it whenever appropriate.<br><br>
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
		// Check if script exists by looking for  Script Name:
		//                                          Type: ...
		if (m.matches() && ((Denizen) Bukkit.getPluginManager().getPlugin("Denizen"))
				.getScripts().contains(arg.split(":")[1].toUpperCase() + ".TYPE"))
			return true;
		return false;
	}

	/**
	 * Used to determine if a dScript argument string is a valid toggle argument. Uses
	 * regex to match the string arg provided. In order to return true, the 'toggle:' 
	 * prefix must be present along with a valid value of either TRUE, FALSE or TOGGLE.
	 * Since this argument is used throughout the core members of Denizen, it is 
	 * encouraged to use it whenever appropriate.<br><br>
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
		return m.matches();
	}


	/**
	 * Used to match a custom argument with a value. In practice, the standard 
	 * arguments should be used whenever possible to keep things consistent across
	 * the entire Denizen experience. Should you need to use custom arguments, 
	 * however, this method provides support. After all, while using standard 
	 * arguments is nice, you should never reach. Arguments should make as much
	 * sense to the user/script writer as possible.<br><br>
	 * 
	 * <b>Small code example:</b>
	 * <ol>
	 * <tt>0 if (aH.matchesValueArg("HARDNESS", arg, ArgumentType.Word))</tt><br> 
	 * <tt>1     try { </tt><br>
	 * <tt>2        hardness = Hardness.valueOf(aH.getStringFrom(arg));</tt><br>
	 * <tt>3     } catch (Exception e) { </tt><br>
	 * <tt>4		dB.echoError("Invalid HARDNESS!") </tt><br>
	 * <tt>5 }</tt><br>
	 * </ol>
	 * 
	 * <br><br>Note: Like matchesArg(...), matchesValueArg(...) supports multiple
	 * argument names, separated by a comma (,) character. This method will trim()
	 * each name specified.<br><br>
	 * 
	 * Also requires a specified ArgumentType, which will filter the type of value
	 * to match to. If anything should be excepted as the value, or you plan
	 * on parsing the value yourself, use ArgumentType.Custom, otherwise use an
	 * an appropriate ArgumentType. See: {@link ArgumentType}.
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
			if (matched == false) return false;
		}

		arg = arg.split(":", 2)[1];
		Matcher m;

		switch (type) {
		case Word:
			m = wordPtrn.matcher(arg);
			return m.matches();

		case Integer:
			m = integerPtrn.matcher(arg);
			return m.matches();

		case Double:
			m = doublePtrn.matcher(arg);
			return m.matches();

		case Float:
			m = floatPtrn.matcher(arg);
			return m.matches();

		case Boolean:
			if (arg.equalsIgnoreCase("true")) return true;
			else return false;

		case Location:
			if (matchesLocation("location:" + arg)) return true;
			else return false;

		case Script:
			if (matchesLocation("script:" + arg)) return true;
			else return false;

		case Item:
			if (matchesItem("item:" + arg)) return true;
			else return false;

		case Entity:
			if (matchesEntity("entity:" + arg)) return true;
			else return false;

		default:
			return true;
		}
	}

}
