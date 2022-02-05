package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.utilities.Deprecations;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;

public class EntityItem implements Property {

    public static boolean describes(ObjectTag object) {
        if (!(object instanceof EntityTag)) {
            return false;
        }
        Entity entity = ((EntityTag) object).getBukkitEntity();
        return entity instanceof Item
                || entity instanceof Enderman
                || entity instanceof SizedFireball
                || entity instanceof ThrowableProjectile
                || entity instanceof EnderSignal;
    }

    public static EntityItem getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityItem((EntityTag) entity);
        }
    }

    public static final String[] handledMechs = new String[] {
            "item"
    };

    private EntityItem(EntityTag entity) {
        item = entity;
    }

    EntityTag item;

    public ItemTag getItem(boolean includeDeprecated, TagContext context) {
        if (isDroppedItem()) {
            return new ItemTag(getDroppedItem().getItemStack());
        }
        else if (includeDeprecated && isEnderman()) {
            Deprecations.entityItemEnderman.warn(context);
            BlockData data = getEnderman().getCarriedBlock();
            if (data == null) {
                return new ItemTag(Material.AIR);
            }
            return new ItemTag(data.getMaterial());
        }
        else if (isFireball()) {
            return new ItemTag(getFireball().getDisplayItem());
        }
        else if (isThrowableProjectile()) {
            return new ItemTag(getThrowableProjectile().getItem());
        }
        else if (isEnderSignal()) {
            return new ItemTag(getEnderSignal().getItem());
        }
        return null;
    }

    public boolean isDroppedItem() {
        return item.getBukkitEntity() instanceof Item;
    }

    public boolean isEnderman() {
        return item.getBukkitEntity() instanceof Enderman;
    }

    public boolean isFireball() {
        return item.getBukkitEntity() instanceof SizedFireball;
    }

    public boolean isThrowableProjectile() {
        return item.getBukkitEntity() instanceof ThrowableProjectile;
    }

    public boolean isEnderSignal() {
        return item.getBukkitEntity() instanceof EnderSignal;
    }

    public Item getDroppedItem() {
        return (Item) item.getBukkitEntity();
    }

    public Enderman getEnderman() {
        return (Enderman) item.getBukkitEntity();
    }

    public EnderSignal getEnderSignal() {
        return (EnderSignal) item.getBukkitEntity();
    }

    public ThrowableProjectile getThrowableProjectile() {
        return (ThrowableProjectile) item.getBukkitEntity();
    }

    public SizedFireball getFireball() {
        return (SizedFireball) item.getBukkitEntity();
    }

    @Override
    public String getPropertyString() {
        ItemTag item = getItem(false, null);
        if (item.getBukkitMaterial() != Material.AIR) {
            return item.identify();
        }
        return null;
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
        // If the entity is a throwable projectile, returns the display item for that projectile.
        // If the entity is an eye-of-ender, returns the item to be displayed and dropped by it.
        // If the entity is a fireball, returns the fireball's display item.
        // -->
        PropertyParser.<EntityItem, ItemTag>registerTag(ItemTag.class, "item", (attribute, object) -> {
            return object.getItem(true, attribute.context);
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
        // If the entity is an eye-of-ender, sets the item to be displayed and dropped by it.
        // If the entity is a fireball, sets the fireball's display item.
        // @tags
        // <EntityTag.item>
        // -->
        if (mechanism.matches("item") && mechanism.requireObject(ItemTag.class)) {
            ItemStack itemStack = mechanism.valueAsType(ItemTag.class).getItemStack();
            if (item.isCitizensNPC()) {
                item.getDenizenNPC().getCitizen().data().setPersistent(NPC.ITEM_ID_METADATA, itemStack.getType().name());
            }
            if (isDroppedItem()) {
                getDroppedItem().setItemStack(itemStack);
            }
            else if (isEnderman()) {
                Deprecations.entityItemEnderman.warn(mechanism.context);
                getEnderman().setCarriedBlock(itemStack.getType().createBlockData());
            }
            else if (isFireball()) {
                getFireball().setDisplayItem(itemStack);
            }
            else if (isThrowableProjectile()) {
                getThrowableProjectile().setItem(itemStack);
            }
            else if (isEnderSignal()) {
                getEnderSignal().setItem(itemStack);
            }
        }
    }
}
