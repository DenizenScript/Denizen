package com.denizenscript.denizen.scripts.triggers;

import com.denizenscript.denizen.scripts.triggers.core.ChatTrigger;
import com.denizenscript.denizen.scripts.triggers.core.DamageTrigger;
import com.denizenscript.denizen.scripts.triggers.core.ProximityTrigger;
import com.denizenscript.denizencore.utilities.CoreConfiguration;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.scripts.triggers.core.ClickTrigger;
import net.citizensnpcs.api.npc.NPC;

import java.util.HashMap;
import java.util.Map;

public class TriggerRegistry {

    ////////
    // Registry
    //////

    private Map<String, AbstractTrigger> instances = new HashMap<>();
    private Map<Class<? extends AbstractTrigger>, String> classes = new HashMap<>();

    public void disableCoreMembers() {
        for (AbstractTrigger member : instances.values()) {
            try {
                member.onDisable();
            }
            catch (Exception e) {
                Debug.echoError("Unable to disable '" + member.getClass().getName() + "'!");
                Debug.echoError(e);
            }
        }
    }

    public <T extends AbstractTrigger> T get(Class<T> clazz) {
        if (classes.containsKey(clazz)) {
            return (T) instances.get(classes.get(clazz));
        }
        else {
            return null;
        }
    }

    public AbstractTrigger get(String triggerName) {
        return instances.getOrDefault(triggerName.toUpperCase(), null);
    }

    public Map<String, AbstractTrigger> list() {
        return instances;
    }

    public void register(String triggerName, AbstractTrigger instance) {
        this.instances.put(triggerName.toUpperCase(), instance);
        this.classes.put(((AbstractTrigger) instance).getClass(), triggerName.toUpperCase());
    }

    public void registerCoreMembers() {
        new ClickTrigger().activate().as("Click");
        new ChatTrigger().activate().as("Chat");
        new DamageTrigger().activate().as("Damage");
        new ProximityTrigger().activate().as("Proximity");
        if (CoreConfiguration.debugVerbose) {
            Debug.echoApproval("Loaded core triggers: " + instances.keySet());
        }
        else {
            Debug.log("Loaded <A>" + instances.size() + "<W> core triggers");
        }
    }

    /////////
    // Trigger Cooldowns
    ///////

    Map<String, Map<String, Long>> playerCooldown = new HashMap<>();

    public boolean checkCooldown(NPC npc, PlayerTag player, AbstractTrigger triggerClass) {
        if (!playerCooldown.containsKey(player.getName() + "/" + npc.getId())) {
            return true;
        }
        else if (!playerCooldown.get(player.getName() + "/" + npc.getId()).containsKey(triggerClass.name)) {
            return true;
        }
        else if (CoreUtilities.monotonicMillis() > playerCooldown.get(player.getName() + "/" + npc.getId()).get(triggerClass.name)) {
            return true;
        }
        return false;
    }

    public void setCooldown(NPC npc, PlayerTag player, AbstractTrigger triggerClass, double seconds) {
        Map<String, Long> triggerMap = new HashMap<>();
        boolean noCooldown = seconds <= 0;
        if (playerCooldown.containsKey(player.getName() + "/" + npc.getId())) {
            triggerMap = playerCooldown.get(player.getName() + "/" + npc.getId());
        }
        if (noCooldown && playerCooldown.containsKey(player.getName() + "/" + npc.getId())) {
            triggerMap.remove(player.getName() + "/" + npc.getId());
        }
        else {
            triggerMap.put(triggerClass.name, CoreUtilities.monotonicMillis() + (long) (seconds * 1000));
        }
        playerCooldown.put(player.getName() + "/" + npc.getId(), triggerMap);
    }
}
