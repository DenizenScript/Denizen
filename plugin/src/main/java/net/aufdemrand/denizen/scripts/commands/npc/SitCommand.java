package net.aufdemrand.denizen.scripts.commands.npc;

import net.aufdemrand.denizen.npc.traits.SittingTrait;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.utilities.Utilities;
import net.aufdemrand.denizen.utilities.debugging.dB;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.aH;
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

        for (aH.Argument arg : aH.interpretArguments(scriptEntry.aHArgs)) {
            if (arg.matchesArgumentType(dLocation.class)
                    && !scriptEntry.hasObject("location")) {
                scriptEntry.addObject("location", arg.asType(dLocation.class));
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
        dLocation location = (dLocation) scriptEntry.getObject("location");
        if (Utilities.getEntryNPC(scriptEntry).getEntityType() != EntityType.PLAYER
                && Utilities.getEntryNPC(scriptEntry).getEntityType() != EntityType.OCELOT
                && Utilities.getEntryNPC(scriptEntry).getEntityType() != EntityType.WOLF) {
            dB.echoError(scriptEntry.getResidingQueue(), "...only Player, ocelot, or wolf type NPCs can sit!");
            return;
        }

        if (scriptEntry.dbCallShouldDebug()) {

            dB.report(scriptEntry, getName(), aH.debugObj("npc", Utilities.getEntryNPC(scriptEntry))
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
                dB.echoDebug(scriptEntry, "...added sitting trait");
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
