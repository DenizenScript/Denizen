package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.inventory.meta.BlockStateMeta;

public class ItemSpawnerRange implements Property {

    public static boolean describes(ObjectTag item) {
        return item instanceof ItemTag
                && ((ItemTag) item).getItemMeta() instanceof BlockStateMeta
                && ((BlockStateMeta) ((ItemTag) item).getItemMeta()).getBlockState() instanceof CreatureSpawner;
    }

    public static ItemSpawnerRange getFrom(ObjectTag _item) {
        if (!describes(_item)) {
            return null;
        }
        else {
            return new ItemSpawnerRange((ItemTag) _item);
        }
    }

    public static final String[] handledTags = new String[] {
            "spawner_range"
    };

    public static final String[] handledMechs = new String[] {
            "spawner_range"
    };

    public ItemSpawnerRange(ItemTag _item) {
        item = _item;
    }

    ItemTag item;

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <ItemTag.spawner_range>
        // @returns ElementTag(Number)
        // @mechanism ItemTag.spawner_range
        // @group properties
        // @description
        // Returns the spawn range for a spawner block item (the radius mobs will spawn in).
        // -->
        if (attribute.startsWith("spawner_range")) {
            BlockStateMeta meta = (BlockStateMeta) item.getItemMeta();
            CreatureSpawner state = (CreatureSpawner) meta.getBlockState();
            return new ElementTag(state.getSpawnRange())
                    .getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public String getPropertyString() {
        BlockStateMeta meta = (BlockStateMeta) item.getItemMeta();
        CreatureSpawner state = (CreatureSpawner) meta.getBlockState();
        return String.valueOf(state.getSpawnRange());
    }

    @Override
    public String getPropertyId() {
        return "spawner_range";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object ItemTag
        // @name spawner_range
        // @input ElementTag(Number)
        // @description
        // Sets the spawn range of a spawner block item (the radius mobs will spawn in).
        // @tags
        // <ItemTag.spawner_range>
        // -->
        if (mechanism.matches("spawner_range") && mechanism.requireInteger()) {
            BlockStateMeta meta = (BlockStateMeta) item.getItemMeta();
            CreatureSpawner state = (CreatureSpawner) meta.getBlockState();
            state.setSpawnRange(mechanism.getValue().asInt());
            meta.setBlockState(state);
            item.setItemMeta(meta);
        }
    }
}
