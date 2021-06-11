package com.denizenscript.denizen.nms.v1_17.impl.network.packets;

import com.denizenscript.denizen.nms.interfaces.packets.PacketOutEntityMetadata;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.SynchedEntityData;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public class PacketOutEntityMetadataImpl implements PacketOutEntityMetadata {

    private ClientboundSetEntityDataPacket internal;
    private int entityId;
    private List<SynchedEntityData.DataItem<?>> metadata;

    public PacketOutEntityMetadataImpl(ClientboundSetEntityDataPacket internal) {
        this.internal = internal;
        try {
            entityId = ENTITY_ID.getInt(internal);
            metadata = (List<SynchedEntityData.DataItem<?>>) METADATA.get(internal);
        }
        catch (Exception e) {
            Debug.echoError(e);
        }
    }

    @Override
    public int getEntityId() {
        return entityId;
    }

    @Override
    public boolean checkForGlow() {
        for (SynchedEntityData.DataItem<?> data : metadata) {
            if (data.a().a() == 0) {
                // TODO: Instead of cancelling, casually strip out the 0x40 "Glowing" metadata rather than cancelling entirely?
                return true;
            }
        }
        return false;
    }

    private static final Field ENTITY_ID, METADATA;

    static {
        Map<String, Field> fields = ReflectionHelper.getFields(ClientboundSetEntityDataPacket.class);
        ENTITY_ID = fields.get("id");
        METADATA = fields.get("packedItems");
    }
}
