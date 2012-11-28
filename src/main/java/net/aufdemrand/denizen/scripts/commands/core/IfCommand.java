package net.aufdemrand.denizen.scripts.commands.core;

import java.util.ArrayList;
import java.util.List;

import javax.script.ScriptException;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.npc.DenizenNPC;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;

/**
 * Runs a task script if a flag is met.
 * 
 * @author Jeremy Schroeder
 */

public class IfCommand extends AbstractCommand {

	enum Operator { EQUALS, ISINT, ISDOUBLE, ISPLAYER, ORMORE, ORLESS, CONTAINS }
	enum Bridge { OR, AND, FIRST }
	enum Logic { REGULAR, NEGATIVE }

	// Represents one set of items for comparison in an IF command.
	// ie. IF <FLAG.P:FLAGNAME.ASINT> 20 OR <FLAG.P:OTHERFLAG> 'Twenty' NARRATE '20 or more!' ELSE NARRATE 'Not quite 20!'
	//        |                        | |                            |
	//        +----- comparable[0] ----+ +------- comparable[1] ------+

	private class Comparable {
		Logic logic = Logic.REGULAR;
		Bridge bridge = Bridge.OR;
		Object comparable = null;
		Operator operator = Operator.EQUALS;
		Object comparedto = (boolean) true;
		Boolean outcome = null;
	}

	List<Comparable> comparables = new ArrayList<Comparable>();

	String outcomeCommand = null;
	List<String> outcomeArgs = new ArrayList<String>();

	String elseCommand = null;
	List<String> elseArgs = new ArrayList<String>();

	Player player = null;
	DenizenNPC npc = null;
	String script = null;
	Integer step = 1;
		
	@Override
	public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

		player = scriptEntry.getPlayer();
		npc = scriptEntry.getDenizen();
		script = scriptEntry.getScript();
		step = scriptEntry.getStep();
		
		comparables.add(new Comparable());
		int index = 0;

		for (String arg : scriptEntry.getArguments()) {

			if (outcomeCommand == null) {
				// Set logic (Optional, default is REGULAR)
				if (arg.startsWith("!")) {
					comparables.get(index).logic = Logic.NEGATIVE;
					arg = arg.substring(1);
				}
				// Set bridge
				if (aH.matchesArg("OR", arg) || aH.matchesArg("AND", arg)) {
					index++;
					// new Comparable to add to the list
					comparables.add(new Comparable());
					comparables.get(index).bridge = Bridge.valueOf(arg.toUpperCase());
				}
				// Set operator (Optional, default is EQUALS)
				else if (aH.matchesArg("EQUALS", arg) || aH.matchesArg("ISINT", arg) || aH.matchesArg("ISDOUBLE", arg) || aH.matchesArg("ISPLAYER", arg)
						|| aH.matchesArg("ORMORE", arg) || aH.matchesArg("ORLESS", arg) || aH.matchesArg("CONTAINS", arg)) 
					comparables.get(index).operator = Operator.valueOf(arg.toUpperCase());
				// Set outcomeCommand
				else if (denizen.getCommandRegistry().get(arg) != null)
					outcomeCommand = arg;
				// Set comparable
				else if (comparables.get(index).comparable == null) comparables.get(index).comparable = findObjectType(arg);
				// Set compared-to
				else comparables.get(index).comparedto = findObjectType(arg);

			}  else if (elseCommand == null) {
				// Move to ELSE command
				if (aH.matchesArg("ELSE", arg)) elseCommand = ""; 
				// Add outcomeArgs arguments
				else outcomeArgs.add(arg);

			} else {
				// Specify ELSE command
				if (elseCommand.equals("")) elseCommand = arg;
				// Add elseArgs arguments
				else elseArgs.add(arg);
			}


		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void execute(String commandName) throws CommandExecutionException {

		// IF (!)[COMPARABLE] (OPERATOR) [COMPARED_TO] (BRIDGE) (...add'l Comparable) [COMMAND] (Command Arguments) ELSE [COMMAND] (Command Agruments)

		// Valid OPERATORs: EQUALS*, ISINT, ISDOUBLE, ISPLAYER, ORMORE, ORLESS, CONTAINS   *Note: If not supplied, EQUALS is used by default.
		// Valid BRIDGEs: OR, AND

		// Simple IF THIS equals THAT or THIS equals THAT do COMMAND else COMMAND
		// IF <FLAG.P:FLAGNAME.ASINT> ORMORE 20 OR <FLAG.P:OTHERFLAG> 'Twenty' NARRATE '20 or more!' ELSE NARRATE 'Not quite 20!'

		// More examples:
		// IF <CONS.ACCEPTABLE_ITEMS.ASLIST> CONTAINS <PLAYER.ITEM_IN_HAND.MATERIAL> RUNTASK SCRIPT:'Sell items' ELSE RUNTASK SCRIPT:'Item not available'

		for (Comparable com : comparables) {
			com.outcome = false;

			if (com.comparable instanceof String) {
				switch(com.operator) {
				case EQUALS:
					if (((String) com.comparable).equalsIgnoreCase((String) com.comparedto)) com.outcome = true;
					break;
				case CONTAINS:
					if (((String) com.comparable).toUpperCase().contains(((String) com.comparedto).toUpperCase())) com.outcome = true;
					break;
				case ORMORE:
					if (((String) com.comparable).length() >= ((String) com.comparedto).length()) com.outcome = true;
					break;
				case ORLESS:
					if (((String) com.comparable).length() <= ((String) com.comparedto).length()) com.outcome = true;
					break;
				case ISPLAYER:
					for (Player player : denizen.getServer().getOnlinePlayers())
						if (player.getName().equalsIgnoreCase((String) com.comparable)) {
							com.outcome = true;
							break;
						}
					if (!com.outcome)
						for (OfflinePlayer player : denizen.getServer().getOfflinePlayers())
							if (player.getName().equalsIgnoreCase((String) com.comparable)) {
								com.outcome = true;
								break;
							}
					break;
				}

			}	else if (com.comparable instanceof List) {
				switch(com.operator) {
				case CONTAINS:
					for (String string : ((List<String>) com.comparable)) {
						if (com.comparedto instanceof Integer) {
							if (aH.getIntegerFrom(string) == (Integer) com.comparedto) com.outcome = true;
							break;
						}   else if (com.comparedto instanceof Double) {
							if (aH.getDoubleFrom(string) == (Double) com.comparedto) com.outcome = true;
							break;
						}	else if (com.comparedto instanceof Boolean) {
							if (Boolean.valueOf(string) == (Boolean) com.comparedto) com.outcome = true;
							break;
						}   else if (com.comparedto instanceof String) {
							if (string.equalsIgnoreCase((String) com.comparedto)) com.outcome = true;
							break;
						}
					}
					break;
				case ORMORE:
					if (((List<String>) com.comparable).size() >= ((Integer) com.comparedto)) com.outcome = true;
					break;
				case ORLESS:
					if (((List<String>) com.comparable).size() <= ((Integer) com.comparedto)) com.outcome = true;
					break;
				}

			}   else if (com.comparable instanceof Double) {
				switch(com.operator) {
				case EQUALS:
					if (((Integer) com.comparable).compareTo((Integer) com.comparedto) == 0) com.outcome = true;
					break;
				case ORMORE:
					if (((Integer) com.comparable).compareTo((Integer) com.comparedto) >= 0) com.outcome = true;
					break;
				case ORLESS:
					if (((Integer) com.comparable).compareTo((Integer) com.comparedto) <= 0) com.outcome = true;
					break;
				}

			}	else if (com.comparable instanceof Integer) {
				switch(com.operator) {
				case EQUALS:
					if (((Integer) com.comparable).compareTo((Integer) com.comparedto) == 0) com.outcome = true;
					break;
				case ORMORE:
					if (((Integer) com.comparable).compareTo((Integer) com.comparedto) >= 0) com.outcome = true;
					break;
				case ORLESS:
					if (((Integer) com.comparable).compareTo((Integer) com.comparedto) <= 0) com.outcome = true;
					break;
				}

			}   else if (com.comparable instanceof Boolean) {
				if (((Boolean) com.comparable).compareTo(Boolean.valueOf((String) com.comparedto)) == 0) com.outcome = true;
			}

			if (com.logic == Logic.NEGATIVE) com.outcome = !com.outcome; 
		}

		// Compare outcomes 
		
		int ormet = 0;
		for (Comparable compareable : comparables) {
			if (compareable.bridge == Bridge.OR)
				if (compareable.outcome) ormet++;
		}
		
		int andcount = 0;
		int andmet = 0;
		for (Comparable compareable : comparables) {
			if (compareable.bridge == Bridge.AND)
				if (compareable.outcome) andmet++;
			andcount++;
		}

		// Determine outcome -- do, or else?
		if (ormet > 0 && andcount == andmet) doCommand();
		else doElse();
	}

	private Object findObjectType(String arg) {

		// If a Integer
		if (aH.matchesInteger(arg))
			return aH.getIntegerFrom(arg);

		// If a Double
		else if (aH.matchesDouble(arg))
			return aH.getDoubleFrom(arg);

		// If a Boolean
		else if (arg.equalsIgnoreCase("true")) return true;
		else if (arg.equalsIgnoreCase("false")) return false;

		// If a List<String>
		else if (arg.contains("|")) {
			List<String> toList = new ArrayList<String>();
			for (String string : arg.split("|"))
				toList.add(string);
			return toList;
		}

		// Must be a String
		else return arg;
	}

	private void doCommand() {
		try {
			denizen.getScriptEngine().getScriptExecuter().execute(new ScriptEntry(outcomeCommand.toUpperCase(), (String[]) outcomeArgs.toArray(), player, npc, script, step));
		} catch (ScriptException e) {
			e.printStackTrace();
		}
	}
	
	private void doElse() {
		try {
			denizen.getScriptEngine().getScriptExecuter().execute(new ScriptEntry(elseCommand.toUpperCase(), (String[]) elseArgs.toArray(), player, npc, script, step));
		} catch (ScriptException e) {
			e.printStackTrace();
		}
		
	}

    @Override
    public void onEnable() {
        // TODO Auto-generated method stub
        
    }

}