package net.aufdemrand.denizen.scripts.commands.world;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.objects.dMaterial;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import net.aufdemrand.denizencore.scripts.commands.Holdable;
import net.citizensnpcs.api.ai.tree.BehaviorStatus;
import net.citizensnpcs.api.npc.BlockBreaker;
import net.citizensnpcs.npc.ai.CitizensBlockBreaker;
import org.bukkit.Bukkit;

import java.util.HashMap;

public class BreakCommand extends AbstractCommand implements Holdable { // TODO: Should this be a NPC command?

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
                (((BukkitScriptEntryData) scriptEntry.entryData).hasNPC() ? ((BukkitScriptEntryData) scriptEntry.entryData).getNPC().getDenizenEntity() : null));

        if (!scriptEntry.hasObject("entity"))
            throw new InvalidArgumentsException("Must specify an entity!");

        scriptEntry.defaultObject("radius", new Element(2));

    }

    // <--[action]
    // @Actions
    // dig
    //
    // @Triggers when the NPC breaks a block with the Break Command
    //
    // @Context
    // <context.location> returns the location the NPC Dug
    // <context.material> Returns the Block dug
    //
    // -->
    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        final dLocation location = (dLocation) scriptEntry.getObject("location");
        final dEntity entity = (dEntity) scriptEntry.getObject("entity");
        Element radius = scriptEntry.getElement("radius");

        final HashMap<String, dObject> context = new HashMap<String, dObject>();
        dMaterial material = dMaterial.getMaterialFrom(location.getBlock().getType(), location.getBlock().getData());
        context.put("location", location);
        context.put("material", material);

        dB.report(scriptEntry, getName(), location.debug() + entity.debug() + radius.debug());

        final ScriptEntry se = scriptEntry;
        BlockBreaker.BlockBreakerConfiguration config = new BlockBreaker.BlockBreakerConfiguration();
        config.item(entity.getLivingEntity().getEquipment().getItemInHand());
        config.radius(radius.asDouble());
        config.callback(new Runnable() {
            @Override
            public void run() {
                if (entity.isCitizensNPC()) {
                    DenizenAPI.getDenizenNPC(entity.getDenizenNPC().getCitizen()).action("dig", null, context);
                    se.setFinished(true);
                }
            }
        });

        final CitizensBlockBreaker breaker = new CitizensBlockBreaker(entity.getLivingEntity(),
                location.getBlock(), config);
        if (breaker.shouldExecute()) {
            TaskRunnable run = new TaskRunnable(breaker);
            run.taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(DenizenAPI.getCurrentInstance(), run, 0, 1);
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
