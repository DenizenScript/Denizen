package net.aufdemrand.denizen.scripts.commands.world;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.citizensnpcs.npc.ai.BlockBreaker;

/**
 * Breaks a block using Citizens' BlockBreaker
 *
 * @author Jeremy Schroeder
 */

public class BreakCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("location")
                    && arg.matchesArgumentType(dLocation.class))
                scriptEntry.addObject("location", arg.asType(dLocation.class));

            else if (!scriptEntry.hasObject("entity")
                    && arg.matchesArgumentType(dEntity.class))
                scriptEntry.addObject("entity", arg.asType(dEntity.class));

            else if (!scriptEntry.hasObject("radius")
                    && arg.matchesPrimitive(aH.PrimitiveType.Double))
                scriptEntry.addObject("radius", arg.asElement());

        }

        // Make sure location and entity were fulfilled
        if (!scriptEntry.hasObject("location"))
            throw new InvalidArgumentsException("Must specify a location!");

        if (!scriptEntry.hasObject("entity")) {
            if (scriptEntry.getPlayer() != null && scriptEntry.getPlayer().isOnline())
                scriptEntry.addObject("entity", new dEntity(scriptEntry.getPlayer().getPlayerEntity()));

            else if (scriptEntry.getNPC() != null && scriptEntry.getNPC().isSpawned())
                scriptEntry.addObject("entity", new dEntity(scriptEntry.getNPC().getEntity()));

            else throw new InvalidArgumentsException("Must specify an entity!");

        }

        scriptEntry.defaultObject("radius", new Element(1));

    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        final dLocation location = (dLocation) scriptEntry.getObject("location");
        final dEntity entity = (dEntity) scriptEntry.getObject("entity");
        Element radius = scriptEntry.getElement("radius");

        dB.report(getName(), location.debug() + entity.debug() + radius.debug());

        BlockBreaker.Configuration config = new BlockBreaker.Configuration()
                .item(entity.getLivingEntity().getEquipment().getItemInHand())
                .radius(radius.asDouble())
                .callback(new Runnable() {
                    @Override
                    public void run() {
                        dB.echoDebug(entity.debug() + " dug " + location.debug());
                    }
                });

        BlockBreaker breaker = BlockBreaker.createWithConfiguration(entity.getLivingEntity(), location.getBlock(), config);
        breaker.run();
    }

}
