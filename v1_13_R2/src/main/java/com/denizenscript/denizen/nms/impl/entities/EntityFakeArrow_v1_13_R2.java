package com.denizenscript.denizen.nms.impl.entities;

import net.minecraft.server.v1_13_R2.EntitySpectralArrow;
import net.minecraft.server.v1_13_R2.ItemStack;
import net.minecraft.server.v1_13_R2.Items;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_13_R2.CraftServer;
import org.bukkit.craftbukkit.v1_13_R2.CraftWorld;

public class EntityFakeArrow_v1_13_R2 extends EntitySpectralArrow {

    public EntityFakeArrow_v1_13_R2(CraftWorld craftWorld, Location location) {
        super(craftWorld.getHandle());
        bukkitEntity = new CraftFakeArrow_v1_13_R2((CraftServer) Bukkit.getServer(), this);
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
    public CraftFakeArrow_v1_13_R2 getBukkitEntity() {
        return (CraftFakeArrow_v1_13_R2) bukkitEntity;
    }
}
