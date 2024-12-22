package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.Mechanism;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.spawner.Spawner;

public class ItemSpawnerEntity extends ItemProperty<EntityTag> {

    // <--[property]
    // @object ItemTag
    // @name spawner_entity
    // @input EntityTag
    // @description
    // If the item is a spawner, sets the entity type that the spawner will spawn.
    // -->

    public static boolean describes(ItemTag item) {
        return item.getItemMeta() instanceof BlockStateMeta meta
                && meta.getBlockState() instanceof Spawner;
    }

    @Override
    public EntityTag getPropertyValue() {
        Spawner spawner = (Spawner) ((BlockStateMeta) getItemMeta()).getBlockState();
        if (!Bukkit.getWorlds().isEmpty()) {
            World world = Bukkit.getWorlds().getFirst();
            return new EntityTag(spawner.getSpawnedEntity().createEntity(world));
        }
        return new EntityTag(spawner.getSpawnedEntity().getEntityType());
    }

    @Override
    public void setPropertyValue(EntityTag entity, Mechanism mechanism) {
        editMeta(BlockStateMeta.class, meta -> {
            Spawner spawner = (Spawner) ((BlockStateMeta) getItemMeta()).getBlockState();
            spawner.setSpawnedEntity(entity.getBukkitEntity().createSnapshot());
            meta.setBlockState((BlockState) spawner);
        });
    }

    @Override
    public String getPropertyId() {
        return "spawner_entity";
    }

    public static void register() {
        autoRegisterNullable("spawner_entity", ItemSpawnerEntity.class, EntityTag.class, false);
    }
}
