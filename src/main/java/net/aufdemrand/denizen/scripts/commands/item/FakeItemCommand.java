package net.aufdemrand.denizen.scripts.commands.item;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.DenizenCore;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Duration;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import net.aufdemrand.denizencore.utilities.scheduling.OneTimeSchedulable;
import net.minecraft.server.v1_9_R1.ItemStack;
import net.minecraft.server.v1_9_R1.PacketPlayOutSetSlot;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_9_R1.inventory.CraftItemStack;

import java.util.Arrays;
import java.util.List;

public class FakeItemCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        /* Match arguments to expected variables */
        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("slot")
                    && arg.matchesPrefix("slot")
                    && arg.matchesPrimitive(aH.PrimitiveType.Integer)) {
                scriptEntry.addObject("slot", arg.asElement());
            }

            else if (!scriptEntry.hasObject("duration")
                    && arg.matchesPrefix("duration")
                    && arg.matchesArgumentType(Duration.class)) {
                scriptEntry.addObject("duration", arg.asType(Duration.class));
            }

            else if (!scriptEntry.hasObject("item")
                    && arg.matchesArgumentList(dItem.class)) {
                scriptEntry.addObject("item", arg.asType(dList.class).filter(dItem.class));
            }

            else if (!scriptEntry.hasObject("players")
                    && arg.matchesArgumentList(dPlayer.class)
                    && arg.matchesPrefix("players")) {
                scriptEntry.addObject("players", arg.asType(dList.class).filter(dPlayer.class));
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
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        List<dItem> items = (List<dItem>) scriptEntry.getObject("item");
        final Element elSlot = scriptEntry.getElement("slot");
        Duration duration = scriptEntry.getdObject("duration");
        final List<dPlayer> players = (List<dPlayer>) scriptEntry.getObject("players");
        final Element player_only = scriptEntry.getElement("player_only");

        dB.report(scriptEntry, getName(), aH.debugList("items", items) + elSlot.debug() + duration.debug()
                + aH.debugList("players", players) + player_only.debug());

        int slot = elSlot.asInt() - 1;

        for (dItem item : items) {
            if (item == null) {
                slot++;
                continue;
            }

            net.minecraft.server.v1_9_R1.ItemStack itemStack = CraftItemStack.asNMSCopy(item.getItemStack());

            for (dPlayer player : players) {
                setSlot((CraftPlayer) player.getPlayerEntity(), slot, itemStack, player_only.asBoolean());
            }

            final int slotSnapshot = slot;
            slot++;

            if (duration.getSeconds() > 0) {
                DenizenCore.schedule(new OneTimeSchedulable(new Runnable() {
                    @Override
                    public void run() {
                        for (dPlayer player : players) {
                            CraftPlayer craftPlayer = (CraftPlayer) player.getPlayerEntity();
                            ItemStack original = CraftItemStack.asNMSCopy(craftPlayer.getOpenInventory()
                                    .getItem(translateSlot(craftPlayer, slotSnapshot, player_only.asBoolean())));
                            setSlot(craftPlayer, slotSnapshot, original, player_only.asBoolean());
                        }
                    }
                }, (float) duration.getSeconds()));
            }
        }
    }

    static void setSlot(CraftPlayer craftPlayer, int slot, ItemStack itemStack, boolean player_only) {
        PacketPlayOutSetSlot setSlotPacket = new PacketPlayOutSetSlot(
                player_only ? 0 : craftPlayer.getHandle().activeContainer.windowId,
                translateSlot(craftPlayer, slot, player_only),
                itemStack
        );
        craftPlayer.getHandle().playerConnection.sendPacket(setSlotPacket);
    }

    static int translateSlot(CraftPlayer craftPlayer, int slot, boolean player_only) {
        int total = player_only ? 41 : craftPlayer.getOpenInventory().countSlots();
        if (total == 41) {
            total += 4;
            if (slot > 35) {
                slot = 44 + 36 - slot;
            }
        }
        return (int) (slot + (total - 9) - (9 * (2 * Math.floor(slot / 9))));
    }
}
