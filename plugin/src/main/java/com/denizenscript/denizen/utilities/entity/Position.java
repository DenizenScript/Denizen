package com.denizenscript.denizen.utilities.entity;

import org.bukkit.entity.Entity;

import java.util.List;

public class Position {

    public static void mount(List<Entity> entities) {
        Entity lastEntity = null;
        for (Entity entity : entities) {
            if (entity != null) {
                if (lastEntity != null && entity != lastEntity) {
                    if (!entity.getPassengers().contains(lastEntity)) {
                        lastEntity.teleport(entity.getLocation());
                        entity.addPassenger(lastEntity);
                    }
                }
                lastEntity = entity;
            }
        }
    }

    public static void dismount(List<Entity> entities) {
        for (Entity entity : entities) {
            if (entity != null) {
                entity.leaveVehicle();
            }
        }
    }
}
