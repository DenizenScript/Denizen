package com.denizenscript.denizen.paper.events;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import io.papermc.paper.connection.PlayerConfigurationConnection;
import io.papermc.paper.connection.PlayerGameConnection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLinksSendEvent;

public class PlayerLinksSendScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player receives links
    //
    // @Group Paper
    //
    // @Plugin Paper
    //
    // @Triggers when a player receives a list of links
    //
    // @Determine
    // "LINKS:<ListTag(MapTag)>" to set the links sent to the player. Each item in the list must be a MapTag in <@link language Server Links Format>.
    // "ADD_LINKS:<ListTag(MapTag)>" to add the links sent to the player. Each item in the list must be a MapTag in <@link language Server Links Format>.
    //
    // @Player Always.
    //
    // @Warning this may fire early in the player login process and thus the linked player is essentially an offline player
    //
    // -->

    public PlayerLinksSendEvent event;
    public PlayerTag player;

    public PlayerLinksSendScriptEvent() {
        registerCouldMatcher("player receives links");
        this.<PlayerLinksSendScriptEvent, ListTag>registerDetermination("set_links", ListTag.class, (evt, context, value) -> {
            Utilities.replaceServerLinks(evt.event.getLinks(), value, context);
        });
        this.<PlayerLinksSendScriptEvent, ListTag>registerDetermination("add_links", ListTag.class, (evt, context, value) -> {
            Utilities.fillServerLinks(evt.event.getLinks(), value, context);
        });
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(player, null);
    }

    @EventHandler
    public void onPlayerLinksSend(PlayerLinksSendEvent event) {
        if (event.getConnection() instanceof PlayerGameConnection connection) {
            player = new PlayerTag(connection.getPlayer());
        }
        else if (event.getConnection() instanceof PlayerConfigurationConnection connection) {
            player = new PlayerTag(connection.getProfile().getId());
        }
        else {
            player = null;
        }
        this.event = event;
        fire(event);
    }
}
