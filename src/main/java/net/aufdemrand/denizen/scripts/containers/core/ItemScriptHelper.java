package net.aufdemrand.denizen.scripts.containers.core;

import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.scripts.ScriptBuilder;
import net.aufdemrand.denizen.scripts.ScriptEntry;
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
    	if (!item.hasItemMeta()) return false;
    	if (!item.getItemMeta().hasLore()) return false;
    	for (String line : item.getItemMeta().getLore())
    		if (dScript.matches(ChatColor.stripColor(line).substring(3))) {
    			return true;
    		}
    	return false;
    }
    
    @EventHandler
    public void boundPrepareItem(PrepareItemCraftEvent event) {
    	List<ItemStack> clonedMatrix = new ArrayList<ItemStack>();
    	for (ItemStack stack : event.getInventory().getMatrix()) {
    		clonedMatrix.add(stack);
    	}
    	for (ItemStack stack : clonedMatrix) {
    		if (!stack.hasItemMeta()) return;
    		if (!stack.getItemMeta().hasLore()) return;
    		for (String line : stack.getItemMeta().getLore()) {
    			if (dScript.matches(ChatColor.stripColor(line).substring(3))
    					&& dScript.valueOf(ChatColor.stripColor(line).substring(3)).getContainer().getAsContainerType(ItemScriptContainer.class).bound) {
    				clonedMatrix.remove(stack);
    				event.getView().getPlayer().getInventory().addItem(stack);
    				break;
    			}
    		}
    	}
    	event.getInventory().setMatrix((ItemStack[]) clonedMatrix.toArray());
    }
    
    @EventHandler
    public void boundDropItem(PlayerDropItemEvent event) {
    	if (!event.getItemDrop().getItemStack().hasItemMeta()) return;
    	if (!event.getItemDrop().getItemStack().getItemMeta().hasLore()) return;
    	for (String line : event.getItemDrop().getItemStack().getItemMeta().getLore()) {
    		if (dScript.matches(ChatColor.stripColor(line).substring(3))
					&& dScript.valueOf(ChatColor.stripColor(line).substring(3)).getContainer().getAsContainerType(ItemScriptContainer.class).bound) {
    			event.setCancelled(true);
    			break;
    		}
    	}
    }
    
    @EventHandler
    public void craftItem(CraftItemEvent event) {
    	if (isItemScript(event.getRecipe().getResult())) {
    		if (!(event.getWhoClicked() instanceof Player)) return;
            String determination = doEvents(Arrays.asList
            		("craft"),
            		null, (Player) event.getWhoClicked(), null);
            if (determination.toUpperCase().startsWith("CANCELLED"))
            	event.setCancelled(true);
    	}
    }
    
    @EventHandler
    public void dropItem(PlayerDropItemEvent event) {
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
