package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.inventory.meta.BlockStateMeta;

public class ItemSpawnerType implements Property {

    public static boolean describes(ObjectTag item) {
        return item instanceof ItemTag
                && ((ItemTag) item).getItemMeta() instanceof BlockStateMeta
                && ((BlockStateMeta) ((ItemTag) item).getItemMeta()).getBlockState() instanceof CreatureSpawner;
    }

    public static ItemSpawnerType getFrom(ObjectTag _item) {
        if (!describes(_item)) {
            return null;
        }
        else {
            return new ItemSpawnerType((ItemTag) _item);
        }
    }

    public static final String[] handledTags = new String[] {
            "spawner_type"
    };

    public static final String[] handledMechs = new String[] {
            "spawner_type"
    };

    public ItemSpawnerType(ItemTag _item) {
        item = _item;
    }

    ItemTag item;

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <ItemTag.spawner_type>
        // @returns EntityTag
        // @mechanism ItemTag.spawner_type
        // @group properties
        // @description
        // Returns the spawn type for a spawner block item.
        // -->
        if (attribute.startsWith("spawner_type")) {
            BlockStateMeta meta = (BlockStateMeta) item.getItemMeta();
            CreatureSpawner state = (CreatureSpawner) meta.getBlockState();
            return new EntityTag(state.getSpawnedType())
                    .getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public String getPropertyString() {
        BlockStateMeta meta = (BlockStateMeta) item.getItemMeta();
        CreatureSpawner state = (CreatureSpawner) meta.getBlockState();
        return state.getSpawnedType().name();
    }

    @Override
    public String getPropertyId() {
        return "spawner_type";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object ItemTag
        // @name spawner_type
        // @input EntityTag
        // @description
        // Sets the spawn type of a spawner block item.
        // @tags
        // <ItemTag.spawner_type>
        // -->
        if (mechanism.matches("spawner_type") && mechanism.requireObject(EntityTag.class)) {
            BlockStateMeta meta = (BlockStateMeta) item.getItemMeta();
            CreatureSpawner state = (CreatureSpawner) meta.getBlockState();
            state.setSpawnedType(mechanism.valueAsType(EntityTag.class).getBukkitEntityType());
            meta.setBlockState(state);
            item.setItemMeta(meta);
        }
    }
}
