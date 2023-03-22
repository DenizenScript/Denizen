package com.denizenscript.denizen.paper.events;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.destroystokyo.paper.event.player.PlayerElytraBoostEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerElytraBoostScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player boosts elytra
    //
    // @Location true
    //
    // @Plugin Paper
    //
    // @Group Paper
    //
    // @Cancellable true
    //
    // @Switch with:<item> to only process the event if the firework item used matches the specified item.
    // @Switch elytra:<item> to only process the event if the elytra used matches the specified item.
    //
    // @Triggers when a player boosts their elytra with a firework rocket while gliding.
    //
    // @Context
    // <context.item> returns the firework item used to boost.
    // <context.entity> returns the firework entity that was spawned.
    // <context.should_keep> returns whether the firework item gets consumed.
    //
    // @Player Always.
    //
    // @Determine
    // "KEEP:<ElementTag(Boolean)>" to set whether the firework item should be kept.
    //
    // -->

    public PlayerElytraBoostScriptEvent() {
        registerCouldMatcher("player boosts elytra");
        registerSwitches("with", "elytra");
    }

    public PlayerElytraBoostEvent event;
    public ItemTag firework;
    public PlayerTag player;

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, player.getLocation())) {
            return false;
        }
        if (!runWithCheck(path, firework)) {
            return false;
        }
        if (!path.tryObjectSwitch("elytra", new ItemTag(player.getPlayerEntity().getEquipment().getChestplate()))) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(player, null);
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "item":
                return firework;
            case "entity":
                return new EntityTag(event.getFirework());
            case "should_keep":
                return new ElementTag(!event.shouldConsume());
        }
        return super.getContext(name);
    }

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        String determination = CoreUtilities.toLowerCase(determinationObj.toString());
        if (determination.startsWith("keep:")) {
            String value = determination.substring("keep:".length());
            event.setShouldConsume(!(new ElementTag(value).asBoolean()));
            return true;
        }
        return super.applyDetermination(path, determinationObj);
    }

    @EventHandler
    public void onPlayerElytraBoost(PlayerElytraBoostEvent event) {
        firework = new ItemTag(event.getItemStack());
        player = new PlayerTag(event.getPlayer());
        this.event = event;
        fire(event);
    }
}
