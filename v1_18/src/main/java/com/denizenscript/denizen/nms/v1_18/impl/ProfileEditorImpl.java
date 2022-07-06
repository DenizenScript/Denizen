package com.denizenscript.denizen.nms.v1_18.impl;

import com.denizenscript.denizen.nms.abstracts.ProfileEditor;
import com.denizenscript.denizen.nms.v1_18.Handler;
import com.denizenscript.denizen.nms.v1_18.helpers.PacketHelperImpl;
import com.denizenscript.denizen.nms.v1_18.impl.network.handlers.DenizenNetworkManagerImpl;
import com.denizenscript.denizen.scripts.commands.entity.RenameCommand;
import com.denizenscript.denizen.utilities.FormattedTextHelper;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.util.PlayerProfile;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_18_R2.CraftServer;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.invoke.MethodHandle;
import java.util.List;
import java.util.UUID;

public class ProfileEditorImpl extends ProfileEditor {

    @Override
    protected void updatePlayer(final Player player, final boolean isSkinChanging) {
        final ServerPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        final UUID uuid = player.getUniqueId();
        ClientboundPlayerInfoPacket playerInfo = new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, entityPlayer);
        for (Player otherPlayer : Bukkit.getServer().getOnlinePlayers()) {
            PacketHelperImpl.send(otherPlayer, playerInfo);
        }
        for (Player otherPlayer : NMSHandler.entityHelper.getPlayersThatSee(player)) {
            if (!otherPlayer.getUniqueId().equals(uuid)) {
                PacketHelperImpl.forceRespawnPlayerEntity(player, otherPlayer);
            }
        }
        if (isSkinChanging) {
            ((CraftServer) Bukkit.getServer()).getHandle().respawn(entityPlayer, (ServerLevel) entityPlayer.level, true, player.getLocation(), false);
        }
        player.updateInventory();
    }

    public static boolean handleAlteredProfiles(ClientboundPlayerInfoPacket packet, DenizenNetworkManagerImpl manager) {
        if (ProfileEditor.mirrorUUIDs.isEmpty() && !RenameCommand.hasAnyDynamicRenames()) {
            return true;
        }
        ClientboundPlayerInfoPacket.Action action = packet.getAction();
        if (action != ClientboundPlayerInfoPacket.Action.ADD_PLAYER && action != ClientboundPlayerInfoPacket.Action.UPDATE_DISPLAY_NAME) {
            return true;
        }
        List<ClientboundPlayerInfoPacket.PlayerUpdate> dataList = packet.getEntries();
        if (dataList == null) {
            return true;
        }
        try {
            boolean any = false;
            for (ClientboundPlayerInfoPacket.PlayerUpdate data : dataList) {
                if (ProfileEditor.mirrorUUIDs.contains(data.getProfile().getId()) || RenameCommand.customNames.containsKey(data.getProfile().getId())) {
                    any = true;
                }
            }
            if (!any) {
                return true;
            }
            GameProfile ownProfile = manager.player.getGameProfile();
            for (ClientboundPlayerInfoPacket.PlayerUpdate data : dataList) {
                if (!ProfileEditor.mirrorUUIDs.contains(data.getProfile().getId()) && !RenameCommand.customNames.containsKey(data.getProfile().getId())) {
                    ClientboundPlayerInfoPacket newPacket = new ClientboundPlayerInfoPacket(action);
                    List<ClientboundPlayerInfoPacket.PlayerUpdate> newPacketDataList = newPacket.getEntries();
                    newPacketDataList.add(data);
                    manager.oldManager.send(newPacket);
                }
                else {
                    String rename = RenameCommand.getCustomNameFor(data.getProfile().getId(), manager.player.getBukkitEntity(), false);
                    ClientboundPlayerInfoPacket newPacket = new ClientboundPlayerInfoPacket(action);
                    List<ClientboundPlayerInfoPacket.PlayerUpdate> newPacketDataList = newPacket.getEntries();
                    GameProfile patchedProfile = new GameProfile(data.getProfile().getId(), rename != null ? (rename.length() > 16 ? rename.substring(0, 16) : rename) : data.getProfile().getName());
                    if (ProfileEditor.mirrorUUIDs.contains(data.getProfile().getId())) {
                        patchedProfile.getProperties().putAll(ownProfile.getProperties());
                    }
                    else {
                        patchedProfile.getProperties().putAll(data.getProfile().getProperties());
                    }
                    String listRename = RenameCommand.getCustomNameFor(data.getProfile().getId(), manager.player.getBukkitEntity(), true);
                    Component displayName = listRename != null ? Handler.componentToNMS(FormattedTextHelper.parse(listRename, ChatColor.WHITE)) : data.getDisplayName();
                    ClientboundPlayerInfoPacket.PlayerUpdate newData = new ClientboundPlayerInfoPacket.PlayerUpdate(patchedProfile, data.getLatency(), data.getGameMode(), displayName);
                    newPacketDataList.add(newData);
                    manager.oldManager.send(newPacket);
                }
            }
            return false;
        }
        catch (Exception e) {
            Debug.echoError(e);
            return true;
        }
    }

    public static void updatePlayerProfiles(ClientboundPlayerInfoPacket packet) {
        ClientboundPlayerInfoPacket.Action action = packet.getAction();
        if (action != ClientboundPlayerInfoPacket.Action.ADD_PLAYER) {
            return;
        }
        List<ClientboundPlayerInfoPacket.PlayerUpdate> dataList = packet.getEntries();
        if (dataList != null) {
            try {
                for (ClientboundPlayerInfoPacket.PlayerUpdate data : dataList) {
                    GameProfile gameProfile = data.getProfile();
                    if (fakeProfiles.containsKey(gameProfile.getId())) {
                        playerInfoData_gameProfile_Setter.invoke(data, getGameProfile(fakeProfiles.get(gameProfile.getId())));
                    }
                }
            }
            catch (Throwable e) {
                Debug.echoError(e);
            }
        }
    }

    private static GameProfile getGameProfile(PlayerProfile playerProfile) {
        GameProfile gameProfile = new GameProfile(playerProfile.getUniqueId(), playerProfile.getName());
        if (playerProfile.hasTexture()) {
            gameProfile.getProperties().put("textures", new Property("textures", playerProfile.getTexture(), playerProfile.getTextureSignature()));
        }
        return gameProfile;
    }

    public static final MethodHandle playerInfoData_gameProfile_Setter = ReflectionHelper.getFinalSetterForFirstOfType(ClientboundPlayerInfoPacket.PlayerUpdate.class, GameProfile.class);
}
