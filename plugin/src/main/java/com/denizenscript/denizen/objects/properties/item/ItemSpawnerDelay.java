package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.inventory.meta.BlockStateMeta;

public class ItemSpawnerDelay implements Property {

    public static boolean describes(ObjectTag item) {
        return item instanceof ItemTag
                && ((ItemTag) item).getItemMeta() instanceof BlockStateMeta
                && ((BlockStateMeta) ((ItemTag) item).getItemMeta()).getBlockState() instanceof CreatureSpawner;
    }

    public static ItemSpawnerDelay getFrom(ObjectTag _item) {
        if (!describes(_item)) {
            return null;
        }
        else {
            return new ItemSpawnerDelay((ItemTag) _item);
        }
    }

    public static final String[] handledTags = new String[] {
            "spawner_spawn_delay", "spawner_minimum_spawn_delay", "spawner_maximum_spawn_delay"
    };

    public static final String[] handledMechs = new String[] {
            "spawner_delay_data"
    };

    public ItemSpawnerDelay(ItemTag _item) {
        item = _item;
    }

    ItemTag item;

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <ItemTag.spawner_spawn_delay>
        // @returns ElementTag(Number)
        // @mechanism ItemTag.spawner_delay_data
        // @group properties
        // @description
        // Returns the current spawn delay for a spawner block item.
        // This changes over time between <@link tag ItemTag.spawner_minimum_spawn_delay> and <@link tag ItemTag.spawner_maximum_spawn_delay>.
        // -->
        if (attribute.startsWith("spawner_spawn_delay")) {
            BlockStateMeta meta = (BlockStateMeta) item.getItemMeta();
            CreatureSpawner state = (CreatureSpawner) meta.getBlockState();
            return new ElementTag(state.getDelay())
                    .getObjectAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <ItemTag.spawner_minimum_spawn_delay>
        // @returns ElementTag(Number)
        // @mechanism ItemTag.spawner_delay_data
        // @group properties
        // @description
        // Returns the minimum spawn delay for a spawner block item.
        // -->
        if (attribute.startsWith("spawner_minimum_spawn_delay")) {
            BlockStateMeta meta = (BlockStateMeta) item.getItemMeta();
            CreatureSpawner state = (CreatureSpawner) meta.getBlockState();
            return new ElementTag(state.getMinSpawnDelay())
                    .getObjectAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <ItemTag.spawner_maximum_spawn_delay>
        // @returns ElementTag(Number)
        // @mechanism ItemTag.spawner_delay_data
        // @group properties
        // @description
        // Returns the maximum spawn delay for a spawner block item.
        // -->
        if (attribute.startsWith("spawner_maximum_spawn_delay")) {
            BlockStateMeta meta = (BlockStateMeta) item.getItemMeta();
            CreatureSpawner state = (CreatureSpawner) meta.getBlockState();
            return new ElementTag(state.getMaxSpawnDelay())
                    .getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public String getPropertyString() {
        BlockStateMeta meta = (BlockStateMeta) item.getItemMeta();
        CreatureSpawner state = (CreatureSpawner) meta.getBlockState();
        return state.getDelay() + "|" + state.getMinSpawnDelay() + "|" + state.getMaxSpawnDelay();
    }

    @Override
    public String getPropertyId() {
        return "spawner_delay_data";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object ItemTag
        // @name spawner_delay_data
        // @input ListTag
        // @description
        // Sets the current spawn delay, minimum spawn delay, and maximum spawn delay of a mob spawner block item.
        // For example, -1|200|800
        // @tags
        // <ItemTag.spawner_spawn_delay>
        // <ItemTag.spawner_minimum_spawn_delay>
        // <ItemTag.spawner_maximum_spawn_delay>
        // -->
        if (mechanism.matches("spawner_delay_data")) {
            ListTag list = mechanism.valueAsType(ListTag.class);
            if (list.size() < 3) {
                return;
            }
            BlockStateMeta meta = (BlockStateMeta) item.getItemMeta();
            CreatureSpawner state = (CreatureSpawner) meta.getBlockState();
            state.setDelay(Integer.parseInt(list.get(0)));
            int minDelay = Integer.parseInt(list.get(1));
            int maxDelay = Integer.parseInt(list.get(2));
            // Minecraft won't set the limits if the new max would be lower than the current min
            // or new min would be higher than the current max
            if (minDelay > state.getMaxSpawnDelay()) {
                state.setMaxSpawnDelay(maxDelay);
                state.setMinSpawnDelay(minDelay);
            } else {
                state.setMinSpawnDelay(minDelay);
                state.setMaxSpawnDelay(maxDelay);
            }
            meta.setBlockState(state);
            item.setItemMeta(meta);
        }
    }
}
