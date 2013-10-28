package net.aufdemrand.denizen.scripts.triggers.core;

import net.aufdemrand.denizen.Settings;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.npc.traits.ChatbotTrait;
import net.aufdemrand.denizen.npc.traits.TriggerTrait;
import net.aufdemrand.denizen.scripts.containers.core.InteractScriptContainer;
import net.aufdemrand.denizen.scripts.containers.core.InteractScriptHelper;
import net.aufdemrand.denizen.scripts.triggers.AbstractTrigger;
import net.aufdemrand.denizen.tags.TagManager;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.Utilities;
import net.aufdemrand.denizen.utilities.entity.Rotation;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.citizensnpcs.api.ai.speech.SpeechContext;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("deprecation")
public class ChatTrigger extends AbstractTrigger implements Listener {

    final static Pattern triggerPattern = Pattern.compile("\\/([^/]*)\\/");

    @Override
    public void onEnable() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    // Technically defined in TriggerTrait, but placing here instead.
    // <--[action]
    // @Actions
    // chat
    //
    // @Triggers when a player chats to the NPC.
    //
    // @Context
    // None
    //
    // -->
    public Boolean process(Player player, String message) {

        // Check if there is an NPC within range of a player to chat to.
        dNPC npc = Utilities.getClosestNPC_ChatTrigger(player.getLocation(), 25);
        dPlayer denizenPlayer = dPlayer.mirrorBukkitPlayer(player);

        // No NPC? Nothing else to do here.
        if (npc == null) return false;

        // If the NPC doesn't have triggers, or the triggers are not enabled, then
        // just return false.
        if (!npc.getCitizen().hasTrait(TriggerTrait.class)) return false;
        if (!npc.getCitizen().getTrait(TriggerTrait.class).isEnabled(name)) return false;

        // Check range
        if (npc.getTriggerTrait().getRadius(name) < npc.getLocation().distance(player.getLocation()))
            return false;

        // The Denizen config can require some other criteria for a successful chat-with-npc.
        // Should we check 'line of sight'? Players cannot talk to NPCs through walls
        // if enabled. Should the Player chat only when looking at the NPC? This may
        // reduce accidental chats with NPCs.


        if (Settings.ChatMustSeeNPC())
            if (!player.hasLineOfSight(npc.getEntity())) return false;

        if (Settings.ChatMustLookAtNPC())
            if (!Rotation.isFacingEntity(player, npc.getEntity(), 45)) return false;

        Boolean ret = false;

        // Denizen should be good to interact with. Let's get the script.
        InteractScriptContainer script = npc.getInteractScript(denizenPlayer, ChatTrigger.class);

        // If engaged or not cool, calls On Unavailable, if cool, calls On Chat
        // If available (not engaged, and cool) sets cool down and returns true.
        if (!npc.getTriggerTrait().trigger(ChatTrigger.this, denizenPlayer)) {
            // If the NPC is not interactable, Settings may allow the chat to filter
            // through. Check the Settings if this is enabled.
            if (Settings.ChatGloballyIfUninteractable()) {
                dB.echoDebug(script, ChatColor.YELLOW + "Resuming. " + ChatColor.WHITE
                        + "The NPC is currently cooling down or engaged.");
                return false;
            } else {
                ret = true;
            }
        }

        // Debugger
        dB.report(script, name, aH.debugObj("Player", player.getName())
                + aH.debugObj("NPC", npc.toString())
                + aH.debugObj("Radius(Max)", npc.getLocation().distance(player.getLocation())
                + "(" + npc.getTriggerTrait().getRadius(name) + ")")
                + aH.debugObj("Trigger text", message)
                + aH.debugObj("LOS", String.valueOf(player.hasLineOfSight(npc.getEntity())))
                + aH.debugObj("Facing", String.valueOf(Rotation.isFacingEntity(player, npc.getEntity(), 45))));

        if (script == null) return false;

        // Check if the NPC has Chat Triggers for this step.
        if (!script.containsTriggerInStep(
                InteractScriptHelper.getCurrentStep(denizenPlayer,
                        script.getName()),  ChatTrigger.class)) {

            // If this is a Chatbot, make it chat anything it wants if
            // it has no chat triggers for this step
            if (npc.getCitizen().hasTrait(ChatbotTrait.class)) {
                Utilities.talkToNPC(message, denizenPlayer, npc, Settings.ChatToNpcOverhearingRange());
                npc.getCitizen().getTrait(ChatbotTrait.class).chatTo(player, message);
                return true;
            }
            // No chat trigger for this step.. do we chat globally, or to the NPC?
            else if (!Settings.ChatGloballyIfNoChatTriggers()) {
                dB.echoDebug(script, player.getName() + " says to "
                        + npc.getNicknameTrait().getNickname() + ", " + message);
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

        // Use TreeMap to sort chat triggers alphabetically
        TreeMap<String, String> idMap = new TreeMap<String, String>();
        idMap.putAll(script.getIdMapFor(ChatTrigger.class, denizenPlayer));

        if (!idMap.isEmpty()) {
            // Iterate through the different id entries in the step's chat trigger
            for (Map.Entry<String, String> entry : idMap.entrySet()) {

                // Check if the chat trigger specified in the specified id's 'trigger:' key
                // matches the text the player has said
                String triggerText = TagManager.tag(denizenPlayer, npc, entry.getValue());
                Matcher matcher = triggerPattern.matcher(triggerText);
                while (matcher.find ()) {
                    if (!script.checkSpecificTriggerScriptRequirementsFor(ChatTrigger.class,
                            denizenPlayer, npc, entry.getKey())) continue;
                    String keyword = TagManager.tag(denizenPlayer, npc, matcher.group().replace("/", ""));
                    // Check if the trigger is REGEX, but only if we don't have a REGEX
                    // match already (thus using alphabetical priority for triggers)
                    if(regexId == null && isKeywordRegex(keyword)) {
                        Pattern    pattern = Pattern.compile(keyword.substring(6));
                        Matcher m = pattern.matcher(message);
                        if (m.find()) {
                            // REGEX matches are left for last, so save it in case non-REGEX
                            // matches don't exist
                            regexId = entry.getKey();
                            regexMessage = triggerText.replace(matcher.group(), m.group());
                            dB.log("entry value: " + triggerText + "  keyword: " + keyword + "  m.group: " + m.group() + "  matcher.group: " + matcher.group());
                        }
                    }
                    else if (isKeywordStrict(keyword)) {
                        if (message.toUpperCase().equalsIgnoreCase(keyword.toUpperCase()))
                        {
                            // Trigger matches
                            id = entry.getKey();
                            replacementText = triggerText.replace("/", "");
                            matched = true;
                        }
                    }
                    else if (message.toUpperCase().contains(keyword.toUpperCase()))
                    {
                        // Trigger matches
                        id = entry.getKey();
                        replacementText = triggerText.replace("/", "");
                        matched = true;
                    }
                }
                if (matched) break;
            }
        }

        if (!matched && regexId != null) {
            id = regexId;
            replacementText = regexMessage;
        }

        // If there was a match, the id of the match should have been returned.
        if (id != null) {
            Utilities.talkToNPC(replacementText, denizenPlayer, npc, Settings.ChatToNpcOverhearingRange());
            Map<String, dObject> context = new HashMap<String, dObject>();
            context.put("message", new Element(message.replace('<', (char)0x01)
                    .replace('>', (char)0x02).replace(String.valueOf((char)0x01), "<&lt>")
                    .replace(String.valueOf((char)0x02), "<&gt>").replace("%", "<&pc>")));
            parse(npc, denizenPlayer, script, id, context);
            return true;
        }
        else {
            // If this is a Chatbot, make it chat anything it wants if
            // none of its chat triggers worked
            if (npc.getCitizen().hasTrait(ChatbotTrait.class)) {
                Utilities.talkToNPC(message, denizenPlayer, npc, Settings.ChatToNpcOverhearingRange());
                npc.getCitizen().getTrait(ChatbotTrait.class).chatTo(player, message);
                return true;
            }
            else if (!Settings.ChatGloballyIfFailedChatTriggers ()) {
                Utilities.talkToNPC(message, denizenPlayer, npc, Settings.ChatToNpcOverhearingRange());
                return true;
            }
            // No matching chat triggers, and the config.yml says we
            // should just ignore the interaction...
        }
        return ret;
    }

    @EventHandler
    public void asyncChatTrigger(final AsyncPlayerChatEvent event) {
        if (event.isCancelled()) return;

        // Return if "Use asynchronous event" is false in config file
        if (!Settings.ChatAsynchronous()) return;

        Callable<Boolean> call = new Callable<Boolean>() {
            public Boolean call() {
                return process(event.getPlayer(), event.getMessage());
            }
        };

        Boolean cancelled = false;

        try {
            cancelled = event.isAsynchronous() ? Bukkit.getScheduler().callSyncMethod(DenizenAPI.getCurrentInstance(), call).get() : call.call();
        } catch (InterruptedException e) {
            // e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        event.setCancelled(cancelled);
    }

    @EventHandler
    public void syncChatTrigger(final PlayerChatEvent event) {
        if (event.isCancelled()) return;

        // Return if "Use asynchronous event" is true in config file
        if (Settings.ChatAsynchronous()) return;

        Boolean cancelled = process(event.getPlayer(), event.getMessage());
        event.setCancelled(cancelled);
    }

    private boolean isKeywordRegex (String keyWord) {
        return keyWord.toUpperCase().startsWith("REGEX:");
    }

    private boolean isKeywordStrict (String keyWord) {
        return keyWord.toUpperCase().startsWith("STRICT:");
    }

}
