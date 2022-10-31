package com.denizenscript.denizen.scripts.commands.item;

import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.command.TabCompleteHelper;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.inventory.SlotHelper;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.DenizenCore;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.utilities.scheduling.OneTimeSchedulable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;

public class FakeItemCommand extends AbstractCommand {

    public FakeItemCommand() {
        setName("fakeitem");
        setSyntax("fakeitem [<item>|...] [slot:<slot>] (duration:<duration>) (players:<player>|...) (raw)");
        setRequiredArguments(2, 5);
        isProcedural = false;
        setBooleansHandled("raw");
    }

    // <--[command]
    // @Name FakeItem
    // @Syntax fakeitem [<item>|...] [slot:<slot>] (duration:<duration>) (players:<player>|...) (raw)
    // @Required 2
    // @Maximum 5
    // @Short Show a fake item in a player's inventory.
    // @Group item
    //
    // @Description
    // This command allows you to display an item in an inventory that is not really there.
    //
    // To make it automatically disappear at a specific time, use the 'duration:' argument.
    // Note that the reset can be unreliable, especially if the player changes their open inventory view. Consider using "- inventory update" after a delay instead.
    //
    // By default, it will use any inventory the player currently has open.
    //
    // Slots function as follows:
    // Player inventory is slots 1-36, same as normal inventory slot indices.
    // If the player has an open inventory, to apply the item to a slot in that inventory, add 36 to the slot index.
    // If the player does not have an open inventory, slots 36-40 are equipment, 41 is offhand, 42 is recipe result, 43-46 are recipe.
    //
    // For modifying equipment, consider <@link mechanism PlayerTag.fake_equipment> instead.
    //
    // The slot argument can be any valid slot, see <@link language Slot Inputs>.
    //
    // Optionally specify 'raw' to indicate that the slow is a raw network slot ID.
    //
    // @Tags
    // None
    //
    // @Usage
    // Use to show a clientside-only pumpkin on the player's head.
    // - fakeitem pumpkin slot:head
    // -->

    @Override
    public void addCustomTabCompletions(TabCompletionsBuilder tab) {
        TabCompleteHelper.tabCompleteItems(tab);
    }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
            if (!scriptEntry.hasObject("slot")
                    && arg.matchesPrefix("slot")) {
                scriptEntry.addObject("slot", arg.asElement());
            }
            else if (!scriptEntry.hasObject("duration")
                    && arg.matchesPrefix("duration")
                    && arg.matchesArgumentType(DurationTag.class)) {
                scriptEntry.addObject("duration", arg.asType(DurationTag.class));
            }
            else if (!scriptEntry.hasObject("item")
                    && arg.matchesArgumentList(ItemTag.class)) {
                scriptEntry.addObject("item", arg.asType(ListTag.class).filter(ItemTag.class, scriptEntry));
            }
            else if (!scriptEntry.hasObject("players")
                    && arg.matchesArgumentList(PlayerTag.class)
                    && arg.matchesPrefix("players")) {
                scriptEntry.addObject("players", arg.asType(ListTag.class).filter(PlayerTag.class, scriptEntry));
            }
            else {
                arg.reportUnhandled();
            }
        }
        if (!scriptEntry.hasObject("item")) {
            throw new InvalidArgumentsException("Must specify a valid item to fake!");
        }
        if (!scriptEntry.hasObject("slot")) {
            throw new InvalidArgumentsException("Must specify a valid slot!");
        }
        scriptEntry.defaultObject("duration", new DurationTag(0))
                .defaultObject("players", Collections.singletonList(Utilities.getEntryPlayer(scriptEntry)));
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        boolean raw = scriptEntry.argAsBoolean("raw");
        List<ItemTag> items = (List<ItemTag>) scriptEntry.getObject("item");
        final ElementTag elSlot = scriptEntry.getElement("slot");
        DurationTag duration = scriptEntry.getObjectTag("duration");
        final List<PlayerTag> players = (List<PlayerTag>) scriptEntry.getObject("players");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), db("items", items), elSlot, duration, db("players", players), db("raw", raw));
        }
        if (players.size() == 0) {
            return;
        }
        int slot = SlotHelper.nameToIndex(elSlot.asString(), players.get(0).getPlayerEntity());
        if (slot == -1) {
            Debug.echoError(scriptEntry, "The input '" + elSlot.asString() + "' is not a valid slot!");
            return;
        }
        for (ItemTag item : items) {
            if (item == null) {
                slot++;
                continue;
            }
            int slotSnapshot = slot;
            for (PlayerTag player : players) {
                final Player ent = player.getPlayerEntity();
                final int translated = raw ? slot : translateSlot(ent, slot);
                final InventoryView view = ent.getOpenInventory();
                final Inventory top = view.getTopInventory();
                NMSHandler.packetHelper.setSlot(ent, translated, item.getItemStack(), false);
                if (duration.getSeconds() > 0) {
                    DenizenCore.schedule(new OneTimeSchedulable(() -> {
                        if (!ent.isOnline()) {
                            return;
                        }
                        if (top == view.getTopInventory()) {
                            ItemStack original = view.getItem(translated);
                            NMSHandler.packetHelper.setSlot(ent, translated, original, false);
                        }
                        else if (slotSnapshot < 36) {
                            NMSHandler.packetHelper.setSlot(ent, translateSlot(ent, slotSnapshot), ent.getInventory().getItem(slotSnapshot), false);
                        }
                    }, (float) duration.getSeconds()));
                }
            }
            slot++;
        }
    }

    static int translateSlot(Player player, int slot) {
        // This translates Spigot slot standards to vanilla slots.
        // The slot order is different when a player is viewing an inventory vs not doing so, leading to this chaos.
        int topSize = player.getOpenInventory().getTopInventory().getSize();
        if (player.getOpenInventory().getTopInventory() instanceof CraftingInventory) {
            topSize = 9;
            if (slot > 35) {
                if (slot < 40) { // Armor equipment
                    return 8 - (slot - 36);
                }
                else if (slot == 40) { // Offhand
                    return 45;
                }
                else if (slot < 46) { // Recipe (Server slot IDs for this are effectively made up just to be linearly on the end)
                    return slot - 41;
                }
            }
        }
        int result;
        int total = 36 + topSize;
        int rowCount = (int) Math.ceil(total / 9.0);
        if (slot < 9) { // First row on server is last row on client
            int row = (int) Math.floor(slot / 9.0);
            int flippedRow = rowCount - row - 1;
            result = flippedRow * 9 + slot;
        }
        else if (slot < 36) { // player inv insides on client come after the top inv
            result = slot + (rowCount - 5) * 9;
        }
        else { // Top-inv slots are same server/client, but offset by the size of player inv
            result = slot - 36;
        }
        if (result < 0) {
            return 0;
        }
        if (result > total) {
            return total - 1;
        }
        return result;
    }
}
