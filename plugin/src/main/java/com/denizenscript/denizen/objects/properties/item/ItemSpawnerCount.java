package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.inventory.meta.BlockStateMeta;

public class ItemSpawnerCount implements Property {

    public static boolean describes(ObjectTag item) {
        return item instanceof ItemTag
                && ((ItemTag) item).getItemMeta() instanceof BlockStateMeta
                && ((BlockStateMeta) ((ItemTag) item).getItemMeta()).getBlockState() instanceof CreatureSpawner;
    }

    public static ItemSpawnerCount getFrom(ObjectTag _item) {
        if (!describes(_item)) {
            return null;
        }
        else {
            return new ItemSpawnerCount((ItemTag) _item);
        }
    }

    public static final String[] handledTags = new String[] {
            "spawner_count"
    };

    public static final String[] handledMechs = new String[] {
            "spawner_count"
    };

    public ItemSpawnerCount(ItemTag _item) {
        item = _item;
    }

    ItemTag item;

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <ItemTag.spawner_count>
        // @returns ElementTag(Number)
        // @mechanism ItemTag.spawner_count
        // @group properties
        // @description
        // Returns the spawn count for a spawner block item.
        // -->
        if (attribute.startsWith("spawner_count")) {
            BlockStateMeta meta = (BlockStateMeta) item.getItemMeta();
            CreatureSpawner state = (CreatureSpawner) meta.getBlockState();
            return new ElementTag(state.getSpawnCount())
                    .getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public String getPropertyString() {
        BlockStateMeta meta = (BlockStateMeta) item.getItemMeta();
        CreatureSpawner state = (CreatureSpawner) meta.getBlockState();
        return String.valueOf(state.getSpawnCount());
    }

    @Override
    public String getPropertyId() {
        return "spawner_count";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object ItemTag
        // @name spawner_count
        // @input ElementTag(Number)
        // @description
        // Sets the spawn count of a spawner block item.
        // @tags
        // <ItemTag.spawner_count>
        // -->
        if (mechanism.matches("spawner_count") && mechanism.requireInteger()) {
            BlockStateMeta meta = (BlockStateMeta) item.getItemMeta();
            CreatureSpawner state = (CreatureSpawner) meta.getBlockState();
            state.setSpawnCount(mechanism.getValue().asInt());
            meta.setBlockState(state);
            item.setItemMeta(meta);
        }
    }
}
