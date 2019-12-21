package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.entity.Bee;
import org.bukkit.entity.Entity;
import org.bukkit.entity.PigZombie;

public class EntityAnger implements Property {

    public static boolean describes(ObjectTag entity) {
        if (!(entity instanceof EntityTag)) {
            return false;
        }
        Entity bukkitEntity = ((EntityTag) entity).getBukkitEntity();
        return bukkitEntity instanceof PigZombie ||
                (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_15) && bukkitEntity instanceof Bee);
    }

    public static EntityAnger getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityAnger((EntityTag) entity);
        }
    }

    public static final String[] handledTags = new String[] {
            "anger"
    };

    public static final String[] handledMechs = new String[] {
            "anger"
    };


    ///////////////////
    // Instance Fields and Methods
    /////////////

    private EntityAnger(EntityTag entity) {
        this.entity = entity;
    }

    EntityTag entity;

    /////////
    // Property Methods
    ///////

    @Override
    public String getPropertyString() {
        return String.valueOf(getAnger());
    }

    @Override
    public String getPropertyId() {
        return "anger";
    }

    ///////////
    // ObjectTag Attributes
    ////////

    public int getAnger() {
        if (entity.getBukkitEntity() instanceof PigZombie) {
            return ((PigZombie) entity.getBukkitEntity()).getAnger();
        }
        else {
            return ((Bee) entity.getBukkitEntity()).getAnger();
        }
    }

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <EntityTag.anger>
        // @returns ElementTag(Number)
        // @mechanism EntityTag.anger
        // @group properties
        // @description
        // Returns the anger level of a PigZombie or Bee.
        // -->
        if (attribute.startsWith("anger")) {
            return new ElementTag(getAnger())
                    .getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name anger
        // @input ElementTag(Boolean)
        // @description
        // Changes the anger level of a PigZombie or Bee.
        // @tags
        // <EntityTag.anger>
        // -->

        if (mechanism.matches("anger") && mechanism.requireInteger()) {
            if (entity.getBukkitEntity() instanceof PigZombie) {
                ((PigZombie) entity.getBukkitEntity()).setAnger(mechanism.getValue().asInt());
            }
            else {
                ((Bee) entity.getBukkitEntity()).setAnger(mechanism.getValue().asInt());
            }
        }
    }
}
