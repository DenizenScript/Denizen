package net.aufdemrand.denizen.npc.speech;

import net.citizensnpcs.Citizens;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.speech.SpeechContext;
import net.citizensnpcs.api.ai.speech.SpeechController;
import net.citizensnpcs.api.ai.speech.event.NPCSpeechEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;

public class DenizenSpeechController implements SpeechController {

    private Entity entity;
    private boolean isNPC;

    public DenizenSpeechController(Entity entity) {
        this.entity = entity;
        isNPC = CitizensAPI.getNPCRegistry().isNPC(entity);
    }

    @Override
    public void speak(SpeechContext context) {
        speak(context, "denizen_chat");
    }

    @Override
    public void speak(SpeechContext context, String vocalChordName) {
        context.setTalker(entity);
        if (isNPC) {
            NPCSpeechEvent event = new NPCSpeechEvent(context, vocalChordName);
            Bukkit.getServer().getPluginManager().callEvent(event);
            if (event.isCancelled())
                return;
            vocalChordName = event.getVocalChordName();
        }
        CitizensAPI.getSpeechFactory().getVocalChord(vocalChordName).talk(context);
    }

}
