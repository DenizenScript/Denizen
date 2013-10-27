package net.aufdemrand.denizen.npc.traits;

import net.aufdemrand.denizen.Settings;
import net.aufdemrand.denizen.npc.dNPCRegistry;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.scripts.commands.npc.EngageCommand;
import net.aufdemrand.denizen.scripts.triggers.AbstractTrigger;
import net.aufdemrand.denizen.scripts.triggers.TriggerRegistry.CooldownType;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.command.exception.CommandException;
import net.citizensnpcs.api.util.Paginator;
import net.citizensnpcs.util.Messages;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class TriggerTrait extends Trait implements Listener {

    @Persist(value="enabled", collectionType=ConcurrentHashMap.class)
    private Map<String, Boolean> enabled = new ConcurrentHashMap<String, Boolean>(8, 0.9f, 1);
    @Persist(value="duration", collectionType=ConcurrentHashMap.class)
    private Map<String, Double> duration = new ConcurrentHashMap<String, Double>(8, 0.9f, 1);
    @Persist(value="cooldowntype", collectionType=ConcurrentHashMap.class)
    private Map<String, CooldownType> type = new ConcurrentHashMap<String, CooldownType>(8, 0.9f, 1);
    @Persist(value="radius", collectionType=ConcurrentHashMap.class)
    private Map<String, Integer> radius = new ConcurrentHashMap<String, Integer>(8, 0.9f, 1);

    public void report() {
        dB.echoDebug("enabled: " + enabled.entrySet().toString());
        dB.echoDebug("duration: " + duration.entrySet().toString());
        dB.echoDebug("type: " + type.entrySet().toString());
        dB.echoDebug("radius: " + radius.entrySet().toString());
    }

    public TriggerTrait() {
        super("triggers");
        for (String triggerName : DenizenAPI.getCurrentInstance().getTriggerRegistry().list().keySet())
            if (!enabled.containsKey(triggerName))
                enabled.put(triggerName, Settings.TriggerEnabled(triggerName));
    }

    public void onSpawn() {
        for (String triggerName : DenizenAPI.getCurrentInstance().getTriggerRegistry().list().keySet())
            if (!enabled.containsKey(triggerName))
                enabled.put(triggerName, Settings.TriggerEnabled(triggerName));
    }

    /**
     * Toggles a trigger on or off for this NPC.
     *
     * @param triggerName name of the Trigger, as specified by the Trigger. Case in-sensitive.
     * @param toggle new state of the trigger
     * @return output debug information.
     */
    public String toggleTrigger(String triggerName, boolean toggle) {
        if (enabled.containsKey(triggerName.toUpperCase())) {
                enabled.put(triggerName.toUpperCase(), toggle);
                return triggerName + " trigger is now " + (toggle ? "enabled." : "disabled.");
            }
        else return triggerName + " trigger not found!";
    }

    public String toggleTrigger(String triggerName) {
        if (enabled.containsKey(triggerName.toUpperCase()))
            if (enabled.get(triggerName.toUpperCase())) {
                enabled.put(triggerName.toUpperCase(), false);
                return triggerName + " trigger is now disabled.";
            } else {
                enabled.put(triggerName.toUpperCase(), true);
                return triggerName + " trigger is now enabled.";
            }
        else return triggerName + " trigger not found!";
    }

    public boolean hasTrigger(String triggerName) {
        return enabled.containsKey(triggerName.toUpperCase()) && enabled.get(triggerName.toUpperCase());
    }

    public boolean isEnabled(String triggerName) {
        if (!DenizenAPI.getDenizenNPC(npc).getAssignmentTrait().hasAssignment()) return false;
        if (enabled.containsKey(triggerName.toUpperCase()))
            return enabled.get(triggerName.toUpperCase());
        else return false;
    }

    public void setLocalCooldown(String triggerName, double value) {
        duration.put(triggerName.toUpperCase(), value);
    }

    public double getCooldownDuration(String triggerName) {
        if (duration.containsKey(triggerName.toUpperCase()))
            return duration.get(triggerName.toUpperCase());
        else return Settings.TriggerDefaultCooldown(triggerName);
    }

    public CooldownType getCooldownType(String triggerName) {
        try {
        if (type.containsKey(triggerName.toUpperCase()))
            return type.get(triggerName.toUpperCase());
        else return CooldownType.valueOf(Settings.TriggerDefaultCooldownType(triggerName).toUpperCase());
        } catch (Exception e) { return CooldownType.PLAYER; }
    }

    public void setLocalRadius(String triggerName, int value) {
        radius.put(triggerName.toUpperCase(), value);
    }

    public double getRadius(String triggerName) {
        if (radius.containsKey(triggerName.toUpperCase()))
            return radius.get(triggerName.toUpperCase());
        else return Settings.TriggerDefaultRange(triggerName);
    }

    public void describe(CommandSender sender, int page) throws CommandException {
        Paginator paginator = new Paginator().header("Triggers");
        paginator.addLine("<e>Key: <a>Name  <b>Status  <c>Cooldown  <d>Cooldown Type  <e>(Radius)");
        for (Entry<String, Boolean> entry : enabled.entrySet()) {
            String line = "<a> " + entry.getKey()
                    + "<b> " + (entry.getValue() ? "Enabled" : "Disabled")
                    + "<c> " + getCooldownDuration(entry.getKey())
                    + "<d> " + getCooldownType(entry.getKey()).name()
                    + "<e> " + (getRadius(entry.getKey()) == -1 ? "" : getRadius(entry.getKey()));
            paginator.addLine(line);
        }
        if (!paginator.sendPage(sender, page))
            throw new CommandException(Messages.COMMAND_PAGE_MISSING, page);
    }

    public boolean triggerCooldownOnly(AbstractTrigger triggerClass, dPlayer player) {
        // Check cool down, return false if not yet met
        if (!DenizenAPI.getCurrentInstance().getTriggerRegistry().checkCooldown(npc, player, triggerClass, getCooldownType(triggerClass.getName())))
                return false;
        // Check engaged
        if (EngageCommand.getEngaged(npc)) {
            return false;
        }
        // Set cool down
        DenizenAPI.getCurrentInstance().getTriggerRegistry().setCooldown(npc, player, triggerClass, getCooldownDuration(triggerClass.getName()), getCooldownType(triggerClass.getName()));
        return true;
    }

    // <--[action]
    // @Actions
    // unavailable
    //
    // @Triggers when a trigger fires but the NPC is engaged.
    //
    // @Context
    // None
    //
    // -->
    public boolean trigger(AbstractTrigger triggerClass, dPlayer player) {
        // Check cool down, return false if not yet met
        if (!DenizenAPI.getCurrentInstance().getTriggerRegistry().checkCooldown(npc, player, triggerClass, getCooldownType(triggerClass.getName())))
                return false;
        // Check engaged
        if (EngageCommand.getEngaged(npc)) {
            // On Unavailable Action
            // TODO: Add triggertype context + relevant triggering contexts
            dNPCRegistry.getDenizen(npc).action("unavailable", player);
            return false;
        }
        // Set cool down, On [TriggerName] Action
        DenizenAPI.getCurrentInstance().getTriggerRegistry().setCooldown(npc, player, triggerClass, getCooldownDuration(triggerClass.getName()), getCooldownType(triggerClass.getName()));
        dNPCRegistry.getDenizen(npc).action(triggerClass.getName(), player);
        return true;
    }

}
