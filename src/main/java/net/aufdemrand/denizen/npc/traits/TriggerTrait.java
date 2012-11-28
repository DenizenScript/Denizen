package net.aufdemrand.denizen.npc.traits;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.scripts.commands.core.EngageCommand;
import net.aufdemrand.denizen.scripts.triggers.AbstractTrigger;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.command.exception.CommandException;
import net.citizensnpcs.util.Messages;
import net.citizensnpcs.util.Paginator;


public class TriggerTrait extends Trait implements Listener {

    private Map<String, Boolean> enabled = new HashMap<String, Boolean>();
    private Map<String, Integer> localCooldown = new HashMap<String, Integer>();
    private Denizen denizen;

    public TriggerTrait() {
        super("triggers");
        if (denizen == null) denizen = (Denizen) Bukkit.getServer().getPluginManager().getPlugin("Denizen");
        // Populate triggers
        for (String triggerName : denizen.getTriggerRegistry().list().keySet()) 
            enabled.put(triggerName, denizen.getTriggerRegistry().get(triggerName).getOptions().ENABLED_BY_DEFAULT);
    }

    // Loading/Saving

    @Override
    public void load(DataKey key) throws NPCLoadException {
        // Load triggers from config
        for (String triggerName : denizen.getTriggerRegistry().list().keySet()) {
            enabled.put(triggerName, key.getBoolean(triggerName.toLowerCase() + "-trigger" + ".enabled", false));
            if (key.keyExists(triggerName.toLowerCase() + "-trigger" + ".cooldown")) 
                localCooldown.put(triggerName, key.getInt(triggerName.toLowerCase() + "-trigger" + ".cooldown"));
        }
    }

    @Override
    public void save(DataKey key) {
        for (Entry<String, Boolean> entry : enabled.entrySet())
            key.setBoolean(entry.getKey().toLowerCase() + "-trigger" + ".enabled", entry.getValue());
        for (Entry<String, Integer> entry : localCooldown.entrySet()) 
            key.setInt(entry.getKey().toLowerCase() + "-trigger" + ".cooldown", entry.getValue());
    }

    // Setting/Adjusting/Describing

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

    public boolean isEnabled(String triggerName) {
        if (enabled.containsKey(triggerName.toUpperCase()))
            return enabled.get(triggerName.toUpperCase());
        else return false;
    }

    public void setLocalCooldown(String triggerName, int value) {
        if (localCooldown.containsKey(triggerName.toUpperCase()))
            localCooldown.put(triggerName, value);
    }

    public int getCooldownDuration(String triggerName) {
        if (localCooldown.containsKey(triggerName.toUpperCase()))
            return localCooldown.get(triggerName.toUpperCase());
        else return denizen.getTriggerRegistry().get(triggerName).getOptions().DEFAULT_COOLDOWN;
    }

    public void describe(CommandSender sender, int page) throws CommandException {
        Paginator paginator = new Paginator().header("Triggers");
        paginator.addLine("<e>Key: <a>Name  <b>Status  <c>Cooldown");
        int i = 0;
        for (Entry<String, Boolean> entry : enabled.entrySet()) {
            String line = "<e>" + i + "<a>  " + entry.getKey() +  "<b>  " + (entry.getValue() ? "Enabled" : "Disabled") + "<c>  " + getCooldownDuration(entry.getKey());
            paginator.addLine(line);
            i++;
        }
        if (!paginator.sendPage(sender, page))
            throw new CommandException(Messages.COMMAND_PAGE_MISSING);
    }

    public boolean trigger(AbstractTrigger triggerClass, Player player) {
        // Check cool down
        if (!denizen.getTriggerRegistry().checkCooldown(npc, triggerClass)
                || denizen.getCommandRegistry().get(EngageCommand.class).getEngaged(npc)) {
            // On Unavailable Action
            denizen.getNPCRegistry().getDenizen(npc).action("unavailable", player);
            return false;
        }
        // Set cool down, On [TriggerName] Action
        denizen.getTriggerRegistry().setCooldown(npc, triggerClass.getClass(), getCooldownDuration(triggerClass.getName()));
        denizen.getNPCRegistry().getDenizen(npc).action(triggerClass.getName(), player);
        return true;
    }

}
