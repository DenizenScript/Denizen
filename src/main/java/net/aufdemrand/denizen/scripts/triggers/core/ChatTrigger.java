package net.aufdemrand.denizen.scripts.triggers.core;

import net.aufdemrand.denizen.Settings;
import net.aufdemrand.denizen.npc.DenizenNPC;
import net.aufdemrand.denizen.npc.traits.TriggerTrait;
import net.aufdemrand.denizen.scripts.ScriptEngine;
import net.aufdemrand.denizen.scripts.ScriptEngine.QueueType;
import net.aufdemrand.denizen.scripts.triggers.AbstractTrigger;
import net.aufdemrand.denizen.utilities.DenizenPlayer;
import net.aufdemrand.denizen.utilities.Utilities;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatTrigger extends AbstractTrigger implements Listener {
    // TODO:  Should not be instance based, due to this class being a singleton.
    //				Move to the metadata API.
    private	String	playerMessage;

    @Override
    public void onEnable() {
        denizen.getServer().getPluginManager().registerEvents(this, denizen);
    }

    private Boolean isKeywordRegex (String keyWord) {
        return keyWord.toUpperCase().startsWith("REGEX:");
    }


    @EventHandler
    public void chatTrigger(AsyncPlayerChatEvent event) {
        //
        // Try to find the closest NPC to the player's location.
        //
        NPC	closestNPC = Utilities.getClosestNPC(event.getPlayer().getLocation(), 3);
        // No NPC? Nothing else to do here.
        if (closestNPC == null)
            return;

        //
        // If the NPC doesn't have triggers, or the triggers are not enabled, then
        // just return.
        //
        if (!closestNPC.hasTrait(TriggerTrait.class)
                || !closestNPC.getTrait(TriggerTrait.class).isEnabled(name))
            return;

        //
        // The Denizen config can require some other criteria for a successful chat-with-npc.
        // Should we check 'line of sight'? Players cannot talk to NPCs through walls
        // if enabled.
        // Should the Player chat only when looking at the NPC? This may reduce accidental
        // chats with NPCs.
        //
        if (Settings.CheckLineOfSightWhenChatting())
            if (!closestNPC.getBukkitEntity().hasLineOfSight(event.getPlayer())) return;
        // if (Settings.ChatOnlyWhenLookingAtNPC())
        //    if (DenizenPlayer.getTargetNPC(event.getPlayer(), closestNPC.getTrait(TriggerTrait.class).getRadius(name))
        //            != closestNPC) return;

        //
        // Chattable NPC is close-by, alert the debugger
        //
        dB.log("Found a chat-able NPC close-by: " + closestNPC.getName() + "/" + closestNPC.getId() + ". Interrupting chat...");

        //
        // If engaged or not cool, calls On Unavailable, if cool, calls On Click
        // If available (not engaged, and cool) sets cool down and returns true.
        //
        if (!closestNPC.getTrait(TriggerTrait.class).trigger(this, event.getPlayer())) {
            if (Settings.ChatGloballyIfNotInteractable()) {
                dB.echoDebug (ChatColor.YELLOW + "Resuming. " + ChatColor.WHITE
                    + "The NPC is currently cooling down or engaged.");
                return;
            } else {
                event.setCancelled(true);
                DenizenPlayer.chat(event.getPlayer(), closestNPC, event.getMessage());
            }
        }

        //
        // Get the denizen-npc object that is associated to this NPC to get quicker
        // access to some core methods.
        //
        DenizenNPC denizenNPC = denizen.getNPCRegistry().getDenizen(closestNPC);

        //
        // Denizen should be good to interact with. Let's get the script.
        //
        String theScript = denizenNPC.getInteractScript(event.getPlayer(), this.getClass());

        //
        // Parse the script and match Triggers.. if found, cancel the text! The
        // parser will take care of everything else.
        //
        // List<MetadataValue>	metaData = event.getPlayer().getMetadata("denizen.chatmessage");
        // metaData.add(new FixedMetadataValue(denizen, event.getMessage()));
        this.playerMessage = event.getMessage ();

        if (this.parse (denizenNPC, event.getPlayer(), theScript)) {
            event.setCancelled(true);

        } else {

            if (!Settings.ChatGloballyIfFailedChatTriggers ()) {
                event.setCancelled(true);
                DenizenPlayer.chat(event.getPlayer(), closestNPC, event.getMessage());
                dB.echoDebug(ChatColor.YELLOW + "INFO! " + ChatColor.WHITE + "No matching chat trigger.");
                dB.echoDebug(dB.DebugElement.Footer);
                return;
            }

            // No matching chat triggers, and the config.yml says we should just
            // ignore the interaction...
            dB.echoDebug(ChatColor.YELLOW + "INFO! " + ChatColor.WHITE + "No matching chat trigger... resuming chat.");
            dB.echoDebug(dB.DebugElement.Footer);
        }
    }

    /*
     * Parses the scripts for Chat Triggers and sends new ScriptCommands to the
     * queue if found matched. Returning FALSE will cancel intervention and allow
     * the PlayerChatEvent to pass through.
     */
    public boolean parse (DenizenNPC npc, Player player, String scriptName) {
        Boolean	foundTrigger = false;
        ScriptEngine	sE = denizen.getScriptEngine();

        dB.echoDebug(dB.DebugElement.Header, "Parsing " + name + " trigger: " + npc.getName() + "/" + player.getName());

        //
        // Get Player's current step.
        //
        String	theStep = sH.getCurrentStep(player, scriptName);

        //
        // Figure out if any of the triggers fired.
        //
        Map<String,List<String>> triggerMap = this.getChatTriggers(scriptName, theStep);
        for (String triggerStep : triggerMap.keySet()) {

            //
            // Iterate over the keywords that can trigger this step and see if all of
            // them match what the user typed.  All of the keywords must match in
            // order for the trigger to fire.
            //
            Boolean	foundMatch = true;
            for (String keyWord : triggerMap.get(triggerStep)) {
                // Fill tags in the keyWord
                keyWord = denizen.tagManager().tag(player, npc, keyWord, false);

                dB.echoDebug("Checking keywords...");

                //
                // Is this a REGEX keyword?  If so, and it doesn't match what the user
                // entered, we can stop looking.
                //
                if (this.isKeywordRegex(keyWord)) {
                    Pattern	pattern = Pattern.compile(keyWord.substring(6));
                    if (pattern.matcher(playerMessage).find () ) {
                        continue;
                    }
                    foundMatch = false;
                    break;
                }

                //
                // This is a normal keyword.  If the player's message doesn't match the
                // keyword, then stop looking.
                //
                else if (playerMessage.toLowerCase().contains(keyWord.toLowerCase()) == false) {
                    foundMatch = false;
                    break;
                }
            }

            //
            // Did we find a match?
            //
            if (foundMatch) {
                //
                // Found a match to the keyword.  Now get the script that needs to be
                // executed by using the triggerStep that we're on.
                //
                dB.echoApproval("Found match.");
                foundTrigger = true;
                List<String> scriptContents = sE.getScriptHelper().getScriptContents (triggerStep + sE.getScriptHelper().scriptKey);
                if (scriptContents == null || scriptContents.isEmpty()) {
                    continue;
                }

                //
                // Queue the script in the player's queue.
                //
                sB.queueScriptEntries (
                        player,
                        sB.buildScriptEntries (
                                player,
                                npc,
                                scriptContents,
                                scriptName,
                                theStep),
                        QueueType.PLAYER);
            }
        }

        return foundTrigger;
    }


    /**
     * This method will return all of the steps of a script that have chat
     * triggers associated to them.  This only returns those steps that have
     * associated 'Chat Trigger' sections.
     *
     * @param scriptName	The script being processed.
     * @param stepName	The current step.
     *
     * @return	This will return a map of script paths to the chat triggers that
     * 					cause the step to process.
     */
    public Map<String,List<String>> getChatTriggers(String scriptName, String stepName) {
        //
        // This is the REGEX for extracting the "key words" from a trigger.
        // Keywords are denoted by surround them with forward slahes, such as:
        //
        //		/Yes/ I'll help.
        //
        Pattern	triggerPattern = Pattern.compile ("\\/([^/]*)\\/");

        //
        // This is the path to the "Chat Trigger" we're processing.
        //
        String	path = (scriptName + ".steps." + stepName + ".chat trigger").toUpperCase();

        //
        // This is the map of the script keys to the keywords that can trigger the
        // step.
        //
        Map<String,List<String>> triggerMap = new HashMap<String,List<String>> ();

        //
        // Iterate over all of this step's keys looking for chat triggers.
        //
        ConfigurationSection	config = denizen.getScripts ().getConfigurationSection(path);
        if (config != null) {
            Set<String> keys = config.getKeys(false);
            if (keys != null) {
                for (String key : keys) {
                    //
                    // Build the key to the trigger and attempt to get it for the step that
                    // we're currently processing.
                    //
                    String	stepKey = (path + "." + key + ".trigger").toUpperCase();
                    String	triggerValue = denizen.getScripts ().getString (stepKey);

                    //
                    // Did we find a trigger for the current step that we're on and does this
                    // trigger contain a "/" character (which is used for designating the key
                    // text that causes the chat trigger to fire)?
                    //
                    if (triggerValue != null && triggerValue.contains("/")) {
                        List<String>	keyWords = new ArrayList<String> ();
                        //
                        // Now find all of the keywords in the trigger.  Make sure to strip off
                        // the slashes when building the list of key words.
                        //
                        Matcher matcher = triggerPattern.matcher(triggerValue);
                        while (matcher.find ()) {
                            String keyWord = matcher.group ();
                            keyWords.add(keyWord.substring(1, keyWord.length() - 1));
                        }

                        triggerMap.put(path + "." + key, keyWords);
                    }
                }
            }
        }

        // No triggers
        if (triggerMap.isEmpty())
          dB.echoDebug(ChatColor.YELLOW + "INFO +> " + ChatColor.WHITE + "No chat triggers found for script '" + scriptName
            + "' in step '" + stepName + "'.");

        return triggerMap;
    }

}