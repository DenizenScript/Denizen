package net.aufdemrand.denizen.npc.traits;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.interfaces.NPCSpeechEngine;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;

public class TalkTrait extends Trait {

    private Denizen denizen;
    NPCSpeechEngine speechEngine;

    public TalkTrait() {
        super("talk");
        denizen = (Denizen) Bukkit.getServer().getPluginManager().getPlugin("Denizen");
        speechEngine = denizen.getSpeechEngine();
    }

    @Override 
    public void load(DataKey key) throws NPCLoadException {  
        // Nothing to do here
    }

    @Override 
    public void save(DataKey key) {  
        // Nothing to do here
    }

    public void chat(String message, LivingEntity target) {
        speechEngine.chat((LivingEntity) npc.getBukkitEntity(), message, target);
    }
    
    public void shout(String message, LivingEntity target) {
        speechEngine.shout((LivingEntity) npc.getBukkitEntity(), message, target);
    }

    public void whisper(String message, LivingEntity target) {
        speechEngine.whisper((LivingEntity) npc.getBukkitEntity(), message, target);
    }

}
