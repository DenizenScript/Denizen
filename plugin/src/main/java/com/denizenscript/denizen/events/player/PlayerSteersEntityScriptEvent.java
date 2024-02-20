package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.utilities.packets.NetworkInterceptHelper;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;

import java.util.function.Consumer;

public class PlayerSteersEntityScriptEvent extends BukkitScriptEvent {

    // <--[event]
    // @Events
    // player steers entity
    // player steers <entity>
    //
    // @Regex ^on player steers [^\s]+$
    //
    // @Group Player
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers every tick that a player is controlling a vehicle.
    //
    // @Context
    // <context.entity> returns the EntityTag being steered by the player.
    // <context.sideways> returns an ElementTag(Decimal) where a positive number signifies leftward movement.
    // <context.forward> returns an ElementTag(Decimal) where a positive number signifies forward movement.
    // <context.jump> returns an ElementTag(Boolean) that signifies whether the player is attempting to jump with the entity.
    // <context.dismount> returns an ElementTag(Boolean) that signifies whether the player is attempting to dismount.
    //
    // @Player Always.
    //
    // -->

    public PlayerSteersEntityScriptEvent() {
        instance = this;
    }

    public static PlayerSteersEntityScriptEvent instance;
    public EntityTag entity;
    public PlayerTag player;
    public ElementTag sideways;
    public ElementTag forward;
    public ElementTag jump;
    public ElementTag dismount;

    public Consumer<Boolean> modifyCancellation;

    @Override
    public void cancellationChanged() {
        modifyCancellation.accept(cancelled);
    }

    @Override
    public boolean couldMatch(ScriptPath path) {
        if (!path.eventLower.startsWith("player steers")) {
            return false;
        }
        if (!couldMatchEntity(path.eventArgLowerAt(2))) {
            return false;
        }
        return true;
    }

    @Override
    public boolean matches(ScriptPath path) {
        String entityName = path.eventArgLowerAt(2);
        if (!entity.tryAdvancedMatcher(entityName)) {
            return false;
        }
        if (!runInCheck(path, entity.getLocation())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public void init() {
        NetworkInterceptHelper.enable();
        super.init();
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(player, entity.isCitizensNPC() ? entity.getDenizenNPC() : null);
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "entity":
                return entity;
            case "sideways":
                return sideways;
            case "forward":
                return forward;
            case "jump":
                return jump;
            case "dismount":
                return dismount;
        }
        return super.getContext(name);
    }
}
