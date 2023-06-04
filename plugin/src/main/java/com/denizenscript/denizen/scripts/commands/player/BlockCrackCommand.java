package com.denizenscript.denizen.scripts.commands.player;

import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.DenizenCore;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.scripts.commands.generator.*;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.utilities.scheduling.RepeatingSchedulable;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BlockCrackCommand extends AbstractCommand {

    public BlockCrackCommand() {
        setName("blockcrack");
        setSyntax("blockcrack [<location>] [progress:<#>] (stack) (players:<player>|...) (duration:<duration>)");
        setRequiredArguments(2, 5);
        isProcedural = false;
        autoCompile();
    }

    // <--[command]
    // @Name BlockCrack
    // @Syntax blockcrack [<location>] [progress:<#>] (stack) (players:<player>|...) (duration:<duration>)
    // @Required 2
    // @Maximum 5
    // @Short Shows the player(s) a block cracking animation.
    // @Group player
    //
    // @Description
    // You must specify a progress number between 1 and 10, where 1 is the first stage and 10 is the last.
    // To remove the animation, you must specify any number outside of that range. For example, 0.
    // Optionally, you can stack multiple effects or set a duration for how long the effect should be shown.
    //
    // @Tags
    // None
    //
    // @Usage
    // Use to show a crack in a block to the currently attached player.
    // - blockcrack <context.location> progress:4
    //
    // @Usage
    // Use to stop showing a crack in a block to all online players.
    // - blockcrack <context.location> progress:0 players:<server.online_players>
    //
    // @Usage
    // Use to show all 10 layers of block cracking at the same time.
    // - repeat 10:
    //   - blockcrack <context.location> progress:<[value]> stack
    //
    // @Usage
    // Use to show a crack in a block to the attached player for 5 seconds.
    // - blockcrack <context.location> progress:4 duration:5s
    // -->

    private static class IntHolder {
        public int theInt;
        public int base;
    }

    private static final Map<Location, Map<UUID, IntHolder>> progressTracker = new HashMap<>();
    private static int lastBase;

    public static void autoExecute(ScriptEntry scriptEntry,
                            @ArgName("location") @ArgLinear LocationTag location,
                            @ArgName("progress") @ArgPrefixed int progress,
                            @ArgName("stack") boolean stack,
                            @ArgName("players") @ArgDefaultNull @ArgPrefixed @ArgSubType(PlayerTag.class) List<PlayerTag> players,
                            @ArgName("duration") @ArgPrefixed @ArgDefaultNull DurationTag duration) {
        if (players == null) {
            players = List.of(Utilities.getEntryPlayer(scriptEntry));
        }
        Location loc = location.getBlock().getLocation();
        if (!progressTracker.containsKey(loc)) {
            progressTracker.put(loc, new HashMap<>());
        }
        Map<UUID, IntHolder> uuidInt = progressTracker.get(loc);
        for (PlayerTag player : players) {
            if (!player.isOnline()) {
                Debug.echoError("Players must be online!");
                continue;
            }
            Player playerEnt = player.getPlayerEntity();
            UUID uuid = playerEnt.getUniqueId();
            if (!uuidInt.containsKey(uuid)) {
                lastBase += 10;
                IntHolder newIntHolder = new IntHolder();
                newIntHolder.theInt = lastBase;
                newIntHolder.base = lastBase;
                uuidInt.put(uuid, newIntHolder);
            }
            IntHolder intHolder = uuidInt.get(uuid);
            if (!stack && intHolder.theInt > intHolder.base) {
                for (int i = intHolder.base; i <= intHolder.theInt; i++) {
                    showBlockCrack(playerEnt, i, loc, -1, duration);
                }
                intHolder.theInt = intHolder.base;
            }
            else if (stack && intHolder.theInt - intHolder.base > 10) {
                continue;
            }
            int id = stack ? intHolder.theInt++ : intHolder.theInt;
            showBlockCrack(playerEnt, id, loc, progress - 1, duration);
        }
    }

    private static void showBlockCrack(Player player, int id, Location location, int progress, DurationTag duration) {
        if (duration == null || progress == -1) {
            NMSHandler.packetHelper.showBlockCrack(player, id, location, progress);
            return;
        }
        final RepeatingSchedulable schedulable = new RepeatingSchedulable(null, 1);
        long endTime = DenizenCore.serverTimeMillis + duration.getMillis();
        // Showing it before the schedulable will allow the block crack to appear as soon as the command is run.
        NMSHandler.packetHelper.showBlockCrack(player, id, location, progress);
        schedulable.run = () -> {
            if (!player.isOnline()) {
                schedulable.cancel();
                return;
            }
            if (endTime <= DenizenCore.serverTimeMillis) {
                NMSHandler.packetHelper.showBlockCrack(player, id, location, -1);
                schedulable.cancel();
                return;
            }
            NMSHandler.packetHelper.showBlockCrack(player, id, location, progress);
        };
        DenizenCore.schedule(schedulable);
    }
}
