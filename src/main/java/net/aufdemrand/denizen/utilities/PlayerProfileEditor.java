package net.aufdemrand.denizen.utilities;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.aufdemrand.denizen.objects.properties.item.ItemSkullskin;
import net.aufdemrand.denizen.utilities.packets.PacketHelper;
import net.aufdemrand.denizencore.utilities.debugging.dB;
import net.minecraft.server.v1_8_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerProfileEditor {

    private static final Map<UUID, GameProfile> fakeProfiles = new HashMap<UUID, GameProfile>();
    private static final Field playerGameProfile;
    private static final Field gameProfileId;
    private static final Field gameProfileName;

    static {
        Field profileField = null;
        Field profileIdField = null;
        Field profileNameField = null;
        try {
            profileField = EntityHuman.class.getDeclaredField("bF");
            profileField.setAccessible(true);
            profileIdField = GameProfile.class.getDeclaredField("id");
            profileIdField.setAccessible(true);
            profileNameField = GameProfile.class.getDeclaredField("name");
            profileNameField.setAccessible(true);
        } catch (Exception e) {
            dB.echoError(e);
        }
        playerGameProfile = profileField;
        gameProfileId = profileIdField;
        gameProfileName = profileNameField;
    }

    public static void setPlayerName(Player player, String name) {
        GameProfile gameProfile = getFakeProfile(player);
        setProfileName(gameProfile, name);
        setPlayerProfile(player, gameProfile);
        updatePlayer(player, false);
    }

    public static void setPlayerSkin(Player player, String name) {
        GameProfile gameProfile = getFakeProfile(player);
        gameProfile.getProperties().get("textures").clear();
        GameProfile skinProfile = ItemSkullskin.fillGameProfile(new GameProfile(null, name));
        for (Property texture : skinProfile.getProperties().get("textures"))
            gameProfile.getProperties().put("textures", texture);
        setPlayerProfile(player, gameProfile);
        updatePlayer(player, true);
    }

    private static void updatePlayer(Player player, final boolean isSkinChanging) {
        final EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        final UUID uuid = player.getUniqueId();
        PacketPlayOutEntityDestroy destroyPacket = new PacketPlayOutEntityDestroy(entityPlayer.getId());
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            if (!p.getUniqueId().equals(uuid))
                PacketHelper.sendPacket(p, destroyPacket);
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                PacketPlayOutPlayerInfo playerInfo = new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.ADD_PLAYER, entityPlayer);
                PacketPlayOutNamedEntitySpawn spawnPacket = new PacketPlayOutNamedEntitySpawn(entityPlayer);
                for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                    PacketHelper.sendPacket(player, playerInfo);
                    if (!player.getUniqueId().equals(uuid)) {
                        PacketHelper.sendPacket(player, spawnPacket);
                    }
                    else if (isSkinChanging) {
                        boolean isFlying = player.isFlying();
                        PacketHelper.sendPacket(player, new PacketPlayOutRespawn(
                                player.getWorld().getEnvironment().getId(),
                                entityPlayer.getWorld().getDifficulty(),
                                entityPlayer.getWorld().worldData.getType(),
                                entityPlayer.playerInteractManager.getGameMode()));
                        player.teleport(player.getLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
                        player.setFlying(isFlying);
                    }
                }
            }
        }.runTaskLater(DenizenAPI.getCurrentInstance(), 5);
    }

    private static GameProfile getFakeProfile(Player player) {
        UUID uuid = player.getUniqueId();
        if (fakeProfiles.containsKey(uuid)) {
            return fakeProfiles.get(uuid);
        }
        else {
            GameProfile fakeProfile = new GameProfile(player.getUniqueId(), player.getName());
            for (Property texture : getPlayerProfile(player).getProperties().get("textures"))
                fakeProfile.getProperties().put("textures", texture);
            fakeProfiles.put(uuid, fakeProfile);
            return fakeProfile;
        }
    }

    private static GameProfile getPlayerProfile(Player player) {
        try {
            return (GameProfile) playerGameProfile.get(((CraftPlayer) player).getHandle());
        } catch (Exception e) {
            dB.echoError(e);
            return null;
        }
    }

    private static void setPlayerProfile(Player player, GameProfile gameProfile) {
        try {
            playerGameProfile.set(((CraftPlayer) player).getHandle(), gameProfile);
        } catch (Exception e) {
            dB.echoError(e);
        }
    }

    public static void setProfileId(GameProfile gameProfile, UUID uuid) {
        try {
            gameProfileId.set(gameProfile, uuid);
        } catch (Exception e) {
            dB.echoError(e);
        }
    }

    public static void setProfileName(GameProfile gameProfile, String name) {
        try {
            gameProfileName.set(gameProfile, name);
        } catch (Exception e) {
            dB.echoError(e);
        }
    }
}
