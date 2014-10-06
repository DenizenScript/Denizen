package net.aufdemrand.denizen.scripts.triggers.core;

import net.aufdemrand.denizen.Settings;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.npc.traits.ChatbotTrait;
import net.aufdemrand.denizen.npc.traits.TriggerTrait;
import net.aufdemrand.denizen.scripts.commands.core.DetermineCommand;
import net.aufdemrand.denizen.scripts.containers.core.InteractScriptContainer;
import net.aufdemrand.denizen.scripts.containers.core.InteractScriptHelper;
import net.aufdemrand.denizen.scripts.triggers.AbstractTrigger;
import net.aufdemrand.denizen.tags.TagManager;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.Utilities;
import net.aufdemrand.denizen.utilities.entity.Rotation;
import net.aufdemrand.denizen.utilities.debugging.dB;

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

    final static Pattern triggerPattern = Pattern.compile("/([^/]*)/");

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
    // <context.message> returns the triggering message
    // <context.keyword> returns the keyword matched by a RegEx trigger
    //
    // @Determine
    // "CANCELLED" to stop the player from chatting.
    // Element(String) to change the message.
    //
    // -->
    public ChatContext process(Player player, String message) {

        // Check if there is an NPC within range of a player to chat to.
        dNPC npc = Utilities.getClosestNPC_ChatTrigger(player.getLocation(), 25);
        dPlayer denizenPlayer = dPlayer.mirrorBukkitPlayer(player);

        // No NPC? Nothing else to do here.
        if (npc == null) return new ChatContext(false);

        // If the NPC doesn't have triggers, or the triggers are not enabled, then
        // just return false.
        if (!npc.getCitizen().hasTrait(TriggerTrait.class)) return new ChatContext(false);
        if (!npc.getCitizen().getTrait(TriggerTrait.class).isEnabled(name)) return new ChatContext(false);

        // Check range
        if (npc.getTriggerTrait().getRadius(name) < npc.getLocation().distance(player.getLocation()))
            return new ChatContext(false);

        // The Denizen config can require some other criteria for a successful chat-with-npc.
        // Should we check 'line of sight'? Players cannot talk to NPCs through walls
        // if enabled. Should the Player chat only when looking at the NPC? This may
        // reduce accidental chats with NPCs.


        if (Settings.chatMustSeeNPC())
            if (!player.hasLineOfSight(npc.getEntity())) return new ChatContext(false);

        if (Settings.chatMustLookAtNPC())
            if (!Rotation.isFacingEntity(player, npc.getEntity(), 45)) return new ChatContext(false);

        Boolean ret = false;

        // Denizen should be good to interact with. Let's get the script.
        InteractScriptContainer script = npc.getInteractScript(denizenPlayer, ChatTrigger.class);

        Map<String, dObject> context = new HashMap<String, dObject>();
        context.put("message", new Element(message));

        //
        // Fire the Actions!
        //

        // If engaged or not cool, calls On Unavailable, if cool, calls On Chat
        // If available (not engaged, and cool) sets cool down and returns true.
        TriggerTrait.TriggerContext trigger = npc.getTriggerTrait()
                .trigger(ChatTrigger.this, denizenPlayer, context);

        // Return false if determine cancelled
        if (trigger.hasDetermination()) {
            if (trigger.getDetermination().equalsIgnoreCase("cancelled")) {
                // Mark as handled, the event will cancel.
                return new ChatContext(true);
            }
        }

        // Return false if trigger was unable to fire
        if (!trigger.wasTriggered()) {
            // If the NPC is not interact-able, Settings may allow the chat to filter
            // through. Check the Settings if this is enabled.
            if (Settings.chatGloballyIfUninteractable()) {
                dB.echoDebug(script, ChatColor.YELLOW + "Resuming. " + ChatColor.WHITE
                        + "The NPC is currently cooling down or engaged.");
                return new ChatContext(false);

            } else
                ret = true;
        }

        // Debugger
        dB.report(script, name, aH.debugObj("Player", player.getName())
                + aH.debugObj("NPC", npc.toString())
                + aH.debugObj("Radius(Max)", npc.getLocation().distance(player.getLocation())
                + "(" + npc.getTriggerTrait().getRadius(name) + ")")
                + aH.debugObj("Trigger text", message)
                + aH.debugObj("LOS", String.valueOf(player.hasLineOfSight(npc.getEntity())))
                + aH.debugObj("Facing", String.valueOf(Rotation.isFacingEntity(player, npc.getEntity(), 45))));

        // Change the text if it's in the determination
        if (trigger.hasDetermination()) {
            message = trigger.getDetermination();
        }

        if (script == null) return new ChatContext(message, false);

        // Check if the NPC has Chat Triggers for this step.
        if (!script.containsTriggerInStep(
                InteractScriptHelper.getCurrentStep(denizenPlayer,
                        script.getName()),  ChatTrigger.class)) {

            // If this is a Chatbot, make it chat anything it wants if
            // it has no chat triggers for this step
            if (npc.getCitizen().hasTrait(ChatbotTrait.class)) {
                Utilities.talkToNPC(message, denizenPlayer, npc, Settings.chatToNpcOverhearingRange());
                npc.getCitizen().getTrait(ChatbotTrait.class).chatTo(player, message);
                return new ChatContext(false);
            }

            // No chat trigger for this step.. do we chat globally, or to the NPC?
            else if (!Settings.chatGloballyIfNoChatTriggers()) {
                dB.echoDebug(script, player.getName() + " says to "
                        + npc.getNicknameTrait().getNickname() + ", " + message);
                return new ChatContext(false);
            }

            else return new ChatContext(message, ret);
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
                    String[] split = keyword.split("\\\\\\+REPLACE:", 2);
                    String replace = null;
                    if (split.length == 2) {
                        keyword = split[0];
                        replace = split[1];
                    }
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
                            context.put("keyword", new Element(m.group()));
                            if (replace != null)
                                regexMessage = replace;
                        }
                    }
                    else if (isKeywordStrict(keyword)) {
                        if (message.toUpperCase().equalsIgnoreCase(keyword.toUpperCase()))
                        {
                            // Trigger matches
                            id = entry.getKey();
                            replacementText = triggerText.replace("/", "");
                            matched = true;
                            if (replace != null)
                                replacementText = replace;
                        }
                    }
                    else if (message.toUpperCase().contains(keyword.toUpperCase()))
                    {
                        // Trigger matches
                        id = entry.getKey();
                        replacementText = triggerText.replace("/", "");
                        matched = true;
                        if (replace != null)
                            replacementText = replace;
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
            Utilities.talkToNPC(replacementText, denizenPlayer, npc, Settings.chatToNpcOverhearingRange());
            parse(npc, denizenPlayer, script, id, context);
            return new ChatContext(true);
        }
        else {
            // If this is a Chatbot, make it chat anything it wants if
            // none of its chat triggers worked
            if (npc.getCitizen().hasTrait(ChatbotTrait.class)) {
                Utilities.talkToNPC(message, denizenPlayer, npc, Settings.chatToNpcOverhearingRange());
                npc.getCitizen().getTrait(ChatbotTrait.class).chatTo(player, message);
                return new ChatContext(true);
            }
            else if (!Settings.chatGloballyIfFailedChatTriggers ()) {
                Utilities.talkToNPC(message, denizenPlayer, npc, Settings.chatToNpcOverhearingRange());
                return new ChatContext(true);
            }
            // No matching chat triggers, and the config.yml says we
            // should just ignore the interaction...
        }
        return new ChatContext(message, ret);
    }


    @EventHandler
    public void asyncChatTrigger(final AsyncPlayerChatEvent event) {
        if (event.isCancelled()) return;

        // Return if "Use asynchronous event" is false in config file
        if (!Settings.chatAsynchronous()) return;

        Callable<ChatContext> call = new Callable<ChatContext>() {
            public ChatContext call() {
                return process(event.getPlayer(), event.getMessage());
            }
        };

        Boolean cancelled = false;

        try {
            // Determine if the chat should be cancelled
            cancelled = event.isAsynchronous()
                    ? Bukkit.getScheduler().callSyncMethod(DenizenAPI.getCurrentInstance(), call).get().wasTriggered()
                    : call.call().wasTriggered();

            // Handle any changes with the ChatContext message
            if (call.call().hasChanges())
                event.setMessage(call.call().getChanges());

        } catch (InterruptedException e) {
            // This is normal -- probably.
            // dB.echoError(e);
        } catch (ExecutionException e) {
            dB.echoError(e);
        } catch (Exception e) {
            dB.echoError(e);
        }


        if (cancelled)
            event.setCancelled(true);
    }

    @EventHandler
    public void syncChatTrigger(final PlayerChatEvent event) {
        if (event.isCancelled()) return;

        // Return if "Use asynchronous event" is true in config file
        if (Settings.chatAsynchronous()) return;

        ChatContext chat = process(event.getPlayer(), event.getMessage());

        if (chat.wasTriggered())
            event.setCancelled(true);


        if (chat.hasChanges())
            event.setMessage(chat.getChanges());
    }

    private boolean isKeywordRegex (String keyWord) {
        return keyWord.toUpperCase().startsWith("REGEX:");
    }

    private boolean isKeywordStrict (String keyWord) {
        return keyWord.toUpperCase().startsWith("STRICT:");
    }


    /**
     * Contains whether the chat trigger successfully 'triggered' and any context that was
     * available while triggering or attempting to trigger.
     *
     */
    public class ChatContext {

        public ChatContext(boolean triggered) {
            this.triggered = triggered;
        }

        public ChatContext(String changed_text, boolean triggered) {
            this.changed_text = changed_text;
            this.triggered = triggered;
        }

        String changed_text; boolean triggered;

        public boolean hasChanges() {
            return changed_text != null;
        }

        public String getChanges() {
            return changed_text != null ? changed_text : DetermineCommand.DETERMINE_NONE;
        }

        public Boolean wasTriggered() {
            return triggered;
        }

    }
}
