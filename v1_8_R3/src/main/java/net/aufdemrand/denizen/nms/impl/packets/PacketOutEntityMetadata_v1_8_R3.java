package net.aufdemrand.denizen.nms.impl.packets;

import net.aufdemrand.denizen.nms.interfaces.packets.PacketOutEntityMetadata;
import net.aufdemrand.denizen.nms.util.ReflectionHelper;
import net.minecraft.server.v1_8_R3.DataWatcher;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityMetadata;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public class PacketOutEntityMetadata_v1_8_R3 implements PacketOutEntityMetadata {

    private PacketPlayOutEntityMetadata internal;
    private int entityId;
    private List<DataWatcher.WatchableObject> metadata;

    public PacketOutEntityMetadata_v1_8_R3(PacketPlayOutEntityMetadata internal) {
        this.internal = internal;
        try {
            entityId = ENTITY_ID.getInt(internal);
            metadata = (List<DataWatcher.WatchableObject>) METADATA.get(internal);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getEntityId() {
        return entityId;
    }

    @Override
    public boolean checkForGlow() {
        // There is no glow effect in 1.8
        return false;
    }

    private static final Field ENTITY_ID, METADATA;

    static {
        Map<String, Field> fields = ReflectionHelper.getFields(PacketPlayOutEntityMetadata.class);
        ENTITY_ID = fields.get("a");
        METADATA = fields.get("b");
    }
}
