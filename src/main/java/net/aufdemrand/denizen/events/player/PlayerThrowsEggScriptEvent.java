package net.aufdemrand.denizen.events.player;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.ScriptEntryData;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEggThrowEvent;

public class PlayerThrowsEggScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player throws (hatching/non-hatching) egg (in <area>)
    //
    // @Regex ^on player throws( (hatching|non-hatching))? egg( in ((notable (cuboid|ellipsoid))|([^\s]+)))?$
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
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        if (CoreUtilities.getXthArg(2, lower).equals("hatching") && !is_hatching) {
            return false;
        }
        if (CoreUtilities.getXthArg(2, lower).equals("non-hatching") && is_hatching) {
            return false;
        }

        return runInCheck(scriptContainer, s, lower, egg.getLocation());
    }

    @Override
    public String getName() {
        return "PlayerThrowsEgg";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        PlayerEggThrowEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        String lower = CoreUtilities.toLowerCase(determination);
        if (dEntity.matches(lower)) {
            is_hatching = true;
            type = dEntity.valueOf(determination).getBukkitEntityType();
            return true;
        }
        if (lower.equals("cancelled")) {
            is_hatching = false;
            return true;
        }
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(dEntity.isPlayer(event.getPlayer()) ? dEntity.getPlayerFrom(event.getPlayer()) : null, null);
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

    @EventHandler(ignoreCancelled = true)
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
        fire();
        dEntity.forgetEntity(eggEntity);
        event.setHatching(is_hatching);
        event.setHatchingType(type);
    }
}
