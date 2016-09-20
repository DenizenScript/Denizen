package net.aufdemrand.denizen.nms.impl;

import net.minecraft.server.v1_10_R1.EntitySpectralArrow;
import net.minecraft.server.v1_10_R1.ItemStack;
import net.minecraft.server.v1_10_R1.Items;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_10_R1.CraftServer;
import org.bukkit.craftbukkit.v1_10_R1.CraftWorld;

public class EntityFakeArrow_v1_10_R1 extends EntitySpectralArrow {

    public EntityFakeArrow_v1_10_R1(CraftWorld craftWorld, Location location) {
        super(craftWorld.getHandle());
        setPositionRotation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        world.addEntity(this);
        bukkitEntity = new CraftFakeArrow_v1_10_R1((CraftServer) Bukkit.getServer(), this);
    }

    @Override
    public void m() {
        // Do nothing
    }

    @Override
    protected ItemStack j() {
        return new ItemStack(Items.ARROW);
    }

    @Override
    public CraftFakeArrow_v1_10_R1 getBukkitEntity() {
        return (CraftFakeArrow_v1_10_R1) bukkitEntity;
    }
}
