package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;

public class EntityPotion implements Property {

    public static boolean describes(ObjectTag entity) {
        if (!(entity instanceof EntityTag)) {
            return false;
        }
        return ((EntityTag) entity).getBukkitEntity() instanceof ThrownPotion
                || ((EntityTag) entity).getBukkitEntity() instanceof Arrow;
    }

    public static EntityPotion getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityPotion((EntityTag) entity);
        }
    }

    public static final String[] handledTags = new String[] {
            "potion"
    };

    public static final String[] handledMechs = new String[] {
            "potion"
    };

    EntityTag entity;

    public EntityPotion(EntityTag entity) {
        this.entity = entity;
    }

    public ItemStack getPotion() {
        if (entity.getBukkitEntity() instanceof ThrownPotion) {
            return ((ThrownPotion) entity.getBukkitEntity()).getItem();
        }
        else { // Tipped arrow
            ItemStack refItem = new ItemStack(Material.POTION);
            PotionMeta meta = (PotionMeta) refItem.getItemMeta();
            // TODO: 1.20.6: PotionData API
            if (NMSHandler.getVersion().isAtMost(NMSVersion.v1_19)) {
                meta.setBasePotionData(((Arrow) entity.getBukkitEntity()).getBasePotionData());
            }
            refItem.setItemMeta(meta);
            return refItem;
        }
    }

    public void setPotion(ItemStack item) {
        if (entity.getBukkitEntity() instanceof ThrownPotion) {
            ((ThrownPotion) entity.getBukkitEntity()).setItem(item);
        }
        else { // Tipped arrow
            // TODO: 1.20.6: PotionData API
            if (NMSHandler.getVersion().isAtMost(NMSVersion.v1_19)) {
                ((Arrow) entity.getBukkitEntity()).setBasePotionData(((PotionMeta) item.getItemMeta()).getBasePotionData());
            }
        }
    }

    @Override
    public String getPropertyString() {
        return new ItemTag(getPotion()).identify();
    }

    @Override
    public String getPropertyId() {
        return "potion";
    }

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {
        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <EntityTag.potion>
        // @returns ItemTag
        // @mechanism EntityTag.potion
        // @group properties
        // @description
        // If the entity is a Tipped Arrow, returns an ItemTag of a potion with the base potion data of the arrow.
        // If the entity is a Splash Potion, returns an ItemTag of the splash potion's full potion data.
        // -->
        if (attribute.startsWith("potion")) {
            return new ItemTag(getPotion()).getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name potion
        // @input ItemTag
        // @description
        // Input must be a potion item!
        // If the entity is a Tipped Arrow, sets the arrow's base potion data based on the item input.
        // If the entity is a splash Potion, sets the splash potion's full potion data from the item input.
        // @tags
        // <EntityTag.potion>
        // -->
        if (mechanism.matches("potion") && mechanism.requireObject(ItemTag.class)) {
            setPotion(mechanism.valueAsType(ItemTag.class).getItemStack());
        }

    }
}
