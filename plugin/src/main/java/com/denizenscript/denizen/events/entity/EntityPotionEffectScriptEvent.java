package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.properties.item.ItemPotion;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;

public class EntityPotionEffectScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // <entity> potion effects modified
    // <entity> potion effects <'change_action'>
    //
    // @Group Entity
    //
    // @Location true
    // @Switch cause:<cause> to only process the event when it came from a specified cause.
    // @Switch effect:<effect type> to only process the event when a specified potion effect is applied.
    //
    // @Cancellable true
    //
    // @Triggers when an entity's potion effects change.
    //
    // @Context
    // <context.entity> returns the EntityTag.
    // <context.cause> returns the cause of the effect change, based on <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/event/entity/EntityPotionEffectEvent.Cause.html>
    // <context.action> returns the action of the effect changed, which can be 'added', 'changed', 'cleared', or 'removed'
    // <context.override> returns whether the new potion effect will override the old.
    // <context.new_effect_data> returns the new potion effect in <@link language Potion Effect Format>.
    // <context.old_effect_data> returns the old potion effect in <@link language Potion Effect Format>.
    // <context.effect_type> returns the name of the modified potion effect type.
    //
    // @Determine
    // "OVERRIDE:<ElementTag(Boolean)>" to set whether the new potion effect should override.
    //
    // @Player when the entity that has changed is a player.
    //
    // @NPC when the entity that has changed is an NPC.
    //
    // -->

    public EntityPotionEffectScriptEvent() {
        registerCouldMatcher("<entity> potion effects modified");
        registerCouldMatcher("<entity> potion effects <'change_action'>");
        registerSwitches("cause", "effect");
    }

    public EntityTag entity;
    public EntityPotionEffectEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        if (!super.couldMatch(path)) {
            return false;
        }
        String change = path.eventArgAt(3);
        if (!change.equals("modified") && !couldMatchEnum(change, EntityPotionEffectEvent.Action.values())) {
            return false;
        }
        return true;
    }

    @Override
    public boolean matches(ScriptPath path) {
        String target = path.eventArgLowerAt(0);
        String change = path.eventArgAt(3);

        if (!change.equals("modified") && !runGenericCheck(change, CoreUtilities.toLowerCase(event.getAction().name()))) {
            return false;
        }
        if (!entity.tryAdvancedMatcher(target, path.context)) {
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
        return super.matches(path);
    }

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        if (determinationObj instanceof ElementTag) {
            String lower = CoreUtilities.toLowerCase(determinationObj.toString());
            if (lower.startsWith("override:")) {
                event.setOverride(lower.substring("override".length()).equals("true"));
                return true;
            }
        }
        return super.applyDetermination(path, determinationObj);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(entity);
    }

    @Override
    public ObjectTag getContext(String name) {
        return switch (name) {
            case "entity" -> entity.getDenizenObject();
            case "cause" -> new ElementTag(event.getCause());
            case "action" -> new ElementTag(event.getAction());
            case "effect_type" -> new ElementTag(event.getModifiedType().getName());
            case "override" -> new ElementTag(event.isOverride());
            case "new_effect" -> event.getNewEffect() == null ? null : new ElementTag(ItemPotion.effectToLegacyString(event.getNewEffect(), null));
            case "old_effect" -> event.getOldEffect() == null ? null : new ElementTag(ItemPotion.effectToLegacyString(event.getOldEffect(), null));
            case "new_effect_data" -> event.getNewEffect() == null ? null : ItemPotion.effectToMap(event.getNewEffect(), true);
            case "old_effect_data" -> event.getOldEffect() == null ? null : ItemPotion.effectToMap(event.getOldEffect(), true);
            default -> super.getContext(name);
        };
    }

    @EventHandler
    public void onEntityPotionEffect(EntityPotionEffectEvent event) {
        entity = new EntityTag(event.getEntity());
        this.event = event;
        fire(event);
    }
}
