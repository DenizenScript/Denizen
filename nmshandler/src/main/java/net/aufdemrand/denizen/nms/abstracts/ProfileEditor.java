package net.aufdemrand.denizen.nms.abstracts;

import net.aufdemrand.denizen.nms.NMSHandler;
import net.aufdemrand.denizen.nms.util.PlayerProfile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

public abstract class ProfileEditor {

    public final static Map<UUID, PlayerProfile> fakeProfiles = new HashMap<>();

    public final static HashSet<UUID> mirrorUUIDs = new HashSet<>();

    public ProfileEditor() {
        Bukkit.getServer().getPluginManager().registerEvents(new PlayerProfileEditorListener(), NMSHandler.getJavaPlugin());
    }

    public void setPlayerName(Player player, String name) {
        PlayerProfile profile = getFakeProfile(player);
        profile.setName(name);
        updatePlayer(player, false);
    }

    public String getPlayerName(Player player) {
        return getFakeProfile(player).getName();
    }

    public void setPlayerSkin(Player player, String name) {
        PlayerProfile profile = getFakeProfile(player);
        PlayerProfile skinProfile = NMSHandler.getInstance().fillPlayerProfile(new PlayerProfile(name, null));
        if (skinProfile.getTexture() != null) {
            profile.setTexture(skinProfile.getTexture());
            profile.setTextureSignature(skinProfile.getTextureSignature());
            updatePlayer(player, true);
        }
    }

    public void setPlayerSkinBlob(Player player, String blob) {
        PlayerProfile profile = getFakeProfile(player);
        String[] split = blob.split(";");
        profile.setTexture(split[0]);
        profile.setTextureSignature(split.length > 0 ? split[1] : null);
        updatePlayer(player, true);
    }

    public String getPlayerSkinBlob(Player player) {
        PlayerProfile prof = getFakeProfile(player);
        return prof.getTexture() + ";" + prof.getTextureSignature();
    }

    protected abstract void updatePlayer(Player player, boolean isSkinChanging);

    private PlayerProfile getFakeProfile(Player player) {
        UUID uuid = player.getUniqueId();
        if (fakeProfiles.containsKey(uuid)) {
            return fakeProfiles.get(uuid);
        }
        else {
            PlayerProfile fakeProfile = NMSHandler.getInstance().getPlayerProfile(player);
            fakeProfiles.put(uuid, fakeProfile);
            return fakeProfile;
        }
    }

    private static class PlayerProfileEditorListener implements Listener {
        @EventHandler
        public void onPlayerQuit(PlayerQuitEvent event) {
            UUID uuid = event.getPlayer().getUniqueId();
            if (fakeProfiles.containsKey(uuid)) {
                fakeProfiles.remove(uuid);
            }
        }
    }
}
