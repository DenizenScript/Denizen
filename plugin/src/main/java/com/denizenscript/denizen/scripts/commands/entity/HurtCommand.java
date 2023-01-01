package com.denizenscript.denizen.scripts.commands.entity;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsRuntimeException;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.commands.generator.*;
import com.denizenscript.denizencore.utilities.Deprecations;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.List;

public class HurtCommand extends AbstractCommand {

    public HurtCommand() {
        setName("hurt");
        setSyntax("hurt (<#.#>) ({player}/<entity>|...) (cause:<cause>) (source:<entity>/<location>)");
        setRequiredArguments(0, 4);
        isProcedural = false;
        addRemappedPrefixes("source", "s");
        autoCompile();
    }

    // <--[command]
    // @Name Hurt
    // @Syntax hurt (<#.#>) ({player}/<entity>|...) (cause:<cause>) (source:<entity>/<location>)
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
    // If using a block-type cause such as "contact", you *must* specify (source:<location>) to set that block location as the attacker. The block can be any block, even just "<player.location>" (as long as the player in inside a world).
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

    public static void autoExecute(ScriptEntry scriptEntry,
                                   @ArgName("source") @ArgPrefixed @ArgDefaultNull ObjectTag source,
                                   @ArgName("cause") @ArgPrefixed @ArgDefaultNull EntityDamageEvent.DamageCause cause,
                                   @ArgName("amount") @ArgLinear @ArgDefaultText("1") ObjectTag amountObj,
                                   @ArgName("entities") @ArgLinear @ArgDefaultNull ObjectTag entitiesObj) {
        if (!amountObj.asElement().isDouble()) { // Compensate for legacy out-of-order args
            ObjectTag swapEntities = entitiesObj;
            entitiesObj = amountObj;
            if (swapEntities != null && swapEntities.asElement().isDouble()) {
                Deprecations.outOfOrderArgs.warn(scriptEntry);
                amountObj = swapEntities;
            }
            else {
                amountObj = new ElementTag(1.0d);
            }
        }
        List<EntityTag> entities = entitiesObj == null ? null : entitiesObj.asType(ListTag.class, scriptEntry.context).filter(EntityTag.class, scriptEntry.context);
        if (entities == null) {
            entities = Utilities.entryDefaultEntityList(scriptEntry, true);
            if (entities == null) {
                throw new InvalidArgumentsRuntimeException("No valid target entities found.");
            }
        }
        EntityTag sourceEntity = null;
        LocationTag sourceLocation = null;
        if (source != null) {
            if (source.shouldBeType(LocationTag.class)) {
                sourceLocation = source.asType(LocationTag.class, scriptEntry.context);
            }
            if (source.shouldBeType(EntityTag.class)) {
                sourceEntity = source.asType(EntityTag.class, scriptEntry.context);
            }
        }
        double amount = amountObj.asElement().asDouble();
        for (EntityTag entity : entities) {
            if (entity.getLivingEntity() == null) {
                Debug.echoDebug(scriptEntry, entity + " is not a living entity!");
                continue;
            }
            NMSHandler.entityHelper.damage(entity.getLivingEntity(), (float) amount, sourceEntity, sourceLocation, cause);
        }
    }
}
