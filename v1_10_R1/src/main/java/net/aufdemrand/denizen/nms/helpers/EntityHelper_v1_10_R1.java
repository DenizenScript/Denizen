package net.aufdemrand.denizen.nms.helpers;

import net.aufdemrand.denizen.nms.interfaces.EntityHelper;
import net.minecraft.server.v1_10_R1.Block;
import net.minecraft.server.v1_10_R1.BlockPosition;
import net.minecraft.server.v1_10_R1.EntityHuman;
import net.minecraft.server.v1_10_R1.EntityLiving;
import net.minecraft.server.v1_10_R1.EnumDirection;
import net.minecraft.server.v1_10_R1.EnumHand;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_10_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftAnimals;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftCreature;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftPlayer;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityTargetEvent;

import java.util.UUID;

public class EntityHelper_v1_10_R1 implements EntityHelper {

    @Override
    public void forceInteraction(Player player, Location location) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        BlockPosition pos = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        Block.getById(location.getBlock().getType().getId())
                .interact(((CraftWorld) location.getWorld()).getHandle(), pos,
                        ((CraftWorld) location.getWorld()).getHandle().getType(pos),
                        craftPlayer != null ? craftPlayer.getHandle() : null, EnumHand.MAIN_HAND, null,
                        EnumDirection.NORTH, 0f, 0f, 0f);
    }

    @Override
    public Entity getEntity(World world, UUID uuid) {
        net.minecraft.server.v1_10_R1.Entity entity = ((CraftWorld) world).getHandle().getEntity(uuid);
        return entity == null ? null : entity.getBukkitEntity();
    }

    @Override
    public boolean isBreeding(Animals entity) {
        return ((CraftAnimals) entity).getHandle().isInLove();
    }

    @Override
    public void setBreeding(Animals entity, boolean breeding) {
        if (breeding) {
            ((CraftAnimals) entity).getHandle().c((EntityHuman) null);
        }
        else {
            ((CraftAnimals) entity).getHandle().resetLove();
        }
    }

    @Override
    public void setTarget(Creature entity, LivingEntity target) {
        EntityLiving nmsTarget = target != null ? ((CraftLivingEntity) target).getHandle() : null;
        ((CraftCreature) entity).getHandle().setGoalTarget(nmsTarget, EntityTargetEvent.TargetReason.CUSTOM, true);
        entity.setTarget(target);
    }
}
