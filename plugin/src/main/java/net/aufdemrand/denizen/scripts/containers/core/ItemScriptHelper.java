package net.aufdemrand.denizen.scripts.containers.core;

import net.aufdemrand.denizen.events.bukkit.ScriptReloadEvent;
import net.aufdemrand.denizen.events.player.ItemRecipeFormedScriptEvent;
import net.aufdemrand.denizen.events.player.PlayerCraftsItemScriptEvent;
import net.aufdemrand.denizen.nms.NMSHandler;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.tags.BukkitTagContext;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.objects.dScript;
import net.aufdemrand.denizencore.tags.TagManager;
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
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ItemScriptHelper implements Listener {

    public static final Map<String, ItemScriptContainer> item_scripts = new ConcurrentHashMap<String, ItemScriptContainer>(8, 0.9f, 1);
    public static final Map<String, ItemScriptContainer> item_scripts_by_hash_id = new HashMap<String, ItemScriptContainer>();
    public static final Map<ItemScriptContainer, List<String>> recipes_to_register = new HashMap<ItemScriptContainer, List<String>>();
    public static final Map<ItemScriptContainer, String> shapeless_to_register = new HashMap<ItemScriptContainer, String>();
    public static final Map<ItemScriptContainer, String> furnace_to_register = new HashMap<ItemScriptContainer, String>();

    public ItemScriptHelper() {
        DenizenAPI.getCurrentInstance().getServer().getPluginManager()
                .registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    // Remove all recipes stored by Denizen
    public static void removeDenizenRecipes() {
        ItemScriptContainer.specialrecipesMap.clear();
        ItemScriptContainer.shapelessRecipesMap.clear();
    }

    @EventHandler
    public void scriptReload(ScriptReloadEvent event) {

        for (Map.Entry<ItemScriptContainer, List<String>> entry : recipes_to_register.entrySet()) {

            ItemScriptContainer container = entry.getKey();
            List<String> recipeList = entry.getValue();

            // Process all tags in list
            for (int n = 0; n < recipeList.size(); n++) {
                recipeList.set(n, TagManager.tag(recipeList.get(n), new BukkitTagContext(container.player, container.npc,
                        false, null, dB.shouldDebug(container), new dScript(container))));
            }

            // Store every ingredient in a List
            List<dItem> ingredients = new ArrayList<dItem>();

            boolean shouldRegister = true;
            recipeLoop:
            for (String recipeRow : recipeList) {
                String[] elements = recipeRow.split("\\|", 3);

                for (String element : elements) {
                    dItem ingredient = dItem.valueOf(element.replaceAll("[iImM]@", ""));
                    if (ingredient == null) {
                        dB.echoError("Invalid dItem ingredient, recipe will not be registered for item script '"
                                + container.getName() + "': " + element);
                        shouldRegister = false;
                        break recipeLoop;
                    }
                    ingredients.add(ingredient);
                }
            }

            // Add the recipe to Denizen's item script recipe list so it
            // will be checked manually inside ItemScriptHelper
            if (shouldRegister) {
                ItemScriptContainer.specialrecipesMap.put(container, ingredients);
            }
        }

        for (Map.Entry<ItemScriptContainer, String> entry : shapeless_to_register.entrySet()) {

            ItemScriptContainer container = entry.getKey();
            String string = entry.getValue();

            String list = TagManager.tag(string, new BukkitTagContext(container.player, container.npc,
                    false, null, dB.shouldDebug(container), new dScript(container)));

            List<dItem> ingredients = new ArrayList<dItem>();

            boolean shouldRegister = true;
            for (String element : dList.valueOf(list)) {
                dItem ingredient = dItem.valueOf(element.replaceAll("[iImM]@", ""));
                if (ingredient == null) {
                    dB.echoError("Invalid dItem ingredient, shapeless recipe will not be registered for item script '"
                            + container.getName() + "': " + element);
                    shouldRegister = false;
                    break;
                }
                ingredients.add(ingredient);
            }
            if (shouldRegister) {
                ItemScriptContainer.shapelessRecipesMap.put(container, ingredients);
            }
        }

        for (Map.Entry<ItemScriptContainer, String> entry : furnace_to_register.entrySet()) {

            dItem furnace_item = dItem.valueOf(entry.getValue());
            if (furnace_item == null) {
                dB.echoError("Invalid item '" + entry.getValue() + "'");
                continue;
            }
            FurnaceRecipe recipe = new FurnaceRecipe(entry.getKey().getItemFrom().getItemStack(), furnace_item.getMaterial().getMaterial(), furnace_item.getItemStack().getDurability());
            Bukkit.getServer().addRecipe(recipe);
        }

        recipes_to_register.clear();
        shapeless_to_register.clear();
        furnace_to_register.clear();
    }

    public static boolean isBound(ItemStack item) {
        return (isItemscript(item) && getItemScriptContainer(item).bound);
    }

    public static boolean isItemscript(ItemStack item) {
        return getItemScriptContainer(item) != null;
    }

    public static ItemScriptContainer getItemScriptContainer(ItemStack item) {
        if (item == null) {
            return null;
        }
        String nbt = NMSHandler.getInstance().getItemHelper().getNbtData(item).getString("Denizen Item Script");
        if (nbt != null && !nbt.equals("")) {
            return item_scripts_by_hash_id.get(nbt);
        }
        if (!item.hasItemMeta() || !item.getItemMeta().hasLore()) {
            return null;
        }
        for (String itemLore : item.getItemMeta().getLore()) {
            if (itemLore.startsWith(dItem.itemscriptIdentifier)) {
                return item_scripts.get(itemLore.replace(dItem.itemscriptIdentifier, ""));
            }
            if (itemLore.startsWith(ItemScriptHashID)) {
                return item_scripts_by_hash_id.get(itemLore);
            }
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
                && ItemScriptContainer.shapelessRecipesMap.isEmpty()) {
            return;
        }

        // Proceed only if this is a CraftingInventory
        if (!(event.getInventory() instanceof CraftingInventory)) {
            return;
        }

        // Store the slot type that was clicked
        SlotType slotType = event.getSlotType();

        // Proceed only if a CRAFTING or RESULT slot was clicked
        if (slotType.equals(InventoryType.SlotType.CRAFTING) ||
                slotType.equals(InventoryType.SlotType.RESULT)) {

            CraftingInventory inventory = (CraftingInventory) event.getInventory();
            Player player = (Player) event.getWhoClicked();

            if (slotType == SlotType.RESULT && inventory.getResult() != null
                    && inventory.getResult().getData().getItemType() != Material.AIR) {
                PlayerCraftsItemScriptEvent scriptEvent = PlayerCraftsItemScriptEvent.instance;
                scriptEvent.inventory = inventory;
                scriptEvent.result = new dItem(inventory.getResult());
                dList recipeList = new dList();
                for (ItemStack item : inventory.getMatrix()) {
                    if (item != null) {
                        recipeList.add(new dItem(item.clone()).identify());
                    }
                    else {
                        recipeList.add(new dItem(Material.AIR).identify());
                    }
                }
                scriptEvent.recipe = recipeList;
                scriptEvent.player = dPlayer.mirrorBukkitPlayer(player);
                scriptEvent.resultChanged = false;
                scriptEvent.fire();
                if (scriptEvent.cancelled) {
                    event.setCancelled(true);
                    return;
                }
                else if (scriptEvent.resultChanged) {
                    event.setCurrentItem(scriptEvent.result.getItemStack());
                }
            }

            // If the RESULT slot was shift-clicked, emulate
            // shift click behavior for it
            boolean clicked;
            if (slotType == SlotType.RESULT && event.isShiftClick()) {
                clicked = emulateSpecialRecipeResultShiftClick(inventory, player);
            }
            // Otherwise check for special recipe matches
            else {
                clicked = processSpecialRecipes(inventory, player);
            }
            if (clicked && slotType.equals(SlotType.RESULT)) {
                removeOneFromEachSlot(inventory, player);
            }
        }
    }

    public void removeOneFromEachSlot(final CraftingInventory inventory, final Player player) {
        // This cloning is a workaround for what seems to be a Spigot issue
        // Basically, after taking the crafted item, it randomly sets the recipe items
        // to twice their amount minus 2. I have no idea why.
        final ItemStack[] matrix = inventory.getMatrix();
        for (int i = 0; i < matrix.length; i++) {
            ItemStack is = matrix[i];
            matrix[i] = is == null ? new ItemStack(Material.AIR) : is.clone();
        }
        Bukkit.getScheduler().scheduleSyncDelayedTask(DenizenAPI.getCurrentInstance(),
                new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < matrix.length; i++) {
                            if (matrix[i] != null) {
                                if (matrix[i].getAmount() == 0) {
                                    matrix[i] = null;
                                }
                                else {
                                    matrix[i].setAmount(matrix[i].getAmount() - 1);
                                    if (matrix[i].getAmount() == 0) {
                                        matrix[i] = null;
                                    }
                                }
                            }
                        }
                        inventory.setMatrix(matrix);
                        player.updateInventory();
                    }
                }, 0);
    }

    // When special Denizen recipes that have itemscripts as ingredients
    // are being used, check crafting matrix for recipe matches whenever
    // drags (which are entirely separate from clicks) are made in CRAFTING slots
    @EventHandler
    public void specialRecipeDrag(InventoryDragEvent event) {

        // Proceed only if at least one special recipe has been stored
        if (ItemScriptContainer.specialrecipesMap.isEmpty()
                && ItemScriptContainer.shapelessRecipesMap.isEmpty()) {
            return;
        }

        // Proceed only if this is a CraftingInventory
        if (!(event.getInventory() instanceof CraftingInventory)) {
            return;
        }

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
    public boolean processSpecialRecipes(final CraftingInventory inventory, final Player player) {

        // Store the current matrix
        ItemStack[] matrix1 = inventory.getMatrix();
        for (int i = 0; i < matrix1.length; i++) {
            matrix1[i] = matrix1[i] == null ? new ItemStack(Material.AIR) : matrix1[i].clone();
        }

        // Get the result of the special recipe that this matrix matches,
        // if any
        dItem result1 = getSpecialRecipeResult(matrix1, player);

        boolean returnme = result1 != null;

        // Run a task 1 tick later than the event from which this method
        // was called, to check the new state of the CraftingInventory's matrix
        Bukkit.getScheduler().scheduleSyncDelayedTask(DenizenAPI.getCurrentInstance(),
                new Runnable() {
                    @Override
                    public void run() {
                        // Store the current matrix
                        ItemStack[] matrix = inventory.getMatrix();
                        for (int i = 0; i < matrix.length; i++) {
                            matrix[i] = matrix[i] == null ? new ItemStack(Material.AIR) : matrix[i].clone();
                        }

                        // Get the result of the special recipe that this matrix matches,
                        // if any
                        dItem result = getSpecialRecipeResult(matrix, player);

                        // Proceed only if the result was not null
                        if (result != null) {
                            dList recipeList = new dList();
                            for (ItemStack item : matrix) {
                                if (item != null) {
                                    recipeList.add(new dItem(item).identify());
                                }
                                else {
                                    recipeList.add(new dItem(Material.AIR).identify());
                                }
                            }

                            ItemRecipeFormedScriptEvent event = ItemRecipeFormedScriptEvent.instance;
                            event.result = result;
                            event.recipe = recipeList;
                            event.inventory = inventory;
                            event.player = dPlayer.mirrorBukkitPlayer(player);
                            event.cancelled = false;
                            event.resultChanged = false;
                            event.fire();
                            if (event.cancelled) {
                                inventory.setResult(null);
                            }
                            else {
                                // If this was a valid match, set the crafting's result
                                inventory.setResult(event.result.getItemStack());
                            }

                            // Update the player's inventory
                            player.updateInventory();
                        }
                    }
                }, 2);

        return returnme;
    }

    // Check if a CraftingInventory's crafting matrix matches a special
    // recipe and return that recipe's dItem result if it does
    public dItem getSpecialRecipeResult(ItemStack[] matrix, Player player) {

        // Iterate through all the special recipes
        master:
        for (Map.Entry<ItemScriptContainer, List<dItem>> entry :
                ItemScriptContainer.specialrecipesMap.entrySet()) {

            // Check if the two sets of items match each other
            for (int n = 0; n < 9; n++) {

                // Use dItem.valueOf on the entry values to ensure
                // correct comparison
                dItem valueN = entry.getValue().get(n);
                dItem matrixN = matrix.length <= n || matrix[n] == null ? new dItem(Material.AIR) : new dItem(matrix[n].clone());

                // If one's an item script and the other's not, it's a fail
                if (valueN.isItemscript() != matrixN.isItemscript()) {
                    continue master;
                }
                // If they're both item scripts, and they are different scripts, it's a fail
                if (valueN.isItemscript() && matrixN.isItemscript()) {
                    if (!valueN.getScriptName().equalsIgnoreCase(matrixN.getScriptName())) {
                        continue master;
                    }
                }
                // If they're both not item scripts, and the materials are different, it's a fail
                else if (!valueN.getMaterial().matchesMaterialData(matrixN.getMaterial().getMaterialData())) {
                    continue master;
                }
            }

            // If all the items match, return the special recipe's dItem key
            return entry.getKey().getItemFrom(dPlayer.mirrorBukkitPlayer(player), null);
        }

        primary:
        for (Map.Entry<ItemScriptContainer, List<dItem>> entry :
                ItemScriptContainer.shapelessRecipesMap.entrySet()) {
            for (int i = 0; i < entry.getValue().size(); i++) {
                if (!containsAny(entry.getValue().get(i), matrix)) {
                    continue primary;
                }
            }
            return entry.getKey().getItemFrom(dPlayer.mirrorBukkitPlayer(player), null);
        }

        return null;
    }

    public boolean containsAny(dItem item, ItemStack[] matrix) {
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
    public boolean emulateSpecialRecipeResultShiftClick(CraftingInventory inventory, Player player) {

        // Store the crafting matrix
        ItemStack[] matrix = inventory.getMatrix();
        for (int i = 0; i < matrix.length; i++) {
            matrix[i] = matrix[i].clone();
        }

        // Get the result of the special recipe that this matrix matches,
        // if any
        dItem result = getSpecialRecipeResult(matrix, player);

        // Proceed only if the result was not null
        if (result != null) {

            // In a shift click, the amount of the resulting dItem should
            // be based on the amount of the least numerous ingredient,
            // so track it
            int lowestAmount = 0;

            // Set lowestAmount to the amount of the first item found,
            // then set it to that of the ingredient with the lowest amount
            // found that isn't zero
            for (int n = 0; n < matrix.length; n++) {
                if ((matrix[n].getAmount() > 0 &&
                        matrix[n].getAmount() < lowestAmount) || lowestAmount == 0) {
                    lowestAmount = matrix[n].getAmount();
                }
            }

            // Deduct that amount from every ingredient in the matrix
            for (int n = 0; n < matrix.length; n++) {
                if (matrix[n].getAmount() > 0) {
                    matrix[n].setAmount(matrix[n].getAmount() - lowestAmount);
                    if (matrix[n].getAmount() <= 0) {
                        matrix[n] = null;
                    }
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

                inventory.setMatrix(matrix);

                // Set the crafting's result
                inventory.setResult(resultStack);

                // Update the player's inventory
                //
                // TODO: Replace with non-deprecated method once one is added to Bukkit
                player.updateInventory();
            }
            return true;
        }
        return false;
    }

    @EventHandler
    public void boundInventoryClickEvent(InventoryClickEvent event) {
        // Proceed only if this is a CraftingInventory
        if (!(event.getInventory() instanceof CraftingInventory)) {
            return;
        }

        // Proceed only if the click has a cursor item that is bound
        ItemStack item = event.getCursor();
        if (item == null || !isBound(item)) {
            return;
        }

        if (event.getInventory().getType() != InventoryType.PLAYER) {
            event.setCancelled(true);
            return;
        }

        if (!((Player) event.getInventory().getHolder()).getName().equalsIgnoreCase(event.getWhoClicked().getName())) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler
    public void boundInventoryDragEvent(InventoryDragEvent event) {
        // Proceed only if this is a CraftingInventory
        if (!(event.getInventory() instanceof CraftingInventory)) {
            return;
        }

        // Proceed only if the items are bound
        ItemStack item = event.getOldCursor();
        if (item == null || !isBound(item)) {
            return;
        }

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
