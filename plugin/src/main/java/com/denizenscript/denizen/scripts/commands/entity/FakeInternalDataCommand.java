package com.denizenscript.denizen.scripts.commands.entity;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.DenizenCore;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsRuntimeException;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.scripts.commands.generator.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public class FakeInternalDataCommand extends AbstractCommand {

    // <--[language]
    // @name Internal Entity Data
    // @group Minecraft Logic
    // @description
    // Each entity in Minecraft has a set of data values that get sent to the client, with each data value being a number id -> value pair.
    // Denizen allows direct control over that data, as it can be useful for things like setting values that would usually be blocked.
    // Because this is such a direct control that's meant to impose less restriction, there's no limitations/verification on the values being set other than basic type checking.
    // Note as well that as these are raw internal values, they are liable to change between minecraft version updates, especially the numeric IDs.
    // For all possible internal entity data values and their respective ids, see <@link url https://github.com/DenizenScript/Denizen/blob/dev/v1_20/src/main/java/com/denizenscript/denizen/nms/v1_20/helpers/EntityDataNameMapper.java#L50>.
    // Alternatively, you can use the number id directly instead of the names listed there.
    // For a list of all entity data ids and their values, see <@link url https://wiki.vg/Entity_metadata>
    // (note that it documents the values that eventually get sent to the client, so the input this expects might be slightly different in some cases).
    // You can input the equivalent denizen objects to have them be auto-converted to the internal types.
    // -->

    public FakeInternalDataCommand() {
        setName("fakeinternaldata");
        setSyntax("fakeinternaldata [entity:<entity>] [data:<map>|...] (for:<player>|...) (speed:<duration>)");
        setRequiredArguments(2, 4);
        autoCompile();
    }

    // <--[command]
    // @Name FakeInternalData
    // @Syntax fakeinternaldata [entity:<entity>] [data:<map>|...] (for:<player>|...) (speed:<duration>)
    // @Required 2
    // @Maximum 4
    // @Short Sends fake entity data updates, optionally animating them with sub-tick precision.
    // @Group entity
    //
    // @Description
    // Sends fake internal entity data updates, optionally sending multiple over time.
    // This supports sub-tick precision, allowing smooth/high FPS animations.
    //
    // The input to 'data:' is a list of <@link ObjectType MapTag>s, with each map being a frame to send; see <@link language Internal Entity Data> for more information.
    //
    // Optionally specify a list of players to fake the data for, defaults to the linked player.
    //
    // 'speed:' is the amount of time between each frame getting sent, supporting sub-tick delays.
    // Note that this is the delay between each frame, regardless of their content (see examples).
    //
    // @Tags
    // None
    //
    // @Usage
    // Animates an item display entity's item for the linked player, and slowly scales it up.
    // - fakeinternaldata entity:<[item_display]> data:[item=iron_ingot;scale=0.6,0.6,0.6]|[item=gold_ingot;scale=0.8,0.8,0.8]|[item=netherite_ingot;scale=1,1,1] speed:0.5s
    //
    // @Usage
    // Changes an item display's item, then its scale a second later, then its item again another second later.
    // - fakeinternaldata entity:<[item_display]> data:[item=stone]|[scale=2,2,2]|[item=waxed_weathered_cut_copper_slab] speed:1s
    //
    // @Usage
    // Animates a rainbow glow on a display entity for all online players.
    // - define color <color[red]>
    // - repeat 256 from:0 as:hue:
    //   - define frames:->:[glow_color=<[color].with_hue[<[hue]>].argb_integer>]
    // - fakeinternaldata entity:<[display]> data:<[frames]> for:<server.online_players> speed:0.01s
    // -->

    public static void autoExecute(ScriptEntry scriptEntry,
                                   @ArgName("entity") @ArgPrefixed EntityTag inputEntity,
                                   @ArgName("data") @ArgPrefixed @ArgSubType(MapTag.class) List<MapTag> data,
                                   @ArgName("for") @ArgPrefixed @ArgDefaultNull @ArgSubType(PlayerTag.class) List<PlayerTag> forPlayers,
                                   @ArgName("speed") @ArgPrefixed @ArgDefaultText("0s") DurationTag speed) {
        List<Player> sendTo;
        if (forPlayers != null) {
            sendTo = new ArrayList<>(forPlayers.size());
            for (PlayerTag player : forPlayers) {
                sendTo.add(player.getPlayerEntity());
            }
        }
        else if (Utilities.entryHasPlayer(scriptEntry)) {
            sendTo = List.of(Utilities.getEntryPlayer(scriptEntry).getPlayerEntity());
        }
        else {
            throw new InvalidArgumentsRuntimeException("Must specify players to fake the internal data for.");
        }
        Entity entity = inputEntity.getBukkitEntity();
        if (data.size() == 1) {
            NMSHandler.packetHelper.sendEntityDataPacket(sendTo, entity, NMSHandler.entityHelper.convertInternalEntityDataValues(entity, data.get(0)));
            return;
        }
        List<List<Object>> frames = new ArrayList<>(data.size());
        for (MapTag frame : data) {
            frames.add(NMSHandler.entityHelper.convertInternalEntityDataValues(entity, frame));
        }
        long delayNanos = TimeUnit.MILLISECONDS.toNanos(speed.getMillis());
        DenizenCore.runAsync(() -> {
            long expectedTime = System.nanoTime();
            for (List<Object> frame : frames) {
                if (sendTo.isEmpty()) {
                    break;
                }
                NMSHandler.packetHelper.sendEntityDataPacket(sendTo, entity, frame);
                LockSupport.parkNanos(delayNanos + (expectedTime - System.nanoTime()));
                expectedTime += delayNanos;
            }
        });
    }
}
