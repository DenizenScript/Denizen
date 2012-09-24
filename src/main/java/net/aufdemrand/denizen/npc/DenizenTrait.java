package net.aufdemrand.denizen.npc;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.runnables.TwoItemRunnable;
import net.citizensnpcs.api.ai.event.NavigationCompleteEvent;
import net.citizensnpcs.api.event.NPCPushEvent;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.trait.Toggleable;
import net.minecraft.server.EntityLiving;


public class DenizenTrait extends Trait implements Toggleable, Listener {

	private Map<String, Boolean> triggerMap = new HashMap<String, Boolean>();
	private Denizen plugin;

	private boolean isToggled = true;
	private boolean pushable;
	private boolean pushLocation = false;
	private Location pushedLocation;

	public DenizenTrait() {
		super("denizen");
		pushable = false;
	}

	@EventHandler
	public void NPCPush (NPCPushEvent event) {
		if (event.getNPC() == npc && pushable && isToggled) {
			event.setCancelled(false);

			if (!pushLocation) {
				pushLocation = true;
				pushedLocation = npc.getBukkitEntity().getLocation().clone();

				plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new TwoItemRunnable<NPC, Location>(npc, npc.getBukkitEntity().getLocation().clone()) {
					@Override
					public void run(NPC theNPC, Location theLocation) { 
						navigateBack();
					}

				}, 20);
			}
		}
	}

	protected void navigateBack() {

		if (npc.getNavigator().isNavigating())
			pushLocation = false;

		if (pushLocation != false) {
			pushLocation = false;
			npc.getNavigator().setTarget(pushedLocation);
			pushLocation = true;
		}

	}

	@EventHandler
	public void NPCCompleteDestination (NavigationCompleteEvent event) {

		if (pushLocation && event.getNPC() == npc) {

			EntityLiving handle = ((CraftLivingEntity) npc.getBukkitEntity()).getHandle();
			handle.yaw = pushedLocation.getYaw();
			handle.pitch = pushedLocation.getPitch();
			handle.as = handle.yaw;

			pushLocation = false;
		}
		
	}

	@Override
	public void onSpawn() {
		plugin = (Denizen) Bukkit.getServer().getPluginManager().getPlugin("Denizen");
		plugin.getDenizenNPCRegistry().registerNPC(npc);

		for (String theTriggerName : plugin.getTriggerRegistry().listTriggers().keySet())
			if (!triggerMap.containsKey(theTriggerName))
				triggerMap.put(theTriggerName, plugin.getTriggerRegistry().getTrigger(theTriggerName).getEnabledByDefault());

	}

	@Override
	public void load(DataKey key) throws NPCLoadException {
		plugin = (Denizen) Bukkit.getServer().getPluginManager().getPlugin("Denizen");

		plugin.getDenizenNPCRegistry().registerNPC(npc);

		isToggled = key.getBoolean("toggled", true);
		pushable = key.getBoolean("pushable", false);
		for (String theTriggerName : plugin.getTriggerRegistry().listTriggers().keySet())
			if (key.keyExists("enable." + theTriggerName.toLowerCase() + "-trigger")) {
				triggerMap.put(theTriggerName, key.getBoolean("enable." + theTriggerName.toLowerCase() + "-trigger"));
			} else {
				triggerMap.put(theTriggerName, plugin.getTriggerRegistry().getTrigger(theTriggerName).getEnabledByDefault());
			}

	}

	@Override
	public void save(DataKey key) {

		key.setBoolean("toggled", isToggled);
		key.setBoolean("pushable", pushable);

		for (Entry<String, Boolean> theEntry : triggerMap.entrySet()) {
			key.setBoolean("enable." + theEntry.getKey().toLowerCase() + "-trigger", theEntry.getValue());
		}
	}


	@Override
	public boolean toggle() {
		isToggled = !isToggled;
		return isToggled;
	}


	public boolean isToggled() {
		return isToggled;
	}


	public boolean triggerIsEnabled(String theName) {
		if (triggerMap.containsKey(theName.toUpperCase()))
			return triggerMap.get(theName.toUpperCase());
		else return false;
	}

	public String listTriggers() {
		String theList = "";
		for (Entry<String, Boolean> theEntry : triggerMap.entrySet()) {
			if (theEntry.getValue())
				theList = theList + ChatColor.GREEN + theEntry.getKey().toLowerCase() + "-trigger" + ChatColor.GRAY + ", ";
			else
				theList = theList + ChatColor.RED + theEntry.getKey().toLowerCase() + "-trigger" + ChatColor.GRAY + ", ";
		}
		theList = theList.substring(0, theList.length() - 2);
		return theList;
	}

	public String toggleTrigger(String theTrigger) {
		if (triggerMap.containsKey(theTrigger.toUpperCase())) {
			if (triggerMap.get(theTrigger.toUpperCase())) {
				triggerMap.put(theTrigger.toUpperCase(), false);
				return theTrigger + "-trigger now disabled.";
			} else {
				triggerMap.put(theTrigger.toUpperCase(), true);
				return theTrigger + "-trigger now enabled.";
			}
		} else {
			return "Trigger not found!";
		}
	}

	public boolean isPushable() {
		return pushable;
	}

	public boolean togglePushable() {
		pushable = !pushable;
		return pushable;
	}

	public void setPushable(boolean pushable) {
		this.pushable = pushable;
	}

}
