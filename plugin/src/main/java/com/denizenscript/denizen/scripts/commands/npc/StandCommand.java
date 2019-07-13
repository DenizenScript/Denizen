package com.denizenscript.denizen.scripts.commands.npc;

import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.npc.traits.SittingTrait;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.*;

public class StandCommand extends AbstractCommand {

    // <--[command]
    // @Name Stand
    // @Syntax stand
    // @Required 0
    // @Plugin Citizens
    // @Short Causes the NPC to stand. To make them sit, see <@link command Sit>.
    // @Group npc
    //
    // @Description
    // Makes the linked NPC stop sitting.
    // To make them sit, see <@link command Sit>.
    //
    // @Tags
    // None
    //
    // @Usage
    // Make the linked NPC stand up.
    // - stand
    //
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry)
            throws InvalidArgumentsException {
        //stand should have no additional arguments
        for (Argument arg : ArgumentHelper.interpretArguments(scriptEntry.aHArgs)) {
            arg.reportUnhandled();
        }
        if (!Utilities.entryHasNPC(scriptEntry)) {
            throw new InvalidArgumentsException("This command requires a linked NPC!");
        }

    }

    @Override
    public void execute(ScriptEntry scriptEntry) {

        if (Utilities.getEntryNPC(scriptEntry).getEntityType() != EntityType.PLAYER
                && Utilities.getEntryNPC(scriptEntry).getEntityType() != EntityType.OCELOT
                && Utilities.getEntryNPC(scriptEntry).getEntityType() != EntityType.WOLF) {
            Debug.echoError(scriptEntry.getResidingQueue(), "...only Player, ocelot, or wolf type NPCs can sit!");
            return;
        }

        if (scriptEntry.dbCallShouldDebug()) {

            Debug.report(scriptEntry, getName(), ArgumentHelper.debugObj("npc", Utilities.getEntryNPC(scriptEntry)));

        }

        Entity entity = Utilities.getEntryNPC(scriptEntry).getEntity();
        if (entity instanceof Sittable) {
            ((Sittable) entity).setSitting(false);
        }
        else {
            NPC npc = Utilities.getEntryNPC(scriptEntry).getCitizen();
            SittingTrait trait = npc.getTrait(SittingTrait.class);

            if (!npc.hasTrait(SittingTrait.class)) {
                npc.addTrait(SittingTrait.class);
                Debug.echoDebug(scriptEntry, "...added sitting trait");
            }

            trait.stand();
            npc.removeTrait(SittingTrait.class);
        }
    }
}
