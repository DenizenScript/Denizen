package net.aufdemrand.denizen;

import net.aufdemrand.denizen.utilities.DenizenAPI;

public class Settings {

	/*
	
	# Whether Denizen should display debug in the console
     
	 */
	
	public static boolean ShowDebug() {
		return DenizenAPI.getCurrentInstance().getConfig()
                .getBoolean("Show Debug", true);
	}
	
	/*
	
	# Whether the Health trait should be enabled by default
     
	 */
	
	public static boolean HealthTraitEnabledByDefault() {
		return DenizenAPI.getCurrentInstance().getConfig()
                .getBoolean("Health Trait Enabled by Default", false);
	}
	
	/*
	
	# Whether NPCs with the Health trait should respawn after being killed
    
	 */
	
	public static boolean HealthTraitRespawnEnabled() {
		return DenizenAPI.getCurrentInstance().getConfig()
               .getBoolean("Health Trait Respawn Enabled", true);
	}
	
	/*
	
	# Whether NPCs with the Health trait should respawn after being killed
    
	 */
	
	public static String HealthTraitRespawnDelay() {
		return DenizenAPI.getCurrentInstance().getConfig()
               .getString("Health Trait Respawn Delay", "10s");
	}
	
	/*
	
	# Default time in seconds of cooldown set to Denizens when a trigger is triggered.
	# Not all triggers may use this, it is optional!
	#
    # Format: [Name] Trigger Default Cooldown in Seconds: #.#
    # For example: Click Trigger Default Cooldown in Seconds: 2.0
     
	 */
	
	public static long TriggerDefaultCooldown(String triggerName) {
		return (long) (DenizenAPI.getCurrentInstance().getConfig()
                .getDouble(triggerName.substring(0, 1).toUpperCase() +
                			triggerName.substring(1).toLowerCase() +
                			" Trigger Default Cooldown in Seconds", 5));
	}
	
	/* 
	 
	# If the damage trigger is disabled on an NPC, and the NPC is not vulnerable,
	# both the right and left clicks can be used to activate the click trigger.
	# Default setting in versions 0.8+ is 'false', but it's worth noting that in p
	# previous versions this was by default 'true'.
	# Select true if you would like invulnerable NPCs to use both right and left clicks
	# to activate the click trigger.

	Click Trigger Allows Left Click: false
	
	 */
	
	public static boolean ClickTriggerAllowsLeftClick() {
		return DenizenAPI.getCurrentInstance().getConfig()
                .getBoolean("Click Trigger Allows Left Click", false);
	}
	 
	/*

	# Interact delay sets the pace of the 'Player Queue' and 'Denizen Queue' 
	# execution, which is where commands triggered by scripts are stored.
	# Recommend 5-15.

	Interact Delay in Ticks: 7

	 */

	public static int InteractDelayInTicks() {
		int delay = DenizenAPI.getCurrentInstance().getConfig()
                .getInt("Interact Delay in Ticks", 10);
		// Check for users setting delay to 0, which will in turn lock up the server
		if (delay < 1) delay = 1;
		return delay;
	}

	/*
	  
	# Default engage timeout. When NPCs are set to ENGAGE, this is the default timeout
    # that they will auto-DISENGAGE if not otherwise specified. (Default, 150 seconds)

	Engage Timeout in Seconds: 150
	  
	 */
	
	public static int EngageTimeoutInSeconds() {
		return DenizenAPI.getCurrentInstance().getConfig()
                .getInt("Engage Timeout in Seconds", 150);
	}

	/*

	# This set of nodes defines ranges for different types of
	# interact-script triggers. Not all triggers use a range,
	# as it may not be applicable to the trigger.
	#
    # Format: [Name] Trigger Default Range in Blocks: #.#
    # For example: Location Trigger Default Range in Blocks: 1.0
  
  	Chat Trigger Default Range in Blocks: 3
	Proximity Trigger Default Range in Blocks: 10
	Location Trigger Default Range in Blocks: 2
	

	 */

	public static int TriggerDefaultRangeInBlocks(String triggerName) {
		return DenizenAPI.getCurrentInstance().getConfig()
                .getInt(triggerName.substring(0, 1).toUpperCase() +
                		triggerName.substring(1).toLowerCase() +
                		" Trigger Default Range in Blocks", -1);
	}


    /*

    # In certain circumstances the Denizen plugin can force a player
    # to speak locally. Following is the format of the chatting
    # that will occur.

    Chat No Target: <player.name> says '<chat.message>'
    Chat to Target: <player.name> says to <chat.target>, '<chat.message>'
    Chat to Targets: <player.name> says to <chat.targets>, '<chat.message>'

     */

    public static String ChatToNpcFormat() {
        return DenizenAPI.getCurrentInstance().getConfig()
                .getString("Chat to NPC Format", "You -> <npc.name.nickname>: <text>");
    }

    public static String ChatToNpcBystandersFormat() {
        return DenizenAPI.getCurrentInstance().getConfig()
                .getString("Chat to NPC Bystanders Format", "<player.name> -> <npc.name.nickname>: <text>");
    }

    public static int ChatToNpcBystandersRange() {
        return DenizenAPI.getCurrentInstance().getConfig()
                .getInt("Chat to NPC Bystanders Range", 4);
    }

    public static boolean ChatOnlyWhenHavingLineOfSightToNPC() {
        return DenizenAPI.getCurrentInstance().getConfig()
                .getBoolean("Chat Only When Having Line of Sight to NPC", true);
    }

    public static boolean ChatOnlyWhenLookingAtNPC() {
        return DenizenAPI.getCurrentInstance().getConfig()
                .getBoolean("Chat Only When Looking at NPC", true);
    }

    public static boolean ChatGloballyIfFailedChatTriggers() {
        return DenizenAPI.getCurrentInstance().getConfig()
                .getBoolean("Chat Globally If Failed Chat Triggers", true);
    }

    public static boolean ChatGloballyIfNoChatTriggers() {
        return DenizenAPI.getCurrentInstance().getConfig()
                .getBoolean("Chat Globally If No Chat Triggers", true);
    }

    public static boolean ChatGloballyIfNotInteractable() {
        return DenizenAPI.getCurrentInstance().getConfig()
                .getBoolean("Chat Globally If Not Interactable", true);
    }
    
    public static boolean LoadScriptsInSubfolders() {
        return DenizenAPI.getCurrentInstance().getConfig()
                .getBoolean("Load Scripts in Subfolders", true);
    }

}
