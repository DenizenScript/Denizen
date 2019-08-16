package com.denizenscript.denizen.v1_12.impl.entities;

import net.minecraft.server.v1_12_R1.EntitySpectralArrow;
import net.minecraft.server.v1_12_R1.ItemStack;
import net.minecraft.server.v1_12_R1.Items;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;

public class EntityFakeArrow_v1_12_R1 extends EntitySpectralArrow {

    public EntityFakeArrow_v1_12_R1(CraftWorld craftWorld, Location location) {
        super(craftWorld.getHandle());
        bukkitEntity = new CraftFakeArrow_v1_12_R1((CraftServer) Bukkit.getServer(), this);
        setPositionRotation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        world.addEntity(this);
    }

    @Override
    public void B_() {
        // Do nothing
    }

    @Override
    protected ItemStack j() {
        return new ItemStack(Items.ARROW);
    }

    @Override
    public CraftFakeArrow_v1_12_R1 getBukkitEntity() {
        return (CraftFakeArrow_v1_12_R1) bukkitEntity;
    }
}
