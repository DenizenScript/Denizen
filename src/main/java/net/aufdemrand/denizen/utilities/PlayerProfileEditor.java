package net.aufdemrand.denizen.utilities;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.aufdemrand.denizen.objects.properties.item.ItemSkullskin;
import net.aufdemrand.denizen.utilities.packets.PacketHelper;
import net.aufdemrand.denizencore.utilities.debugging.dB;
import net.minecraft.server.v1_9_R2.EntityHuman;
import net.minecraft.server.v1_9_R2.EntityPlayer;
import net.minecraft.server.v1_9_R2.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_9_R2.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.v1_9_R2.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_9_R2.PacketPlayOutRespawn;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static net.minecraft.server.v1_9_R2.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;

public class PlayerProfileEditor {

    private static final Map<UUID, GameProfile> fakeProfiles = new HashMap<UUID, GameProfile>();
    private static final Field playerGameProfile, gameProfileId, gameProfileName;
    private static final Field playerInfo_action, playerInfo_data;
    private static final Field playerInfoData_gameProfile;

    static {
        Map<String, Field> fields = PacketHelper.registerFields(PacketPlayOutPlayerInfo.class);
        playerInfo_action = fields.get("a");
        playerInfo_data = fields.get("b");
        Field profileField = null;
        Field profileIdField = null;
        Field profileNameField = null;
        Field pidGameProfile = null;
        try {
            profileField = EntityHuman.class.getDeclaredField("bS");
            profileField.setAccessible(true);
            profileIdField = GameProfile.class.getDeclaredField("id");
            profileIdField.setAccessible(true);
            profileNameField = GameProfile.class.getDeclaredField("name");
            profileNameField.setAccessible(true);
            pidGameProfile = PacketPlayOutPlayerInfo.class.getDeclaredClasses()[0].getDeclaredField("d"); // PlayerInfoData.
            pidGameProfile.setAccessible(true);
        }
        catch (Exception e) {
            dB.echoError(e);
        }
        playerGameProfile = profileField;
        gameProfileId = profileIdField;
        gameProfileName = profileNameField;
        playerInfoData_gameProfile = pidGameProfile;
        DenizenAPI.getCurrentInstance().getServer().getPluginManager()
                .registerEvents(new PlayerProfileEditorListener(), DenizenAPI.getCurrentInstance());
    }

    public static void updatePlayerProfiles(PacketPlayOutPlayerInfo packet) {
        try {
            EnumPlayerInfoAction action = (EnumPlayerInfoAction) playerInfo_action.get(packet);
            if (action != EnumPlayerInfoAction.ADD_PLAYER) {
                return;
            }
            List<?> dataList = (List<?>) playerInfo_data.get(packet);
            for (Object data : dataList) {
                GameProfile gameProfile = (GameProfile) playerInfoData_gameProfile.get(data);
                if (fakeProfiles.containsKey(gameProfile.getId())) {
                    playerInfoData_gameProfile.set(data, fakeProfiles.get(gameProfile.getId()));
                }
            }
        }
        catch (Exception e) {
            dB.echoError(e);
        }
    }

    public static void setPlayerName(Player player, String name) {
        GameProfile gameProfile = getFakeProfile(player);
        setProfileName(gameProfile, name);
        updatePlayer(player, false);
    }

    public static String getPlayerName(Player player) {
        return getFakeProfile(player).getName();
    }

    public static void setPlayerSkin(Player player, String name) {
        GameProfile gameProfile = getFakeProfile(player);
        gameProfile.getProperties().get("textures").clear();
        GameProfile skinProfile = ItemSkullskin.fillGameProfile(new GameProfile(null, name));
        for (Property texture : skinProfile.getProperties().get("textures")) {
            gameProfile.getProperties().put("textures", texture);
        }
        updatePlayer(player, true);
    }

    public static void setPlayerSkinBlob(Player player, String blob) {
        GameProfile gameProfile = getFakeProfile(player);
        gameProfile.getProperties().get("textures").clear();
        gameProfile.getProperties().put("textures", new Property("textures", blob));
        updatePlayer(player, true);
    }

    public static String getPlayerSkinBlob(Player player) {
        return getFakeProfile(player).getProperties().get("textures").iterator().next().getValue();
    }

    private static void updatePlayer(Player player, final boolean isSkinChanging) {
        final EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        final UUID uuid = player.getUniqueId();
        PacketPlayOutEntityDestroy destroyPacket = new PacketPlayOutEntityDestroy(entityPlayer.getId());
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            if (!p.getUniqueId().equals(uuid)) {
                PacketHelper.sendPacket(p, destroyPacket);
            }
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
                    else {
                        if (isSkinChanging) {
                            boolean isFlying = player.isFlying();
                            PacketHelper.sendPacket(player, new PacketPlayOutRespawn(
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
        }.runTaskLater(DenizenAPI.getCurrentInstance(), 5);
    }

    private static GameProfile getFakeProfile(Player player) {
        UUID uuid = player.getUniqueId();
        if (fakeProfiles.containsKey(uuid)) {
            return fakeProfiles.get(uuid);
        }
        else {
            GameProfile fakeProfile = new GameProfile(player.getUniqueId(), player.getName());
            fakeProfile.getProperties().replaceValues("textures",
                    getPlayerProfile(player).getProperties().get("textures"));
            fakeProfiles.put(uuid, fakeProfile);
            return fakeProfile;
        }
    }

    private static GameProfile getPlayerProfile(Player player) {
        try {
            return (GameProfile) playerGameProfile.get(((CraftPlayer) player).getHandle());
        }
        catch (Exception e) {
            dB.echoError(e);
            return null;
        }
    }

    public static void setProfileName(GameProfile gameProfile, String name) {
        try {
            gameProfileName.set(gameProfile, name);
        }
        catch (Exception e) {
            dB.echoError(e);
        }
    }

    public static void setProfileId(GameProfile gameProfile, UUID uuid) {
        try {
            gameProfileId.set(gameProfile, uuid);
        }
        catch (Exception e) {
            dB.echoError(e);
        }
    }

    public static class PlayerProfileEditorListener implements Listener {
        @EventHandler
        public void onPlayerQuit(PlayerQuitEvent event) {
            UUID uuid = event.getPlayer().getUniqueId();
            if (fakeProfiles.containsKey(uuid)) {
                fakeProfiles.remove(uuid);
            }
        }
    }
}
