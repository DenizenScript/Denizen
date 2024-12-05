package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Vindicator;
import org.bukkit.entity.Wolf;

public class EntityAngry extends EntityProperty<ElementTag> {

    // <--[property]
    // @object EntityTag
    // @name angry
    // @input ElementTag(Boolean)
    // @description
    // Controls whether an entity is angry.
    // -->

    public static boolean describes(EntityTag entity) {
        return entity.getBukkitEntity() instanceof Wolf
                || entity.getBukkitEntity() instanceof PigZombie
                || (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_18) && entity.getBukkitEntity() instanceof Vindicator);
    }

    EntityTag entity;

    @Override
    public ElementTag getPropertyValue() {
        if (isWolf()) {
            return new ElementTag(getWolf().isAngry());
        }
        else if (isPigZombie()) {
            return new ElementTag(getPigZombie().isAngry());
        }
        else if (isVindicator()) {
            return new ElementTag(getVindicator().isJohnny());
        }
        return null;
    }

    @Override
    public void setPropertyValue(ElementTag param, Mechanism mechanism) {
        if (mechanism.requireBoolean()) {
            if (isWolf()) {
                getWolf().setAngry(param.asBoolean());
            }
            else if (isPigZombie()) {
                getPigZombie().setAngry(param.asBoolean());
            }
            else if (isVindicator()) {
                getVindicator().setJohnny(param.asBoolean());
            }
        }
    }

    @Override
    public String getPropertyId() {
        return "angry";
    }

    public static void register() {
        autoRegister("angry", EntityAngry.class, ElementTag.class, false);
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
