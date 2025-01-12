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

public class EntityPotion extends EntityProperty<ItemTag> {

    // <--[property]
    // @object EntityTag
    // @name potion
    // @input ItemTag
    // @description
    // Controls a tipped arrow/splash potion's potion data.
    // For a Tipped Arrow, this is an ItemTag of a potion with the base potion data of the arrow.
    // For a Splash Potion, this is an ItemTag of the splash potion's full potion data.
    // -->

    public static boolean describes(EntityTag entity) {
        return entity.getBukkitEntity() instanceof ThrownPotion || entity.getBukkitEntity() instanceof Arrow;
    }

    @Override
    public ItemTag getPropertyValue() {
        if (getEntity() instanceof ThrownPotion thrownPotion) {
            return new ItemTag(thrownPotion.getItem());
        }
        else { // Tipped arrow
            ItemStack refItem = new ItemStack(Material.POTION);
            PotionMeta meta = (PotionMeta) refItem.getItemMeta();
            if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_20)) {
                meta.setBasePotionType(as(Arrow.class).getBasePotionType());
            }
            else {
                meta.setBasePotionData(as(Arrow.class).getBasePotionData());
            }
            refItem.setItemMeta(meta);
            return new ItemTag(refItem);
        }
    }

    @Override
    public void setPropertyValue(ItemTag value, Mechanism mechanism) {
        if (getEntity() instanceof ThrownPotion thrownPotion) {
            thrownPotion.setItem(value.getItemStack());
        }
        else { // Tipped arrow
            if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_20)) {
                as(Arrow.class).setBasePotionType(((PotionMeta) value.getItemMeta()).getBasePotionType());
            }
            else {
                as(Arrow.class).setBasePotionData(((PotionMeta) value.getItemMeta()).getBasePotionData());
            }
        }
    }

    @Override
    public String getPropertyId() {
        return "potion";
    }

    public static void register() {
        autoRegister("potion", EntityPotion.class, ItemTag.class, false);
    }
}
