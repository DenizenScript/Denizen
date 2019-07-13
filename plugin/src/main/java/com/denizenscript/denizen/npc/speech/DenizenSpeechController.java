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

    /**
     * Makes the talker speak, based on the context given, with the DenizenChat vocal chord.
     *
     * @param context the context
     */
    public void speak(DenizenSpeechContext context) {
        context.setTalker(entity);
        if (isNPC) {
            NPCSpeechEvent event = new NPCSpeechEvent(context, "denizen_chat");
            Bukkit.getServer().getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return;
            }
        }
        (CitizensAPI.getSpeechFactory().getVocalChord("denizen_chat")).talk(context);
    }

    @Override
    public void speak(SpeechContext context) {
        if (context instanceof DenizenSpeechContext) {
            speak((DenizenSpeechContext) context);
        }
        else {
            speak(context, "chat");
        }
    }

    @Override
    public void speak(SpeechContext context, String vocalChordName) {
        context.setTalker(entity);
        if (isNPC) {
            NPCSpeechEvent event = new NPCSpeechEvent(context, vocalChordName);
            Bukkit.getServer().getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return;
            }
            vocalChordName = event.getVocalChordName();
        }
        CitizensAPI.getSpeechFactory().getVocalChord(vocalChordName).talk(context);
    }
}
