package net.aufdemrand.denizen.npc.traits;

import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.minecraft.server.v1_5_R2.EntityHuman;
import net.minecraft.server.v1_5_R2.EntityPlayer;

import org.bukkit.craftbukkit.v1_5_R2.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Listener;

public class SneakingTrait extends Trait implements Listener  {
	
	@Persist("sneaking")
    private boolean sneaking = false;
		
	EntityHuman eh = null;
	
	@Override
	public void onSpawn() {
		eh = ((CraftPlayer) npc.getBukkitEntity()).getHandle();
		if (sneaking) sneak();
	}
	
	/**x
	 * Makes the NPC sneak
	 */
	public void sneak() {
		DenizenAPI.getDenizenNPC(npc).action("sneak", null);

		if (npc.getBukkitEntity().getType() != EntityType.PLAYER) {
			return;
		}

		((EntityPlayer) eh).getDataWatcher().watch(0, Byte.valueOf((byte) 0x02));

		sneaking = true;
		return;
	}
	
	/**
	 * Makes the NPC stand
	 */
	public void stand() {
		DenizenAPI.getDenizenNPC(npc).action("stand", null);
		
		((EntityPlayer) eh).getDataWatcher().watch(0, Byte.valueOf((byte) 0x00));
		
		sneaking = false;
	}
	
	/**
	 * Checks if the NPC is currently sneaking
	 * 
	 * @return boolean
	 */
	public boolean isSneaking() {
		return sneaking;
	}
	
	public SneakingTrait() {
		super("sneaking");
	}

}
