package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;

public class PlayerSteersEntityScriptEvent extends BukkitScriptEvent {

    // <--[event]
    // @Events
    // player steers entity
    // player steers <entity>
    //
    // @Regex ^on player steers [^\s]+$
    // @Switch in <area>
    //
    // @Cancellable true
    //
    // @Triggers every tick that a player is controlling a vehicle.
    //
    // @Context
    // <context.entity> returns the EntityTag being steered by the player.
    // <context.sideways> returns an Element(Decimal) where a positive number signifies leftward movement.
    // <context.forward> returns an Element(Decimal) where a positive number signifies forward movement.
    // <context.jump> returns an Element(Boolean) that signifies whether the player is attempting to jump with the entity.
    // <context.dismount> returns an Element(Boolean) that signifies whether the player is attempting to dismount.
    //
    // -->

    public PlayerSteersEntityScriptEvent() {
        instance = this;
    }

    public static PlayerSteersEntityScriptEvent instance;
    public boolean enabled;
    public EntityTag entity;
    public PlayerTag player;
    public ElementTag sideways;
    public ElementTag forward;
    public ElementTag jump;
    public ElementTag dismount;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.getXthArg(1, CoreUtilities.toLowerCase(s)).startsWith("steers");
    }

    @Override
    public boolean matches(ScriptPath path) {
        String entityName = path.eventArgLowerAt(2);
        if (!tryEntity(entity, entityName)) {
            return false;
        }
        return runInCheck(path, entity.getLocation());
    }

    @Override
    public String getName() {
        return "PlayerSteersEntity";
    }

    @Override
    public void init() {
        enabled = true;
    }

    @Override
    public void destroy() {
        enabled = false;
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(player, entity.isCitizensNPC() ? entity.getDenizenNPC() : null);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("entity")) {
            return entity;
        }
        else if (name.equals("sideways")) {
            return sideways;
        }
        else if (name.equals("forward")) {
            return forward;
        }
        else if (name.equals("jump")) {
            return jump;
        }
        else if (name.equals("dismount")) {
            return dismount;
        }
        return super.getContext(name);
    }
}
