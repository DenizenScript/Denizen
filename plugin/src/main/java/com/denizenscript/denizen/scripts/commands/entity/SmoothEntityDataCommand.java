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
                                   @ArgName("entity") @ArgPrefixed EntityTag inputEntity,
                                   @ArgName("for") @ArgPrefixed @ArgDefaultNull @ArgSubType(PlayerTag.class) List<PlayerTag> forPlayers,
                                   @ArgName("data") @ArgPrefixed MapTag inputData,
                                   @ArgName("speed") @ArgPrefixed DurationTag speed) {
        Entity entity = inputEntity.getBukkitEntity();
        // Each sub-list contains DataValues for a specific id
        List<List<Object>> data = new ArrayList<>();
        int maxLength = 0;
        for (Map.Entry<StringHolder, ObjectTag> entry : inputData.entrySet()) {
            int id = NMSHandler.entityHelper.mapInternalEntityDataName(entity, entry.getKey().low);
            List<ObjectTag> denizenObjects = entry.getValue().asType(ListTag.class, scriptEntry.context).objectForms;
            List<Object> convertedDataValues = new ArrayList<>(denizenObjects.size());
            for (ObjectTag object : denizenObjects) {
                Object converted = NMSHandler.entityHelper.convertInternalEntityDataValue(entity, id, object);
                if (converted != null) {
                    convertedDataValues.add(converted);
                }
            }
            maxLength = Math.max(maxLength, convertedDataValues.size());
            data.add(convertedDataValues);
        }
        List<Player> sendTo = new ArrayList<>(forPlayers.size());
        for (PlayerTag player : forPlayers) {
            sendTo.add(player.getPlayerEntity());
        }
        long ms = speed.getMillis();
        final int finalMaxLength = maxLength;
        DenizenCore.runAsync(() -> {
            try {
                for (int i = 0; i < finalMaxLength; i++) {
                    List<Object> toSend = new ArrayList<>();
                    for (List<Object> dataValues : data) {
                        if (dataValues.size() > i) {
                            toSend.add(dataValues.get(i));
                        }
                    }
                    NMSHandler.packetHelper.sendEntityDataPacket(sendTo, entity, toSend);
                    Thread.sleep(ms);
                }
            }
            catch (Exception ex) {
                Debug.echoError(ex);
            }
        });
    }
}
