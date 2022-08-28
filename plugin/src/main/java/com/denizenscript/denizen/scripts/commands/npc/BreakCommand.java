package com.denizenscript.denizen.scripts.commands.npc;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.scripts.commands.Holdable;
import net.citizensnpcs.api.ai.tree.BehaviorStatus;
import net.citizensnpcs.api.npc.BlockBreaker;
import org.bukkit.Bukkit;

import java.util.HashMap;

public class BreakCommand extends AbstractCommand implements Holdable {

    public BreakCommand() {
        setName("break");
        setSyntax("break [<location>] (<npc>) (radius:<#.#>)");
        setRequiredArguments(1, 3);
        isProcedural = false;
    }

    // <--[command]
    // @Name Break
    // @Syntax break [<location>] (<npc>) (radius:<#.#>)
    // @Required 1
    // @Maximum 3
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
    // The break command is ~waitable. Refer to <@link language ~waitable>.
    //
    // @Tags
    // <NPCTag.is_navigating>
    // <NPCTag.target_location>
    //
    // @Usage
    // Use to make the npc break a related block.
    // - ~break <context.location>
    //
    // @Usage
    // Use to make a different NPC break a related block.
    // - ~break <context.location> <[some_npc]>
    //
    // @Usage
    // Use to make a different NPC break a related block and start digging from 5 blocks away.
    // - ~break <context.location> <[some_npc]> radius:5
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (Argument arg : scriptEntry) {

            if (!scriptEntry.hasObject("location")
                    && arg.matchesArgumentType(LocationTag.class)) {
                scriptEntry.addObject("location", arg.asType(LocationTag.class));
            }
            else if (!scriptEntry.hasObject("npc")
                    && arg.matchesArgumentType(NPCTag.class)) {
                scriptEntry.addObject("npc", arg.asType(NPCTag.class));
            }
            else if (!scriptEntry.hasObject("radius")
                    && arg.matchesFloat()) {
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

        final LocationTag location = scriptEntry.getObjectTag("location");
        final NPCTag npc = scriptEntry.getObjectTag("npc");
        ElementTag radius = scriptEntry.getElement("radius");
        final HashMap<String, ObjectTag> context = new HashMap<>();
        MaterialTag material = new MaterialTag(location.getBlock());
        context.put("location", location);
        context.put("material", material);
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), location, npc, radius);
        }
        final ScriptEntry se = scriptEntry;
        BlockBreaker.BlockBreakerConfiguration config = new BlockBreaker.BlockBreakerConfiguration();
        config.item(npc.getLivingEntity().getEquipment().getItemInMainHand());
        config.radius(radius.asDouble());
        config.callback(() -> {
            npc.action("dig", null, context);
            se.setFinished(true);
        });

        BlockBreaker breaker = npc.getCitizen().getBlockBreaker(location.getBlock(), config);
        if (breaker.shouldExecute()) {
            TaskRunnable run = new TaskRunnable(breaker);
            run.taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(Denizen.getInstance(), run, 0, 1);
        }
        else {
            se.setFinished(true);
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
