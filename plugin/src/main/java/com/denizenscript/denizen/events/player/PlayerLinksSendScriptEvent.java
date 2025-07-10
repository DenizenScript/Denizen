package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.utilities.debugging.Debug;
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
    // @Triggers when a player opens server links.
    //
    // @Determine
    // "LINKS:<ListTag(MapTag)>" to change what links should player see. Each map needs to have "uri" key which represents URI address of this link and either display or a type. Valid types are listed at <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/ServerLinks.Type.html>
    //
    // @Player Always.
    //
    // -->

    public PlayerLinksSendEvent event;

    public PlayerLinksSendScriptEvent() {
        registerCouldMatcher("player links send");
        this.<PlayerLinksSendScriptEvent, ListTag>registerDetermination("links", ListTag.class, (evt, context, value) -> {
            Utilities.replaceServerLinks(evt.event.getLinks(), value, context);
        });
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(event.getPlayer());
    }

    @EventHandler
    public void onPlayerLinksSend(PlayerLinksSendEvent event) {
        this.event = event;
        fire(event);
    }
}
