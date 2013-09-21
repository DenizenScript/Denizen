package net.aufdemrand.denizen.scripts.commands.npc;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;

/**
 * Renames an NPC.
 *
 *
 */

public class RenameCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("name"))
                scriptEntry.addObject("name", arg.asElement());

        }

        if (!scriptEntry.hasObject("name"))
            throw new InvalidArgumentsException("Must specify a name!");

        if (scriptEntry.getNPC() == null || !scriptEntry.getNPC().isValid())
            throw new InvalidArgumentsException("Must have a NPC attached!");
    }

    @Override
    public void execute(final ScriptEntry scriptEntry) throws CommandExecutionException {

        Element name = (Element) scriptEntry.getObject("name");

        dB.report(getName(), name.debug());

        NPC npc = scriptEntry.getNPC().getCitizen();

        Location prev = npc.isSpawned() ? npc.getBukkitEntity().getLocation() : null;
        npc.despawn(DespawnReason.PENDING_RESPAWN);
        npc.setName(name.asString());
        if (prev != null)
            npc.spawn(prev);

    }

}
