package com.denizenscript.denizen.paper.events;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.paper.PaperModule;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import io.papermc.paper.event.player.PlayerNameEntityEvent;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerNameEntityScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player names <entity>
    //
    // @Location true
    //
    // @Plugin Paper
    //
    // @Group Paper
    //
    // @Cancellable true
    //
    // @Triggers when a player is attempting to rename an entity.
    //
    // @Context
    // <context.entity> returns an EntityTag of the renamed entity.
    // <context.old_name> returns the old name of the entity.
    // <context.name> returns the new name of the entity.
    // <context.persistent> returns whether this will cause the entity to persist through server restarts.
    //
    // @Determine
    // "NAME:ElementTag" to set a different name for the entity.
    // "PERSISTENT" to indicate that the entity should remain persistent.
    // "NOT_PERSISTENT" to indicate that the entity should not remain persistent.
    //
    // @Player Always.
    //
    // -->

    public PlayerNameEntityScriptEvent() {
        registerCouldMatcher("player names <entity>");
        this.<PlayerNameEntityScriptEvent>registerTextDetermination("persistent", (evt) -> {
            event.setPersistent(true);
        });
        this.<PlayerNameEntityScriptEvent>registerTextDetermination("not_persistent", (evt) -> {
            event.setPersistent(false);
        });
        this.<PlayerNameEntityScriptEvent, ElementTag>registerDetermination("name", ElementTag.class, (evt, context, determination) -> {
            event.setName(PaperModule.parseFormattedText(determination.toString(), ChatColor.WHITE));
        });
    }

    public PlayerNameEntityEvent event;
    public EntityTag entity;

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, entity.getLocation())) {
            return false;
        }
        if (!path.tryArgObject(2, entity)) {
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
            case "entity" -> entity.getDenizenObject();
            case "name" -> new ElementTag(PaperModule.stringifyComponent(event.getName()));
            case "old_name" -> new ElementTag(entity.getName());
            case "persistent" -> new ElementTag(event.isPersistent());
            default -> super.getContext(name);
        };
    }

    @EventHandler
    public void playerNamesEntity(PlayerNameEntityEvent event) {
        this.event = event;
        entity = new EntityTag(event.getEntity());
        fire(event);
    }
}
