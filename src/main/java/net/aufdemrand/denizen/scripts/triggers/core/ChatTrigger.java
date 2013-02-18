package net.aufdemrand.denizen.scripts.triggers.core;

import net.aufdemrand.denizen.Settings;
import net.aufdemrand.denizen.npc.dNPC;
import net.aufdemrand.denizen.npc.traits.TriggerTrait;
import net.aufdemrand.denizen.scripts.containers.core.InteractScriptContainer;
import net.aufdemrand.denizen.scripts.containers.core.InteractScriptHelper;
import net.aufdemrand.denizen.scripts.triggers.AbstractTrigger;
import net.aufdemrand.denizen.utilities.Utilities;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatTrigger extends AbstractTrigger implements Listener {

    final static Pattern triggerPattern = Pattern.compile("\\/([^/]*)\\/");

    @Override
    public void onEnable() {
        denizen.getServer().getPluginManager().registerEvents(this, denizen);
    }

    @EventHandler
    public void chatTrigger(AsyncPlayerChatEvent event) {

        // Check if there is an NPC within range of a player to chat to.
        dNPC npc = Utilities.getClosestNPC(event.getPlayer().getLocation(), 25);

        // No NPC? Nothing else to do here.
        if (npc == null) return;

        // If the NPC doesn't have triggers, or the triggers are not enabled, then
        // just return.
        if (!npc.getCitizen().hasTrait(TriggerTrait.class)) return;
        if (!npc.getCitizen().getTrait(TriggerTrait.class).isEnabled(name)) return;

        // If the chat radius is not set, use default radius from settings
        if (npc.getTriggerTrait().getRadius(name) == -1)
            npc.getTriggerTrait().setLocalRadius(name, (Settings.TriggerRangeInBlocks(name)));

        // Check range
        if (npc.getTriggerTrait().getRadius(name) < npc.getLocation().distance(event.getPlayer().getLocation()))
            return;

        // The Denizen config can require some other criteria for a successful chat-with-npc.
        // Should we check 'line of sight'? Players cannot talk to NPCs through walls
        // if enabled. Should the Player chat only when looking at the NPC? This may
        // reduce accidental chats with NPCs.
        if (Settings.ChatOnlyWhenHavingLineOfSightToNPC())
            if (!npc.getEntity().hasLineOfSight(event.getPlayer())) return;
        if (Settings.ChatOnlyWhenLookingAtNPC())
            if (!Utilities.isFacingEntity(event.getPlayer(), npc.getEntity(), 45)) return;

        // If engaged or not cool, calls On Unavailable, if cool, calls On Chat
        // If available (not engaged, and cool) sets cool down and returns true.
        if (!npc.getTriggerTrait().trigger(this, event.getPlayer())) {
            // If the NPC is not interactable, Settings may allow the chat to filter
            // through. Check the Settings if this is enabled.
            if (Settings.ChatGloballyIfNotInteractable()) {
                dB.echoDebug (ChatColor.YELLOW + "Resuming. " + ChatColor.WHITE
                        + "The NPC is currently cooling down or engaged.");
                return;
            } else {
                event.setCancelled(true);
            }
        }

        // Denizen should be good to interact with. Let's get the script.
        InteractScriptContainer script = npc.getInteractScript(event.getPlayer(), this.getClass());

        // Check if the NPC has Chat Triggers for this step.
        if (!script.containsTriggerInStep(
                InteractScriptHelper.getCurrentStep(event.getPlayer(),
                        script.getName()), this.getClass())) {

            // No chat trigger for this step.. do we chat globally, or to the NPC?
            if (!Settings.ChatGloballyIfNoChatTriggers()) {
                event.setCancelled(true);
                dB.echoDebug(event.getPlayer().getName() + " says to "
                        + npc.getNicknameTrait().getNickname() + ", " + event.getMessage());
                return;
            }

            else return;
        }


        // Parse the script and match Triggers.. if found, cancel the text! The
        // parser will take care of everything else.
        String id = null;
        boolean matched = false;
        String replacementText = null;
        String regexId = null;
        String regexMessage = null;
        Map<String, String> idMap = new HashMap<String, String>();
        if (script != null)
            idMap = script.getIdMapFor(this.getClass(), event.getPlayer());

        if (!idMap.isEmpty()) {
            // Iterate through the different id entries in the step's chat trigger
            for (Map.Entry<String, String> entry : idMap.entrySet()) {
                // Check if the chat trigger specified in the specified id's 'trigger:' key
                // matches the text the player has said
                Matcher matcher = triggerPattern.matcher(entry.getValue());
                while (matcher.find ()) {
                    if (!script.checkSpecificTriggerScriptRequirementsFor(this.getClass(),
                            event.getPlayer(), npc, entry.getKey())) continue;
                    String keyword = matcher.group().replace("/", "");
                    // Check if the trigger is REGEX
                    if(isKeywordRegex(keyword)) {
                        Pattern	pattern = Pattern.compile(keyword.substring(6));
                        Matcher m = pattern.matcher(event.getMessage());
                        if (m.find()) {
                            // REGEX matches are left for last, so save it in case non-REGEX
                            // matches don't exist
                            regexId = entry.getKey();
                            regexMessage = entry.getValue().replace("/" + keyword + "/", m.group());
                        }
                    }
                    else if (event.getMessage().toUpperCase().contains(keyword.toUpperCase()))
                    {
                        // Trigger matches
                        id = entry.getKey();
                        replacementText = entry.getValue().replace("/", "");
                        matched = true;
                    }
                }
                if (matched) break;
            }
        }

        if (matched == false && regexId != null) {
            id = regexId;
            replacementText = regexMessage;
        }

        // If there was a match, the id of the match should have been returned.
        if (id != null) {
            event.setCancelled(true);
            Utilities.talkToNPC(replacementText, event.getPlayer(), npc, Settings.ChatToNpcBystandersRange());
            parse(npc, event.getPlayer(), script, id);

        } else {

            if (!Settings.ChatGloballyIfFailedChatTriggers ()) {
                event.setCancelled(true);
                dB.echoDebug(event.getPlayer().getName() + " says to "
                        + npc.getNicknameTrait().getNickname() + ", " + replacementText);
                return;
            }

            // No matching chat triggers, and the config.yml says we should just
            // ignore the interaction...

        }
    }

    private Boolean isKeywordRegex (String keyWord) {
        return keyWord.toUpperCase().startsWith("REGEX:");
    }


}