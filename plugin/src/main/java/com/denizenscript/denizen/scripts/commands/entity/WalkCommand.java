package com.denizenscript.denizen.scripts.commands.entity;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.depends.Depends;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.scripts.commands.Holdable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WalkCommand extends AbstractCommand implements Holdable {

    public WalkCommand() {
        setName("walk");
        setSyntax("walk (<entity>|...) [<location>/stop] (speed:<#.#>) (auto_range) (radius:<#.#>) (lookat:<location>)");
        setRequiredArguments(1, 6);
        if (Depends.citizens != null) {
            Denizen.getInstance().getServer().getPluginManager().registerEvents(new WalkCommandCitizensEvents(), Denizen.getInstance());
        }
        isProcedural = false;
    }

    // <--[command]
    // @Name Walk
    // @Syntax walk (<entity>|...) [<location>/stop] (speed:<#.#>) (auto_range) (radius:<#.#>) (lookat:<location>)
    // @Required 1
    // @Maximum 6
    // @Short Causes an entity or list of entities to walk to another location.
    // @Group entity
    //
    // @Description
    // Causes an entity or list of entities to walk to another location.
    //
    // Specify a destination location to walk to, or 'stop' to stop all walking.
    //
    // Optionally, specify a "speed:<#.#>" argument to control the speed of the NPCs.
    //
    // Optionally, specify "auto_range" to automatically set the path range for the walk instruction
    // (if not specified, an NPC will not be able to walk to a location outside of its existing path range, by default 25 blocks).
    // (Does not apply to non-NPC entities).
    //
    // Note that in most cases, the walk command should not be used for paths longer than 100 blocks.
    // For ideal performance, keep it below 25.
    //
    // Optionally, specify a list of entities to give them all the same walk instruction at the same time.
    // If the list is of NPCs, optionally specify a "radius:<#.#>" argument to change the flocking radius.
    // ('Radius' does not apply to non-NPC entities).
    //
    // Optionally, specify "lookat:<location>" to cause the NPCs to stare at a specific location while walking (as opposed to straight ahead).
    // ('Radius' does not apply to non-NPC entities).
    //
    // The walk command is ~waitable. Refer to <@link language ~waitable>.
    //
    // @Tags
    // <NPCTag.is_navigating>
    // <NPCTag.speed>
    // <NPCTag.range>
    // <NPCTag.target_location>
    //
    // @Usage
    // Use to make the NPC walk to an anchored position.
    // - walk <npc> <npc.anchor[spot1]>
    //
    // @Usage
    // Use to make the NPC walk to an anchored position that may be far away.
    // - walk <npc> <npc.anchor[spot2]> auto_range
    //
    // @Usage
    // Use to make the NPC walk to an anchored position while looking backwards.
    // - walk <npc> <npc.anchor[spot3]> lookat:<npc.anchor[spot2]>
    //
    // @Usage
    // Use to make the NPC walk to an anchored position, and then say something after arrival, using ~waitable syntax.
    // - ~walk <npc> <npc.anchor[spot4]>
    // - chat "I'm here!"
    //
    // @Usage
    // Use to make a list of NPCs stored in a flag all move together, with a flocking radius based on the number of NPCs included.
    // - walk <player.flag[squad]> radius:<player.flag[squad].size> <player.location>
    // -->

    @Override
    public void addCustomTabCompletions(TabCompletionsBuilder tab) {
        tab.addNotesOfType(LocationTag.class);
    }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
            if (!scriptEntry.hasObject("lookat")
                    && arg.matchesPrefix("lookat")
                    && arg.matchesArgumentType(LocationTag.class)) {
                scriptEntry.addObject("lookat", arg.asType(LocationTag.class));
            }
            else if (!scriptEntry.hasObject("speed")
                    && arg.matchesFloat()
                    && arg.matchesPrefix("s", "speed")) {
                scriptEntry.addObject("speed", arg.asElement());
            }
            else if (!scriptEntry.hasObject("auto_range")
                    && arg.matches("auto_range")) {
                scriptEntry.addObject("auto_range", new ElementTag(true));
            }
            else if (!scriptEntry.hasObject("radius")
                    && arg.matchesFloat()
                    && arg.matchesPrefix("radius")) {
                scriptEntry.addObject("radius", arg.asElement());
            }
            else if (!scriptEntry.hasObject("stop")
                    && arg.matches("stop")) {
                scriptEntry.addObject("stop", new ElementTag(true));
            }
            else if (!scriptEntry.hasObject("location")
                    && arg.matchesArgumentType(LocationTag.class)) {
                scriptEntry.addObject("location", arg.asType(LocationTag.class));
            }
            else if (!scriptEntry.hasObject("entities")
                    && arg.matchesArgumentList(EntityTag.class)) {
                scriptEntry.addObject("entities", arg.asType(ListTag.class).filter(EntityTag.class, scriptEntry));
            }
            else {
                arg.reportUnhandled();
            }
        }
        if (!scriptEntry.hasObject("location") && !scriptEntry.hasObject("stop")) {
            throw new InvalidArgumentsException("Must specify a location!");
        }
        if (!scriptEntry.hasObject("entities")) {
            if (Utilities.getEntryNPC(scriptEntry) == null
                    || !Utilities.getEntryNPC(scriptEntry).isValid()
                    || !Utilities.getEntryNPC(scriptEntry).isSpawned()) {
                throw new InvalidArgumentsException("Must have a valid spawned NPC attached, or an entity specified.");
            }
            else {
                scriptEntry.addObject("entities", Collections.singletonList(Utilities.getEntryNPC(scriptEntry).getDenizenEntity()));
            }
        }
        scriptEntry.defaultObject("stop", new ElementTag(false));
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        LocationTag loc = scriptEntry.getObjectTag("location");
        ElementTag speed = scriptEntry.getElement("speed");
        ElementTag auto_range = scriptEntry.getElement("auto_range");
        ElementTag radius = scriptEntry.getElement("radius");
        ElementTag stop = scriptEntry.getElement("stop");
        List<EntityTag> entities = (List<EntityTag>) scriptEntry.getObject("entities");
        final LocationTag lookat = scriptEntry.getObjectTag("lookat");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), loc, speed, auto_range, radius, lookat, stop, (db("entities", entities)));
        }
        boolean shouldStop = stop.asBoolean();
        List<NPCTag> npcs = new ArrayList<>();
        final List<EntityTag> waitForEntities = new ArrayList<>();
        for (final EntityTag entity : entities) {
            if (entity.isCitizensNPC()) {
                NPCTag npc = entity.getDenizenNPC();
                npcs.add(npc);
                if (!npc.isSpawned()) {
                    Debug.echoError(scriptEntry, "NPC " + npc.identify() + " is not spawned!");
                    continue;
                }
                if (shouldStop) {
                    npc.getNavigator().cancelNavigation();
                    continue;
                }
                if (auto_range != null && auto_range.asBoolean()) {
                    double distance = npc.getLocation().distance(loc);
                    if (npc.getNavigator().getLocalParameters().range() < distance + 10) {
                        npc.getNavigator().getLocalParameters().range((float) distance + 10);
                    }
                }
                npc.getNavigator().setTarget(loc);
                if (lookat != null) {
                    npc.getNavigator().getLocalParameters().lookAtFunction(nav -> lookat);
                }
                if (speed != null) {
                    npc.getNavigator().getLocalParameters().speedModifier(speed.asFloat());
                }
                if (radius != null) {
                    npc.getNavigator().getLocalParameters().addRunCallback(WalkCommandCitizensEvents.generateNewFlocker(npc.getCitizen(), radius.asDouble()));
                }
            }
            else if (shouldStop) {
                NMSHandler.entityHelper.stopWalking(entity.getBukkitEntity());
            }
            else {
                waitForEntities.add(entity);
                NMSHandler.entityHelper.walkTo(entity.getLivingEntity(), loc, speed != null ? speed.asDouble() : null,
                        () -> checkHeld(entity));
            }
        }
        if (scriptEntry.shouldWaitFor()) {
            held.add(scriptEntry);
            if (!npcs.isEmpty()) { // TODO: de-jank this
                scriptEntry.addObject("tally", npcs);
            }
            if (!waitForEntities.isEmpty()) {
                scriptEntry.addObject("entities", waitForEntities);
            }
        }
    }

    // Held script entries
    public static List<ScriptEntry> held = new ArrayList<>();

    public void checkHeld(EntityTag entity) {
        for (int i = 0; i < held.size(); i++) {
            ScriptEntry entry = held.get(i);
            List<EntityTag> waitForEntities = (List<EntityTag>) entry.getObject("entities");
            if (waitForEntities == null) {
                continue;
            }
            waitForEntities.remove(entity);
            if (waitForEntities.isEmpty()) {
                if (!entry.hasObject("tally") || ((List<NPCTag>) entry.getObject("tally")).isEmpty()) {
                    entry.setFinished(true);
                    held.remove(i);
                    i--;
                }
            }
        }
    }
}
