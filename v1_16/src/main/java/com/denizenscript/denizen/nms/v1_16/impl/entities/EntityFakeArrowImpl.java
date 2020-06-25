package com.denizenscript.denizen.nms.v1_16.impl.entities;

import com.denizenscript.denizen.nms.v1_16.Handler;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.minecraft.server.v1_16_R1.EntitySpectralArrow;
import net.minecraft.server.v1_16_R1.EntityTypes;
import net.minecraft.server.v1_16_R1.ItemStack;
import net.minecraft.server.v1_16_R1.Items;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R1.CraftServer;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;

public class EntityFakeArrowImpl extends EntitySpectralArrow {

    public EntityFakeArrowImpl(CraftWorld craftWorld, Location location) {
        // TODO: 1.14 - provide a custom EntityTypes?
        super(EntityTypes.SPECTRAL_ARROW, craftWorld.getHandle());
        try {
            Handler.ENTITY_BUKKITYENTITY.set(this, new CraftFakeArrowImpl((CraftServer) Bukkit.getServer(), this));
        }
        catch (Exception ex) {
            Debug.echoError(ex);
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
    public CraftFakeArrowImpl getBukkitEntity() {
        return (CraftFakeArrowImpl) super.getBukkitEntity();
    }
}
