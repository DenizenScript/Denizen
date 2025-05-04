package com.denizenscript.denizen.paper.properties;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.properties.entity.EntityProperty;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.entity.*;

public class EntityShouldBurn extends EntityProperty<ElementTag> {

    // <--[property]
    // @object EntityTag
    // @name should_burn
    // @input ElementTag(Boolean)
    // @plugin Paper
    // @description
    // If the entity is a Zombie, Skeleton, Stray, Bogged, or Phantom, controls whether it should burn in daylight.
    // -->

    public static boolean describes(EntityTag entity) {
        return entity.getBukkitEntity() instanceof Zombie
                || entity.getBukkitEntity() instanceof Phantom
                || entity.getBukkitEntity() instanceof Skeleton
                || entity.getBukkitEntity() instanceof Stray
                || (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_20) && entity.getBukkitEntity() instanceof Bogged);
    }

    @Override
    public ElementTag getPropertyValue() {
        if (getEntity() instanceof Zombie zombie) {
            return new ElementTag(zombie.shouldBurnInDay());
        }
        else if (getEntity() instanceof Phantom phantom) {
            return new ElementTag(phantom.shouldBurnInDay());
        }
        else if (getEntity() instanceof Skeleton skeleton) {
            return new ElementTag(skeleton.shouldBurnInDay());
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_20) && getEntity() instanceof Bogged bogged) {
            return new ElementTag(bogged.shouldBurnInDay());
        }
        else { // stray
            return new ElementTag(as(Stray.class).shouldBurnInDay());
        }
    }

    @Override
    public String getPropertyId() {
        return "should_burn";
    }

    @Override
    public void setPropertyValue(ElementTag param, Mechanism mechanism) {
        if (mechanism.requireBoolean()) {
            if (getEntity() instanceof Zombie zombie) {
                zombie.setShouldBurnInDay(param.asBoolean());
            }
            else if (getEntity() instanceof Phantom phantom) {
                phantom.setShouldBurnInDay(param.asBoolean());
            }
            else if (getEntity() instanceof Skeleton skeleton) {
                skeleton.setShouldBurnInDay(param.asBoolean());
            }
            else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_20) && getEntity() instanceof Bogged bogged) {
                bogged.setShouldBurnInDay(param.asBoolean());
            }
            else { // stray
                as(Stray.class).setShouldBurnInDay(param.asBoolean());
            }
        }
    }

    public static void register() {
        autoRegister("should_burn", EntityShouldBurn.class, ElementTag.class, false);
    }
}
