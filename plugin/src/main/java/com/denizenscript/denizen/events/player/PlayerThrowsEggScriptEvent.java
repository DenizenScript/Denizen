package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEggThrowEvent;

public class PlayerThrowsEggScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player throws (hatching/non-hatching) egg
    //
    // @Regex ^on player throws( (hatching|non-hatching))? egg$
    // @Switch in <area>
    //
    // @Cancellable true
    //
    // @Triggers when a player throws an egg.
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
        instance = this;
    }

    public static PlayerThrowsEggScriptEvent instance;
    public EntityTag egg;
    public Boolean is_hatching;
    private EntityType type;
    public PlayerEggThrowEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        return lower.startsWith("player throws") && lower.contains("egg");
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (path.eventArgLowerAt(2).equals("hatching") && !is_hatching) {
            return false;
        }
        if (path.eventArgLowerAt(2).equals("non-hatching") && is_hatching) {
            return false;
        }

        return runInCheck(path, egg.getLocation());
    }

    @Override
    public String getName() {
        return "PlayerThrowsEgg";
    }

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        String determination = determinationObj.toString();
        if (EntityTag.matches(determination)) {
            is_hatching = true;
            type = EntityTag.valueOf(determination).getBukkitEntityType();
            return true;
        }
        return super.applyDetermination(path, determinationObj);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(new PlayerTag(event.getPlayer()), null);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("is_hatching")) {
            return new ElementTag(is_hatching);
        }
        else if (name.equals("egg")) {
            return egg;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPlayerThrowsEgg(PlayerEggThrowEvent event) {
        if (EntityTag.isNPC(event.getPlayer())) {
            return;
        }
        is_hatching = event.isHatching();
        Entity eggEntity = event.getEgg();
        EntityTag.rememberEntity(eggEntity);
        egg = new EntityTag(event.getEgg());
        type = event.getHatchingType();
        this.event = event;
        cancelled = false;
        fire(event);
        if (cancelled) {
            is_hatching = false;
        }
        EntityTag.forgetEntity(eggEntity);
        event.setHatching(is_hatching);
        event.setHatchingType(type);
    }
}
