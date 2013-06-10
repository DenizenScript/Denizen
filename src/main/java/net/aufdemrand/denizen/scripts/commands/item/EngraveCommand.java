package net.aufdemrand.denizen.scripts.commands.item;

import java.util.HashMap;
import java.util.Map;

import net.aufdemrand.denizen.utilities.nbt.CustomNBT;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;

/*
 * Denizen dScript ENGRAVE command:
 * 
 * Engraves an item in the player's hand. Engraved items are bound to their engraver and cannot be picked
 * up by other Players.
 * 
 */

public class EngraveCommand extends AbstractCommand implements Listener {

    /* ENGRAVE (REMOVE|ADD) (TARGET:target_name)

    /* Arguments: [] - Required, () - Optional 
     * 
     * Example Usage:
     */
    
    private enum Action { ADD, REMOVE }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        // Initialize fields
        Action action = Action.ADD;
        String target = scriptEntry.getPlayer().getName();
        ItemStack item = scriptEntry.getPlayer().getPlayerEntity().getItemInHand() != null ? scriptEntry.getPlayer().getPlayerEntity().getItemInHand(): null;

        for (String arg : scriptEntry.getArguments()) {
            if (aH.matchesArg("ADD, REMOVE, REMOVEALL", arg)) {
                action = Action.valueOf(aH.getStringFrom(arg).toUpperCase());
            } else if (aH.matchesItem(arg)) {
                item = aH.getItemFrom(arg).getItemStack();
            } else throw new InvalidArgumentsException(Messages.ERROR_UNKNOWN_ARGUMENT, arg);
        }
        
        if (item == null) throw new InvalidArgumentsException(Messages.ERROR_MISSING_OTHER, "ITEM");
        if (target == null) throw new InvalidArgumentsException(Messages.ERROR_NO_PLAYER);

        // Add objects that need to be passed to execute() to the scriptEntry
        scriptEntry.addObject("action", action)
                    .addObject("target", target)
                    .addObject("item", item);
    }
    
    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {
        // Fetch objects
        Action action = (Action) scriptEntry.getObject("action");
        String target = String.valueOf(scriptEntry.getObject("target"));
        ItemStack item = (ItemStack) scriptEntry.getObject("item");

        // Report to dB
        dB.report(getName(),
                aH.debugObj("Action", action.toString())
                        + aH.debugObj("Target", target)
                        + aH.debugObj("Item", item.getType().toString()));

        switch (action) {
        case ADD:
            dB.echoDebug("Engraving '" + item.getType() + "' with an inscription of '" + target + "'.");
            CustomNBT.addCustomNBT(item, "owner", target);
            dB.echoDebug(CustomNBT.getCustomNBT(item, "owner"));
            
            return;
        case REMOVE: 
            dB.echoDebug("Removing engraving on '" + item.getType() + "'.");            
            CustomNBT.removeCustomNBT(item, "owner");
            return;            
        }
    }
    
    
    // Map to keep track of notification cooldowns (String playerName, Long timeout)
    Map<String, Long> notifyCooldown = new HashMap<String, Long>();
    
    /*
     * Checks the owner of the picked up item, if any. Will not let the Player pick up
     * the item if they are not the owner of the engraved item.
     * 
     * If sneaking, a message will let the Player know that it is engraved.
     * 
     */
    
    @EventHandler
    public void checkOwner(PlayerPickupItemEvent event) {
        ItemStack item = event.getItem().getItemStack();
        if (CustomNBT.hasCustomNBT(item, "owner")
                && !CustomNBT.getCustomNBT(item, "owner").equalsIgnoreCase(event.getPlayer().getName())) {
            dB.echoDebug(CustomNBT.getCustomNBT(item, "owner"));

            // See why item isn't being picked up if sneaking.
            if (event.getPlayer().isSneaking()) {
                // Check cooldown to avoid spam from multiple items/event firings
                if (!notifyCooldown.containsKey(event.getPlayer().getName())) {
                    // If not cooled down, set cool-down and alert Player they cannot pick it up.
                    notifyCooldown.put(event.getPlayer().getName(), System.currentTimeMillis() + 10000);
                    event.getPlayer().sendMessage("That " + event.getItem().getItemStack().getType() + " does not belong to you!");
                } else {
                    // If cooled down, remove the cooldown. 
                    if (notifyCooldown.get(event.getPlayer().getName()) < System.currentTimeMillis()) 
                        notifyCooldown.remove(event.getPlayer().getName());
                }
            }
            // If engraved, and not the engraved Player, cancel the pickup event.
            event.setCancelled(true);
        }
        // Otherwise, don't interfere.
    }

    
    
    // Map to keep track of despawn delays. (Integer entityID, Long timeout)
    Map<Integer, Long> despawnDelay = new HashMap<Integer, Long>();

    /*
     * An engraved item will take longer to despawn in the world, allowing a little bit of extra time
     * for the player to retrieve it.
     * 
     */
    @EventHandler
    public void stopDespawn(ItemDespawnEvent event) {
        ItemStack item = event.getEntity().getItemStack();
        // Check if the item has an engraving, otherwise carry on.
        if (CustomNBT.hasCustomNBT(item, "owner")) {
            dB.echoDebug(CustomNBT.getCustomNBT(item, "owner"));

            // If in the delay map
            if (despawnDelay.containsKey(event.getEntity().getEntityId())) {
                // If not cooled, cancel despawn.
                if (despawnDelay.get(event.getEntity().getEntityId()) < System.currentTimeMillis())
                    event.setCancelled(true);
                else 
                    // If cooled, remove from map.
                    dB.echoDebug("Removed an ENGRAVED '" + item.getType().name() + "' which belonged to '" + CustomNBT.getCustomNBT(item, "owner") + "'.");
                    despawnDelay.remove(event.getEntity().getEntityId());
            } else {
                // If not in delay map, add to delay map and cancel despawn.
                event.setCancelled(true);
                dB.echoDebug("Stopped despawn of an ENGRAVED '" + item + "' which belonged to '" + CustomNBT.getCustomNBT(item, "owner") + "'. Will remove from world in 10 minutes.");
                despawnDelay.put(event.getEntity().getEntityId(), System.currentTimeMillis() + (1000 * 60 * 10));
            }
        }
    }
    

    @Override
    public void onEnable() {
        denizen.getServer().getPluginManager().registerEvents(this, denizen);
    }
}
