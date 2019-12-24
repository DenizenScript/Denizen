package com.denizenscript.denizen.nms.v1_15.impl.network.handlers;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.minecraft.server.v1_15_R1.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;

import java.util.Set;

public class AbstractListenerPlayInImpl extends PlayerConnection {

    public final PlayerConnection oldListener;

    public AbstractListenerPlayInImpl(NetworkManager networkManager, EntityPlayer entityPlayer, PlayerConnection oldListener) {
        super(MinecraftServer.getServer(), networkManager, entityPlayer);
        this.oldListener = oldListener;
    }

    @Override
    public CraftPlayer getPlayer() {
        return oldListener.getPlayer();
    }

    @Override
    public NetworkManager a() {
        return this.networkManager;
    }

    @Override
    public void disconnect(String s) {
        oldListener.disconnect(s);
    }

    @Override
    public void a(double d0, double d1, double d2, float f, float f1) {
        oldListener.a(d0, d1, d2, f, f1);
    }

    @Override
    public void a(double d0, double d1, double d2, float f, float f1, Set<PacketPlayOutPosition.EnumPlayerTeleportFlags> set) {
        oldListener.a(d0, d1, d2, f, f1, set);
    }

    @Override
    public void teleport(Location dest) {
        oldListener.teleport(dest);
    }

    @Override
    public void sendPacket(final Packet packet) {
        oldListener.sendPacket(packet);
    }

    @Override
    public void chat(String s, boolean async) {
        oldListener.chat(s, async);
    }

    @Override
    public void tick() {
        oldListener.tick();
    }

    public void handlePacketIn(Packet<PacketListenerPlayIn> packet) {
        if (NMSHandler.debugPackets) {
            Debug.log("Packet: " + packet.getClass().getCanonicalName() + " sent from " + player.getName());
        }
    }

    @Override
    public void a(PacketPlayInChat packet) {
        handlePacketIn(packet);
        oldListener.a(packet);
    }

    @Override
    public void a(PacketPlayInKeepAlive packet) {
        handlePacketIn(packet);
        oldListener.a(packet);
    }

    @Override
    public void a(PacketPlayInArmAnimation packet) {
        handlePacketIn(packet);
        oldListener.a(packet);
    }

    @Override
    public void a(PacketPlayInTabComplete packet) {
        handlePacketIn(packet);
        oldListener.a(packet);
    }

    @Override
    public void a(PacketPlayInClientCommand packet) {
        handlePacketIn(packet);
        oldListener.a(packet);
    }

    @Override
    public void a(PacketPlayInSettings packet) {
        handlePacketIn(packet);
        oldListener.a(packet);
    }

    @Override
    public void a(PacketPlayInTransaction packet) {
        handlePacketIn(packet);
        oldListener.a(packet);
    }

    @Override
    public void a(PacketPlayInEnchantItem packet) {
        handlePacketIn(packet);
        oldListener.a(packet);
    }

    @Override
    public void a(PacketPlayInWindowClick packet) {
        handlePacketIn(packet);
        oldListener.a(packet);
    }

    @Override
    public void a(PacketPlayInCloseWindow packet) {
        handlePacketIn(packet);
        oldListener.a(packet);
    }

    @Override
    public void a(PacketPlayInCustomPayload packet) {
        handlePacketIn(packet);
        oldListener.a(packet);
    }

    @Override
    public void a(PacketPlayInUseEntity packet) {
        handlePacketIn(packet);
        oldListener.a(packet);
    }

    @Override
    public void a(PacketPlayInFlying packet) {
        handlePacketIn(packet);
        oldListener.a(packet);
    }

    @Override
    public void a(PacketPlayInAbilities packet) {
        handlePacketIn(packet);
        oldListener.a(packet);
    }

    @Override
    public void a(PacketPlayInBlockDig packet) {
        handlePacketIn(packet);
        oldListener.a(packet);
    }

    @Override
    public void a(PacketPlayInEntityAction packet) {
        handlePacketIn(packet);
        oldListener.a(packet);
    }

    @Override
    public void a(PacketPlayInSteerVehicle packet) {
        handlePacketIn(packet);
        oldListener.a(packet);
    }

    @Override
    public void a(PacketPlayInHeldItemSlot packet) {
        handlePacketIn(packet);
        oldListener.a(packet);
    }

    @Override
    public void a(PacketPlayInSetCreativeSlot packet) {
        handlePacketIn(packet);
        oldListener.a(packet);
    }

    @Override
    public void a(PacketPlayInUpdateSign packet) {
        handlePacketIn(packet);
        oldListener.a(packet);
    }

    @Override
    public void a(PacketPlayInBlockPlace packet) {
        handlePacketIn(packet);
        oldListener.a(packet);
    }

    @Override
    public void a(PacketPlayInSpectate packet) {
        handlePacketIn(packet);
        oldListener.a(packet);
    }

    @Override
    public void a(PacketPlayInResourcePackStatus packet) {
        handlePacketIn(packet);
        oldListener.a(packet);
    }

    @Override
    public void a(PacketPlayInBoatMove packet) {
        handlePacketIn(packet);
        oldListener.a(packet);
    }

    @Override
    public void a(PacketPlayInTeleportAccept packet) {
        handlePacketIn(packet);
        oldListener.a(packet);
    }

    @Override
    public void a(PacketPlayInUseItem packet) {
        handlePacketIn(packet);
        oldListener.a(packet);
    }

    @Override
    public void a(PacketPlayInVehicleMove packet) {
        handlePacketIn(packet);
        oldListener.a(packet);
    }

    @Override
    public void a(PacketPlayInAdvancements packet) {
        handlePacketIn(packet);
        oldListener.a(packet);
    }

    @Override
    public void a(PacketPlayInAutoRecipe packet) {
        handlePacketIn(packet);
        oldListener.a(packet);
    }

    @Override
    public void a(PacketPlayInRecipeDisplayed packet) {
        handlePacketIn(packet);
        oldListener.a(packet);
    }

    @Override
    public void a(PacketPlayInBEdit packet) {
        handlePacketIn(packet);
        oldListener.a(packet);
    }

    @Override
    public void a(PacketPlayInTrSel packet) {
        handlePacketIn(packet);
        oldListener.a(packet);
    }

    @Override
    public void a(PacketPlayInBeacon packet) {
        handlePacketIn(packet);
        oldListener.a(packet);
    }

    @Override
    public void a(PacketPlayInStruct packet) {
        handlePacketIn(packet);
        oldListener.a(packet);
    }

    @Override
    public void a(PacketPlayInItemName packet) {
        handlePacketIn(packet);
        oldListener.a(packet);
    }

    @Override
    public void a(PacketPlayInPickItem packet) {
        handlePacketIn(packet);
        oldListener.a(packet);
    }

    @Override
    public void a(PacketPlayInTileNBTQuery packet) {
        handlePacketIn(packet);
        oldListener.a(packet);
    }

    @Override
    public void a(PacketPlayInEntityNBTQuery packet) {
        handlePacketIn(packet);
        oldListener.a(packet);
    }

    @Override
    public void a(PacketPlayInSetCommandBlock packet) {
        handlePacketIn(packet);
        oldListener.a(packet);
    }

    @Override
    public void a(PacketPlayInSetCommandMinecart packet) {
        handlePacketIn(packet);
        oldListener.a(packet);
    }

    @Override
    public void a(PacketPlayInSetJigsaw packet) {
        handlePacketIn(packet);
        oldListener.a(packet);
    }

    @Override
    public void a(PacketPlayInDifficultyLock packet) {
        handlePacketIn(packet);
        oldListener.a(packet);
    }

    @Override
    public void a(PacketPlayInDifficultyChange packet) {
        handlePacketIn(packet);
        oldListener.a(packet);
    }

    @Override
    public void a(IChatBaseComponent iChatBaseComponent) {
        oldListener.a(iChatBaseComponent);
    }
}
