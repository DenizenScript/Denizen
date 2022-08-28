package com.denizenscript.denizen.scripts.commands.world;

import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;

public class StrikeCommand extends AbstractCommand {

    public StrikeCommand() {
        setName("strike");
        setSyntax("strike [<location>] (no_damage) (silent)");
        setRequiredArguments(1, 3);
        isProcedural = false;
        setBooleansHandled("no_damage", "silent");
    }

    // <--[command]
    // @Name Strike
    // @Syntax strike [<location>] (no_damage) (silent)
    // @Required 1
    // @Maximum 3
    // @Short Strikes lightning down upon the location.
    // @Synonyms Lightning
    // @Group world
    //
    // @Description
    // Causes lightning to strike at the specified location, which can optionally have damage disabled.
    //
    // The lightning will still cause fires to start, even without the 'no_damage' argument.
    //
    // Lightning caused by this command will cause creepers to activate. Using the no_damage argument makes the
    // lightning do no damage to the player or any other entities, and means creepers struck will not activate.
    //
    // Use 'silent' to remove the sound of the lightning strike.
    // NOTE: The 'silent' option appears to have been removed in a Minecraft update and thus lightning will be audible until/unless Mojang re-adds it.
    //
    // @Tags
    // None
    //
    // @Usage
    // Use to cause lightning to strike the player.
    // - strike <player.location>
    //
    // @Usage
    // Use to strike the player with lightning causing no damage.
    // - strike no_damage <player.location>
    // -->

    @Override
    public void addCustomTabCompletions(TabCompletionsBuilder tab) {
        tab.addNotesOfType(LocationTag.class);
    }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
            if (!scriptEntry.hasObject("location")
                    && arg.matchesArgumentType(LocationTag.class)) {
                scriptEntry.addObject("location", arg.asType(LocationTag.class));
            }
            else {
                arg.reportUnhandled();
            }
        }
        if (!scriptEntry.hasObject("location")) {
            throw new InvalidArgumentsException("Missing location argument!");
        }
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        LocationTag location = scriptEntry.getObjectTag("location");
        boolean noDamage = scriptEntry.argAsBoolean("no_damage");
        boolean silent = scriptEntry.argAsBoolean("silent");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), location, db("no_damage", noDamage), db("silent", silent));
        }
        if (noDamage) {
            location.getWorld().spigot().strikeLightningEffect(location, silent);
        }
        else {
            location.getWorld().spigot().strikeLightning(location, silent);
        }
    }
}
