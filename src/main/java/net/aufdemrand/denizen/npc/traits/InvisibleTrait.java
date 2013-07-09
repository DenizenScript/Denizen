package net.aufdemrand.denizen.npc.traits;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.trait.Toggleable;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class InvisibleTrait extends Trait implements Listener, Toggleable {

    @Persist("")
    private boolean invisible = false;

    PotionEffect invis = new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1);

    public InvisibleTrait() {
        super("invisible");
    }

    public void setInvisible(boolean invisible) {
    	
        this.invisible = invisible;
        if (invisible) setInvisible();
        else if (npc.isSpawned())
            if (npc.getBukkitEntity().hasPotionEffect(PotionEffectType.INVISIBILITY))
                npc.getBukkitEntity().removePotionEffect(PotionEffectType.INVISIBILITY);
    }

    private void setInvisible() {

        invis.apply(npc.getBukkitEntity());
    }

    @Override
    public void onSpawn() {
        if (invisible) setInvisible();
    }

    @Override
    public boolean toggle() {
        if (invisible) setInvisible(false);
        else setInvisible(true);
        return invisible;
    }
}
