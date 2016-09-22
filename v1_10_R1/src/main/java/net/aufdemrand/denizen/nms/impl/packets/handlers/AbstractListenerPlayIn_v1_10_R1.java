package net.aufdemrand.denizen.nms.impl.packets.handlers;

import net.aufdemrand.denizen.nms.util.DenizenAtomicIntegerFieldUpdater;
import net.minecraft.server.v1_10_R1.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftPlayer;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Set;

public class AbstractListenerPlayIn_v1_10_R1 extends PlayerConnection {

    protected final PlayerConnection oldListener;

    public AbstractListenerPlayIn_v1_10_R1(NetworkManager networkManager, EntityPlayer entityPlayer, PlayerConnection oldListener) {
        super(MinecraftServer.getServer(), networkManager, entityPlayer);
        this.oldListener = oldListener;
        try {
            Field chatSpamField = PlayerConnection.class.getDeclaredField("chatSpamField");
            chatSpamField.setAccessible(true);
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(chatSpamField, chatSpamField.getModifiers() & ~Modifier.FINAL);
            Field chatThrottle = PlayerConnection.class.getDeclaredField("chatThrottle");
            chatThrottle.setAccessible(true);
            chatSpamField.set(null, new DenizenAtomicIntegerFieldUpdater<PlayerConnection>(chatThrottle));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
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
    public void E_() {
        oldListener.E_();
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
    public void a(IChatBaseComponent iChatBaseComponent) {
        oldListener.a(iChatBaseComponent);
    }
}
