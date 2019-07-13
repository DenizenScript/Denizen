package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.dEntity;
import com.denizenscript.denizen.objects.dPlayer;
import com.denizenscript.denizencore.objects.Element;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.dList;
import com.denizenscript.denizencore.objects.dObject;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Tameable;

public class EntityTame implements Property {


    public static boolean describes(dObject entity) {
        return entity instanceof dEntity &&
                ((dEntity) entity).getBukkitEntity() instanceof Tameable;
    }

    public static EntityTame getFrom(dObject entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityTame((dEntity) entity);
        }
    }

    public static final String[] handledTags = new String[] {
            "is_tamed", "get_owner", "owner"
    };

    public static final String[] handledMechs = new String[] {
            "tame", "owner"
    };


    ///////////////////
    // Instance Fields and Methods
    /////////////

    private EntityTame(dEntity tame) {
        entity = tame;
    }

    dEntity entity;

    /////////
    // Property Methods
    ///////

    @Override
    public String getPropertyString() {
        if (((Tameable) entity.getBukkitEntity()).isTamed()) {
            OfflinePlayer owner = (OfflinePlayer) ((Tameable) entity.getBukkitEntity()).getOwner();
            if (owner == null) {
                return "true";
            }
            else {
                return "true|" + owner.getUniqueId();
            }
        }
        else {
            return null;
        }
    }

    @Override
    public String getPropertyId() {
        return "tame";
    }


    ///////////
    // dObject Attributes
    ////////

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <e@entity.is_tamed>
        // @returns Element(Boolean)
        // @mechanism dEntity.tame
        // @group properties
        // @description
        // Returns whether the entity has been tamed.
        // -->
        if (attribute.startsWith("is_tamed")) {
            return new Element(((Tameable) entity.getBukkitEntity()).isTamed())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <e@entity.owner>
        // @returns dPlayer
        // @mechanism dEntity.owner
        // @group properties
        // @description
        // Returns the owner of a tamed entity.
        // -->
        if (attribute.startsWith("owner") || attribute.startsWith("get_owner")) {
            if (((Tameable) entity.getBukkitEntity()).isTamed()) {
                return new dPlayer((OfflinePlayer) ((Tameable) entity.getBukkitEntity()).getOwner())
                        .getAttribute(attribute.fulfill(1));
            }
            else {
                return null;
            }
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dEntity
        // @name tame
        // @input Element(Boolean)(|dPlayer)
        // @description
        // Sets whether the entity has been tamed.
        // Also available: <@link mechanism dEntity.owner>
        // @tags
        // <e@entity.is_tamed>
        // <e@entity.is_tameable>
        // -->

        if (mechanism.matches("tame")) {
            dList list = mechanism.valueAsType(dList.class);
            if (list.size() == 0) {
                Debug.echoError("Missing value for 'tame' mechanism!");
                return;
            }
            if (new Element(list.get(0)).isBoolean()) {
                ((Tameable) entity.getBukkitEntity()).setTamed(mechanism.getValue().asBoolean());
            }
            else {
                Debug.echoError("Invalid boolean value!");
            }
            if (list.size() > 1 && new Element(list.get(1)).matchesType(dPlayer.class)) {
                ((Tameable) entity.getBukkitEntity()).setOwner(new Element(list.get(1)).asType(dPlayer.class, mechanism.context).getOfflinePlayer());
            }
        }

        // <--[mechanism]
        // @object dEntity
        // @name owner
        // @input dPlayer
        // @description
        // Sets the entity's owner. Use with no input to make it not have an owner.
        // Also available: <@link mechanism dEntity.tame>
        // @tags
        // <e@entity.is_tamed>
        // <e@entity.is_tameable>
        // <e@entity.owner>
        // -->

        if (mechanism.matches("owner")) {
            if (mechanism.hasValue() && mechanism.requireObject(dPlayer.class)) {
                ((Tameable) entity.getBukkitEntity()).setOwner(mechanism.valueAsType(dPlayer.class).getOfflinePlayer());
            }
            else {
                ((Tameable) entity.getBukkitEntity()).setOwner(null);
            }
        }

    }
}

