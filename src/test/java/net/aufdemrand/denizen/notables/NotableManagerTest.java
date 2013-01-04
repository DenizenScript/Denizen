package net.aufdemrand.denizen.notables;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.BlockChangeDelegate;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Difficulty;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.TreeType;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Item;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;
import org.junit.Test;

public class NotableManagerTest {
	class TestWorld implements World {
		private	String	name;
		
		public TestWorld (String name) {
			this.name = name;
		}

		@Override
		public Set<String> getListeningPluginChannels() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void sendPluginMessage(Plugin arg0, String arg1, byte[] arg2) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public List<MetadataValue> getMetadata(String arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean hasMetadata(String arg0) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void removeMetadata(String arg0, Plugin arg1) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setMetadata(String arg0, MetadataValue arg1) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean canGenerateStructures() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean createExplosion(Location arg0, float arg1) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean createExplosion(Location arg0, float arg1, boolean arg2) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean createExplosion(double arg0, double arg1, double arg2,
				float arg3) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean createExplosion(double arg0, double arg1, double arg2,
				float arg3, boolean arg4) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean createExplosion(double arg0, double arg1, double arg2,
				float arg3, boolean arg4, boolean arg5) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public Item dropItem(Location arg0, ItemStack arg1) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Item dropItemNaturally(Location arg0, ItemStack arg1) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean generateTree(Location arg0, TreeType arg1) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean generateTree(Location arg0, TreeType arg1,
				BlockChangeDelegate arg2) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean getAllowAnimals() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean getAllowMonsters() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public int getAmbientSpawnLimit() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public int getAnimalSpawnLimit() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public Biome getBiome(int arg0, int arg1) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Block getBlockAt(Location arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Block getBlockAt(int arg0, int arg1, int arg2) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int getBlockTypeIdAt(Location arg0) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public int getBlockTypeIdAt(int arg0, int arg1, int arg2) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public Chunk getChunkAt(Location arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Chunk getChunkAt(Block arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Chunk getChunkAt(int arg0, int arg1) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Difficulty getDifficulty() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public ChunkSnapshot getEmptyChunkSnapshot(int arg0, int arg1,
				boolean arg2, boolean arg3) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public List<Entity> getEntities() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		@Deprecated
		public <T extends Entity> Collection<T> getEntitiesByClass(Class<T>... arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public <T extends Entity> Collection<T> getEntitiesByClass(Class<T> arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Collection<Entity> getEntitiesByClasses(Class<?>... arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Environment getEnvironment() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getFullTime() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public String getGameRuleValue(String arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String[] getGameRules() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public ChunkGenerator getGenerator() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Block getHighestBlockAt(Location arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Block getHighestBlockAt(int arg0, int arg1) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int getHighestBlockYAt(Location arg0) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public int getHighestBlockYAt(int arg0, int arg1) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public double getHumidity(int arg0, int arg1) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public boolean getKeepSpawnInMemory() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public List<LivingEntity> getLivingEntities() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Chunk[] getLoadedChunks() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int getMaxHeight() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public int getMonsterSpawnLimit() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public String getName() {
			return this.name;
		}

		@Override
		public boolean getPVP() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public List<Player> getPlayers() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public List<BlockPopulator> getPopulators() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int getSeaLevel() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public long getSeed() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public Location getSpawnLocation() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public double getTemperature(int arg0, int arg1) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public int getThunderDuration() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public long getTicksPerAnimalSpawns() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public long getTicksPerMonsterSpawns() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public long getTime() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public UUID getUID() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int getWaterAnimalSpawnLimit() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public int getWeatherDuration() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public File getWorldFolder() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public WorldType getWorldType() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean hasStorm() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isAutoSave() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isChunkInUse(int arg0, int arg1) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isChunkLoaded(Chunk arg0) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isChunkLoaded(int arg0, int arg1) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isGameRule(String arg0) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isThundering() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void loadChunk(Chunk arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void loadChunk(int arg0, int arg1) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean loadChunk(int arg0, int arg1, boolean arg2) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void playEffect(Location arg0, Effect arg1, int arg2) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public <T> void playEffect(Location arg0, Effect arg1, T arg2) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void playEffect(Location arg0, Effect arg1, int arg2, int arg3) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public <T> void playEffect(Location arg0, Effect arg1, T arg2, int arg3) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void playSound(Location arg0, Sound arg1, float arg2, float arg3) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean refreshChunk(int arg0, int arg1) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean regenerateChunk(int arg0, int arg1) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void save() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setAmbientSpawnLimit(int arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setAnimalSpawnLimit(int arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setAutoSave(boolean arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setBiome(int arg0, int arg1, Biome arg2) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setDifficulty(Difficulty arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setFullTime(long arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean setGameRuleValue(String arg0, String arg1) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void setKeepSpawnInMemory(boolean arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setMonsterSpawnLimit(int arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setPVP(boolean arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setSpawnFlags(boolean arg0, boolean arg1) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean setSpawnLocation(int arg0, int arg1, int arg2) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void setStorm(boolean arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setThunderDuration(int arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setThundering(boolean arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setTicksPerAnimalSpawns(int arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setTicksPerMonsterSpawns(int arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setTime(long arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setWaterAnimalSpawnLimit(int arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setWeatherDuration(int arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public <T extends Entity> T spawn(Location arg0, Class<T> arg1)
				throws IllegalArgumentException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Arrow spawnArrow(Location arg0, Vector arg1, float arg2, float arg3) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		@Deprecated
		public LivingEntity spawnCreature(Location arg0, EntityType arg1) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		@Deprecated
		public LivingEntity spawnCreature(Location arg0, CreatureType arg1) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Entity spawnEntity(Location arg0, EntityType arg1) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public FallingBlock spawnFallingBlock(Location arg0, Material arg1,
				byte arg2) throws IllegalArgumentException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public FallingBlock spawnFallingBlock(Location arg0, int arg1, byte arg2)
				throws IllegalArgumentException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public LightningStrike strikeLightning(Location arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public LightningStrike strikeLightningEffect(Location arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean unloadChunk(Chunk arg0) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean unloadChunk(int arg0, int arg1) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean unloadChunk(int arg0, int arg1, boolean arg2) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean unloadChunk(int arg0, int arg1, boolean arg2, boolean arg3) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean unloadChunkRequest(int arg0, int arg1) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean unloadChunkRequest(int arg0, int arg1, boolean arg2) {
			// TODO Auto-generated method stub
			return false;
		}
	}
	
  @Test
  public void testNotable () {
  	NotableManager mgr = new NotableManager ();
  	
  	Location loc = new Location (new TestWorld ("Test Name"), 69, 72, 6972);
  	mgr.addNotable("Test Notable", loc);
  	
  	assertTrue (mgr.getNotable ("Test Notable") != null);
  	assertTrue (loc.getX() == mgr.getNotable("Test Notable").getLocation().getX());
  	assertTrue (loc.getY() == mgr.getNotable("Test Notable").getLocation().getY());
  	assertTrue (loc.getZ() == mgr.getNotable("Test Notable").getLocation().getZ());
  	assertTrue (loc.getWorld ().getName ().equalsIgnoreCase (mgr.getNotable("Test Notable").getLocation().getWorld ().getName ()));
  }
}
