package com.denizenscript.denizen.nms.v1_19.impl;

import com.denizenscript.denizen.nms.abstracts.ProfileEditor;
import com.denizenscript.denizen.nms.v1_19.Handler;
import com.denizenscript.denizen.nms.v1_19.helpers.PacketHelperImpl;
import com.denizenscript.denizen.nms.v1_19.impl.network.handlers.DenizenNetworkManagerImpl;
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
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_19_R2.CraftServer;
import org.bukkit.craftbukkit.v1_19_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.invoke.MethodHandle;
import java.util.List;
import java.util.UUID;

public class ProfileEditorImpl extends ProfileEditor {

    @Override
    protected void updatePlayer(final Player player, final boolean isSkinChanging) {
        final ServerPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        final UUID uuid = player.getUniqueId();
        ClientboundPlayerInfoUpdatePacket playerInfo = new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, entityPlayer);
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

    public static boolean handleAlteredProfiles(ClientboundPlayerInfoUpdatePacket packet, DenizenNetworkManagerImpl manager) {
        if (ProfileEditor.mirrorUUIDs.isEmpty() && !RenameCommand.hasAnyDynamicRenames()) {
            return true;
        }
        ClientboundPlayerInfoUpdatePacket.Action action = packet.actions();
        if (action != ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER && action != ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME) {
            return true;
        }
        List<ClientboundPlayerInfoUpdatePacket.Entry> dataList = packet.entries();
        if (dataList == null) {
            return true;
        }
        try {
            boolean any = false;
            for (ClientboundPlayerInfoUpdatePacket.Entry data : dataList) {
                if (ProfileEditor.mirrorUUIDs.contains(data.profile().getId()) || RenameCommand.customNames.containsKey(data.profile().getId())) {
                    any = true;
                }
            }
            if (!any) {
                return true;
            }
            GameProfile ownProfile = manager.player.getGameProfile();
            for (ClientboundPlayerInfoUpdatePacket.Entry data : dataList) {
                if (!ProfileEditor.mirrorUUIDs.contains(data.profile().getId()) && !RenameCommand.customNames.containsKey(data.profile().getId())) {
                    ClientboundPlayerInfoUpdatePacket newPacket = new ClientboundPlayerInfoUpdatePacket(action);
                    List<ClientboundPlayerInfoUpdatePacket.Entry> newPacketDataList = newPacket.entries();
                    newPacketDataList.add(data);
                    manager.oldManager.send(newPacket);
                }
                else {
                    String rename = RenameCommand.getCustomNameFor(data.profile().getId(), manager.player.getBukkitEntity(), false);
                    ClientboundPlayerInfoUpdatePacket newPacket = new ClientboundPlayerInfoUpdatePacket(action);
                    List<ClientboundPlayerInfoUpdatePacket.Entry> newPacketDataList = newPacket.entries();
                    GameProfile patchedProfile = new GameProfile(data.profile().getId(), rename != null ? (rename.length() > 16 ? rename.substring(0, 16) : rename) : data.profile().getName());
                    if (ProfileEditor.mirrorUUIDs.contains(data.profile().getId())) {
                        patchedProfile.getProperties().putAll(ownProfile.getProperties());
                    }
                    else {
                        patchedProfile.getProperties().putAll(data.profile().getProperties());
                    }
                    String listRename = RenameCommand.getCustomNameFor(data.profile().getId(), manager.player.getBukkitEntity(), true);
                    Component displayName = listRename != null ? Handler.componentToNMS(FormattedTextHelper.parse(listRename, ChatColor.WHITE)) : data.displayName();
                    ClientboundPlayerInfoUpdatePacket.Entry newData = new ClientboundPlayerInfoUpdatePacket.Entry(data.profileId(), patchedProfile, data.listed(), data.latency(), data.gameMode(), displayName, data.chatSession());
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

    public static void updatePlayerProfiles(ClientboundPlayerInfoUpdatePacket packet) {
        ClientboundPlayerInfoUpdatePacket.Action action = packet.actions();
        if (action != ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER) {
            return;
        }
        List<ClientboundPlayerInfoUpdatePacket.Entry> dataList = packet.entries();
        if (dataList != null) {
            try {
                for (ClientboundPlayerInfoUpdatePacket.Entry data : dataList) {
                    GameProfile gameProfile = data.profile();
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

    public static final MethodHandle playerInfoData_gameProfile_Setter = ReflectionHelper.getFinalSetterForFirstOfType(ClientboundPlayerInfoUpdatePacket.Entry.class, GameProfile.class);
}
