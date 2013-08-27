package net.aufdemrand.denizen.scripts.commands.core;

import java.util.*;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.exceptions.ScriptEntryCreationException;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.scripts.ScriptBuilder;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.debugging.dB;

import org.bukkit.ChatColor;

/**
 * Core dScript If command.
 *
 * @author Jeremy Schroeder, David Cernat
 */

public class IfCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Comparables check the logic
        List<Comparable> comparables = new ArrayList<Comparable>();
        // Insert new comparable into the list
        comparables.add(new Comparable());

        // Indicate that comparables are building
        boolean buildingComparables = true;

        // What to do depending on the logic of the comparables
        // is stored in two tree maps
        TreeMap<Integer, ArrayList<String>> thenOutcome = new TreeMap<Integer, ArrayList<String>>();
        TreeMap<Integer, ArrayList<String>> elseOutcome = new TreeMap<Integer, ArrayList<String>>();
        
        // Keep tracking of whether we're inside the Else part of the statement or not
        boolean insideElse = false;

        // Keep track of this to avoid Denizen overlooking comparedTo when an operator is used
        // with a value that matches the name of a command. (Good find dimensionZ!)
        boolean usedOperator = false;

        // Track whether we are adding a new command or not
        boolean newCommand = false;

        // Track whether we are inside nested brackets whose contents
        // should be added as arguments to our current If, not as commands
        int bracketsEntered = 0;

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {
            if (buildingComparables) {

                // Set logic to NEGATIVE
                if (arg.startsWith("!")) {
                    comparables.get(comparables.size() - 1).setNegativeLogic();
                    if (arg.getValue().length() == 1) continue;
                    if (arg.startsWith("!=")) arg.replaceValue("==");
                    else arg.replaceValue(arg.getValue().substring(1));
                }

                // Replace symbol-operators/bridges with ENUM value for matching
                arg.replaceValue(arg.getValue().replace("==", "EQUALS").replace(">=", "OR_MORE").replace("<=", "OR_LESS")
                        .replace("<", "LESS").replace(">", "MORE").replace("||", "OR").replace("&&", "AND"));

                // Set bridge
                if (arg.matchesEnum(Comparable.Bridge.values())) {
                    // new Comparable to add to the list
                    comparables.add(new Comparable());
                    comparables.get(comparables.size() - 1).bridge =
                            Comparable.Bridge.valueOf(arg.getValue().toUpperCase());
                }

                // Set operator (Optional, default is EQUALS)
                else if (arg.matchesEnum(Comparable.Operator.values())) {
                    comparables.get(comparables.size() - 1).operator =
                            Comparable.Operator.valueOf(arg.getValue().toUpperCase());
                    usedOperator = true;
                }

                // Set comparable
                else if (comparables.get(comparables.size() - 1).comparable == null) {
                    // If using MATCHES operator, keep as string.
                    comparables.get(comparables.size() - 1).setComparable(arg.getValue());
                }

                else if (!usedOperator && arg.matches("{")) {
                    buildingComparables = false;
                }

                // Check if filling comparables are done by checking the command registry for valid commands.
                // If using an operator though, skip on to compared-to!
                else if (!usedOperator && denizen.getCommandRegistry()
                        .get(arg.getValue().replace("^", "")) != null) {
                    buildingComparables = false;
                }

                // Set compared-to
                else {
                    comparables.get(comparables.size() - 1).setComparedto(arg.getValue());
                    usedOperator = false;
                }
            }

            if (!buildingComparables) {

                // Read "-" as meaning we are moving to a new command, unless we
                // are inside nested brackets (i.e. bracketsEntered of at least 2)
                // in which case we just treat what follows as arguments
                if (arg.matches("-") && bracketsEntered < 2) {
                    newCommand = true;
                }

                else if (!insideElse) {
                    
                    // Move to else commands if we read an "else" and we're not
                    // currently going through nested arguments
                    if (arg.matches("else") && bracketsEntered == 0) {
                        insideElse = true;
                    }
                    
                    // If we find a bracket, and we're already inside
                    // nested brackets, add the bracket to the current
                    // command's arguments
                    else if (arg.matches("{")) {
                        bracketsEntered++;
                        
                        if (bracketsEntered > 1) {
                            thenOutcome.get(thenOutcome.lastKey()).add(arg.raw_value);
                        }
                    }

                    else if (arg.matches("}")) {
                        bracketsEntered--;

                        if (bracketsEntered > 0) {
                            thenOutcome.get(thenOutcome.lastKey()).add(arg.raw_value);
                        }
                    }

                    // Add new outcome command if the last argument was a non-nested "-"
                    // or if there are no outcome commands yet
                    else if (newCommand || thenOutcome.size() == 0) {
                        thenOutcome.put(thenOutcome.size(), new ArrayList<String>());
                        thenOutcome.get(thenOutcome.lastKey()).add(arg.raw_value);
                        newCommand = false;
                    }

                    // Add new outcome argument
                    else {
                        thenOutcome.get(thenOutcome.lastKey()).add(arg.raw_value);
                    }
                }

                else if (insideElse) {

                    // If we find a bracket, and we're already inside
                    // nested brackets, add the bracket to the current
                    // command's arguments
                    if (arg.matches("{")) {
                        bracketsEntered++;
                        
                        if (bracketsEntered > 1) {
                            elseOutcome.get(elseOutcome.lastKey()).add(arg.raw_value);
                        }
                    }

                    else if (arg.matches("}")) {
                        bracketsEntered--;
                        
                        if (bracketsEntered > 0) {
                            elseOutcome.get(elseOutcome.lastKey()).add(arg.raw_value);
                        }
                    }

                    // Add new else command if the last argument was a non-nested "-"
                    // or if it was "else" and we have no else commands yet
                    else if (newCommand || elseOutcome.size() == 0) {
                        newCommand = false;
                        elseOutcome.put(elseOutcome.size(), new ArrayList<String>());
                        elseOutcome.get(elseOutcome.lastKey()).add(arg.raw_value);

                        // Important!
                        //
                        // If we find an "if", act like we entered a set of
                        // brackets, so we treat the if's commands as arguments
                        // and don't add them to our current else commands
                        if (arg.matches("if")) {
                            bracketsEntered++;
                        }
                    }

                    // Add new else argument
                    else {
                        elseOutcome.get(elseOutcome.lastKey()).add(arg.raw_value);
                    }
                }
            }
        }

        // Stash objects required to execute() into the ScriptEntry
        scriptEntry.addObject("comparables", comparables)
                .addObject("then-outcome", thenOutcome)
                .addObject("else-outcome", elseOutcome);
    }


    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        // Grab comparables from the ScriptEntry
        List<Comparable> comparables = (List<Comparable>) scriptEntry.getObject("comparables");

        int counter = 1;

        // Evaluate comparables
        for (Comparable com : comparables) {
            com.determineOutcome();

            // Show outcome of Comparable
            dB.echoDebug(ChatColor.YELLOW + "Comparable " + counter + ": " + ChatColor.WHITE + com.toString());
            counter++;
        }

        // Compare outcomes 

        int ormet = 0;
        for (Comparable comparable : comparables) {
            if (comparable.bridge == Comparable.Bridge.OR)
                if (comparable.outcome) ormet++;
        }

        int andcount = 0;
        int andmet = 0;
        for (Comparable comparable : comparables) {
            if (comparable.bridge == Comparable.Bridge.AND) {
                if (comparable.outcome) andmet++;
                andcount++;
            }
        }

        boolean do_then;

        if (comparables.size() > 1) {
                do_then = (ormet > 0) || (andcount == andmet && comparables.get(0).outcome == true);
        } else
            do_then = comparables.get(0).outcome;

        // Determine outcome -- then, or else?
        if (do_then) {
            // dB.log("then: " + scriptEntry.getObject("then-outcome").toString());
            doCommand(scriptEntry, "then-outcome");
        }
        else {
            // dB.log("else: " + scriptEntry.getObject("else-outcome").toString());
            doCommand(scriptEntry, "else-outcome");
        }
    }


    private void doCommand(ScriptEntry scriptEntry, String mapName) {
        TreeMap<Integer, ArrayList<String>> commandMap =
                (TreeMap<Integer, ArrayList<String>>) scriptEntry.getObject(mapName);

        if (commandMap == null || commandMap.size() == 0) return;

        List<ScriptEntry> entries = new ArrayList<ScriptEntry>();

        for (Map.Entry<Integer, ArrayList<String>> pairs : commandMap.entrySet()) {
            ArrayList<String> commandArray = pairs.getValue();
            String command = commandArray.get(0);
            commandArray.remove(0);
            String[] arguments = commandArray.toArray(new String[commandArray.size()]);

            try {
                ScriptEntry entry = new ScriptEntry(command, arguments,
                        (scriptEntry.getScript() == null ? null : scriptEntry.getScript().getContainer()))
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

        // Put tracked objects into new script entries.
        for (String tracked_object : scriptEntry.tracked_objects) {
            ScriptBuilder.addObjectToEntries(entries, tracked_object, scriptEntry.getObject(tracked_object));
        }

        scriptEntry.getResidingQueue().injectEntries(entries, 0);
    }

}