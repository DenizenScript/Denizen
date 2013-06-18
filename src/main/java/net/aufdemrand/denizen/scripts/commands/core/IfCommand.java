package net.aufdemrand.denizen.scripts.commands.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.exceptions.ScriptEntryCreationException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.citizensnpcs.api.CitizensAPI;

import org.bukkit.ChatColor;

/**
 * Core dScript IF command.
 *
 * @author Jeremy Schroeder, David Cernat
 */

public class IfCommand extends AbstractCommand {

    enum Operator { EQUALS, MATCHES, ORMORE, ORLESS, MORE, LESS, CONTAINS, ISEMPTY }
    enum Bridge { OR, AND, FIRST }
    enum Logic { REGULAR, NEGATIVE }

    private class Comparable {
        Logic logic = Logic.REGULAR;
        Bridge bridge = Bridge.OR;
        Object comparable = null;
        Operator operator = Operator.EQUALS;
        Object comparedto = (boolean) true;
        Boolean outcome = null;

        @Override
        public String toString() {
            return  (logic != Logic.REGULAR ? "Logic='" + logic.toString() + "', " : "")
                    + "Comparable='" + (comparable == null ? "null'" : comparable.getClass().getSimpleName()
                    + "(" + ChatColor.AQUA + comparable + ChatColor.WHITE + ")'")
                    + ", Operator='" + operator.toString()
                    + "', ComparedTo='" + (comparedto == null ? "null'" : comparedto.getClass().getSimpleName()
                    + "(" + ChatColor.AQUA + comparedto + ChatColor.WHITE + ")' ")
                    + ChatColor.YELLOW + "--> OUTCOME='" + outcome + "'";
        }
    }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Initialize necessary fields
        List<Comparable> comparables = new ArrayList<Comparable>();

        TreeMap<Integer, ArrayList<String>> outcomeCommands = new TreeMap<Integer, ArrayList<String>>();
        TreeMap<Integer, ArrayList<String>> elseCommands = new TreeMap<Integer, ArrayList<String>>();

        // Keep track of this to avoid Denizen overlooking comparedTo when an operator is used
        // with a value that matches the name of a command. (Good find dimensionZ!)
        boolean usedOperator = false;
        
        // Track whether we are inside the Else argument or not
        boolean insideElse = false;
        
        // Track whether we are adding a new command or not
        boolean newCommand = false;
        
        // Track whether we are inside recursive brackets whose contents
        // should be added as arguments to our current If, not as commands
        int bracketsEntered = 0;

        comparables.add(new Comparable());
        int index = 0;

        // Iterate through the arguments, build comparables
        for (String arg : scriptEntry.getArguments()) {
        	
            // Trim unwanted spaces
            arg = arg.trim();


            // Read "-" as meaning we are moving to a new command, unless we
            // are inside nested brackets (i.e. bracketsEntered of at least 2)
            // in which case we just treat what follows as arguments
            if (aH.matchesArg("-", arg) && bracketsEntered < 2) {
            	
            	newCommand = true;
            }
            
            else if (outcomeCommands.isEmpty()) {

                // Set logic (Optional, default is REGULAR)
            	if (arg.startsWith("!")) {
                    comparables.get(index).logic = Logic.NEGATIVE;
                    if ( arg.length() == 1)
                        continue;
                    if (arg.equals("!="))
                        arg = "==";
                    else
                        arg = arg.substring(1);
                }

                // Replace symbol-operators/bridges with ENUM value for matching
                arg = arg.replace("==", "EQUALS").replace(">=", "ORMORE").replace("<=", "ORLESS")
                        .replace("<", "LESS").replace(">", "MORE")
                        .replace("||", "OR").replace("&&", "AND");


                // Set bridge
                if (aH.matchesArg("OR", arg) || aH.matchesArg("AND", arg)) {
                    index++;
                    // new Comparable to add to the list
                    comparables.add(new Comparable());
                    comparables.get(index).bridge = Bridge.valueOf(arg.toUpperCase());
                }

                // Set operator (Optional, default is EQUALS)
                else if (aH.matchesArg("EQUALS", arg) || aH.matchesArg("MATCHES", arg) || aH.matchesArg("ISEMPTY", arg)
                        || aH.matchesArg("ORMORE", arg) || aH.matchesArg("MORE", arg) || aH.matchesArg("LESS", arg)
                        || aH.matchesArg("ORLESS", arg) || aH.matchesArg("CONTAINS", arg)) {
                    comparables.get(index).operator = Operator.valueOf(arg.toUpperCase());
                    usedOperator = true;
                }

                // Set comparable
                else if (comparables.get(index).comparable == null) {
                    // If using MATCHES operator, keep as string.
                    comparables.get(index).comparable = findObjectType(arg);
                }
                
                // If we find a bracket, add it to the number of brackets we
                // are inside (which should probably always be 1 at this point)
                else if (aH.matchesArg("{", arg)) {
                	
                	bracketsEntered++;
                }
                
                // Add outcomeCommand first since compared-to is technically optional.
                // If using an operator though, skip on to compared-to!
                else if (!usedOperator && denizen.getCommandRegistry().get(arg.replace("^", "")) != null) {

                	// Only add a new command here if the list of commands is empty,
                	// so as to not add two commands when people use an optional -
                	// in front of their first (and sometimes only) command
                	if (outcomeCommands.size() == 0) {

                    	outcomeCommands.put(outcomeCommands.size(), new ArrayList<String>());
                    	outcomeCommands.get(outcomeCommands.lastKey()).add(arg.toUpperCase());
                    	newCommand = false;
                	}
                	
                }
                // Set compared-to
                else {
                	
                    comparables.get(index).comparedto = matchObjectType(comparables.get(index), arg);
                    usedOperator = false;
                }
            }
            // Process outcome commands
            else if (insideElse == false) {
            	// Move to else commands if we read an "else" and we're not
            	// currently going through nested arguments
                if (aH.matchesArg("else", arg) && bracketsEntered == 0) {

                	insideElse = true;
                }

            	// If we find a bracket, and we're already inside
            	// nested brackets, add the bracket to the current
            	// command's arguments
                else if (aH.matchesArg("{", arg)) {
                	
                	bracketsEntered++;
                	
                	if (bracketsEntered > 1) {
                		
                		outcomeCommands.get(outcomeCommands.lastKey()).add(arg);
                	}
                }
                
                else if (aH.matchesArg("}", arg)) {
                	
                	bracketsEntered--;
                	
                	if (bracketsEntered > 0) {
                		
                		outcomeCommands.get(outcomeCommands.lastKey()).add(arg);
                	}
                }
                // Add new outcome command if the last argument was a non-nested "-"
                else if (newCommand == true) {

                	newCommand = false;
                	outcomeCommands.put(outcomeCommands.size(), new ArrayList<String>());
                	outcomeCommands.get(outcomeCommands.lastKey()).add(arg.toUpperCase());
                }
                // Add new outcome argument
                else {

                	outcomeCommands.get(outcomeCommands.lastKey()).add(arg);
                }
            }
            // Process else commands
            else {
            	// If we find a bracket, and we're already inside
            	// nested brackets, add the bracket to the current
            	// command's arguments
            	if (aH.matchesArg("{", arg)) {
                	
                	bracketsEntered++;
                	
                	if (bracketsEntered > 1) {
                	
                		elseCommands.get(elseCommands.lastKey()).add(arg);
                	}
                }
                
                else if (aH.matchesArg("}", arg)) {
                	
                	bracketsEntered--;
                	
                	if (bracketsEntered > 0) {
         
                		elseCommands.get(elseCommands.lastKey()).add(arg);
                	}
                }
                // Add new else command if the last argument was a non-nested "-"
            	// or if it was "else" and we have no else commands yet
            	else if (newCommand == true || elseCommands.size() == 0) {

                	newCommand = false;
                	elseCommands.put(elseCommands.size(), new ArrayList<String>());
                	elseCommands.get(elseCommands.lastKey()).add(arg.toUpperCase());
                	
                	// If we find an "if", act like we entered a set of
                	// brackets, so we treat the if's commands as arguments
                	// and don't add them to our current else commands
                	if (aH.matchesArg("if", arg)) {
                		
                		bracketsEntered++;
                	}
                }
                // Add new else argument
                else {
                	
                	elseCommands.get(elseCommands.lastKey()).add(arg);
                }
            }
        }

        // Stash objects required to execute() into the ScriptEntry
        scriptEntry.addObject("comparables", comparables);
        scriptEntry.addObject("outcomeCommands", outcomeCommands);
        scriptEntry.addObject("elseCommands", elseCommands);
    }

    @SuppressWarnings({ "unchecked", "incomplete-switch" })
    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        // Grab comparables from the ScriptEntry
        List<Comparable> comparables = (List<Comparable>) scriptEntry.getObject("comparables");

        int counter = 1;

        // Evaluate comparables
        for (Comparable com : comparables) {
            com.outcome = false;

            //
            // Comparable is a STRING
            //
            if (com.comparable instanceof String) {

                String comparable = (String) com.comparable;

                if (!(com.comparedto instanceof String) && com.operator != Operator.ISEMPTY) {


                } else {
                    switch(com.operator) {
                        case ISEMPTY:
                            // For checking if a FLAG is empty.
                            if (comparable.equalsIgnoreCase("EMPTY")) com.outcome = true;
                            break;
                        case EQUALS:
                            // For checking straight up if comparable is equal to (ignoring case) comparedto
                            if (comparable.equalsIgnoreCase((String) com.comparedto)) com.outcome = true;
                            break;
                        case CONTAINS:
                            // For checking if the comparable contains comparedto
                            if (comparable.toUpperCase().contains(((String) com.comparedto).toUpperCase())) com.outcome = true;
                            break;
                        case MATCHES:
                            // This is a fun one! Can check if the STRING compareable matches a specific argument type, as specified by comparedto
                            if (((String) com.comparedto).equalsIgnoreCase("location")) {
                                if (aH.matchesLocation("location:" + comparable)) com.outcome = true;
                            }
                            else if (((String) com.comparedto).equalsIgnoreCase("pose")) {
                                com.outcome = true; // TODO: if (aH.matchesPose(comparable)) com.outcome = true;
                            }
                            else if (((String) com.comparedto).equalsIgnoreCase("double")) {
                                if (aH.matchesDouble(comparable)) com.outcome = true;
                            }
                            else if (((String) com.comparedto).equalsIgnoreCase("integer")) {
                                if (aH.matchesInteger(comparable)) com.outcome = true;
                            }
                            else if (((String) com.comparedto).equalsIgnoreCase("even integer")) {
                                if (aH.matchesInteger(comparable) && (aH.getIntegerFrom(comparable) % 2) == 0)
                                    com.outcome = true;
                            }
                            else if (((String) com.comparedto).equalsIgnoreCase("odd integer")) {
                                if (aH.matchesInteger(comparable) && (aH.getIntegerFrom(comparable) % 2) == 1)
                                    com.outcome = true;
                            }
                            else if (((String) com.comparedto).equalsIgnoreCase("duration")) {
                                if (aH.matchesDuration("duration:" + comparable)) com.outcome = true;
                            }
                            else if (((String) com.comparedto).equalsIgnoreCase("boolean")) {
                                if (comparable.equalsIgnoreCase("true")
                                        || comparable.equalsIgnoreCase("false")) com.outcome = true;
                            }
                            else if (((String) com.comparedto).equalsIgnoreCase("entitytype")) {
                                if (aH.matchesEntityType("entity:" + comparable)) com.outcome = true;
                            }
                            else if (((String) com.comparedto).equalsIgnoreCase("livingentity")) {
                                if (aH.getLivingEntityFrom(comparable) != null) com.outcome = true;
                            }
                            else if (((String) com.comparedto).equalsIgnoreCase("npc")) {
                                if (CitizensAPI.getNPCRegistry().getNPC(aH.getLivingEntityFrom(comparable)) != null) com.outcome = true;
                            }
                            else if (((String) com.comparedto).equalsIgnoreCase("player")) {
                                if (aH.getPlayerFrom(comparable) != null) com.outcome = true;
                            }
                            else if (((String) com.comparedto).equalsIgnoreCase("offlineplayer")) {
                                if (aH.getOfflinePlayerFrom(comparable) != null) com.outcome = true;
                            }
                            else if (((String) com.comparedto).equalsIgnoreCase("item")) {
                                if (aH.matchesItem("item:" + comparable)) com.outcome = true;
                            }
                            break;
                        // ORMORE/ORLESS/etc. deal with the LENGTH of the the comparable/comparedto strings
                        case ORMORE:
                            if (((String) com.comparable).length() >= ((String) com.comparedto).length()) com.outcome = true;
                            break;
                        case ORLESS:
                            if (((String) com.comparable).length() <= ((String) com.comparedto).length()) com.outcome = true;
                            break;
                        case MORE:
                            if (((String) com.comparable).length() > ((String) com.comparedto).length()) com.outcome = true;
                            break;
                        case LESS:
                            if (((String) com.comparable).length() < ((String) com.comparedto).length()) com.outcome = true;
                            break;

                    }
                }

            }	else if (com.comparable instanceof List) {
                switch(com.operator) {
                    case CONTAINS:
                        for (String string : ((List<String>) com.comparable)) {
                            if (com.comparedto instanceof Integer) {
                                if (aH.getIntegerFrom(string) == (Integer) com.comparedto) com.outcome = true;

                            }   else if (com.comparedto instanceof Double) {
                                if (aH.getDoubleFrom(string) == (Double) com.comparedto) com.outcome = true;

                            }	else if (com.comparedto instanceof Boolean) {
                                if (Boolean.valueOf(string).booleanValue() == ((Boolean) com.comparedto).booleanValue()) com.outcome = true;

                            }   else if (com.comparedto instanceof String) {
                                if (string.equalsIgnoreCase((String) com.comparedto)) com.outcome = true;
                            }
                        }
                        break;
                    case ORMORE:
                        if (((List<String>) com.comparable).size() >= (Integer.parseInt(String.valueOf(com.comparedto)))) com.outcome = true;
                        break;
                    case ORLESS:
                        if (((List<String>) com.comparable).size() <= (Integer.parseInt(String.valueOf(com.comparedto)))) com.outcome = true;
                        break;
                    case MORE:
                        if (((List<String>) com.comparable).size() > (Integer.parseInt(String.valueOf(com.comparedto)))) com.outcome = true;
                        break;
                    case LESS:
                        if (((List<String>) com.comparable).size() < (Integer.parseInt(String.valueOf(com.comparedto)))) com.outcome = true;
                        break;
                }


                //
                // COMPARABLE IS DOUBLE
                //
            }   else if (com.comparable instanceof Double) {

                // Check to make sure comparedto is Double
                if (!(com.comparedto instanceof Double)) {
                    // Not comparing with a Double, outcome = false
                } else {

                    switch(com.operator) {
                        case EQUALS:
                            if (((Double) com.comparable).compareTo((Double) com.comparedto) == 0) com.outcome = true;
                            break;
                        case ORMORE:
                            if (((Double) com.comparable).compareTo((Double) com.comparedto) >= 0) com.outcome = true;
                            break;
                        case ORLESS:
                            if (((Double) com.comparable).compareTo((Double) com.comparedto) <= 0) com.outcome = true;
                            break;
                        case MORE:
                            if (((Double) com.comparable).compareTo((Double) com.comparedto) > 0) com.outcome = true;
                            break;
                        case LESS:
                            if (((Double) com.comparable).compareTo((Double) com.comparedto) < 0) com.outcome = true;
                            break;
                    }
                }


                //
                // COMPARABLE IS INTEGER
                //
            }	else if (com.comparable instanceof Integer) {

                // Check to make sure comparedto is Integer
                if (!(com.comparedto instanceof Integer)) {
                    // Not comparing with an Integer, outcome = false;
                } else {
                    // Comparing integers.. let's do the logic
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
                        case MORE:
                            if (((Integer) com.comparable).compareTo((Integer) com.comparedto) > 0) com.outcome = true;
                            break;
                        case LESS:
                            if (((Integer) com.comparable).compareTo((Integer) com.comparedto) < 0) com.outcome = true;
                            break;
                    }
                }


                //
                // COMPARABLE IS BOOLEAN
                //
            }   else if (com.comparable instanceof Boolean) {

                // Check to make sure comparedto is Boolean
                if (!(com.comparedto instanceof Boolean)) {
                    // Not comparing with a Boolean, outcome = false;
                } else {
                    // Comparing booleans.. let's do the logic
                    if ((Boolean) com.comparable.equals((Boolean) com.comparedto))
                        com.outcome = true;
                    else
                        com.outcome = false;
                }
            }

            if (com.logic == Logic.NEGATIVE) com.outcome = !com.outcome;

            // Show outcome of Comparable
            dB.echoDebug(ChatColor.YELLOW + "Comparable " + counter + ": " + ChatColor.WHITE + com.toString());
            counter++;
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
            if (compareable.bridge == Bridge.AND) {
                if (compareable.outcome) andmet++;
                andcount++;
            }
        }

        // Determine outcome -- do, or else?
        if (ormet > 0 && andcount == andmet) doCommands(scriptEntry, "outcomeCommands");
        else doCommands(scriptEntry, "elseCommands");
    }


    // Finds the object type of the String arg

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

            // If a List<Object>
        else if (arg.contains("|")) {
            return aH.getListFrom(arg);
        }

        // If none of the above, must be a String! :D
        // 'arg' is already a String, so return it.
        else return arg;
    }


    // Attempts to match a String arg with a Comparable's comparable

    private Object matchObjectType(Comparable match, String arg) {

        // If MATCHES, change comparable to String
        if (match.operator == Operator.MATCHES)
            match.comparable = String.valueOf(match.comparable);

        Object comparable = match.comparable;

        // Comparable is String, return String
        if (comparable instanceof String)
            return arg;

            // Comparable is Double, return Double
        else if (comparable instanceof Double) {
            if (aH.matchesDouble(arg) || aH.matchesInteger(arg))
                return aH.getDoubleFrom(arg);
            else {
                dB.echoDebug(ChatColor.YELLOW + "WARNING! " + ChatColor.WHITE + "Cannot compare DOUBLE("
                        + match.comparable + ") with '" + arg + "'. Outcome for this Comparable will be false.");
                return Double.NaN;
            }
        }

        else if (comparable instanceof Integer) {
            // If comparedTo is Double, convert comparable to double as well
            if (aH.matchesDouble(arg)) {
                match.comparable = Double.valueOf(String.valueOf(match.comparable));
                return aH.getDoubleFrom(arg);
            } else if (aH.matchesInteger(arg)) {
                return aH.getIntegerFrom(arg);
            } else {
                dB.echoDebug(ChatColor.YELLOW + "WARNING! " + ChatColor.WHITE + "Cannot compare INTEGER("
                        + match.comparable + ") with '" + arg + "'. Outcome for this Comparable will be false.");
                return Double.NaN;
            }
        }

        else if (comparable instanceof Boolean) {
            return aH.getBooleanFrom(arg);
        }

        else if (comparable instanceof List) {
            return arg;
        }

        return arg;
    }


    // Runs the outcome-command if the evaluation of the statement == true

    @SuppressWarnings("unchecked")
	private void doCommands(ScriptEntry scriptEntry, String map) {
    	
    	TreeMap<Integer, ArrayList<String>> commandMap = (TreeMap<Integer, ArrayList<String>>) scriptEntry.getObject(map);
    	
    	List<ScriptEntry> entries = new ArrayList<ScriptEntry>();
    	
    	for (Map.Entry<Integer, ArrayList<String>> pairs : commandMap.entrySet())
    	{
    		ArrayList<String> commandArray = pairs.getValue();
    		String command = commandArray.get(0);
    		commandArray.remove(0);
    		String[] arguments = commandArray.toArray(new String[commandArray.size()]);
    		
            try {
            	ScriptEntry entry = new ScriptEntry(command, arguments,
            			scriptEntry.getScript().getContainer())
                		.setPlayer(scriptEntry.getPlayer())
                		.setNPC(scriptEntry.getNPC()).setInstant(true)
                		.addObject("reqId", scriptEntry.getObject("reqId"));
            
            	entries.add(entry);
                
            } catch (ScriptEntryCreationException e) {
                dB.echoError("There has been a problem running the Command. Check syntax.");
                if (dB.showStackTraces) {
                    dB.echoDebug("STACKTRACE follows:");
                    e.printStackTrace();
                }
                else dB.echoDebug("Use '/denizen debug -s' for the nitty-gritty.");
            }
    	}
    	
        scriptEntry.getResidingQueue().injectEntries(entries, 0);
    }
}