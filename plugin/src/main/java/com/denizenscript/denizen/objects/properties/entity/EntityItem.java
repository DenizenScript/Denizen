package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.Mechanism;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.*;

public class EntityItem extends EntityProperty<ItemTag> {

    // <--[property]
    // @object EntityTag
    // @name item
    // @input ItemTag
    // @description
    // An entity's item, which can be:
    // - the item represented and displayed by a dropped item.
    // - the item represented by a thrown trident.
    // - a throwable projectile's display item.
    // - an eye-of-ender's item, which is both displayed and dropped.
    // - a fireball's display item.
    // - an item display's display item.
    // - an ominous item spawner's display item.
    // -->

    public static boolean describes(EntityTag object) {
        Entity entity = object.getBukkitEntity();
        return entity instanceof Item
                || entity instanceof Enderman
                || entity instanceof SizedFireball
                || entity instanceof ThrowableProjectile
                || entity instanceof EnderSignal
                || (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19) && entity instanceof ItemDisplay)
                || (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21) && entity instanceof OminousItemSpawner);
    }

    @Override
    public ItemTag getPropertyValue() {
        if (getEntity() instanceof Item item) {
            return new ItemTag(item.getItemStack());
        }
        else if (getEntity() instanceof SizedFireball fireball) {
            return new ItemTag(fireball.getDisplayItem());
        }
        else if (getEntity() instanceof ThrowableProjectile projectile) {
            return new ItemTag(projectile.getItem());
        }
        else if (getEntity() instanceof EnderSignal signal) {
            return new ItemTag(signal.getItem());
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19) && getEntity() instanceof ItemDisplay itemDisplay) {
            return new ItemTag(itemDisplay.getItemStack());
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21) && getEntity() instanceof OminousItemSpawner ominousItemSpawner) {
            return new ItemTag(ominousItemSpawner.getItem());
        }
        return null;
    }

    @Override
    public void setPropertyValue(ItemTag item, Mechanism mechanism) {
        if (object.isCitizensNPC()) {
            object.getDenizenNPC().getCitizen().data().setPersistent(NPC.Metadata.ITEM_ID, item.getBukkitMaterial().name());
        }
        if (getEntity() instanceof Item droppedItem) {
            droppedItem.setItemStack(item.getItemStack());
        }
        else if (getEntity() instanceof SizedFireball fireball) {
            fireball.setDisplayItem(item.getItemStack());
        }
        else if (getEntity() instanceof ThrowableProjectile projectile) {
            projectile.setItem(item.getItemStack());
        }
        else if (getEntity() instanceof EnderSignal signal) {
            signal.setItem(item.getItemStack());
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19) && getEntity() instanceof ItemDisplay itemDisplay) {
            itemDisplay.setItemStack(item.getItemStack());
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21) && getEntity() instanceof OminousItemSpawner ominousItemSpawner) {
            ominousItemSpawner.setItem(item.getItemStack());
        }
    }

    @Override
    public String getPropertyId() {
        return "item";
    }

    public static void register() {
        autoRegister("item", EntityItem.class, ItemTag.class, false);
    }
}
