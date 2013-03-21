package net.aufdemrand.denizen.npc.traits;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.citizensnpcs.api.trait.Trait;
import net.minecraft.server.v1_5_R1.DataWatcher;
import net.minecraft.server.v1_5_R1.EntityLiving;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_5_R1.entity.CraftLivingEntity;

public class ParticlesTrait extends Trait {
	
	public enum EffectType { NONE, SMOKE, FLAME, ENDER, POTBREAK, POTION }
	
	//DataWatcher dw;
	//EntityLiving el;
	
	Denizen denizen = DenizenAPI.getCurrentInstance();
	World world;
	
	EffectType effectType = EffectType.NONE;
	
	int wait = 10;
	int counter = 0;
	//int c = 0;
	//int tempcounter = 0;
	
	@Override
	public void run() {
		if (world == null) {
			return;
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
                c = net.minecraft.server.v1_5_R1.PotionBrewer.a(el.effects.values());
            }
            dw.watch(8, Integer.valueOf(c));
            */
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
	}
	
	public void playEnderEffect() {
		Location location = npc.getBukkitEntity().getLocation();
		world.playEffect(location, Effect.ENDER_SIGNAL, 0);
	}
	
	public void playPotionEffect() {
		//dw.watch(8, Integer.valueOf(2));
	}
	
	public void playPotionBreakEffect() {
		Location location = npc.getBukkitEntity().getLocation();
		world.playEffect(location, Effect.POTION_BREAK, 0);
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

}
