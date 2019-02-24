package net.aufdemrand.denizen.scripts.commands.item;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.nms.NMSHandler;
import net.aufdemrand.denizen.nms.interfaces.PacketHelper;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.inventory.SlotHelper;
import net.aufdemrand.denizencore.DenizenCore;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Duration;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import net.aufdemrand.denizencore.utilities.scheduling.OneTimeSchedulable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public class FakeItemCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        /* Match arguments to expected variables */
        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("slot")
                    && arg.matchesPrefix("slot")) {
                scriptEntry.addObject("slot", arg.asElement());
            }
            else if (!scriptEntry.hasObject("duration")
                    && arg.matchesPrefix("duration")
                    && arg.matchesArgumentType(Duration.class)) {
                scriptEntry.addObject("duration", arg.asType(Duration.class));
            }
            else if (!scriptEntry.hasObject("item")
                    && arg.matchesArgumentList(dItem.class)) {
                scriptEntry.addObject("item", arg.asType(dList.class).filter(dItem.class, scriptEntry));
            }
            else if (!scriptEntry.hasObject("players")
                    && arg.matchesArgumentList(dPlayer.class)
                    && arg.matchesPrefix("players")) {
                scriptEntry.addObject("players", arg.asType(dList.class).filter(dPlayer.class, scriptEntry));
            }
            else if (!scriptEntry.hasObject("player_only")
                    && arg.matches("player_only")) {
                scriptEntry.addObject("player_only", new Element(true));
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

        scriptEntry.defaultObject("duration", Duration.ZERO).defaultObject("player_only", new Element(false))
                .defaultObject("players", Arrays.asList(((BukkitScriptEntryData) scriptEntry.entryData).getPlayer()));

    }

    @Override
    public void execute(ScriptEntry scriptEntry) {

        List<dItem> items = (List<dItem>) scriptEntry.getObject("item");
        final Element elSlot = scriptEntry.getElement("slot");
        Duration duration = scriptEntry.getdObject("duration");
        final List<dPlayer> players = (List<dPlayer>) scriptEntry.getObject("players");
        final Element player_only = scriptEntry.getElement("player_only");

        if (scriptEntry.dbCallShouldDebug()) {
            dB.report(scriptEntry, getName(), aH.debugList("items", items) + elSlot.debug() + duration.debug()
                    + aH.debugList("players", players) + player_only.debug());
        }

        int slot = SlotHelper.nameToIndex(elSlot.asString());
        if (slot == -1) {
            dB.echoError(scriptEntry.getResidingQueue(), "The input '" + elSlot.asString() + "' is not a valid slot!");
            return;
        }
        final boolean playerOnly = player_only.asBoolean();

        final PacketHelper packetHelper = NMSHandler.getInstance().getPacketHelper();

        for (dItem item : items) {
            if (item == null) {
                slot++;
                continue;
            }

            for (dPlayer player : players) {
                Player ent = player.getPlayerEntity();
                packetHelper.setSlot(ent, translateSlot(ent, slot, playerOnly), item.getItemStack(), playerOnly);
            }

            final int slotSnapshot = slot;
            slot++;

            if (duration.getSeconds() > 0) {
                DenizenCore.schedule(new OneTimeSchedulable(new Runnable() {
                    @Override
                    public void run() {
                        for (dPlayer player : players) {
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
