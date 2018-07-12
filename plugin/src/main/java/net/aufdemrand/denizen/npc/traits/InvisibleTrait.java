package net.aufdemrand.denizen.npc.traits;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.trait.ArmorStandTrait;
import net.citizensnpcs.trait.Toggleable;
import net.citizensnpcs.util.NMS;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class InvisibleTrait extends Trait implements Listener, Toggleable {

    // <--[language]
    // @name Invisible Trait
    // @group NPC Traits
    // @description
    // The invisible trait will allow a NPC to remain invisible, even after a server restart. It permanently applies
    // the invisible potion effect. Use '/trait invisible' or the 'invisible' script command to toggle this trait.
    //
    // Note that player-type NPCs must have '/npc playerlist' toggled to be turned invisible.
    // Once invisible, the player-type NPCs can be taken off the playerlist.
    // This only applies specifically to player-type NPCs.
    // Playerlist will be enabled automatically if not set in advance, but not automatically removed.
    // -->

    @Persist("")
    private boolean invisible = true;

    private static PotionEffect invis = new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1);

    public InvisibleTrait() {
        super("invisible");
    }

    public void setInvisible(boolean invisible) {
        this.invisible = invisible;
        if (npc.isSpawned() && npc.getEntity() instanceof LivingEntity) {
            setInvisible((LivingEntity) npc.getEntity(), npc, invisible);
        }
    }

    public static void setInvisible(LivingEntity ent, NPC npc, boolean invisible) {
        if (invisible) {
            setInvisible(ent, npc);
        }
        else {
            setVisible(ent, npc);
        }
    }

    public static void setVisible(LivingEntity ent, NPC npc) {
        if (ent.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
            ent.removePotionEffect(PotionEffectType.INVISIBILITY);
        }
        if (ent.getType() == EntityType.ARMOR_STAND) {
            ((ArmorStand) ent).setVisible(true);
            if (npc != null) {
                npc.getTrait(ArmorStandTrait.class).setVisible(true);
            }
        }
    }

    public static void setInvisible(LivingEntity ent, NPC npc) {
        // Apply NPC Playerlist if necessary
        if (npc != null && ent.getType() == EntityType.PLAYER) {
            npc.data().setPersistent("removefromplayerlist", false);
            NMS.addOrRemoveFromPlayerList(ent, false);
        }
        else if (ent.getType() == EntityType.ARMOR_STAND) {
            ((ArmorStand) ent).setVisible(false);
            if (npc != null) {
                npc.getTrait(ArmorStandTrait.class).setVisible(false);
            }
        }
        else {
            invis.apply(ent);
        }
    }

    private void setInvisible() {
        if (npc.isSpawned() && npc.getEntity() instanceof LivingEntity) {
            setInvisible((LivingEntity) npc.getEntity(), npc);
        }
    }

    @Override
    public void onSpawn() {
        if (invisible) {
            setInvisible();
        }
    }

    @Override
    public boolean toggle() {
        setInvisible(!invisible);
        return invisible;
    }

    @Override
    public void onRemove() {
        setInvisible(false);
    }

    @Override
    public void onAttach() {
        setInvisible(invisible);
    }
}
