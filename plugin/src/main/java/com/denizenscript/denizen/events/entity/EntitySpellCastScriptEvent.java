package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpellCastEvent;

public class EntitySpellCastScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // <entity> casts <'spell'>
    //
    // @Group Entity
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers when an entity, usually an Evoker or Illusioner, casts a spell.
    //
    // @Context
    // <context.entity> returns the EntityTag of the Spellcaster entity.
    // <context.spell> returns an ElementTag of the spell used. Valid spells can be found at <@link url https://jd.papermc.io/paper/1.20/org/bukkit/entity/Spellcaster.Spell.html>
    //
    // -->

    public EntitySpellCastScriptEvent() {
        registerCouldMatcher("<entity> casts <'spell'>");
    }

    public EntityTag entity;
    public EntitySpellCastEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        if (!path.tryArgObject(0, entity)) {
            return false;
        }
        if (!path.eventArgLowerAt(2).equals("spell") && !runGenericCheck(path.eventArgLowerAt(2), event.getSpell().toString())) {
            return false;
        }
        if (!runInCheck(path, entity.getLocation())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ObjectTag getContext(String name) {
        return switch (name) {
            case "entity" -> entity;
            case "spell" -> new ElementTag(event.getSpell());
            default -> super.getContext(name);
        };
    }

    @EventHandler
    public void onSpellCast(EntitySpellCastEvent event) {
        entity = new EntityTag(event.getEntity());
        this.event = event;
        fire(event);
    }
}
