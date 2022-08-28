package com.denizenscript.denizen.scripts.commands.entity;

import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.npc.traits.HealthTrait;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;

import java.util.Collections;
import java.util.List;

public class HealthCommand extends AbstractCommand {

    public HealthCommand() {
        setName("health");
        setSyntax("health ({npc}/<entity>|...) [<#>] (state:{true}/false/toggle) (heal)");
        setRequiredArguments(1, 4);
        isProcedural = false;
    }

    // <--[command]
    // @Name Health
    // @Syntax health ({npc}/<entity>|...) [<#>] (state:{true}/false/toggle) (heal)
    // @Required 1
    // @Maximum 4
    // @Short Changes the target's maximum health.
    // @Group entity
    //
    // @Description
    // Use this command to modify an entity's maximum health.
    //
    // If the target is an NPC, you can use the 'state' argument to enable, disable, or toggle the Health trait
    // (which is used to track the NPC's health, and handle actions such as 'on death').
    // The Health trait will be enabled by default.
    //
    // By default, this command will target the linked NPC but can be set to target any other living entity, such as a player or mob.
    //
    // Optionally specify the 'heal' argument to automatically heal the entity to the new health value.
    // If not specified, the entity's health will remain wherever it was
    // (so for example a change from 20 max to 50 max will leave an entity with 20 health out of 50 max).
    //
    // Additionally, you may input a list of entities, each one will calculate the effects explained above.
    //
    // @Tags
    // <EntityTag.health>
    // <EntityTag.health_max>
    // <NPCTag.has_trait[health]>
    //
    // @Usage
    // Use to set the NPC's maximum health to 50.
    // - health 50
    //
    // @Usage
    // Use to disable tracking of health value on the NPC.
    // - health state:false
    //
    // @Usage
    // Use to change a player's health limit and current health both to 50.
    // - health <player> 50 heal
    //
    // @Usage
    // Use to change a list of entities' health limits all to 50.
    // - health <player.location.find.living_entities.within[10]> 50
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
            if (!scriptEntry.hasObject("target")
                    && arg.matches("player")) {
                if (!Utilities.entryHasPlayer(scriptEntry)) {
                    throw new InvalidArgumentsException("No player attached!");
                }
                scriptEntry.addObject("target", Collections.singletonList(Utilities.getEntryPlayer(scriptEntry).getDenizenEntity()));
            }
            else if (!scriptEntry.hasObject("quantity")
                    && arg.matchesFloat()) {
                scriptEntry.addObject("quantity", arg.asElement());
            }
            else if (!scriptEntry.hasObject("target")
                    && arg.matchesArgumentList(EntityTag.class)) {
                scriptEntry.addObject("target", arg.asType(ListTag.class).filter(EntityTag.class, scriptEntry));
            }
            else if (!scriptEntry.hasObject("action")
                    && arg.matchesPrefix("state")) {
                scriptEntry.addObject("action", arg.asElement());
            }
            else if (!scriptEntry.hasObject("heal")
                    && arg.matches("heal")) {
                scriptEntry.addObject("heal", new ElementTag(true));
            }
            else {
                arg.reportUnhandled();
            }
        }
        if (!scriptEntry.hasObject("quantity") && !scriptEntry.hasObject("action")) {
            throw new InvalidArgumentsException("Must specify a quantity!");
        }
        if (!scriptEntry.hasObject("target")) {
            if (!Utilities.entryHasNPC(scriptEntry)) {
                throw new InvalidArgumentsException("Missing NPC!");
            }
            scriptEntry.addObject("target", Collections.singletonList(Utilities.getEntryNPC(scriptEntry).getDenizenEntity()));
        }
        scriptEntry.defaultObject("heal", new ElementTag(false));
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        ElementTag quantity = scriptEntry.getElement("quantity");
        ElementTag action = scriptEntry.getElement("action");
        ElementTag heal = scriptEntry.getElement("heal");
        List<EntityTag> targets = (List<EntityTag>) scriptEntry.getObject("target");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), quantity, action, heal, db("target", targets));
        }
        if (quantity == null && action == null) {
            Debug.echoError(scriptEntry, "Null quantity!");
        }
        if (action == null) {
            action = new ElementTag(true);
        }
        for (EntityTag target : targets) {
            if (target.isCitizensNPC()) {
                if (action.asString().equalsIgnoreCase("true")) {
                    target.getDenizenNPC().getCitizen().addTrait(HealthTrait.class);
                }
                else if (action.asString().equalsIgnoreCase("false")) {
                    target.getDenizenNPC().getCitizen().removeTrait(HealthTrait.class);
                }
                else if (target.getDenizenNPC().getCitizen().hasTrait(HealthTrait.class)) {
                    target.getDenizenNPC().getCitizen().removeTrait(HealthTrait.class);
                }
                else {
                    target.getDenizenNPC().getCitizen().addTrait(HealthTrait.class);
                }
            }
            if (quantity != null) {
                if (target.isCitizensNPC()) {
                    if (target.getDenizenNPC().getCitizen().hasTrait(HealthTrait.class)) {
                        HealthTrait trait = target.getDenizenNPC().getCitizen().getOrAddTrait(HealthTrait.class);
                        trait.setMaxhealth(quantity.asInt());
                        if (heal.asBoolean()) {
                            trait.setHealth(quantity.asDouble());
                        }
                    }
                    else {
                        Debug.echoError(scriptEntry, "NPC doesn't have health trait!");
                    }
                }
                else if (target.isLivingEntity()) {
                    target.getLivingEntity().setMaxHealth(quantity.asDouble());
                    if (heal.asBoolean()) {
                        target.getLivingEntity().setHealth(quantity.asDouble());
                    }
                }
                else {
                    Debug.echoError(scriptEntry, "Entity '" + target.identify() + "'is not alive!");
                }
            }
        }
    }
}
