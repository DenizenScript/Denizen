package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Vindicator;
import org.bukkit.entity.Wolf;

public class EntityAngry implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag && (((EntityTag) entity).getBukkitEntity() instanceof Wolf
                || ((EntityTag) entity).getBukkitEntity() instanceof PigZombie
                || (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_17) && ((EntityTag) entity).getBukkitEntity() instanceof Vindicator));
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

    private EntityAngry(EntityTag entity) {
        this.entity = entity;
    }

    EntityTag entity;

    @Override
    public String getPropertyString() {
        if (entity.getBukkitEntity() instanceof Wolf) {
            if (!((Wolf) entity.getLivingEntity()).isAngry()) {
                return null;
            }
            else {
                return "true";
            }
        }
        else if (entity.getBukkitEntity() instanceof PigZombie) {
            if (!((PigZombie) entity.getLivingEntity()).isAngry()) {
                return null;
            }
            else {
                return "true";
            }
        }
        return null;
    }

    @Override
    public String getPropertyId() {
        return "angry";
    }

    /*@Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <EntityTag.angry>
        // @returns ElementTag(Boolean)
        // @mechanism EntityTag.angry
        // @group properties
        // @description
        // If the entity is a wolf or PigZombie, returns whether the entity is angry.
        // -->
        if (attribute.startsWith("angry")) {
            if (entity.getBukkitEntity() instanceof Wolf) {
                return new ElementTag(((Wolf) entity.getBukkitEntity()).isAngry())
                        .getObjectAttribute(attribute.fulfill(1));
            }
            else if (entity.getBukkitEntity() instanceof PigZombie) {
                return new ElementTag(((PigZombie) entity.getBukkitEntity()).isAngry())
                        .getObjectAttribute(attribute.fulfill(1));
            }
        }

        return null;
    }*/

    public static void registerTags() {

        // <--[tag]
        // @attribute <EntityTag.angry>
        // @returns ElementTag(Boolean)
        // @mechanism EntityTag.angry
        // @group properties
        // @description
        // If the entity is a wolf or PigZombie, returns whether the entity is angry.
        // If the entity is a Vindicator, returns whether it is in "Johnny" mode.
        // -->
        PropertyParser.<EntityAngry, ElementTag>registerTag(ElementTag.class, "angry", (attribute, entity) -> {
            if (entity.isWolf()) {
                return new ElementTag(entity.getWolf().isAngry());
            }
            else if (entity.isPigZombie()) {
                return new ElementTag(entity.getPigZombie().isAngry());
            }
            else if (entity.isVindicator()) {
                // idk
                return new ElementTag(false);
            }
            return null;
        });
    }

    public boolean isWolf() {
        return entity.getBukkitEntity() instanceof Wolf;
    }

    public boolean isPigZombie() {
        return entity.getBukkitEntity() instanceof PigZombie;
    }

    public boolean isVindicator() {
        return entity.getBukkitEntity() instanceof Vindicator;
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

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name angry
        // @input ElementTag(Boolean)
        // @description
        // Changes the anger state of a Wolf or PigZombie.
        // @tags
        // <EntityTag.angry>
        // -->
        if (mechanism.matches("angry") && mechanism.requireBoolean()) {
            if (isWolf()) {
                getWolf().setAngry(mechanism.getValue().asBoolean());
            }
            else if (isPigZombie()) {
                getPigZombie().setAngry(mechanism.getValue().asBoolean());
            }
            else if (isVindicator()) {

            }
        }
    }
}
