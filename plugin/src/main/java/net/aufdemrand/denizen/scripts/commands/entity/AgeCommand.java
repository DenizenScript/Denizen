package net.aufdemrand.denizen.scripts.commands.entity;

import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.properties.entity.EntityAge;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;

import java.util.List;


/**
 * Sets the ages of a list of entities, optionally locking them in those ages.
 *
 * @author David Cernat
 */
public class AgeCommand extends AbstractCommand {

    // <--[example]
    // @Title Age Command and Item Script/Item Event Example
    // @Description
    // This script shows a very small example that utilizes the Age Command with the use
    // of some items scripts and item events. Use /ex give i@aged_wand or /ex give i@baby_wand
    // and right click an entity.

    // @Code
    // # +--------------------
    // # | Age Command and Item Script/Item Event Example
    // # |
    // # | This script shows a very small example that utilizes the Age Command with the use
    // # | of some items scripts and item events. Use /ex give i@aged_wand or /ex give i@baby_wand
    // # | and right click an entity.
    //
    // Age Wands Handler:
    //   type: world
    //
    //   events:
    //
    //     # Check for the player right clicking entities with the baby_wand item
    //     on player right clicks entity with baby_wand:
    //
    //     # Play an effect, and run the age command on the entity
    //     - playeffect <c.entity.location> effect:mob_spell quantity:100 data:1 offset:0.5
    //     - age <c.entity> baby
    //
    //     # ...and again for the aged_wand item
    //     on player right clicks entity with aged_wand:
    //     - playeffect <c.entity.location> effect:mob_spell quantity:200 data:0 offset:0.5
    //     - age <c.entity>
    //
    //
    // # Build item script containers
    // baby_wand:
    //   type: item
    //
    //   material: blaze_rod
    //   display name: a baby wand
    //   lore:
    //   - "This wand is smooth as a baby's bottom."
    //
    // aged_wand:
    //   type: item
    //
    //   material: bone
    //   display name: an aged wand
    //   lore:
    //   - "This wand reeks of old age."
    //

    // -->


    private enum AgeType {ADULT, BABY}

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("entities")
                    && arg.matchesArgumentList(dEntity.class)) {
                scriptEntry.addObject("entities", arg.asType(dList.class).filter(dEntity.class));
            }
            else if (!scriptEntry.hasObject("agetype")
                    && arg.matchesEnum(AgeType.values())) {
                scriptEntry.addObject("agetype", AgeType.valueOf(arg.getValue().toUpperCase()));
            }
            else if (!scriptEntry.hasObject("age")
                    && arg.matchesPrimitive(aH.PrimitiveType.Integer)) {
                scriptEntry.addObject("age", arg.asElement());
            }
            else if (!scriptEntry.hasObject("lock")
                    && arg.matches("lock")) {
                scriptEntry.addObject("lock", Element.TRUE);
            }
            else {
                arg.reportUnhandled();
            }
        }

        // Check to make sure required arguments have been filled
        if (!scriptEntry.hasObject("entities")) {
            throw new InvalidArgumentsException("No valid entities specified.");
        }

        // Use default age if one is not specified
        scriptEntry.defaultObject("age", new Element(1));

    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(final ScriptEntry scriptEntry) throws CommandExecutionException {

        // Get objects
        List<dEntity> entities = (List<dEntity>) scriptEntry.getObject("entities");
        AgeType ageType = (AgeType) scriptEntry.getObject("agetype");
        int age = scriptEntry.getElement("age").asInt();
        boolean lock = scriptEntry.hasObject("lock");

        // Report to dB
        if (scriptEntry.dbCallShouldDebug()) {
            dB.report(scriptEntry, getName(), (lock ? aH.debugObj("lock", lock) : "") +
                    (ageType != null ? aH.debugObj("agetype", ageType)
                            : aH.debugObj("age", age)) +
                    aH.debugObj("entities", entities.toString()));
        }

        // Go through all the entities and set their ages
        for (dEntity entity : entities) {
            if (entity.isSpawned()) {

                // Check if entity specified can be described by 'EntityAge'
                if (EntityAge.describes(entity)) {

                    EntityAge property = EntityAge.getFrom(entity);

                    // Adjust 'ageType'
                    if (ageType != null) {
                        if (ageType.equals(AgeType.BABY)) {
                            property.setBaby(true);
                        }
                        else {
                            property.setBaby(false);
                        }
                    }
                    else {
                        property.setAge(age);
                    }

                    // Adjust 'locked'
                    property.setLock(lock);
                }
                else {
                    dB.echoError(scriptEntry.getResidingQueue(), entity.identify() + " is not ageable!");
                }

            }
        }

    }
}
