package com.denizenscript.denizen.nms.v1_17.impl.network.handlers;

import com.denizenscript.denizen.events.player.PlayerChangesSignScriptEvent;
import com.denizenscript.denizen.events.player.PlayerSteersEntityScriptEvent;
import com.denizenscript.denizen.nms.v1_17.impl.network.packets.PacketInResourcePackStatusImpl;
import com.denizenscript.denizen.nms.v1_17.impl.network.packets.PacketInSteerVehicleImpl;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizen.scripts.commands.entity.FakeEquipCommand;
import com.denizenscript.denizen.utilities.packets.DenizenPacketHandler;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.nms.NMSHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.event.block.SignChangeEvent;

import java.nio.charset.StandardCharsets;

public class DenizenPacketListenerImpl extends AbstractListenerPlayInImpl {

    public String brand = "unknown";

    public BlockPos fakeSignExpected;

    public DenizenPacketListenerImpl(DenizenNetworkManagerImpl networkManager, ServerPlayer entityPlayer) {
        super(networkManager, entityPlayer, entityPlayer.connection);
    }

    @Override
    public void handlePlayerInput(final ServerboundPlayerInputPacket packet) {
        if (!PlayerSteersEntityScriptEvent.instance.eventData.isEnabled) {
            super.handlePlayerInput(packet);
            return;
        }
        DenizenPacketHandler.instance.receivePacket(player.getBukkitEntity(), new PacketInSteerVehicleImpl(packet), () -> super.handlePlayerInput(packet));
    }

    @Override
    public void handleResourcePackResponse(ServerboundResourcePackPacket packet) {
        DenizenPacketHandler.instance.receivePacket(player.getBukkitEntity(), new PacketInResourcePackStatusImpl(packet));
        super.handleResourcePackResponse(packet);
    }

    @Override
    public void handleUseItem(ServerboundUseItemPacket packet) {
        DenizenPacketHandler.instance.receivePlacePacket(player.getBukkitEntity());
        super.handleUseItem(packet);
    }

    @Override
    public void handlePlayerAction(ServerboundPlayerActionPacket packet) {
        DenizenPacketHandler.instance.receiveDigPacket(player.getBukkitEntity());
        super.handlePlayerAction(packet);
    }

    @Override
    public void handleAnimate(ServerboundSwingPacket packet) {
        FakeEquipCommand.EquipmentOverride override = FakeEquipCommand.getOverrideFor(player.getUUID(), player.getBukkitEntity());
        if (override != null && (override.hand != null || override.offhand != null)) {
            player.getBukkitEntity().updateInventory();
        }
        super.handleAnimate(packet);
    }

    @Override
    public void handleSetCarriedItem(ServerboundSetCarriedItemPacket packet) {
        FakeEquipCommand.EquipmentOverride override = FakeEquipCommand.getOverrideFor(player.getUUID(), player.getBukkitEntity());
        if (override != null && override.hand != null) {
            Bukkit.getScheduler().runTaskLater(NMSHandler.getJavaPlugin(), player.getBukkitEntity()::updateInventory, 2);
        }
        super.handleSetCarriedItem(packet);
    }

    @Override
    public void handleContainerClick(ServerboundContainerClickPacket packet) {
        FakeEquipCommand.EquipmentOverride override = FakeEquipCommand.getOverrideFor(player.getUUID(), player.getBukkitEntity());
        if (override != null && packet.getContainerId() == 0) {
            Bukkit.getScheduler().runTaskLater(NMSHandler.getJavaPlugin(), player.getBukkitEntity()::updateInventory, 1);
        }
        super.handleContainerClick(packet);
    }

    @Override
    public void handleCustomPayload(ServerboundCustomPayloadPacket packet) {
        if (NMSHandler.debugPackets) {
            Debug.log("Custom packet payload: " + packet.identifier.toString() + " sent from " + player.getScoreboardName());
        }
        if (packet.identifier.getNamespace().equals("minecraft") && packet.identifier.getPath().equals("brand")) {
            FriendlyByteBuf newData = new FriendlyByteBuf(packet.data.copy());
            int i = newData.readVarInt(); // read off the varInt of length to get rid of it
            brand = StandardCharsets.UTF_8.decode(newData.nioBuffer()).toString();
        }
        super.handleCustomPayload(packet);
    }

    @Override
    public void handleSignUpdate(ServerboundSignUpdatePacket packet) {
        if (fakeSignExpected != null && packet.getPos().equals(fakeSignExpected)) {
            fakeSignExpected = null;
            PlayerChangesSignScriptEvent evt = (PlayerChangesSignScriptEvent) PlayerChangesSignScriptEvent.instance.clone();
            evt.material = new MaterialTag(org.bukkit.Material.OAK_WALL_SIGN);
            evt.location = new LocationTag(player.getBukkitEntity().getLocation());
            LocationTag loc = evt.location.clone();
            loc.setY(0);
            evt.event = new SignChangeEvent(loc.getBlock(), player.getBukkitEntity(), packet.getLines());
            evt.fire(evt.event);
        }
        super.handleSignUpdate(packet);
    }

    @Override
    public void handleMovePlayer(ServerboundMovePlayerPacket packet) {
        if (DenizenPacketHandler.forceNoclip.contains(player.getUUID())) {
            player.noPhysics = true;
        }
        super.handleMovePlayer(packet);
    }

    // For compatibility with other plugins using Reflection weirdly...
    @Override
    public void send(Packet<?> packet) {
        super.send(packet);
    }
}
