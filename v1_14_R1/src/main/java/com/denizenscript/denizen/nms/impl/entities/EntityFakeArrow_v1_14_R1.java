package com.denizenscript.denizen.nms.impl.entities;

import com.denizenscript.denizen.nms.Handler_v1_14_R1;
import com.denizenscript.denizencore.utilities.debugging.dB;
import net.minecraft.server.v1_14_R1.EntitySpectralArrow;
import net.minecraft.server.v1_14_R1.EntityTypes;
import net.minecraft.server.v1_14_R1.ItemStack;
import net.minecraft.server.v1_14_R1.Items;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_14_R1.CraftServer;
import org.bukkit.craftbukkit.v1_14_R1.CraftWorld;

public class EntityFakeArrow_v1_14_R1 extends EntitySpectralArrow {

    public EntityFakeArrow_v1_14_R1(CraftWorld craftWorld, Location location) {
        // TODO: 1.14 - provide a custom EntityTypes?
        super(EntityTypes.SPECTRAL_ARROW, craftWorld.getHandle());
        try {
            Handler_v1_14_R1.ENTITY_BUKKITYENTITY.set(this, new CraftFakeArrow_v1_14_R1((CraftServer) Bukkit.getServer(), this));
        }
        catch (Exception ex) {
            dB.echoError(ex);
        }
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
    public CraftFakeArrow_v1_14_R1 getBukkitEntity() {
        return (CraftFakeArrow_v1_14_R1) super.getBukkitEntity();
    }
}
