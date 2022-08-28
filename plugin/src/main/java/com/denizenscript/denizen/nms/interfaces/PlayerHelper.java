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
import java.util.List;
import java.util.UUID;

public abstract class PlayerHelper {

    public abstract void stopSound(Player player, String sound, SoundCategory category); // TODO: 1.19 - remove the category param

    public FakeEntity sendEntitySpawn(List<PlayerTag> players, DenizenEntityType entityType, LocationTag location, ArrayList<Mechanism> mechanisms, int customId, UUID customUUID, boolean autoTrack) {
        throw new UnsupportedOperationException();
    }

    public void deTrackEntity(Player player, Entity entity) {
        throw new UnsupportedOperationException();
    }

    public void sendEntityDestroy(Player player, Entity entity) {
        throw new UnsupportedOperationException();
    }

    public abstract int getFlyKickCooldown(Player player);

    public abstract void setFlyKickCooldown(Player player, int ticks);

    public abstract int ticksPassedDuringCooldown(Player player);

    public abstract float getMaxAttackCooldownTicks(Player player);

    public abstract void setAttackCooldown(Player player, int ticks);

    public abstract boolean hasChunkLoaded(Player player, Chunk chunk);

    public abstract void setTemporaryOp(Player player, boolean op);

    public abstract void showEndCredits(Player player);

    public abstract ImprovedOfflinePlayer getOfflineData(UUID uuid);

    public abstract ImprovedOfflinePlayer getOfflineData(OfflinePlayer offlinePlayer);

    public abstract void resendDiscoveredRecipes(Player player);

    public abstract void quietlyAddRecipe(Player player, NamespacedKey key);

    public abstract void resendRecipeDetails(Player player);

    public abstract String getPlayerBrand(Player player);

    public enum SkinLayer {
        CAPE(0),
        HAT(6),
        JACKET(1),
        LEFT_PANTS(4),
        LEFT_SLEEVE(2),
        RIGHT_PANTS(5),
        RIGHT_SLEEVE(3);

        public int flag;

        SkinLayer(int offset) {
            this.flag = 1 << offset;
        }
    }
    public byte getSkinLayers(Player player) {
        throw new UnsupportedOperationException();
    }

    public void setSkinLayers(Player player, byte flags) {
        throw new UnsupportedOperationException();
    }

    public void setBossBarTitle(BossBar bar, String title) {
        bar.setTitle(title);
    }

    public boolean getSpawnForced(Player player) {
        throw new UnsupportedOperationException();
    }

    public void setSpawnForced(Player player, boolean forced) {
        throw new UnsupportedOperationException();
    }

    public long getLastActionTime(Player player) {
        throw new UnsupportedOperationException();
    }

    public enum ProfileEditMode { ADD, UPDATE_DISPLAY, UPDATE_LATENCY }

    public void sendPlayerInfoAddPacket(Player player, ProfileEditMode mode, String name, String display, UUID id, String texture, String signature, int latency, GameMode gameMode) {
        throw new UnsupportedOperationException();
    }

    public void sendPlayerRemovePacket(Player player, UUID id) {
        throw new UnsupportedOperationException();
    }

    public void sendClimbableMaterials(Player player, List<Material> materials) {
        throw new UnsupportedOperationException();
    }
}
