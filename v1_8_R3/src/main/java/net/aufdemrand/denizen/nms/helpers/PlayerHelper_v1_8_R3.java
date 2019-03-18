package net.aufdemrand.denizen.nms.helpers;

import com.mojang.authlib.GameProfile;
import net.aufdemrand.denizen.nms.abstracts.ImprovedOfflinePlayer;
import net.aufdemrand.denizen.nms.impl.BossBar_v1_8_R3;
import net.aufdemrand.denizen.nms.impl.ImprovedOfflinePlayer_v1_8_R3;
import net.aufdemrand.denizen.nms.interfaces.PlayerHelper;
import net.aufdemrand.denizencore.utilities.debugging.dB;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import net.minecraft.server.v1_8_R3.OpList;
import net.minecraft.server.v1_8_R3.OpListEntry;
import net.minecraft.server.v1_8_R3.PacketPlayOutGameStateChange;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PlayerHelper_v1_8_R3 implements PlayerHelper {

    @Override
    public float getAbsorption(Player player) {
        return ((CraftPlayer) player).getHandle().getDataWatcher().getFloat(17);
    }

    @Override
    public void setAbsorption(Player player, float value) {
        ((CraftPlayer) player).getHandle().getDataWatcher().watch(17, value);
    }

    @Override
    public int ticksPassedDuringCooldown(Player player) {
        throw new UnsupportedOperationException("Attack cooldowns don't exist prior to Minecraft 1.9.");
    }

    @Override
    public float getAttackCooldownPercent(Player player) {
        throw new UnsupportedOperationException("Attack cooldowns don't exist prior to Minecraft 1.9.");
    }

    @Override
    public float getMaxAttackCooldownTicks(Player player) {
        throw new UnsupportedOperationException("Attack cooldowns don't exist prior to Minecraft 1.9.");
    }

    @Override
    public void setAttackCooldown(Player player, int ticks) {
        dB.echoError("Attack cooldowns don't exist prior to Minecraft 1.9.");
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
            int permLevel = server.p();
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
        return new ImprovedOfflinePlayer_v1_8_R3(uuid);
    }

    @Override
    public ImprovedOfflinePlayer getOfflineData(OfflinePlayer offlinePlayer) {
        return new ImprovedOfflinePlayer_v1_8_R3(offlinePlayer.getUniqueId());
    }

    @Override
    public void showSimpleBossBar(Player player, String title, double progress) {
        BossBar_v1_8_R3.showBossBar(player, title, (int) (progress * 300));
    }

    @Override
    public void removeSimpleBossBar(Player player) {
        BossBar_v1_8_R3.removeBossBar(player);
    }
}
