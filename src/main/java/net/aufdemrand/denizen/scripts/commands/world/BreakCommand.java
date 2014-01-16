package net.aufdemrand.denizen.scripts.commands.world;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.citizensnpcs.api.ai.tree.BehaviorStatus;
import net.citizensnpcs.npc.ai.BlockBreaker;

import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;

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

            else
                arg.reportUnhandled();
        }

        // Make sure location and entity were fulfilled
        if (!scriptEntry.hasObject("location"))
            throw new InvalidArgumentsException("Must specify a location!");

        // Use the NPC or the Player as the default entity
        scriptEntry.defaultObject("entity",
                (scriptEntry.hasNPC() ? scriptEntry.getNPC().getDenizenEntity() : null));

        if (!scriptEntry.hasObject("entity"))
            throw new InvalidArgumentsException("Must specify an entity!");

        scriptEntry.defaultObject("radius", new Element(2));

    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        final dLocation location = (dLocation) scriptEntry.getObject("location");
        final dEntity entity = (dEntity) scriptEntry.getObject("entity");
        Element radius = scriptEntry.getElement("radius");

        final HashMap<String, dObject> context = new HashMap<String, dObject>();
        context.put("location", (dObject) scriptEntry.getdObject("Location"));


        dB.report(scriptEntry, getName(), location.debug() + entity.debug() + radius.debug());

        final ScriptEntry se = scriptEntry;
        BlockBreaker.Configuration config = new BlockBreaker.Configuration();
        config.item(entity.getLivingEntity().getEquipment().getItemInHand());
        config.radius(radius.asDouble());
        config.callback(new Runnable() {
            @Override
            public void run() {
                dB.echoDebug(se, entity.debug() + " dug " + location.debug());
                if (entity.isNPC()) {
                    DenizenAPI.getDenizenNPC(entity.getNPC()).action("dig", null, context);
                }
            }
        });


        final BlockBreaker breaker = BlockBreaker.createWithConfiguration(entity.getLivingEntity(),
                location.getBlock(), config);
        if (breaker.shouldExecute()) {
            TaskRunnable run = new TaskRunnable(breaker);
            run.taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(denizen, run, 0, 1);
        }
    }

    private static class TaskRunnable implements Runnable {
        private int taskId;
        private final BlockBreaker breaker;

        public TaskRunnable(BlockBreaker breaker) {
            this.breaker = breaker;
        }

        @Override
        public void run() {
            if (breaker.run() != BehaviorStatus.RUNNING) {
                Bukkit.getScheduler().cancelTask(taskId);
                breaker.reset();
            }
        }
    }
}
