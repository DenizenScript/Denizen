package com.denizenscript.denizen.v1_13.impl.packets.handlers;

import net.minecraft.server.v1_13_R2.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;

import java.util.Set;

public class AbstractListenerPlayIn_v1_13_R2 extends PlayerConnection {

    public final PlayerConnection oldListener;

    public AbstractListenerPlayIn_v1_13_R2(NetworkManager networkManager, EntityPlayer entityPlayer, PlayerConnection oldListener) {
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

    @Override
    public void a(PacketPlayInChat packet) {
        oldListener.a(packet);
    }

    @Override
    public void a(PacketPlayInKeepAlive packet) {
        oldListener.a(packet);
    }

    @Override
    public void a(PacketPlayInArmAnimation packet) {
        oldListener.a(packet);
    }

    @Override
    public void a(PacketPlayInTabComplete packet) {
        oldListener.a(packet);
    }

    @Override
    public void a(PacketPlayInClientCommand packet) {
        oldListener.a(packet);
    }

    @Override
    public void a(PacketPlayInSettings packet) {
        oldListener.a(packet);
    }

    @Override
    public void a(PacketPlayInTransaction packet) {
        oldListener.a(packet);
    }

    @Override
    public void a(PacketPlayInEnchantItem packet) {
        oldListener.a(packet);
    }

    @Override
    public void a(PacketPlayInWindowClick packet) {
        oldListener.a(packet);
    }

    @Override
    public void a(PacketPlayInCloseWindow packet) {
        oldListener.a(packet);
    }

    @Override
    public void a(PacketPlayInCustomPayload packet) {
        oldListener.a(packet);
    }

    @Override
    public void a(PacketPlayInUseEntity packet) {
        oldListener.a(packet);
    }

    @Override
    public void a(PacketPlayInFlying packet) {
        oldListener.a(packet);
    }

    @Override
    public void a(PacketPlayInAbilities packet) {
        oldListener.a(packet);
    }

    @Override
    public void a(PacketPlayInBlockDig packet) {
        oldListener.a(packet);
    }

    @Override
    public void a(PacketPlayInEntityAction packet) {
        oldListener.a(packet);
    }

    @Override
    public void a(PacketPlayInSteerVehicle packet) {
        oldListener.a(packet);
    }

    @Override
    public void a(PacketPlayInHeldItemSlot packet) {
        oldListener.a(packet);
    }

    @Override
    public void a(PacketPlayInSetCreativeSlot packet) {
        oldListener.a(packet);
    }

    @Override
    public void a(PacketPlayInUpdateSign packet) {
        oldListener.a(packet);
    }

    @Override
    public void a(PacketPlayInBlockPlace packet) {
        oldListener.a(packet);
    }

    @Override
    public void a(PacketPlayInSpectate packet) {
        oldListener.a(packet);
    }

    @Override
    public void a(PacketPlayInResourcePackStatus packet) {
        oldListener.a(packet);
    }

    @Override
    public void a(PacketPlayInBoatMove packet) {
        oldListener.a(packet);
    }

    @Override
    public void a(PacketPlayInTeleportAccept packet) {
        oldListener.a(packet);
    }

    @Override
    public void a(PacketPlayInUseItem packet) {
        oldListener.a(packet);
    }

    @Override
    public void a(PacketPlayInVehicleMove packet) {
        oldListener.a(packet);
    }

    @Override
    public void a(PacketPlayInAdvancements packet) {
        oldListener.a(packet);
    }

    @Override
    public void a(PacketPlayInAutoRecipe packet) {
        oldListener.a(packet);
    }

    @Override
    public void a(PacketPlayInRecipeDisplayed packet) {
        oldListener.a(packet);
    }

    @Override
    public void a(PacketPlayInBEdit packet) {
        oldListener.a(packet);
    }

    @Override
    public void a(PacketPlayInTrSel packet) {
        oldListener.a(packet);
    }

    @Override
    public void a(PacketPlayInBeacon packet) {
        oldListener.a(packet);
    }

    @Override
    public void a(PacketPlayInStruct packet) {
        oldListener.a(packet);
    }

    @Override
    public void a(PacketPlayInItemName packet) {
        oldListener.a(packet);
    }

    @Override
    public void a(PacketPlayInPickItem packet) {
        oldListener.a(packet);
    }

    @Override
    public void a(PacketPlayInTileNBTQuery packet) {
        oldListener.a(packet);
    }

    @Override
    public void a(PacketPlayInEntityNBTQuery packet) {
        oldListener.a(packet);
    }

    @Override
    public void a(PacketPlayInSetCommandBlock packet) {
        oldListener.a(packet);
    }

    @Override
    public void a(PacketPlayInSetCommandMinecart packet) {
        oldListener.a(packet);
    }

    @Override
    public void a(IChatBaseComponent iChatBaseComponent) {
        oldListener.a(iChatBaseComponent);
    }
}
