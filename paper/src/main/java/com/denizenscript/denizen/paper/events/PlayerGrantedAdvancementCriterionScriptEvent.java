package com.denizenscript.denizen.paper.events;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.destroystokyo.paper.event.player.PlayerAdvancementCriterionGrantEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerGrantedAdvancementCriterionScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player granted advancement criterion
    //
    // @Group Paper
    //
    // @Plugin Paper
    //
    // @Cancellable true
    //
    // @Switch advancement:<name> to only fire if the advancement for the criterion has the specified name.
    // @Switch criterion:<name> to only fire if the criterion being granted has the specified name.
    //
    // @Triggers when a player is granted a single criterion for an advancement.
    // To fire when ALL the criteria for an advancement is met, use <@link event player completes advancement>
    //
    // @Context
    // <context.advancement> returns the advancement's minecraft ID key.
    // <context.criterion> returns the criterion minecraft ID key.
    //
    // @Player Always.
    //
    // @Example
    // # Prevent a player from being granted an advancement criterion.
    // on player granted advancement criterion:
    // - determine cancelled
    //
    // @Example
    // # This will only narrate when the player is granted the criterion for taming a Calico cat
    // # for the "A Complete Catalogue" advancement.
    // on player granted advancement criterion advancement:husbandry/complete_catalogue criterion:calico:
    // - narrate "That is a pretty cute Calico cat you have there!"
    //
    // @Example
    // # This will fire for a custom Denizen advancement called "my_advancement".
    // on player granted advancement criterion advancement:denizen:my_advancement:
    // - narrate "You got the advancement!"
    // -->

    public PlayerGrantedAdvancementCriterionScriptEvent() {
        registerCouldMatcher("player granted advancement criterion");
        registerSwitches("advancement", "criterion");
    }

    public ElementTag criterion;
    public ElementTag advancement;
    public PlayerAdvancementCriterionGrantEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        if (!path.tryObjectSwitch("advancement", advancement)) {
            return false;
        }
        if (!path.tryObjectSwitch("criterion", criterion)) {
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
            case "criterion" -> criterion;
            case "advancement" -> advancement;
            default -> super.getContext(name);
        };
    }

    @EventHandler
    public void playerGrantedAdvancementCriterionEvent(PlayerAdvancementCriterionGrantEvent event) {
        this.event = event;
        criterion = new ElementTag(Utilities.namespacedKeyToString(Utilities.parseNamespacedKey(event.getCriterion())));
        advancement = new ElementTag(Utilities.namespacedKeyToString(event.getAdvancement().getKey()));
        fire(event);
    }
}
