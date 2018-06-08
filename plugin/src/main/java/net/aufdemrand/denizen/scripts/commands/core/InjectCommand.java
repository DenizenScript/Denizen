package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dScript;
import net.aufdemrand.denizencore.scripts.ScriptBuilder;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;

import java.util.List;

/**
 * Injects a task script in the current ScriptQueue.
 * This replaces the now-deprecated runtask command without the queue argument.
 *
 * @author Jeremy Schroeder
 */
public class InjectCommand extends AbstractCommand {

    // <--[example]
    // @Title Script Injection tutorial
    // @Description
    // Script injection is an alternative way to run task scripts. Pluses include the ability
    // to use and modify established definitions and context contained in the script. Check out
    // this tutorial on some simple ways to use script injection.
    //
    // @Code
    // # +-----------------------------------
    // # | Script Injection tutorial
    // # |
    // # | Script injection is an alternative way to run task scripts. Pluses include the ability
    // # | to use and modify established definitions and context contained in the script. Check out
    // # | this example on some simple ways to use script injection.
    // # |
    // # | Take the example of a 'command pack' script that provides a couple of commands for
    // # | administrators to use. First, a world script since we need to utilize events.
    //
    //
    // A Cool Command Pack:
    //   type: world
    //
    //   # +-- the SET UP --+
    //   # Let's create a local script that will check for permissions from players. Since
    //   # some people using this might want to use different criteria on who should be able to
    //   # use it (ie. group check/permissions check/op check), it's a good idea to expose this
    //   # for easy modification. Plus, since it's a utility script, we'll call it with inject
    //   # to reuse the code in each of the commands provided. We'll call it...
    //
    //   command_permission_check:
    //
    //   # This just checks op for most basic usage. Add additional comparisons to include more
    //   # criteria. If the player doesn't meet criteria, clear the queue. Since this code will
    //   # be injected, it'll clear the queue it was injected to, which in this case is exactly
    //   # what we need. Clearing the queue will 'cancel' any further execution to the script.
    //
    //   # But first, determine the script passively fulfilled so it doesn't display a help message.
    //   # Specifying 'passively' will allow the script to run beyond the 'determination'.
    //
    //   - determine passively fulfilled
    //   - if !<player.is_op> {
    //     - narrate 'You must be an administrator to use this command!'
    //     - queue clear
    //     }
    //
    //   # +-- the PAYOFF --+
    //   # Here's a couple simple commands that'll use our injected utility script!
    //
    //   events:
    //
    //     # | /TEL command - Teleport to your cursor!
    //     on tel command:
    //
    //     # Here's the inject usage, check this out. Since all the commands are intended to be
    //     # for administrators, making a check here for each command would be counterintutitive.
    //     # Let's instead just inject the command_permission_check local script! If the conditions
    //     # aren't met, it'll clear the queue so that the command doesn't go through.
    //     - inject locally command_permission_check
    //
    //     # Now the command, since we've checked permission...
    //     - narrate 'Teleporting to <player.location.cursor_on.add[0,2,0]>!'
    //     - teleport location:<player.location.cursor_on[150].add[0,2,0].with_pose[<player>]>
    //
    //
    //     # | /REMOVEMOBS command - Remove vanilla mob entities, not NPCs!
    //     on removemobs command:
    //
    //     # One more example -- notice the code is exactly the same! That's the point! Changing
    //     # up the command_permission_check local script will change all usages of the script.
    //     # Utility scripts are cool!
    //     - inject locally command_permission_check
    //
    //     # Now the command, since we've checked permission...
    //     - narrate 'Removing all mob entities within 15 blocks...!'
    //     - foreach <player.location.find.entities.within[15]> {
    //         - if <%value%.is_mob>
    //           remove %value%
    //       }
    //
    //
    // # Just remember. 'RUN' makes a NEW queue, and 'INJECT' uses an EXISTING queue!
    //
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (arg.matches("instant", "instantly")) {
                scriptEntry.addObject("instant", new Element(true));
            }
            else if (arg.matches("local", "locally")) {
                scriptEntry.addObject("local", new Element(true));
            }
            else if (!scriptEntry.hasObject("script")
                    && arg.matchesArgumentType(dScript.class)
                    && !arg.matchesPrefix("p", "path")) {
                scriptEntry.addObject("script", arg.asType(dScript.class));
            }
            else if (!scriptEntry.hasObject("path")) {
                scriptEntry.addObject("path", arg.asElement());
            }
            else {
                arg.reportUnhandled();
            }

        }

        if (!scriptEntry.hasObject("script") && !scriptEntry.hasObject("local")) {
            throw new InvalidArgumentsException("Must define a SCRIPT to be injected.");
        }

        if (!scriptEntry.hasObject("path") && scriptEntry.hasObject("local")) {
            throw new InvalidArgumentsException("Must specify a PATH.");
        }

    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        dB.report(scriptEntry, getName(),
                (scriptEntry.hasObject("script") ? scriptEntry.getdObject("script").debug() : scriptEntry.getScript().debug())
                        + (scriptEntry.hasObject("instant") ? scriptEntry.getdObject("instant").debug() : "")
                        + (scriptEntry.hasObject("path") ? scriptEntry.getElement("path").debug() : "")
                        + (scriptEntry.hasObject("local") ? scriptEntry.getElement("local").debug() : ""));

        // Get the script
        dScript script = scriptEntry.getdObject("script");

        // Get the entries
        List<ScriptEntry> entries;
        // If it's local
        if (scriptEntry.hasObject("local")) {
            entries = scriptEntry.getScript().getContainer().getEntries(scriptEntry.entryData.clone(),
                    scriptEntry.getElement("path").asString());
        }

        // If it has a path
        else if (scriptEntry.hasObject("path")) {
            entries = script.getContainer().getEntries(scriptEntry.entryData.clone(),
                    scriptEntry.getElement("path").asString());
        }

        // Else, assume standard path
        else {
            entries = script.getContainer().getBaseEntries(scriptEntry.entryData.clone());
        }

        // For determine
        ScriptBuilder.addObjectToEntries(entries, "ReqId", scriptEntry.getObject("ReqId"));

        // If 'instantly' was specified, run the commands immediately.
        if (scriptEntry.hasObject("instant")) {
            scriptEntry.getResidingQueue().runNow(entries, "INJECT");
        }
        else {
            // Inject the entries into the current scriptqueue
            scriptEntry.getResidingQueue().injectEntries(entries, 0);
        }

    }
}
