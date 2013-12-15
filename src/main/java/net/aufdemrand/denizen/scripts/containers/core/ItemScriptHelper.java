package net.aufdemrand.denizen.scripts.containers.core;

import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.scripts.ScriptBuilder;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.core.DetermineCommand;
import net.aufdemrand.denizen.scripts.queues.core.InstantQueue;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ItemScriptHelper implements Listener {

    public static Map<String, ItemScriptContainer> item_scripts = new ConcurrentHashMap<String, ItemScriptContainer>(8, 0.9f, 1);

    public ItemScriptHelper() {
        DenizenAPI.getCurrentInstance().getServer().getPluginManager()
                .registerEvents(this, DenizenAPI.getCurrentInstance());
    }


    /////////////////////
    //   EVENT HANDLER
    /////////////////

    public static String doEvents(List<String> eventNames, dNPC npc, Player player, Map<String, Object> context) {

        String determination = "none";

        for (ItemScriptContainer script : item_scripts.values()) {

            if (script == null) continue;

            for (String eventName : eventNames) {

                if (!script.contains("EVENTS.ON " + eventName.toUpperCase())) continue;

                List<ScriptEntry> entries = script.getEntries
                        (player != null ? new dPlayer(player) : null,
                         npc, "events.on " + eventName);
                if (entries.isEmpty()) continue;

                dB.report(script, "Event",
                        aH.debugObj("Type", "On " + eventName)
                        + script.getAsScriptArg().debug()
                        + (npc != null ? aH.debugObj("NPC", npc.toString()) : "")
                        + (player != null ? aH.debugObj("Player", player.getName()) : "")
                        + (context != null ? aH.debugObj("Context", context.toString()) : ""));

                dB.echoDebug(script, dB.DebugElement.Header, "Building event 'On " + eventName.toUpperCase() + "' for " + script.getName());

                if (context != null) {
                    for (Map.Entry<String, Object> entry : context.entrySet()) {
                        ScriptBuilder.addObjectToEntries(entries, entry.getKey(), entry.getValue());
                    }
                }

                // Create new ID -- this is what we will look for when determining an outcome
                long id = DetermineCommand.getNewId();

                // Add the reqId to each of the entries
                ScriptBuilder.addObjectToEntries(entries, "ReqId", id);
                InstantQueue.getQueue(null).addEntries(entries).start();

                if (DetermineCommand.hasOutcome(id))
                    determination =  DetermineCommand.getOutcome(id);
                }
        }

        return determination;
    }

    // Remove all recipes added by Denizen
    public static void removeDenizenRecipes() {

        try {
        // Remove regular Bukkit recipes added by Denizen
        Iterator<Recipe> recipes = Bukkit.getServer().recipeIterator();
        while (recipes.hasNext()) {
            Recipe current = recipes.next();

            if (isItemscript(current.getResult())) {
                recipes.remove();
            }
        }

        // Remove special recipes stored by Denizen
        ItemScriptContainer.specialrecipesMap.clear();
        }
        catch (Throwable e) {
            // Disable until bukkit fixes the issue, no sense worrying people about
            // something we can't control.
            // dB.echoError(e);
        }
    }

    public static boolean isItemscript(ItemStack item) {
        if (item.hasItemMeta() && item.getItemMeta().hasLore()) {
            for (String itemLore : item.getItemMeta().getLore()) {
                if (itemLore.startsWith(dItem.itemscriptIdentifier)) {
                    return true;
                }
            }
        }

        return false;
    }

    // When special Denizen recipes that have itemscripts as ingredients
    // are being used, check crafting matrix for recipe matches whenever
    // clicks are made in CRAFTING or RESULT slots
    @EventHandler
    public void specialRecipeClick(InventoryClickEvent event) {

        // Proceed only if at least one special recipe has been stored
        if (ItemScriptContainer.specialrecipesMap.isEmpty())
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
        if (ItemScriptContainer.specialrecipesMap.isEmpty())
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

                    // If this was a valid match, set the crafting's result
                    inventory.setResult(result.getItemStack());

                    // Update the player's inventory
                    //
                    // TODO: Replace with non-deprecated method once one
                    // is added to Bukkit
                    player.updateInventory();
                }
            }
        }, 1);
    }

    // Check if a CraftingInventory's crafting matrix matches a special
    // recipe and return that recipe's dItem result if it does
    public dItem getSpecialRecipeResult(ItemStack[] matrix) {

        // Iterate through all the special recipes
        for (Map.Entry<dItem, dList> entry :
                ItemScriptContainer.specialrecipesMap.entrySet()) {

            boolean matchesSpecialRecipe = true;

            // Check if the two sets of items match each other
            for (int n = 0; n < matrix.length - 1; n++) {

                // Use dItem.valueOf on the entry values to ensure
                // correct comparison
                if (!dItem.valueOf(entry.getValue().get(n)).identify()
                        .equals(new dItem(matrix[n]).identify())) {

                    // If the current item does not match, set the
                    // boolean to false
                    matchesSpecialRecipe = false;
                    break;
                }
            }

            // If all the items match, return the special recipe's dItem key
            if (matchesSpecialRecipe) return entry.getKey();
        }

        return null;
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
                resultStack.setAmount(lowestAmount);

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
    public void boundPrepareItem(PrepareItemCraftEvent event) {
        // Since the crafting matrix uses an array, we need a cloned version as a list
        List<ItemStack> clonedMatrix = new ArrayList<ItemStack>(Arrays.asList(event.getInventory().getMatrix()));
        // Now that we have all of the items, we need to make sure one of them is bound
        for (int i=0; i < clonedMatrix.size(); i++) {
            ItemStack stack = clonedMatrix.get(i);
            if (stack == null) continue;
            // We need to check this manually, since the event is a bit different than others
            if (!stack.hasItemMeta()) continue;
            if (!stack.getItemMeta().hasLore()) continue;
            for (String line : stack.getItemMeta().getLore()) {
                // Make sure it's an item script AND that's it's bound to the player
                if (ChatColor.stripColor(line).substring(0, 3).equalsIgnoreCase("id:")
                        && dScript.matches(ChatColor.stripColor(line).substring(3))
                        && dScript.valueOf(ChatColor.stripColor(line).substring(3)).getContainer().getAsContainerType(ItemScriptContainer.class).bound) {
                    // If it's a bound item, don't let it get away!
                    clonedMatrix.remove(stack);
                    event.getView().getPlayer().getInventory().addItem(stack);
                    break;
                }
            }
        }
        // Now, return the modified matrix back to the crafting screen
        event.getInventory().setMatrix(clonedMatrix.toArray(new ItemStack[clonedMatrix.size()]));
    }

    @EventHandler
    public void boundDropItem(PlayerDropItemEvent event) {

        // If the item has no ItemMeta or lore, ignore it.
        if (!event.getItemDrop().getItemStack().hasItemMeta()
                || !event.getItemDrop().getItemStack().getItemMeta().hasLore()
                || event.getItemDrop().getItemStack().getItemMeta().getLore().isEmpty())
            return;

        for (String line : event.getItemDrop().getItemStack().getItemMeta().getLore()) {
            // If the item being dropped is bound, don't drop it.
            if (line.startsWith("§0id:")
                    && dScript.matches(line.replace("§0id:", ""))
                    && dScript.valueOf(line.replace("§0id:", "")).getContainer().getAsContainerType(ItemScriptContainer.class).bound) {
                event.setCancelled(true);
                break;
            }
        }

    }

    @EventHandler
    public void dropItem(PlayerDropItemEvent event) {
        // Run a script on drop of an item script
        if (isItemscript(event.getItemDrop().getItemStack())) {
            Map<String, Object> context = new HashMap<String, Object>();
            context.put("location", new dLocation(event.getItemDrop().getLocation()));
            String determination = doEvents(Arrays.asList
                    ("drop"),
                    null, event.getPlayer(), context);
            if (determination.toUpperCase().startsWith("CANCELLED"))
                event.setCancelled(true);
        }
    }
}
