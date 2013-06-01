package net.aufdemrand.denizen.scripts.triggers.core;

import net.aufdemrand.denizen.Settings;
import net.aufdemrand.denizen.objects.dNPC;
import net.aufdemrand.denizen.npc.traits.TriggerTrait;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.scripts.containers.core.InteractScriptContainer;
import net.aufdemrand.denizen.scripts.containers.core.InteractScriptHelper;
import net.aufdemrand.denizen.scripts.triggers.AbstractTrigger;
import net.aufdemrand.denizen.tags.TagManager;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.Utilities;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatTrigger extends AbstractTrigger implements Listener {

    final static Pattern triggerPattern = Pattern.compile("\\/([^/]*)\\/");

    @EventHandler
    public void chatTrigger(final AsyncPlayerChatEvent event) {
        if (event.isCancelled()) return;
        
        Callable<Boolean> call = new Callable<Boolean>() {
            public Boolean call() {

        // Check if there is an NPC within range of a player to chat to.
        dNPC npc = Utilities.getClosestNPC(event.getPlayer().getLocation(), 25);
        dPlayer player = dPlayer.mirrorBukkitPlayer(event.getPlayer());

        // No NPC? Nothing else to do here.
        if (npc == null) return null;

        // If the NPC doesn't have triggers, or the triggers are not enabled, then
        // just return.
        if (!npc.getCitizen().hasTrait(TriggerTrait.class)) return null;
        if (!npc.getCitizen().getTrait(TriggerTrait.class).isEnabled(name)) return null;

        // Check range
        if (npc.getTriggerTrait().getRadius(name) < npc.getLocation().distance(event.getPlayer().getLocation()))
            return null;

        // Debugger
        dB.report(name, aH.debugObj("Player", event.getPlayer().getName())
                + aH.debugObj("NPC", npc.toString())
                + aH.debugObj("Radius(Max)", npc.getLocation().distance(event.getPlayer().getLocation())
                + "(" + npc.getTriggerTrait().getRadius(name) + ")")
                + aH.debugObj("Trigger text", event.getMessage())
                + aH.debugObj("LOS", String.valueOf(npc.getEntity().hasLineOfSight(event.getPlayer())))
                + aH.debugObj("Facing", String.valueOf(Utilities.isFacingEntity(event.getPlayer(), npc.getEntity(), 45))));

        // The Denizen config can require some other criteria for a successful chat-with-npc.
        // Should we check 'line of sight'? Players cannot talk to NPCs through walls
        // if enabled. Should the Player chat only when looking at the NPC? This may
        // reduce accidental chats with NPCs.
        if (Settings.ChatMustSeeNPC())
            if (!npc.getEntity().hasLineOfSight(event.getPlayer())) return null;
        if (Settings.ChatMustLookAtNPC())
            if (!Utilities.isFacingEntity(event.getPlayer(), npc.getEntity(), 45)) return null;

        Boolean ret = null;
        
        // If engaged or not cool, calls On Unavailable, if cool, calls On Chat
        // If available (not engaged, and cool) sets cool down and returns true.
         if (!npc.getTriggerTrait().trigger(ChatTrigger.this, player)) {
            // If the NPC is not interactable, Settings may allow the chat to filter
            // through. Check the Settings if this is enabled.
            if (Settings.ChatGloballyIfUninteractable()) {
                dB.echoDebug (ChatColor.YELLOW + "Resuming. " + ChatColor.WHITE
                        + "The NPC is currently cooling down or engaged.");
                return null;
            } else {
                ret = true;
            }
        }

        // Denizen should be good to interact with. Let's get the script.
        InteractScriptContainer script = npc.getInteractScript(player, ChatTrigger.class);

        // Check if the NPC has Chat Triggers for this step.
        if (!script.containsTriggerInStep(
                InteractScriptHelper.getCurrentStep(player,
                        script.getName()),  ChatTrigger.class)) {

            // No chat trigger for this step.. do we chat globally, or to the NPC?
            if (!Settings.ChatGloballyIfNoChatTriggers()) {
                dB.echoDebug(event.getPlayer().getName() + " says to "
                        + npc.getNicknameTrait().getNickname() + ", " + event.getMessage());
                return true;
            }
            else return ret;
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
            idMap = script.getIdMapFor(ChatTrigger.class, player);

        if (!idMap.isEmpty()) {
            // Iterate through the different id entries in the step's chat trigger
            for (Map.Entry<String, String> entry : idMap.entrySet()) {
                // Check if the chat trigger specified in the specified id's 'trigger:' key
                // matches the text the player has said
                Matcher matcher = triggerPattern.matcher(entry.getValue());
                while (matcher.find ()) {
                    if (!script.checkSpecificTriggerScriptRequirementsFor(ChatTrigger.class,
                            player, npc, entry.getKey())) continue;
                    String keyword = TagManager.tag(player, npc, matcher.group().replace("/", ""));
                    // Check if the trigger is REGEX
                    if(isKeywordRegex(keyword)) {
                        Pattern	pattern = Pattern.compile(keyword.substring(6));
                        Matcher m = pattern.matcher(event.getMessage());
                        if (m.find()) {
                            // REGEX matches are left for last, so save it in case non-REGEX
                            // matches don't exist
                            regexId = entry.getKey();
                            regexMessage = entry.getValue().replace(matcher.group(), m.group());
                            dB.log("entry value: " + entry.getValue() + "  keyword: " + keyword + "  m.group: " + m.group() + "  matcher.group: " + matcher.group());
                        }
                    }
                    else if (isKeywordStrict(keyword)) {
                        if (event.getMessage().toUpperCase().equalsIgnoreCase(keyword.toUpperCase()))
                        {
                            // Trigger matches
                            id = entry.getKey();
                            replacementText = entry.getValue().replace("/", "");
                            matched = true;
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
            Utilities.talkToNPC(replacementText, player, npc, Settings.ChatToNpcOverhearingRange());
            parse(npc, player, script, id);
            return true;
        } else {
            if (!Settings.ChatGloballyIfFailedChatTriggers ()) {
                Utilities.talkToNPC(event.getMessage(), player, npc, Settings.ChatToNpcOverhearingRange());
                return true;
            }
                    // No matching chat triggers, and the config.yml says we
                    // should just ignore the interaction...
            }
        return ret;
        }
        };
        Boolean cancelled = null;
        try {
            cancelled = event.isAsynchronous() ? Bukkit.getScheduler().callSyncMethod(DenizenAPI.getCurrentInstance(), call).get() : call.call();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (cancelled == null)
            return;
        event.setCancelled(cancelled);
    }

    private boolean isKeywordRegex (String keyWord) {
        return keyWord.toUpperCase().startsWith("REGEX:");
    }

    private boolean isKeywordStrict (String keyWord) {
        return keyWord.toUpperCase().startsWith("STRICT:");
    }

}