package net.aufdemrand.denizen.listeners.core;

import net.aufdemrand.denizen.listeners.AbstractListener;
import net.aufdemrand.denizen.objects.dCuboid;
import net.aufdemrand.denizen.objects.dInventory;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public class ItemListenerInstance extends AbstractListener implements Listener {

    public enum ItemType {CRAFT, SMELT, FISH}

    ItemType type = null;

    dList items;

    int required = 0;
    int items_so_far = 0;

    String region = null;
    dCuboid cuboid = null;

    @Override
    public void onBuild(List<aH.Argument> args) {

        for (aH.Argument arg : args) {

            if (arg.matchesEnum(ItemType.values()) && type == null) {
                this.type = ItemType.valueOf(arg.getValue().toUpperCase());
            }
            else if (arg.matchesPrefix("qty, q")
                    && arg.matchesPrimitive(aH.PrimitiveType.Integer)) {
                this.required = aH.getIntegerFrom(arg.getValue());
            }
            else if (arg.matchesPrefix("items, item, i, name, names")) {
                items = arg.asType(dList.class);
            }
            else if (arg.matchesPrefix("region, r")) {
                this.region = arg.getValue();
            }
            else if (arg.matchesPrefix("cuboid, c")
                    && arg.matchesArgumentType(dCuboid.class)) {
                this.cuboid = arg.asType(dCuboid.class);
            }
        }

        if (items == null) {
            items = new dList("*");
        }

        if (type == null) {
            dB.echoError("Missing TYPE argument! Valid: CRAFT, SMELT, FISH");
            cancel();
        }
    }

    public void increment(String object, int amount) {
        items_so_far = items_so_far + amount;
        dB.log(ChatColor.YELLOW + "// " + player.getName() + " " +
                CoreUtilities.toLowerCase(type.toString()) + "ed " + amount + " " + object + ".");
        check();
    }

    @EventHandler
    public void listenItem(InventoryClickEvent event) {

        // Proceed if the slot clicked is a RESULT slot and the player is the right one
        if (event.getSlotType().toString().equals("RESULT")
                && event.getWhoClicked() == player.getPlayerEntity()) {

            // If REGION argument specified, check. If not in region, don't count kill!
            if (region != null) {
                //if (!WorldGuardUtilities.inRegion(player.getLocation(), region)) return;
            }

            // Same with the CUBOID argument...
            if (cuboid != null) {
                if (!cuboid.isInsideCuboid(player.getLocation())) {
                    return;
                }
            }

            // Put the type of this inventory in a string and check if it matches the
            // listener's type
            String inventoryType = event.getInventory().getType().toString();
            if ((type == ItemType.CRAFT && (inventoryType.equals("CRAFTING") || inventoryType.equals("WORKBENCH")))
                    || (type == ItemType.SMELT && inventoryType.equals("FURNACE"))) {

                // Get the item in the result slot as an ItemStack
                final ItemStack item = new ItemStack(event.getCurrentItem());

                //if item isn't a required item, then return
                if (!items.contains(CoreUtilities.toLowerCase(item.getType().name()))
                        && !items.contains(String.valueOf(item.getTypeId())) && !items.contains("*")) {
                    return;
                }

                if (event.isShiftClick()) {
                    // Save the quantity of items of this type that the player had
                    // before the event took place
                    final int initialQty = new dInventory(player.getPlayerEntity().getInventory()).count(item, false);

                    // Run a task 1 tick later, after the event has occurred, and
                    // see how many items of this type the player has then in the
                    // inventory
                    Bukkit.getScheduler().scheduleSyncDelayedTask(DenizenAPI.getCurrentInstance(),
                            new Runnable() {
                                @Override
                                public void run() {
                                    int newQty = new dInventory(player.getPlayerEntity().getInventory()).count(item, false);
                                    int difference = newQty - initialQty;

                                    // If any items were obtained (i.e. if shift click was
                                    // used with the player's inventory not being full),
                                    // increase the number of current items
                                    if (difference > 0) {
                                        increment(item.getType().toString(), difference);
                                    }

                                }
                            }, 1);
                }
                else {
                    // If shift click was not used, simply increase the current items
                    // by the quantity of the item in the result slot
                    increment(item.getType().toString(), item.getAmount());
                }

            }
        }
    }

    @EventHandler
    public void listenFish(PlayerFishEvent event) {
        // Only continue if the event is an event for the player that owns this listener.
        if (event.getPlayer() != player.getPlayerEntity()) {
            return;
        }

        // If REGION argument specified, check. If not in region, don't count kill!
        if (region != null) {
            //if (!WorldGuardUtilities.inRegion(player.getLocation(), region)) return;
        }

        // Same with the CUBOID argument...
        if (cuboid != null) {
            if (!cuboid.isInsideCuboid(player.getLocation())) {
                return;
            }
        }

        if (event.getState().toString().equals("CAUGHT_FISH")) {
            increment("FISH", 1);
        }
    }

    @Override
    public void onSave() {
        try {
            store("Type", type.name());
            store("Items", this.items);
            store("Quantity Needed", this.required);
            store("Quantity Done", this.items_so_far);
            store("Region", region);
            if (cuboid != null) {
                store("Cuboid", cuboid.identify());
            }
        }
        catch (Exception e) {
            dB.echoError("Unable to save ITEM listener for '" + player.getName() + "'!");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onLoad() {
        try {
            type = ItemType.valueOf((String) get("Type"));
            items = new dList((List<String>) get("Items"));
            required = (Integer) get("Quantity Needed");
            items_so_far = (Integer) get("Quantity Done");
            region = (String) get("Region");
            cuboid = dCuboid.valueOf((String) get("Cuboid"));
        }
        catch (Exception e) {
            dB.echoError("Unable to load ITEM listener for '" + player.getName() + "'!");
            cancel();
        }
    }

    @Override
    public void onFinish() {

    }

    public void check() {
        if (items_so_far >= required) {
            InventoryClickEvent.getHandlerList().unregister(this);
            PlayerFishEvent.getHandlerList().unregister(this);
            finish();
        }
    }

    @Override
    public void onCancel() {

    }

    @Override
    public String report() {
        return player.getName() + " current has quest listener '" + id
                + "' active and must " + type.name() + " " + Arrays.toString(items.toArray())
                + " '(s). Current progress '" + items_so_far + "/" + required + "'.";
    }

    @Override
    public void constructed() {
        denizen.getServer().getPluginManager().registerEvents(this, denizen);
    }

    @Override
    public void deconstructed() {
        InventoryClickEvent.getHandlerList().unregister(this);
        PlayerFishEvent.getHandlerList().unregister(this);
    }
}
