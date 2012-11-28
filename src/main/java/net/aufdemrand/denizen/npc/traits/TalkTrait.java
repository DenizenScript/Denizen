package net.aufdemrand.denizen.npc.traits;

import org.bukkit.entity.Player;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.interfaces.SpeechEngine;
import net.aufdemrand.denizen.interfaces.SpeechEngine.TalkType;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;

public class TalkTrait extends Trait {

    private Denizen denizen;
    SpeechEngine speechEngine;

    public TalkTrait() {
        super("talk");
        denizen = (Denizen) denizen.getServer().getPluginManager().getPlugin("Denizen");
        speechEngine = denizen.getSpeechEngine();
    }

    @Override public void load(DataKey key) throws NPCLoadException {   }

    @Override public void save(DataKey key) {   }

    
    // Chat

    public void chat(String message) {
        speechEngine.npcTalk(npc, message, TalkType.CHAT);
    }

    public void chat(String message, Player player) {
        speechEngine.npcTalk(npc, message, player, TalkType.CHAT);
    }

    public void chat(String message, NPC npc) {
        speechEngine.npcTalk(this.npc, message, npc, TalkType.CHAT);
    }

    
    // Shout

    public void shout(String message) {
        speechEngine.npcTalk(npc, message, TalkType.SHOUT);
    }

    public void shout(String message, Player player) {
        speechEngine.npcTalk(npc, message, player, TalkType.SHOUT);
    }

    public void shout(String message, NPC npc) {
        speechEngine.npcTalk(this.npc, message, npc, TalkType.SHOUT);
    }


    // Whisper

    public void whisper(String message) {
        speechEngine.npcTalk(npc, message, TalkType.WHISPER);
    }

    public void whisper(String message, Player player) {
        speechEngine.npcTalk(npc, message, player, TalkType.WHISPER);
    }

    public void whisper(String message, NPC npc) {
        speechEngine.npcTalk(this.npc, message, npc, TalkType.WHISPER);
    }


}
