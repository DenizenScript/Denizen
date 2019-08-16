package com.denizenscript.denizen.nms.helpers;

import com.denizenscript.denizen.nms.abstracts.ImprovedOfflinePlayer;
import com.denizenscript.denizen.nms.interfaces.PlayerHelper;
import com.denizenscript.denizen.nms.util.ReflectionHelper;
import com.mojang.authlib.GameProfile;
import com.denizenscript.denizen.nms.impl.ImprovedOfflinePlayer_v1_13_R2;
import com.denizenscript.denizen.nms.impl.packets.handlers.AbstractListenerPlayIn_v1_13_R2;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.minecraft.server.v1_13_R2.*;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_13_R2.CraftServer;
import org.bukkit.craftbukkit.v1_13_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;

public class PlayerHelper_v1_13_R2 extends PlayerHelper {

    public static final Field ATTACK_COOLDOWN_TICKS = ReflectionHelper.getFields(EntityLiving.class).get("aH");

    public static final Map<String, Field> PLAYER_CONNECTION_FIELDS = ReflectionHelper.getFields(PlayerConnection.class);
    public static final Field FLY_TICKS = PLAYER_CONNECTION_FIELDS.get("C");
    public static final Field VEHICLE_FLY_TICKS = PLAYER_CONNECTION_FIELDS.get("E");

    @Override
    public int getFlyKickCooldown(Player player) {
        PlayerConnection conn = ((CraftPlayer) player).getHandle().playerConnection;
        if (conn instanceof AbstractListenerPlayIn_v1_13_R2) {
            conn = ((AbstractListenerPlayIn_v1_13_R2) conn).oldListener;
        }

        try {
            return Math.max(80 - Math.max(FLY_TICKS.getInt(conn), VEHICLE_FLY_TICKS.getInt(conn)), 0);
        }
        catch (IllegalAccessException e) {
            Debug.echoError(e);
        }
        return 80;
    }

    @Override
    public void setFlyKickCooldown(Player player, int ticks) {
        ticks = 80 - ticks;
        PlayerConnection conn = ((CraftPlayer) player).getHandle().playerConnection;
        if (conn instanceof AbstractListenerPlayIn_v1_13_R2) {
            conn = ((AbstractListenerPlayIn_v1_13_R2) conn).oldListener;
        }

        try {
            FLY_TICKS.setInt(conn, ticks);
            VEHICLE_FLY_TICKS.setInt(conn, ticks);
        }
        catch (IllegalAccessException e) {
            Debug.echoError(e);
        }
    }

    @Override
    public float getAbsorption(Player player) {
        return ((CraftPlayer) player).getHandle().getDataWatcher().get(DataWatcherRegistry.c.a(11));
    }

    @Override
    public void setAbsorption(Player player, float value) {
        ((CraftPlayer) player).getHandle().getDataWatcher().set(DataWatcherRegistry.c.a(11), value);
    }

    @Override
    public int ticksPassedDuringCooldown(Player player) {
        try {
            return ATTACK_COOLDOWN_TICKS.getInt(((CraftPlayer) player).getHandle());
        }
        catch (IllegalAccessException e) {
            Debug.echoError(e);
        }
        return -1;
    }

    @Override
    public float getMaxAttackCooldownTicks(Player player) {
        return ((CraftPlayer) player).getHandle().dG() + 3;
    }

    @Override
    public float getAttackCooldownPercent(Player player) {
        return ((CraftPlayer) player).getHandle().r(0.5f);
    }

    @Override
    public void setAttackCooldown(Player player, int ticks) {
        // Theoretically the a(EnumHand) method sets the ATTACK_COOLDOWN_TICKS field to 0 and performs an
        // animation, but I'm unable to confirm if the animation actually triggers.
        //((CraftPlayer) player).getHandle().a(EnumHand.MAIN_HAND);
        try {
            ATTACK_COOLDOWN_TICKS.setInt(((CraftPlayer) player).getHandle(), ticks);
        }
        catch (IllegalAccessException e) {
            Debug.echoError(e);
        }

    }

    @Override
    public boolean hasChunkLoaded(Player player, Chunk chunk) {
        return ((CraftWorld) chunk.getWorld()).getHandle().getPlayerChunkMap()
                .a(((CraftPlayer) player).getHandle(), chunk.getX(), chunk.getZ());
    }

    @Override
    public int getPing(Player player) {
        return ((CraftPlayer) player).getHandle().ping;
    }

    @Override
    public void setTemporaryOp(Player player, boolean op) {
        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        GameProfile profile = ((CraftPlayer) player).getProfile();
        OpList opList = server.getPlayerList().getOPs();
        if (op) {
            int permLevel = server.j();
            opList.add(new OpListEntry(profile, permLevel, opList.b(profile)));
        }
        else {
            opList.remove(profile);
        }
        player.recalculatePermissions();
    }

    @Override
    public void showEndCredits(Player player) {
        ((CraftPlayer) player).getHandle().viewingCredits = true;
        ((CraftPlayer) player).getHandle().playerConnection
                .sendPacket(new PacketPlayOutGameStateChange(4, 0.0F));
    }

    @Override
    public ImprovedOfflinePlayer getOfflineData(UUID uuid) {
        return new ImprovedOfflinePlayer_v1_13_R2(uuid);
    }

    @Override
    public ImprovedOfflinePlayer getOfflineData(OfflinePlayer offlinePlayer) {
        return new ImprovedOfflinePlayer_v1_13_R2(offlinePlayer.getUniqueId());
    }
}
