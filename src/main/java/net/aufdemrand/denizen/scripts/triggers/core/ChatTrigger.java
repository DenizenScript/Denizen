package net.aufdemrand.denizen.scripts.triggers.core;

import net.aufdemrand.denizen.Settings;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.npc.traits.ChatbotTrait;
import net.aufdemrand.denizen.npc.traits.TriggerTrait;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.commands.core.DetermineCommand;
import net.aufdemrand.denizen.scripts.containers.core.InteractScriptContainer;
import net.aufdemrand.denizen.scripts.containers.core.InteractScriptHelper;
import net.aufdemrand.denizen.scripts.triggers.AbstractTrigger;
import net.aufdemrand.denizen.tags.BukkitTagContext;
import net.aufdemrand.denizencore.tags.TagManager;
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

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("deprecation")
public class ChatTrigger extends AbstractTrigger implements Listener {

    final static Pattern triggerPattern = Pattern.compile("/([^/]*)/");
    final static boolean HyperDebug = false;

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

        if (HyperDebug) dB.log("Processing chat trigger: valid npc? " + (npc != null));
        // No NPC? Nothing else to do here.
        if (npc == null) return new ChatContext(false);

        if (HyperDebug) dB.log("Has trait?  " + npc.getCitizen().hasTrait(TriggerTrait.class));
        // If the NPC doesn't have triggers, or the triggers are not enabled, then
        // just return false.
        if (!npc.getCitizen().hasTrait(TriggerTrait.class)) return new ChatContext(false);
        if (HyperDebug) dB.log("enabled? " + npc.getCitizen().getTrait(TriggerTrait.class).isEnabled(name));
        if (!npc.getCitizen().getTrait(TriggerTrait.class).isEnabled(name)) return new ChatContext(false);

        // Check range
        if (npc.getTriggerTrait().getRadius(name) < npc.getLocation().distance(player.getLocation())) {
            if (HyperDebug) dB.log("Not in range");
            return new ChatContext(false);
        }

        // The Denizen config can require some other criteria for a successful chat-with-npc.
        // Should we check 'line of sight'? Players cannot talk to NPCs through walls
        // if enabled. Should the Player chat only when looking at the NPC? This may
        // reduce accidental chats with NPCs.


        if (Settings.chatMustSeeNPC()) {
            if (!player.hasLineOfSight(npc.getEntity())) {
                if (HyperDebug) dB.log("no LOS");
                return new ChatContext(false);
            }
        }

        if (Settings.chatMustLookAtNPC()) {
            if (!Rotation.isFacingEntity(player, npc.getEntity(), 45)) {
                if (HyperDebug) dB.log("Not facing");
                return new ChatContext(false);
            }
        }

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
                if (HyperDebug) dB.log("Cancelled");
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

        if (script == null) {
            if (HyperDebug) dB.log("null script");
            return new ChatContext(message, false);
        }

        // Check if the NPC has Chat Triggers for this step.
        if (!script.containsTriggerInStep(
                InteractScriptHelper.getCurrentStep(denizenPlayer,
                        script.getName()),  ChatTrigger.class)) {

            // If this is a Chatbot, make it chat anything it wants if
            // it has no chat triggers for this step
            if (npc.getCitizen().hasTrait(ChatbotTrait.class)) {
                Utilities.talkToNPC(message, denizenPlayer, npc, Settings.chatToNpcOverhearingRange());
                npc.getCitizen().getTrait(ChatbotTrait.class).chatTo(player, message);
                if (HyperDebug) dB.log("chatbot");
                return new ChatContext(false);
            }

            // No chat trigger for this step.. do we chat globally, or to the NPC?
            else if (!Settings.chatGloballyIfNoChatTriggers()) {
                dB.echoDebug(script, player.getName() + " says to "
                        + npc.getNicknameTrait().getNickname() + ", " + message);
                return new ChatContext(false);
            }

            else {
                if (HyperDebug) dB.log("No trigger in step, chatting globally");
                return new ChatContext(message, ret);
            }
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
            List<Map.Entry<String, String>> entries = new ArrayList<Map.Entry<String, String>>(idMap.entrySet());
            Collections.sort(entries, new Comparator<Map.Entry<String, String>>() {
                @Override
                public int compare(Map.Entry<String, String> o1, Map.Entry<String, String> o2) {
                    if (o1 == null || o2 == null) {
                        return 0;
                    }
                    return o1.getKey().compareToIgnoreCase(o2.getKey());
                }
            });
            for (Map.Entry<String, String> entry : entries) {

                // Check if the chat trigger specified in the specified id's 'trigger:' key
                // matches the text the player has said
                // TODO: script arg?
                String triggerText = TagManager.tag(entry.getValue(), new BukkitTagContext
                        (denizenPlayer, npc, false, null, false, null));
                Matcher matcher = triggerPattern.matcher(triggerText);
                while (matcher.find ()) {
                    if (!script.checkSpecificTriggerScriptRequirementsFor(ChatTrigger.class,
                            denizenPlayer, npc, entry.getKey())) continue;
                    // TODO: script arg?
                    String keyword = TagManager.tag(matcher.group().replace("/", ""), new BukkitTagContext
                            (denizenPlayer, npc, false, null, false, null));
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
            if (HyperDebug) dB.log("chat to NPC");
            return new ChatContext(true);
        }
        else {
            // If this is a Chatbot, make it chat anything it wants if
            // none of its chat triggers worked
            if (npc.getCitizen().hasTrait(ChatbotTrait.class)) {
                Utilities.talkToNPC(message, denizenPlayer, npc, Settings.chatToNpcOverhearingRange());
                npc.getCitizen().getTrait(ChatbotTrait.class).chatTo(player, message);
                if (HyperDebug) dB.log("Chatbot");
                return new ChatContext(true);
            }
            else if (!Settings.chatGloballyIfFailedChatTriggers ()) {
                Utilities.talkToNPC(message, denizenPlayer, npc, Settings.chatToNpcOverhearingRange());
                if (HyperDebug) dB.log("Chat globally");
                return new ChatContext(true);
            }
            // No matching chat triggers, and the config.yml says we
            // should just ignore the interaction...
        }
        if (HyperDebug) dB.log("Finished calculating");
        return new ChatContext(message, ret);
    }


    @EventHandler
    public void asyncChatTrigger(final AsyncPlayerChatEvent event) {
        if (HyperDebug) dB.log("Chat trigger seen, cancelled: " + event.isCancelled()
                + ", chatasync: " + Settings.chatAsynchronous());
        if (event.isCancelled()) return;

        // Return if "Use asynchronous event" is false in config file
        if (!Settings.chatAsynchronous()) return;

        FutureTask<ChatContext> futureTask =  new FutureTask<ChatContext>(new Callable<ChatContext>() {
            @Override
            public ChatContext call() {
                return process(event.getPlayer(), event.getMessage());
            }
        });

        Bukkit.getScheduler().runTask(DenizenAPI.getCurrentInstance(), futureTask);

        try {
            ChatContext context = futureTask.get();
            event.setCancelled(context.wasTriggered());
            if (context.hasChanges()) {
                event.setMessage(context.getChanges());
            }
        } catch (InterruptedException e) {
            dB.echoError(e);
        } catch (ExecutionException e) {
            dB.echoError(e);
        }
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
