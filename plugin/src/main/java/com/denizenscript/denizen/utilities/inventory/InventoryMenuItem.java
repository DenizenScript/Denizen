package com.denizenscript.denizen.utilities.inventory;

import com.denizenscript.denizen.utilities.Utilities;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class InventoryMenuItem {
    protected InventoryMenu menu;
    protected int index;
    protected ItemStack item;

    public InventoryMenuItem(ItemStack item) {
        this.item = item;
    }

    public InventoryMenuItem(String text) {
        this(text, new MaterialData(Material.PAPER));
    }

    public InventoryMenuItem(String text, MaterialData icon) {
        this(text, icon, 1);
    }

    public InventoryMenuItem(String text, MaterialData icon, int amount) {
        item = new ItemStack(icon.getItemType(), 1, icon.getData());

        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(text);
        item.setItemMeta(meta);

        item.setAmount(amount);
    }

    /**
     * Called when a player clicks on a button in a menu
     *
     * @param player       The player clicking the button
     * @param isRightClick True if the right mouse button has been pressed
     * @param isShiftClick True if the shift key has been pressed
     */
    public abstract void onClick(Player player, boolean isRightClick, boolean isShiftClick);

    protected void addToMenu(InventoryMenu menu) {
        this.menu = menu;
    }

    protected void removeFromMenu(InventoryMenu menu) {
        if (this.menu == menu) {
            this.menu = null;
        }
    }

    public InventoryMenu getMenu() {
        return menu;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int i) {
        index = i;
    }

    public int getAmount() {
        return item.getAmount();
    }

    public MaterialData getIcon() {
        return item.getData();
    }

    public String getText() {
        return item.getItemMeta().getDisplayName();
    }

    public void setDescriptions(String... lines) {
        ItemMeta meta = item.getItemMeta();
        List<String> lore = new ArrayList<>();

        for (String s : lines) {
            lore.addAll(Arrays.asList(Utilities.wrapWords(s, 24)));
        }

        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    protected ItemStack getItemStack() {
        return item;
    }
}
