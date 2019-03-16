package net.aufdemrand.denizen.events.player;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.ScriptEntryData;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;

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
    // <context.entity> returns the dEntity being steered by the player.
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
    public dEntity entity;
    public dPlayer player;
    public Element sideways;
    public Element forward;
    public Element jump;
    public Element dismount;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.getXthArg(1, CoreUtilities.toLowerCase(s)).startsWith("steers");
    }

    @Override
    public boolean matches(ScriptPath path) {
        String lower = path.eventLower;
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
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(player, entity.isCitizensNPC() ? entity.getDenizenNPC() : null);
    }

    @Override
    public dObject getContext(String name) {
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
