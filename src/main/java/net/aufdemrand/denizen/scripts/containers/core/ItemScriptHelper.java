package net.aufdemrand.denizen.scripts.containers.core;

import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.scripts.ScriptBuilder;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.ScriptRegistry;
import net.aufdemrand.denizen.scripts.commands.core.DetermineCommand;
import net.aufdemrand.denizen.scripts.queues.core.InstantQueue;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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

                dB.report("Event",
                        aH.debugObj("Type", "On " + eventName)
                        + script.getAsScriptArg().debug()
                        + (npc != null ? aH.debugObj("NPC", npc.toString()) : "")
                        + (player != null ? aH.debugObj("Player", player.getName()) : "")
                        + (context != null ? aH.debugObj("Context", context.toString()) : ""));

                dB.echoDebug(dB.DebugElement.Header, "Building event 'On " + eventName.toUpperCase() + "' for " + script.getName());

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

    public static boolean isItemScript(ItemStack item) {
        // If the item doesn't have ItemMeta, ignore it.
        if (!item.hasItemMeta()) return false;

        // Since dItems are handled by lore, it must match an item script on one of the lines
        if (!item.getItemMeta().hasLore()) return false;

        // Check to make sure the lore includes id:ItemScriptName. If not, ignore the item.
        for (String line : item.getItemMeta().getLore())
            if (ChatColor.stripColor(line).substring(0, 3).equalsIgnoreCase("id:") &&
                    dScript.matches(ChatColor.stripColor(line).substring(3))) {
                return true;
            }
        return false;
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
        if (!event.getItemDrop().getItemStack().hasItemMeta()) return;
        if (!event.getItemDrop().getItemStack().getItemMeta().hasLore()) return;
        for (String line : event.getItemDrop().getItemStack().getItemMeta().getLore()) {
            // If the item being dropped is bound, don't drop it.
            if (ChatColor.stripColor(line).substring(0, 3).equalsIgnoreCase("id:")
                    && dScript.matches(ChatColor.stripColor(line).substring(3))
                    && dScript.valueOf(ChatColor.stripColor(line).substring(3)).getContainer().getAsContainerType(ItemScriptContainer.class).bound) {
                event.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler
    public void craftItem(CraftItemEvent event) {
        // Run a script on craft of an item script
        if (isItemScript(event.getRecipe().getResult())) {
            if (!(event.getWhoClicked() instanceof Player)) return;

            dItem item = new dItem(event.getRecipe().getResult());
            ItemScriptContainer script = null;
            for (String itemLore : item.getItemStack().getItemMeta().getLore())
                if (itemLore.startsWith("§0id:")) // Note: Update this when the id: is stored less stupidly!
                    script = (ItemScriptContainer) ScriptRegistry.getScriptContainerAs(itemLore.substring(5), ItemScriptContainer.class);

            if (script == null) {
                dB.echoDebug("Tried to craft non-existant script!");
                return;
            }

            for (int i = 0;i < 9;i++) {
                if (!script.getRecipe().get(i).identify().split(":")[0].equalsIgnoreCase(
                (new dItem(event.getInventory().getMatrix()[i])).identify().split(":")[0])) { // This probably can be compared more efficiently...
                    dB.echoDebug("Ignoring craft attempt using "
                            + (new dItem(event.getInventory().getMatrix()[i])).identify().split(":")[0]
                            + " instead of " + script.getRecipe().get(i).identify().split(":")[0]);
                    event.setCancelled(true);
                    return;
                }
            }

            Map<String, Object> context = new HashMap<String, Object>();
            String determination = doEvents(Arrays.asList("craft"),
                                    null, (Player) event.getWhoClicked(), context);

            if (determination.toUpperCase().startsWith("CANCELLED"))
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void dropItem(PlayerDropItemEvent event) {
        // Run a script on drop of an item script
        if (isItemScript(event.getItemDrop().getItemStack())) {
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
