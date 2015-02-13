package net.aufdemrand.denizen.utilities.entity;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public interface DenizenCustomEntity extends Entity {
    @Retention(RetentionPolicy.RUNTIME)
    public @interface CreateEntity {}

    public String getEntityTypeName();
}
