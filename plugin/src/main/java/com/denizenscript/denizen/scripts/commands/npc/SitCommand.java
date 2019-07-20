package com.denizenscript.denizen.scripts.commands.npc;

import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.npc.traits.SittingTrait;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.entity.*;

public class SitCommand extends AbstractCommand {

    // <--[command]
    // @Name Sit
    // @Syntax sit (<location>)
    // @Required 0
    // @Short Causes the NPC to sit. To make them stand, see <@link command Stand>.
    // @Group npc
    //
    // @Description
    // Makes the linked NPC sit at the specified location.
    // Use <@link command Stand> to make the NPC stand up again.
    //
    // @Tags
    // None
    //
    // @Usage
    // Make the linked NPC sit at the player's cursor location.
    // - sit <player.location.cursor_on>
    //
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (Argument arg : scriptEntry.getProcessedArgs()) {
            if (arg.matchesArgumentType(LocationTag.class)
                    && !scriptEntry.hasObject("location")) {
                scriptEntry.addObject("location", arg.asType(LocationTag.class));
            }
            else {
                arg.reportUnhandled();
            }
        }
        if (!Utilities.entryHasNPC(scriptEntry)) {
            throw new InvalidArgumentsException("This command requires a linked NPC!");
        }

    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        LocationTag location = (LocationTag) scriptEntry.getObject("location");
        if (Utilities.getEntryNPC(scriptEntry).getEntityType() != EntityType.PLAYER
                && Utilities.getEntryNPC(scriptEntry).getEntityType() != EntityType.OCELOT
                && Utilities.getEntryNPC(scriptEntry).getEntityType() != EntityType.WOLF) {
            Debug.echoError(scriptEntry.getResidingQueue(), "...only Player, ocelot, or wolf type NPCs can sit!");
            return;
        }

        if (scriptEntry.dbCallShouldDebug()) {

            Debug.report(scriptEntry, getName(), ArgumentHelper.debugObj("npc", Utilities.getEntryNPC(scriptEntry))
                    + (location != null ? location.debug() : ""));

        }

        Entity entity = Utilities.getEntryNPC(scriptEntry).getEntity();
        if (entity instanceof Sittable) {
            ((Sittable) entity).setSitting(true);
        }
        else {
            SittingTrait trait = Utilities.getEntryNPC(scriptEntry).getCitizen().getTrait(SittingTrait.class);
            if (!Utilities.getEntryNPC(scriptEntry).getCitizen().hasTrait(SittingTrait.class)) {
                Utilities.getEntryNPC(scriptEntry).getCitizen().addTrait(SittingTrait.class);
                Debug.echoDebug(scriptEntry, "...added sitting trait");
            }

            if (location != null) {
                trait.sit(location);
            }
            else {
                trait.sit();
            }
        }
    }
}
