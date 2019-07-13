package net.aufdemrand.denizen.events.entity;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import com.denizenscript.denizencore.objects.Element;
import com.denizenscript.denizencore.objects.aH;
import com.denizenscript.denizencore.objects.dObject;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityAirChangeEvent;

public class EntityAirLevelChangeScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // entity changes air level
    // <entity> changes air level
    //
    // @Regex ^on [^\s]+ changes air level$
    // @Switch in <area>
    //
    // @Cancellable true
    //
    // @Triggers when an entity's air level changes.
    //
    // @Context
    // <context.entity> returns the dEntity.
    // <context.air> returns an Element(Number) of the entity's new air level (measured in ticks).
    //
    // @Determine
    // Element(Decimal) to set the entity's new air level.
    //
    // @Player when the entity that's air level has changed is a player.
    //
    // @NPC when the entity that's air level has changed is an NPC.
    //
    // -->

    public EntityAirLevelChangeScriptEvent() {
        instance = this;
    }

    public static EntityAirLevelChangeScriptEvent instance;
    public dEntity entity;
    public Integer air;
    public EntityAirChangeEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return (CoreUtilities.toLowerCase(s).contains("changes air level"));
    }

    @Override
    public boolean matches(ScriptPath path) {
        String target = path.eventArgLowerAt(0);

        if (!tryEntity(entity, target)) {
            return false;
        }

        if (!runInCheck(path, entity.getLocation())) {
            return false;
        }

        return true;
    }

    @Override
    public String getName() {
        return "AirLevelChanged";
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        if (aH.matchesInteger(determination)) {
            air = aH.getIntegerFrom(determination);
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
        else if (name.equals("air")) {
            return new Element(air);
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onEntityAirLevelChanged(EntityAirChangeEvent event) {
        entity = new dEntity(event.getEntity());
        air = event.getAmount();
        this.event = event;
        fire(event);
        event.setAmount(air);
    }
}
