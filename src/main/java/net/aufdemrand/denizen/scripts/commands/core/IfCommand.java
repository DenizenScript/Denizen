package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.exceptions.ScriptEntryCreationException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Core dScript IF command.
 *
 * @author Jeremy Schroeder
 */

public class IfCommand extends AbstractCommand {

    enum Operator { EQUALS, ISINT, ISDOUBLE, ISPLAYER, ORMORE, ORLESS, MORE, LESS, CONTAINS, ISEMPTY }
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

        @Override
        public String toString() {
            return  (logic != Logic.REGULAR ? "Logic=" + logic.toString() + ", " : "")
                    + "Comparable=" + (comparable == null ? "null" : comparable.getClass().getSimpleName()
                    + "(" + ChatColor.AQUA + comparable + ChatColor.WHITE + ")")
                    + ", Operator=" + operator.toString()
                    + ", ComparedTo=" + (comparedto == null ? "null" : comparedto.getClass().getSimpleName()
                    + "(" + ChatColor.AQUA + comparedto + ChatColor.WHITE + ") ")
                    + ChatColor.YELLOW + "--> OUTCOME='" + outcome + "'";
        }
    }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Initialize necessary fields
        List<Comparable> comparables = new ArrayList<Comparable>();

        String outcomeCommand = null;
        ArrayList<String> outcomeArgs = new ArrayList<String>();
        String elseCommand = null;
        ArrayList<String> elseArgs = new ArrayList<String>();

        comparables.add(new Comparable());
        int index = 0;

        // Iterate through the arguments, build comparables
        for (String arg : scriptEntry.getArguments()) {
            if (outcomeCommand == null) {
                // Set logic (Optional, default is REGULAR)
                if (arg.startsWith("!")) {
                    comparables.get(index).logic = Logic.NEGATIVE;
                    arg = arg.substring(1);
                }
                // Replace symbol-operators/bridges with ENUM value for matching
                arg = arg.replace("==", "EQUALS").replace("<", "LESS").replace(">", "MORE")
                        .replace(">=", "ORMORE").replace("<=", "ORLESS")
                        .replace("||", "OR").replace("&&", "AND");
                // Set bridge
                if (aH.matchesArg("OR", arg) || aH.matchesArg("AND", arg)) {
                    index++;
                    // new Comparable to add to the list
                    comparables.add(new Comparable());
                    comparables.get(index).bridge = Bridge.valueOf(arg.toUpperCase());
                }
                // Set operator (Optional, default is EQUALS)
                else if (aH.matchesArg("EQUALS", arg) || aH.matchesArg("ISINT", arg) || aH.matchesArg("ISDOUBLE", arg) || aH.matchesArg("ISPLAYER", arg) || aH.matchesArg("ISEMPTY", arg)
                        || aH.matchesArg("ORMORE", arg) || aH.matchesArg("MORE", arg) || aH.matchesArg("LESS", arg) || aH.matchesArg("ORLESS", arg) || aH.matchesArg("CONTAINS", arg))
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
                else {
                    outcomeArgs.add(arg);
                }

            } else {
                // Specify ELSE command
                if (elseCommand.equals("")) elseCommand = arg;
                    // Add elseArgs arguments
                else {
                    elseArgs.add(arg);
                }
            }
        }

        // Stash objects required to execute() into the ScriptEntry
        scriptEntry.addObject("comparables", comparables);
        scriptEntry.addObject("outcome-command", outcomeCommand);
        scriptEntry.addObject("outcome-command-args", outcomeArgs.toArray());
        scriptEntry.addObject("else-command", elseCommand);
        scriptEntry.addObject("else-command-args", elseArgs.toArray());
    }


    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        // Grab comparables from the ScriptEntry
        List<Comparable> comparables = (List<Comparable>) scriptEntry.getObject("comparables");

        int counter = 1;

        // Evaluate comparables
        for (Comparable com : comparables) {
            com.outcome = false;

            if (com.comparable instanceof String) {
                switch(com.operator) {
                    case ISEMPTY:
                        if (((String) com.comparable).equals("EMPTY")) com.outcome = true;
                        break;
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
                    case MORE:
                        if (((String) com.comparable).length() > ((String) com.comparedto).length()) com.outcome = true;
                        break;
                    case LESS:
                        if (((String) com.comparable).length() < ((String) com.comparedto).length()) com.outcome = true;
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
                                if (aH.getIntegerFrom(string) == ((Integer) com.comparedto).intValue()) com.outcome = true;
                                break;
                            }   else if (com.comparedto instanceof Double) {
                                if (aH.getDoubleFrom(string) == ((Double) com.comparedto).doubleValue()) com.outcome = true;
                                break;
                            }	else if (com.comparedto instanceof Boolean) {
                                if (Boolean.valueOf(string).booleanValue() == ((Boolean) com.comparedto).booleanValue()) com.outcome = true;
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


                //
                // COMPARABLE IS DOUBLE
                //
            }   else if (com.comparable instanceof Double) {

                // Check comparedto for Double, make it Integer
                if (com.comparedto instanceof Integer) {
                    dB.echoDebug(ChatColor.YELLOW + "WARNING! " + ChatColor.WHITE + "Attempting to compare DOUBLE("
                            + com.comparable + ") with INTEGER(" + com.comparedto + "). Converting INTEGER to DOUBLE "
                            + "value. If this is not intended, use the .ASINT or .ASDOUBLE modifier. See 'IF' documentation");
                    com.comparedto = ((Integer) com.comparedto).doubleValue();
                }
                // Check to make sure comparedto is Integer
                if (!(com.comparedto instanceof Integer)) {
                    dB.echoDebug(ChatColor.YELLOW + "WARNING! " + ChatColor.WHITE + "Cannot compare DOUBLE("
                            + com.comparable + ") with " + com.comparable.getClass().getSimpleName() + "("
                            + com.comparedto + "). Outcome for this Comparable will be false.");
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

                // Check comparedto for Double, make it Integer
                if (com.comparedto instanceof Double) {
                    dB.echoDebug(ChatColor.YELLOW + "WARNING! " + ChatColor.WHITE + "Attempting to compare INTEGER("
                            + com.comparable + ") with DOUBLE(" + com.comparedto + "). Converting DOUBLE to INTEGER "
                            + "value. If this is not intended, use the .ASDOUBLE or .ASINT modifier. See 'IF' documentation");
                    com.comparedto = ((Double) com.comparedto).intValue();
                }

                // Check to make sure comparedto is Integer
                if (!(com.comparedto instanceof Integer)) {
                    dB.echoDebug(ChatColor.YELLOW + "WARNING! " + ChatColor.WHITE + "Cannot compare INTEGER("
                            + com.comparable + ") with " + com.comparable.getClass().getSimpleName() + "("
                            + com.comparedto + "). Outcome for this Comparable will be false.");
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
                com.outcome = ((Boolean) com.comparable).booleanValue();
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
        if (ormet > 0 && andcount == andmet) doCommand(scriptEntry);
        else doElse(scriptEntry);
    }


    // Convert the string comparable/comparedto argument to a specific Object
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
            List<String> toList = new ArrayList<String>();
            for (String string : arg.split("|"))
                toList.add(string);
            return toList;
        }

        // Welp, if none of the above, must be a String! :D
        // 'arg' is already a String, so return it.
        else return arg;
    }


    private void doCommand(ScriptEntry scriptEntry) {
        String outcomeCommand = ((String) scriptEntry.getObject("outcome-command")).toUpperCase();
        String[] outcomeArgs = Arrays.copyOf((Object[]) scriptEntry.getObject("outcome-command-args"),
                ((Object[]) scriptEntry.getObject("outcome-command-args")).length, String[].class);
        try {
            denizen.getScriptEngine().getScriptExecuter().execute(
                    new ScriptEntry(outcomeCommand, outcomeArgs, scriptEntry.getPlayer(),
                            scriptEntry.getNPC(), scriptEntry.getScript(), scriptEntry.getStep()));
        } catch (ScriptEntryCreationException e) {
            dB.echoError("There has been a problem running the Command. Check syntax.");
            if (dB.showStackTraces) {
                dB.echoDebug("STACKTRACE follows:");
                e.printStackTrace();
            }
            else dB.echoDebug("Use '/denizen debug -s' for the nitty-gritty.");
        }
    }


    private void doElse(ScriptEntry scriptEntry) {

        String elseCommand = null;
        if (scriptEntry.getObject("else-command") != null)
            elseCommand = ((String) scriptEntry.getObject("else-command")).toUpperCase();
        String[] elseArgs = null;
        if (scriptEntry.getObject("else-command-args") != null)
            elseArgs = Arrays.copyOf((Object[]) scriptEntry.getObject("else-command-args"),
                ((Object[]) scriptEntry.getObject("else-command-args")).length, String[].class);
        if (elseCommand == null) return;

        try {
            denizen.getScriptEngine().getScriptExecuter().execute(
                    new ScriptEntry(elseCommand, elseArgs, scriptEntry.getPlayer(),
                            scriptEntry.getNPC(), scriptEntry.getScript(), scriptEntry.getStep()));
        } catch (ScriptEntryCreationException e) {
            dB.echoError("There has been a problem running the ELSE Command. Check syntax.");
            if (dB.showStackTraces) {
                dB.echoDebug("STACKTRACE follows:");
                e.printStackTrace();
            }
            else dB.echoDebug("Use '/denizen debug -s' for the nitty-gritty.");
        }
    }

    @Override
    public void onEnable() {
        // TODO Auto-generated method stub

    }

}