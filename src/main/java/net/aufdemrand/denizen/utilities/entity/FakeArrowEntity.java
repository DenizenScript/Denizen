package net.aufdemrand.denizen.utilities.entity;

import net.minecraft.server.v1_9_R1.EntitySpectralArrow;
import net.minecraft.server.v1_9_R1.ItemStack;
import net.minecraft.server.v1_9_R1.Items;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_9_R1.CraftServer;
import org.bukkit.craftbukkit.v1_9_R1.CraftWorld;

public class FakeArrowEntity extends EntitySpectralArrow {

    public FakeArrowEntity(CraftWorld craftWorld, Location location) {
        super(craftWorld.getHandle());
        setPositionRotation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        world.addEntity(this);
        bukkitEntity = new CraftFakeArrow((CraftServer) Bukkit.getServer(), this);
    }

    @Override
    public void m() {
        // Do nothing
    }

    @Override
    protected ItemStack j() {
        return new ItemStack(Items.ARROW);
    }
}
