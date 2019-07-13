package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.meta.SpawnEggMeta;

public class ItemSpawnEgg implements Property {

    public static boolean describes(ObjectTag item) {
        return item instanceof ItemTag
                && ((ItemTag) item).getItemStack().getItemMeta() instanceof SpawnEggMeta;
    }

    public static ItemSpawnEgg getFrom(ObjectTag _item) {
        if (!describes(_item)) {
            return null;
        }
        else {
            return new ItemSpawnEgg((ItemTag) _item);
        }
    }

    public static final String[] handledTags = new String[] {
            "spawn_id", "spawn_egg_entity", "spawn_type"
    };

    public static final String[] handledMechs = new String[] {
            "spawn_id", "spawn_egg", "spawn_type"
    };


    private ItemSpawnEgg(ItemTag _item) {
        item = _item;
    }

    ItemTag item;

    private EntityType getEntityType() {
        return ((SpawnEggMeta) item.getItemStack().getItemMeta()).getSpawnedType();
    }

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <ItemTag.spawn_id>
        // @returns ElementTag(Number)
        // @group properties
        // @mechanism ItemTag.spawn_id
        // @description
        // NOTE: ID numbers are deprecated since 1.11
        //  Use <ItemTag.spawn_type> instead!
        // Returns the spawn egg number of the item.
        // -->
        if ((attribute.startsWith("spawn_id") || attribute.startsWith("spawn_egg_entity"))
                && getEntityType() != null) {
            return new ElementTag(getEntityType().getTypeId())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <ItemTag.spawn_type>
        // @returns ElementTag
        // @group properties
        // @mechanism ItemTag.spawn_type
        // @description
        // Returns the spawn egg's entity type.
        // -->
        if (attribute.startsWith("spawn_type") && getEntityType() != null) {
            return new ElementTag(getEntityType().name())
                    .getAttribute(attribute.fulfill(1));
        }

        return null;
    }


    @Override
    public String getPropertyString() {
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13_R2)) {
            return null; // handled by the Material itself in 1.13
        }
        return getEntityType() != null ? getEntityType().name() : null;
    }

    @Override
    public String getPropertyId() {
        return "spawn_type";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object ItemTag
        // @name spawn_id
        // @input Element(Number)
        // @description
        // NOTE: ID numbers are deprecated since 1.11
        //  Use <@link mechanism ItemTag.spawn_type> instead!
        // Sets what mob a spawn egg holds.
        // @tags
        // <ItemTag.spawn_id>
        // -->
        if ((mechanism.matches("spawn_id") || mechanism.matches("spawn_egg"))
                && mechanism.requireInteger()) {
            SpawnEggMeta sem = (SpawnEggMeta) item.getItemStack().getItemMeta();
            sem.setSpawnedType(EntityType.fromId(mechanism.getValue().asInt()));
            item.getItemStack().setItemMeta(sem);
        }

        // <--[mechanism]
        // @object ItemTag
        // @name spawn_type
        // @input Element
        // @description
        // Sets what entity type a spawn egg holds.
        // @tags
        // <ItemTag.spawn_type>
        // -->
        if (mechanism.matches("spawn_type") && mechanism.requireEnum(false, EntityType.values())) {
            try {
                SpawnEggMeta sem = (SpawnEggMeta) item.getItemStack().getItemMeta();
                sem.setSpawnedType(EntityType.valueOf(mechanism.getValue().asString().toUpperCase()));
                item.getItemStack().setItemMeta(sem);
            }
            catch (IllegalArgumentException e) {
                Debug.echoError(mechanism.getValue().asString().toUpperCase() + " is not a valid spawn egg entity!");
            }
        }
    }
}
