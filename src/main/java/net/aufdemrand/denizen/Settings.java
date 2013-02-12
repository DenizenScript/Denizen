package net.aufdemrand.denizen;
import net.aufdemrand.denizen.utilities.DenizenAPI;

public class Settings {

	/*
	
	# Default time in seconds of cooldown set to Denizens when a trigger is triggered.
	# Not all triggers may use this, it is optional!
	#
    # Format: default_triggername_cooldown_in_seconds: #.#
    # For example: default_chat_cooldown_in_seconds: 2.0
    
    default_click_cooldown_in_seconds: 2.0
    default_location_cooldown_in_seconds: 15.0
    default_proximity_cooldown_in_seconds: 1.0
    default_damage_cooldown_in_seconds: 0.5
    default_chat_cooldown_in_seconds: 2.0
	 
	 */
	
	public static long DefaultCooldown(String triggerName) {
		return (long) (DenizenAPI.getCurrentInstance().getConfig()
                .getDouble("default_" + triggerName.toLowerCase().replace(" ", "_")
                        + "_cooldown_in_seconds", 5) * 1000);
	}
	
	/* 
	 
	# If the damage trigger is disabled on an NPC, and the NPC is not vulnerable,
	# both the right and left clicks can be used to activate the click trigger.
	# Default setting in versions 0.8+ is 'false', but it's worth noting that in p
	# previous versions this was by default 'true'.
	# Select true if you would like invulnerable NPCs to use both right and left clicks
	# to activate the click trigger.

	disabled_damage_trigger_instead_triggers_click: false
	
	 */
	
	public static boolean DisabledDamageTriggerInsteadTriggersClick() {
		return DenizenAPI.getCurrentInstance().getConfig()
                .getBoolean("disabled_damage_trigger_instead_triggers_click", false);
	}
	 
	/*

	# Interact delay sets the pace of the 'Player Queue' and 'Denizen Queue' 
	# execution, which is where commands triggered by scripts are stored.
	# Recommend 5-15.

	interact_delay_in_ticks: 7

	 */

	public static int InteractDelayInTicks() {
		int delay = DenizenAPI.getCurrentInstance().getConfig()
                .getInt("interact_delay_in_ticks", 10);
		// Check for users setting delay to 0, which will in turn lock up the server
		if (delay < 1) delay = 1;
		return delay;
	}

	/*
	  
	# Default engage timeout. When NPCs are set to ENGAGE, this is the default timeout
    # that they will auto-DISENGAGE if not otherwise specified. (Default, 150 seconds)

	engage_timeout_in_seconds: 150
	  
	 */
	
	public static int EngageTimeoutInSeconds() {
		return DenizenAPI.getCurrentInstance().getConfig()
                .getInt("engage_timeout_in_seconds", 150);
	}

	/*

	# This set of nodes defines ranges for different types of
	# interact-script triggers. Not all triggers use a range,
	# as it may not be applicable to the trigger.
	#
    # Format: triggername_trigger_range_in_blocks: #.#
    # For example: location_trigger_range_in_blocks: 1.0
  
	proximity_trigger_range_in_blocks: 10
	location_trigger_range_in_blocks: 2
	chat_trigger_range_in_blocks: 2

	 */

	public static int TriggerRangeInBlocks(String triggerName) {
		return DenizenAPI.getCurrentInstance().getConfig()
                .getInt(triggerName.toLowerCase().replace(" ", "_") + "_trigger_range_in_blocks", -1);
	}


    /*

    # In certain circumstances the Denizen plugin can force a player
    # to speak locally. Following is the format of the chatting
    # that will occur.

    player_chat_no_target: <player.name> says '<chat.message>'
    player_chat_to_target: <player.name> says to <chat.target>, '<chat.message>'
    player_chat_to_targets: <player.name> says to <chat.targets>, '<chat.message>'

     */



    public static boolean CheckLineOfSightWhenChatting() {
        return DenizenAPI.getCurrentInstance().getConfig()
                .getBoolean("check_line_of_sight_when_chatting", true);
    }

    public static boolean ChatOnlyWhenLookingAtNPC() {
        return DenizenAPI.getCurrentInstance().getConfig()
                .getBoolean("chat_with_npc_only_when_looking_at_npc", true);
    }

    public static boolean ChatGloballyIfFailedChatTriggers() {
        return DenizenAPI.getCurrentInstance().getConfig()
                .getBoolean("chat_globally_if_failed_chat_triggers", false);
    }

    public static boolean ChatGloballyIfNoChatTriggers() {
        return DenizenAPI.getCurrentInstance().getConfig()
                .getBoolean("chat_globally_if_no_chat_triggers", false);
    }

    public static boolean ChatGloballyIfNotInteractable() {
        return DenizenAPI.getCurrentInstance().getConfig()
                .getBoolean("chat_globally_if_not_interactable", false);
    }

    public static String PlayerChatToNpcFormat() {
        return DenizenAPI.getCurrentInstance().getConfig()
                .getString("player_chat_to_npc_format", "You -> <npc.nickname>: <text>");
    }

    public static String PlayerChatToNpcToBystandersFormat() {
        return DenizenAPI.getCurrentInstance().getConfig()
                .getString("player_chat_to_npc_to_bystanders_format", "<player.name> -> <npc.nickname>: <text>");
    }

    public static int PlayerChatToNpcBystandersRange() {
        return DenizenAPI.getCurrentInstance().getConfig()
                .getInt("player_chat_to_npc_to_bystanders_range", 4);
    }

}
