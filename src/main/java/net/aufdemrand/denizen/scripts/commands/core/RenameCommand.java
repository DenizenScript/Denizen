package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.arguments.aH.ArgumentType;
import net.aufdemrand.denizen.utilities.arguments.dEntity;
import net.aufdemrand.denizen.utilities.arguments.dLocation;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftCreature;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftLivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Skeleton;

/**
 * Renames a NPC.
 *
 *
 */

public class RenameCommand extends AbstractCommand {
	
    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (String arg : scriptEntry.getArguments()) {
            if (!scriptEntry.hasObject("name"))
                scriptEntry.addObject("name", aH.getStringFrom(arg));

            else throw new InvalidArgumentsException(Messages.ERROR_UNKNOWN_ARGUMENT, arg);
        }

        if (!scriptEntry.hasObject("name"))
            throw new InvalidArgumentsException("Must specify a name!");

        if (scriptEntry.getNPC() == null)
            throw new InvalidArgumentsException("Must have a NPC attached!");
    }
    
	@Override
    public void execute(final ScriptEntry scriptEntry) throws CommandExecutionException {

        String name = (String) scriptEntry.getObject("name");

        dB.report(getName(), aH.debugObj("Name", name));

        NPC npc = scriptEntry.getNPC().getCitizen();

        Location prev = npc.isSpawned() ? npc.getBukkitEntity().getLocation() : null;
        npc.despawn(DespawnReason.PENDING_RESPAWN);
        npc.setName(name);
        if (prev != null)
            npc.spawn(prev);

    }

}