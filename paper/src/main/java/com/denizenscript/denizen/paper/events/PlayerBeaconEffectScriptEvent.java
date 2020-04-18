package com.denizenscript.denizen.paper.events;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.objects.properties.item.ItemPotion;
import com.denizenscript.denizen.utilities.debugging.Debug;
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
    // @Regex ^on player beacon effect applied$
    //
    // @Switch in:<area> to only process the event if it occurred within a specified area.
    //
    // @Plugin Paper
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
        instance = this;
    }

    public static PlayerBeaconEffectScriptEvent instance;
    public BeaconEffectEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        return path.eventLower.startsWith("player beacon effect applied");
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, event.getPlayer().getLocation())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public String getName() {
        return "PlayerBeaconEffect";
    }

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        if (!isDefaultDetermination(determinationObj)) {
            try {
                event.setEffect(ItemPotion.parseEffect(determinationObj.toString()));
            }
            catch (Exception e) {
                Debug.echoError(e);
            }
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
        if (name.equals("location")) {
            return new LocationTag(event.getBlock().getLocation());
        }
        else if (name.equals("effect")) {
            return new ElementTag(ItemPotion.stringifyEffect(event.getEffect()));
        }
        else if (name.equals("effect_type")) {
            return new ElementTag(event.getEffect().getType().getName());
        }
        else if (name.equals("is_primary")) {
            return new ElementTag(event.isPrimary());
        }
        return super.getContext(name);
    }

    @EventHandler
    public void beaconEffectEvent(BeaconEffectEvent event) {
        this.event = event;
        fire(event);
    }
}
