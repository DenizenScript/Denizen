package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.Mechanism;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.spawner.Spawner;

public class MaterialSpawnerEntity extends MaterialProperty<EntityTag> {

    // <--[property]
    // @object MaterialTag
    // @name spawner_entity
    // @input EntityTag
    // @description
    // If the material is a spawner, sets the entity type that the spawner will spawn.
    // -->

    public static boolean describes(MaterialTag material) {
        return material.getModernData() instanceof BlockState blockState
                && blockState.getBlockData() instanceof Spawner;
    }

    @Override
    public EntityTag getPropertyValue() {
        Spawner spawner = ((Spawner) getBlockData());
        if (!Bukkit.getWorlds().isEmpty()) {
            World world = Bukkit.getWorlds().getFirst();
            return new EntityTag(spawner.getSpawnedEntity().createEntity(world));
        }
        return new EntityTag(spawner.getSpawnedEntity().getEntityType());
    }

    @Override
    public void setPropertyValue(EntityTag entityTag, Mechanism mechanism) {
        Spawner spawner = ((Spawner) getBlockData());
        spawner.setSpawnedEntity(entityTag.getBukkitEntity().createSnapshot());
    }

    @Override
    public String getPropertyId() {
        return "spawner_entity";
    }

    public static void register() {
        autoRegisterNullable("spawner_entity", MaterialSpawnerEntity.class, EntityTag.class, false);
    }
}
