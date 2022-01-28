package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import com.denizenscript.denizencore.tags.Attribute;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.ThrowableProjectile;
import org.bukkit.inventory.ItemStack;

public class EntityItem implements Property {

    public static boolean describes(ObjectTag entity) {
        if (!(entity instanceof EntityTag)) {
            return false;
        }
        EntityType type = ((EntityTag) entity).getBukkitEntityType();
        if (type == EntityType.DROPPED_ITEM || type == EntityType.ENDERMAN || type == EntityType.TRIDENT) {
            return true;
        }
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_15)) {
            if (((EntityTag) entity).getBukkitEntity() instanceof ThrowableProjectile) {
                return true;
            }
        }
        return false;
    }

    public static EntityItem getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityItem((EntityTag) entity);
        }
    }

    public static final String[] handledTags = new String[] {
            "item"
    };

    public static final String[] handledMechs = new String[] {
            "item"
    };

    private EntityItem(EntityTag entity) {
        item = entity;
    }

    EntityTag item;

    public ItemTag getItem() {
        if (item.getBukkitEntity() instanceof Item) {
            return new ItemTag(((Item) item.getBukkitEntity()).getItemStack());
        }
        else if (item.getBukkitEntityType() == EntityType.TRIDENT) {
            return new ItemTag(NMSHandler.getEntityHelper().getItemFromTrident(item.getBukkitEntity()));
        }
        else if (item.getBukkitEntity() instanceof Enderman) {
            BlockData data = ((Enderman) item.getBukkitEntity()).getCarriedBlock();
            if (data == null) {
                return new ItemTag(Material.AIR);
            }
            Material mat = data.getMaterial();
            return new ItemTag(mat);
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_15) && item.getBukkitEntity() instanceof ThrowableProjectile) {
            return new ItemTag(((ThrowableProjectile) item.getBukkitEntity()).getItem());
        }
        return null;
    }

    @Override
    public String getPropertyString() {
        ItemTag item = getItem();
        if (item.getBukkitMaterial() != Material.AIR) {
            return item.identify();
        }
        else {
            return null;
        }
    }

    @Override
    public String getPropertyId() {
        return "item";
    }


    public static void registerTags() {

        // <--[tag]
        // @attribute <EntityTag.item>
        // @returns ItemTag
        // @mechanism EntityTag.item
        // @group properties
        // @description
        // If the entity is a dropped item, returns the item represented by the entity.
        // If the entity is a trident, returns the trident item represented by the entity.
        // If the item is a throwable projectile, returns the display item for that projectile.
        // -->
        PropertyParser.<EntityItem, ItemTag>registerTag(ItemTag.class, "item", (attribute, object) -> {
            return object.getItem();
        });
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name item
        // @input ItemTag
        // @description
        // If the entity is a dropped item, sets the item represented by the entity.
        // If the entity is a trident, sets the trident item represented by the entity.
        // If the item is a throwable projectile, sets the display item for that projectile.
        // @tags
        // <EntityTag.item>
        // -->
        if (mechanism.matches("item") && mechanism.requireObject(ItemTag.class)) {
            ItemStack itemStack = mechanism.valueAsType(ItemTag.class).getItemStack();
            if (item.isCitizensNPC()) {
                item.getDenizenNPC().getCitizen().data().setPersistent(NPC.ITEM_ID_METADATA, itemStack.getType().name());
            }
            if (item.getBukkitEntity() instanceof Item) {
                ((Item) item.getBukkitEntity()).setItemStack(itemStack);
            }
            else if (item.getBukkitEntityType() == EntityType.TRIDENT) {
                NMSHandler.getEntityHelper().setItemForTrident(item.getBukkitEntity(), itemStack);
            }
            else if (item.getBukkitEntity() instanceof Enderman) {
                NMSHandler.getEntityHelper().setCarriedItem((Enderman) item.getBukkitEntity(), itemStack);
            }
            else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_15) && item.getBukkitEntity() instanceof ThrowableProjectile) {
                ((ThrowableProjectile) item.getBukkitEntity()).setItem(itemStack);
            }
        }
    }
}
