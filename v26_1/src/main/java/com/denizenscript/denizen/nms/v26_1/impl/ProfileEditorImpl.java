package com.denizenscript.denizen.nms.v1_21.impl;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.abstracts.ProfileEditor;
import com.denizenscript.denizen.nms.util.PlayerProfile;
import com.denizenscript.denizen.nms.v1_21.Handler;
import com.denizenscript.denizen.nms.v1_21.ReflectionMappingsInfo;
import com.denizenscript.denizen.nms.v1_21.helpers.PacketHelperImpl;
import com.denizenscript.denizen.nms.v1_21.impl.network.handlers.DenizenNetworkManagerImpl;
import com.denizenscript.denizen.scripts.commands.entity.RenameCommand;
import com.denizenscript.denizen.utilities.FormattedTextHelper;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.google.common.collect.ImmutableMultimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.datafixers.util.Either;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.component.ResolvableProfile;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_21_R7.CraftServer;
import org.bukkit.craftbukkit.v1_21_R7.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.util.*;

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
            ((CraftServer) Bukkit.getServer()).getHandle().respawn(nmsPlayer, true, Entity.RemovalReason.CHANGED_DIMENSION, PlayerRespawnEvent.RespawnReason.PLUGIN);
        }
        else {
            NMSHandler.playerHelper.refreshPlayer(player);
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
            PropertyMap modifiedProperties;
            if (ProfileEditor.mirrorUUIDs.contains(entry.profileId())) {
                modifiedProperties = ownProfile.properties();
            }
            else {
                // On Paper 1.19+, we use Paper's PlayerProfile API instead of this system
                modifiedProperties = Denizen.supportsPaper ? entry.profile().properties() : baseProfile.properties();
            }
            GameProfile modifiedProfile = new GameProfile(baseProfile.id(), rename != null ? (rename.length() > 16 ? rename.substring(0, 16) : rename) : baseProfile.name(), modifiedProperties);
            String listRename = RenameCommand.getCustomNameFor(entry.profileId(), networkManager.player.getBukkitEntity(), true);
            Component displayName = listRename != null ? Handler.componentToNMS(FormattedTextHelper.parse(listRename, ChatColor.WHITE)) : entry.displayName();
            ClientboundPlayerInfoUpdatePacket.Entry modifiedEntry = new ClientboundPlayerInfoUpdatePacket.Entry(entry.profileId(), modifiedProfile, entry.listed(), entry.latency(), entry.gameMode(), displayName, entry.showHat(), entry.listOrder(), entry.chatSession());
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

    public static GameProfile createGameProfile(UUID uuid, String name, String texture, String signature) {
        return new GameProfile(
                uuid != null ? uuid : NIL_UUID, name != null ? name : EMPTY_NAME,
                texture != null ? new PropertyMap(ImmutableMultimap.of("textures", new Property("textures", texture, signature))) : PropertyMap.EMPTY
        );
    }

    public static GameProfile getGameProfileNoProperties(PlayerProfile playerProfile) {
        return createGameProfile(playerProfile.getUniqueId(), playerProfile.getName(), null, null);
    }

    public static GameProfile getGameProfile(PlayerProfile playerProfile) {
        return createGameProfile(playerProfile.getUniqueId(), playerProfile.getName(), playerProfile.getTexture(), playerProfile.getTextureSignature());
    }

    public static final MethodHandle RESOLVABLEPROFILE_UNPACK = ReflectionHelper.getMethodHandle(ResolvableProfile.class, ReflectionMappingsInfo.ResolvableProfile_unpack_method);
    public static final MethodHandle RESOLVABLEPROFILE_PARTIAL_ID = ReflectionHelper
            .getFields(ReflectionHelper.getClassOrThrow("net.minecraft.world.item.component.ResolvableProfile$Partial"))
            .getGetter(ReflectionMappingsInfo.ResolvableProfilePartial_id, Optional.class);

    public static UUID getUUID(ResolvableProfile resolvableProfile) {
        try {
            Either<GameProfile, Object> unpacked = (Either<GameProfile, Object>) RESOLVABLEPROFILE_UNPACK.invokeExact(resolvableProfile);
            return unpacked.map(GameProfile::id,
                    partial -> {
                        try {
                            Optional<UUID> uuid = (Optional<UUID>) RESOLVABLEPROFILE_PARTIAL_ID.invoke(partial);
                            return uuid.orElse(null);
                        }
                        catch (Throwable e) {
                            Debug.echoError(e);
                            return null;
                        }
                    }
            );
        }
        catch (Throwable e) {
            Debug.echoError(e);
            return null;
        }
    }
}
