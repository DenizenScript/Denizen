package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.properties.ObjectProperty;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public abstract class ItemProperty extends ObjectProperty<ItemTag> {

    public MaterialTag getMaterialTag() {
        return object.getMaterial();
    }

    public Material getMaterial() {
        return object.getBukkitMaterial();
    }

    public ItemStack getItemStack() {
        return object.getItemStack();
    }

    public ItemMeta getItemMeta() {
        return object.getItemMeta();
    }

    public void setItemStack(ItemStack item) {
        object.setItemStack(item);
    }

    public void setItemMeta(ItemMeta meta) {
        object.setItemMeta(meta);
    }
}
