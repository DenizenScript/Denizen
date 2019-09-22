package com.denizenscript.denizen.scripts.containers.core;

import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.utilities.DenizenAPI;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.events.bukkit.ScriptReloadEvent;
import com.denizenscript.denizen.events.player.PlayerCraftsItemScriptEvent;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.tags.BukkitTagContext;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.core.ScriptTag;
import com.denizenscript.denizencore.scripts.ScriptBuilder;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.YamlConfiguration;
import com.denizenscript.denizencore.utilities.text.StringHolder;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.*;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

public class ItemScriptHelper implements Listener {

    public static final Map<String, ItemScriptContainer> item_scripts = new HashMap<>();
    public static final Map<String, ItemScriptContainer> item_scripts_by_hash_id = new HashMap<>();
    public static final Map<String, ItemScriptContainer> recipeIdToItemScript = new HashMap<>();

    public ItemScriptHelper() {
        DenizenAPI.getCurrentInstance().getServer().getPluginManager()
                .registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    public static void removeDenizenRecipes() {
        recipeIdToItemScript.clear();
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13)) {
            NMSHandler.getItemHelper().clearDenizenRecipes();
        }
        else {
            specialrecipesMap.clear();
            shapelessRecipesMap.clear();
        }
    }

    public String getIdFor(ItemScriptContainer container, String type, int id) {
        String basicId = type + "_" + Utilities.cleanseNamespaceID(container.getName()) + "_" + id;
        if (!recipeIdToItemScript.containsKey(basicId)) {
            recipeIdToItemScript.put("denizen:" + basicId, container);
            return basicId;
        }
        int newNumber = 1;
        String newId = basicId + "_1";
        while (recipeIdToItemScript.containsKey(newId)) {
            newId = basicId + "_" + newNumber++;
        }
        recipeIdToItemScript.put("denizen:" + newId, container);
        return newId;
    }

    public void registerShapedRecipe(ItemScriptContainer container, List<String> recipeList, String internalId, String group) {
        for (int n = 0; n < recipeList.size(); n++) {
            recipeList.set(n, TagManager.tag(ScriptBuilder.stripLinePrefix(recipeList.get(n)), new BukkitTagContext(container.player, container.npc, new ScriptTag(container))));
        }
        List<ItemTag> ingredients = new ArrayList<>();
        int width = 1;
        for (String recipeRow : recipeList) {
            String[] elements = recipeRow.split("\\|", 3);
            if (width < 3 && elements.length == 3) {
                width = 3;
            }
            if (width < 2 && elements.length >= 2) {
                width = 2;
            }

            for (String element : elements) {
                ItemTag ingredient = ItemTag.valueOf(element.replaceAll("[iImM]@", ""), container);
                if (ingredient == null) {
                    Debug.echoError("Invalid ItemTag ingredient, recipe will not be registered for item script '"
                            + container.getName() + "': " + element);
                    return;
                }
                ingredients.add(ingredient);
            }
        }
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13)) {
            NamespacedKey key = new NamespacedKey("denizen", internalId);
            ShapedRecipe recipe = new ShapedRecipe(key, container.getCleanReference().getItemStack());
            recipe.setGroup(group);
            String shape1 = "ABC".substring(0, width);
            String shape2 = "DEF".substring(0, width);
            String shape3 = "GHI".substring(0, width);
            String itemChars = shape1 + shape2 + shape3;
            if (recipeList.size() == 3) {
                recipe = recipe.shape(shape1, shape2, shape3);
            }
            else if (recipeList.size() == 2) {
                recipe = recipe.shape(shape1, shape2);
            }
            else {
                recipe = recipe.shape(shape1);
            }
            for (int i = 0; i < ingredients.size(); i++) {
                NMSHandler.getItemHelper().setShapedRecipeIngredient(recipe, itemChars.charAt(i), ingredients.get(i).getItemStack().clone());
            }
            Bukkit.addRecipe(recipe);
        }
        else {
            specialrecipesMap.put(container, ingredients);
        }
    }

    public void registerShapelessRecipe(ItemScriptContainer container, String shapelessString, String internalId, String group) {
        String list = TagManager.tag(shapelessString, new BukkitTagContext(container.player, container.npc, new ScriptTag(container)));
        List<ItemTag> ingredients = new ArrayList<>();
        for (String element : ListTag.valueOf(list)) {
            ItemTag ingredient = ItemTag.valueOf(element.replaceAll("[iImM]@", ""), container);
            if (ingredient == null) {
                Debug.echoError("Invalid ItemTag ingredient, shapeless recipe will not be registered for item script '"
                        + container.getName() + "': " + element);
                return;
            }
            ingredients.add(ingredient);
        }
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13)) {
            ItemStack result = container.getCleanReference().getItemStack().clone();
            ItemStack[] input = new ItemStack[ingredients.size()];
            for (int i = 0; i < input.length; i++) {
                input[i] = ingredients.get(i).getItemStack().clone();
            }
            NMSHandler.getItemHelper().registerShapelessRecipe(internalId, group, result, input);
        }
        else {
            shapelessRecipesMap.put(container, ingredients);
        }
    }

    public void registerFurnaceRecipe(ItemScriptContainer container, String furnaceItemString, float exp, int time, String type, String internalId, String group) {
        ItemTag furnace_item = ItemTag.valueOf(furnaceItemString, container);
        if (furnace_item == null) {
            Debug.echoError("Invalid item '" + furnaceItemString + "', furnace recipe will not be registered for item script '" + container.getName() + "'.");
            return;
        }
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13)) {
            ItemStack result = container.getCleanReference().getItemStack().clone();
            ItemStack input = furnace_item.getItemStack().clone();
            NMSHandler.getItemHelper().registerFurnaceRecipe(internalId, group, result, input, exp, time, type);
        }
        else {
            FurnaceRecipe recipe = new FurnaceRecipe(container.getCleanReference().getItemStack(), furnace_item.getMaterial().getMaterial(), furnace_item.getItemStack().getDurability());
            Bukkit.addRecipe(recipe);
            currentFurnaceRecipes.put(container, furnace_item);
        }
    }

    public void registerStonecuttingRecipe(ItemScriptContainer container, String inputItemString, String internalId, String group) {
        if (!NMSHandler.getVersion().isAtLeast(NMSVersion.v1_14)) {

        }
        ItemTag furnace_item = ItemTag.valueOf(inputItemString, container);
        if (furnace_item == null) {
            Debug.echoError("Invalid item '" + inputItemString + "', stonecutting recipe will not be registered for item script '" + container.getName() + "'.");
            return;
        }
        ItemStack result = container.getCleanReference().getItemStack().clone();
        ItemStack input = furnace_item.getItemStack().clone();
        NMSHandler.getItemHelper().registerStonecuttingRecipe(internalId, group, result, input);
    }

    public void rebuildRecipes() {
        currentFurnaceRecipes.clear();
        for (ItemScriptContainer container : item_scripts.values()) {
            if (container.contains("recipes")) {
                YamlConfiguration section = container.getConfigurationSection("recipes");
                int id = 0;
                for (StringHolder key : section.getKeys(false)) {
                    id++;
                    YamlConfiguration subSection = section.getConfigurationSection(key.str);
                    String type = CoreUtilities.toLowerCase(subSection.getString("type"));
                    String internalId = subSection.contains("recipe_id") ? subSection.getString("recipe_id") : getIdFor(container, type + "_recipe", id);
                    String group = subSection.contains("group") ? subSection.getString("group") : "";
                    if (type.equals("shaped")) {
                        registerShapedRecipe(container, subSection.getStringList("input"), internalId, group);
                    }
                    else if (type.equals("shapeless")) {
                        registerShapelessRecipe(container, subSection.getString("input"), internalId, group);
                    }
                    else if (type.equals("stonecutting")) {
                        registerStonecuttingRecipe(container, subSection.getString("input"), internalId, group);
                    }
                    else if (type.equals("furnace") || type.equals("blast") || type.equals("smoker") || type.equals("campfire")) {
                        float exp = 0;
                        int cookTime = 40;
                        if (subSection.contains("experience")) {
                            exp = Float.valueOf(subSection.getString("experience"));
                        }
                        if (subSection.contains("cook_time")) {
                            cookTime = DurationTag.valueOf(subSection.getString("cook_time")).getTicksAsInt();
                        }
                        registerFurnaceRecipe(container, subSection.getString("input"), exp, cookTime, type, internalId, group);
                    }
                }
            }
            // Old script style
            if (container.contains("RECIPE")) {
                registerShapedRecipe(container, container.getStringList("RECIPE"), getIdFor(container, "old_recipe", 0), "custom");
            }
            if (container.contains("SHAPELESS_RECIPE")) {
                registerShapelessRecipe(container, container.getString("SHAPELESS_RECIPE"), getIdFor(container, "old_shapeless", 0), "custom");
            }
            if (container.contains("FURNACE_RECIPE")) {
                registerFurnaceRecipe(container, container.getString("FURNACE_RECIPE"), 0, 40, "furnace", getIdFor(container, "old_furnace", 0), "custom");
            }
        }
    }


    @EventHandler
    public void scriptReload(ScriptReloadEvent event) {
        rebuildRecipes();
    }

    public static boolean isItemscript(ItemStack item) {
        return getItemScriptContainer(item) != null;
    }

    public static ItemScriptContainer getItemScriptContainer(ItemStack item) {
        if (item == null) {
            return null;
        }
        String nbt = NMSHandler.getItemHelper().getNbtData(item).getString("Denizen Item Script");
        if (nbt != null && !nbt.equals("")) {
            return item_scripts_by_hash_id.get(nbt);
        }
        if (!item.hasItemMeta() || !item.getItemMeta().hasLore()) {
            return null;
        }
        for (String itemLore : item.getItemMeta().getLore()) {
            if (itemLore.startsWith(ItemTag.itemscriptIdentifier)) {
                return item_scripts.get(itemLore.replace(ItemTag.itemscriptIdentifier, ""));
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
            byte[] bytes = script.getBytes(StandardCharsets.UTF_8);
            md.update(bytes, 0, bytes.length);
            String hash = new BigInteger(1, md.digest()).toString(16);
            for (int i = 0; i < 16; i++) {
                colors.append(ChatColor.COLOR_CHAR).append(hash.charAt(i));
            }
        }
        catch (Exception ex) {
            Debug.echoError(ex);
            colors.append(ChatColor.BLUE);
        }
        return colors.toString();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////// All the below is for the legacy crafting system, which is still used for 1.12 and some recipe types! ///////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    public static Map<ItemScriptContainer, List<ItemTag>> specialrecipesMap = new HashMap<>();
    public static Map<ItemScriptContainer, List<ItemTag>> shapelessRecipesMap = new HashMap<>();
    public Map<ItemScriptContainer, ItemTag> currentFurnaceRecipes = new HashMap<>();

    @EventHandler
    public void furnaceSmeltHandler(FurnaceSmeltEvent event) {
        if (isItemscript(event.getResult())) {
            ItemScriptContainer isc = getItemScriptContainer(event.getResult());
            ItemTag convertResult = currentFurnaceRecipes.get(isc);
            if (convertResult == null) {
                return;
            }
            ItemTag itm = new ItemTag(convertResult.getItemStack().clone());
            itm.setAmount(1);
            ItemTag src = new ItemTag(event.getSource().clone());
            src.setAmount(1);
            if (!itm.getFullString().equals(src.getFullString())) {
                List<Recipe> recipes = Bukkit.getServer().getRecipesFor(event.getSource());
                for (Recipe rec : recipes) {
                    if (rec instanceof FurnaceRecipe) {
                        event.setResult(rec.getResult());
                        return;
                    }
                }
                event.setCancelled(true);
                return;
            }
        }
    }

    // When special Denizen recipes that have itemscripts as ingredients
    // are being used, check crafting matrix for recipe matches whenever
    // clicks are made in CRAFTING or RESULT slots
    @EventHandler
    public void specialRecipeClick(InventoryClickEvent event) {
        // Proceed only if at least one special recipe has been stored
        if (specialrecipesMap.isEmpty() && shapelessRecipesMap.isEmpty()) {
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
            Map.Entry<ItemScriptContainer, List<ItemTag>> recipeEntry = null;

            if (slotType == SlotType.RESULT && inventory.getResult() != null
                    && inventory.getResult().getData().getItemType() != Material.AIR) {

                // Proceed only if the player can fit more items on their cursor
                if (!event.isShiftClick() && event.getCursor().getData().getItemType() != Material.AIR
                        && (!event.getCursor().isSimilar(inventory.getResult())
                        || event.getCursor().getAmount() + inventory.getResult().getAmount() > event.getCursor().getMaxStackSize())) {
                    return;
                }

                // Couldn't match a recipe script to the result
                recipeEntry = getSpecialRecipeEntry(inventory.getMatrix());
                if (recipeEntry == null) {
                    return;
                }

                PlayerCraftsItemScriptEvent scriptEvent = PlayerCraftsItemScriptEvent.instance;
                scriptEvent.inventory = inventory;
                scriptEvent.result = new ItemTag(inventory.getResult());
                ListTag recipeList = new ListTag();
                for (ItemStack item : inventory.getMatrix()) {
                    if (item != null) {
                        recipeList.add(new ItemTag(item.clone()).identify());
                    }
                    else {
                        recipeList.add(new ItemTag(Material.AIR).identify());
                    }
                }
                scriptEvent.recipe = recipeList;
                scriptEvent.player = PlayerTag.mirrorBukkitPlayer(player);
                scriptEvent.resultChanged = false;
                scriptEvent.cancelled = false;
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
                removeFromEachSlot(inventory, recipeEntry.getValue(), player);
            }
        }
    }

    public void removeFromEachSlot(final CraftingInventory inventory, final List<ItemTag> recipe, final Player player) {
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
                                    matrix[i].setAmount(matrix[i].getAmount() - (recipe != null ? recipe.get(i).getAmount() : 1));
                                    if (matrix[i].getAmount() <= 0) {
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
        if (specialrecipesMap.isEmpty() && shapelessRecipesMap.isEmpty()) {
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
        ItemTag result1 = getSpecialRecipeResult(matrix1, player);

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
                        ItemTag result = getSpecialRecipeResult(matrix, player);

                        // Proceed only if the result was not null
                        if (result != null) {
                            ListTag recipeList = new ListTag();
                            for (ItemStack item : matrix) {
                                if (item != null) {
                                    recipeList.add(new ItemTag(item).identify());
                                }
                                else {
                                    recipeList.add(new ItemTag(Material.AIR).identify());
                                }
                            }

                            /*ItemRecipeFormedScriptEvent event = ItemRecipeFormedScriptEvent.instance;
                            event.result = result;
                            event.recipe = recipeList;
                            event.inventory = inventory;
                            event.player = PlayerTag.mirrorBukkitPlayer(player);
                            event.cancelled = false;
                            event.resultChanged = false;
                            event.fire();
                            if (event.cancelled) {
                                inventory.setResult(null);
                            }
                            else*/ {
                                // If this was a valid match, set the crafting's result
                                inventory.setResult(result.getItemStack());
                            }

                            // Update the player's inventory
                            player.updateInventory();
                        }
                    }
                }, 2);

        return returnme;
    }

    public Map.Entry<ItemScriptContainer, List<ItemTag>> getSpecialRecipeEntry(ItemStack[] matrix) {
        // Iterate through all the special recipes
        master:
        for (Map.Entry<ItemScriptContainer, List<ItemTag>> entry : specialrecipesMap.entrySet()) {

            // Check if the two sets of items match each other
            for (int n = 0; n < 9; n++) {

                // Use ItemTag.valueOf on the entry values to ensure
                // correct comparison
                ItemTag valueN = entry.getValue().get(n);
                ItemTag matrixN = matrix.length <= n || matrix[n] == null ? new ItemTag(Material.AIR) : new ItemTag(matrix[n].clone());

                // If one's an item script and the other's not, it's a fail
                if (valueN.isItemscript() != matrixN.isItemscript()) {
                    continue master;
                }

                // If the item's quantity is less than the recipe item's quantity, it's a fail
                if (matrixN.getAmount() < valueN.getAmount()) {
                    continue master;
                }

                // If they're both item scripts, and they are different scripts, it's a fail
                if (valueN.isItemscript() && matrixN.isItemscript()) {
                    if (!valueN.getScriptName().equalsIgnoreCase(matrixN.getScriptName())) {
                        continue master;
                    }
                }

                // If they're both not item scripts, and the materials are different, it's a fail
                if (!valueN.getMaterial().matchesMaterialData(matrixN.getMaterial().getMaterialData())) {
                    continue master;
                }
            }

            // If all the items match, return the special recipe's ItemTag key
            return entry;
        }

        // Forms a shaped recipe from a shapeless recipe
        primary:
        for (Map.Entry<ItemScriptContainer, List<ItemTag>> entry : shapelessRecipesMap.entrySet()) {

            // Clone recipe & matrix so we can remove items from them
            List<ItemStack> entryList = new ArrayList<>();
            List<ItemStack> matrixList = new ArrayList<>();
            for (ItemTag entryItem : entry.getValue()) {
                entryList.add(entryItem.getItemStack().clone());
            }
            for (ItemStack itemStack : matrix) {
                matrixList.add(itemStack != null ? itemStack.clone() : new ItemStack(Material.AIR));
            }

            List<ItemTag> shapedRecipe = new ArrayList<>();
            ItemStack matrixItem;
            ItemStack entryItem;

            // Iterate through each item in the matrix.
            Iterator<ItemStack> mL = matrixList.iterator();
            while (mL.hasNext()) {
                matrixItem = mL.next();

                if (matrixItem != null && matrixItem.getType() != Material.AIR) {
                    boolean matched = false;

                    // Iterate through the remaining shapeless recipe items to see if any match
                    Iterator<ItemStack> eL = entryList.iterator();
                    while (eL.hasNext()) {
                        entryItem = eL.next();

                        // If the items are similar & the matrix has enough, we have a match
                        if (matrixItem.isSimilar(entryItem) && matrixItem.getAmount() >= entryItem.getAmount()) {
                            shapedRecipe.add(new ItemTag(entryItem));
                            mL.remove();
                            eL.remove();
                            matched = true;
                            break;
                        }
                    }
                    // If a matrix item doesn't match any items from the recipe, it's a fail
                    if (!matched) {
                        continue primary;
                    }
                }
                else {
                    shapedRecipe.add(new ItemTag(Material.AIR));
                    mL.remove();
                }
            }

            // If successful, we should have removed every entry from both the matrix and the shapeless recipe
            // Otherwise, it's a fail
            if (!entryList.isEmpty() || !matrixList.isEmpty()) {
                continue;
            }

            // Returns a shaped recipe entry based on the shapeless recipe
            return new AbstractMap.SimpleEntry<>(entry.getKey(), shapedRecipe);
        }

        return null;
    }

    // Check if a CraftingInventory's crafting matrix matches a special
    // recipe and return that recipe's ItemTag result if it does
    public ItemTag getSpecialRecipeResult(ItemStack[] matrix, Player player) {
        Map.Entry<ItemScriptContainer, List<ItemTag>> recipeEntry = getSpecialRecipeEntry(matrix);
        if (recipeEntry != null) {
            return recipeEntry.getKey().getItemFrom(PlayerTag.mirrorBukkitPlayer(player), null);
        }
        return null;
    }

    // Because Denizen special recipes are basically fake recipes,
    // shift clicking the result slot will not work by itself and needs
    // to be emulated like below
    public boolean emulateSpecialRecipeResultShiftClick(CraftingInventory inventory, Player player) {

        // Store the crafting matrix
        ItemStack[] matrix = inventory.getMatrix();
        for (int i = 0; i < matrix.length; i++) {
            matrix[i] = matrix[i] == null ? new ItemStack(Material.AIR) : matrix[i].clone();
        }

        // Get the recipe entry for this matrix, if any
        Map.Entry<ItemScriptContainer, List<ItemTag>> recipeEntry = getSpecialRecipeEntry(matrix);

        // Proceed only if the result was not null
        if (recipeEntry != null) {
            List<ItemTag> recipe = recipeEntry.getValue();
            ItemTag result = recipeEntry.getKey().getItemFrom(PlayerTag.mirrorBukkitPlayer(player), null);

            // In a shift click, the amount of the resulting ItemTag should
            // be based on the amount of the least numerous ingredient multiple,
            // so track it
            int lowestAmount = 0;

            // Set lowestAmount to the amount of the first item found,
            // then set it to that of the ingredient with the lowest multiple
            // found that isn't zero
            for (int n = 0; n < matrix.length; n++) {
                if (matrix[n].getAmount() == 0 || recipe.get(n).getAmount() == 0) {
                    continue;
                }

                if ((matrix[n].getAmount() / recipe.get(n).getAmount() < lowestAmount) || lowestAmount == 0) {
                    lowestAmount = matrix[n].getAmount() / recipe.get(n).getAmount();
                }
            }

            // Deduct that multiple from every ingredient in the matrix
            for (int n = 0; n < matrix.length; n++) {
                if (matrix[n].getAmount() > 0) {
                    matrix[n].setAmount(matrix[n].getAmount() - lowestAmount * recipe.get(n).getAmount());
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
                player.updateInventory();
            }
            return true;
        }
        return false;
    }
}
