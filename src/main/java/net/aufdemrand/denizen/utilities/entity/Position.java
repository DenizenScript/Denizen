package net.aufdemrand.denizen.utilities.entity;

import java.util.List;

import org.bukkit.entity.Entity;

public class Position {

    /**
     * Mounts a list of entities on top of each other.
     * 
     * @param entities The list of entities
     */

	public static void mount(List<Entity> entities) {

		Entity lastEntity = null;
		
		for (Entity entity : entities) {
			
        	if (lastEntity != null) {
        	// Because setPassenger() is a toggle, only use it if the new passenger
        	// is not already the current passenger
	        		
        		if (entity.getPassenger() != lastEntity) {
        			lastEntity.teleport(entity.getLocation());
        			entity.setPassenger(lastEntity);
        		}
        	}
	        	
        	lastEntity = entity;
        }
	}
	
    /**
     * Dismounts a list of entities.
     * 
     * @param entities The list of entities
     */
	public static void dismount(List<Entity> entities) {
		
		for (Entity entity : entities) {
		
			entity.leaveVehicle();
		}
	}
}
