package net.aufdemrand.denizen.scripts.commands.core;

import java.lang.*;
import java.util.*;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.exceptions.ScriptEntryCreationException;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.debugging.dB;

import org.bukkit.ChatColor;

/**
 * Core dScript IF command.
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
        boolean building_comparables = true;

        // What to do depending on the logic of the comparables
        // is stored in two strings
        TreeMap<Integer, ArrayList<String>> then_outcome = new TreeMap<Integer, ArrayList<String>>();
        TreeMap<Integer, ArrayList<String>> else_outcome = new TreeMap<Integer, ArrayList<String>>();
        // Need this for building the outcomes
        boolean then_used = false;

        // Keep track of this to avoid Denizen overlooking comparedTo when an operator is used
        // with a value that matches the name of a command. (Good find dimensionZ!)
        boolean used_operator = false;

        // Track whether we are adding a new command or not
        boolean newCommand = false;

        // Track whether we are inside recursive brackets whose contents
        // should be added as arguments to our current If, not as commands
        int bracketsEntered = 0;

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {
            if (building_comparables) {

                // Set logic to NEGATIVE
                if (arg.startsWith("!")) {
                    comparables.get(comparables.size() - 1).setNegativeLogic();
                    if (arg.getValue().length() == 1) continue;
                    if (arg.startsWith("!=")) arg.replaceValue("==");
                    else arg.replaceValue(arg.getValue().substring(1));
                }

                // Replace symbol-operators/bridges with ENUM value for matching
                arg.replaceValue(arg.getValue().replace("==", "EQUALS").replace(">=", "ORMORE").replace("<=", "ORLESS")
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
                    used_operator = true;
                }

                // Set comparable
                else if (comparables.get(comparables.size() - 1).comparable == null) {
                    // If using MATCHES operator, keep as string.
                    comparables.get(comparables.size() - 1).setComparable(arg.getValue());
                }

                else if (!used_operator && arg.matches("{"))
                    building_comparables = false;

                // Check if filling comparables are done by checking the command registry for valid commands.
                // If using an operator though, skip on to compared-to!
                else if (!used_operator && denizen.getCommandRegistry()
                        .get(arg.getValue().replace("^", "")) != null) {
                    building_comparables = false;
                }

                // Set compared-to
                else {
                    comparables.get(comparables.size() - 1).setComparedto(arg.getValue());
                    used_operator = false;
                }
            }

            if (!building_comparables) {

                // Read "-" as meaning we are moving to a new command, unless we
                // are inside nested brackets (i.e. bracketsEntered of at least 2)
                // in which case we just treat what follows as arguments
                if (arg.matches("-") && bracketsEntered < 2) {
                    newCommand = true;
                }

                else if (arg.matches("else")) then_used = true;

                else if (!then_used) {
                    if (arg.matches("{")) {
                        bracketsEntered++;
                        if (bracketsEntered > 1)
                            then_outcome.get(then_outcome.lastKey()).add(arg.getValue());
                    }

                    else if (arg.matches("}")) {
                        bracketsEntered--;
                        if (bracketsEntered > 0)
                            then_outcome.get(then_outcome.lastKey()).add(arg.getValue());
                    }

                    else if (then_outcome.size() == 0) {
                        then_outcome.put(then_outcome.size(), new ArrayList<String>());
                        then_outcome.get(then_outcome.lastKey()).add(arg.getValue());
                        newCommand = false;
                    }

                    else if (newCommand == true) {
                        newCommand = false;
                        then_outcome.put(then_outcome.size(), new ArrayList<String>());
                        then_outcome.get(then_outcome.lastKey()).add(arg.getValue());
                    }

                    // Add new outcome argument
                    else {
                        then_outcome.get(then_outcome.lastKey()).add(arg.getValue());
                    }
                }

                else if (then_used) {

                    // If we find a bracket, and we're already inside
                    // nested brackets, add the bracket to the current
                    // command's arguments
                    if (arg.matches("{")) {
                        bracketsEntered++;
                        if (bracketsEntered > 1) {
                            else_outcome.get(else_outcome.lastKey()).add(arg.getValue());
                        }
                    }

                    else if (arg.matches("}")) {
                        bracketsEntered--;
                        if (bracketsEntered > 0) {
                            else_outcome.get(else_outcome.lastKey()).add(arg.getValue());
                        }
                    }

                    // Add new else command if the last argument was a non-nested "-"
                    // or if it was "else" and we have no else commands yet
                    else if (newCommand == true || else_outcome.size() == 0) {
                        newCommand = false;
                        else_outcome.put(else_outcome.size(), new ArrayList<String>());
                        else_outcome.get(else_outcome.lastKey()).add(arg.getValue());

                        // If we find an "if", act like we entered a set of
                        // brackets, so we treat the if's commands as arguments
                        // and don't add them to our current else commands
                        // if (arg.matches("if")) {
                        //    bracketsEntered++;
                        // }
                    }

                    // Add new else argument
                    else {
                        else_outcome.get(else_outcome.lastKey()).add(arg.getValue());
                    }
                }
            }
        }

        // Stash objects required to execute() into the ScriptEntry
        scriptEntry.addObject("comparables", comparables)
                .addObject("then-outcome", then_outcome)
                .addObject("else-outcome", else_outcome);
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
            if (ormet > 0)
                do_then = true;
            else if (andcount == andmet && comparables.get(0).outcome == true)
                do_then = true;
            else do_then = false;
        } else
            do_then = comparables.get(0).outcome;

        // Determine outcome -- then, or else?
        if (do_then) {
            dB.log("then: " + scriptEntry.getObject("then-outcome").toString());
            doCommand(scriptEntry, "then-outcome");
        }
        else {
            dB.log("else: " + scriptEntry.getObject("else-outcome").toString());
            doCommand(scriptEntry, "else-outcome");
        }
    }


    private void doCommand(ScriptEntry scriptEntry, String map) {
        TreeMap<Integer, ArrayList<String>> commandMap =
                (TreeMap<Integer, ArrayList<String>>) scriptEntry.getObject(map);

        if (map == null || map.length() == 0) return;

        List<ScriptEntry> entries = new ArrayList<ScriptEntry>();

        for (Map.Entry<Integer, ArrayList<String>> pairs : commandMap.entrySet()) {
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