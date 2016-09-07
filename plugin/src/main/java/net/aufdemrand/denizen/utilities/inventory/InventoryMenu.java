package net.aufdemrand.denizen.utilities.inventory;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class InventoryMenu implements InventoryHolder {
    public final static int ROW_SIZE = 9;

    protected InventoryMenuItem[] items;
    protected Inventory inventory;
    protected String title;
    protected int size;
    protected MenuCloseBehaviour closeBehaviour;

    public InventoryMenu(String title, int rows) {
        this.title = title;
        this.size = rows * ROW_SIZE;

        this.items = new InventoryMenuItem[size];
    }

    public void setMenuCloseBehaviour(MenuCloseBehaviour menuCloseBehaviour) {
        this.closeBehaviour = menuCloseBehaviour;

    }

    public MenuCloseBehaviour getMenuCloseBehaviour() {
        return closeBehaviour;
    }

    public Inventory getInventory() {
        if (inventory == null) {
            inventory = Bukkit.createInventory(this, size, title);
        }

        return inventory;
    }

    public boolean addItem(InventoryMenuItem item, int x, int y) {
        return addItem(item, y * ROW_SIZE + x);
    }

    public boolean addItem(InventoryMenuItem item, int index) {
        ItemStack slot = getInventory().getItem(index);
        if (slot != null && slot.getType() != Material.AIR) {
            return false;
        }

        getInventory().setItem(index, item.getItemStack());
        items[index] = item;
        item.setIndex(index);
        item.addToMenu(this);
        return true;
    }

    public void selectItem(Player player, int index) {
        selectItem(player, index, false, false);
    }

    public void selectItem(Player player, int index, boolean right, boolean shift) {
        InventoryMenuItem item = items[index];
        if (item != null) {
            item.onClick(player, right, shift);
        }
    }

    public void openMenu(Player player) {
        if (getInventory().getViewers().contains(player)) {
            throw new IllegalArgumentException(player.getName() + " is already viewing " + getInventory().getTitle());
        }

        player.openInventory(getInventory());
    }

    public void updateMenu() {
        for (HumanEntity entity : getInventory().getViewers()) {
            ((Player) entity).updateInventory(); // Currently deprecated, but there is no alternative :(
        }
    }

    public void closeMenu(Player player) {
        if (getInventory().getViewers().contains(player)) {
            InventoryCloseEvent event = new InventoryCloseEvent(player.getOpenInventory());
            Bukkit.getPluginManager().callEvent(event);
            player.closeInventory();
        }
    }

    /* -------- General override functions -------- */
    @Override
    protected InventoryMenu clone() { // TODO: if not using super.clone, should this be a separate method?
        InventoryMenu c = new InventoryMenu(title, size);
        c.setMenuCloseBehaviour(closeBehaviour);

        for (int i = 0; i < items.length; i++) {
            addItem(items[i], i);
        }

        return c;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{title=" + title + "; size=" + size + "}";
    }
}
