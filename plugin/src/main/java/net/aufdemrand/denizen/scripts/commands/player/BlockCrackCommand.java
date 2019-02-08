package net.aufdemrand.denizen.scripts.commands.player;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.nms.NMSHandler;
import net.aufdemrand.denizen.nms.interfaces.PacketHelper;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BlockCrackCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (arg.matchesPrefix("players")
                    && arg.matchesArgumentList(dPlayer.class)) {
                scriptEntry.addObject("players", arg.asType(dList.class).filter(dPlayer.class));
            }
            else if (arg.matchesPrefix("progress")
                    && arg.matchesPrimitive(aH.PrimitiveType.Integer)) {
                scriptEntry.addObject("progress", arg.asElement());
            }
            else if (arg.matchesArgumentType(dLocation.class)) {
                scriptEntry.addObject("location", arg.asType(dLocation.class));
            }
            else if (arg.matches("stack")) {
                scriptEntry.addObject("stack", new Element(true));
            }
            else {
                arg.reportUnhandled();
            }

        }

        if (!scriptEntry.hasObject("progress")) {
            throw new InvalidArgumentsException("Must specify crack animation progress!");
        }

        if (!scriptEntry.hasObject("location")) {
            throw new InvalidArgumentsException("Must specify a valid location!");
        }

        scriptEntry.defaultObject("players", Collections.singletonList(((BukkitScriptEntryData) scriptEntry.entryData).getPlayer()))
                .defaultObject("stack", new Element(false));
    }

    private static class IntHolder {
        public int theInt;
        public int base;
    }

    private static Map<Location, Map<UUID, IntHolder>> progressTracker = new HashMap<Location, Map<UUID, IntHolder>>();
    private static int lastBase;

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        List<dPlayer> players = (List<dPlayer>) scriptEntry.getObject("players");
        Element progress = scriptEntry.getElement("progress");
        dLocation location = scriptEntry.getdObject("location");
        Element stack = scriptEntry.getElement("stack");

        if (scriptEntry.dbCallShouldDebug()) {

            dB.report(scriptEntry, getName(), aH.debugList("players", players)
                    + progress.debug() + location.debug() + stack.debug());

        }

        Location loc = location.getBlock().getLocation();
        if (!progressTracker.containsKey(loc)) {
            progressTracker.put(loc, new HashMap<UUID, IntHolder>());
            lastBase += 10;
        }
        Map<UUID, IntHolder> uuidInt = progressTracker.get(loc);

        boolean stackVal = stack.asBoolean();

        PacketHelper packetHelper = NMSHandler.getInstance().getPacketHelper();

        for (dPlayer player : players) {
            if (!player.isOnline()) {
                dB.echoError("Players must be online!");
                continue;
            }
            Player playerEnt = player.getPlayerEntity();
            UUID uuid = playerEnt.getUniqueId();
            if (!uuidInt.containsKey(uuid)) {
                IntHolder newIntHolder = new IntHolder();
                newIntHolder.theInt = lastBase;
                newIntHolder.base = lastBase;
                uuidInt.put(uuid, newIntHolder);
            }
            IntHolder intHolder = uuidInt.get(uuid);
            if (!stackVal && intHolder.theInt > intHolder.base) {
                for (int i = intHolder.base; i <= intHolder.theInt; i++) {
                    packetHelper.showBlockCrack(playerEnt, i, loc, -1);
                }
                intHolder.theInt = intHolder.base;
            }
            else if (stackVal && intHolder.theInt - intHolder.base > 10) {
                continue;
            }
            int id = stackVal ? intHolder.theInt++ : intHolder.theInt;
            packetHelper.showBlockCrack(player.getPlayerEntity(), id, loc, progress.asInt() - 1);
        }
    }
}
