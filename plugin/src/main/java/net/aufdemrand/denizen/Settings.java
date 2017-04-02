package net.aufdemrand.denizen;

import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.objects.Duration;
import net.aufdemrand.denizencore.utilities.CoreUtilities;

public class Settings {

    /*

    # Scripts location settings

    */

    public static boolean useDefaultScriptPath() {
        return DenizenAPI.getCurrentInstance().getConfig()
                .getBoolean("Scripts location.Use default script folder", true);
    }

    public static String getAlternateScriptPath() {
        return DenizenAPI.getCurrentInstance().getConfig()
                .getString("Scripts location.Alternative folder path", "plugins/Denizen");
    }


    /*

    # Whether Denizen should display debug in the console

    */

    public static boolean showDebug() {
        return DenizenAPI.getCurrentInstance().getConfig()
                .getBoolean("Debug.Show", true);
    }

    public static boolean overrideHelp() {
        return DenizenAPI.getCurrentInstance().getConfig()
                .getBoolean("Debug.Override help", true);
    }

    public static int consoleWidth() {
        return DenizenAPI.getCurrentInstance().getConfig()
                .getInt("Debug.Console width", 60);
    }

    public static int trimLength() {
        return DenizenAPI.getCurrentInstance().getConfig()
                .getInt("Debug.Trim length", 512);
    }

    public static boolean showExHelp() {
        return DenizenAPI.getCurrentInstance().getConfig()
                .getBoolean("Debug.Ex command help", true);
    }

    public static boolean allowConsoleRedirection() {
        return DenizenAPI.getCurrentInstance().getConfig()
                .getBoolean("Debug.Allow console redirection", false);
    }

    public static boolean canRecordStats() {
        return DenizenAPI.getCurrentInstance().getConfig().getBoolean("Debug.Stats", true);
    }

    /*

    # Sets the default speed between execution of commands in queues

    */

    public static String scriptQueueSpeed() {
        String delay = DenizenAPI.getCurrentInstance().getConfig()
                .getString("Scripts.Queue speed", "0.5s");

        // Check for users setting delay to 0, which will in turn lock up the server
        try {
            if (Duration.valueOf(delay).getTicks() < 1) {
                delay = "1t";
            }
        }
        catch (Exception e) {
            delay = "0.5s";
        }

        return delay;
    }

    /*

    # Whether the Health trait should be enabled by default

    */

    public static boolean healthTraitEnabledByDefault() {
        return DenizenAPI.getCurrentInstance().getConfig()
                .getBoolean("Traits.Health.Enabled", false);
    }

    /*

    # Whether NPCs with the Health trait should respawn after being killed

    */

    public static boolean healthTraitRespawnEnabled() {
        return DenizenAPI.getCurrentInstance().getConfig()
                .getBoolean("Traits.Health.Respawn.Enabled", true);
    }

    /*

    # Whether NPCs with the Health trait should have a death animation

    */

    public static boolean healthTraitAnimatedDeathEnabled() {
        return DenizenAPI.getCurrentInstance().getConfig()
                .getBoolean("Traits.Health.Animated death.Enabled", true);
    }

    /*

    # How long it should take for NPCs with the Health trait to respawn

    */

    public static String healthTraitRespawnDelay() {
        return DenizenAPI.getCurrentInstance().getConfig()
                .getString("Traits.Health.Respawn.Delay", "10s");
    }

    /*

    # Whether a certain trigger is enabled by default or not

    */

    public static boolean triggerEnabled(String triggerName) {
        return DenizenAPI.getCurrentInstance().getConfig()
                .getBoolean("Triggers." + String.valueOf(triggerName.charAt(0)).toUpperCase()
                        + CoreUtilities.toLowerCase(triggerName.substring(1)) + ".Enabled", true);
    }

    /*

    # Default duration of cooldown set to Denizens for when a trigger is
    # triggered. Not all triggers may use this, it is optional!

    */

    public static double triggerDefaultCooldown(String triggerName) {
        return Duration.valueOf(DenizenAPI.getCurrentInstance().getConfig()
                .getString("Triggers." + String.valueOf(triggerName.charAt(0)).toUpperCase()
                        + CoreUtilities.toLowerCase(triggerName.substring(1)) + ".Cooldown", "5s")).getSeconds();
    }

    /*

    # This set of nodes defines ranges for different types of
    # interact-script triggers. Not all triggers use a range,
    # as it may not be applicable to the trigger.

    */

    public static double triggerDefaultRange(String triggerName) {
        return DenizenAPI.getCurrentInstance().getConfig()
                .getDouble("Triggers." + String.valueOf(triggerName.charAt(0)).toUpperCase()
                        + CoreUtilities.toLowerCase(triggerName.substring(1)) + ".Range", -1);
    }

    /*

    # This set of nodes defines cooldown-types for different types of
    # interact-script triggers.

    */

    public static String triggerDefaultCooldownType(String triggerName) {
        return DenizenAPI.getCurrentInstance().getConfig()
                .getString("Triggers." + String.valueOf(triggerName.charAt(0)).toUpperCase()
                        + CoreUtilities.toLowerCase(triggerName.substring(1)) + ".Cooldown Type", "Player");
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

    public static boolean clickTriggerAllowsLeftClick() { // TODO: Remove?
        return DenizenAPI.getCurrentInstance().getConfig()
                .getBoolean("Triggers.Click.Allows left click", false);
    }

    /*

    # Default engage timeout. When NPCs are set to ENGAGE, this is
    # the default timeout that they will auto-DISENGAGE if not otherwise
    # specified. (Default, 150 seconds)

    */

    public static String engageTimeoutInSeconds() {
        return DenizenAPI.getCurrentInstance().getConfig()
                .getString("Commands.Engage.Timeout", "150s");
    }

    public static int whileMaxLoops() {
        return DenizenAPI.getCurrentInstance().getConfig()
                .getInt("Commands.While.Max loops", 10000);
    }

    public static boolean allowWebget() {
        return DenizenAPI.getCurrentInstance().getConfig()
                .getBoolean("Commands.Webget.Allow", false);
    }

    public static boolean allowStupids() {
        return allowStupid1() && allowStupid2() && allowStupid3();
    }

    public static boolean allowStupid1() {
        return DenizenAPI.getCurrentInstance().getConfig()
                .getBoolean("Commands.General.Allow stupid file abuse you moron dont enable this", false);
    }

    public static boolean allowStupid2() {
        return DenizenAPI.getCurrentInstance().getConfig()
                .getBoolean("Commands.General.are you sure", false);
    }

    public static boolean allowStupid3() {
        return DenizenAPI.getCurrentInstance().getConfig()
                .getBoolean("Commands.General.are you absolutely seriously sure dont enable this", false);
    }

    public static boolean allowStupidx() {
        return DenizenAPI.getCurrentInstance().getConfig()
                .getBoolean("Commands.General.This one dont touch", false);
    }

    public static boolean allowFilecopy() {
        return DenizenAPI.getCurrentInstance().getConfig()
                .getBoolean("Commands.Filecopy.Allow copying files", true);
    }

    public static boolean allowDelete() {
        return DenizenAPI.getCurrentInstance().getConfig()
                .getBoolean("Commands.Delete.Allow file deletion", true);
    }

    public static boolean allowServerStop() {
        return DenizenAPI.getCurrentInstance().getConfig()
                .getBoolean("Commands.Restart.Allow server stop", false);
    }

    public static boolean allowServerRestart() {
        return DenizenAPI.getCurrentInstance().getConfig()
                .getBoolean("Commands.Restart.Allow server restart", true);
    }

    public static boolean allowRunningJava() {
        return DenizenAPI.getCurrentInstance().getConfig()
                .getBoolean("Commands.Java.Allow running java", false);
    }

    public static boolean allowLogging() {
        return DenizenAPI.getCurrentInstance().getConfig()
                .getBoolean("Commands.Log.Allow logging", true);
    }

    public static boolean allowStrangeYAMLSaves() {
        return DenizenAPI.getCurrentInstance().getConfig()
                .getBoolean("Commands.Yaml.Allow saving outside folder", false);
    }

    public static String chatMultipleTargetsFormat() {
        return DenizenAPI.getCurrentInstance().getConfig()
                .getString("Commands.Chat.Options.Multiple targets format", "%target%, %target%, %target%, and others");
    }

    public static double chatBystandersRange() {
        return DenizenAPI.getCurrentInstance().getConfig()
                .getDouble("Commands.Chat.Options.Range for bystanders", 5.0);
    }

    public static String chatNoTargetFormat() {
        return DenizenAPI.getCurrentInstance().getConfig()
                .getString("Commands.Chat.Formats.No target", "[<def[talker].name>]: <def[message]>");
    }

    public static String chatToTargetFormat() {
        return DenizenAPI.getCurrentInstance().getConfig()
                .getString("Commands.Chat.Formats.To target", "[<def[talker].name>] -> You: <def[message]>");
    }

    public static String chatWithTargetToBystandersFormat() {
        return DenizenAPI.getCurrentInstance().getConfig()
                .getString("Commands.Chat.Formats.With target to bystanders", "[<def[talker].name>] -> <def[target].name>: <def[message]>");
    }

    public static String chatWithTargetsToBystandersFormat() {
        return DenizenAPI.getCurrentInstance().getConfig()
                .getString("Commands.Chat.Formats.With targets to bystanders", "[<def[talker].name>] -> [<def[targets]>]: <def[message]>");
    }

    /*

    # Whether the Chat Trigger should use an asynchronous Bukkit
    # event or not

    */

    public static boolean chatAsynchronous() {
        return DenizenAPI.getCurrentInstance().getConfig()
                .getBoolean("Triggers.Chat.Use asynchronous event", false);
    }

    /*

    # The formats in which Chat Trigger input from players appears to
    # themselves and to players who can overhear them

    */

    public static String chatToNpcFormat() {
        return DenizenAPI.getCurrentInstance().getConfig()
                .getString("Triggers.Chat.Formats.Player to NPC", "You -> <npc.name.nickname>: <text>");
    }

    public static String chatToNpcOverheardFormat() {
        return DenizenAPI.getCurrentInstance().getConfig()
                .getString("Triggers.Chat.Formats.Player to NPC overheard", "<player.name> -> <npc.name.nickname>: <text>");
    }

    /*

    # The distance from which a player chatting to an NPC can be overheard
    # by other players

    */

    public static double chatToNpcOverhearingRange() {
        return DenizenAPI.getCurrentInstance().getConfig()
                .getDouble("Triggers.Chat.Overhearing range", 4);
    }

    /*

    # Prerequisites for triggering a Chat Trigger

    */


    public static boolean chatMustSeeNPC() {
        return DenizenAPI.getCurrentInstance().getConfig()
                .getBoolean("Triggers.Chat.Prerequisites.Must be able to see NPC", true);
    }

    public static boolean chatMustLookAtNPC() {
        return DenizenAPI.getCurrentInstance().getConfig()
                .getBoolean("Triggers.Chat.Prerequisites.Must be looking in direction of NPC", true);
    }

    /*

    # Circumstances under which a player's Chat Trigger input should
    # appear in the global chat

    */

    public static boolean chatGloballyIfFailedChatTriggers() {
        return DenizenAPI.getCurrentInstance().getConfig()
                .getBoolean("Triggers.Chat.Appears globally.If triggers failed", false);
    }

    public static boolean chatGloballyIfNoChatTriggers() {
        return DenizenAPI.getCurrentInstance().getConfig()
                .getBoolean("Triggers.Chat.Appears globally.If triggers missing", true);
    }

    public static boolean chatGloballyIfUninteractable() {
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

    public static boolean worldScriptChatEventAsynchronous() {
        return DenizenAPI.getCurrentInstance().getConfig()
                .getBoolean("Scripts.World.Events.On player chats.Use asynchronous event", false);
    }

    /*

    # The frequency with which the "on time changes" world script
    # event will be checked

    */

    public static Duration worldScriptTimeEventFrequency() {
        return Duration.valueOf(DenizenAPI.getCurrentInstance().getConfig()
                .getString("Scripts.World.Events.On time changes.Frequency of check", "250t"));
    }

    public static int blockTagsMaxBlocks() {
        return DenizenAPI.getCurrentInstance().getConfig()
                .getInt("Tags.Block tags.Max blocks", 1000000);
    }

    public static int pathfindingMaxDistance() {
        return DenizenAPI.getCurrentInstance().getConfig()
                .getInt("Tags.Path finding.Max distance", 100);
    }

    public static int chatHistoryMaxMessages() {
        return DenizenAPI.getCurrentInstance().getConfig()
                .getInt("Tags.Chat history.Max messages", 10);
    }

    public static int tagTimeout() {
        return DenizenAPI.getCurrentInstance().getConfig()
                .getInt("Tags.Timeout", 10);
    }

    public static boolean packetInterception() {
        return DenizenAPI.getCurrentInstance().getConfig()
                .getBoolean("Packets.Interception", true);
    }
}
