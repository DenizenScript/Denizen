package com.denizenscript.denizen.events.world;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.ChunkTag;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.EntitiesLoadEvent;

public class ChunkLoadEntitiesScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // chunk loads entities
    //
    // @Group World
    //
    // @Location true
    //
    // @Warning This event will fire very rapidly.
    //
    // @Switch entity_type:<type-matcher> to only fire in the chunk contains an entity that matches the given entity matcher.
    // @Switch include_empty:<true/false> defaults to false, set to 'true' to include chunks loading an empty set of entities.
    //
    // @Triggers when a chunk loads in its entities.
    //
    // @Context
    // <context.chunk> returns the loading chunk.
    // <context.entities> returns a ListTag of all entities to be loaded.
    //
    // -->

    public ChunkLoadEntitiesScriptEvent() {
        registerCouldMatcher("chunk loads entities");
        registerSwitches("entity_type", "include_empty");
    }


    public ChunkTag chunk;
    public EntitiesLoadEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        if (path.checkSwitch("include_empty", "false") && event.getEntities().isEmpty()) {
            return false;
        }
        if (!runInCheck(path, chunk.getCenter())) {
            return false;
        }
        String typeMatch = path.switches.get("entity_type");
        if (typeMatch != null) {
            boolean any = false;
            for (Entity e : event.getEntities()) {
                any = new EntityTag(e).tryAdvancedMatcher(typeMatch);
                if (any) {
                    break;
                }
            }
            if (!any) {
                return false;
            }
        }
        return super.matches(path);
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "chunk": return chunk;
            case "entities":
                ListTag entList = new ListTag();
                for (Entity e : event.getEntities()) {
                    entList.addObject(new EntityTag(e));
                }
                return entList;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onChunkLoad(EntitiesLoadEvent event) {
        chunk = new ChunkTag(event.getChunk());
        this.event = event;
        fire(event);
    }
}
