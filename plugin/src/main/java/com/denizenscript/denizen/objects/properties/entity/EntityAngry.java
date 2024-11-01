package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.entity.Entity;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Vindicator;
import org.bukkit.entity.Wolf;

public class EntityAngry implements Property {

    // <--[property]
    // @object EntityTag
    // @name angry
    // @input ElementTag(Boolean)
    // @description
    // If the entity is a Wolf or PigZombie, controls the entity's anger mode.
    // If the entity is a Vindicator, controls it in "Johnny" mode.
    // -->

    public static boolean describes(ObjectTag object) {
        if (!(object instanceof EntityTag)) {
            return false;
        }
        Entity entity = ((EntityTag) object).getBukkitEntity();
        return entity instanceof Wolf
                || entity instanceof PigZombie
                || (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_18) && entity instanceof Vindicator);
    }

    public static EntityAngry getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityAngry((EntityTag) entity);
        }
    }

    public static final String[] handledMechs = new String[] {
            "angry"
    };

    public EntityAngry(EntityTag entity) {
        this.entity = entity;
    }

    EntityTag entity;

    @Override
    public void setPropertyValue(Boolean param, Mechanism mechanism) {
        
        if (isWolf()) {
            getWolf().setAngry(mechanism.getValue().asBoolean());
        }
        else if (isPigZombie()) {
            getPigZombie().setAngry(mechanism.getValue().asBoolean());
        }
        else if (isVindicator()) {
            getVindicator().setJohnny(mechanism.getValue().asBoolean());
        }
    }
    @Override
    public String getPropertyString() {
        if (isWolf()) {
            return getWolf().isAngry() ? "true" : null;
        }
        else if (isPigZombie()) {
            return getPigZombie().isAngry() ? "true" : null;
        }
        else if (isVindicator()) {
            return getVindicator().isJohnny() ? "true" : null;
        }
        return null;
    }

    @Override
    public String getPropertyId() {
        return "angry";
    }

    public static void register() {
        autoRegister("angry", EntityAngry.class, ElementTag.class, false)
    }

    public boolean isWolf() {
        return entity.getBukkitEntity() instanceof Wolf;
    }

    public boolean isPigZombie() {
        return entity.getBukkitEntity() instanceof PigZombie;
    }

    public boolean isVindicator() {
        return NMSHandler.getVersion().isAtLeast(NMSVersion.v1_18) && entity.getBukkitEntity() instanceof Vindicator;
    }

    public Wolf getWolf() {
        return (Wolf) entity.getBukkitEntity();
    }

    public PigZombie getPigZombie() {
        return (PigZombie) entity.getBukkitEntity();
    }

    public Vindicator getVindicator() {
        return (Vindicator) entity.getBukkitEntity();
    }
}
