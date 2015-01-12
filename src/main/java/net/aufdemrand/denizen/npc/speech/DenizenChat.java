package net.aufdemrand.denizen.npc.speech;

import net.aufdemrand.denizen.Settings;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.queues.ScriptQueue;
import net.aufdemrand.denizen.tags.BukkitTagContext;
import net.aufdemrand.denizen.tags.TagManager;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.citizensnpcs.api.ai.speech.SpeechContext;
import net.citizensnpcs.api.ai.speech.Talkable;
import net.citizensnpcs.api.ai.speech.VocalChord;
import net.citizensnpcs.npc.ai.speech.TalkableEntity;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DenizenChat implements VocalChord {

    public final String VOCAL_CHORD_NAME = "denizen_chat";

    @Override
    public String getName() {
        return VOCAL_CHORD_NAME;
    }

    @Override
    public void talk(SpeechContext speechContext) {
        if (!(speechContext instanceof DenizenSpeechContext)) return;

        DenizenSpeechContext context = (DenizenSpeechContext) speechContext;

        Talkable talker = context.getTalker();
        if (talker == null) return;

        ScriptEntry entry = context.getScriptEntry();
        ScriptQueue queue = entry.getResidingQueue();

        String defTalker = null;
        if (queue.hasDefinition("talker"))
            defTalker = queue.getDefinition("talker");
        queue.addDefinition("talker", new dEntity(talker.getEntity()).identify());

        String defMessage = null;
        if (queue.hasDefinition("message"))
            defMessage = queue.getDefinition("message");
        queue.addDefinition("message", context.getMessage());

        // Chat to the world using Denizen chat settings
        if (!context.hasRecipients()) {
            String text = TagManager.tag(Settings.chatNoTargetFormat(), new BukkitTagContext(entry, false));
            talkToBystanders(talker, text, context);
        }

        // Single recipient
        else if (context.size() <= 1) {
            // Send chat to target
            String text = TagManager.tag(Settings.chatToTargetFormat(), new BukkitTagContext(entry, false));
            for (Talkable entity : context) {
                entity.talkTo(context, text, this);
            }
            // Check if bystanders hear targeted chat
            if (context.isBystandersEnabled()) {
                String defTarget = null;
                if (queue.hasDefinition("target"))
                    defTarget = queue.getDefinition("target");
                queue.addDefinition("target", new dEntity(context.iterator().next().getEntity()).identify());
                String bystanderText = TagManager.tag(Settings.chatWithTargetToBystandersFormat(), new BukkitTagContext(entry, false));
                talkToBystanders(talker, bystanderText, context);
                if (defTarget != null)
                    queue.addDefinition("target", defTarget);
            }
        }

        // Multiple recipients
        else {
            // Send chat to targets
            String text = TagManager.tag(Settings.chatToTargetFormat(), new BukkitTagContext(entry, false));
            for (Talkable entity : context) {
                entity.talkTo(context, text, this);
            }
            if (context.isBystandersEnabled()) {
                String[] format = Settings.chatMultipleTargetsFormat().split("%target%");
                if (format.length <= 1)
                    dB.echoError("Invalid 'Commands.Chat.Options.Multiple targets format' in config.yml! Must have at least 1 %target%");
                StringBuilder parsed = new StringBuilder();
                Iterator<Talkable> iter = context.iterator();
                int i = 0;
                while (iter.hasNext()) {
                    if (i == format.length) {
                        parsed.append(format[i]);
                        break;
                    }
                    parsed.append(format[i]).append(new dEntity(iter.next().getEntity()).getName());
                    i++;
                }
                String targets = TagManager.tag(parsed.toString(), new BukkitTagContext(entry, false));

                String defTargets = null;
                if (queue.hasDefinition("targets"))
                    defTargets = queue.getDefinition("targets");
                queue.addDefinition("targets", targets);

                String bystanderText = TagManager.tag(Settings.chatWithTargetsToBystandersFormat(), new BukkitTagContext(entry, false));
                talkToBystanders(talker, bystanderText, context);

                if (defTargets != null)
                    queue.addDefinition("targets", defTargets);
            }
        }

        if (defMessage != null)
            queue.addDefinition("message", defMessage);
        if (defTalker != null)
            queue.addDefinition("talker", defTalker);
    }

    private void talkToBystanders(Talkable talkable, String text, DenizenSpeechContext context) {
        double range = context.getChatRange();
        List<Entity> bystanderEntities = new ArrayList<Entity>();
        if (range == 0D) {
            for (Player player : Bukkit.getServer().getOnlinePlayers())
                bystanderEntities.add(player);
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
                new TalkableEntity(bystander).talkNear(context, text, this);
            }
        }
    }
}
