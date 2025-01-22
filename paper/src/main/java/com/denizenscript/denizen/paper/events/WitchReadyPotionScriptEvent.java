package com.denizenscript.denizen.paper.events;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.destroystokyo.paper.event.entity.WitchReadyPotionEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class WitchReadyPotionScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // witch prepares to throw potion
    //
    // @Location true
    //
    // @Plugin Paper
    //
    // @Group Paper
    //
    // @Cancellable true
    //
    // @Triggers when a witch is preparing to throw a potion, for the witch actually throwing the potion, use <@link event witch throws potion>.
    //
    // @Context
    // <context.entity> returns an EntityTag of the witch that is preparing to throw a potion.
    // <context.potion> returns an ItemTag of the potion being prepared to be thrown.
    //
    // -->

    public WitchReadyPotionScriptEvent() {
        registerCouldMatcher("witch prepares to throw potion");
    }

    public WitchReadyPotionEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, event.getEntity().getLocation())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(event.getEntity());
    }

    @Override
    public ObjectTag getContext(String name) {
        return switch (name) {
            case "entity" -> new EntityTag(event.getEntity());
            case "potion" -> new ItemTag(event.getPotion());
            default -> super.getContext(name);
        };
    }

    @EventHandler
    public void onWitchReadyPotion(WitchReadyPotionEvent event) {
        this.event = event;
        fire(event);
    }
}
