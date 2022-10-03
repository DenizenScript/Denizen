package com.denizenscript.denizen.scripts.commands.item;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.*;
import com.denizenscript.denizen.scripts.containers.core.InventoryScriptHelper;
import com.denizenscript.denizen.utilities.PaperAPITools;
import com.denizenscript.denizen.utilities.Conversion;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.inventory.InventoryTrackerSystem;
import com.denizenscript.denizen.utilities.inventory.SlotHelper;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizencore.objects.core.*;
import com.denizenscript.denizencore.objects.notable.NoteManager;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.scripts.commands.core.FlagCommand;
import com.denizenscript.denizen.utilities.BukkitImplDeprecations;
import com.denizenscript.denizencore.utilities.data.DataAction;
import com.denizenscript.denizencore.utilities.data.DataActionHelper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.*;

import java.util.AbstractMap;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;

public class InventoryCommand extends AbstractCommand implements Listener {

    public InventoryCommand() {
        setName("inventory");
        setSyntax("inventory [open/close/copy/move/swap/set/keep/exclude/fill/clear/update/adjust <mechanism>:<value>/flag <name>(:<action>)[:<value>] (expire:<time>)] (destination:<inventory>) (origin:<inventory>/<item>|...) (slot:<slot>)");
        setRequiredArguments(1, 6);
        isProcedural = false;
        allowedDynamicPrefixes = true;
        Bukkit.getPluginManager().registerEvents(this, Denizen.instance);
    }

    // <--[language]
    // @name Virtual Inventories
    // @group Inventory System
    // @description
    // Virtual inventories are inventories that have no attachment to anything within the world of Minecraft.
    // They can be used for a wide range of purposes - from looting fallen enemies to serving as interactive menus with item 'buttons'.
    //
    // In Denizen, all noted inventories (saved by the Note command) are automatically converted into a virtual copy of the saved inventory.
    // This enables you to open and edit the items inside freely, with automatic saving, as if it were a normal inventory.
    //
    // Noting is not the only way to create virtual inventories, however.
    // Using 'generic' along with inventory properties will allow you to create temporary custom inventories to do with as you please.
    // The properties that can be used like this are:
    //
    // size=<size>
    // contents=<item>|...
    // title=<title>
    // holder=<inventory type>
    //
    // For example, the following task script opens a virtual inventory with 18 slots,
    // where the second slot is a snowball, all the rest are empty, and the title is "My Awesome Inventory" with some colors in it.
    // <code>
    // open random inventory:
    //   type: task
    //   script:
    //   - inventory open "d:generic[size=18;title=<red>My <green>Awesome <blue>Inventory;contents=air|snowball]"
    // </code>
    //
    // -->

    // <--[command]
    // @Name Inventory
    // @Syntax inventory [open/close/copy/move/swap/set/keep/exclude/fill/clear/update/adjust <mechanism>:<value>/flag <name>(:<action>)[:<value>] (expire:<time>)] (destination:<inventory>) (origin:<inventory>/<item>|...) (slot:<slot>)
    // @Required 1
    // @Maximum 6
    // @Short Edits the inventory of a player, NPC, or chest.
    // @Group item
    //
    // @Description
    // Use this command to edit the state of inventories.
    // By default, the destination inventory is the current attached player's inventory.
    //
    // If you are copying, swapping, removing from (including via "keep" and "exclude"), adding to, moving, or filling inventories,
    // you'll need both destination and origin inventories.
    //
    // Origin inventories may be specified as a list of ItemTags, but destinations must be actual InventoryTags.
    //
    // Using "open", "clear", or "update" only require a destination.
    // "Update" also requires the destination to be a valid player inventory.
    //
    // Using "close" closes any inventory that the currently attached player has opened.
    //
    // The "adjust" option adjusts mechanisms on an item within a specific slot of an inventory (the "slot" parameter is required).
    // Note that this is only for items, it does NOT adjust the inventory itself. Use <@link command adjust> to adjust an inventory mechanism.
    //
    // The "flag" option sets a flag on items, similar to <@link command flag>.
    // See also <@link language flag system>.
    //
    // The "update" option will refresh the client's view of an inventory to match the server's view, which is useful to workaround some sync bugs.
    //
    // Note that to add items to an inventory, you should usually use <@link command give>,
    // and to remove items from an inventory, you should usually use <@link command take>.
    //
    // The slot argument can be any valid slot, see <@link language Slot Inputs>.
    //
    // @Tags
    // <PlayerTag.inventory>
    // <PlayerTag.enderchest>
    // <PlayerTag.open_inventory>
    // <NPCTag.inventory>
    // <LocationTag.inventory>
    //
    // @Usage
    // Use to open a chest inventory, at a location.
    // - inventory open d:<context.location>
    //
    // @Usage
    // Use to open a virtual inventory with a title and some items.
    // - inventory open d:generic[size=27;title=BestInventory;contents=snowball|stick]
    //
    // @Usage
    // Use to open another player's inventory.
    // - inventory open d:<[player].inventory>
    //
    // @Usage
    // Use to remove all items from a chest, except any items in the specified list.
    // - inventory keep d:<context.location.inventory> o:snowball|ItemScript
    //
    // @Usage
    // Use to remove all sticks and stones from the player's inventory.
    // - inventory exclude origin:stick|stone
    //
    // @Usage
    // Use to clear the player's inventory entirely.
    // - inventory clear
    //
    // @Usage
    // Use to swap two players' inventories.
    // - inventory swap d:<[playerOne].inventory> o:<[playerTwo].inventory>
    //
    // @Usage
    // Use to adjust a specific item in the player's inventory.
    // - inventory adjust slot:5 "lore:Item modified!"
    //
    // @Usage
    // Use to set a single stick into slot 10 of the player's inventory.
    // - inventory set o:stick slot:10
    //
    // @Usage
    // Use to set a temporary flag on the player's held item.
    // - inventory flag slot:hand my_target:<player.cursor_on> expire:1d
    // -->

    private enum Action {OPEN, CLOSE, COPY, MOVE, SWAP, ADD, REMOVE, SET, KEEP, EXCLUDE, FILL, CLEAR, UPDATE, ADJUST, FLAG}

    @Override
    public void addCustomTabCompletions(TabCompletionsBuilder tab) {
        tab.add(PropertyParser.propertiesByClass.get(ItemTag.class).propertiesByMechanism.keySet());
        if (tab.arg.contains(":")) {
            Consumer<String> addAll = (s) -> {
                tab.add("o:" + s);
                tab.add("origin:" + s);
                tab.add("d:" + s);
                tab.add("dest:" + s);
                tab.add("destination:" + s);
            };
            for (InventoryTag inventory : (HashSet<InventoryTag>) ((HashSet) NoteManager.notesByType.get(InventoryTag.class))) {
                addAll.accept(inventory.noteName);
            }
            for (String script : InventoryScriptHelper.inventoryScripts.keySet()) {
                addAll.accept(script);
            }
        }
    }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        boolean isAdjust = false, isFlag = false;
        for (Argument arg : scriptEntry) {
            if (!scriptEntry.hasObject("origin")
                    && arg.matchesPrefix("origin", "o", "source", "items", "item", "i", "from", "f")
                    && (arg.matchesArgumentTypes(InventoryTag.class, EntityTag.class, LocationTag.class, MapTag.class)
                    || arg.matchesArgumentList(ItemTag.class))) {
                scriptEntry.addObject("origin", Conversion.getInventory(arg, scriptEntry));
            }
            else if (!scriptEntry.hasObject("destination")
                    && arg.matchesPrefix("destination", "dest", "d", "target", "to", "t")
                    && arg.matchesArgumentTypes(InventoryTag.class, EntityTag.class, LocationTag.class)) {
                scriptEntry.addObject("destination", Conversion.getInventory(arg, scriptEntry));
            }
            else if (!scriptEntry.hasObject("slot")
                    && arg.matchesPrefix("slot", "s")) {
                scriptEntry.addObject("slot", arg.asElement());
            }
            else if (!scriptEntry.hasObject("actions")
                    && arg.matchesEnumList(Action.class)) {
                scriptEntry.addObject("actions", arg.asType(ListTag.class).filter(Action.values()));
                isAdjust = arg.toString().equalsIgnoreCase("adjust");
                isFlag = arg.toString().equalsIgnoreCase("flag");
            }
            else if (!scriptEntry.hasObject("mechanism")
                    && isAdjust) {
                if (arg.hasPrefix()) {
                    scriptEntry.addObject("mechanism", new ElementTag(arg.getPrefix().getValue()));
                    scriptEntry.addObject("mechanism_value", arg.object);
                }
                else {
                    scriptEntry.addObject("mechanism", arg.asElement());
                }
            }
            else if (!scriptEntry.hasObject("expiration")
                    && arg.matchesPrefix("duration", "expire", "expires", "expiration")
                    && isFlag) {
                if (arg.matchesArgumentType(DurationTag.class)) {
                    TimeTag now = TimeTag.now();
                    scriptEntry.addObject("expiration", new TimeTag(now.millis() + arg.asType(DurationTag.class).getMillis(), now.instant.getZone()));
                }
                else if (arg.matchesArgumentType(TimeTag.class)) {
                    scriptEntry.addObject("expiration", arg.asType(TimeTag.class));
                }
                else {
                    arg.reportUnhandled();
                }
            }
            else if (!scriptEntry.hasObject("flag_action")
                    && isFlag) {
                scriptEntry.addObject("flag_action", DataActionHelper.parse(new FlagCommand.FlagActionProvider(), arg, scriptEntry.context));
            }
            else {
                arg.reportUnhandled();
            }
        }
        // Check to make sure required arguments have been filled
        if (!scriptEntry.hasObject("actions")) {
            throw new InvalidArgumentsException("Must specify an Inventory action!");
        }
        if (isAdjust && !scriptEntry.hasObject("mechanism")) {
            throw new InvalidArgumentsException("Inventory adjust must have a mechanism!");
        }
        if (isAdjust && !scriptEntry.hasObject("slot")) {
            throw new InvalidArgumentsException("Inventory adjust must have an explicit slot!");
        }
        if (isFlag && !scriptEntry.hasObject("flag_action")) {
            throw new InvalidArgumentsException("Inventory flag must have a flag action!");
        }
        scriptEntry.defaultObject("slot", new ElementTag(1));
        scriptEntry.defaultObject("destination",
                Utilities.entryHasPlayer(scriptEntry) ?
                        new AbstractMap.SimpleEntry<>(0, Utilities.getEntryPlayer(scriptEntry).getInventory()) : null);
        if (!scriptEntry.hasObject("destination")) {
            throw new InvalidArgumentsException("Must specify a Destination Inventory!");
        }
    }

    public Player currentAltPlayer;
    public Location currentAltLocation;
    public String currentAltTitle, currentAltType;
    public ObjectTag currentAltHolder;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onOpen(InventoryOpenEvent event) {
        if (currentAltHolder == null || currentAltPlayer == null) {
            return;
        }
        if (event.getInventory().getLocation() == null || currentAltLocation.distanceSquared(event.getInventory().getLocation()) > 1) {
            return;
        }
        if (!event.getPlayer().getUniqueId().equals(currentAltPlayer.getUniqueId())) {
            return;
        }
        if (currentAltTitle != null && NMSHandler.getVersion().isAtLeast(NMSVersion.v1_18)) {
            NMSHandler.getInstance().setInventoryTitle(event.getView(), currentAltTitle);
        }
        InventoryTag newTag = new InventoryTag(event.getInventory(), currentAltType, currentAltHolder);
        InventoryTrackerSystem.trackTemporaryInventory(event.getInventory(), newTag);
    }

    public void doSpecialOpen(InventoryType type, Player player, InventoryTag destination) {
        try {
            if (destination.customTitle != null || destination.idType.equals("script")) {
                currentAltType = destination.getIdType();
                currentAltTitle = destination.customTitle;
                currentAltHolder = destination.getIdHolder();
                currentAltPlayer = player;
                currentAltLocation = player.getLocation();
                currentAltLocation.setX(currentAltLocation.getBlockX());
                currentAltLocation.setZ(currentAltLocation.getBlockZ());
                currentAltLocation.setY(-1000);
            }
            InventoryView view;
            if (type == InventoryType.ANVIL) {
                view = PaperAPITools.instance.openAnvil(player, currentAltLocation);
            }
            else if (type == InventoryType.WORKBENCH) {
                view = player.openWorkbench(currentAltLocation, true);
            }
            else {
                return;
            }
            Inventory newInv = view.getTopInventory();
            newInv.setContents(destination.getContents());
        }
        finally {
            currentAltHolder = null;
            currentAltType = null;
            currentAltPlayer = null;
            currentAltLocation = null;
            currentAltTitle = null;
        }
    }

    @Override
    public void execute(final ScriptEntry scriptEntry) {
        List<String> actions = (List<String>) scriptEntry.getObject("actions");
        AbstractMap.SimpleEntry<Integer, InventoryTag> originentry = (AbstractMap.SimpleEntry<Integer, InventoryTag>) scriptEntry.getObject("origin");
        InventoryTag origin = originentry != null ? originentry.getValue() : null;
        AbstractMap.SimpleEntry<Integer, InventoryTag> destinationentry = (AbstractMap.SimpleEntry<Integer, InventoryTag>) scriptEntry.getObject("destination");
        InventoryTag destination = destinationentry.getValue();
        ElementTag slot = scriptEntry.getElement("slot");
        ElementTag mechanism = scriptEntry.getElement("mechanism");
        ObjectTag mechanismValue = scriptEntry.getObjectTag("mechanism_value");
        DataAction flagAction = (DataAction) scriptEntry.getObject("flag_action");
        TimeTag expiration = scriptEntry.getObjectTag("expiration");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), db("actions", actions), destination, origin, mechanism, mechanismValue, flagAction, expiration, slot);
        }
        int slotId = SlotHelper.nameToIndexFor(slot.asString(), destination.getInventory().getHolder());
        if (slotId < 0) {
            if (slotId == -1) {
                Debug.echoError(scriptEntry, "The input '" + slot.asString() + "' is not a valid slot (unrecognized)!");
            }
            else {
                Debug.echoError(scriptEntry, "The input '" + slot.asString() + "' is not a valid slot (negative values are invalid)!");
            }
            return;
        }
        InventoryTag.trackTemporaryInventory(destination);
        if (origin != null) {
            InventoryTag.trackTemporaryInventory(origin);
        }
        PlayerTag player = Utilities.getEntryPlayer(scriptEntry);
        for (String action : actions) {
            switch (Action.valueOf(action.toUpperCase())) {
                // Make the attached player open the destination inventory
                case OPEN:
                    // Use special method to make opening workbenches and anvils work properly
                    if ((destination.getInventoryType() == InventoryType.WORKBENCH || (destination.getInventoryType() == InventoryType.ANVIL && Denizen.supportsPaper)) && destination.getInventory().getLocation() == null) {
                        doSpecialOpen(destination.getInventoryType(), player.getPlayerEntity(), destination);
                    }
                    // Otherwise, open inventory as usual
                    else {
                        player.getPlayerEntity().openInventory(destination.getInventory());
                    }
                    break;
                // Make the attached player close any open inventory
                case CLOSE:
                    player.getPlayerEntity().closeInventory();
                    break;
                // Turn destination's contents into a copy of origin's
                case COPY:
                    if (origin == null) {
                        Debug.echoError(scriptEntry, "Missing origin argument!");
                        return;
                    }
                    replace(origin, destination);
                    break;
                // Copy origin's contents to destination, then empty origin
                case MOVE:
                    if (origin == null) {
                        Debug.echoError(scriptEntry, "Missing origin argument!");
                        return;
                    }
                    replace(origin, destination);
                    origin.clear();
                    break;
                // Swap the contents of the two inventories
                case SWAP:
                    if (origin == null) {
                        Debug.echoError(scriptEntry, "Missing origin argument!");
                        return;
                    }
                    InventoryTag temp = new InventoryTag(destination.getInventory().getContents());
                    replace(origin, destination);
                    replace(temp, origin);
                    break;
                // Add origin's contents to destination
                case ADD:
                    BukkitImplDeprecations.oldInventoryCommands.warn(scriptEntry);
                    if (origin == null) {
                        Debug.echoError(scriptEntry, "Missing origin argument!");
                        return;
                    }
                    destination.add(slotId, origin.getContents());
                    break;
                // Remove origin's contents from destination
                case REMOVE:
                    BukkitImplDeprecations.oldInventoryCommands.warn(scriptEntry);
                    if (origin == null) {
                        Debug.echoError(scriptEntry, "Missing origin argument!");
                        return;
                    }
                    remove(destination.getInventory(), origin.getContents());
                    break;
                // Set items by slot
                case SET:
                    if (origin == null) {
                        Debug.echoError(scriptEntry, "Missing origin argument!");
                        return;
                    }
                    destination.setSlots(slotId, origin.getContents(), originentry.getKey());
                    break;

                // Keep only items from the origin's contents in the destination
                case KEEP: {
                    if (origin == null) {
                        Debug.echoError(scriptEntry, "Missing origin argument!");
                        return;
                    }
                    ItemStack[] items = origin.getContents();
                    for (ItemStack invStack : destination.getInventory()) {
                        if (invStack != null) {
                            boolean keep = false;
                            // See if the item array contains
                            // this inventory item
                            for (ItemStack item : items) {
                                if (invStack.isSimilar(item)) {
                                    keep = true;
                                    break;
                                }
                            }
                            // If the item array did not contain
                            // this inventory item, remove it
                            // from the inventory
                            if (!keep) {
                                destination.getInventory().remove(invStack);
                            }
                        }
                    }
                    break;
                }
                // Exclude all items from the origin's contents in the destination
                case EXCLUDE: {
                    if (origin == null) {
                        Debug.echoError(scriptEntry, "Missing origin argument!");
                        return;
                    }
                    int oldCount = destination.count(null, false);
                    int newCount = -1;
                    while (oldCount != newCount) {
                        oldCount = newCount;
                        remove(destination.getInventory(), origin.getContents());
                        newCount = destination.count(null, false);
                    }
                    break;
                }
                // Add origin's contents over and over to destination until it is full
                case FILL: {
                    if (origin == null) {
                        Debug.echoError(scriptEntry, "Missing origin argument!");
                        return;
                    }
                    int oldCount = destination.count(null, false);
                    int newCount = -1;
                    while (oldCount != newCount) {
                        oldCount = newCount;
                        newCount = destination.add(0, origin.getContents()).count(null, false);
                    }
                    break;
                }
                // Clear the content of the destination inventory
                case CLEAR:
                    destination.clear();
                    break;
                // If this is a player inventory, update it
                case UPDATE:
                    if (destination.idHolder instanceof PlayerTag) {
                        ((PlayerTag) destination.idHolder).getPlayerEntity().updateInventory();
                    }
                    else {
                        Debug.echoError("Only player inventories can be force-updated!");
                    }
                    break;
                case ADJUST:
                    ItemTag toAdjust = new ItemTag(destination.getInventory().getItem(slotId));
                    toAdjust.safeAdjust(new Mechanism(mechanism.asString(), mechanismValue, scriptEntry.getContext()));
                    NMSHandler.itemHelper.setInventoryItem(destination.getInventory(), toAdjust.getItemStack(), slotId);
                    break;
                case FLAG:
                    ItemTag toFlag = new ItemTag(destination.getInventory().getItem(slotId));
                    FlagCommand.FlagActionProvider provider = (FlagCommand.FlagActionProvider) flagAction.provider;
                    provider.expiration = expiration;
                    provider.tracker = toFlag.getFlagTracker();
                    flagAction.execute(scriptEntry.context);
                    toFlag.reapplyTracker(provider.tracker);
                    NMSHandler.itemHelper.setInventoryItem(destination.getInventory(), toFlag.getItemStack(), slotId);
                    break;
            }
        }
    }

    public void replace(InventoryTag origin, InventoryTag destination) {
        // If the destination is smaller than our current inventory, add as many items as possible
        if (destination.getSize() < origin.getSize()) {
            destination.clear();
            destination.add(0, origin.getContents());
        }
        else {
            destination.setContents(origin.getContents());
        }
    }

    public void remove(Inventory inventory, ItemStack[] items) {
        for (ItemStack item : items) {
            if (item != null) {
                inventory.removeItem(item.clone());
            }
        }
    }
}
