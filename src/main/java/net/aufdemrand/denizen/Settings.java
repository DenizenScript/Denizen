package net.aufdemrand.denizen;

import org.bukkit.Bukkit;

public class Settings {

	private Denizen plugin;

	public Settings() {
		plugin = (Denizen) Bukkit.getPluginManager().getPlugin("Denizen");
	}

	/* 
	 
	# OPs can sneak and right click a NPC to see information about the NPC. If disabled,
	# the command '/denizen info' can still be used to obtain the information.
	right_click_and_sneak_info_mode_enabled: true
	
	 */
	
	public boolean RightClickAndSneakInfoModeEnabled() {
		return plugin.getConfig().getBoolean("right_click_and_sneak_info_mode_enabled", true);
	}
	
	/*
	
	# Default time in seconds of cooldown set to Denizens when a trigger is triggered.
	# Not all triggers may use this, it is optional!
	#
    # Format: default_triggername_cooldown_in_seconds: #.#
    # For example: default_chat_cooldown_in_seconds: 2.0
    
    default_click_cooldown_in_seconds: 2.0
    default_location_cooldown_in_seconds: 30.0
    default_proximity_cooldown_in_seconds: 15.0
    default_death_cooldown_in_seconds: 0.0
    default_damage_cooldown_in_seconds: 0.5
    default_chat_cooldown_in_seconds: 2.0
	 
	 */
	
	public long DefaultCooldown(String triggerName) {
		return (long) (plugin.getConfig().getDouble("default_" + triggerName.toLowerCase() + "_cooldown_in_seconds", 5) * 1000);
	}
	
	/* 
	 
	# Denizen by default uses both right and left clicks to activate the click trigger
	# if the damage trigger (left click) is disabled. Select false if you would prefer
	# to have click triggers activate with the right mouse button only.
	
	disabled_damage_trigger_instead_triggers_click: true
	
	 */
	
	public boolean DisabledDamageTriggerInsteadTriggersClick() {
		return plugin.getConfig().getBoolean("disabled_damage_trigger_instead_triggers_click", true);
	}
	 
	/*

	# Interact delay sets the pace of the 'Player Queue' and 'Denizen Queue' 
	# execution, which is where commands triggered by scripts are stored.
	# Recommend 10-20.
	interact_delay_in_ticks: 10

	 */

	public int InteractDelayInTicks() {
		int delay = plugin.getConfig().getInt("interact_delay_in_ticks", 10);

		/* Check for users setting delay to 0, which will in turn lock up the server. */
		if (delay < 1) delay = 1;
		return delay;
	}

	/*
	  
	# Default engage timeout. When NPCs are set to ENGAGE, this is the default timeout
    # that they DISENGAGE if not specified. (Default, 5 minutes)
	engage_timeout_in_seconds: 150
	  
	 */
	
	public int EngageTimeoutInSeconds() {
		return plugin.getConfig().getInt("engage_timeout_in_seconds", 150);
	}


	/*

	# This set of nodes defines ranges for different types of
	# interaction/etc.
	# Not all triggers may use this, it is optional!
	#
    # Format: triggername_trigger_range_in_blocks: #.#
    # For example: location_trigger_range_in_blocks: 1.0
  
	proximity_trigger_range_in_blocks: 3
	location_trigger_range_in_blocks: 1
	 */

	public int TriggerRangeInBlocks(String triggerName) {
		return plugin.getConfig().getInt(triggerName.toLowerCase() + "_trigger_range_in_blocks", 1);
	}

	/*

	# Default texts that Denizens use when interacting. These
	# can also be set per-denizen, which will override the
	# defaults set below.
	default_denizen_unavailable_text: "I'm busy at the moment, can't you see?"
	default_no_click_trigger_text: "I have nothing else for you right now."
	default_no_damage_trigger_text: "Ouch! That hurts!"
	default_no_chat_trigger_text: "Sorry, I don't understand."

	 */

	public String DefaultDenizenUnavailableText() {
		return plugin.getConfig().getString("default_denizen_unavailable_text", "Hmm?");
	}
	
	public String DefaultNoTriggerText(String triggerName) {
		return plugin.getConfig().getString("default_no_" + triggerName.toLowerCase() + "_trigger_text", "Sorry, I don't understand.");
	}
	
	/* 
	 
	# Default Hint command prefix. This can be changed globally and will affect the output of all HINT commands.
	
	npc_hint-prefix: "[HINT] You can say:"

     */
	
	public String NpcHintPrefix() {
	 	return plugin.getConfig().getString("npc_hint_prefix", "[HINT] You can say: ");
	 }

}
