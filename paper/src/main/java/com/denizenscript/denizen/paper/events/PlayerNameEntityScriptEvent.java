package com.denizenscript.denizen.paper.events;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.paper.PaperModule;
import com.denizenscript.denizen.utilities.PaperAPITools;
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
    // @Triggers when a player attempts to rename an entity with a name tag.
    //
    // @Context
    // <context.entity> returns an EntityTag of the renamed entity.
    // <context.old_name> returns the old name of the entity, if any.
    // <context.name> returns the new name of the entity.
    // <context.persistent> returns whether this will cause the entity to persist through server restarts.
    //
    // @Determine
    // "NAME:<ElementTag>" to set a different name for the entity.
    // "PERSISTENT:<ElementTag(Boolean)>" to set whether the entity should remain through server restarts. ("True" keeps the entity across server restarts, "false" gets rid of the entity when the server restarts.)
    //
    // @Player Always.
    //
    // -->

    public PlayerNameEntityScriptEvent() {
        registerCouldMatcher("player names <entity>");
        this.<PlayerNameEntityScriptEvent, ElementTag>registerDetermination("persistent", ElementTag.class, (evt, context, determination) -> {
            event.getEntity().setPersistent(determination.asBoolean());
        });
        this.<PlayerNameEntityScriptEvent, ElementTag>registerDetermination("name", ElementTag.class, (evt, context, determination) -> {
            event.setName(PaperModule.parseFormattedText(determination.toString(), ChatColor.WHITE));
        });
    }

    public PlayerNameEntityEvent event;
    public EntityTag entity;
    public ElementTag oldName;

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
            case "old_name" -> oldName;
            case "persistent" -> new ElementTag(event.getEntity().isPersistent());
            default -> super.getContext(name);
        };
    }

    @EventHandler
    public void playerNamesEntity(PlayerNameEntityEvent event) {
        this.event = event;
        entity = new EntityTag(event.getEntity());
        String name = PaperAPITools.instance.getCustomName(entity.getBukkitEntity());
        oldName = name == null ? null : new ElementTag(name);
        fire(event);
    }
}
