package com.denizenscript.denizen.nms.v1_19.impl;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.abstracts.ProfileEditor;
import com.denizenscript.denizen.nms.util.PlayerProfile;
import com.denizenscript.denizen.nms.v1_19.Handler;
import com.denizenscript.denizen.nms.v1_19.helpers.PacketHelperImpl;
import com.denizenscript.denizen.nms.v1_19.impl.network.handlers.DenizenNetworkManagerImpl;
import com.denizenscript.denizen.scripts.commands.entity.RenameCommand;
import com.denizenscript.denizen.utilities.FormattedTextHelper;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_19_R3.CraftServer;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.lang.reflect.Field;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

public class ProfileEditorImpl extends ProfileEditor {

    @Override
    protected void updatePlayer(final Player player, final boolean isSkinChanging) {
        final ServerPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
        final UUID uuid = player.getUniqueId();
        ClientboundPlayerInfoRemovePacket removePlayerInfoPacket = new ClientboundPlayerInfoRemovePacket(List.of(uuid));
        ClientboundPlayerInfoUpdatePacket addPlayerInfoPacket = ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(List.of(nmsPlayer));
        for (Player otherPlayer : Bukkit.getServer().getOnlinePlayers()) {
            PacketHelperImpl.send(otherPlayer, removePlayerInfoPacket);
            PacketHelperImpl.send(otherPlayer, addPlayerInfoPacket);
        }
        for (Player otherPlayer : NMSHandler.entityHelper.getPlayersThatSee(player)) {
            if (!otherPlayer.getUniqueId().equals(uuid)) {
                PacketHelperImpl.forceRespawnPlayerEntity(player, otherPlayer);
            }
        }
        if (isSkinChanging) {
            ((CraftServer) Bukkit.getServer()).getHandle().respawn(nmsPlayer, (ServerLevel) nmsPlayer.level, true, player.getLocation(), false, PlayerRespawnEvent.RespawnReason.PLUGIN);
        }
        player.updateInventory();
    }

    public static boolean handleAlteredProfiles(ClientboundPlayerInfoUpdatePacket packet, DenizenNetworkManagerImpl manager) {
        if (ProfileEditor.mirrorUUIDs.isEmpty() && !RenameCommand.hasAnyDynamicRenames() && fakeProfiles.isEmpty()) {
            return true;
        }
        EnumSet<ClientboundPlayerInfoUpdatePacket.Action> actions = packet.actions();
        if (!actions.contains(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER) && !actions.contains(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME)) {
            return true;
        }
        try {
            boolean any = false;
            for (ClientboundPlayerInfoUpdatePacket.Entry entry : packet.entries()) {
                if (ProfileEditor.mirrorUUIDs.contains(entry.profileId()) || RenameCommand.customNames.containsKey(entry.profileId()) || fakeProfiles.containsKey(entry.profileId())) {
                    any = true;
                    break;
                }
            }
            if (!any) {
                return true;
            }
            GameProfile ownProfile = manager.player.getGameProfile();
            for (ClientboundPlayerInfoUpdatePacket.Entry data : packet.entries()) {
                if (!ProfileEditor.mirrorUUIDs.contains(data.profileId()) && !RenameCommand.customNames.containsKey(data.profileId()) && !fakeProfiles.containsKey(data.profileId())) {
                    manager.oldManager.send(createInfoPacket(actions, List.of(data)));
                }
                else {
                    String rename = RenameCommand.getCustomNameFor(data.profileId(), manager.player.getBukkitEntity(), false);
                    GameProfile baseProfile = fakeProfiles.containsKey(data.profileId()) ? getGameProfile(fakeProfiles.get(data.profileId())) : data.profile();
                    GameProfile patchedProfile = new GameProfile(baseProfile.getId(), rename != null ? (rename.length() > 16 ? rename.substring(0, 16) : rename) : baseProfile.getName());
                    if (ProfileEditor.mirrorUUIDs.contains(data.profileId())) {
                        patchedProfile.getProperties().putAll(ownProfile.getProperties());
                    }
                    else {
                        patchedProfile.getProperties().putAll(baseProfile.getProperties());
                    }
                    String listRename = RenameCommand.getCustomNameFor(data.profileId(), manager.player.getBukkitEntity(), true);
                    Component displayName = listRename != null ? Handler.componentToNMS(FormattedTextHelper.parse(listRename, ChatColor.WHITE)) : data.displayName();
                    ClientboundPlayerInfoUpdatePacket.Entry newData = new ClientboundPlayerInfoUpdatePacket.Entry(data.profileId(), patchedProfile, data.listed(), data.latency(), data.gameMode(), displayName, data.chatSession());
                    manager.oldManager.send(createInfoPacket(actions, List.of(newData)));
                }
            }
            return false;
        }
        catch (Exception e) {
            Debug.echoError(e);
            return true;
        }
    }

    public static final Field ClientboundPlayerInfoUpdatePacket_entries = ReflectionHelper.getFields(ClientboundPlayerInfoUpdatePacket.class).getFirstOfType(List.class);

    public static ClientboundPlayerInfoUpdatePacket createInfoPacket(EnumSet<ClientboundPlayerInfoUpdatePacket.Action> actions, List<ClientboundPlayerInfoUpdatePacket.Entry> entries) {
        ClientboundPlayerInfoUpdatePacket playerInfoUpdatePacket = new ClientboundPlayerInfoUpdatePacket(actions, List.of());
        try {
            ClientboundPlayerInfoUpdatePacket_entries.set(playerInfoUpdatePacket, entries);
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
        return playerInfoUpdatePacket;
    }

    private static GameProfile getGameProfile(PlayerProfile playerProfile) {
        GameProfile gameProfile = new GameProfile(playerProfile.getUniqueId(), playerProfile.getName());
        if (playerProfile.hasTexture()) {
            gameProfile.getProperties().put("textures", new Property("textures", playerProfile.getTexture(), playerProfile.getTextureSignature()));
        }
        return gameProfile;
    }
}
