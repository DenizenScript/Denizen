package net.aufdemrand.denizen.scripts.containers.core;

import net.aufdemrand.denizen.events.EventManager;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ItemScriptHelper implements Listener {

    public static Map<String, ItemScriptContainer> item_scripts = new ConcurrentHashMap<String, ItemScriptContainer>(8, 0.9f, 1);

    public static Map<String, ItemScriptContainer> item_scripts_by_hash_id = new HashMap<String, ItemScriptContainer>();

    public ItemScriptHelper() {
        DenizenAPI.getCurrentInstance().getServer().getPluginManager()
                .registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    // Remove all recipes stored by Denizen
    public static void removeDenizenRecipes() {
        ItemScriptContainer.specialrecipesMap.clear();
        ItemScriptContainer.shapelessRecipesMap.clear();
    }

    public static boolean isBound(ItemStack item) {
        return (isItemscript(item) && getItemScriptContainer(item).bound);
    }

    public static boolean isItemscript(ItemStack item) {
        return getItemScriptContainer(item) != null;
    }

    public static ItemScriptContainer getItemScriptContainer(ItemStack item) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore())
            return null;
        for (String itemLore : item.getItemMeta().getLore()) {
            if (itemLore.startsWith(dItem.itemscriptIdentifier))
                return item_scripts.get(itemLore.replace(dItem.itemscriptIdentifier, ""));
            if (itemLore.startsWith(ItemScriptHashID))
                return item_scripts_by_hash_id.get(itemLore);
        }
        return null;
    }

    public static String ItemScriptHashID = ChatColor.RED.toString() + ChatColor.BLUE + ChatColor.BLACK;

    public static String createItemScriptID(ItemScriptContainer container) {
        String colors = createItemScriptID(container.getName());
        container.setHashID(colors);
        return colors;
    }
    public static String createItemScriptID(String name) {
        String script = name.toUpperCase();
        StringBuilder colors = new StringBuilder();
        colors.append(ItemScriptHashID);
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = script.getBytes("UTF-8");
            md.update(bytes, 0, bytes.length);
            String hash = new BigInteger(1, md.digest()).toString(16);
            for (int i = 0; i < 16; i++) {
                colors.append(ChatColor.COLOR_CHAR).append(hash.charAt(i));
            }
        }
        catch (Exception ex) {
            dB.echoError(ex);
            colors.append(ChatColor.BLUE);
        }
        return colors.toString();
    }

    // When special Denizen recipes that have itemscripts as ingredients
    // are being used, check crafting matrix for recipe matches whenever
    // clicks are made in CRAFTING or RESULT slots
    @EventHandler
    public void specialRecipeClick(InventoryClickEvent event) {

        // Proceed only if at least one special recipe has been stored
        if (ItemScriptContainer.specialrecipesMap.isEmpty()
                && ItemScriptContainer.shapelessRecipesMap.isEmpty())
            return;

        // Proceed only if this is a CraftingInventory
        if (!(event.getInventory() instanceof CraftingInventory))
            return;

        // Store the slot type that was clicked
        SlotType slotType = event.getSlotType();

        // Proceed only if a CRAFTING or RESULT slot was clicked
        if (slotType.equals(InventoryType.SlotType.CRAFTING) ||
            slotType.equals(InventoryType.SlotType.RESULT)) {

            CraftingInventory inventory = (CraftingInventory) event.getInventory();
            Player player = (Player) event.getWhoClicked();

            // If the RESULT slot was shift-clicked, emulate
            // shift click behavior for it
            if (slotType.equals(InventoryType.SlotType.RESULT) &&
                event.isShiftClick()) {
                emulateSpecialRecipeResultShiftClick(inventory, player);
            }
            // Otherwise check for special recipe matches
            else {
                processSpecialRecipes(inventory, player);
            }
        }
    }

    // When special Denizen recipes that have itemscripts as ingredients
    // are being used, check crafting matrix for recipe matches whenever
    // drags (which are entirely separate from clicks) are made in CRAFTING slots
    @EventHandler
    public void specialRecipeDrag(InventoryDragEvent event) {

        // Proceed only if at least one special recipe has been stored
        if (ItemScriptContainer.specialrecipesMap.isEmpty()
                && ItemScriptContainer.shapelessRecipesMap.isEmpty())
            return;

        // Proceed only if this is a CraftingInventory
        if (!(event.getInventory() instanceof CraftingInventory))
            return;

        // Check for special recipe matches if the drag involved a CRAFTING slot,
        // which can have an ID of between 1 and 9 in a CraftingInventory
        for (Integer slot : event.getInventorySlots()) {
            if (slot < 10) {

                CraftingInventory inventory = (CraftingInventory) event.getInventory();
                Player player = (Player) event.getWhoClicked();
                processSpecialRecipes(inventory, player);
                break;
            }
        }
    }

    // Compare a crafting matrix with all stored special recipes right
    // after a click or drag has been made in it
    public void processSpecialRecipes(final CraftingInventory inventory, final Player player) {

        // Run a task 1 tick later than the event from which this method
        // was called, to check the new state of the CraftingInventory's matrix
        Bukkit.getScheduler().scheduleSyncDelayedTask(DenizenAPI.getCurrentInstance(),
        new Runnable() {
            @Override
            public void run() {
                // Store the current matrix
                ItemStack[] matrix = inventory.getMatrix();

                // Get the result of the special recipe that this matrix matches,
                // if any
                dItem result = getSpecialRecipeResult(matrix);

                // Proceed only if the result was not null
                if (result != null) {
                    Map<String, dObject> context = new HashMap<String, dObject>();
                    context.put("inventory", new dInventory(inventory));
                    context.put("item", result);

                    dList recipeList = new dList();
                    for (ItemStack item : inventory.getMatrix()) {
                        if (item != null)
                            recipeList.add(new dItem(item).identify());
                        else
                            recipeList.add(new dItem(Material.AIR).identify());
                    }
                    context.put("recipe", recipeList);

                    String determination = EventManager.doEvents(Arrays.asList
                            ("item crafted",
                                    result.identifySimple() + " crafted",
                                    result.identifyMaterial() + " crafted"),
                            null, new dPlayer(player), context);

                    if (determination.toUpperCase().startsWith("CANCELLED"))
                        return;
                    else if (dItem.matches(determination)) {
                        result = dItem.valueOf(determination);
                    }

                    // If this was a valid match, set the crafting's result
                    inventory.setResult(result.getItemStack());

                    // Update the player's inventory
                    //
                    // TODO: Replace with non-deprecated method once one
                    // is added to Bukkit
                    player.updateInventory();
                }
            }
        }, 0);
    }

    // Check if a CraftingInventory's crafting matrix matches a special
    // recipe and return that recipe's dItem result if it does
    public dItem getSpecialRecipeResult(ItemStack[] matrix) {

        // Iterate through all the special recipes
        master: for (Map.Entry<dItem, dList> entry :
                ItemScriptContainer.specialrecipesMap.entrySet()) {

            // Check if the two sets of items match each other
            for (int n = 0; n < 9; n++) {

                // Use dItem.valueOf on the entry values to ensure
                // correct comparison
                dItem valueN = dItem.valueOf(entry.getValue().get(n));
                dItem matrixN = matrix.length <= n || matrix[n] == null ? new dItem(Material.AIR): new dItem(matrix[n].clone());

                // If one's an item script and the other's not, it's a fail
                if (valueN.isItemscript() != matrixN.isItemscript())
                    continue master;
                // If they're both item scripts, and they are different scripts, it's a fail
                if (valueN.isItemscript() && matrixN.isItemscript()) {
                        if (!valueN.getScriptName().equalsIgnoreCase(matrixN.getScriptName()))
                    continue master;
                }
                // If they're both not item scripts, and the materials are different, it's a fail
                else if (!valueN.getMaterial().matchesMaterialData(matrixN.getMaterial().getMaterialData()))
                    continue master;
            }

            // If all the items match, return the special recipe's dItem key
            return entry.getKey();
        }

        primary: for (Map.Entry<dItem, dList> entry :
                ItemScriptContainer.shapelessRecipesMap.entrySet()) {
            for (int i = 0; i < entry.getValue().size(); i++) {
                if (!contains_any(dItem.valueOf(entry.getValue().get(i)), matrix)) {
                    continue primary;
                }
            }
            return entry.getKey();
        }

        return null;
    }

    public boolean contains_any(dItem item, ItemStack[] matrix) {
        String full = item.getFullString();
        for (int i = 0; i < matrix.length; i++) {
            if (full.equalsIgnoreCase(new dItem(matrix[i]).getFullString())) {
                return true;
            }
        }
        return false;
    }

    // Because Denizen special recipes are basically fake recipes,
    // shift clicking the result slot will not work by itself and needs
    // to be emulated like below
    public void emulateSpecialRecipeResultShiftClick(CraftingInventory inventory, Player player) {

        // Store the crafting matrix
        ItemStack[] matrix = inventory.getMatrix();

        // Get the result of the special recipe that this matrix matches,
        // if any
        dItem result = getSpecialRecipeResult(matrix);

        // Proceed only if the result was not null
        if (result != null) {

            // In a shift click, the amount of the resulting dItem should
            // be based on the amount of the least numerous ingredient,
            // so track it
            int lowestAmount = 0;

            // Set lowestAmount to the amount of the first item found,
            // then set it to that of the ingredient with the lowest amount
            // found that isn't zero
            for (int n = 0; n < matrix.length - 1; n++) {
                if ((matrix[n].getAmount() > 0 &&
                     matrix[n].getAmount() < lowestAmount) || lowestAmount == 0) {
                        lowestAmount = matrix[n].getAmount();
                }
            }

            // Deduct that amount from every ingredient in the matrix,
            // but account for the fact that clicking the RESULT slot
            // will also deduct 1 from every ingredient by itself
            for (int n = 0; n < matrix.length - 1; n++) {
                if (matrix[n].getAmount() > 0) {
                    matrix[n].setAmount(matrix[n].getAmount() - lowestAmount + 1);
                }
            }

            // If the lowest amount is 1, there is no need to further
            // emulate a shift click at this point, and continuing the
            // emulation will actually cause bugs in the inventory's updating,
            // so only proceed if lowestAmount is higher than 1
            if (lowestAmount > 1) {

                // Get a clone of the recipe's result itemstack, so that
                // changing its amount now does not affect future craftings
                ItemStack resultStack = result.getItemStack().clone();

                // Set the itemstack's amount
                resultStack.setAmount(lowestAmount * resultStack.getAmount());

                // Set the crafting's result
                inventory.setResult(resultStack);

                // Update the player's inventory
                //
                // TODO: Replace with non-deprecated method once one
                // is added to Bukkit
                player.updateInventory();
            }
        }
    }

    @EventHandler
    public void boundInventoryClickEvent(InventoryClickEvent event) {
        // Proceed only if this is a CraftingInventory
        if (!(event.getInventory() instanceof CraftingInventory))
            return;

        // Proceed only if the click has a cursor item that is bound
        ItemStack item = event.getCursor();
        if (item == null || !isBound(item))
            return;

        if (event.getInventory().getType() != InventoryType.PLAYER) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler
    public void boundInventoryDragEvent(InventoryDragEvent event) {
        // Proceed only if this is a CraftingInventory
        if (!(event.getInventory() instanceof CraftingInventory))
            return;

        // Proceed only if the items are bound
        ItemStack item = event.getOldCursor();
        if (item == null || !isBound(item))
            return;

        if (event.getInventory().getType() != InventoryType.PLAYER) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler
    public void boundDropItem(PlayerDropItemEvent event) {
        // If the item is bound, don't let them drop it!
        if (isBound(event.getItemDrop().getItemStack())) {
            event.setCancelled(true);
        }
    }
}
