package net.aufdemrand.denizen;

import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.objects.Duration;

public class Settings {

    /*
    
    # Whether Denizen should display debug in the console
     
    */
    
    public static boolean ShowDebug() {
        return DenizenAPI.getCurrentInstance().getConfig()
                .getBoolean("Debug.Show", false);
    }

    public static int ConsoleWidth() {
        return DenizenAPI.getCurrentInstance().getConfig()
                .getInt("Debug.Console width", 60);
    }
    
    /*

    # Sets the default speed between execution of commands in queues

    */

    public static String ScriptQueueSpeed() {
        String delay = DenizenAPI.getCurrentInstance().getConfig()
                .getString("Scripts.Queue speed", "0.5s");
        
        // Check for users setting delay to 0, which will in turn lock up the server
        try { if (Double.valueOf(delay) < 1) delay = "1t"; }
        catch (Exception e) { delay = "0.5s"; }
        
        return delay;
    }
    
    /*

    # Whether scripts in subfolders of the scripts folder should be loaded
    
    */
    
    public static boolean LoadScriptsInSubfolders() {
        return DenizenAPI.getCurrentInstance().getConfig()
                .getBoolean("Scripts.Load scripts in subfolders", true);
    }
    
    /*
    
    # Whether the Health trait should be enabled by default
     
    */
    
    public static boolean HealthTraitEnabledByDefault() {
        return DenizenAPI.getCurrentInstance().getConfig()
                .getBoolean("Traits.Health.Enabled", false);
    }
    
    /*
    
    # Whether NPCs with the Health trait should respawn after being killed
    
    */
    
    public static boolean HealthTraitRespawnEnabled() {
        return DenizenAPI.getCurrentInstance().getConfig()
               .getBoolean("Traits.Health.Respawn.Enabled", true);
    }
    
    /*
    
    # Whether NPCs with the Health trait should have a death animation
    
    */
    
    public static boolean HealthTraitAnimatedDeathEnabled() {
        return DenizenAPI.getCurrentInstance().getConfig()
               .getBoolean("Traits.Health.Animated death.Enabled", true);
    }
    
    /*
    
    # How long it should take for NPCs with the Health trait to respawn
    
    */
    
    public static String HealthTraitRespawnDelay() {
        return DenizenAPI.getCurrentInstance().getConfig()
               .getString("Traits.Health.Respawn.Delay", "10s");
    }
    
    /*
    
    # Whether a certain trigger is enabled by default or not
     
    */
    
    public static boolean TriggerEnabled(String triggerName) {
        return DenizenAPI.getCurrentInstance().getConfig()
                .getBoolean("Triggers." + String.valueOf(triggerName.charAt(0)).toUpperCase() + triggerName.substring(1).toLowerCase() + ".Enabled", true);
    }
    
    /*
    
    # Default duration of cooldown set to Denizens for when a trigger is
    # triggered. Not all triggers may use this, it is optional!
     
    */
    
    public static double TriggerDefaultCooldown(String triggerName) {
        return Duration.valueOf(DenizenAPI.getCurrentInstance().getConfig()
                .getString("Triggers." + String.valueOf(triggerName.charAt(0)).toUpperCase() + triggerName.substring(1).toLowerCase() + ".Cooldown", "5s")).getSeconds();
    }
    
    /*

    # This set of nodes defines ranges for different types of
    # interact-script triggers. Not all triggers use a range,
    # as it may not be applicable to the trigger.

    */

    public static double TriggerDefaultRange(String triggerName) {
        return DenizenAPI.getCurrentInstance().getConfig()
                .getDouble("Triggers." + String.valueOf(triggerName.charAt(0)).toUpperCase() + triggerName.substring(1).toLowerCase() + ".Range", -1);
    }

    /*

    # This set of nodes defines cooldown-types for different types of
    # interact-script triggers.

    */

    public static String TriggerDefaultCooldownType(String triggerName) {
        return DenizenAPI.getCurrentInstance().getConfig()
                .getString("Triggers." + String.valueOf(triggerName.charAt(0)).toUpperCase() + triggerName.substring(1).toLowerCase() + ".Cooldown Type", "Player");
    }

    /* 
     
    # If the damage trigger is disabled on an NPC, and the NPC is not
    # vulnerable, both the right and left clicks can be used to activate
    # the click trigger. Default setting in versions 0.8+ is 'false',
    # but it's worth noting that in previous versions this was by
    # default 'true'.
    #
    # Select true if you would like invulnerable NPCs to use both right
    # and left clicks to activate the click trigger.
    
    */
    
    public static boolean ClickTriggerAllowsLeftClick() {
        return DenizenAPI.getCurrentInstance().getConfig()
                .getBoolean("Triggers.Click.Allows left click", false);
    }

    /*
      
    # Default engage timeout. When NPCs are set to ENGAGE, this is
    # the default timeout that they will auto-DISENGAGE if not otherwise
    # specified. (Default, 150 seconds)
      
    */
    
    public static String EngageTimeoutInSeconds() {
        return DenizenAPI.getCurrentInstance().getConfig()
                .getString("Commands.Engage.Timeout", "150s");
    }

    /*

    # Whether the Chat Trigger should use an asynchronous Bukkit
    # event or not

    */

    public static boolean ChatAsynchronous() {
        return DenizenAPI.getCurrentInstance().getConfig()
                .getBoolean("Triggers.Chat.Use asynchronous event", true);
    }
    
    /*

    # The formats in which Chat Trigger input from players appears to
    # themselves and to players who can overhear them 

    */

    public static String ChatToNpcFormat() {
        return DenizenAPI.getCurrentInstance().getConfig()
                .getString("Triggers.Chat.Formats.Player to NPC", "You -> <npc.name.nickname>: <text>");
    }

    public static String ChatToNpcOverheardFormat() {
        return DenizenAPI.getCurrentInstance().getConfig()
                .getString("Triggers.Chat.Formats.Player to NPC overheard", "<player.name> -> <npc.name.nickname>: <text>");
    }
    
    /*

    # The distance from which a player chatting to an NPC can be overheard
    # by other players

    */

    public static double ChatToNpcOverhearingRange() {
        return DenizenAPI.getCurrentInstance().getConfig()
                .getDouble("Triggers.Chat.Overhearing range", 4);
    }
    
    /*

    # Prerequisites for triggering a Chat Trigger

    */

    // Currently broken because of Bukkit changes
    
    //public static boolean ChatMustSeeNPC() {
    //    return DenizenAPI.getCurrentInstance().getConfig()
    //            .getBoolean("Triggers.Chat.Prerequisites.Must be able to see NPC", true);
    //}

    public static boolean ChatMustLookAtNPC() {
        return DenizenAPI.getCurrentInstance().getConfig()
                .getBoolean("Triggers.Chat.Prerequisites.Must be looking in direction of NPC", true);
    }

    /*

    # Circumstances under which a player's Chat Trigger input should
    # appear in the global chat

    */
    
    public static boolean ChatGloballyIfFailedChatTriggers() {
        return DenizenAPI.getCurrentInstance().getConfig()
                .getBoolean("Triggers.Chat.Appears globally.If triggers failed", false);
    }

    public static boolean ChatGloballyIfNoChatTriggers() {
        return DenizenAPI.getCurrentInstance().getConfig()
                .getBoolean("Triggers.Chat.Appears globally.If triggers missing", true);
    }

    public static boolean ChatGloballyIfUninteractable() {
        return DenizenAPI.getCurrentInstance().getConfig()
                .getBoolean("Triggers.Chat.Appears globally.If NPC uninteractable", true);
    }

    
    /////////////////////
    //   WORLD SCRIPTS
    /////////////////
    
    /*

    # Whether the "on player chats" world event should use an
    # asynchronous Bukkit event or not

    */
    
    public static boolean WorldScriptChatEventAsynchronous() {
        return DenizenAPI.getCurrentInstance().getConfig()
                .getBoolean("Scripts.World.Events.On player chats.Use asynchronous event", true);
    }
    
    /*

    # The frequency with which the "on time changes" world script
    # event will be checked

    */

    public static Duration WorldScriptTimeEventFrequency() {
        return Duration.valueOf(DenizenAPI.getCurrentInstance().getConfig()
                .getString("Scripts.World.Events.On time changes.Frequency of check", "250t"));

    }

}
