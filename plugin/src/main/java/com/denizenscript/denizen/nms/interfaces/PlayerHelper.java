package com.denizenscript.denizen.nms.interfaces;

import com.denizenscript.denizen.nms.abstracts.ImprovedOfflinePlayer;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.entity.DenizenEntityType;
import com.denizenscript.denizen.utilities.entity.FakeEntity;
import com.denizenscript.denizencore.objects.Mechanism;
import org.bukkit.*;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

public abstract class PlayerHelper {

    public abstract void stopSound(Player player, String sound, SoundCategory category); // TODO: remove the category param once 1.19 is the minimum version

    public abstract FakeEntity sendEntitySpawn(List<PlayerTag> players, DenizenEntityType entityType, LocationTag location, ArrayList<Mechanism> mechanisms, int customId, UUID customUUID, boolean autoTrack);

    public abstract void deTrackEntity(Player player, Entity entity);

    public abstract void sendEntityDestroy(Player player, Entity entity);

    public abstract int getFlyKickCooldown(Player player);

    public abstract void setFlyKickCooldown(Player player, int ticks);

    public abstract int ticksPassedDuringCooldown(Player player);

    public abstract float getMaxAttackCooldownTicks(Player player);

    public abstract void setAttackCooldown(Player player, int ticks);

    public abstract boolean hasChunkLoaded(Player player, Chunk chunk);

    public abstract void setTemporaryOp(Player player, boolean op);

    public abstract void showEndCredits(Player player);

    public abstract ImprovedOfflinePlayer getOfflineData(UUID uuid);

    public abstract void resendDiscoveredRecipes(Player player);

    public abstract void quietlyAddRecipe(Player player, NamespacedKey key);

    public abstract void resendRecipeDetails(Player player);

    // TODO: once 1.20 is the minimum supported version, remove from NMS in favor of Paper API
    public String getClientBrand(Player player) {
        throw new UnsupportedOperationException();
    }

    public enum SkinLayer {
        CAPE(0),
        HAT(6),
        JACKET(1),
        LEFT_PANTS(4),
        LEFT_SLEEVE(2),
        RIGHT_PANTS(5),
        RIGHT_SLEEVE(3);

        public final int flag;

        SkinLayer(int offset) {
            this.flag = 1 << offset;
        }
    }
    public abstract byte getSkinLayers(Player player);

    public abstract void setSkinLayers(Player player, byte flags);

    public abstract void setBossBarTitle(BossBar bar, String title);

    public abstract boolean getSpawnForced(Player player);

    public abstract void setSpawnForced(Player player, boolean forced);

    public abstract Location getBedSpawnLocation(Player player);

    public abstract long getLastActionTime(Player player);

    public enum ProfileEditMode { ADD, UPDATE_DISPLAY, UPDATE_LATENCY, UPDATE_GAME_MODE, UPDATE_LISTED }

    public void sendPlayerInfoAddPacket(Player player, EnumSet<ProfileEditMode> editModes, String name, String display, UUID id, String texture, String signature, int latency, GameMode gameMode, boolean listed) { // TODO: once minimum version is 1.19 or higher, rename to 'sendPlayerInfoUpdatePacket'
        throw new UnsupportedOperationException();
    }

    public void sendPlayerInfoRemovePacket(Player player, UUID id) {
        throw new UnsupportedOperationException();
    }

    public void sendClimbableMaterials(Player player, List<Material> materials) {
        throw new UnsupportedOperationException();
    }

    public void addFakePassenger(List<PlayerTag> players, Entity entity, FakeEntity fakeEntity) {
        throw new UnsupportedOperationException();
    }

    public void refreshPlayer(Player player) {
        throw new UnsupportedOperationException();
    }
}
