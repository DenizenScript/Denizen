package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;

public class EntityHealsScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // entity heals (because <cause>)
    // <entity> heals (because <cause>)
    //
    // @Regex ^on [^\s]+ heals( because [^\s]+)?$
    // @Switch in <area>
    //
    // @Cancellable true
    //
    // @Triggers when an entity heals.
    //
    // @Context
    // <context.amount> returns the amount the entity healed.
    // <context.entity> returns the EntityTag that healed.
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
    public EntityTag entity;
    public ElementTag amount;
    public ElementTag reason;
    public EntityRegainHealthEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.getXthArg(1, CoreUtilities.toLowerCase(s)).equals("heals");
    }

    @Override
    public boolean matches(ScriptPath path) {

        if (!tryEntity(entity, path.eventArgLowerAt(0))) {
            return false;
        }

        if (path.eventArgLowerAt(2).equals("because") &&
                !path.eventArgLowerAt(3).equals(CoreUtilities.toLowerCase(reason.toString()))) {
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
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        String determination = determinationObj.toString();
        if (ArgumentHelper.matchesInteger(determination)) {
            amount = new ElementTag(ArgumentHelper.getDoubleFrom(determination));
            return true;
        }
        return super.applyDetermination(path, determinationObj);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(entity.isPlayer() ? EntityTag.getPlayerFrom(event.getEntity()) : null,
                entity.isCitizensNPC() ? EntityTag.getNPCFrom(event.getEntity()) : null);
    }

    @Override
    public ObjectTag getContext(String name) {
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
        entity = new EntityTag(event.getEntity());
        amount = new ElementTag(event.getAmount());
        reason = new ElementTag(event.getRegainReason().toString());
        this.event = event;
        fire(event);
        event.setAmount(amount.asDouble());
    }
}
