package net.aufdemrand.denizen.events.entity;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.events.ScriptEvent;
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

import java.util.HashMap;

public class EntityHealsScriptEvent extends ScriptEvent implements Listener {

    // <--[event]
    // @Events
    // entity heals (because <cause>)
    // <entity> heals (because <cause>)
    //
    // @Cancellable true
    //
    // @Triggers when an entity heals.
    //
    // @Context
    // <context.amount> returns the amount the entity healed.
    // <context.entity> returns the dEntity that healed.
    // <context.reason> returns the cause of the entity healing. Can be: REGEN, SATIATED, EATING, ENDER_CRYSTAL,
    // MAGIC, MAGIC_REGEN, WITHER_SPAWN, WITHER, CUSTOM
    //
    // @Determine
    // Element(Decimal) to set the amount of health the entity receives.
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
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        if (!entity.matchesEntity(CoreUtilities.getXthArg(0, lower))) {
            return false;
        }
        String cause = CoreUtilities.getXthArg(3, lower);
        if (cause.length() > 0 && !cause.equals(CoreUtilities.toLowerCase(reason.toString()))) {
            return false;
        }
        return true;
    }

    @Override
    public String getName() {
        return "EntityHeals";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        EntityRegainHealthEvent.getHandlerList().unregister(this);
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
    public HashMap<String, dObject> getContext() {
        HashMap<String, dObject> context = super.getContext();
        context.put("entity", entity);
        context.put("reason", reason);
        context.put("amount", amount);
        return context;
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
