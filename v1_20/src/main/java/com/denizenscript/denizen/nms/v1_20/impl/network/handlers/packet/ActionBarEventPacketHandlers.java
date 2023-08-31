package com.denizenscript.denizen.nms.v1_20.impl.network.handlers.packet;

import com.denizenscript.denizen.events.player.PlayerReceivesActionbarScriptEvent;
import com.denizenscript.denizen.nms.v1_20.Handler;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.FormattedTextHelper;
import com.denizenscript.denizencore.objects.core.ElementTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;

public class ActionBarEventPacketHandlers {

    public static void registerHandlers() {

    }

    public boolean processActionbarPacket(Packet<?> packet, PacketSendListener genericfuturelistener) {
        if (!PlayerReceivesActionbarScriptEvent.instance.loaded) {
            return false;
        }
        if (packet instanceof ClientboundSetActionBarTextPacket) {
            ClientboundSetActionBarTextPacket actionbarPacket = (ClientboundSetActionBarTextPacket) packet;
            PlayerReceivesActionbarScriptEvent event = PlayerReceivesActionbarScriptEvent.instance;
            Component baseComponent = actionbarPacket.getText();
            event.reset();
            event.message = new ElementTag(FormattedTextHelper.stringify(Handler.componentToSpigot(baseComponent)));
            event.rawJson = new ElementTag(Component.Serializer.toJson(baseComponent));
            event.system = new ElementTag(false);
            event.player = PlayerTag.mirrorBukkitPlayer(player.getBukkitEntity());
            event = (PlayerReceivesActionbarScriptEvent) event.triggerNow();
            if (event.cancelled) {
                return true;
            }
            if (event.modified) {
                Component component = Handler.componentToNMS(event.altMessageDetermination);
                ClientboundSetActionBarTextPacket newPacket = new ClientboundSetActionBarTextPacket(component);
                oldManager.send(newPacket, genericfuturelistener);
                return true;
            }
        }
        return false;
    }
}
