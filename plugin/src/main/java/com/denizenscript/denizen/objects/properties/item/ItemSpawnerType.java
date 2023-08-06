package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.Mechanism;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.meta.BlockStateMeta;

public class ItemSpawnerType extends ItemProperty<EntityTag> {

    // <--[property]
    // @object ItemTag
    // @name spawner_type
    // @input EntityTag
    // @description
    // The entity type a spawner item will spawn, if any.
    // For the mechanism: provide no input to unset the type.
    // Note that the type can only be unset on 1.20 and above.
    // -->

    public static boolean describes(ItemTag item) {
        return item.getItemMeta() instanceof BlockStateMeta meta && meta.getBlockState() instanceof CreatureSpawner;
    }

    @Override
    public EntityTag getPropertyValue() {
        EntityType spawnedType = ((CreatureSpawner) ((BlockStateMeta) getItemMeta()).getBlockState()).getSpawnedType();
        return spawnedType != null ? new EntityTag(spawnedType) : null;
    }

    @Override
    public void setPropertyValue(EntityTag entity, Mechanism mechanism) {
        if (entity == null && NMSHandler.getVersion().isAtMost(NMSVersion.v1_19)) {
            mechanism.echoError("must have input of type 'EntityTag', but none was given.");
            return;
        }
        editMeta(BlockStateMeta.class, meta -> {
            CreatureSpawner spawner = (CreatureSpawner) meta.getBlockState();
            spawner.setSpawnedType(entity != null ? entity.getBukkitEntityType() : null);
            meta.setBlockState(spawner);
        });
    }

    @Override
    public String getPropertyId() {
        return "spawner_type";
    }

    public static void register() {
        autoRegisterNullable("spawner_type", ItemSpawnerType.class, EntityTag.class, false);
    }
}
