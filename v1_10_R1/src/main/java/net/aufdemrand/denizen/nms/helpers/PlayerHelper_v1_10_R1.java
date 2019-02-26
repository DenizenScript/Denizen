package net.aufdemrand.denizen.nms.helpers;

import com.mojang.authlib.GameProfile;
import net.aufdemrand.denizen.nms.abstracts.ImprovedOfflinePlayer;
import net.aufdemrand.denizen.nms.impl.ImprovedOfflinePlayer_v1_10_R1;
import net.aufdemrand.denizen.nms.interfaces.PlayerHelper;
import net.aufdemrand.denizen.nms.util.ReflectionHelper;
import net.aufdemrand.denizencore.utilities.debugging.dB;
import net.minecraft.server.v1_10_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.OfflinePlayer;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.craftbukkit.v1_10_R1.CraftServer;
import org.bukkit.craftbukkit.v1_10_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PlayerHelper_v1_10_R1 implements PlayerHelper {

    public static Field ATTACK_COOLDOWN_TICKS = ReflectionHelper.getFields(EntityLiving.class).get("aF");

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
            dB.echoError(e);
        }
        return -1;
    }

    @Override
    public float getMaxAttackCooldownTicks(Player player) {
        return ((CraftPlayer) player).getHandle().dd();
    }

    @Override
    public float getAttackCooldownPercent(Player player) {
        return ((CraftPlayer) player).getHandle().o(0.5f);
    }

    @Override
    public void resetAttackCooldown(Player player) {
        ((CraftPlayer) player).getHandle().a(EnumHand.MAIN_HAND);
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
            int permLevel = server.q();
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
        return new ImprovedOfflinePlayer_v1_10_R1(uuid);
    }

    @Override
    public ImprovedOfflinePlayer getOfflineData(OfflinePlayer offlinePlayer) {
        return new ImprovedOfflinePlayer_v1_10_R1(offlinePlayer.getUniqueId());
    }

    /*
        Boss Bars
     */

    private static final Map<UUID, List<BossBar>> bossBars = new HashMap<UUID, List<BossBar>>();

    @Override
    public void showSimpleBossBar(Player player, String title, double progress) {
        UUID uuid = player.getUniqueId();
        if (!bossBars.containsKey(uuid)) {
            bossBars.put(uuid, new ArrayList<BossBar>());
        }
        List<BossBar> playerBars = bossBars.get(uuid);
        if (!playerBars.isEmpty()) {
            Iterator<BossBar> iterator = playerBars.iterator();
            while (iterator.hasNext()) {
                BossBar bossBar = iterator.next();
                bossBar.removePlayer(player);
                iterator.remove();
            }
        }
        BossBar bossBar = Bukkit.createBossBar(title, BarColor.PURPLE, BarStyle.SOLID);
        bossBar.setProgress(progress);
        bossBar.addPlayer(player);
        bossBar.setVisible(true);
        playerBars.add(bossBar);
    }

    @Override
    public void removeSimpleBossBar(Player player) {
        UUID uuid = player.getUniqueId();
        if (bossBars.containsKey(uuid) && !bossBars.get(uuid).isEmpty()) {
            Iterator<BossBar> iterator = bossBars.get(uuid).iterator();
            while (iterator.hasNext()) {
                BossBar bossBar = iterator.next();
                bossBar.removePlayer(player);
                iterator.remove();
            }
        }
    }
}
