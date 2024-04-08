package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEggThrowEvent;

public class PlayerThrowsEggScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player throws (hatching|non-hatching) egg
    //
    // @Group Player
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers when a player throws an egg - this event specifically fires when the egg hits, for the initial throw event use <@link event projectile launched>.
    //
    // @Context
    // <context.egg> returns the EntityTag of the egg.
    // <context.is_hatching> returns an ElementTag with a value of "true" if the egg will hatch and "false" otherwise.
    //
    // @Determine
    // EntityTag to set the type of the hatching entity.
    //
    // @Player Always.
    //
    // -->

    public PlayerThrowsEggScriptEvent() {
        registerCouldMatcher("player throws (hatching|non-hatching) egg");
    }

    public EntityTag egg;
    public PlayerEggThrowEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        if (path.eventArgLowerAt(2).equals("hatching") && !event.isHatching()) {
            return false;
        }
        if (path.eventArgLowerAt(2).equals("non-hatching") && event.isHatching()) {
            return false;
        }

        if (!runInCheck(path, egg.getLocation())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        String determination = determinationObj.toString();
        if (EntityTag.matches(determination)) {
            event.setHatching(true);
            EntityType type = EntityTag.valueOf(determination, getTagContext(path)).getBukkitEntityType();
            event.setHatchingType(type);
            return true;
        }
        return super.applyDetermination(path, determinationObj);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(event.getPlayer());
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("is_hatching")) {
            return new ElementTag(event.isHatching());
        }
        else if (name.equals("egg")) {
            return egg;
        }
        return super.getContext(name);
    }

    @Override
    public void cancellationChanged() {
        event.setHatching(!cancelled);
    }

    @EventHandler
    public void onPlayerThrowsEgg(PlayerEggThrowEvent event) {
        if (EntityTag.isNPC(event.getPlayer())) {
            return;
        }
        Entity eggEntity = event.getEgg();
        EntityTag.rememberEntity(eggEntity);
        egg = new EntityTag(event.getEgg());
        this.event = event;
        fire(event);
        EntityTag.forgetEntity(eggEntity);
    }
}
