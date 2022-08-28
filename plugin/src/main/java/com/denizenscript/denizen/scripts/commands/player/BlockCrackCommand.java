package com.denizenscript.denizen.scripts.commands.player;

import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BlockCrackCommand extends AbstractCommand {

    public BlockCrackCommand() {
        setName("blockcrack");
        setSyntax("blockcrack [<location>] [progress:<#>] (stack) (players:<player>|...)");
        setRequiredArguments(2, 4);
        isProcedural = false;
    }

    // <--[command]
    // @Name BlockCrack
    // @Syntax blockcrack [<location>] [progress:<#>] (stack) (players:<player>|...)
    // @Required 2
    // @Maximum 4
    // @Short Shows the player(s) a block cracking animation.
    // @Group player
    //
    // @Description
    // You must specify a progress number between 1 and 10, where 1 is the first stage and 10 is the last.
    // To remove the animation, you must specify any number outside of that range. For example, 0.
    // Optionally, you can stack multiple effects
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
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
            if (arg.matchesPrefix("players")
                    && arg.matchesArgumentList(PlayerTag.class)) {
                scriptEntry.addObject("players", arg.asType(ListTag.class).filter(PlayerTag.class, scriptEntry));
            }
            else if (arg.matchesPrefix("progress")
                    && arg.matchesInteger()) {
                scriptEntry.addObject("progress", arg.asElement());
            }
            else if (arg.matchesArgumentType(LocationTag.class)) {
                scriptEntry.addObject("location", arg.asType(LocationTag.class));
            }
            else if (arg.matches("stack")) {
                scriptEntry.addObject("stack", new ElementTag(true));
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
        scriptEntry.defaultObject("players", Collections.singletonList(Utilities.getEntryPlayer(scriptEntry)))
                .defaultObject("stack", new ElementTag(false));
    }

    private static class IntHolder {
        public int theInt;
        public int base;
    }

    private static Map<Location, Map<UUID, IntHolder>> progressTracker = new HashMap<>();
    private static int lastBase;

    @Override
    public void execute(ScriptEntry scriptEntry) {

        List<PlayerTag> players = (List<PlayerTag>) scriptEntry.getObject("players");
        ElementTag progress = scriptEntry.getElement("progress");
        LocationTag location = scriptEntry.getObjectTag("location");
        ElementTag stack = scriptEntry.getElement("stack");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), db("players", players), progress, location, stack);
        }
        Location loc = location.getBlock().getLocation();
        if (!progressTracker.containsKey(loc)) {
            progressTracker.put(loc, new HashMap<>());
            lastBase += 10;
        }
        Map<UUID, IntHolder> uuidInt = progressTracker.get(loc);
        boolean stackVal = stack.asBoolean();
        for (PlayerTag player : players) {
            if (!player.isOnline()) {
                Debug.echoError("Players must be online!");
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
                    NMSHandler.packetHelper.showBlockCrack(playerEnt, i, loc, -1);
                }
                intHolder.theInt = intHolder.base;
            }
            else if (stackVal && intHolder.theInt - intHolder.base > 10) {
                continue;
            }
            int id = stackVal ? intHolder.theInt++ : intHolder.theInt;
            NMSHandler.packetHelper.showBlockCrack(player.getPlayerEntity(), id, loc, progress.asInt() - 1);
        }
    }
}
