package com.denizenscript.denizen.nms.v1_20.impl.network.handlers.packet;

import com.denizenscript.denizen.nms.v1_20.Handler;
import com.denizenscript.denizen.nms.v1_20.helpers.PacketHelperImpl;
import com.denizenscript.denizen.nms.v1_20.impl.network.handlers.DenizenNetworkManagerImpl;
import com.denizenscript.denizen.scripts.commands.entity.GlowCommand;
import com.denizenscript.denizen.scripts.commands.entity.InvisibleCommand;
import com.denizenscript.denizen.scripts.commands.entity.RenameCommand;
import com.denizenscript.denizen.scripts.commands.entity.SneakCommand;
import com.denizenscript.denizen.utilities.FormattedTextHelper;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EntityMetadataPacketHandlers {

    public static void registerHandlers() {
        DenizenNetworkManagerImpl.registerPacketHandler(ClientboundSetEntityDataPacket.class, EntityMetadataPacketHandlers::processMetadataChangesForPacket);
    }

    public static ClientboundSetEntityDataPacket getModifiedMetadataFor(DenizenNetworkManagerImpl networkManager, ClientboundSetEntityDataPacket metadataPacket) {
        if (!RenameCommand.hasAnyDynamicRenames() && SneakCommand.forceSetSneak.isEmpty() && InvisibleCommand.helper.noOverrides() && GlowCommand.helper.noOverrides()) {
            return null;
        }
        try {
            Entity entity = networkManager.player.level().getEntity(metadataPacket.id());
            if (entity == null) {
                return null; // If it doesn't exist on-server, it's definitely not relevant, so move on
            }
            String nameToApply = RenameCommand.getCustomNameFor(entity.getUUID(), networkManager.player.getBukkitEntity(), false);
            Boolean forceSneak = SneakCommand.shouldSneak(entity.getUUID(), networkManager.player.getUUID());
            Boolean isInvisible = InvisibleCommand.helper.getState(entity.getBukkitEntity(), networkManager.player.getUUID(), true);
            Boolean isGlowing = GlowCommand.helper.getState(entity.getBukkitEntity(), networkManager.player.getUUID(), true);
            boolean shouldModifyFlags = isInvisible != null || forceSneak != null || isGlowing != null;
            if (nameToApply == null && !shouldModifyFlags) {
                return null;
            }
            List<SynchedEntityData.DataValue<?>> data = new ArrayList<>(metadataPacket.packedItems().size());
            Byte currentFlags = null;
            for (SynchedEntityData.DataValue<?> dataValue : metadataPacket.packedItems()) {
                if (dataValue.id() == 0 && shouldModifyFlags) { // 0: Entity Flags
                    currentFlags = (Byte) dataValue.value();
                }
                else if (nameToApply == null || (dataValue.id() != 2 && dataValue.id() != 3)) { // 2 and 3: Custom name and custom name visible
                    data.add(dataValue);
                }
            }
            if (shouldModifyFlags) {
                byte flags = currentFlags == null ? entity.getEntityData().get(PacketHelperImpl.ENTITY_DATA_ACCESSOR_FLAGS) : currentFlags;
                flags = applyEntityDataFlag(flags, forceSneak, 0x02);
                flags = applyEntityDataFlag(flags, isInvisible, 0x20);
                flags = applyEntityDataFlag(flags, isGlowing, 0x40);
                data.add(SynchedEntityData.DataValue.create(PacketHelperImpl.ENTITY_DATA_ACCESSOR_FLAGS, flags));
            }
            if (nameToApply != null) {
                data.add(SynchedEntityData.DataValue.create(PacketHelperImpl.ENTITY_DATA_ACCESSOR_CUSTOM_NAME, Optional.of(Handler.componentToNMS(FormattedTextHelper.parse(nameToApply, ChatColor.WHITE)))));
                data.add(SynchedEntityData.DataValue.create(PacketHelperImpl.ENTITY_DATA_ACCESSOR_CUSTOM_NAME_VISIBLE, true));
            }
            return new ClientboundSetEntityDataPacket(metadataPacket.id(), data);
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
            return null;
        }
    }

    public static byte applyEntityDataFlag(byte currentFlags, Boolean value, int flag) {
        if (value == null) {
            return currentFlags;
        }
        return (byte) (value ? currentFlags | flag : currentFlags & ~flag);
    }

    public static Packet<ClientGamePacketListener> processMetadataChangesForPacket(DenizenNetworkManagerImpl networkManager, Packet<ClientGamePacketListener> packet) {
        if (!(packet instanceof ClientboundSetEntityDataPacket entityDataPacket)) {
            return packet;
        }
        ClientboundSetEntityDataPacket altPacket = getModifiedMetadataFor(networkManager, entityDataPacket);
        if (altPacket == null) {
            return packet;
        }
        return altPacket;
    }
}
