package com.denizenscript.denizen.nms.v1_20.impl;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.abstracts.ProfileEditor;
import com.denizenscript.denizen.nms.util.PlayerProfile;
import com.denizenscript.denizen.nms.v1_20.Handler;
import com.denizenscript.denizen.nms.v1_20.helpers.PacketHelperImpl;
import com.denizenscript.denizen.nms.v1_20.impl.network.handlers.DenizenNetworkManagerImpl;
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
import org.bukkit.craftbukkit.v1_20_R4.CraftServer;
import org.bukkit.craftbukkit.v1_20_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

public class ProfileEditorImpl extends ProfileEditor {

    public static final String EMPTY_NAME = "";
    public static final UUID NIL_UUID = new UUID(0L, 0L);

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
            ((CraftServer) Bukkit.getServer()).getHandle().respawn(nmsPlayer, (ServerLevel) nmsPlayer.level(), true, player.getLocation(), false, PlayerRespawnEvent.RespawnReason.PLUGIN);
        }
        player.updateInventory();
    }

    public static void registerHandlers() {
        DenizenNetworkManagerImpl.registerPacketHandler(ClientboundPlayerInfoUpdatePacket.class, ProfileEditorImpl::processPlayerInfoUpdatePacket);
    }

    public static ClientboundPlayerInfoUpdatePacket processPlayerInfoUpdatePacket(DenizenNetworkManagerImpl networkManager, ClientboundPlayerInfoUpdatePacket playerInfoUpdatePacket) {
        if (ProfileEditor.mirrorUUIDs.isEmpty() && !RenameCommand.hasAnyDynamicRenames() && fakeProfiles.isEmpty()) {
            return playerInfoUpdatePacket;
        }
        EnumSet<ClientboundPlayerInfoUpdatePacket.Action> actions = playerInfoUpdatePacket.actions();
        if (!actions.contains(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER) && !actions.contains(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME)) {
            return playerInfoUpdatePacket;
        }
        boolean any = false;
        for (ClientboundPlayerInfoUpdatePacket.Entry entry : playerInfoUpdatePacket.entries()) {
            if (shouldChange(entry)) {
                any = true;
                break;
            }
        }
        if (!any) {
            return playerInfoUpdatePacket;
        }
        GameProfile ownProfile = networkManager.player.getGameProfile();
        List<ClientboundPlayerInfoUpdatePacket.Entry> modifiedEntries = new ArrayList<>(playerInfoUpdatePacket.entries().size());
        for (ClientboundPlayerInfoUpdatePacket.Entry entry : playerInfoUpdatePacket.entries()) {
            if (!shouldChange(entry)) {
                modifiedEntries.add(entry);
                continue;
            }
            String rename = RenameCommand.getCustomNameFor(entry.profileId(), networkManager.player.getBukkitEntity(), false);
            GameProfile baseProfile = fakeProfiles.containsKey(entry.profileId()) ? getGameProfile(fakeProfiles.get(entry.profileId())) : entry.profile();
            GameProfile modifiedProfile = new GameProfile(baseProfile.getId(), rename != null ? (rename.length() > 16 ? rename.substring(0, 16) : rename) : baseProfile.getName());
            if (ProfileEditor.mirrorUUIDs.contains(entry.profileId())) {
                modifiedProfile.getProperties().putAll(ownProfile.getProperties());
            }
            else {
                modifiedProfile.getProperties().putAll(baseProfile.getProperties());
            }
            String listRename = RenameCommand.getCustomNameFor(entry.profileId(), networkManager.player.getBukkitEntity(), true);
            Component displayName = listRename != null ? Handler.componentToNMS(FormattedTextHelper.parse(listRename, ChatColor.WHITE)) : entry.displayName();
            ClientboundPlayerInfoUpdatePacket.Entry modifiedEntry = new ClientboundPlayerInfoUpdatePacket.Entry(entry.profileId(), modifiedProfile, entry.listed(), entry.latency(), entry.gameMode(), displayName, entry.chatSession());
            modifiedEntries.add(modifiedEntry);
        }
        return createInfoPacket(actions, modifiedEntries);
    }

    public static boolean shouldChange(ClientboundPlayerInfoUpdatePacket.Entry entry) {
        return ProfileEditor.mirrorUUIDs.contains(entry.profileId()) || RenameCommand.customNames.containsKey(entry.profileId()) || fakeProfiles.containsKey(entry.profileId());
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

    public static GameProfile getGameProfileNoProperties(PlayerProfile playerProfile) {
        UUID uuid = playerProfile.getUniqueId();
        String name = playerProfile.getName();
        return new GameProfile(uuid != null ? uuid : NIL_UUID, name != null ? name : EMPTY_NAME);
    }

    public static GameProfile getGameProfile(PlayerProfile playerProfile) {
        GameProfile gameProfile = getGameProfileNoProperties(playerProfile);
        if (playerProfile.hasTexture()) {
            gameProfile.getProperties().put("textures", new Property("textures", playerProfile.getTexture(), playerProfile.getTextureSignature()));
        }
        return gameProfile;
    }
}
