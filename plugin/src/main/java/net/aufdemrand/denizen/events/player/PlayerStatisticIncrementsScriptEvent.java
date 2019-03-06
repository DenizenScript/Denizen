package net.aufdemrand.denizen.events.player;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dMaterial;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.utilities.entity.DenizenEntityType;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.ScriptEntryData;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Statistic;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerStatisticIncrementEvent;

public class PlayerStatisticIncrementsScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player statistic increments
    // player statistic <statistic> increments
    //
    // @Regex ^on player ( [^\s]+ )increments$
    //
    // @Cancellable true
    //
    // @Triggers when a player's statistics increment.
    //
    // @Context
    // <context.statistic> returns the statistic that incremented.
    // <context.previous_value> returns the old value of the statistic.
    // <context.new_value> returns the new value of the statistic.
    // <context.qualifier> returns the qualifier (dEntity/dMaterial) if any.
    //
    // -->

    public PlayerStatisticIncrementsScriptEvent() {
        instance = this;
    }

    public static PlayerStatisticIncrementsScriptEvent instance;
    public Statistic statistic;
    public Integer previous_value;
    public Integer new_value;
    public dMaterial material;
    public dEntity entity;
    public PlayerStatisticIncrementEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.toLowerCase(s).startsWith("player statistic");
    }

    @Override
    public boolean matches(ScriptPath path) {
        String lower = path.eventLower;
        String stat = CoreUtilities.getXthArg(2, lower);

        if (!stat.equals("increments") && !stat.equals(CoreUtilities.toLowerCase(statistic.toString()))) {
            return false;
        }

        return true;
    }

    @Override
    public String getName() {
        return "PlayerStatisticIncrements";
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(dPlayer.mirrorBukkitPlayer(event.getPlayer()), null);
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("statistic")) {
            return new Element(statistic.toString());
        }
        else if (name.equals("previous_value")) {
            return new Element(previous_value);
        }
        else if (name.equals("new_value")) {
            return new Element(new_value);
        }
        else if (name.equals("qualifier")) {
            if (statistic.getType() == Statistic.Type.BLOCK || statistic.getType() == Statistic.Type.ITEM) {
                return material;
            }
            else if (statistic.getType() == Statistic.Type.ENTITY) {
                return entity;
            }
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPlayerStatisticIncrements(PlayerStatisticIncrementEvent event) {
        if (dEntity.isNPC(event.getPlayer())) {
            return;
        }
        material = null;
        entity = null;
        previous_value = null;
        statistic = event.getStatistic();
        if (statistic.getType() == Statistic.Type.BLOCK || statistic.getType() == Statistic.Type.ITEM) {
            material = new dMaterial(event.getMaterial());
        }
        else if (statistic.getType() == Statistic.Type.ENTITY) {
            entity = new dEntity(DenizenEntityType.getByName(event.getEntityType().name()));
        }
        previous_value = event.getPreviousValue();
        new_value = event.getNewValue();
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        event.setCancelled(cancelled);
    }
}
