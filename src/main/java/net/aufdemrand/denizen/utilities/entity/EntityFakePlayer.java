package net.aufdemrand.denizen.utilities.entity;

import com.mojang.authlib.GameProfile;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.entity.network.FakeNetworkManager;
import net.aufdemrand.denizen.utilities.entity.network.FakePlayerConnection;
import net.aufdemrand.denizen.utilities.packets.PacketHelper;
import net.minecraft.server.v1_8_R2.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R2.CraftServer;
import org.bukkit.craftbukkit.v1_8_R2.entity.CraftPlayer;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.util.Set;

public class EntityFakePlayer extends EntityPlayer {

    private static final Field entryFlag;
    private static final Field trackerSet;

    static {
        Field entryFlagField = null;
        Field trackerSetField = null;
        try {
            entryFlagField = EntityTrackerEntry.class.getDeclaredField("u");
            entryFlagField.setAccessible(true);
            trackerSetField = EntityTracker.class.getDeclaredField("c");
            trackerSetField.setAccessible(true);
        } catch (Exception e) {
            dB.echoError(e);
        }
        entryFlag = entryFlagField;
        trackerSet = trackerSetField;
    }

    public EntityFakePlayer(MinecraftServer minecraftserver, WorldServer worldserver, GameProfile gameprofile, PlayerInteractManager playerinteractmanager) {
        super(minecraftserver, worldserver, gameprofile, playerinteractmanager);
        playerinteractmanager.setGameMode(WorldSettings.EnumGamemode.SURVIVAL);
        NetworkManager networkManager = new FakeNetworkManager(EnumProtocolDirection.CLIENTBOUND);
        playerConnection = new FakePlayerConnection(minecraftserver, networkManager, this);
        networkManager.a(playerConnection);
        datawatcher.watch(10, (byte) 127);
        this.bukkitEntity = new CraftFakePlayer((CraftServer) Bukkit.getServer(), this);
        worldserver.addEntity(this, CreatureSpawnEvent.SpawnReason.CUSTOM);
        EntityTrackerEntry entry = (EntityTrackerEntry) worldserver.getTracker().trackedEntities.get(getId());
        FakePlayerEntityTrackerEntry newEntry = new FakePlayerEntityTrackerEntry(entry);
        worldserver.getTracker().trackedEntities.a(getId(), newEntry);
        try {
            Set set = (Set) trackerSet.get(worldserver.getTracker());
            set.remove(entry);
            set.add(newEntry);
        } catch (Exception e) {
            dB.echoError(e);
        }
    }

    @Override
    public CraftFakePlayer getBukkitEntity() {
        return (CraftFakePlayer) bukkitEntity;
    }

    private static boolean getFlag(EntityTrackerEntry entry) {
        try {
            return entryFlag.getBoolean(entry);
        } catch (Exception e) {
            return false;
        }
    }

    public class FakePlayerEntityTrackerEntry extends EntityTrackerEntry {

        public FakePlayerEntityTrackerEntry(EntityTrackerEntry entry) {
            super(entry.tracker, entry.b, entry.c, getFlag(entry));
        }

        @Override
        public void updatePlayer(final EntityPlayer entityplayer) {
            if (entityplayer != this.tracker) {
                if (this.c(entityplayer)) {
                    if (!this.trackedPlayers.contains(entityplayer)
                            && (entityplayer.u().getPlayerChunkMap().a(entityplayer, this.tracker.ae, this.tracker.ag)
                            || this.tracker.attachedToPlayer)) {
                        if (this.tracker instanceof EntityPlayer) {
                            CraftPlayer player = ((EntityPlayer)this.tracker).getBukkitEntity();
                            if(!entityplayer.getBukkitEntity().canSee(player)) {
                                return;
                            }
                            final PacketPlayOutPlayerInfo[] playerListPacket = {new PacketPlayOutPlayerInfo(
                                    PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, (EntityPlayer) this.tracker)};
                            PacketHelper.sendPacket(entityplayer.getBukkitEntity(), playerListPacket[0]);
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    playerListPacket[0] = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER,
                                            (EntityPlayer) tracker);
                                    PacketHelper.sendPacket(entityplayer.getBukkitEntity(), playerListPacket[0]);
                                }
                            }.runTaskLater(DenizenAPI.getCurrentInstance(), 2);
                        }
                    }
                }
            }
            super.updatePlayer(entityplayer);
        }
    }
}
