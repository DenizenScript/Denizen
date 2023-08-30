package com.denizenscript.denizen.scripts.triggers.core;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.scripts.containers.core.InteractScriptContainer;
import com.denizenscript.denizen.scripts.containers.core.InteractScriptHelper;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.utilities.CoreConfiguration;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.Settings;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.npc.traits.TriggerTrait;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.scripts.triggers.AbstractTrigger;
import com.denizenscript.denizen.tags.BukkitTagContext;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ScriptTag;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatEvent;

import java.util.*;
import java.util.concurrent.FutureTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("deprecation")
public class ChatTrigger extends AbstractTrigger implements Listener {

    final static Pattern triggerPattern = Pattern.compile("/([^/]*)/");

    // <--[language]
    // @name Chat Triggers
    // @group NPC Interact Scripts
    // @description
    // Chat Triggers are triggered when a player chats to the NPC (usually while standing close to the NPC and facing the NPC).
    //
    // They can also be triggered by the command "/denizenclickable chat hello" (where 'hello' is replaced with the chat message). This is used for clickable triggers.
    // This option enforces all the same limitations as chatting directly, but unlike real chat, won't display the message in global chat when there's no match.
    // This requires players have the permission "denizen.clickable".
    //
    // Interact scripts are allowed to define a list of possible messages a player may type and the scripts triggered in response.
    //
    // Within any given step, the format is then as follows:
    // <code>
    // # Some identifier for the trigger, this only serves to make the sub-triggers unique, and sort them (alphabetically).
    // 1:
    //     # The trigger message written by a player. The text between // must be typed by a player, the other text is filled automatically.
    //     trigger: /keyword/ othertext
    //     script:
    //     # Your code here
    //     - wait 1
    //     # use "<context.message>" for the exact text written by the player.
    //     - chat "<context.message> eh?"
    // # You can list as many as you want
    // 2:
    //     # You can have multi-option triggers, separated by pipes (the "|" symbol). This example matches if player types 'hi', 'hello', OR 'hey'.
    //     trigger: /hi|hello|hey/
    //     script:
    //     - wait 1
    //     # use "<context.keyword>" for the specific word that was said.
    //     # this example will respond to players that said 'hi' with "hi there buddy!", 'hello' with "hello there buddy!", etc.
    //     - chat "<context.keyword> there buddy!"
    // 3:
    //     # You can have regex triggers. This example matches when the player types any numbers.
    //     trigger: /regex:\d+/
    //     script:
    //     - wait 1
    //     # use "<context.keyword>" for the text matched by the regex matcher.
    //     - chat "<context.keyword> eh?"
    // 4:
    //     # Use '*' as the trigger to match anything at all.
    //     trigger: /*/
    //     # Add this line to hide the "[Player -> NPC]: hi" initial trigger message.
    //     hide trigger message: true
    //     # Add this line to show the player chat message in the normal chat.
    //     show as normal chat: true
    //     script:
    //     # If you hide the trigger message but not show as normal chat, you might want to fill that spot with something else.
    //     - narrate "[Player -> NPC]: I don't know how to type the right thing"
    //     - wait 1
    //     - chat "Well type 'hello' or any number!"
    //     - narrate "Click <element[here].on_hover[click me!].on_click[/denizenclickable chat hello]> to auto-activate the 'hello' trigger!"
    // </code>
    //
    // -->

    public static ChatTrigger instance;

    @Override
    public AbstractTrigger activate() {
        instance = this;
        return super.activate();
    }

    @Override
    public void onEnable() {
        Bukkit.getServer().getPluginManager().registerEvents(this, Denizen.getInstance());
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
    // ElementTag to change the message.
    //
    // -->
    public ChatContext process(Player player, String message) {
        NPCTag npc = Utilities.getClosestNPC_ChatTrigger(player.getLocation(), 25);
        if (CoreConfiguration.debugVerbose) {
            Debug.log("Processing chat trigger: valid npc? " + (npc != null));
        }
        if (npc == null) {
            return new ChatContext(false);
        }
        if (CoreConfiguration.debugVerbose) {
            Debug.log("Has trait?  " + npc.getCitizen().hasTrait(TriggerTrait.class));
        }
        if (!npc.getCitizen().hasTrait(TriggerTrait.class)) {
            return new ChatContext(false);
        }
        if (CoreConfiguration.debugVerbose) {
            Debug.log("enabled? " + npc.getCitizen().getOrAddTrait(TriggerTrait.class).isEnabled(name));
        }
        if (!npc.getCitizen().getOrAddTrait(TriggerTrait.class).isEnabled(name)) {
            return new ChatContext(false);
        }
        if (npc.getTriggerTrait().getRadius(name) < npc.getLocation().distance(player.getLocation())) {
            if (CoreConfiguration.debugVerbose) {
                Debug.log("Not in range");
            }
            return new ChatContext(false);
        }
        if (Settings.chatMustSeeNPC()) {
            if (!player.hasLineOfSight(npc.getEntity())) {
                if (CoreConfiguration.debugVerbose) {
                    Debug.log("no LOS");
                }
                return new ChatContext(false);
            }
        }
        if (Settings.chatMustLookAtNPC()) {
            if (!NMSHandler.entityHelper.isFacingEntity(player, npc.getEntity(), 45)) {
                if (CoreConfiguration.debugVerbose) {
                    Debug.log("Not facing");
                }
                return new ChatContext(false);
            }
        }
        boolean ret = false;
        Map<String, ObjectTag> context = new HashMap<>();
        context.put("message", new ElementTag(message));
        TriggerTrait.TriggerContext trigger = npc.getTriggerTrait().trigger(ChatTrigger.this, new PlayerTag(player), context);
        if (trigger.hasDetermination()) {
            if (trigger.getDeterminations().containsCaseInsensitive("cancelled")) {
                if (CoreConfiguration.debugVerbose) {
                    Debug.log("Cancelled");
                }
                return new ChatContext(true);
            }
        }
        if (!trigger.wasTriggered()) {
            if (Settings.chatGloballyIfUninteractable()) {
                if (CoreConfiguration.debugVerbose) {
                    Debug.log(ChatColor.YELLOW + "Resuming. " + ChatColor.WHITE + "The NPC is currently cooling down or engaged.");
                }
                return new ChatContext(false);
            }
            else {
                ret = true;
            }
        }
        if (trigger.hasDetermination()) {
            message = trigger.getDeterminations().get(0);
        }
        List<InteractScriptContainer> scripts = npc.getInteractScripts(new PlayerTag(player), ChatTrigger.class);
        if (scripts == null) {
            if (CoreConfiguration.debugVerbose) {
                Debug.log("null scripts");
            }
            return new ChatContext(message, false);
        }
        ChatContext returnable = new ChatContext(ret);
        for (InteractScriptContainer script : scripts) {
            processSingle(message, player, npc, context, script, returnable);
        }
        return returnable;
    }

    public void processSingle(String message, Player player, NPCTag npc, Map<String, ObjectTag> context, InteractScriptContainer script, ChatContext returnable) {
        context = new HashMap<>(context);
        PlayerTag denizenPlayer = PlayerTag.mirrorBukkitPlayer(player);
        if (script.shouldDebug()) {
            Debug.report(script, name, ArgumentHelper.debugObj("Player", player.getName())
                    + ArgumentHelper.debugObj("NPC", npc.toString())
                    + ArgumentHelper.debugObj("Radius(Max)", npc.getLocation().distance(player.getLocation())
                    + "(" + npc.getTriggerTrait().getRadius(name) + ")")
                    + ArgumentHelper.debugObj("Trigger text", message)
                    + ArgumentHelper.debugObj("LOS", String.valueOf(player.hasLineOfSight(npc.getEntity())))
                    + ArgumentHelper.debugObj("Facing", String.valueOf(NMSHandler.entityHelper.isFacingEntity(player, npc.getEntity(), 45))));
        }
        String step = InteractScriptHelper.getCurrentStep(denizenPlayer, script.getName());
        if (!script.containsTriggerInStep(step, ChatTrigger.class)) {
            if (!Settings.chatGloballyIfNoChatTriggers()) {
                Debug.echoDebug(script, player.getName() + " says to " + npc.getNicknameTrait().getNickname() + ", " + message);
            }
            else if (CoreConfiguration.debugVerbose) {
                Debug.log("No trigger in step, chatting globally");
            }
            return;
        }
        String id = null;
        String replacementText = null;
        String messageLow = CoreUtilities.toLowerCase(message);
        Map<String, String> idMap = script.getIdMapFor(ChatTrigger.class, denizenPlayer);
        if (!idMap.isEmpty()) {
            mainLoop:
            for (Map.Entry<String, String> entry : idMap.entrySet()) {
                String triggerText = TagManager.tag(entry.getValue(), new BukkitTagContext(denizenPlayer, npc, null, false, null));
                Matcher matcher = triggerPattern.matcher(triggerText);
                while (matcher.find()) {
                    String keyword = TagManager.tag(matcher.group().replace("/", ""), new BukkitTagContext(denizenPlayer, npc, null, false, null));
                    String[] split = keyword.split("\\\\\\+REPLACE:", 2);
                    String replace = null;
                    if (split.length == 2) {
                        keyword = split[0];
                        replace = split[1];
                    }
                    String keywordLow = CoreUtilities.toLowerCase(keyword);
                    if (keywordLow.startsWith("regex:")) {
                        Pattern pattern = Pattern.compile(keyword.substring(6));
                        Matcher m = pattern.matcher(message);
                        if (m.find()) {
                            id = entry.getKey();
                            replacementText = triggerText.replace(matcher.group(), m.group());
                            context.put("keyword", new ElementTag(m.group()));
                            if (replace != null) {
                                replacementText = replace;
                            }
                            break mainLoop;
                        }
                    }
                    else if (keyword.contains("|")) {
                        for (String subkeyword : CoreUtilities.split(keyword, '|')) {
                            if (messageLow.contains(CoreUtilities.toLowerCase(subkeyword))) {
                                id = entry.getKey();
                                replacementText = triggerText.replace(matcher.group(), subkeyword);
                                context.put("keyword", new ElementTag(subkeyword));
                                if (replace != null) {
                                    replacementText = replace;
                                }
                                break mainLoop;
                            }
                        }
                    }
                    else if (keyword.equals("*")) {
                        id = entry.getKey();
                        replacementText = triggerText.replace("/*/", message);
                        if (replace != null) {
                            replacementText = replace;
                        }
                        break mainLoop;
                    }
                    else if (keywordLow.startsWith("strict:") && messageLow.equals(keywordLow.substring("strict:".length()))) {
                        id = entry.getKey();
                        replacementText = triggerText.replace(matcher.group(), keyword.substring("strict:".length()));
                        if (replace != null) {
                            replacementText = replace;
                        }
                        break mainLoop;
                    }
                    else if (messageLow.contains(keywordLow)) {
                        id = entry.getKey();
                        replacementText = triggerText.replace(matcher.group(), keyword);
                        if (replace != null) {
                            replacementText = replace;
                        }
                        break mainLoop;
                    }
                }
            }
        }

        // If there was a match, the id of the match should have been returned.
        String showNormalChat = script.getString("STEPS." + step + ".CHAT TRIGGER." + id + ".SHOW AS NORMAL CHAT", "false");
        if (id != null) {
            String hideTriggerMessage = script.getString("STEPS." + step + ".CHAT TRIGGER." + id + ".HIDE TRIGGER MESSAGE", "false");
            if (!hideTriggerMessage.equalsIgnoreCase("true")) {
                Utilities.talkToNPC(replacementText, denizenPlayer, npc, Settings.chatToNpcOverhearingRange(), new ScriptTag(script));
            }
            parse(npc, denizenPlayer, script, id, context);
            if (CoreConfiguration.debugVerbose) {
                Debug.log("chat to NPC");
            }
            if (!showNormalChat.equalsIgnoreCase("true")) {
                returnable.triggered = true;
            }
            return;
        }
        else {
            if (!Settings.chatGloballyIfFailedChatTriggers()) {
                Utilities.talkToNPC(message, denizenPlayer, npc, Settings.chatToNpcOverhearingRange(), new ScriptTag(script));
                if (CoreConfiguration.debugVerbose) {
                    Debug.log("Chat globally");
                }
                if (!showNormalChat.equalsIgnoreCase("true")) {
                    returnable.triggered = true;
                }
                return;
            }
            // No matching chat triggers, and the config.yml says we
            // should just ignore the interaction...
        }
        if (CoreConfiguration.debugVerbose) {
            Debug.log("Finished calculating");
        }
        returnable.changed_text = message;
    }

    @EventHandler
    public void asyncChatTrigger(final AsyncPlayerChatEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (!Settings.chatAsynchronous()) {
            return;
        }
        if (!event.isAsynchronous()) {
            syncChatTrigger(new PlayerChatEvent(event.getPlayer(), event.getMessage(), event.getFormat(), event.getRecipients()));
            return;
        }
        FutureTask<ChatContext> futureTask = new FutureTask<>(() -> process(event.getPlayer(), event.getMessage()));
        Bukkit.getScheduler().runTask(Denizen.getInstance(), futureTask);
        try {
            ChatContext context = futureTask.get();
            if (context.wasTriggered()) {
                event.setCancelled(true);
            }
            if (context.hasChanges()) {
                event.setMessage(context.getChanges());
            }
        }
        catch (Exception e) {
            Debug.echoError(e);
        }
    }

    @EventHandler
    public void syncChatTrigger(PlayerChatEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (Settings.chatAsynchronous()) {
            return;
        }
        chatTriggerInternal(event);
    }

    public void chatTriggerInternal(PlayerChatEvent event) {
        ChatContext chat = process(event.getPlayer(), event.getMessage());
        if (chat.wasTriggered()) {
            event.setCancelled(true);
        }
        if (chat.hasChanges()) {
            event.setMessage(chat.getChanges());
        }
    }

    /**
     * Contains whether the chat trigger successfully 'triggered' and any context that was
     * available while triggering or attempting to trigger.
     */
    public static class ChatContext {

        public ChatContext(boolean triggered) {
            this.triggered = triggered;
        }

        public ChatContext(String changed_text, boolean triggered) {
            this.changed_text = changed_text;
            this.triggered = triggered;
        }

        String changed_text;
        boolean triggered;

        public boolean hasChanges() {
            return changed_text != null;
        }

        public String getChanges() {
            return changed_text != null ? changed_text : "none";
        }

        public boolean wasTriggered() {
            return triggered;
        }

    }
}
