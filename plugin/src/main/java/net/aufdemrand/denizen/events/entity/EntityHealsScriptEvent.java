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
import org.bukkit.event.entity.EntityRegainHealthEvent;

public class EntityHealsScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // entity heals (because <cause>) (in <area>)
    // <entity> heals (because <cause>) (in <area>)
    //
    // @Regex ^on [^\s]+ heals( because [^\s]+)?( in ((notable (cuboid|ellipsoid))|([^\s]+)))?$
    //
    // @Cancellable true
    //
    // @Triggers when an entity heals.
    //
    // @Context
    // <context.amount> returns the amount the entity healed.
    // <context.entity> returns the dEntity that healed.
    // <context.reason> returns the cause of the entity healing. Can be: <@link url http://bit.ly/2GTtxsf>
    //
    // @Determine
    // Element(Decimal) to set the amount of health the entity receives.
    //
    // @Player when the entity that was healed is a player.
    //
    // @NPC when the entity that was healed was an NPC.
    //
    // -->

    public EntityHealsScriptEvent() {
        instance = this;
    }

    public static EntityHealsScriptEvent instance;
    public dEntity entity;
    public Element amount;
    public Element reason;
    public EntityRegainHealthEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.getXthArg(1, CoreUtilities.toLowerCase(s)).equals("heals");
    }

    @Override
    public boolean matches(ScriptPath path) {
        String s = path.event;
        String lower = path.eventLower;

        if (!tryEntity(entity, CoreUtilities.getXthArg(0, lower))) {
            return false;
        }

        if (CoreUtilities.getXthArg(2, lower).equals("because") &&
                !CoreUtilities.getXthArg(3, lower).equals(CoreUtilities.toLowerCase(reason.toString()))) {
            return false;
        }

        if (!runInCheck(path, entity.getLocation())) {
            return false;
        }

        return true;
    }

    @Override
    public String getName() {
        return "EntityHeals";
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        if (aH.matchesInteger(determination)) {
            amount = new Element(aH.getDoubleFrom(determination));
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
            return entity;
        }
        else if (name.equals("reason")) {
            return reason;
        }
        else if (name.equals("amount")) {
            return amount;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onEntityHeals(EntityRegainHealthEvent event) {
        entity = new dEntity(event.getEntity());
        amount = new Element(event.getAmount());
        reason = new Element(event.getRegainReason().toString());
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        event.setCancelled(cancelled);
        event.setAmount(amount.asDouble());
    }
}
