package com.denizenscript.denizen.paper.events;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.ChunkTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import io.papermc.paper.event.packet.PlayerChunkUnloadEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerChunkUnloadScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player receives chunk unload
    //
    // @Group Paper
    //
    // @Location true
    //
    // @Plugin Paper
    //
    // @Warning This event will fire *extremely* rapidly and almost guarantees lag. Use with maximum caution.
    //
    // @Triggers when a Player receives a chunk unload packet.
    // Should only be used for packet/clientside related stuff. Not intended for modifying server side.
    // Generally prefer <@link event chunk unloads> in most cases.
    //
    // @Context
    // <context.chunk> returns a ChunkTag of the chunk being unloaded.
    //
    // @Player Always.
    // -->

    public PlayerChunkUnloadScriptEvent() {
        registerCouldMatcher("player receives chunk unload");
    }

    public PlayerChunkUnloadEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, event.getPlayer().getLocation())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(event.getPlayer());
    }

    @Override
    public ObjectTag getContext(String name) {
        return switch (name) {
            case "chunk" -> new ChunkTag(event.getChunk());
            default -> super.getContext(name);
        };
    }

    @EventHandler
    public void playerChunkUnloadEvent(PlayerChunkUnloadEvent event) {
        this.event = event;
        fire(event);
    }
}
