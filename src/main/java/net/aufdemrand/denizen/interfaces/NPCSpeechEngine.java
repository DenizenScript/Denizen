package net.aufdemrand.denizen.interfaces;

import org.bukkit.entity.LivingEntity;

/**
 * Interface for an NPC-compatible Speech Engine. Denizen uses these methods to handle
 * all kinds of talking between NPCs and Players.
 * 
 * @author Jeremy Schroeder
 *
 */

public interface NPCSpeechEngine {

    
    /**
     * Method to handle chatting, which should have some kind of configurable in-game range.
     * 
     * @param chatter 
     *      The Entity doing the chatting.
     * @param message
     *      The message to chat.
     * @param chatTarget
     *      The Entity that the chatter is chatting to. Can be null to indicate no target.
     */
    
    public void chat(LivingEntity chatter, String message, LivingEntity chatTarget);

    
    /**
     * Method to handle shouting, which should have some kind of configurable in-game range.
     * 
     * @param shouter
     *      The Entity doing the shouting.
     * @param message
     *      The message to shout.
     * @param shoutTarget
     *      The Entity that the chatter is shouting to. Can be null to indicate no target.
     */
    
    public void shout(LivingEntity shouter, String message, LivingEntity shoutTarget);
    
    
    /**
     * Method to handle whispering, which should have some kind of configurable in-game range.
     * 
     * @param whisperer
     *      The Entity doing the whispering.
     * @param message
     *      The message to whisper.
     * @param chatTarget
     *      The Entity that the chatter is chatting to. Can be null to indicate no target
     */
    
    public void whisper(LivingEntity whisperer, String message, LivingEntity whisperTarget);

    
}
