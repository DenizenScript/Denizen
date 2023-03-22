package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.inventory.meta.BlockStateMeta;

public class ItemSpawnerMaxNearbyEntities implements Property {

    public static boolean describes(ObjectTag item) {
        return item instanceof ItemTag
                && ((ItemTag) item).getItemMeta() instanceof BlockStateMeta
                && ((BlockStateMeta) ((ItemTag) item).getItemMeta()).getBlockState() instanceof CreatureSpawner;
    }

    public static ItemSpawnerMaxNearbyEntities getFrom(ObjectTag _item) {
        if (!describes(_item)) {
            return null;
        }
        else {
            return new ItemSpawnerMaxNearbyEntities((ItemTag) _item);
        }
    }

    public static final String[] handledTags = new String[] {
            "spawner_max_nearby_entities"
    };

    public static final String[] handledMechs = new String[] {
            "spawner_max_nearby_entities"
    };

    public ItemSpawnerMaxNearbyEntities(ItemTag _item) {
        item = _item;
    }

    ItemTag item;

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <ItemTag.spawner_max_nearby_entities>
        // @returns ElementTag(Number)
        // @mechanism ItemTag.spawner_max_nearby_entities
        // @group properties
        // @description
        // Returns the maximum nearby entities for a spawner block item.
        // -->
        if (attribute.startsWith("spawner_max_nearby_entities")) {
            BlockStateMeta meta = (BlockStateMeta) item.getItemMeta();
            CreatureSpawner state = (CreatureSpawner) meta.getBlockState();
            return new ElementTag(state.getMaxNearbyEntities())
                    .getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public String getPropertyString() {
        BlockStateMeta meta = (BlockStateMeta) item.getItemMeta();
        CreatureSpawner state = (CreatureSpawner) meta.getBlockState();
        return String.valueOf(state.getMaxNearbyEntities());
    }

    @Override
    public String getPropertyId() {
        return "spawner_max_nearby_entities";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object ItemTag
        // @name spawner_max_nearby_entities
        // @input ElementTag(Number)
        // @description
        // Sets the maximum nearby entities of a spawner block item.
        // @tags
        // <ItemTag.spawner_max_nearby_entities>
        // -->
        if (mechanism.matches("spawner_max_nearby_entities") && mechanism.requireInteger()) {
            BlockStateMeta meta = (BlockStateMeta) item.getItemMeta();
            CreatureSpawner state = (CreatureSpawner) meta.getBlockState();
            state.setMaxNearbyEntities(mechanism.getValue().asInt());
            meta.setBlockState(state);
            item.setItemMeta(meta);
        }
    }
}
