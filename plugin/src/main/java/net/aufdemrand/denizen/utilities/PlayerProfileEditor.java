package net.aufdemrand.denizen.utilities;

import net.aufdemrand.denizen.nms.NMSHandler;
import net.aufdemrand.denizen.nms.impl.ProfileEditor_v1_10_R1;
import net.minecraft.server.v1_10_R1.PacketPlayOutPlayerInfo;
import org.bukkit.entity.Player;

public class PlayerProfileEditor {

    public static void updatePlayerProfiles(PacketPlayOutPlayerInfo packet) {
        ProfileEditor_v1_10_R1.updatePlayerProfiles(packet);
    }

    public static void setPlayerName(Player player, String name) {
        NMSHandler.getInstance().getProfileEditor().setPlayerName(player, name);
    }

    public static String getPlayerName(Player player) {
        return NMSHandler.getInstance().getProfileEditor().getPlayerName(player);
    }

    public static void setPlayerSkin(Player player, String name) {
        NMSHandler.getInstance().getProfileEditor().setPlayerSkin(player, name);
    }

    public static void setPlayerSkinBlob(Player player, String blob) {
        NMSHandler.getInstance().getProfileEditor().setPlayerSkinBlob(player, blob);
    }

    public static String getPlayerSkinBlob(Player player) {
        return NMSHandler.getInstance().getProfileEditor().getPlayerSkinBlob(player);
    }
}
