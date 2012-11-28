package net.aufdemrand.denizen.interfaces;

import java.util.List;

import net.citizensnpcs.api.npc.NPC;

import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 * Interface for an NPC-compatible Speech Engine. Denizen uses these methods to handle
 * all kinds of talking between NPCs and Players.
 * 
 * @author Jeremy Schroeder
 *
 */

public interface SpeechEngine {

    public enum TalkType {
        CHAT, SHOUT, WHISPER
    }

    public enum SpeakerType {
        PLAYER, NPC
    }
    
    public enum TargetType {
        NPC, PLAYER, NOBODY
    }


    /*
     * Makes the NPC talk using the Denizen Speech Formatter
     */

    public void npcTalk(NPC npc, String message, TalkType talkType);

    public void npcTalk(NPC npc, String message, Player player, TalkType talkType);

    public void npcTalk(NPC npc, String message, NPC targetNpc, TalkType talkType);

    public void npcTalk(NPC npc, String preformattedText, int range, boolean toSelf);


    public void playerTalk(Player player, String message, TalkType talkType);

    public void playerTalk(Player player, String message, Player targetPlayer, TalkType talkType);

    public void playerTalk(Player player, String message, NPC npc, TalkType talkType);

    public void playerTalk(Player player, NPC npc, List<String> preformattedPlayerText, List<String> preformattedTargetText, List<String> preformattedBystanderText, int range, TargetType targetType);

    
    public void announce(String text, World world);
	
}
