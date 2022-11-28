package com.denizenscript.denizen.npc.traits;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.scripts.commands.npc.EngageCommand;
import com.denizenscript.denizen.scripts.triggers.AbstractTrigger;
import com.denizenscript.denizen.utilities.Settings;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import net.citizensnpcs.api.command.exception.CommandException;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.Paginator;
import net.citizensnpcs.util.Messages;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class TriggerTrait extends Trait implements Listener {

    @Persist(value = "enabled", collectionType = HashMap.class)
    public Map<String, Boolean> enabled = new HashMap<>();
    @Persist(value = "properly_set", collectionType = HashMap.class)
    public Map<String, Boolean> properly_set = new HashMap<>();
    @Persist(value = "duration", collectionType = HashMap.class)
    private Map<String, Double> duration = new HashMap<>();
    @Persist(value = "radius", collectionType = HashMap.class)
    private Map<String, Integer> radius = new HashMap<>();

    public TriggerTrait() {
        super("triggers");
        for (Map.Entry<String, Boolean> entry : enabled.entrySet()) {
            if (!properly_set.containsKey(entry.getKey())) {
                properly_set.put(entry.getKey(), entry.getValue());
            }
        }
        for (String triggerName : Denizen.getInstance().triggerRegistry.list().keySet()) {
            if (!enabled.containsKey(triggerName)) {
                enabled.put(triggerName, Settings.triggerEnabled(triggerName));
                properly_set.put(triggerName, false);
            }
        }
    }

    @Override
    public void onSpawn() {
        if (!npc.hasTrait(AssignmentTrait.class)) {
            npc.removeTrait(TriggerTrait.class);
            return;
        }
        for (Map.Entry<String, AbstractTrigger> trigger : Denizen.getInstance().triggerRegistry.list().entrySet()) {
            if (!enabled.containsKey(trigger.getKey())) {
                enabled.put(trigger.getKey(), Settings.triggerEnabled(trigger.getKey()));
            }
            if (enabled.get(trigger.getKey())) {
                trigger.getValue().timesUsed++;
            }
        }
    }

    @Override
    public void load(DataKey key) {
        if (!key.keyExists("properly_set") && key.keyExists("enabled")) {
            for (final String triggerName : Denizen.getInstance().triggerRegistry.list().keySet()) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(Denizen.getInstance(), () -> properly_set.put(triggerName, key.getBoolean("enabled." + triggerName)));
            }
        }
    }

    /**
     * Toggles a trigger on or off for this NPC.
     *
     * @param triggerName name of the Trigger, as specified by the Trigger. Case in-sensitive.
     * @param toggle      new state of the trigger
     * @return output debug information.
     */
    public String toggleTrigger(String triggerName, boolean toggle) {
        if (enabled.containsKey(triggerName.toUpperCase())) {
            Denizen.getInstance().triggerRegistry.get(triggerName).timesUsed++;
            enabled.put(triggerName.toUpperCase(), toggle);
            properly_set.put(triggerName.toUpperCase(), true);
            return triggerName + " trigger is now " + (toggle ? "enabled." : "disabled.");
        }
        else {
            return triggerName + " trigger not found!";
        }
    }

    public boolean triggerNameIsValid(String triggerName) {
        return enabled.containsKey(triggerName.toUpperCase());
    }

    public String toggleTrigger(String triggerName) {
        if (enabled.containsKey(triggerName.toUpperCase())) {
            if (enabled.get(triggerName.toUpperCase())) {
                enabled.put(triggerName.toUpperCase(), false);
                return triggerName + " trigger is now disabled.";
            }
            else {
                enabled.put(triggerName.toUpperCase(), true);
                properly_set.put(triggerName.toUpperCase(), true);
                return triggerName + " trigger is now enabled.";
            }
        }
        else {
            return triggerName + " trigger not found!";
        }
    }

    public boolean hasTrigger(String triggerName) {
        return enabled.containsKey(triggerName.toUpperCase()) && enabled.get(triggerName.toUpperCase());
    }

    public boolean isEnabled(String triggerName) {
        if (!npc.hasTrait(AssignmentTrait.class)) {
            return false;
        }
        return enabled.getOrDefault(triggerName.toUpperCase(), false);
    }

    public void setLocalCooldown(String triggerName, double value) {
        if (value < 0) {
            value = 0;
        }
        duration.put(triggerName.toUpperCase(), value);
    }

    public double getCooldownDuration(String triggerName) {
        if (duration.containsKey(triggerName.toUpperCase())) {
            return duration.get(triggerName.toUpperCase());
        }
        else {
            return Settings.triggerDefaultCooldown(triggerName);
        }
    }

    public void setLocalRadius(String triggerName, int value) {
        radius.put(triggerName.toUpperCase(), value);
    }

    public double getRadius(String triggerName) {
        if (radius.containsKey(triggerName.toUpperCase())) {
            return radius.get(triggerName.toUpperCase());
        }
        else {
            return Settings.triggerDefaultRange(triggerName);
        }
    }

    public void describe(CommandSender sender, int page) throws CommandException {
        Paginator paginator = new Paginator().header("Triggers");
        paginator.addLine("<e>Key: <a>Name  <b>Status  <c>Cooldown  <d>Cooldown Type  <e>(Radius)");
        for (Entry<String, Boolean> entry : enabled.entrySet()) {
            String line = "<a> " + entry.getKey()
                    + "<b> " + (entry.getValue() ? "Enabled" : "Disabled")
                    + "<c> " + getCooldownDuration(entry.getKey())
                    + "<e> " + (getRadius(entry.getKey()) == -1 ? "" : getRadius(entry.getKey()));
            paginator.addLine(line);
        }
        if (!paginator.sendPage(sender, page)) {
            throw new CommandException(Messages.COMMAND_PAGE_MISSING, page);
        }
    }

    public boolean triggerCooldownOnly(AbstractTrigger triggerClass, PlayerTag player) {
        // Check cool down, return false if not yet met
        if (!Denizen.getInstance().triggerRegistry.checkCooldown(npc, player, triggerClass)) {
            return false;
        }
        // Check engaged
        if (EngageCommand.getEngaged(npc, player)) {
            return false;
        }
        // Set cool down
        Denizen.getInstance().triggerRegistry.setCooldown(npc, player, triggerClass, getCooldownDuration(triggerClass.getName()));
        return true;
    }

    // <--[action]
    // @Actions
    // unavailable
    //
    // @Triggers when a trigger fires but the NPC is engaged.
    //
    // @Context
    // <context.trigger_type> return the type of trigger fired
    //
    // -->
    public TriggerContext trigger(AbstractTrigger triggerClass, PlayerTag player) {
        return trigger(triggerClass, player, null);
    }

    public TriggerContext trigger(AbstractTrigger triggerClass, PlayerTag player, Map<String, ObjectTag> context) {
        String trigger_type = triggerClass.getName();
        if (!Denizen.getInstance().triggerRegistry.checkCooldown(npc, player, triggerClass)) {
            return new TriggerContext(false);
        }
        if (context == null) {
            context = new HashMap<>();
        }
        if (EngageCommand.getEngaged(npc, player)) {
            context.put("trigger_type", new ElementTag(trigger_type));
            // TODO: Should this be refactored?
            if (new NPCTag(npc).action("unavailable", player, context).containsCaseInsensitive("available")) {
                // If determined available, continue on...
                // else, return a 'non-triggered' state.
            }
            else {
                return new TriggerContext(false);
            }
        }
        Denizen.getInstance().triggerRegistry.setCooldown(npc, player, triggerClass, getCooldownDuration(trigger_type));
        ListTag determination = new NPCTag(npc).action(trigger_type, player, context);
        return new TriggerContext(determination, true);
    }

    /**
     * Contains whether the trigger successfully 'triggered' and any context that was
     * available while triggering or attempting to trigger.
     */
    public static class TriggerContext {

        public TriggerContext(boolean triggered) {
            this.triggered = triggered;
        }

        public TriggerContext(ListTag determination, boolean triggered) {
            this.determination = determination;
            this.triggered = triggered;
        }

        ListTag determination;
        boolean triggered;

        public boolean hasDetermination() {
            return determination != null && !determination.isEmpty();
        }

        public ListTag getDeterminations() {
            return determination;
        }

        public boolean wasTriggered() {
            return triggered;
        }

    }
}
