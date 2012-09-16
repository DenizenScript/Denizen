package net.aufdemrand.denizen.commands.core;

import java.util.Random;

import net.aufdemrand.denizen.commands.AbstractCommand;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.citizensnpcs.command.exception.CommandException;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Pig;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.entity.Wolf;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Spawns an entity in the world based on input data.
 * 
 * @author Jeremy Schroeder
 */

public class SpawnCommand extends AbstractCommand {

	/* SPAWN [ENTITY_TYPE] (QUANTITY) (BOOKMARK:LocationBookmark) */

	/* Arguments: [] - Required, () - Optional 
	 * [ENTITY_TYPE] 
	 * (QTY:#) will default to '1' if not specified
	 * (LOCATION BOOKMARK) Will default to the player location if not specified
	 * 
	 * Modifiers:
	 * ('SPREAD:[#]') Increases the 'spread' of the area that the monster can spawn. 
	 * ('EFFECT:[POTION_EFFECT] [LEVEL]') Applies a potion effect on the monster when spawning.
	 * ('OPTION:POWERED|SADDLED|BABY|PROFESSION [PROFESSION_TYPE]|SHEARED|COLORED [DYE_COLOR]|ANGRY')
	 *   Applies a flag to the Mob. Note: Only works for mobs that can accept the flag.
	 *   CREEPER can have POWERED
	 *   PIG can have SADDLED
	 *   PIG, SHEEP, COW, VILLAGER, CHICKEN, OCELOT and WOLF can have BABY
	 *   VILLAGER can have PROFESSION
	 *      Valid PROFESSION_TYPEs: FARMER, LIBRARIAN, PRIEST, BLACKSMITH, BUTCHER
	 *   SHEEP can have SHEARED and COLORED
	 *   WOLF and PIG_ZOMBIE can have ANGRY
	 *  
	 * Example usages:
	 * SPAWN BOAT
	 * SPAWN QTY:3 COW Cage
	 * SPAWN VILLAGER 'El Notcho:Gate'
	 * SPAWN 'QTY:10' PIG_ZOMBIE SPREAD:5
	 * SPAWN QTY:25 ZOMBIE SPREAD:20 'EFFECT:INCREASE_DAMAGE 2'
	 * SPAWN QTY:2 SHEEP COLORED:RED
	 */

	@Override
	public boolean execute(ScriptEntry theEntry) throws CommandException {

		/* Initialize variables */

		EntityType theEntity = null;
		Integer theAmount = null;
		Location theLocation = null;
		Integer theSpread = null;
		PotionEffect theEffect = null;
		Boolean hasFlag = false;
		Boolean isTame = false;
		Boolean isBaby = false;
		Boolean isPowered = false;
		Boolean isAngry = false;
		Boolean isSaddled = false;
		Boolean isSheared = false;
		String hasColor = null;
		String hasProfession = null;

		if (theEntry.arguments() == null)
			throw new CommandException("...Usage: SPAWN [ENTITY_TYPE] (QTY:#) (BOOKMARK:LocationBookmark)");

		for (String thisArg : theEntry.arguments()) {
			
			// Fill replaceables
			if (thisArg.contains("<")) thisArg = aH.fillReplaceables(theEntry.getPlayer(), theEntry.getDenizen(), thisArg, false);
			
			/* If a valid name of an Entity, set theEntity. */
			if (plugin.utilities.isEntity(thisArg)) {
				theEntity = EntityType.valueOf(thisArg.toUpperCase());
				aH.echoDebug("...entity to spawn set to '%s'.", thisArg);
			}

			/* If argument is a #, set theAmount. */
			else if (aH.matchesInteger(thisArg)) {
				theAmount = aH.getIntegerModifier(thisArg);
				aH.echoDebug("...quantity to spawn: '%s'.", thisArg);
			}

			/* If argument is QTY: modifier */
			else if (aH.matchesQuantity(thisArg)) {
				theAmount = aH.getIntegerModifier(thisArg);
				aH.echoDebug("...quantity to spawn: '%s'.", thisArg);
			}

			/* If argument is a modifier, modify */
			else if (thisArg.matches("(?:SPREAD|spread|Spread)(:)(\\d+)"))
				theSpread = Integer.valueOf(thisArg.split(":", 2)[1]);

			else if (thisArg.toUpperCase().contains("EFFECT:"))
				try { 
					int theAmplifier = 1;
					if (thisArg.split(":", 2)[1].split(" ").length == 2) 
						theAmplifier = Integer.valueOf(thisArg.split(":", 2)[1].split(" ")[1]);
					theEffect = new PotionEffect(
							PotionEffectType.getByName(thisArg.split(":", 2)[1].split(" ")[0]),
							Integer.MAX_VALUE,
							theAmplifier);
					aH.echoDebug("...spawning with effect: '%s'.", thisArg);  
				} catch (Exception e) {
					aH.echoError("Invalid Potion_Type! '%s'.", thisArg);

				}

			else if (thisArg.toUpperCase().contains("OPTION:")) {
				String thisFlag = thisArg.split(":", 2)[1];
				hasFlag = true;
				if (thisFlag.toUpperCase().equals("BABY")) isBaby = true;
				if (thisFlag.toUpperCase().equals("ANGRY"))  isAngry = true;
				if (thisFlag.toUpperCase().equals("POWERED")) isPowered = true;
				if (thisFlag.toUpperCase().equals("SHEARED")) isSheared = true;
				if (thisFlag.toUpperCase().equals("TAME")) isTame = true;
				if (thisFlag.toUpperCase().equals("SADDLED")) isSaddled = true;
				if (thisFlag.toUpperCase().contains("COLORED ")) hasColor = thisFlag.split(" ")[1];
				if (thisFlag.toUpperCase().contains("PROFESSION "))
					hasProfession = thisFlag.split(" ")[1];
				aH.echoDebug("...setting: '%s'.", thisArg);
			}

			/* If argument is a BOOKMARK modifier */
			else if (aH.matchesBookmark(thisArg)) {
				theLocation = aH.getBookmarkModifier(thisArg, theEntry.getDenizen());
				if (theLocation != null)
					aH.echoDebug("...using location: '%s'", thisArg);
			}

			else aH.echoError("Could not match argument '%s'.", thisArg);

		}


		/* Location and Quantity are optional, so if they weren't set, let's use the information we have
		 * to set the defaults. Default amount is 1, default Location is the location of the Denizen. If no
		 * denizen attached (ie. this is a Task Script), default location is the location of the Player. */    

		if (theAmount == null) theAmount = 1;
		if (theLocation == null && theEntry.getDenizen() != null) 
			theLocation = theEntry.getDenizen().getLocation();
		if (theLocation == null) theLocation = theEntry.getPlayer().getLocation();


		/* Now the creature spawning! */
		if (theLocation != null && theAmount != null && theEntity != null) {
			for (int x = 0; x < theAmount; x++) {

				Location spawnLoc =  theLocation.clone();

				/* Account for SPREAD: */
				if (theSpread != null) {
					int randomX, randomZ;
					Random randomGenerator = new Random();
					int i = 0;
					boolean ok = false;
					do {
						randomX = randomGenerator.nextInt(theSpread * 2);
						randomZ = randomGenerator.nextInt(theSpread * 2);
						randomX =- theSpread;
						randomZ =- theSpread;
						i++;
						if (theLocation.getWorld().getBlockAt(theLocation.getBlockX() + randomX, theLocation.getBlockY()+1 , theLocation.getBlockZ() + randomZ).getTypeId() == 0)
						{
							spawnLoc.add(randomX, 0, randomZ);
							ok = true;
						}
					} while (i < theSpread*theSpread && !ok);
				}

				Entity spawnedEntity = null;
				if (theEntity.equals(EntityType.BOAT)) spawnLoc.getWorld().spawn(spawnLoc, Boat.class);
				if (theEntity.equals(EntityType.MINECART)) spawnLoc.getWorld().spawn(spawnLoc, Minecart.class);
				else spawnedEntity = spawnLoc.getWorld().spawnEntity(spawnLoc, theEntity);

				if (theEffect != null)
					((LivingEntity) spawnedEntity).addPotionEffect(theEffect);

				if (hasFlag) 
					try {
						if (isBaby) if (spawnedEntity instanceof Ageable) ((Ageable) spawnedEntity).setBaby();
						if (isTame) if (spawnedEntity instanceof Tameable) ((Tameable) spawnedEntity).setTamed(true);
						if (isAngry) if (spawnedEntity instanceof Wolf) ((Wolf) spawnedEntity).setAngry(true);
						if (isAngry) if (spawnedEntity instanceof PigZombie) ((PigZombie) spawnedEntity).setAngry(true);
						if (isPowered) if (spawnedEntity instanceof Creeper) ((Creeper) spawnedEntity).setPowered(true);
						if (isSaddled) if (spawnedEntity instanceof Pig) ((Pig) spawnedEntity).setSaddle(true);
						if (isSheared) if (spawnedEntity instanceof Sheep) ((Sheep) spawnedEntity).setSheared(true);
						if (hasColor != null) if (spawnedEntity instanceof Sheep) ((Sheep) spawnedEntity).setColor(DyeColor.valueOf(hasColor.toUpperCase()));
						if (hasProfession != null) if (spawnedEntity instanceof Villager) ((Villager) spawnedEntity).setProfession(Profession.valueOf(hasProfession.toUpperCase()));

					} catch (Exception e) {
						throw new CommandException("Problem setting flag!");
					}

			}

			return true;
		}
		
		return false;
	}
}