package com.denizenscript.denizen.paper.properties;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.properties.entity.EntityProperty;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Zombie;

public class EntityShouldBurn extends EntityProperty<ElementTag> {

    // <--[property]
    // @object EntityTag
    // @name should_burn
    // @input ElementTag(Boolean)
    // @plugin Paper
    // @description
    // If the entity is a Zombie, Skeleton, or Phantom, controls whether it should burn in daylight.
    // -->

    public static boolean describes(EntityTag entity) {
        return entity.getBukkitEntity() instanceof Zombie
                || entity.getBukkitEntity() instanceof Skeleton
                || entity.getBukkitEntity() instanceof Phantom;
    }

    @Override
    public ElementTag getPropertyValue() {
        if (getEntity() instanceof Zombie zombie) {
            return new ElementTag(zombie.shouldBurnInDay());
        }
        else if (getEntity() instanceof Skeleton skeleton) {
            return new ElementTag(skeleton.shouldBurnInDay());
        }
        else { // phantom
            return new ElementTag(as(Phantom.class).shouldBurnInDay());
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
            else if (getEntity() instanceof Skeleton skeleton) {
                skeleton.setShouldBurnInDay(param.asBoolean());
            }
            else { // phantom
                as(Phantom.class).setShouldBurnInDay(param.asBoolean());
            }
        }
    }

    public static void register() {
        autoRegister("should_burn", EntityShouldBurn.class, ElementTag.class, false);
    }
}
