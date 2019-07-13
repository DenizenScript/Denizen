package com.denizenscript.denizen.scripts.commands.entity;

import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.dEntity;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.ElementTag;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.objects.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.Bukkit;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.ArrayList;
import java.util.List;

public class HurtCommand extends AbstractCommand {

    // <--[command]
    // @Name Hurt
    // @Syntax hurt (<#.#>) ({player}/<entity>|...) (cause:<cause>)
    // @Required 0
    // @Short Hurts the player or a list of entities.
    // @Group entity
    //
    // @Description
    // Does damage to a list of entities, or to any single entity.
    //
    // If no entities are specified: if there is a linked player, the command targets that. If there is no linked
    // player but there is a linked NPC, the command targets the NPC. If neither is available, the command will error.
    //
    // Does a specified amount of damage usually, but, if no damage is specified, does precisely 1HP worth of damage
    // (half a heart).
    // Optionally, specify (source:<entity>) to make the system treat that entity as the attacker,
    // be warned this does not always work as intended, and is liable to glitch.
    // You may also optionally specify a damage cause to fire a proper damage event with the given cause,
    // only doing the damage if the event wasn't cancelled. Calculates the 'final damage' rather
    // than using the raw damage input number. See <@link language damage cause> for damage causes.
    // To make the source only be included in the initial damage event, and not the application of damage, specify 'source_once'.
    //
    // @Tags
    // <e@entity.health>
    // <e@entity.last_damage.amount>
    // <e@entity.last_damage.cause>
    // <e@entity.last_damage.duration>
    // <e@entity.last_damage.max_duration>
    //
    // @Usage
    // Use to hurt the player for 1 HP.
    // - hurt
    //
    // @Usage
    // Use to hurt the NPC for 5 HP.
    // - hurt 5 <npc>
    //
    // @Usage
    // Use to cause the player to hurt the NPC for all its health (if unarmored).
    // - hurt <npc.health> <npc> cause:CUSTOM source:<player>
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (Argument arg : ArgumentHelper.interpretArguments(scriptEntry.aHArgs)) {

            if (!scriptEntry.hasObject("amount")
                    && (arg.matchesPrimitive(ArgumentHelper.PrimitiveType.Double)
                    || arg.matchesPrimitive(ArgumentHelper.PrimitiveType.Integer))) {
                scriptEntry.addObject("amount", arg.asElement());
            }
            else if (!scriptEntry.hasObject("source")
                    && arg.matchesPrefix("source", "s")
                    && arg.matchesArgumentType(dEntity.class)) {
                scriptEntry.addObject("source", arg.asType(dEntity.class));
            }
            else if (!scriptEntry.hasObject("entities")
                    && arg.matchesArgumentList(dEntity.class)) {
                scriptEntry.addObject("entities", arg.asType(ListTag.class).filter(dEntity.class, scriptEntry));
            }
            else if (!scriptEntry.hasObject("cause")
                    && arg.matchesEnum(EntityDamageEvent.DamageCause.values())) {
                scriptEntry.addObject("cause", arg.asElement());
            }
            else if (!scriptEntry.hasObject("source_once")
                    && arg.matchesOne("source_once")) {
                scriptEntry.addObject("source_once", new ElementTag(true));
            }
            else {
                arg.reportUnhandled();
            }
        }

        if (!scriptEntry.hasObject("amount")) {
            scriptEntry.addObject("amount", new ElementTag(1.0d));
        }

        if (!scriptEntry.hasObject("entities")) {
            List<dEntity> entities = new ArrayList<>();
            if (Utilities.getEntryPlayer(scriptEntry) != null) {
                entities.add(Utilities.getEntryPlayer(scriptEntry).getDenizenEntity());
            }
            else if (Utilities.getEntryNPC(scriptEntry) != null) {
                entities.add(Utilities.getEntryNPC(scriptEntry).getDenizenEntity());
            }
            else {
                throw new InvalidArgumentsException("No valid target entities found.");
            }
            scriptEntry.addObject("entities", entities);
        }

    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(ScriptEntry scriptEntry) {

        List<dEntity> entities = (List<dEntity>) scriptEntry.getObject("entities");
        dEntity source = (dEntity) scriptEntry.getObject("source");
        ElementTag amountElement = scriptEntry.getElement("amount");
        ElementTag cause = scriptEntry.getElement("cause");
        ElementTag source_once = scriptEntry.getElement("source_once");

        if (scriptEntry.dbCallShouldDebug()) {

            Debug.report(scriptEntry, getName(), amountElement.debug()
                    + ArgumentHelper.debugList("entities", entities)
                    + (source_once == null ? "" : source_once.debug())
                    + (cause == null ? "" : cause.debug())
                    + (source == null ? "" : source.debug()));

        }

        double amount = amountElement.asDouble();
        for (dEntity entity : entities) {
            if (entity.getLivingEntity() == null) {
                Debug.echoDebug(scriptEntry, entity + " is not a living entity!");
                continue;
            }
            if (cause == null) {
                if (source == null) {
                    entity.getLivingEntity().damage(amount);
                }
                else {
                    entity.getLivingEntity().damage(amount, source.getBukkitEntity());
                }
            }
            else {
                EntityDamageEvent.DamageCause causeEnum = EntityDamageEvent.DamageCause.valueOf(cause.asString().toUpperCase());
                EntityDamageEvent ede = source == null ? new EntityDamageEvent(entity.getBukkitEntity(), causeEnum, amount) :
                        new EntityDamageByEntityEvent(source.getBukkitEntity(), entity.getBukkitEntity(), causeEnum, amount);
                Bukkit.getPluginManager().callEvent(ede);
                if (!ede.isCancelled()) {
                    entity.getLivingEntity().setLastDamageCause(ede);
                    if (source == null || (source_once != null && source_once.asBoolean())) {
                        entity.getLivingEntity().damage(ede.getFinalDamage());
                    }
                    else {
                        entity.getLivingEntity().damage(ede.getFinalDamage(), source.getBukkitEntity());
                    }
                    entity.getLivingEntity().setLastDamageCause(ede);
                }
            }
        }
    }
}
