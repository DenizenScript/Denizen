package net.aufdemrand.denizen.events.entity;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.ScriptEntryData;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public class EntityFoodLevelChangeScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // entity changes food level (in <area>)
    // <entity> changes food level (in <area>)
    //
    // @Regex ^on [^\s]+ changes food level( in ((notable (cuboid|ellipsoid))|([^\s]+)))?$
    //
    // @Cancellable true
    //
    // @Triggers when an entity's food level changes.
    //
    // @Context
    // <context.entity> returns the dEntity.
    // <context.food> returns an Element(Number) of the entity's new food level.
    //
    // @Determine
    // Element(Decimal) to set the entity's new food level.
    //
    // @Player when the entity that's food level has changed is a player.
    //
    // @NPC when the entity that's food level has changed is an NPC.
    //
    // -->

    public EntityFoodLevelChangeScriptEvent() {
        instance = this;
    }

    public static EntityFoodLevelChangeScriptEvent instance;
    public dEntity entity;
    public Integer food;
    public FoodLevelChangeEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return (CoreUtilities.toLowerCase(s).contains("changes food level"));
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String target = CoreUtilities.getXthArg(0, lower);

        if (!tryEntity(entity, target)) {
            return false;
        }

        if (!runInCheck(scriptContainer, s, lower, entity.getLocation())) {
            return false;
        }

        return true;
    }

    @Override
    public String getName() {
        return "FoodLevelChanged";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        FoodLevelChangeEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        if (aH.matchesInteger(determination)) {
            food = aH.getIntegerFrom(determination);
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
        else if (name.equals("food")) {
            return new Element(food);
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onEntityFoodLevelChanged(FoodLevelChangeEvent event) {
        entity = new dEntity(event.getEntity());
        food = event.getFoodLevel();
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        event.setCancelled(cancelled);
        event.setFoodLevel(food);
    }
}
