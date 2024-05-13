package com.denizenscript.denizen.nms.v1_20.impl.entities;

import com.denizenscript.denizen.nms.v1_20.Handler;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.minecraft.world.entity.projectile.SpectralArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R4.CraftServer;
import org.bukkit.craftbukkit.v1_20_R4.CraftWorld;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class EntityFakeArrowImpl extends SpectralArrow {

    public EntityFakeArrowImpl(CraftWorld craftWorld, Location location) {
        super(net.minecraft.world.entity.EntityType.SPECTRAL_ARROW, craftWorld.getHandle());
        try {
            Handler.ENTITY_BUKKITYENTITY.set(this, new CraftFakeArrowImpl((CraftServer) Bukkit.getServer(), this));
        }
        catch (Exception ex) {
            Debug.echoError(ex);
        }
        setPosRaw(location.getX(), location.getY(), location.getZ());
        setRot(location.getYaw(), location.getPitch());
        level().addFreshEntity(this, CreatureSpawnEvent.SpawnReason.CUSTOM);
    }

    @Override
    public void tick() {
        // Do nothing
    }

    @Override
    protected ItemStack getPickupItem() {
        return new ItemStack(Items.ARROW);
    }

    @Override
    public CraftFakeArrowImpl getBukkitEntity() {
        return (CraftFakeArrowImpl) super.getBukkitEntity();
    }
}
