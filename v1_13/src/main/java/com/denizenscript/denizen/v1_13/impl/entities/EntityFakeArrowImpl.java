package com.denizenscript.denizen.v1_13.impl.entities;

import net.minecraft.server.v1_13_R2.EntitySpectralArrow;
import net.minecraft.server.v1_13_R2.ItemStack;
import net.minecraft.server.v1_13_R2.Items;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_13_R2.CraftServer;
import org.bukkit.craftbukkit.v1_13_R2.CraftWorld;

public class EntityFakeArrowImpl extends EntitySpectralArrow {

    public EntityFakeArrowImpl(CraftWorld craftWorld, Location location) {
        super(craftWorld.getHandle());
        bukkitEntity = new CraftFakeArrowImpl((CraftServer) Bukkit.getServer(), this);
        setPositionRotation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        world.addEntity(this);
    }

    @Override
    public void tick() {
        // Do nothing
    }

    @Override
    protected ItemStack getItemStack() {
        return new ItemStack(Items.ARROW);
    }

    @Override
    public CraftFakeArrowImpl getBukkitEntity() {
        return (CraftFakeArrowImpl) bukkitEntity;
    }
}
