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
    // <context.effect_data> returns a MapTag of the potion effect in <@link language Potion Effect Format>.
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
            event.setEffect(ItemPotion.parseLegacyEffectString(determinationObj.toString(), getTagContext(path)));
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
        return switch (name) {
            case "location" -> new LocationTag(event.getBlock().getLocation());
            case "effect" -> new ElementTag(ItemPotion.effectToLegacyString(event.getEffect(), null));
            case "effect_data" -> ItemPotion.effectToMap(event.getEffect(), true);
            case "effect_type" -> new ElementTag(event.getEffect().getType().getName());
            case "is_primary" -> new ElementTag(event.isPrimary());
            default -> super.getContext(name);
        };
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
