package com.denizenscript.denizen.scripts.commands.entity;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.DenizenCore;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.scripts.commands.generator.ArgDefaultNull;
import com.denizenscript.denizencore.scripts.commands.generator.ArgName;
import com.denizenscript.denizencore.scripts.commands.generator.ArgPrefixed;
import com.denizenscript.denizencore.scripts.commands.generator.ArgSubType;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizencore.utilities.text.StringHolder;
import it.unimi.dsi.fastutil.Pair;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SmoothEntityDataCommand extends AbstractCommand {

    public SmoothEntityDataCommand() {
        setName("smoothentitydata");
        autoCompile();
    }

    public static void autoExecute(ScriptEntry scriptEntry,
                                   @ArgName("entity") @ArgPrefixed EntityTag entity,
                                   @ArgName("for") @ArgPrefixed @ArgDefaultNull @ArgSubType(PlayerTag.class) List<PlayerTag> forPlayers,
                                   @ArgName("data") @ArgPrefixed MapTag inputData,
                                   @ArgName("speed") @ArgPrefixed DurationTag speed) {
        Map<Integer, List<Object>> data = new HashMap<>();
        int maxLength = 0;
        for (Map.Entry<StringHolder, ObjectTag> entry : inputData.entrySet()) {
            int id = NMSHandler.entityHelper.mapInternalEntityDataName(entity.getBukkitEntity(), entry.getKey().low);
            List<Object> convertedObjects = new ArrayList<>();
            for (ObjectTag object : entry.getValue().asType(ListTag.class, scriptEntry.context).objectForms) {
                Object converted = NMSHandler.entityHelper.convertInternalEntityDataValue(entity.getBukkitEntity(), id, object);
                if (converted != null) {
                    convertedObjects.add(converted);
                }
            }
            maxLength = Math.max(maxLength, convertedObjects.size());
            data.put(id, convertedObjects);
        }
        List<Player> sendTo = new ArrayList<>(forPlayers.size());
        for (PlayerTag player : forPlayers) {
            sendTo.add(player.getPlayerEntity());
        }
        Entity bukkitEntity = entity.getBukkitEntity();
        long ms = speed.getMillis();
        final int finalMaxLength = maxLength;
        DenizenCore.runAsync(() -> {
            try {
                for (int i = 0; i < finalMaxLength; i++) {
                    List<Pair<Integer, Object>> toSend = new ArrayList<>();
                    for (Map.Entry<Integer, List<Object>> entry : data.entrySet()) {
                        if (entry.getValue().size() > i) {
                            toSend.add(Pair.of(entry.getKey(), entry.getValue().get(i)));
                        }
                    }
                    NMSHandler.packetHelper.sendEntityDataPacket(sendTo, bukkitEntity, toSend);
                    Thread.sleep(ms);
                }
            }
            catch (Exception ex) {
                Debug.echoError(ex);
            }
        });
    }
}
