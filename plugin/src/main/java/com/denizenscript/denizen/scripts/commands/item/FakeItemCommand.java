package com.denizenscript.denizen.scripts.commands.item;

import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.inventory.SlotHelper;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.interfaces.PacketHelper;
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
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public class FakeItemCommand extends AbstractCommand {

    // <--[command]
    // @Name FakeItem
    // @Syntax fakeitem [<item>|...] [slot:<slot>] (duration:<duration>) (players:<player>|...) (player_only)
    // @Required 2
    // @Short Show a fake item in a player's inventory.
    // @Group item
    //
    // @Description
    // This command allows you to display an item in an inventory that is not really there.
    // To make it automatically disappear at a specific time, use the 'duration:' argument.
    // By default, it will use any inventory the player currently has open. To force it to use only the player's
    // inventory, use the 'player_only' argument.
    //
    // @Tags
    // None
    //
    // @Usage
    // Use to show a clientside-only pumpkin on the player's head.
    // - fakeitem pumpkin slot:head
    //
    // @Usage
    // Use to show a fake book in the player's hand for 1 tick.
    // - fakeitem "written_book[book=author|Bob|title|My Book|pages|This is my book!]" slot:<player.held_item_slot> duration:1t
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        /* Match arguments to expected variables */
        for (Argument arg : scriptEntry.getProcessedArgs()) {

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
            else if (!scriptEntry.hasObject("player_only")
                    && arg.matches("player_only")) {
                scriptEntry.addObject("player_only", new ElementTag(true));
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

        scriptEntry.defaultObject("duration", DurationTag.ZERO).defaultObject("player_only", new ElementTag(false))
                .defaultObject("players", Arrays.asList(Utilities.getEntryPlayer(scriptEntry)));

    }

    @Override
    public void execute(ScriptEntry scriptEntry) {

        List<ItemTag> items = (List<ItemTag>) scriptEntry.getObject("item");
        final ElementTag elSlot = scriptEntry.getElement("slot");
        DurationTag duration = scriptEntry.getObjectTag("duration");
        final List<PlayerTag> players = (List<PlayerTag>) scriptEntry.getObject("players");
        final ElementTag player_only = scriptEntry.getElement("player_only");

        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), ArgumentHelper.debugList("items", items) + elSlot.debug() + duration.debug()
                    + ArgumentHelper.debugList("players", players) + player_only.debug());
        }

        int slot = SlotHelper.nameToIndex(elSlot.asString());
        if (slot == -1) {
            Debug.echoError(scriptEntry.getResidingQueue(), "The input '" + elSlot.asString() + "' is not a valid slot!");
            return;
        }
        final boolean playerOnly = player_only.asBoolean();

        final PacketHelper packetHelper = NMSHandler.getPacketHelper();

        for (ItemTag item : items) {
            if (item == null) {
                slot++;
                continue;
            }

            for (PlayerTag player : players) {
                Player ent = player.getPlayerEntity();
                packetHelper.setSlot(ent, translateSlot(ent, slot, playerOnly), item.getItemStack(), playerOnly);
            }

            final int slotSnapshot = slot;
            slot++;

            if (duration.getSeconds() > 0) {
                DenizenCore.schedule(new OneTimeSchedulable(new Runnable() {
                    @Override
                    public void run() {
                        for (PlayerTag player : players) {
                            Player ent = player.getPlayerEntity();
                            int translated = translateSlot(ent, slotSnapshot, playerOnly);
                            ItemStack original = ent.getOpenInventory().getItem(translated);
                            packetHelper.setSlot(ent, translated, original, playerOnly);
                        }
                    }
                }, (float) duration.getSeconds()));
            }
        }
    }

    static int translateSlot(Player player, int slot, boolean player_only) {
        // This is (probably?) a translation from standard player inventory slots to ones that work with the full crafting inventory system
        if (slot < 0) {
            return 0;
        }
        int total = player_only ? 46 : player.getOpenInventory().countSlots();
        if (total == 46) {
            if (slot == 45) {
                return slot;
            }
            else if (slot > 35) {
                slot = 8 - (slot - 36);
                return slot;
            }
            total -= 1;
        }
        if (slot > total) {
            return total;
        }
        return (int) (slot + (total - 9) - (9 * (2 * Math.floor(slot / 9.0))));
    }
}
