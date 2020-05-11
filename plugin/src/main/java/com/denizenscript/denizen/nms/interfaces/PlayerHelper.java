package com.denizenscript.denizen.nms.interfaces;

import com.denizenscript.denizen.nms.abstracts.ImprovedOfflinePlayer;
import com.denizenscript.denizencore.objects.Mechanism;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.*;

public abstract class PlayerHelper {

    public Entity sendEntitySpawn(Player player, EntityType entityType, Location location, ArrayList<Mechanism> mechanisms) {
        throw new UnsupportedOperationException();
    }

    public void sendEntityDestroy(Player player, Entity entity) {
        throw new UnsupportedOperationException();
    }

    public abstract int getFlyKickCooldown(Player player);

    public abstract void setFlyKickCooldown(Player player, int ticks);

    public abstract int ticksPassedDuringCooldown(Player player);

    public abstract float getMaxAttackCooldownTicks(Player player);

    public abstract float getAttackCooldownPercent(Player player);

    public abstract void setAttackCooldown(Player player, int ticks);

    public abstract boolean hasChunkLoaded(Player player, Chunk chunk);

    public abstract int getPing(Player player);

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
}
