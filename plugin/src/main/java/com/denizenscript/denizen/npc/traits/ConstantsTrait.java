package com.denizenscript.denizen.npc.traits;

import com.denizenscript.denizen.events.bukkit.ScriptReloadEvent;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.scripts.containers.core.AssignmentScriptContainer;
import com.denizenscript.denizen.tags.BukkitTagContext;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.text.StringHolder;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import org.bukkit.event.EventHandler;

import java.util.HashMap;
import java.util.Map;

public class ConstantsTrait extends Trait {

    // Saved to C2 saves.yml
    @Persist(value = "", collectionType = HashMap.class)
    private Map<String, String> constants = new HashMap<>();

    // Used internally
    private Map<String, String> assignmentConstants = new HashMap<>();

    public ConstantsTrait() {
        super("constants");
    }

    public String getConstant(String name) {
        if (constants.containsKey(CoreUtilities.toLowerCase(name))) {
            return TagManager.tag(constants.get(CoreUtilities.toLowerCase(name)), new BukkitTagContext(null, new NPCTag(npc), null, true, null));
        }
        else if (assignmentConstants.containsKey(CoreUtilities.toLowerCase(name))) {
            return TagManager.tag(assignmentConstants.get(CoreUtilities.toLowerCase(name)), new BukkitTagContext(null, new NPCTag(npc), null, true, null));
        }
        return null;
    }

    public void setConstant(String name, String value) {
        constants.put(CoreUtilities.toLowerCase(name), value);
    }

    public void removeConstant(String name) {
        constants.remove(CoreUtilities.toLowerCase(name));
    }

    public void rebuildAssignmentConstants() {
        assignmentConstants.clear();
        if (!npc.hasTrait(AssignmentTrait.class)) {
            npc.removeTrait(ConstantsTrait.class);
            return;
        }
        AssignmentTrait trait = npc.getOrAddTrait(AssignmentTrait.class);
        for (AssignmentScriptContainer container : trait.containerCache) {
            if (container != null) {
                if (container.contains("default constants", Map.class)) {
                    for (StringHolder constant : container.getConfigurationSection("default constants").getKeys(false)) {
                        assignmentConstants.put(CoreUtilities.toLowerCase(constant.str), container.getString("default constants." + constant.str.toUpperCase(), ""));
                    }
                }
            }
        }
    }

    @EventHandler
    public void onScriptsReload(ScriptReloadEvent event) {
        rebuildAssignmentConstants();
    }
}
