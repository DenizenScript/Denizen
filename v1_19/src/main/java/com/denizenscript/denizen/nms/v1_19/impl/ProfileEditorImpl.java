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
import io.netty.buffer.Unpooled;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_19_R2.CraftServer;
import org.bukkit.craftbukkit.v1_19_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.*;

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
        EnumSet<ClientboundPlayerInfoUpdatePacket.Action> action = packet.actions();
        if (!action.contains(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER) && !action.contains(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME)) {
            return true;
        }
        List<ClientboundPlayerInfoUpdatePacket.Entry> dataList = packet.entries();
        if (dataList == null) {
            return true;
        }
        try {
            boolean any = false;
            for (ClientboundPlayerInfoUpdatePacket.Entry data : dataList) {
                if (ProfileEditor.mirrorUUIDs.contains(data.profileId()) || RenameCommand.customNames.containsKey(data.profileId()) || fakeProfiles.containsKey(data.profileId())) {
                    any = true;
                }
            }
            if (!any) {
                return true;
            }
            GameProfile ownProfile = manager.player.getGameProfile();
            for (ClientboundPlayerInfoUpdatePacket.Entry data : dataList) {
                if (!ProfileEditor.mirrorUUIDs.contains(data.profileId()) && !RenameCommand.customNames.containsKey(data.profileId()) && !fakeProfiles.containsKey(data.profileId())) {
                    manager.oldManager.send(createInfoPacket(action, Collections.singletonList(data)));
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
                    manager.oldManager.send(createInfoPacket(action, Collections.singletonList(newData)));
                }
            }
            return false;
        }
        catch (Exception e) {
            Debug.echoError(e);
            return true;
        }
    }

    public static final Field ClientboundPlayerInfoUpdatePacket_Action_writer = ReflectionHelper.getFields(ClientboundPlayerInfoUpdatePacket.Action.class).getFirstOfType(ClientboundPlayerInfoUpdatePacket.Action.Writer.class);

    public static ClientboundPlayerInfoUpdatePacket createInfoPacket(EnumSet<ClientboundPlayerInfoUpdatePacket.Action> actions, List<ClientboundPlayerInfoUpdatePacket.Entry> entries) {
        // Based on ClientboundPlayerInfoUpdatePacket#write
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeEnumSet(actions, ClientboundPlayerInfoUpdatePacket.Action.class);
        buf.writeCollection(entries, (inBuf, entry) -> {
            try {
                inBuf.writeUUID(entry.profileId());
                Iterator var3 = actions.iterator();
                while (var3.hasNext()) {
                    ClientboundPlayerInfoUpdatePacket.Action action = (ClientboundPlayerInfoUpdatePacket.Action) var3.next();
                    ClientboundPlayerInfoUpdatePacket.Action.Writer writer = (ClientboundPlayerInfoUpdatePacket.Action.Writer) ClientboundPlayerInfoUpdatePacket_Action_writer.get(action);
                    writer.write(inBuf, entry);
                }
            }
            catch (Throwable ex) {
                Debug.echoError(ex);
            }
        });
        return new ClientboundPlayerInfoUpdatePacket(buf);
    }

    private static GameProfile getGameProfile(PlayerProfile playerProfile) {
        GameProfile gameProfile = new GameProfile(playerProfile.getUniqueId(), playerProfile.getName());
        if (playerProfile.hasTexture()) {
            gameProfile.getProperties().put("textures", new Property("textures", playerProfile.getTexture(), playerProfile.getTextureSignature()));
        }
        return gameProfile;
    }
}
