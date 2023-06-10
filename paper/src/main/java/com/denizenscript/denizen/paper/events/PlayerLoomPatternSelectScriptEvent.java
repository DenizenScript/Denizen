package com.denizenscript.denizen.paper.events;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.InventoryTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import io.papermc.paper.event.player.PlayerLoomPatternSelectEvent;
import org.bukkit.block.banner.PatternType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerLoomPatternSelectScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player selects loom pattern
    //
    // @Plugin Paper
    //
    // @Group Paper
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Player Always.
    //
    // @Triggers when a player selects a loom pattern.
    //
    // @Switch type:<pattern> to only process the event if the specified pattern is selected.
    //
    // @Context
    // <context.loom> returns an InventoryTag of the loom.
    // <context.pattern> returns an ElementTag of the selected pattern. Valid pattern types can be found at: <@link url https://jd.papermc.io/paper/1.19/org/bukkit/block/banner/PatternType.html>
    //
    // @Determine
    // "PATTERN:<ElementTag>" to set the pattern type of the loom.
    //
    // @Example
    // # Announce the player's selected loom pattern.
    // on player selects loom pattern:
    // - announce "<player.name> selected the <context.pattern> pattern!"
    //
    // @Example
    // # If the player selects the "CREEPER" pattern type, publicly shames them before setting the pattern to "SKULL".
    // on player selects loom pattern type:CREEPER:
    // - announce "Shame on <player.name> for selecting the creeper pattern!"
    // - determine pattern:SKULL
    //
    // -->

    public PlayerLoomPatternSelectScriptEvent() {
        registerCouldMatcher("player selects loom pattern");
        registerSwitches("type");
    }

    public PlayerLoomPatternSelectEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, event.getLoomInventory().getLocation())) {
            return false;
        }
        if (!path.tryObjectSwitch("type", new ElementTag(event.getPatternType()))) {
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
            case "loom" -> InventoryTag.mirrorBukkitInventory(event.getLoomInventory());
            case "pattern" -> new ElementTag(event.getPatternType());
            default -> super.getContext(name);
        };
    }

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        if (determinationObj instanceof ElementTag) {
            String lower = CoreUtilities.toLowerCase(determinationObj.toString());
            if (lower.startsWith("pattern:")) {
                ElementTag value = new ElementTag(lower.substring("pattern:".length()));
                event.setPatternType(value.asEnum(PatternType.class));
                return true;
            }
        }
        return super.applyDetermination(path, determinationObj);
    }

    @EventHandler
    public void onPlayerSelectsLoomPattern(PlayerLoomPatternSelectEvent event) {
        this.event = event;
        fire(event);
    }
}
