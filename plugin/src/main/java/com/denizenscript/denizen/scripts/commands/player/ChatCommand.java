package com.denizenscript.denizen.scripts.commands.player;

import com.denizenscript.denizen.npc.speech.DenizenSpeechContext;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.tags.BukkitTagContext;
import com.denizenscript.denizen.utilities.PaperAPITools;
import com.denizenscript.denizen.utilities.Settings;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsRuntimeException;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.scripts.commands.generator.*;
import com.denizenscript.denizencore.scripts.queues.ScriptQueue;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.citizensnpcs.api.ai.speech.Talkable;
import net.citizensnpcs.api.ai.speech.TalkableEntity;
import net.citizensnpcs.api.ai.speech.event.NPCSpeechEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;

import java.util.*;

public class ChatCommand extends AbstractCommand {

    public ChatCommand() {
        setName("chat");
        setSyntax("chat [<text>] (no_target/targets:<entity>|...) (talkers:<entity>|...) (range:<#.#>)");
        setRequiredArguments(1, 4);
        isProcedural = false;
        addRemappedPrefixes("targets", "target", "t");
        addRemappedPrefixes("talkers", "talker");
        addRemappedPrefixes("range", "r");
        autoCompile();
    }

    // TODO: Should the chat command be in the NPC group instead?
    // <--[command]
    // @Name Chat
    // @Syntax chat [<text>] (no_target/targets:<entity>|...) (talkers:<entity>|...) (range:<#.#>)
    // @Required 1
    // @Maximum 4
    // @Plugin Citizens
    // @Short Causes an NPC/NPCs to send a chat message to nearby players.
    // @Synonyms Say,Speak
    // @Group player
    //
    // @Description
    // Chat uses an NPC's speech controller provided by Denizen, typically inside 'interact' or 'task' script-containers.
    // Typically there is already player and NPC context inside a queue that is using the 'chat' command.
    // In this case, only a text input is required.
    // Alternatively, target entities can be specified to have any Entity chat to a different target/targets,
    // or specify 'no_target' to not send the message to any specific target.
    //
    // Chat from an NPC is formatted by the settings present in Denizen's config.yml.
    // Players being chatted to see a slightly different message than surrounding players.
    // By default, a 'chat' will allow other players nearby to also see the conversation. For example:
    // <code>
    // - chat 'Hello!'
    // </code>
    // The player being chatted to, by default the attached Player to the script queue, will see a message 'Jack says to you, Hello!',
    // however surrounding entities will see something along the lines of 'Jack says to Bob, Hello!'.
    // The format for this is configurable via the "Denizen/config.yml" file.
    //
    // If sending messages to the Player without any surrounding entities hearing the message is desirable,
    // it is often times recommended to instead use the 'narrate' command.
    // Alternatively, on a server-wide scale, the configuration node for the 'range' can be set to 0, however this is discouraged.
    //
    // @Tags
    // None
    //
    // @Usage
    // Use to emulate an NPC talking out loud to a Player within an interact script-container.
    // - chat "Hello, <player.name>! Nice day, eh?"
    //
    // @Usage
    // Use to have an NPC talk to a group of individuals.
    // - chat targets:<npc.location.find_players_within[6].filter[has_flag[clan_initiate]]> "Welcome, initiate!"
    // -->

    public static void autoExecute(ScriptEntry scriptEntry,
                                   @ArgName("message") @ArgLinear String message,
                                   @ArgName("talkers") @ArgPrefixed @ArgDefaultNull @ArgSubType(EntityTag.class) List<EntityTag> talkers,
                                   @ArgName("targets") @ArgPrefixed @ArgDefaultNull @ArgSubType(EntityTag.class) List<EntityTag> targets,
                                   @ArgName("no_target") boolean noTarget,
                                   @ArgName("range") @ArgPrefixed @ArgDefaultText("-1") double chatRange) {
        if (targets == null) {
            if (!noTarget) {
                PlayerTag player = Utilities.getEntryPlayer(scriptEntry);
                if (player == null) {
                    throw new InvalidArgumentsRuntimeException("Missing targets!");
                }
                if (!player.isOnline()) {
                    Debug.echoDebug(scriptEntry, "Player is not online, skipping.");
                    return;
                }
                targets = Collections.singletonList(player.getDenizenEntity());
            }
        }
        if (talkers == null) {
            NPCTag talker = Utilities.getEntryNPC(scriptEntry);
            if (talker == null) {
                throw new InvalidArgumentsRuntimeException("Missing talker!");
            }
            if (!talker.isSpawned()) {
                Debug.echoDebug(scriptEntry, "Chat Talker is not spawned! Cannot talk.");
                return;
            }
            talkers = Collections.singletonList(talker.getDenizenEntity());
        }
        if (chatRange == -1) {
            chatRange = Settings.chatBystandersRange();
        }
        DenizenSpeechContext context = new DenizenSpeechContext(message, scriptEntry, chatRange);
        if (targets != null && !targets.isEmpty()) {
            for (EntityTag ent : targets) {
                context.addRecipient(ent.getBukkitEntity());
            }
        }
        for (EntityTag talker : talkers) {
            Entity entity = talker.getBukkitEntity();
            if (entity != null) {
                context.setTalker(entity);
                speak(context);
            }
            else {
                Debug.echoDebug(scriptEntry, "Chat Talker is not spawned! Cannot talk.");
            }
        }
    }

    public static void speak(DenizenSpeechContext speechContext) {
        Entity talker = speechContext.getTalker().getEntity();
        if (EntityTag.isCitizensNPC(talker)) {
            NPCSpeechEvent event = new NPCSpeechEvent(speechContext);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return;
            }
        }
        ScriptQueue queue = speechContext.getScriptEntry().getResidingQueue();
        ObjectTag defTalker = queue.getDefinitionObject("talker");
        ObjectTag defMessage = queue.getDefinitionObject("message");
        queue.addDefinition("talker", new EntityTag(talker));
        queue.addDefinition("message", new ElementTag(speechContext.getMessage(), true));
        talk(speechContext, queue);
        if (defMessage != null) {
            queue.addDefinition("message", defMessage);
        }
        if (defTalker != null) {
            queue.addDefinition("talker", defTalker);
        }
    }

    private static void talk(DenizenSpeechContext speechContext, ScriptQueue queue) {
        TagContext tagContext = new BukkitTagContext(speechContext.getScriptEntry());
        if (!speechContext.hasRecipients()) {
            String text = TagManager.tag(Settings.chatNoTargetFormat(), tagContext);
            talkToBystanders(text, speechContext);
            return;
        }
        String text = PaperAPITools.instance.convertTextToMiniMessage(TagManager.tag(Settings.chatToTargetFormat(), tagContext), true);
        for (Talkable entity : speechContext) {
            entity.talkTo(speechContext, text);
        }
        if (!speechContext.isBystandersEnabled()) {
            return;
        }
        if (speechContext.size() == 1) {
            ObjectTag defTarget = queue.getDefinitionObject("target");
            queue.addDefinition("target", new EntityTag(speechContext.iterator().next().getEntity()));
            talkToBystanders(TagManager.tag(Settings.chatWithTargetToBystandersFormat(), tagContext), speechContext);
            if (defTarget != null) {
                queue.addDefinition("target", defTarget);
            }
            return;
        }
        String[] format = Settings.chatMultipleTargetsFormat().split("%target%");
        if (format.length <= 1) {
            Debug.echoError("Invalid 'Commands.Chat.Options.Multiple targets format' in config.yml! Must have at least 1 %target%");
        }
        StringBuilder parsed = new StringBuilder();
        int i = 0;
        for (Talkable target : speechContext) {
            parsed.append(format[i]);
            if (i == format.length - 1) {
                break;
            }
            parsed.append(new EntityTag(target.getEntity()).getName());
            i++;
        }
        String targets = TagManager.tag(parsed.toString(), tagContext);
        ObjectTag defTargets = queue.getDefinitionObject("targets");
        queue.addDefinition("targets", new ElementTag(targets, true));
        String bystanderText = TagManager.tag(Settings.chatWithTargetsToBystandersFormat(), tagContext);
        talkToBystanders(bystanderText, speechContext);
        if (defTargets != null) {
            queue.addDefinition("targets", defTargets);
        }
    }

    private static void talkToBystanders(String text, DenizenSpeechContext speechContext) {
        Set<UUID> recipients = new HashSet<>(speechContext.size());
        for (Talkable recipient : speechContext) {
            recipients.add(recipient.getEntity().getUniqueId());
        }
        String parsedText = PaperAPITools.instance.convertTextToMiniMessage(text, true);
        double range = speechContext.getChatRange();
        for (Entity bystander : range == 0d ? Bukkit.getOnlinePlayers() : speechContext.getTalker().getEntity().getNearbyEntities(range, range, range)) {
            if (!recipients.contains(bystander.getUniqueId())) {
                new TalkableEntity(bystander).talkNear(speechContext, parsedText);
            }
        }
    }
}
