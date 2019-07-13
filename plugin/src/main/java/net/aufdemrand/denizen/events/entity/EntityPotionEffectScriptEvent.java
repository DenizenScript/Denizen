package net.aufdemrand.denizen.events.entity;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.properties.entity.EntityPotionEffects;
import com.denizenscript.denizencore.objects.Element;
import com.denizenscript.denizencore.objects.dObject;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;

public class EntityPotionEffectScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // entity potion effects modified
    // <entity> potion effects modified
    // entity potion effects <change action>
    // <entity> potion effects <change action>
    //
    // @Regex ^on [^\s]+ potion effects [^\s]+$
    // @Switch in <area>
    // @Switch cause <cause>
    // @Switch effect <effect type>
    //
    // @Cancellable true
    //
    // @Triggers when an entity's potion effects change.
    //
    // @Context
    // <context.entity> returns the dEntity.
    // <context.cause> returns the cause of the effect change, based on <@see link https://hub.spigotmc.org/javadocs/spigot/org/bukkit/event/entity/EntityPotionEffectEvent.Cause.html>
    // <context.action> returns the action of the effect changed, which can be 'added', 'changed', 'cleared', or 'removed'
    // <context.override> returns whether the new potion effect will override the old.
    // <context.new_effect> returns the new potion effect (in the same format as <@link tag e@entity.list_effects>).
    // <context.old_effect> returns the new potion effect (in the same format as <@link tag e@entity.list_effects>).
    // <context.effect_type> returns the name of the modified potion effect type.
    //
    // @Determine
    // "OVERRIDE:" + Element(Boolean) to set whether the new potion effect should override.
    //
    // @Player when the entity that has changed is a player.
    //
    // @NPC when the entity that has changed is an NPC.
    //
    // -->

    public EntityPotionEffectScriptEvent() {
        instance = this;
    }

    public static EntityPotionEffectScriptEvent instance;
    public dEntity entity;
    public EntityPotionEffectEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return (CoreUtilities.toLowerCase(s).contains(" potion effects "));
    }

    @Override
    public boolean matches(ScriptPath path) {
        String target = path.eventArgLowerAt(0);
        String change = path.eventArgAt(3);

        if (!change.equals("modified") && !runGenericCheck(change, CoreUtilities.toLowerCase(event.getAction().name()))) {
            return false;
        }

        if (!tryEntity(entity, target)) {
            return false;
        }

        if (!runInCheck(path, entity.getLocation())) {
            return false;
        }

        if (!runGenericSwitchCheck(path, "cause", CoreUtilities.toLowerCase(event.getCause().name()))) {
            return false;
        }

        if (!runGenericSwitchCheck(path, "effect", CoreUtilities.toLowerCase(event.getModifiedType().getName()))) {
            return false;
        }

        return true;
    }

    @Override
    public String getName() {
        return "PotionEffectsModified";
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        String lower = CoreUtilities.toLowerCase(determination);
        if (lower.startsWith("override:")) {
            event.setOverride(lower.substring("override".length()).equals("true"));
            return true;
        }
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(entity.isPlayer() ? dEntity.getPlayerFrom(event.getEntity()) : null,
                entity.isCitizensNPC() ? dEntity.getNPCFrom(event.getEntity()) : null);
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("entity")) {
            return entity.getDenizenObject();
        }
        else if (name.equals("cause")) {
            return new Element(event.getCause().name());
        }
        else if (name.equals("action")) {
            return new Element(event.getAction().name());
        }
        else if (name.equals("effect_type")) {
            return new Element(event.getModifiedType().getName());
        }
        else if (name.equals("override")) {
            return new Element(event.isOverride());
        }
        else if (name.equals("new_effect") && event.getNewEffect() != null) {
            return new Element(EntityPotionEffects.stringify(event.getNewEffect()));
        }
        else if (name.equals("old_effect") && event.getOldEffect() != null) {
            return new Element(EntityPotionEffects.stringify(event.getOldEffect()));
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onEntityPotionEffect(EntityPotionEffectEvent event) {
        entity = new dEntity(event.getEntity());
        this.event = event;
        fire(event);
    }
}
