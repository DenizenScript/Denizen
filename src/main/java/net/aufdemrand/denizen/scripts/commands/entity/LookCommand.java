package net.aufdemrand.denizen.scripts.commands.entity;

import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.objects.dNPC;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.trait.Trait;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.entity.Rotation;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;
import net.citizensnpcs.trait.LookClose;

/**
 * Controls Denizens' heads.
 * 
 * @author Jeremy Schroeder
 *
 */

public class LookCommand extends AbstractCommand {

    // look (e@entity) [l@location]    // Note: not specifying entity will default to attached NPC
	@Override
	public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("location")
                    && arg.matchesArgumentType(dLocation.class))
                scriptEntry.addObject("location", arg.asType(dLocation.class));

            if (!scriptEntry.hasObject("entity")
                    && arg.matchesArgumentType(dEntity.class))
                    scriptEntry.addObject("entity", arg.asType(dEntity.class));

        }

        if (!scriptEntry.hasObject("entity")
                && scriptEntry.hasNPC()
                && scriptEntry.getNPC().isSpawned())
            scriptEntry.addObject("entity", new dEntity(scriptEntry.getNPC().getEntity()));

        if (!scriptEntry.hasObject("location") || !scriptEntry.hasObject("entity"))
            throw new InvalidArgumentsException("Must specify a location and spawned entity!");
	}

	@Override
	public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {
		
        dLocation loc = (dLocation) scriptEntry.getObject("location");
        dEntity ent = (dEntity) scriptEntry.getObject("entity");

        dB.report(getName(), loc.debug() + ent.debug());

    	Rotation.faceLocation(ent.getLivingEntity(), loc);

	}


	
	
	/*

		else if (lookWhere.equals("LEFT")) {
			theDenizen.lookClose(false);						
			theDenizen.getHandle().yaw = theDenizen.getLocation().getYaw() - (float) 80;
			theDenizen.getHandle().az = theDenizen.getHandle().yaw;
		}

		else if (lookWhere.equals("RIGHT")) {
			theDenizen.lookClose(false);
			theDenizen.getHandle().yaw = theDenizen.getLocation().getYaw() + (float) 80;
			theDenizen.getHandle().az = theDenizen.getHandle().yaw;
		}

		else if (lookWhere.equals("UP")) {
			theDenizen.lookClose(false);
			theDenizen.getHandle().pitch = theDenizen.getHandle().pitch - (float) 60;
			theDenizen.getHandle().az = theDenizen.getHandle().yaw;
		}

		else if (lookWhere.equals("DOWN")) {
			theDenizen.lookClose(false);
			theDenizen.getHandle().pitch = theDenizen.getHandle().pitch + (float) 40;
			theDenizen.getHandle().az = theDenizen.getHandle().yaw;
		}

		else if (lookWhere.equals("BACK")) {
			theDenizen.lookClose(false);
			theDenizen.getHandle().yaw = theDenizen.getLocation().getYaw() - 180;
			theDenizen.getHandle().az = theDenizen.getHandle().yaw;			
		}

		else if (lookWhere.equals("SOUTH")) {
			theDenizen.lookClose(false);
			theDenizen.getHandle().yaw = 0;
			theDenizen.getHandle().az = theDenizen.getHandle().yaw;			
		}

		else if (lookWhere.equals("WEST")) {
			theDenizen.lookClose(false);
			theDenizen.getHandle().yaw = 90;
			theDenizen.getHandle().az = theDenizen.getHandle().yaw;			
		}

		else if (lookWhere.equals("NORTH")) {
			theDenizen.lookClose(false);
			theDenizen.getHandle().yaw = 180;
			theDenizen.getHandle().az = theDenizen.getHandle().yaw;			
		}

		else if (lookWhere.equals("EAST")) {
			theDenizen.lookClose(false);
			theDenizen.getHandle().yaw = 270;
			theDenizen.getHandle().az = theDenizen.getHandle().yaw;			
		}

*/
}

