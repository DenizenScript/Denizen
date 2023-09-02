package com.denizenscript.denizen.nms.v1_20.impl.network.handlers.packet;

import com.denizenscript.denizen.events.player.PlayerReceivesActionbarScriptEvent;
import com.denizenscript.denizen.nms.v1_20.Handler;
import com.denizenscript.denizen.nms.v1_20.impl.network.handlers.DenizenNetworkManagerImpl;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.FormattedTextHelper;
import com.denizenscript.denizencore.objects.core.ElementTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;

public class ActionBarEventPacketHandlers {

    public static void registerHandlers() {
        DenizenNetworkManagerImpl.registerPacketHandler(ClientboundSetActionBarTextPacket.class, ActionBarEventPacketHandlers::processActionbarPacket);
    }

    public static Packet<ClientGamePacketListener> processActionbarPacket(DenizenNetworkManagerImpl networkManager, Packet<ClientGamePacketListener> packet) {
        if (!PlayerReceivesActionbarScriptEvent.instance.loaded) {
            return packet;
        }
        if (packet instanceof ClientboundSetActionBarTextPacket) {
            ClientboundSetActionBarTextPacket actionbarPacket = (ClientboundSetActionBarTextPacket) packet;
            PlayerReceivesActionbarScriptEvent event = PlayerReceivesActionbarScriptEvent.instance;
            Component baseComponent = actionbarPacket.getText();
            event.reset();
            event.message = new ElementTag(FormattedTextHelper.stringify(Handler.componentToSpigot(baseComponent)));
            event.rawJson = new ElementTag(Component.Serializer.toJson(baseComponent));
            event.system = new ElementTag(false);
            event.player = PlayerTag.mirrorBukkitPlayer(networkManager.player.getBukkitEntity());
            event = (PlayerReceivesActionbarScriptEvent) event.triggerNow();
            if (event.cancelled) {
                return null;
            }
            if (event.modified) {
                Component component = Handler.componentToNMS(event.altMessageDetermination);
                ClientboundSetActionBarTextPacket newPacket = new ClientboundSetActionBarTextPacket(component);
                return newPacket;
            }
        }
        return packet;
    }
}
