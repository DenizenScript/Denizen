package com.denizenscript.denizen.scripts.commands.npc;

import com.denizenscript.denizen.utilities.DenizenAPI;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.dLocation;
import com.denizenscript.denizen.objects.dMaterial;
import com.denizenscript.denizen.objects.dNPC;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.ElementTag;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.scripts.commands.Holdable;
import net.citizensnpcs.api.ai.tree.BehaviorStatus;
import net.citizensnpcs.api.npc.BlockBreaker;
import org.bukkit.Bukkit;

import java.util.HashMap;

public class BreakCommand extends AbstractCommand implements Holdable {

    // <--[command]
    // @Name Break
    // @Syntax break [<location>] (<npc>) (radius:<#.#>)
    // @Required 1
    // @Plugin Citizens
    // @Short Makes an NPC walk over and break a block.
    // @Group npc
    //
    // @Description
    // By itself, the 'break' command will act as an NPC command in the sense that an attached
    // NPC will navigate to and break the block at the attached location. It can also accept a specified npc,
    // to fulfill the command, just specify a 'fetchable' npc object. It can also accept a radius to start
    // breaking the block from within. To specify the radius, prefix the radius with 'radius:'.
    //
    // @Tags
    // <n@npc.navigator.is_navigating>
    // <n@npc.navigator.target_location>
    //
    // @Usage
    // Use to make the npc break a block at 17,64,-87 in world.
    // - break l@17,64,-87,world
    //
    // @Usage
    // Use to make an npc with the id 12 break a block at 17,64,-87 in world.
    // - break l@17,64,-87,world n@12
    //
    // @Usage
    // Use to make an npc with the name bob break a block at 17,64,-87 and start digging from 5 blocks away.
    // - break l@17,64,-87,world n@bob radius:5
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (Argument arg : ArgumentHelper.interpretArguments(scriptEntry.aHArgs)) {

            if (!scriptEntry.hasObject("location")
                    && arg.matchesArgumentType(dLocation.class)) {
                scriptEntry.addObject("location", arg.asType(dLocation.class));
            }
            else if (!scriptEntry.hasObject("npc")
                    && arg.matchesArgumentType(dNPC.class)) {
                scriptEntry.addObject("npc", arg.asType(dNPC.class));
            }
            else if (!scriptEntry.hasObject("radius")
                    && arg.matchesPrimitive(ArgumentHelper.PrimitiveType.Double)) {
                scriptEntry.addObject("radius", arg.asElement());
            }
            else {
                arg.reportUnhandled();
            }
        }

        // Make sure location and entity were fulfilled
        if (!scriptEntry.hasObject("location")) {
            throw new InvalidArgumentsException("Must specify a location!");
        }

        // Use the NPC or the Player as the default entity
        if (!scriptEntry.hasObject("npc")) {
            if (Utilities.entryHasNPC(scriptEntry)) {
                scriptEntry.addObject("npc", Utilities.getEntryNPC(scriptEntry));
            }
            else {
                throw new InvalidArgumentsException("Must specify a valid NPC!");
            }
        }

        scriptEntry.defaultObject("radius", new ElementTag(2));

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
    public void execute(ScriptEntry scriptEntry) {

        final dLocation location = (dLocation) scriptEntry.getObject("location");
        final dNPC npc = (dNPC) scriptEntry.getObject("npc");
        ElementTag radius = scriptEntry.getElement("radius");

        final HashMap<String, ObjectTag> context = new HashMap<>();
        dMaterial material = new dMaterial(location.getBlock());
        context.put("location", location);
        context.put("material", material);

        if (scriptEntry.dbCallShouldDebug()) {

            Debug.report(scriptEntry, getName(), location.debug() + npc.debug() + radius.debug());

        }

        final ScriptEntry se = scriptEntry;
        BlockBreaker.BlockBreakerConfiguration config = new BlockBreaker.BlockBreakerConfiguration();
        config.item(npc.getLivingEntity().getEquipment().getItemInHand());
        config.radius(radius.asDouble());
        config.callback(new Runnable() {
            @Override
            public void run() {
                npc.action("dig", null, context);
                se.setFinished(true);
            }
        });

        BlockBreaker breaker = npc.getCitizen().getBlockBreaker(location.getBlock(), config);
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
