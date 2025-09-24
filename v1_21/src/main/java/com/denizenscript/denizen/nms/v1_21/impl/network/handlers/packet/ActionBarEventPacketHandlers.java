package com.denizenscript.denizen.nms.v1_21.impl.network.handlers.packet;

import com.denizenscript.denizen.events.player.PlayerReceivesActionbarScriptEvent;
import com.denizenscript.denizen.nms.v1_21.impl.network.handlers.DenizenNetworkManagerImpl;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.PaperAPITools;
import com.denizenscript.denizencore.objects.core.ElementTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import org.bukkit.craftbukkit.v1_21_R5.util.CraftChatMessage;

public class ActionBarEventPacketHandlers {

    public static void registerHandlers() {
        DenizenNetworkManagerImpl.registerPacketHandler(ClientboundSetActionBarTextPacket.class, ActionBarEventPacketHandlers::processActionbarPacket);
    }

    public static ClientboundSetActionBarTextPacket processActionbarPacket(DenizenNetworkManagerImpl networkManager, ClientboundSetActionBarTextPacket actionbarPacket) {
        PlayerReceivesActionbarScriptEvent event = PlayerReceivesActionbarScriptEvent.instance;
        if (!event.loaded) {
            return actionbarPacket;
        }
        event.reset();
        String rawJson = CraftChatMessage.toJSON(actionbarPacket.text());
        event.message = new ElementTag(PaperAPITools.instance.parseJsonToText(rawJson), true);
        event.rawJson = new ElementTag(rawJson, true);
        event.system = new ElementTag(false);
        event.player = PlayerTag.mirrorBukkitPlayer(networkManager.player.getBukkitEntity());
        event = (PlayerReceivesActionbarScriptEvent) event.triggerNow();
        if (event.cancelled) {
            return null;
        }
        if (event.modified) {
            return new ClientboundSetActionBarTextPacket(CraftChatMessage.fromJSON(event.rawJson.asString()));
        }
        return actionbarPacket;
    }
}
