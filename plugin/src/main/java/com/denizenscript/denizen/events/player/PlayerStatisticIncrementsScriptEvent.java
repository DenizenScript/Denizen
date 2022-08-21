package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.Statistic;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerStatisticIncrementEvent;

public class PlayerStatisticIncrementsScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player statistic increments
    // player statistic <'statistic'> increments
    //
    // @Group Player
    //
    // @Cancellable true
    //
    // @Triggers when a player's statistics increment.
    //
    // @Context
    // <context.statistic> returns the statistic that incremented. Statistic names: <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Statistic.html>
    // <context.previous_value> returns the old value of the statistic.
    // <context.new_value> returns the new value of the statistic.
    // <context.qualifier> returns the qualifier (EntityTag/MaterialTag) if any.
    //
    // @Player Always.
    //
    // -->

    public PlayerStatisticIncrementsScriptEvent() {
        registerCouldMatcher("player statistic increments");
        registerCouldMatcher("player statistic <'statistic'> increments");
    }

    public Statistic statistic;
    public PlayerStatisticIncrementEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        if (!super.couldMatch(path)) {
            return false;
        }
        if (!path.eventArgLowerAt(2).equals("increments")) {
            if (!path.eventArgLowerAt(3).equals("increments") || !couldMatchEnum(path.eventArgLowerAt(2), Statistic.values())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean matches(ScriptPath path) {
        String stat = path.eventArgLowerAt(2);

        if (!stat.equals("increments") && !stat.equals(CoreUtilities.toLowerCase(statistic.toString()))) {
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
        switch (name) {
            case "statistic":
                return new ElementTag(statistic.toString());
            case "previous_value":
                return new ElementTag(event.getPreviousValue());
            case "new_value":
                return new ElementTag(event.getNewValue());
            case "qualifier":
                if (statistic.getType() == Statistic.Type.BLOCK || statistic.getType() == Statistic.Type.ITEM) {
                    return new MaterialTag(event.getMaterial());
                }
                else if (statistic.getType() == Statistic.Type.ENTITY) {
                    return new EntityTag(event.getEntityType());
                }
                break;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPlayerStatisticIncrements(PlayerStatisticIncrementEvent event) {
        if (EntityTag.isNPC(event.getPlayer())) {
            return;
        }
        statistic = event.getStatistic();
        this.event = event;
        fire(event);
    }
}
