package com.denizenscript.denizen.paper.events;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.destroystokyo.paper.event.player.PlayerAdvancementCriterionGrantEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerGrantedAdvancementCriteriaScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player granted advancement criteria
    //
    // @Group Paper
    //
    // @Plugin Paper
    //
    // @Cancellable true
    //
    // @Switch advancement:<name> to only fire if the advancement for the criteria has the specified name.
    // @Switch criteria:<name> to only fire if the criteria being granted has the specified name.
    //
    // @Triggers when a player is granted any criteria for an advancement.
    // To fire when ALL the criteria for an advancement is met, use <@link event player completes advancement>
    //
    // @Context
    // <context.advancement> returns the advancement's minecraft ID key.
    // <context.criteria> returns the criteria minecraft ID key.
    //
    // @Player Always.
    //
    // @Example
    // # This can narrate something like:
    // # "Good job! You completed some criteria for the advancement: minecraft:story/root!"
    // on player granted advancement criteria:
    // - narrate "Good job! You completed some criteria for the advancement: <context.advancement>!"
    //
    // @Example
    // # This will only narrate when the player is granted the criteria for taming a Calico cat
    // # for the "A Complete Catalogue" advancement.
    // on player granted advancement criteria advancement:minecraft:husbandry/complete_catalogue criteria:minecraft:calico:
    // - narrate "That is a pretty cute Calico cat you have there!"
    //
    // @Example
    // # This will fire for a custom Denizen advancement called "my_advancement".
    // on player granted advancement criteria advancement:denizen:my_advancement:
    // - narrate "You got the advancement!"
    // -->

    public PlayerGrantedAdvancementCriteriaScriptEvent() {
        registerCouldMatcher("player granted advancement criteria");
        registerSwitches("advancement", "criteria");
    }

    public ElementTag criteria;
    public ElementTag advancement;
    public PlayerAdvancementCriterionGrantEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        if (!path.tryObjectSwitch("advancement", advancement)) {
            return false;
        }
        if (!path.tryObjectSwitch("criteria", criteria)) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(event.getPlayer());
    }

    @Override
    public ObjectTag getContext(String name) {
        return switch (name) {
            case "criteria" -> criteria;
            case "advancement" -> advancement;
            default -> super.getContext(name);
        };
    }

    @EventHandler
    public void playerGrantedAdvancementCriterionEvent(PlayerAdvancementCriterionGrantEvent event) {
        this.event = event;
        criteria = new ElementTag(event.getCriterion());
        advancement = new ElementTag(event.getAdvancement().getKey().toString());
        fire(event);
    }
}
