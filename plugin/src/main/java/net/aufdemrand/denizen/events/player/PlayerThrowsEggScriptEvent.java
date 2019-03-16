package net.aufdemrand.denizen.events.player;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.ScriptEntryData;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
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
    // <context.egg> returns the dEntity of the egg.
    // <context.is_hatching> returns an Element with a value of "true" if the egg will hatch and "false" otherwise.
    //
    // @Determine
    // dEntity to set the type of the hatching entity.
    //
    // -->

    public PlayerThrowsEggScriptEvent() {
        instance = this;
    }

    public static PlayerThrowsEggScriptEvent instance;
    public dEntity egg;
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
    public boolean applyDetermination(ScriptContainer container, String determination) {
        String lower = CoreUtilities.toLowerCase(determination);
        if (dEntity.matches(lower)) {
            is_hatching = true;
            type = dEntity.valueOf(determination).getBukkitEntityType();
            return true;
        }
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(new dPlayer(event.getPlayer()), null);
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("is_hatching")) {
            return new Element(is_hatching);
        }
        else if (name.equals("egg")) {
            return egg;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPlayerThrowsEgg(PlayerEggThrowEvent event) {
        if (dEntity.isNPC(event.getPlayer())) {
            return;
        }
        dB.log("Is this even firing?");
        is_hatching = event.isHatching();
        Entity eggEntity = event.getEgg();
        dEntity.rememberEntity(eggEntity);
        egg = new dEntity(event.getEgg());
        type = event.getHatchingType();
        this.event = event;
        cancelled = false;
        fire();
        if (cancelled) {
            is_hatching = false;
        }
        dEntity.forgetEntity(eggEntity);
        event.setHatching(is_hatching);
        event.setHatchingType(type);
    }
}
