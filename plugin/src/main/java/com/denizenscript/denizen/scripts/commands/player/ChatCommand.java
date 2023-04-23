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
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.scripts.commands.generator.*;
import com.denizenscript.denizencore.scripts.queues.ScriptQueue;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.citizensnpcs.api.ai.speech.Talkable;
import net.citizensnpcs.api.ai.speech.TalkableEntity;
import net.citizensnpcs.api.ai.speech.event.NPCSpeechEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

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

    public static void speak(DenizenSpeechContext context) {
        if (isNPC) {
            NPCSpeechEvent event = new NPCSpeechEvent(context);
            Bukkit.getServer().getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return;
            }
        }
        Talkable talker = context.getTalker();
        if (talker == null) {
            return;
        }

        ScriptEntry entry = context.getScriptEntry();
        ScriptQueue queue = entry.getResidingQueue();

        String defTalker = null;
        if (queue.hasDefinition("talker")) {
            defTalker = queue.getDefinition("talker");
        }
        queue.addDefinition("talker", new EntityTag(talker.getEntity()).identify());

        String defMessage = null;
        if (queue.hasDefinition("message")) {
            defMessage = queue.getDefinition("message");
        }
        queue.addDefinition("message", context.getMessage());

        // Chat to the world using Denizen chat settings
        if (!context.hasRecipients()) {
            String text = TagManager.tag(Settings.chatNoTargetFormat(), new BukkitTagContext(entry));
            talkToBystanders(talker, text, context);
        }

        // Single recipient
        else if (context.size() <= 1) {
            // Send chat to target
            String text = TagManager.tag(Settings.chatToTargetFormat(), new BukkitTagContext(entry));
            for (Talkable entity : context) {
                entity.talkTo(context, PaperAPITools.instance.convertTextToMiniMessage(text, true));
            }
            // Check if bystanders hear targeted chat
            if (context.isBystandersEnabled()) {
                String defTarget = null;
                if (queue.hasDefinition("target")) {
                    defTarget = queue.getDefinition("target");
                }
                queue.addDefinition("target", new EntityTag(context.iterator().next().getEntity()).identify());
                String bystanderText = TagManager.tag(Settings.chatWithTargetToBystandersFormat(), new BukkitTagContext(entry));
                talkToBystanders(talker, bystanderText, context);
                if (defTarget != null) {
                    queue.addDefinition("target", defTarget);
                }
            }
        }

        // Multiple recipients
        else {
            // Send chat to targets
            String text = TagManager.tag(Settings.chatToTargetFormat(), new BukkitTagContext(entry));
            for (Talkable entity : context) {
                entity.talkTo(context, PaperAPITools.instance.convertTextToMiniMessage(text, true));
            }
            if (context.isBystandersEnabled()) {
                String[] format = Settings.chatMultipleTargetsFormat().split("%target%");
                if (format.length <= 1) {
                    Debug.echoError("Invalid 'Commands.Chat.Options.Multiple targets format' in config.yml! Must have at least 1 %target%");
                }
                StringBuilder parsed = new StringBuilder();
                Iterator<Talkable> iter = context.iterator();
                int i = 0;
                while (iter.hasNext()) {
                    if (i == format.length - 1) {
                        parsed.append(format[i]);
                        break;
                    }
                    parsed.append(format[i]).append(new EntityTag(iter.next().getEntity()).getName());
                    i++;
                }
                String targets = TagManager.tag(parsed.toString(), new BukkitTagContext(entry));

                String defTargets = null;
                if (queue.hasDefinition("targets")) {
                    defTargets = queue.getDefinition("targets");
                }
                queue.addDefinition("targets", targets);

                String bystanderText = TagManager.tag(Settings.chatWithTargetsToBystandersFormat(), new BukkitTagContext(entry));
                talkToBystanders(talker, bystanderText, context);

                if (defTargets != null) {
                    queue.addDefinition("targets", defTargets);
                }
            }
        }

        if (defMessage != null) {
            queue.addDefinition("message", defMessage);
        }
        if (defTalker != null) {
            queue.addDefinition("talker", defTalker);
        }
    }

    public static void talkToBystanders(Talkable talkable, String text, DenizenSpeechContext context) {
        double range = context.getChatRange();
        List<Entity> bystanderEntities;
        if (range == 0D) {
            bystanderEntities = new ArrayList<>(Bukkit.getServer().getOnlinePlayers());
        }
        else {
            bystanderEntities = talkable.getEntity().getNearbyEntities(range, range, range);
        }
        for (Entity bystander : bystanderEntities) {
            boolean shouldTalk = true;
            // Exclude targeted recipients
            if (context.hasRecipients()) {
                for (Talkable target : context) {
                    if (target.getEntity().equals(bystander)) {
                        shouldTalk = false;
                        break;
                    }
                }
            }
            // Found a nearby LivingEntity, make it Talkable and
            // talkNear it if 'should_talk'
            if (shouldTalk) {
                new TalkableEntity(bystander).talkNear(context, PaperAPITools.instance.convertTextToMiniMessage(text, true));
            }
        }
    }
}
