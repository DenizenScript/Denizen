package com.denizenscript.denizen.scripts.commands.world;

import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.notable.Notable;
import com.denizenscript.denizencore.objects.notable.NoteManager;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;

import java.util.function.Consumer;

public class StrikeCommand extends AbstractCommand {

    public StrikeCommand() {
        setName("strike");
        setSyntax("strike (no_damage) [<location>]");
        setRequiredArguments(1, 2);
        isProcedural = false;
    }

    // <--[command]
    // @Name Strike
    // @Syntax strike (no_damage) [<location>]
    // @Required 1
    // @Maximum 2
    // @Short Strikes lightning down upon the location.
    // @Synonyms Lightning
    // @Group world
    //
    // @Description
    // Causes lightning to strike at the specified location, which can optionally have damage disabled.
    // The lightning will still cause fires to start, even without the 'no_damage' argument.
    // Lightning caused by this command will cause creepers to activate. Using the no_damage argument makes the
    // lightning do no damage to the player or any other entities, and means creepers struck will not activate.
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
    public void addCustomTabCompletions(String arg, Consumer<String> addOne) {
        for (Notable note : NoteManager.notesByType.get(LocationTag.class)) {
            addOne.accept(NoteManager.getSavedId(note));
        }
    }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
            if (!scriptEntry.hasObject("location")
                    && arg.matchesArgumentType(LocationTag.class)) {
                scriptEntry.addObject("location", arg.asType(LocationTag.class));
            }
            else if (arg.matches("no_damage") || arg.matches("nodamage")) {
                scriptEntry.addObject("damage", new ElementTag(false));
            }
            else {
                arg.reportUnhandled();
            }
        }
        if (!scriptEntry.hasObject("location")) {
            throw new InvalidArgumentsException("Missing location argument!");
        }
        scriptEntry.defaultObject("damage", new ElementTag(true));
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        LocationTag location = scriptEntry.getObjectTag("location");
        Boolean damage = scriptEntry.getElement("damage").asBoolean();
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(),
                    location.debug()
                            + db("Damageable", String.valueOf(damage)));
        }
        if (damage) {
            location.getWorld().strikeLightning(location);
        }
        else {
            location.getWorld().strikeLightningEffect(location);
        }
    }
}
