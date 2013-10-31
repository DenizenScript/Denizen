package net.aufdemrand.denizen.npc.traits;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.trait.Toggleable;

import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

// <--[language]
// @name Invisible Trait
// @group npc traits
// @description
// The invisible trait will allow a NPC to remain invisible, even after a server restart. It permanently applies
// the invisible potion effect. Use '/trait invisible' or the 'invisible' command to toggle this trait.
//
// Note that player-type NPCs must have '/npc playerlist' toggled to be turned invisible. This does not apply
// to mob-type NPCs. Once invisible, the player-type NPCs can be taken off the playerlist.

// -->

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
        setInvisible(!invisible);
        return invisible;
    }

}
