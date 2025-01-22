package com.denizenscript.denizen.paper.events;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.destroystokyo.paper.event.entity.WitchConsumePotionEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class WitchConsumePotionScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // witch consumes potion
    //
    // @Location true
    //
    // @Plugin Paper
    //
    // @Group Paper
    //
    // @Cancellable true
    //
    // @Triggers when a witch consumes a potion.
    //
    // @Context
    // <context.entity> returns an EntityTag of the witch that is consuming a potion.
    // <context.potion> returns an ItemTag of the potion the witch is consuming.
    //
    // @Determine
    // "POTION:<ItemTag>" to change the potion the witch is consuming.
    //
    // -->

    public WitchConsumePotionScriptEvent() {
        registerCouldMatcher("witch consumes potion");
        this.<WitchConsumePotionScriptEvent, ItemTag>registerOptionalDetermination("potion", ItemTag.class, (evt, context, determination) -> {
            if (determination.canBeType(ItemTag.class)) {
                ItemTag potion = determination.asType(ItemTag.class, context);
                evt.event.setPotion(potion.getItemStack());
                return true;
            }
            return false;
        });
    }

    public WitchConsumePotionEvent event;
    public ItemTag potion;

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
            case "potion" -> potion;
            default -> super.getContext(name);
        };
    }

    @EventHandler
    public void onWitchThrowPotion(WitchConsumePotionEvent event) {
        potion = new ItemTag(event.getPotion());
        this.event = event;
        fire(event);
    }
}
