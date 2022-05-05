package com.denizenscript.denizen.scripts.commands.entity;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizen.utilities.BukkitImplDeprecations;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.ArrayList;
import java.util.List;

public class HurtCommand extends AbstractCommand {

    public HurtCommand() {
        setName("hurt");
        setSyntax("hurt (<#.#>) ({player}/<entity>|...) (cause:<cause>) (source:<entity>)");
        setRequiredArguments(0, 5);
        isProcedural = false;
    }

    // <--[command]
    // @Name Hurt
    // @Syntax hurt (<#.#>) ({player}/<entity>|...) (cause:<cause>) (source:<entity>)
    // @Required 0
    // @Maximum 4
    // @Short Hurts the player or a list of entities.
    // @Synonyms Damage,Injure
    // @Group entity
    //
    // @Description
    // Does damage to a list of entities, or to any single entity.
    //
    // If no entities are specified: if there is a linked player, the command targets that. If there is no linked
    // player but there is a linked NPC, the command targets the NPC. If neither is available, the command will error.
    //
    // Does a specified amount of damage usually, but, if no damage is specified, does precisely 1HP worth of damage (half a heart).
    //
    // Optionally, specify (source:<entity>) to make the system treat that entity as the attacker.
    //
    // You may also specify a damage cause to fire a proper damage event with the given cause, only doing the damage if the event wasn't cancelled.
    // Calculates the 'final damage' rather than using the raw damage input number. See <@link language damage cause> for damage causes.
    //
    // Using a valid 'cause' value is best when trying to replicate natural damage, excluding it is best when trying to force the raw damage through.
    // Note that using invalid or impossible causes may lead to bugs
    //
    // @Tags
    // <EntityTag.health>
    // <EntityTag.last_damage.amount>
    // <EntityTag.last_damage.cause>
    // <EntityTag.last_damage.duration>
    // <EntityTag.last_damage.max_duration>
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
        for (Argument arg : scriptEntry) {
            if (!scriptEntry.hasObject("amount")
                    && (arg.matchesFloat()
                    || arg.matchesInteger())) {
                scriptEntry.addObject("amount", arg.asElement());
            }
            else if (!scriptEntry.hasObject("source")
                    && arg.matchesPrefix("source", "s")
                    && arg.matchesArgumentType(EntityTag.class)) {
                scriptEntry.addObject("source", arg.asType(EntityTag.class));
            }
            else if (!scriptEntry.hasObject("entities")
                    && arg.matchesArgumentList(EntityTag.class)) {
                scriptEntry.addObject("entities", arg.asType(ListTag.class).filter(EntityTag.class, scriptEntry));
            }
            else if (!scriptEntry.hasObject("cause")
                    && arg.matchesEnum(EntityDamageEvent.DamageCause.class)) {
                scriptEntry.addObject("cause", arg.asElement());
            }
            else if (!scriptEntry.hasObject("source_once")
                    && arg.matches("source_once")) {
                BukkitImplDeprecations.hurtSourceOne.warn(scriptEntry);
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
            List<EntityTag> entities = new ArrayList<>();
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

    @Override
    public void execute(ScriptEntry scriptEntry) {
        List<EntityTag> entities = (List<EntityTag>) scriptEntry.getObject("entities");
        EntityTag source = scriptEntry.getObjectTag("source");
        ElementTag amountElement = scriptEntry.getElement("amount");
        ElementTag cause = scriptEntry.getElement("cause");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), amountElement, db("entities", entities), cause, source);
        }
        double amount = amountElement.asDouble();
        for (EntityTag entity : entities) {
            if (entity.getLivingEntity() == null) {
                Debug.echoDebug(scriptEntry, entity + " is not a living entity!");
                continue;
            }
            EntityDamageEvent.DamageCause causeEnum = cause == null ? null : EntityDamageEvent.DamageCause.valueOf(cause.asString().toUpperCase());
            NMSHandler.entityHelper.damage(entity.getLivingEntity(), (float) amount, source == null ? null : source.getBukkitEntity(), causeEnum);
        }
    }
}
