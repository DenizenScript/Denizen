package net.aufdemrand.denizen.scripts.requirements;

import net.aufdemrand.denizen.objects.dNPC;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequirementsContext {

    protected RequirementsMode mode;
    protected List<String> list;

    private Map<String, Object> objects = new HashMap<String, Object>();

    protected dPlayer player = null;
    protected dNPC npc = null;
    protected ScriptContainer container;

    public RequirementsContext(RequirementsMode mode, List<String> list, ScriptContainer scriptContainer) {
        this.mode = mode;
        this.list = list;
        this.container = scriptContainer;
    }

    public RequirementsContext attachPlayer(dPlayer player) {
        this.player = player;
        return this;
    }

    public RequirementsContext attachNPC(dNPC npc) {
        this.npc = npc;
        return this;
    }

    public dNPC getNPC() {
        return npc;
    }

    public dPlayer getPlayer() {
        return player;
    }

    public ScriptContainer getScriptContainer() {
        return container;
    }

    public RequirementsContext addObject(String key, Object obj) {
        key = key.toUpperCase();
        objects.put(key, obj);
        return this;
    }

    public boolean hasObject(String key) {
        key = key.toUpperCase();
        return objects.containsKey(key);
    }

    public Object getObject(String key) {
        key = key.toUpperCase();
        if (objects.containsKey(key)) {
            return objects.get(key);
        }
        return null;
    }
}
