package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.ObjectProperty;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Consumer;

public abstract class ItemProperty<TData extends ObjectTag> extends ObjectProperty<ItemTag, TData> {

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

    @SuppressWarnings("unchecked")
    public <T extends ItemMeta> T as(Class<T> metaType) {
        return (T) getItemMeta();
    }

    @SuppressWarnings("unchecked")
    public <T extends ItemMeta> void editMeta(Class<T> metaType, Consumer<T> editor) {
        T meta = (T) getItemMeta();
        editor.accept(meta);
        setItemMeta(meta);
    }
}
