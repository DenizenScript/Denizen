package com.denizenscript.denizen.npc.speech;

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

    public void speak(DenizenSpeechContext context) {
        context.setTalker(entity);
        if (isNPC) {
            NPCSpeechEvent event = new NPCSpeechEvent(context);
            Bukkit.getServer().getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return;
            }
        }
        DenizenChat.talk(context);
    }

    @Override
    public void speak(SpeechContext context) {
        if (context instanceof DenizenSpeechContext denizenSpeechContext) {
            speak(denizenSpeechContext);
        }
    }
}
