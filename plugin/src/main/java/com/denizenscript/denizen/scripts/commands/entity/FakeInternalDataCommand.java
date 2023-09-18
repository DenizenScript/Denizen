package com.denizenscript.denizen.scripts.commands.entity;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.DenizenCore;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.scripts.commands.generator.ArgDefaultText;
import com.denizenscript.denizencore.scripts.commands.generator.ArgName;
import com.denizenscript.denizencore.scripts.commands.generator.ArgPrefixed;
import com.denizenscript.denizencore.scripts.commands.generator.ArgSubType;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizencore.utilities.text.StringHolder;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FakeInternalDataCommand extends AbstractCommand {

    public FakeInternalDataCommand() {
        setName("fakeinternaldata");
        setSyntax("fakeinternaldata [entity:<entity>] [data:<map>|...] [for:<player>|...] (speed:<duration>)");
        setRequiredArguments(3, 4);
        autoCompile();
    }

    public static void autoExecute(@ArgName("entity") @ArgPrefixed EntityTag inputEntity,
                                   @ArgName("data") @ArgPrefixed @ArgSubType(MapTag.class) List<MapTag> data,
                                   @ArgName("for") @ArgPrefixed @ArgSubType(PlayerTag.class) List<PlayerTag> forPlayers,
                                   @ArgName("speed") @ArgPrefixed @ArgDefaultText("0s") DurationTag speed) {
        Entity entity = inputEntity.getBukkitEntity();
        List<List<Object>> frames = new ArrayList<>(data.size());
        for (MapTag frame : data) {
            frames.add(NMSHandler.entityHelper.convertInternalEntityDataValues(entity, parseEntityDataMap(entity, frame)));
        }
        List<Player> sendTo = new ArrayList<>(forPlayers.size());
        for (PlayerTag player : forPlayers) {
            sendTo.add(player.getPlayerEntity());
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

    public static Map<Integer, ObjectTag> parseEntityDataMap(Entity entity, MapTag map) {
        Map<Integer, ObjectTag> entityData = new HashMap<>(map.size());
        for (Map.Entry<StringHolder, ObjectTag> entry : map.entrySet()) {
            int id = NMSHandler.entityHelper.mapInternalEntityDataName(entity, entry.getKey().low);
            if (id == -1) {
                Debug.echoError("Invalid internal data key: " + entry.getKey());
                continue;
            }
           entityData.put(id, entry.getValue());
        }
        return entityData;
    }
}
