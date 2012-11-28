package net.aufdemrand.denizen.scripts.triggers;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.interfaces.DenizenRegistry;
import net.aufdemrand.denizen.interfaces.RegistrationableInstance;
import net.aufdemrand.denizen.scripts.triggers.core.ClickTrigger;
import net.citizensnpcs.api.npc.NPC;

public class TriggerRegistry implements DenizenRegistry {

    public Denizen denizen;

    public TriggerRegistry(Denizen denizen) {
        this.denizen = denizen;
    }

    private Map<String, AbstractTrigger> instances = new HashMap<String, AbstractTrigger>();
    private Map<Class<? extends AbstractTrigger>, String> classes = new HashMap<Class<? extends AbstractTrigger>, String>();

    @Override
    public boolean register(String triggerName, RegistrationableInstance instance) {
        this.instances.put(triggerName.toUpperCase(), (AbstractTrigger) instance);
        this.classes.put(((AbstractTrigger) instance).getClass(), triggerName.toUpperCase());
        return true;
    }

    @Override
    public Map<String, AbstractTrigger> list() {
        return instances;
    }

    @Override
    public AbstractTrigger get(String triggerName) {
        if (instances.containsKey(triggerName.toUpperCase())) return instances.get(triggerName.toUpperCase());
        else return null;
    }

    @Override
    public <T extends RegistrationableInstance> T get(Class<T> clazz) {
        if (classes.containsKey(clazz)) return (T) clazz.cast(instances.get(classes.get(clazz)));
        else return null;
    }

    @Override
    public void registerCoreMembers() {
        new ClickTrigger().activate().as("Click").withOptions(true, 2);
        denizen.getDebugger().echoApproval("Loaded core triggers: " + instances.keySet().toString());
    }

    /*
     * Trigger cool-downs are used by Denizen internally in intervals specified by the config.
     * Not to be confused with Script Cool-downs. 
     */

    Map<NPC, Map<Class<?>, Long>> cooldown = new ConcurrentHashMap<NPC, Map<Class<?>,Long>>();

    public boolean checkCooldown(NPC npc, AbstractTrigger triggerClass) {
        if (!cooldown.containsKey(npc)) return true;
        else if (!cooldown.get(npc).containsKey(triggerClass)) return true;
        else if (System.currentTimeMillis() > cooldown.get(npc).get(triggerClass)) return true;
        else return false;
    }

    public void setCooldown(NPC npc, Class<?> triggerClass, int seconds) {
        Map<Class<?>, Long> triggerMap = new HashMap<Class<?>, Long>();
        triggerMap.put(triggerClass, System.currentTimeMillis() + (seconds * 1000));
        cooldown.put(npc, triggerMap);
    }

}
