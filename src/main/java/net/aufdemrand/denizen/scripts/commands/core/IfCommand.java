package net.aufdemrand.denizen.scripts.commands.core;

import java.lang.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
 * @author Jeremy Schroeder
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
        List<String> then_outcome = new ArrayList<String>();
        List<String> else_outcome = new ArrayList<String>();
        // Need this for building the outcomes
        boolean then_used = false;

        // Keep track of this to avoid Denizen overlooking comparedTo when an operator is used
        // with a value that matches the name of a command. (Good find dimensionZ!)
        boolean used_operator = false;

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
                    comparables.get(comparables.size() - 1).bridge = Comparable.Bridge.valueOf(arg.getValue().toUpperCase());
                }

                // Set operator (Optional, default is EQUALS)
                else if (arg.matchesEnum(Comparable.Operator.values())) {
                    comparables.get(comparables.size() - 1).operator = Comparable.Operator.valueOf(arg.getValue().toUpperCase());
                    used_operator = true;
                }

                // Set comparable
                else if (comparables.get(comparables.size() - 1).comparable == null) {
                    // If using MATCHES operator, keep as string.
                    comparables.get(comparables.size() - 1).setComparable(arg.getValue());
                }

                // Check if filling comparables are done by checking the command registry for valid commands.
                // If using an operator though, skip on to compared-to!
                else if (!used_operator && denizen.getCommandRegistry().get(arg.getValue().replace("^", "")) != null) {
                    building_comparables = false;
                }

                // Set compared-to
                else {
                    comparables.get(comparables.size() - 1).setComparedto(arg.getValue());
                    used_operator = false;
                }
            }

            if (!building_comparables) {
                if (arg.matches("else")) then_used = true;
                else if (!then_used)
                    then_outcome.add(arg.getValue());
                else if (then_used)
                    else_outcome.add(arg.getValue());
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

        // Determine outcome -- then, or else?
        if (ormet > 0 && andcount == andmet) doCommand(scriptEntry, (List<String>) scriptEntry.getObject("then_outcome"));
        else doCommand(scriptEntry, (List<String>) scriptEntry.getObject("else_outcome"));
    }


    private void doCommand(ScriptEntry scriptEntry, List<String> command) {

        if (command == null || command.size() < 1) return;

        String outcomeCommand = command.get(0);
        command.remove(0);
        String[] outcomeArgs = Arrays.copyOf(command.toArray(),
                (command.toArray()).length, String[].class);

        try {

            ScriptEntry entry = new ScriptEntry(outcomeCommand, outcomeArgs,
                scriptEntry.getScript().getContainer())
                .setPlayer(scriptEntry.getPlayer())
                .setNPC(scriptEntry.getNPC()).setInstant(true)
                .addObject("reqId", scriptEntry.getObject("reqId"));

            scriptEntry.getResidingQueue().injectEntry(entry, 0);

        } catch (ScriptEntryCreationException e) {

            dB.echoError("There has been a problem running the Command. Check syntax.");
            if (dB.showStackTraces) {
                dB.echoDebug("STACKTRACE follows:");
                e.printStackTrace();
            }

            else dB.echoDebug("Use '/denizen debug -s' for the nitty-gritty.");
        }
    }

}