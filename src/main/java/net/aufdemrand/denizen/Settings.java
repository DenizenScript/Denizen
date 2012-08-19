package net.aufdemrand.denizen;

public class Settings {

	private Denizen plugin;

	public Settings(Denizen denizen) {
		plugin = denizen;
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
    default_click_cooldown_in_seconds: 2.0
    default_location_cooldown_in_seconds: 30.0
    default_proximity_cooldown_in_seconds: 15.0
    default_death_cooldown_in_seconds: 0.0
    default_damage_cooldown_in_seconds: 0.5
	default_chat_cooldown_in_seconds: 2.0
	 
	 */
	
	public long DefaultClickCooldown() {
		return (long) (plugin.getConfig().getDouble("default_click_cooldown_in_seconds", 2) * 1000);
	}

	public long DefaultLocationCooldown() {
		return (long) (plugin.getConfig().getDouble("default_location_cooldown_in_seconds", 30.00) * 1000);
	}

	public long DefaultProximityCooldown() {
		return (long) (plugin.getConfig().getDouble("default_proximity_cooldown_in_seconds", 15.0) * 1000);
	}

	public long DefaultDeathCooldown() {
		return (long) (plugin.getConfig().getDouble("default_death_cooldown_in_seconds", 0.0) * 1000);
	}

	public long DefaultDamageCooldown() {
		return (long) (plugin.getConfig().getDouble("default_damage_cooldown_in_seconds", 0.5) * 1000);
	}
	
	public long DefaultChatCooldown() {
		return (long) (plugin.getConfig().getDouble("default_chat_cooldown_in_seconds", 2.0) * 1000);
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

	# How long should the maximum line length be for multi-line text?
	multi_line_text_maximum_length: 55

	 */

	public int MultiLineTextMaximumLength() {
		return plugin.getConfig().getInt("multi_line_text_maximum_length", 55);
	}

	/*

	# By default, players can talk to Denizens even if there are no
	# chat triggers in the script. I believe this is best for RPG-type
	# behavior. Change this to true if you would like chat near a 
	# Denizen to be global if there are no chat triggers for the
	# script being triggered.
	chat_globally_if_no_chat_triggers: false

	# Furthermore, if there are chat triggers available, and the Player
	# doesn't match one, should the player still talk and recieve a
	# response from the Denizen, or should he just chat globally and
	# ignore the Denizen. Change this to true if you would like
	# chat near a Denizen to be global if there are available chat
	# triggers, but the player failed to match one.
	chat_globally_if_failed_chat_triggers: false
	
	# Also.. if the Denizen happens to be un-interactable 
	# (Engaged, or not cooled-down, for instance), by default
	# chat is still targeted to the NPC. You can turn this
	# behavior off by changing this config node to true.
	chat_globally_if_not_interactable: false
	

	 */

	public boolean ChatGloballyIfNoChatTriggers() {
		return plugin.getConfig().getBoolean("chat_globally_if_no_chat_triggers", false);
	}

	public boolean ChatGloballyIfFailedChatTriggers() {
		return plugin.getConfig().getBoolean("chat_globally_if_failed_chat_triggers", false);
	}

	public boolean ChatGloballyIfNotInteractable() {
		return plugin.getConfig().getBoolean("chat_globally_if_not_interactable", false);
	}
	
	/*

	# Should players around the player interacting with the Denizen
	# hear the conversation?
	bystanders_hear_player_to_npc_chat: true
	bystanders_hear_npc_to_player_chat: true

	 */

	public boolean BystandersHearNpcToPlayerChat() {
		return plugin.getConfig().getBoolean("bystanders_hear_npc_to_player_chat", true);
	}

	public boolean BystandersHearPlayerToNpcChat() {
		return plugin.getConfig().getBoolean("bystanders_hear_player_to_npc_chat", true);
	}

	/*

	# This set of nodes defines ranges for different types of
	# chat/interaction/etc.
	player_to_npc_chat_range_in_blocks: 3
	npc_to_player_chat_range_in_blocks: 7
	npc_emote_range_in_blocks: 7
	player_to_npc_shout_range_in_blocks: 15
	npc_to_player_shout_range_in_blocks: 15
	player_to_npc_whisper_range_in_blocks: 3
	npc_to_player_whisper_range_in_blocks: 3
	
	proximity_trigger_range_in_blocks: 3
	location_trigger_range_in_blocks: 1
	 */

	public int ProximityTriggerRangeInBlocks() {
		return plugin.getConfig().getInt("proximity_trigger_range_in_blocks", 3);
	}

	public int LocationTriggerRangeInBlocks() {
		return plugin.getConfig().getInt("location_trigger_range_in_blocks", 3);
	}

	public int PlayerToNpcChatRangeInBlocks() {
		return plugin.getConfig().getInt("player_to_npc_chat_range_in_blocks", 3);
	}

	public int NpcToPlayerChatRangeInBlocks() {
		return plugin.getConfig().getInt("npc_to_player_chat_range_in_blocks", 7);
	}

	public int NpcEmoteRangeInBlocks() {
		return plugin.getConfig().getInt("npc_emote_range_in_blocks", 7);
	}

	public int PlayerToNpcShoutRangeInBlocks() {
		return plugin.getConfig().getInt("player_to_npc_shout_range_in_blocks", 15);
	}

	public int NpcToPlayerShoutRangeInBlocks() {
		return plugin.getConfig().getInt("npc_to_player_shout_range_in_blocks", 15);
	}

	public int PlayerToNpcWhisperRangeInBlocks() {
		return plugin.getConfig().getInt("player_to_npc_whisper_range_in_blocks", 3);
	}

	public int NpcToPlayerWhisperRangeInBlocks() {
		return plugin.getConfig().getInt("npc_to_player_whisper_range_in_blocks", 3);
	}

	/*

	# These nodes define how chatting, shouting, etc. is displayed
	# to players in the game. <PLAYER>, <NPC>, and <TEXT> are all
	# replaceable variables. Remember to use " around your string
	# if your text contains a single apostrophe.
	player_chat_to_npc: "You say to <NPC>, '<TEXT>'"
	player_chat_to_npc_bystander: "<PLAYER> says to <NPC>, '<TEXT>'"
	npc_chat_to_bystanders: "<NPC> says, '<TEXT>'"
	npc_chat_to_player: "<NPC> says to you, '<TEXT>'"
	npc_chat_to_player_bystander: "<NPC> says to <PLAYER>, '<TEXT>'"

	 */

	public String PlayerChatToNpc() {
		return plugin.getConfig().getString("player_chat_to_npc", "You say to <NPC>, '<TEXT>'");
	}

	public String PlayerChatToNpcBystander() {
		return plugin.getConfig().getString("player_chat_to_npc_bystander", "<PLAYER> says to <NPC>, '<TEXT>'");
	}

	public String NpcChatToBystanders() {
		return plugin.getConfig().getString("npc_chat_to_bystanders", "<NPC> says, '<TEXT>'");
	}

	public String NpcChatToPlayer() {
		return plugin.getConfig().getString("npc_chat_to_player", "<NPC> says to you, '<TEXT>'");
	}
	
	public String NpcChatToPlayerBystander() {
		return plugin.getConfig().getString("npc_chat_to_player_bystander", "<NPC> says to <PLAYER>, '<TEXT>'");
	}

	/*

	player_whisper_to_npc: "You whisper to <NPC>, '<TEXT>'"
	player_whisper_to_npc_bystander: "<PLAYER> whispers something to <NPC>."
	npc_whisper_to_bystanders: "<NPC> whispers, '<TEXT>'"
	npc_whisper_to_player: "<NPC> whispers to you, '<TEXT>'"
	npc_whisper_to_player_bystander: "<NPC> whispers something to <PLAYER>."
	
	*/
	
	public String PlayerWhisperToNpc() {
		return plugin.getConfig().getString("player_whisper_to_npc", "You whisper to <NPC>, '<TEXT>'");
	}

	public String PlayerWhisperToNpcBystander() {
		
		return plugin.getConfig().getString("player_whisper_to_npc_bystander", "<PLAYER> whispers someting to <NPC>.");
	}

	public String NpcWhisperToBystanders() {
		return plugin.getConfig().getString("npc_whisper_to_bystanders", "<NPC> whispers, '<TEXT>'");
	}

	public String NpcWhisperToPlayer() {
		return plugin.getConfig().getString("npc_whisper_to_player", "<NPC> whispers to you, '<TEXT>'");
	}
	
	public String NpcWhisperToPlayerBystander() {
		return plugin.getConfig().getString("npc_whisper_to_player_bystander", "<NPC> whispers to <PLAYER>, '<TEXT>'");
	}
	
	public String NpcHintPrefix() {
		return plugin.getConfig().getString("npc_hint_prefix", "[HINT] You can say: ");
	}
	
	/*

	player_shout_to_bystanders: "You shout, '<TEXT>'"
	player_shout_to_bystanders_bystander: "<PLAYER> shouts, '<TEXT>'"
	npc_shout_to_bystanders: "<NPC> shouts, '<TEXT>'"
	npc_shout_to_player: "<NPC> shouts at you, '<TEXT>'"
	npc_shout_to_player_bystander: "<NPC> shouts at <PLAYER>, '<TEXT>'"
	
	*/
	
	public String PlayerShoutToNpc() {
		return plugin.getConfig().getString("player_shout_to_bystanders", "You shout, '<TEXT>'");
	}

	public String PlayerShoutToNpcBystander() {
		return plugin.getConfig().getString("player_shout_to_bystanders_bystander", "<PLAYER> shouts, '<TEXT>'");
	}

	public String NpcShoutToBystanders() {
		return plugin.getConfig().getString("npc_shout_to_bystanders", "<NPC> shouts, '<TEXT>'");
	}

	public String NpcShoutToPlayer() {
		return plugin.getConfig().getString("npc_shout_to_player", "<NPC> shouts to you, '<TEXT>'");
	}
	
	public String NpcShoutToPlayerBystander() {
		return plugin.getConfig().getString("npc_shout_to_player_bystander", "<NPC> shouts to <PLAYER>, '<TEXT>'");
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
		return plugin.getConfig().getString("default_denizen_unavailable_text", "I'm busy at the moment, can't you see?");
	}
	
	public String DefaultNoChatTriggerText() {
		return plugin.getConfig().getString("default_no_chat_trigger_text", "Sorry, I don't understand.");
	}

	public String DefaultNoClickTriggerText() {
		return plugin.getConfig().getString("default_no_click_trigger_text", "I have nothing else for you right now.");
	}
	
	public String DefaultNoDamageTriggerText() {
		return plugin.getConfig().getString("default_no_damage_trigger_text", "Ouch! That hurts!");
	}


}
