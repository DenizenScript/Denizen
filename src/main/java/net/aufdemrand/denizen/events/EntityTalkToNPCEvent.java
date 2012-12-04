package net.aufdemrand.denizen.events;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;


/**
 * Chat event when an NPC is talked to, called by a NPCSpeechEngine.
 * 
 * @author Jeremy Schroeder
 *
 */

public class EntityTalkToNPCEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private LivingEntity talker;
    private NPC npc;
    private String text;
    private TalkType talkType;
    private boolean cancelled;
    private enum TalkType { CHAT, WHISPER, SHOUT }

    public EntityTalkToNPCEvent(NPC npc, LivingEntity talker, String text, TalkType talkType) {
        this.talker = talker;
        this.npc = npc;
        this.text = text;
        this.talkType = talkType;
    }
    
    public boolean talkerIsNPC() {
        if (talker instanceof NPC) return true;
        return false;
    }
    
    public boolean talkerIsPlayer() {
        if (talker instanceof Player && !(CitizensAPI.getNPCRegistry().isNPC(talker))) return true;
        return false;
    }

    public Player getTalkerAsPlayer() {
        return (Player) talker;
    }
    
    public NPC getTalkerAsNPC() {
        return CitizensAPI.getNPCRegistry().getNPC(talker);
    }

    public LivingEntity getTalker() {
        return talker;
    }
    
    public NPC getNPC() {
        return npc;
    }

    public TalkType getTalkType() { 
        return talkType;
    }

    public String getText() {
        return text;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}