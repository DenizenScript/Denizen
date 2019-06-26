package net.aufdemrand.denizen.scripts.commands.entity;

import com.google.common.base.Function;
import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.nms.NMSHandler;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.objects.dNPC;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.depends.Depends;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import net.aufdemrand.denizencore.scripts.commands.Holdable;
import net.citizensnpcs.api.ai.Navigator;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WalkCommand extends AbstractCommand implements Holdable {

    // <--[command]
    // @Name Walk
    // @Syntax walk (<entity>|...) [<location>/stop] (speed:<#.#>) (auto_range) (radius:<#.#>) (lookat:<location>)
    // @Required 1
    // @Short Causes an entity or list of entities to walk to another location.
    // @Group entity
    //
    // @Description
    // TODO: Document Command Details
    //
    // @Tags
    // <n@npc.navigator.is_navigating>
    // <n@npc.navigator.speed>
    // <n@npc.navigator.range>
    // <n@npc.navigator.target_location>
    //
    // @Usage
    // TODO: Document Command Details
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Interpret arguments

        for (aH.Argument arg : aH.interpretArguments(scriptEntry.aHArgs)) {

            if (!scriptEntry.hasObject("lookat")
                    && arg.matchesPrefix("lookat")
                    && arg.matchesArgumentType(dLocation.class)) {
                scriptEntry.addObject("lookat", arg.asType(dLocation.class));
            }
            else if (!scriptEntry.hasObject("speed")
                    && arg.matchesPrimitive(aH.PrimitiveType.Percentage)
                    && arg.matchesPrefix("s, speed")) {
                scriptEntry.addObject("speed", arg.asElement());
            }
            else if (!scriptEntry.hasObject("auto_range")
                    && arg.matches("auto_range")) {
                scriptEntry.addObject("auto_range", new Element(true));
            }
            else if (!scriptEntry.hasObject("radius")
                    && arg.matchesPrimitive(aH.PrimitiveType.Double)
                    && arg.matchesPrefix("radius")) {
                scriptEntry.addObject("radius", arg.asElement());
            }
            else if (!scriptEntry.hasObject("stop")
                    && arg.matches("stop")) {
                scriptEntry.addObject("stop", new Element(true));
            }
            else if (!scriptEntry.hasObject("location")
                    && arg.matchesArgumentType(dLocation.class)) {
                scriptEntry.addObject("location", arg.asType(dLocation.class));
            }
            else if (!scriptEntry.hasObject("entities")
                    && arg.matchesArgumentList(dEntity.class)) {
                scriptEntry.addObject("entities", arg.asType(dList.class).filter(dEntity.class, scriptEntry));
            }
            else {
                arg.reportUnhandled();
            }
        }


        // Check for required information

        if (!scriptEntry.hasObject("location") && !scriptEntry.hasObject("stop")) {
            throw new InvalidArgumentsException("Must specify a location!");
        }

        if (!scriptEntry.hasObject("entities")) {
            if (((BukkitScriptEntryData) scriptEntry.entryData).getNPC() == null
                    || !((BukkitScriptEntryData) scriptEntry.entryData).getNPC().isValid()
                    || !((BukkitScriptEntryData) scriptEntry.entryData).getNPC().isSpawned()) {
                throw new InvalidArgumentsException("Must have a valid spawned NPC attached.");
            }
            else {
                scriptEntry.addObject("entities",
                        Arrays.asList(((BukkitScriptEntryData) scriptEntry.entryData).getNPC().getDenizenEntity()));
            }
        }

        scriptEntry.defaultObject("stop", new Element(false));
    }


    @Override
    public void execute(ScriptEntry scriptEntry) {

        // Fetch required objects

        dLocation loc = (dLocation) scriptEntry.getObject("location");
        Element speed = scriptEntry.getElement("speed");
        Element auto_range = scriptEntry.getElement("auto_range");
        Element radius = scriptEntry.getElement("radius");
        Element stop = scriptEntry.getElement("stop");
        List<dEntity> entities = (List<dEntity>) scriptEntry.getObject("entities");
        final dLocation lookat = scriptEntry.getdObject("lookat");


        // Debug the execution

        if (scriptEntry.dbCallShouldDebug()) {

            dB.report(scriptEntry, getName(), (loc != null ? loc.debug() : "")
                    + (speed != null ? speed.debug() : "")
                    + (auto_range != null ? auto_range.debug() : "")
                    + (radius != null ? radius.debug() : "")
                    + (lookat != null ? lookat.debug() : "")
                    + stop.debug()
                    + (aH.debugObj("entities", entities)));

        }

        // Do the execution

        boolean shouldStop = stop.asBoolean();

        List<dNPC> npcs = new ArrayList<>();
        final List<dEntity> waitForEntities = new ArrayList<>();
        for (final dEntity entity : entities) {
            if (entity.isCitizensNPC()) {
                dNPC npc = entity.getDenizenNPC();
                npcs.add(npc);
                if (!npc.isSpawned()) {
                    dB.echoError(scriptEntry.getResidingQueue(), "NPC " + npc.identify() + " is not spawned!");
                    continue;
                }

                if (shouldStop) {
                    npc.getNavigator().cancelNavigation();
                    continue;
                }

                if (auto_range != null
                        && auto_range.asBoolean()) {
                    double distance = npc.getLocation().distance(loc);
                    if (npc.getNavigator().getLocalParameters().range() < distance + 10) {
                        npc.getNavigator().getLocalParameters().range((float) distance + 10);
                    }
                }

                npc.getNavigator().setTarget(loc);

                if (lookat != null) {
                    npc.getNavigator().getLocalParameters().lookAtFunction(new Function<Navigator, Location>() {
                        @Override
                        public Location apply(Navigator nav) {
                            return lookat;
                        }
                    });
                }

                if (speed != null) {
                    npc.getNavigator().getLocalParameters().speedModifier(speed.asFloat());
                }

                if (radius != null) {
                    npc.getNavigator().getLocalParameters().addRunCallback(WalkCommandCitizensEvents
                            .generateNewFlocker(npc.getCitizen(), radius.asDouble()));
                }
            }
            else if (shouldStop) {
                NMSHandler.getInstance().getEntityHelper().stopWalking(entity.getBukkitEntity());
            }
            else {
                waitForEntities.add(entity);
                NMSHandler.getInstance().getEntityHelper().walkTo(entity.getBukkitEntity(), loc, speed != null ? speed.asDouble() : 0.2,
                        new Runnable() {
                            @Override
                            public void run() {
                                checkHeld(entity);
                            }
                        });
            }
        }

        if (scriptEntry.shouldWaitFor()) {
            held.add(scriptEntry);
            if (!npcs.isEmpty()) {
                scriptEntry.addObject("tally", npcs);
            }
            if (!waitForEntities.isEmpty()) {
                scriptEntry.addObject("entities", waitForEntities);
            }
        }

    }


    // Held script entries
    public static List<ScriptEntry> held = new ArrayList<>();

    public void checkHeld(dEntity entity) {
        for (int i = 0; i < held.size(); i++) {
            ScriptEntry entry = held.get(i);
            List<dEntity> waitForEntities = (List<dEntity>) entry.getObject("entities");
            if (waitForEntities == null) {
                continue;
            }
            waitForEntities.remove(entity);
            if (waitForEntities.isEmpty()) {
                if (!entry.hasObject("tally") || ((List<dNPC>) entry.getObject("tally")).isEmpty()) {
                    entry.setFinished(true);
                    held.remove(i);
                    i--;
                }
            }
        }
    }

    @Override
    public void onEnable() {
        if (Depends.citizens != null) {
            DenizenAPI.getCurrentInstance().getServer().getPluginManager()
                    .registerEvents(new WalkCommandCitizensEvents(), DenizenAPI.getCurrentInstance());
        }
    }
}
