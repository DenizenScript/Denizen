package com.denizenscript.denizen.nms.v1_20.impl.network.handlers.packet;

import com.denizenscript.denizen.events.player.PlayerReceivesTablistUpdateScriptEvent;
import com.denizenscript.denizen.nms.v1_20.Handler;
import com.denizenscript.denizen.nms.v1_20.impl.ProfileEditorImpl;
import com.denizenscript.denizen.nms.v1_20.impl.network.handlers.DenizenNetworkManagerImpl;
import com.denizenscript.denizen.utilities.FormattedTextHelper;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.google.common.base.Joiner;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.world.level.GameType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class TablistUpdateEventPacketHandlers {

    public static void registerHandlers() {
        DenizenNetworkManagerImpl.registerPacketHandler(ClientboundPlayerInfoUpdatePacket.class, TablistUpdateEventPacketHandlers::processTablistPacket);
        DenizenNetworkManagerImpl.registerPacketHandler(ClientboundPlayerInfoRemovePacket.class, TablistUpdateEventPacketHandlers::processTablistPacket);
    }

    public static boolean tablistBreakOnlyOnce = false;

    // TODO: properly rebundle the packet instead of splitting it up
    public static Packet<ClientGamePacketListener> processTablistPacket(DenizenNetworkManagerImpl networkManager, Packet<ClientGamePacketListener> packet) {
        if (!PlayerReceivesTablistUpdateScriptEvent.instance.eventData.isEnabled) {
            return packet;
        }
        if (packet instanceof ClientboundPlayerInfoUpdatePacket) {
            ClientboundPlayerInfoUpdatePacket infoPacket = (ClientboundPlayerInfoUpdatePacket) packet;
            String mode = "";
            for (ClientboundPlayerInfoUpdatePacket.Action action : infoPacket.actions()) {
                switch (action) {
                    case ADD_PLAYER:
                        mode = "add";
                        break;
                    case UPDATE_LATENCY:
                        mode = mode.isEmpty() ? "update_latency" : mode + "|update_latency";
                        break;
                    case UPDATE_GAME_MODE:
                        mode = mode.isEmpty() ? "update_gamemode" : mode + "|update_gamemode";
                        break;
                    case UPDATE_DISPLAY_NAME:
                        mode = mode.isEmpty() ? "update_display" : mode + "|update_display";
                        break;
                    case UPDATE_LISTED:
                        mode = mode.isEmpty() ? "update_listed" : mode + "|update_listed";
                        break;
                    case INITIALIZE_CHAT:
                        mode = mode.isEmpty() ? "initialize_chat" : mode + "|initialize_chat";
                    default:
                        break;
                }
            }
            if (mode.isEmpty()) {
                if (!tablistBreakOnlyOnce) {
                    tablistBreakOnlyOnce = true;
                    Debug.echoError("Tablist packet processing failed: unknown action " + Joiner.on(", ").join(infoPacket.actions()));
                }
                return packet;
            }
            boolean isOverriding = false;
            for (ClientboundPlayerInfoUpdatePacket.Entry update : infoPacket.entries()) {
                GameProfile profile = update.profile();
                String texture = null, signature = null;
                if (profile.getProperties().containsKey("textures")) {
                    Property property = profile.getProperties().get("textures").stream().findFirst().get();
                    texture = property.value();
                    signature = property.signature();
                }
                String modeText = update.gameMode() == null ? null : update.gameMode().name();
                PlayerReceivesTablistUpdateScriptEvent.TabPacketData data = new PlayerReceivesTablistUpdateScriptEvent.TabPacketData(mode, profile.getId(), update.listed(), profile.getName(),
                        update.displayName() == null ? null : FormattedTextHelper.stringify(Handler.componentToSpigot(update.displayName())), modeText, texture, signature, update.latency());
                PlayerReceivesTablistUpdateScriptEvent.fire(networkManager.player.getBukkitEntity(), data);
                if (data.modified) {
                    if (!isOverriding) {
                        isOverriding = true;
                        for (ClientboundPlayerInfoUpdatePacket.Entry priorUpdate : infoPacket.entries()) {
                            if (priorUpdate == update) {
                                break;
                            }
                            networkManager.oldManager.send(ProfileEditorImpl.createInfoPacket(infoPacket.actions(), Collections.singletonList(priorUpdate)));
                        }
                    }
                    if (!data.cancelled) {
                        GameProfile newProfile = new GameProfile(data.id, data.name);
                        if (data.texture != null) {
                            newProfile.getProperties().put("textures", new Property("textures", data.texture, data.signature));
                        }
                        ClientboundPlayerInfoUpdatePacket.Entry entry = new ClientboundPlayerInfoUpdatePacket.Entry(newProfile.getId(), newProfile, data.isListed, data.latency, data.gamemode == null ? null : GameType.byName(CoreUtilities.toLowerCase(data.gamemode)),
                                data.display == null ? null : Handler.componentToNMS(FormattedTextHelper.parse(data.display, ChatColor.WHITE)), update.chatSession());
                        networkManager.oldManager.send(ProfileEditorImpl.createInfoPacket(infoPacket.actions(), Collections.singletonList(entry)));
                    }
                }
                else if (isOverriding) {
                    networkManager.oldManager.send(ProfileEditorImpl.createInfoPacket(infoPacket.actions(), Collections.singletonList(update)));
                }
            }
            return isOverriding ? null : packet;
        }
        else if (packet instanceof ClientboundPlayerInfoRemovePacket) {
            ClientboundPlayerInfoRemovePacket removePacket = (ClientboundPlayerInfoRemovePacket) packet;
            boolean modified = false;
            List<UUID> altIds = new ArrayList<>(((ClientboundPlayerInfoRemovePacket) packet).profileIds());
            for (UUID id : ((ClientboundPlayerInfoRemovePacket) packet).profileIds()) {
                PlayerReceivesTablistUpdateScriptEvent.TabPacketData data = new PlayerReceivesTablistUpdateScriptEvent.TabPacketData("remove", id, false, null, null, null, null, null, 0);
                PlayerReceivesTablistUpdateScriptEvent.fire(networkManager.player.getBukkitEntity(), data);
                if (data.modified && data.cancelled) {
                    modified = true;
                    altIds.remove(id);
                }
            }
            if (modified) {
                return new ClientboundPlayerInfoRemovePacket(altIds);
            }
        }
        return packet;
    }
}
