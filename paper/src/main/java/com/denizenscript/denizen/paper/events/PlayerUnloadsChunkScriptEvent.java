package com.denizenscript.denizen.paper.events;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.ChunkTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import io.papermc.paper.event.packet.PlayerChunkUnloadEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerUnloadsChunkScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player unloads chunk
    //
    // @Group Paper
    //
    // @Location true
    //
    // @Plugin Paper
    //
    // @Triggers when a player unloads a chunk.
    //
    // @Context
    // <context.player> returns a PlayerTag of the player that unloads the chunk.
    // <context.chunk> returns a ChunkTag of the chunk being unloaded.
    // -->

    public PlayerUnloadsChunkScriptEvent() {
        registerCouldMatcher("player unloads chunk");
    }

    public PlayerChunkUnloadEvent event;
    public PlayerTag player;

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, player.getLocation())) {
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
            case "player" -> new PlayerTag(event.getPlayer());
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
