package net.aufdemrand.denizen.npc.traits;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;

import org.bukkit.Effect;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R2.entity.CraftWolf;
import org.bukkit.entity.Wolf;

public class ParticlesTrait extends Trait {

    public enum EffectType { NONE, SMOKE, FLAME, ENDER, POTBREAK, HEART, POTION, EXPLOSION }

    //DataWatcher dw;
    //EntityLiving el;

    Denizen denizen = DenizenAPI.getCurrentInstance();
    World world;

    @Persist("effect type")
    EffectType effectType = EffectType.NONE;

    @Persist("particle delay")
    int wait = 10;

    @Persist("dense")
    Boolean dense = false;

    int counter = 0;
    //int c = 0;
    int tempcounter = 0;

    @Override
    public void run() {
        if (world == null) {
            return;
        }

        if (effectType == null) {
            effectType = EffectType.NONE;
        }

        if (tempcounter > 20) {
            dB.log("current effect: " + effectType.name());
        }
        counter++;

        switch (effectType) {
        case NONE:
            break;
        case FLAME:
            if (counter > wait) {
                playFlameEffect();
                //dB.log("playing flame");
                counter = 0;
            }
            break;
        case ENDER:
            if (counter > wait) {
                playEnderEffect();
                //dB.log("playing ender");
                counter = 0;
            }
        case SMOKE:
            if (counter > wait) {
                playSmokeEffect();
                //dB.log("playing smoke");
                counter = 0;
            }
        case POTBREAK:
            if (counter > wait) {
                playPotionBreakEffect();
                //dB.log("playing potion break");
                counter = 0;
            }
            break;
        case POTION:
            /*
            if (!el.effects.isEmpty()) {
                c = net.minecraft.server.v1_6_R2.PotionBrewer.a(el.effects.values());
            }
            dw.watch(8, Integer.valueOf(c));
            */
            break;
        case HEART:
            if (counter > wait) {
                //dB.log("...playing heart effect");
                playHeartEffect();
                counter = 0;
            }
            break;
        case EXPLOSION:
            if (counter > wait) {
                //dB.log("...playing explosion effect");
                playExplosionEffect();
                counter = 0;
            }
            break;
        }


    }

    @Override
    public void onSpawn() {
        //el = ((CraftLivingEntity)npc.getBukkitEntity()).getHandle();
        //dw = el.getDataWatcher();
        world = npc.getBukkitEntity().getWorld();
    }

    public void playFlameEffect() {
        Location location = npc.getBukkitEntity().getLocation();
        world.playEffect(location, Effect.MOBSPAWNER_FLAMES, 0);
        if (dense) world.playEffect(location.add(0, 1, 0), Effect.MOBSPAWNER_FLAMES, 0);
    }

    public void playEnderEffect() {
        Location location = npc.getBukkitEntity().getLocation();
        world.playEffect(location, Effect.ENDER_SIGNAL, 0);
        if (dense) world.playEffect(location.add(0, 1, 0), Effect.ENDER_SIGNAL, 0);
    }

    public void playPotionEffect() { // TODO: Implement?
        //dw.watch(8, Integer.valueOf(2));
    }

    public void playPotionBreakEffect() {
        Location location = npc.getBukkitEntity().getLocation();
        world.playEffect(location, Effect.POTION_BREAK, 0);
        if (dense) world.playEffect(location.add(0, 1, 0), Effect.POTION_BREAK, 0);
    }

    public void playHeartEffect() {
        Location location = npc.getBukkitEntity().getLocation();
        Wolf tempWolf = world.spawn(location, Wolf.class);
        ((CraftWolf) tempWolf).getHandle().setInvisible(true);
        tempWolf.playEffect(EntityEffect.WOLF_HEARTS);
        if (dense) tempWolf.playEffect(EntityEffect.WOLF_HEARTS);
        tempWolf.remove();
    }

    public void playSmokeEffect() {
        Location location = npc.getBukkitEntity().getLocation();
        world.playEffect(location, Effect.SMOKE, 0);
        world.playEffect(location, Effect.SMOKE, 1);
        world.playEffect(location, Effect.SMOKE, 2);
        world.playEffect(location, Effect.SMOKE, 3);
        world.playEffect(location, Effect.SMOKE, 4);
        world.playEffect(location, Effect.SMOKE, 5);
        world.playEffect(location, Effect.SMOKE, 6);
        world.playEffect(location, Effect.SMOKE, 7);
        world.playEffect(location, Effect.SMOKE, 8);
        if (dense) {
            world.playEffect(location.add(0, 1, 0), Effect.SMOKE, 0);
            world.playEffect(location.add(0, 1, 0), Effect.SMOKE, 1);
            world.playEffect(location.add(0, 1, 0), Effect.SMOKE, 2);
            world.playEffect(location.add(0, 1, 0), Effect.SMOKE, 3);
            world.playEffect(location.add(0, 1, 0), Effect.SMOKE, 4);
            world.playEffect(location.add(0, 1, 0), Effect.SMOKE, 5);
            world.playEffect(location.add(0, 1, 0), Effect.SMOKE, 6);
            world.playEffect(location.add(0, 1, 0), Effect.SMOKE, 7);
            world.playEffect(location.add(0, 1, 0), Effect.SMOKE, 8);
        }
    }

    public void playExplosionEffect() {
        Location location = npc.getBukkitEntity().getLocation();
        world.createExplosion(location, 0);
    }

    public void setEffect(String effectType) {
        this.effectType = EffectType.valueOf(effectType.toUpperCase());
    }

    public void setWait(Integer ticks) {
        wait = ticks;
    }

    public ParticlesTrait() {
        super("particles");
    }

    public void setDense (Boolean dense) {
        this.dense = dense;
    }
}
