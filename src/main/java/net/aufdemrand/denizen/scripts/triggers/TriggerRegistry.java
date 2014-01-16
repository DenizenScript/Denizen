package net.aufdemrand.denizen.scripts.triggers;

import net.aufdemrand.denizen.interfaces.dRegistry;
import net.aufdemrand.denizen.interfaces.RegistrationableInstance;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.scripts.triggers.core.ChatTrigger;
import net.aufdemrand.denizen.scripts.triggers.core.ClickTrigger;
import net.aufdemrand.denizen.scripts.triggers.core.DamageTrigger;
import net.aufdemrand.denizen.scripts.triggers.core.ProximityTrigger;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.citizensnpcs.api.npc.NPC;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TriggerRegistry implements dRegistry {

    ////////
    // Registry
    //////

    private Map<String, AbstractTrigger> instances = new HashMap<String, AbstractTrigger>();
    private Map<Class<? extends AbstractTrigger>, String> classes = new HashMap<Class<? extends AbstractTrigger>, String>();

    @Override
    public void disableCoreMembers() {
        for (RegistrationableInstance member : instances.values())
            try {
                member.onDisable();
            } catch (Exception e) {
                dB.echoError("Unable to disable '" + member.getClass().getName() + "'!");
                dB.echoError(e);
            }
    }

    @Override
    public <T extends RegistrationableInstance> T get(Class<T> clazz) {
        if (classes.containsKey(clazz)) return clazz.cast(instances.get(classes.get(clazz)));
        else return null;
    }

    @Override
    public AbstractTrigger get(String triggerName) {
        if (instances.containsKey(triggerName.toUpperCase())) return instances.get(triggerName.toUpperCase());
        else return null;
    }
    @Override
    public Map<String, AbstractTrigger> list() {
        return instances;
    }

    @Override
    public boolean register(String triggerName, RegistrationableInstance instance) {
        this.instances.put(triggerName.toUpperCase(), (AbstractTrigger) instance);
        this.classes.put(((AbstractTrigger) instance).getClass(), triggerName.toUpperCase());
        return true;
    }

    @Override
    public void registerCoreMembers() {
        new ClickTrigger().activate().as("Click");
        new ChatTrigger().activate().as("Chat");
        new DamageTrigger().activate().as("Damage");
        new ProximityTrigger().activate ().as("Proximity");
        dB.echoApproval("Loaded core triggers: " + instances.keySet().toString());
    }


    /////////
    // Trigger Cooldowns
    ///////

    Map<Integer, Map<String, Long>> npcCooldown    = new ConcurrentHashMap<Integer, Map<String,Long>>(8, 0.9f, 1);
    Map <String, Map<String, Long>> playerCooldown = new ConcurrentHashMap <String, Map<String,Long>>(8, 0.9f, 1);

    public enum CooldownType { NPC, PLAYER }

    /*
     * Trigger cool-downs are used by Denizen internally in intervals specified by the config.
     * Not to be confused with Script Cool-downs.
     */

    public boolean checkCooldown(NPC npc, dPlayer player, AbstractTrigger triggerClass, CooldownType cooldownType) {
        switch (cooldownType) {
            case NPC:
                // Check npcCooldown
                if (!npcCooldown.containsKey(npc.getId())) return true;
                else if (!npcCooldown.get(npc.getId()).containsKey(triggerClass.name)) return true;
                else if (System.currentTimeMillis() > npcCooldown.get(npc.getId()).get(triggerClass.name)) return true;
                break;

            case PLAYER:
                // Check playerCooldown
                if (!playerCooldown.containsKey(player.getName() + "/" + npc.getId())) return true;
                else if (!playerCooldown.get(player.getName() + "/" + npc.getId()).containsKey(triggerClass.name)) return true;
                else if (System.currentTimeMillis() > playerCooldown.get(player.getName() + "/" + npc.getId()).get(triggerClass.name)) return true;
                break;
        }

        // If broken from the switch, the trigger is not cool.
        return false;
    }

    public void setCooldown(NPC npc, dPlayer player, AbstractTrigger triggerClass, double seconds, CooldownType cooldownType) {
        Map<String, Long> triggerMap = new HashMap<String, Long>();

        switch (cooldownType) {
            case NPC:
                // set npcCooldown
                if (npcCooldown.containsKey(npc.getId()))
                    triggerMap = npcCooldown.get(npc.getId());
                triggerMap.put(triggerClass.name, System.currentTimeMillis() + (long) (seconds * 1000));
                npcCooldown.put(npc.getId(), triggerMap);
                break;

            case PLAYER:
                // set playerCooldown
                if (playerCooldown.containsKey(player.getName() + "/" + npc.getId()))
                    triggerMap = playerCooldown.get(player.getName() + "/" + npc.getId());
                triggerMap.put(triggerClass.name, System.currentTimeMillis() + (long) (seconds * 1000));
                playerCooldown.put(player.getName() + "/" + npc.getId(), triggerMap);
                break;
        }

        // All done!
    }

}
