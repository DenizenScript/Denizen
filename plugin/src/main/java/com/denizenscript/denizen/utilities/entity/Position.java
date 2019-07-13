package com.denizenscript.denizen.utilities.entity;

import org.bukkit.entity.Entity;

import java.util.List;

public class Position {

    /**
     * Mounts a list of entities on top of each other.
     *
     * @param entities The list of entities
     */

    public static void mount(List<Entity> entities) {

        Entity lastEntity = null;

        for (Entity entity : entities) {

            if (entity != null) {

                if (lastEntity != null && entity != lastEntity) {

                    // Because setPassenger() is a toggle, only use it if the new passenger
                    // is not already the current passenger, and also make sure we're not
                    // mounting the entity on itself

                    if (entity.getPassenger() != lastEntity) {
                        lastEntity.teleport(entity.getLocation());
                        entity.setPassenger(lastEntity);
                    }
                }

                lastEntity = entity;
            }
        }
    }

    /**
     * Dismounts a list of entities.
     *
     * @param entities The list of entities
     */
    public static void dismount(List<Entity> entities) {

        for (Entity entity : entities) {

            if (entity != null) {
                entity.leaveVehicle();
            }
        }
    }
}
