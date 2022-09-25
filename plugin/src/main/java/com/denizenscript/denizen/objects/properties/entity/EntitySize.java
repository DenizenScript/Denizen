package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.PufferFish;
import org.bukkit.entity.Slime;

public class EntitySize implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag &&
                (((EntityTag) entity).getBukkitEntity() instanceof Slime
                || ((EntityTag) entity).getBukkitEntity() instanceof Phantom
                || ((EntityTag) entity).getBukkitEntity() instanceof PufferFish);
    }

    public static EntitySize getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntitySize((EntityTag) entity);
        }
    }

    private EntitySize(EntityTag ent) {
        entity = ent;
    }

    EntityTag entity;

    public int getSize() {
        if (isSlime()) {
            return getSlime().getSize();
        }
        else if (isPhantom()) {
            return getPhantom().getSize();
        }
        else {
            return getPufferFish().getPuffState();
        }
    }

    @Override
    public String getPropertyString() {
        return String.valueOf(getSize());
    }

    @Override
    public String getPropertyId() {
        return "size";
    }

    public static void registerTags() {

        // <--[tag]
        // @attribute <EntityTag.size>
        // @returns ElementTag(Number)
        // @mechanism EntityTag.size
        // @group properties
        // @description
        // Returns the size of a slime-type entity or a Phantom (1-120).
        // If the entity is a PufferFish it returns the puff state (0-3).
        // -->
        PropertyParser.registerTag(EntitySize.class, ElementTag.class, "size", (attribute, object) -> {
            return new ElementTag(object.getSize());
        });


        // <--[mechanism]
        // @object EntityTag
        // @name size
        // @input ElementTag(Number)
        // @description
        // Sets the size of a slime-type entity or a Phantom (1-120).
        // If the entity is a PufferFish it sets the puff state (0-3).
        // @tags
        // <EntityTag.size>
        // -->
        PropertyParser.registerMechanism(EntitySize.class, ElementTag.class, "size", (object, mechanism, input) -> {
            if (mechanism.requireInteger()) {
                if (object.isSlime()) {
                    object.getSlime().setSize(input.asInt());
                }
                else if (object.isPhantom()) {
                    object.getPhantom().setSize(input.asInt());
                }
                else {
                    object.getPufferFish().setPuffState(input.asInt());
                }
            }
        });
    }

    public boolean isSlime() {
        return entity.getBukkitEntity() instanceof Slime;
    }

    public boolean isPhantom() {
        return entity.getBukkitEntity() instanceof Phantom;
    }

    public Slime getSlime() {
        return (Slime) entity.getBukkitEntity();
    }

    public Phantom getPhantom() {
        return (Phantom) entity.getBukkitEntity();
    }

    public PufferFish getPufferFish() {
        return (PufferFish) entity.getBukkitEntity();
    }
}
