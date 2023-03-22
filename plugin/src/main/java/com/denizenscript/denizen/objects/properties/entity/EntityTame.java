package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Tameable;

public class EntityTame implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag &&
                ((EntityTag) entity).getBukkitEntity() instanceof Tameable;
    }

    public static EntityTame getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityTame((EntityTag) entity);
        }
    }

    public static final String[] handledTags = new String[] {
            "is_tamed", "get_owner", "owner"
    };

    public static final String[] handledMechs = new String[] {
            "tame", "owner"
    };

    public EntityTame(EntityTag tame) {
        entity = tame;
    }

    EntityTag entity;

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

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <EntityTag.is_tamed>
        // @returns ElementTag(Boolean)
        // @mechanism EntityTag.tame
        // @group properties
        // @description
        // Returns whether the entity has been tamed.
        // -->
        if (attribute.startsWith("is_tamed")) {
            return new ElementTag(((Tameable) entity.getBukkitEntity()).isTamed())
                    .getObjectAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <EntityTag.owner>
        // @returns PlayerTag
        // @mechanism EntityTag.owner
        // @group properties
        // @description
        // Returns the owner of a tamed entity.
        // -->
        if (attribute.startsWith("owner") || attribute.startsWith("get_owner")) {
            if (((Tameable) entity.getBukkitEntity()).isTamed()) {
                OfflinePlayer tamer = (OfflinePlayer) ((Tameable) entity.getBukkitEntity()).getOwner();
                if (tamer == null) {
                    return null;
                }
                return new PlayerTag(tamer)
                        .getObjectAttribute(attribute.fulfill(1));
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
        // @object EntityTag
        // @name tame
        // @input ElementTag(Boolean)(|PlayerTag)
        // @description
        // Sets whether the entity has been tamed.
        // Also available: <@link mechanism EntityTag.owner>
        // @tags
        // <EntityTag.is_tamed>
        // <EntityTag.tameable>
        // -->
        if (mechanism.matches("tame")) {
            ListTag list = mechanism.valueAsType(ListTag.class);
            if (list.isEmpty()) {
                mechanism.echoError("Missing value for 'tame' mechanism!");
                return;
            }
            if (new ElementTag(list.get(0)).isBoolean()) {
                ((Tameable) entity.getBukkitEntity()).setTamed(mechanism.getValue().asBoolean());
            }
            else {
                mechanism.echoError("Invalid boolean value!");
            }
            if (list.size() > 1 && new ElementTag(list.get(1)).matchesType(PlayerTag.class)) {
                ((Tameable) entity.getBukkitEntity()).setOwner(new ElementTag(list.get(1)).asType(PlayerTag.class, mechanism.context).getOfflinePlayer());
            }
        }

        // <--[mechanism]
        // @object EntityTag
        // @name owner
        // @input PlayerTag
        // @description
        // Sets the entity's owner. Use with no input to make it not have an owner.
        // Also available: <@link mechanism EntityTag.tame>
        // @tags
        // <EntityTag.is_tamed>
        // <EntityTag.tameable>
        // <EntityTag.owner>
        // -->
        if (mechanism.matches("owner")) {
            if (mechanism.hasValue() && mechanism.requireObject(PlayerTag.class)) {
                ((Tameable) entity.getBukkitEntity()).setOwner(mechanism.valueAsType(PlayerTag.class).getOfflinePlayer());
            }
            else {
                ((Tameable) entity.getBukkitEntity()).setOwner(null);
            }
        }

    }
}
