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
import com.denizenscript.denizencore.utilities.debugging.Debug;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class FakeInternalDataCommand extends AbstractCommand {

    public FakeInternalDataCommand() {
        setName("fakeinternaldata");
        setSyntax("fakeinternaldata [entity:<entity>] [data:<map>|...] (for:<player>|...) (speed:<duration>)");
        setRequiredArguments(3, 4);
        autoCompile();
    }

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
        List<List<Object>> frames = new ArrayList<>(data.size());
        for (MapTag frame : data) {
            frames.add(NMSHandler.entityHelper.convertInternalEntityDataValues(entity, frame));
        }
        long ms = speed.getMillis();
        DenizenCore.runAsync(() -> {
            try {
                for (List<Object> frame : frames) {
                    NMSHandler.packetHelper.sendEntityDataPacket(sendTo, entity, frame);
                    Thread.sleep(ms);
                }
            }
            catch (Exception ex) {
                Debug.echoError(ex);
            }
        });
    }
}
