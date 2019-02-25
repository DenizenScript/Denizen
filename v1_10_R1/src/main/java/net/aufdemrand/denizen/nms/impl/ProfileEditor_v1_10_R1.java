package net.aufdemrand.denizen.nms.impl;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.aufdemrand.denizen.nms.NMSHandler;
import net.aufdemrand.denizen.nms.abstracts.ProfileEditor;
import net.aufdemrand.denizen.nms.helpers.PacketHelper_v1_10_R1;
import net.aufdemrand.denizen.nms.util.PlayerProfile;
import net.aufdemrand.denizen.nms.util.ReflectionHelper;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import net.aufdemrand.denizencore.utilities.debugging.dB;
import net.minecraft.server.v1_10_R1.EntityPlayer;
import net.minecraft.server.v1_10_R1.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_10_R1.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.v1_10_R1.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_10_R1.PacketPlayOutRespawn;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;

public class ProfileEditor_v1_10_R1 extends ProfileEditor {

    @Override
    protected void updatePlayer(Player player, final boolean isSkinChanging) {
        final EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        final UUID uuid = player.getUniqueId();
        PacketPlayOutEntityDestroy destroyPacket = new PacketPlayOutEntityDestroy(entityPlayer.getId());
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            if (!p.getUniqueId().equals(uuid)) {
                PacketHelper_v1_10_R1.sendPacket(p, destroyPacket);
            }
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                PacketPlayOutPlayerInfo playerInfo = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, entityPlayer);
                PacketPlayOutNamedEntitySpawn spawnPacket = new PacketPlayOutNamedEntitySpawn(entityPlayer);
                for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                    PacketHelper_v1_10_R1.sendPacket(player, playerInfo);
                    if (!player.getUniqueId().equals(uuid)) {
                        PacketHelper_v1_10_R1.sendPacket(player, spawnPacket);
                    }
                    else {
                        if (isSkinChanging) {
                            boolean isFlying = player.isFlying();
                            PacketHelper_v1_10_R1.sendPacket(player, new PacketPlayOutRespawn(
                                    player.getWorld().getEnvironment().getId(),
                                    entityPlayer.getWorld().getDifficulty(),
                                    entityPlayer.getWorld().worldData.getType(),
                                    entityPlayer.playerInteractManager.getGameMode()));
                            player.teleport(player.getLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
                            player.setFlying(isFlying);
                        }
                        player.updateInventory();
                    }
                }
            }
        }.runTaskLater(NMSHandler.getJavaPlugin(), 5);
    }

    public static void updatePlayerProfiles(PacketPlayOutPlayerInfo packet) {
        PacketPlayOutPlayerInfo.EnumPlayerInfoAction action = ReflectionHelper.getFieldValue(PacketPlayOutPlayerInfo.class, "a", packet);
        if (action != PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER) {
            return;
        }
        List<?> dataList = ReflectionHelper.getFieldValue(PacketPlayOutPlayerInfo.class, "b", packet);
        if (dataList != null) {
            try {
                for (Object data : dataList) {
                    GameProfile gameProfile = (GameProfile) playerInfoData_gameProfile.get(data);
                    if (fakeProfiles.containsKey(gameProfile.getId())) {
                        playerInfoData_gameProfile.set(data, getGameProfile(fakeProfiles.get(gameProfile.getId())));
                    }
                }
            }
            catch (Exception e) {
                dB.echoError(e);
            }
        }
    }

    private static GameProfile getGameProfile(PlayerProfile playerProfile) {
        GameProfile gameProfile = new GameProfile(playerProfile.getUniqueId(), playerProfile.getName());
        gameProfile.getProperties().put("textures",
                new Property("textures", playerProfile.getTexture(), playerProfile.getTextureSignature()));
        return gameProfile;
    }

    private static final Field playerInfoData_gameProfile;

    static {
        Field pidGameProfile = null;
        try {
            for (Class clzz : PacketPlayOutPlayerInfo.class.getDeclaredClasses()) {
                if (CoreUtilities.toLowerCase(clzz.getName()).contains("infodata")) {
                    pidGameProfile = clzz.getDeclaredField("d"); // PlayerInfoData.
                    pidGameProfile.setAccessible(true);
                    break;
                }
            }
        }
        catch (Exception e) {
            dB.echoError(e);
        }
        playerInfoData_gameProfile = pidGameProfile;
    }
}
