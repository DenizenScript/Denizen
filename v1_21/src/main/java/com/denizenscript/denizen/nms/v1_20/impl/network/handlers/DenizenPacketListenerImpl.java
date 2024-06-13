package com.denizenscript.denizen.nms.v1_20.impl.network.handlers;

import com.denizenscript.denizen.events.player.PlayerChangesSignScriptEvent;
import com.denizenscript.denizen.events.player.PlayerSteersEntityScriptEvent;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.v1_20.impl.network.packets.PacketInResourcePackStatusImpl;
import com.denizenscript.denizen.nms.v1_20.impl.network.packets.PacketInSteerVehicleImpl;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizen.scripts.commands.entity.FakeEquipCommand;
import com.denizenscript.denizen.utilities.packets.DenizenPacketHandler;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_20_R4.block.CraftBlock;
import org.bukkit.event.block.SignChangeEvent;

public class DenizenPacketListenerImpl extends AbstractListenerPlayInImpl {

    public String brand = "unknown";

    public BlockPos fakeSignExpected;

    public DenizenPacketListenerImpl(DenizenNetworkManagerImpl networkManager, ServerPlayer entityPlayer) {
        super(networkManager, entityPlayer, entityPlayer.connection, new CommonListenerCookie(entityPlayer.getGameProfile(), entityPlayer.connection.latency(), entityPlayer.clientInformation(), entityPlayer.connection.isTransferred()));
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
        FakeEquipCommand.EquipmentOverride override = FakeEquipCommand.getOverrideFor(player.getUUID(), getCraftPlayer());
        if (override != null && (override.hand != null || override.offhand != null)) {
            player.getBukkitEntity().updateInventory();
        }
        super.handleAnimate(packet);
    }

    @Override
    public void handleSetCarriedItem(ServerboundSetCarriedItemPacket packet) {
        FakeEquipCommand.EquipmentOverride override = FakeEquipCommand.getOverrideFor(player.getUUID(), getCraftPlayer());
        if (override != null && override.hand != null) {
            Bukkit.getScheduler().runTaskLater(NMSHandler.getJavaPlugin(), player.getBukkitEntity()::updateInventory, 2);
        }
        super.handleSetCarriedItem(packet);
    }

    @Override
    public void handleContainerClick(ServerboundContainerClickPacket packet) {
        FakeEquipCommand.EquipmentOverride override = FakeEquipCommand.getOverrideFor(player.getUUID(), getCraftPlayer());
        if (override != null && packet.getContainerId() == 0) {
            Bukkit.getScheduler().runTaskLater(NMSHandler.getJavaPlugin(), player.getBukkitEntity()::updateInventory, 1);
        }
        super.handleContainerClick(packet);
    }

    @Override
    public void handleCustomPayload(ServerboundCustomPayloadPacket packet) {
        if (NMSHandler.debugPackets) {
            Debug.log("Custom packet payload: " + packet.payload().type().id().toString() + " sent from " + player.getScoreboardName());
        }
        super.handleCustomPayload(packet);
    }

    @Override
    public void handleSignUpdate(ServerboundSignUpdatePacket packet) {
        if (fakeSignExpected != null && packet.getPos().equals(fakeSignExpected)) {
            PlayerChangesSignScriptEvent evt = (PlayerChangesSignScriptEvent) PlayerChangesSignScriptEvent.instance.clone();
            evt.material = new MaterialTag(org.bukkit.Material.OAK_WALL_SIGN);
            evt.location = new LocationTag(player.getBukkitEntity().getLocation());
            evt.event = new SignChangeEvent(CraftBlock.at(player.level(), fakeSignExpected), player.getBukkitEntity(), packet.getLines());
            fakeSignExpected = null;
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
