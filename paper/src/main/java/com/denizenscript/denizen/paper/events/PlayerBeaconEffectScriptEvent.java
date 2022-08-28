package com.denizenscript.denizen.paper.events;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.properties.item.ItemPotion;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.destroystokyo.paper.event.block.BeaconEffectEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerBeaconEffectScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player beacon effect applied
    //
    // @Location true
    //
    // @Plugin Paper
    //
    // @Group Paper
    //
    // @Cancellable true
    //
    // @Triggers when a beacon applies an effect to a player.
    //
    // @Context
    // <context.location> returns the LocationTag of the beacon applying an effect.
    // <context.effect> returns an ElementTag of the potion effect (in the same format as <@link tag EntityTag.list_effects>).
    // <context.effect_type> returns an ElementTag of the effect type.
    // <context.is_primary> returns an ElementTag(Boolean) of whether the beacon effect is the primary effect.
    //
    // @Determine
    // ElementTag to change the applied potion effect (in the same format as <@link tag EntityTag.list_effects>).
    //
    // @Player Always.
    //
    // -->

    public PlayerBeaconEffectScriptEvent() {
        registerCouldMatcher("player beacon effect applied");
    }

    public BeaconEffectEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, event.getPlayer().getLocation())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        try {
            event.setEffect(ItemPotion.parseEffect(determinationObj.toString(), getTagContext(path)));
            return true;
        }
        catch (Exception e) {
            Debug.echoError(e);
        }
        return super.applyDetermination(path, determinationObj);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(event.getPlayer());
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "location":
                return new LocationTag(event.getBlock().getLocation());
            case "effect":
                return new ElementTag(ItemPotion.stringifyEffect(event.getEffect()));
            case "effect_type":
                return new ElementTag(event.getEffect().getType().getName());
            case "is_primary":
                return new ElementTag(event.isPrimary());
        }
        return super.getContext(name);
    }

    @EventHandler
    public void beaconEffectEvent(BeaconEffectEvent event) {
        if (EntityTag.isNPC(event.getPlayer())) {
            return;
        }
        this.event = event;
        fire(event);
    }
}
