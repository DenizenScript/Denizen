package com.denizenscript.denizen.npc.traits;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.events.bukkit.ScriptReloadEvent;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.scripts.containers.core.AssignmentScriptContainer;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.Settings;
import com.denizenscript.denizencore.scripts.ScriptRegistry;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import org.bukkit.event.EventHandler;

import java.util.*;
import java.util.stream.Collectors;

public class AssignmentTrait extends Trait {

    @Persist("assignment")
    public String legacyAssignment = "";

    @Persist("assignment_list")
    public ArrayList<String> assignments = new ArrayList<>();

    public ArrayList<AssignmentScriptContainer> containerCache = new ArrayList<>();

    public AssignmentTrait() {
        super("assignment");
    }

    /**
     * Checks to see if the NPCs assignment is still a valid script on load of NPC.
     */
    @Override
    public void load(DataKey key) {
        if (legacyAssignment != null && !legacyAssignment.isEmpty()) {
            assignments.add(CoreUtilities.toLowerCase(legacyAssignment));
            legacyAssignment = "";
        }
        buildCache();
        if (assignments.isEmpty()) {
            Debug.echoError("NPC " + npc.getId() + " had assignment trait without any assignments? Removing.");
            npc.removeTrait(AssignmentTrait.class);
            return;
        }
        // Fix legacy assignments that might not be lowercased
        List<String> fixedAssignments = assignments.stream().map(CoreUtilities::toLowerCase).collect(Collectors.toList());
        assignments.clear();
        assignments.addAll(fixedAssignments);
        npc.getOrAddTrait(ConstantsTrait.class).rebuildAssignmentConstants();
    }

    public void buildCache() {
        containerCache.clear();
        for (String assignment : assignments) {
            AssignmentScriptContainer container = ScriptRegistry.getScriptContainer(assignment);
            containerCache.add(container);
            if (container == null) {
                Debug.echoError("NPC " + npc.getId() + " has assignment '" + assignment + "' which does not exist.");
            }
        }
    }

    @EventHandler
    public void onReload(ScriptReloadEvent event) {
        buildCache();
    }

    // <--[action]
    // @Actions
    // assignment
    //
    // @Triggers when the assignment script is added to an NPC.
    //
    // @Context
    // None
    //
    // -->
    public boolean addAssignmentScript(AssignmentScriptContainer script, PlayerTag player) {
        String name = CoreUtilities.toLowerCase(script.getName());
        if (assignments.contains(name)) {
            return false;
        }
        assignments.add(name);
        containerCache.add(script);
        ensureDefaultTraits();
        Denizen.getInstance().npcHelper.getActionHandler().doAction("assignment", new NPCTag(npc), player, script, null);
        return true;
    }

    // <--[action]
    // @Actions
    // remove assignment
    //
    // @Triggers when the assignment script is removed from an NPC.
    //
    // @Context
    // None
    //
    // -->
    public boolean removeAssignmentScript(String name, PlayerTag player) {
        int index = assignments.indexOf(CoreUtilities.toLowerCase(name));
        if (index == -1) {
            return false;
        }
        assignments.remove(index);
        AssignmentScriptContainer container = containerCache.remove(index);
        if (container != null) {
            Denizen.getInstance().npcHelper.getActionHandler().doAction("remove assignment", new NPCTag(npc), player, container, null);
        }
        return true;
    }

    public void clearAssignments(PlayerTag player) {
        for (String assign : new ArrayList<>(assignments)) {
            removeAssignmentScript(assign, player);
        }
    }

    public void checkAutoRemove() {
        if (assignments.isEmpty()) {
            npc.removeTrait(AssignmentTrait.class);
        }
    }

    public void ensureDefaultTraits() {
        npc.getOrAddTrait(TriggerTrait.class);
        npc.getOrAddTrait(ConstantsTrait.class).rebuildAssignmentConstants();
        if (Settings.healthTraitEnabledByDefault()) {
            npc.getOrAddTrait(HealthTrait.class);
        }
    }

    public boolean isAssigned(AssignmentScriptContainer container) {
        return containerCache.contains(container);
    }
}
