package net.aufdemrand.denizen.utilities;

import java.util.ArrayList;
import java.util.List;

import net.aufdemrand.denizen.objects.dEntity;

import org.bukkit.entity.Entity;

public class Conversion {

    /**
     * Turn a List of dEntities into a list of Entities.
     * 
     * @param entities The list of dEntities
     */

	public static List<Entity> convert(List<dEntity> oldList) {

		List<Entity> newList = new ArrayList<Entity>();
		
		for (dEntity entity : oldList) {
			
        	newList.add(entity.getBukkitEntity());
        }
		
		return newList;
	}

}
