package com.denizenscript.denizen.paper.events;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import io.papermc.paper.connection.PlayerGameConnection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLinksSendEvent;

public class PlayerLinksSendScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player links send
    //
    // @Group Player
    //
    // @Plugin Paper
    //
    // @Triggers when the list of links is sent to the player
    //
    // @Determine
    // "LINKS:<ListTag(MapTag)>" to change what links should player see. Each map needs to have "uri" key which represents URI address of this link and either display or a type. Valid types are listed at <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/ServerLinks.Type.html>
    //
    // @Player when the event part of player game connection
    //
    // -->

    public PlayerLinksSendEvent event;
    public Player player;

    public PlayerLinksSendScriptEvent() {
        registerCouldMatcher("player links send");
        this.<PlayerLinksSendScriptEvent, ListTag>registerDetermination("links", ListTag.class, (evt, context, value) -> {
            Utilities.replaceServerLinks(evt.event.getLinks(), value, context);
        });
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(player);
    }

    @EventHandler
    public void onPlayerLinksSend(PlayerLinksSendEvent event) {
        player = event.getConnection() instanceof PlayerGameConnection connection ? connection.getPlayer() : null;
        this.event = event;
        fire(event);
    }
}
