package net.aufdemrand.denizen.scripts.commands.npc;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;

public class RenameCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("name"))
                scriptEntry.addObject("name", arg.asElement());

        }

        if (!scriptEntry.hasObject("name"))
            throw new InvalidArgumentsException("Must specify a name!");

        if (((BukkitScriptEntryData) scriptEntry.entryData).getNPC() == null || !((BukkitScriptEntryData) scriptEntry.entryData).getNPC().isValid())
            throw new InvalidArgumentsException("Must have a NPC attached!");
    }

    @Override
    public void execute(final ScriptEntry scriptEntry) throws CommandExecutionException {

        Element name = (Element) scriptEntry.getObject("name");

        dB.report(scriptEntry, getName(), name.debug());

        NPC npc = ((BukkitScriptEntryData) scriptEntry.entryData).getNPC().getCitizen();

        Location prev = npc.isSpawned() ? npc.getEntity().getLocation() : null;
        npc.despawn(DespawnReason.PENDING_RESPAWN);
        npc.setName(name.asString().length() > 100 ? name.asString().substring(0, 100) : name.asString());
        if (prev != null)
            npc.spawn(prev);

    }
}
