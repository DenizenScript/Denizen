package com.denizenscript.denizen.nms.v1_12.impl.entities;

import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;

import java.util.List;

public class EntityItemProjectileImpl extends EntityItem implements IProjectile {

    public Entity shooter;
    public String shooterName;
    public Entity c;
    private int aw;

    public EntityItemProjectileImpl(CraftWorld craftWorld, Location location, org.bukkit.inventory.ItemStack itemStack) {
        super(craftWorld.getHandle());
        bukkitEntity = new CraftItemProjectileImpl((CraftServer) Bukkit.getServer(), this);
        this.pickupDelay = Integer.MAX_VALUE;
        setPositionRotation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        this.setSize(0.25F, 0.25F);
        this.setItemStack(CraftItemStack.asNMSCopy(itemStack));
        world.addEntity(this);
    }

    @Override
    public void B_() {
        this.M = this.locX;
        this.N = this.locY;
        this.O = this.locZ;
        super.Y();

        Vec3D vec3d = new Vec3D(this.locX, this.locY, this.locZ);
        Vec3D vec3d1 = new Vec3D(this.locX + this.motX, this.locY + this.motY, this.locZ + this.motZ);
        MovingObjectPosition movingobjectposition = this.world.rayTrace(vec3d, vec3d1);
        vec3d = new Vec3D(this.locX, this.locY, this.locZ);
        vec3d1 = new Vec3D(this.locX + this.motX, this.locY + this.motY, this.locZ + this.motZ);
        if (movingobjectposition != null) {
            vec3d1 = new Vec3D(movingobjectposition.pos.x, movingobjectposition.pos.y, movingobjectposition.pos.z);
        }

        Entity entity = null;
        List list = this.world.getEntities(this, this.getBoundingBox().b(this.motX, this.motY, this.motZ).g(1.0D));
        double d0 = 0.0D;
        boolean flag = false;

        for (Object aList : list) {
            Entity f1 = (Entity) aList;

            if (f1.isInteractable()) {
                if (f1 == this.c) {
                    flag = true;
                }
                else if (this.shooter != null && this.ticksLived < 2 && this.c == null) {
                    this.c = f1;
                    flag = true;
                }
                else {
                    flag = false;
                    AxisAlignedBB f2 = f1.getBoundingBox().g(0.30000001192092896D);
                    MovingObjectPosition j = f2.b(vec3d, vec3d1);
                    if (j != null) {
                        double d1 = vec3d.distanceSquared(j.pos);
                        if (d1 < d0 || d0 == 0.0D) {
                            entity = f1;
                            d0 = d1;
                        }
                    }
                }
            }
        }

        if (this.c != null) {
            if (flag) {
                this.aw = 2;
            }
            else if (this.aw-- <= 0) {
                this.c = null;
            }
        }

        if (entity != null) {
            movingobjectposition = new MovingObjectPosition(entity);
        }

        if (movingobjectposition != null) {
            if (movingobjectposition.type == MovingObjectPosition.EnumMovingObjectType.BLOCK && this.world.getType(movingobjectposition.a()).getBlock() == Blocks.PORTAL) {
                this.e(movingobjectposition.a());
            }
            else {
                this.a(movingobjectposition);
                if (this.dead) {
                    CraftEventFactory.callProjectileHitEvent(this, movingobjectposition);
                }
            }
        }

        this.locX += this.motX;
        this.locY += this.motY;
        this.locZ += this.motZ;
        float var15 = MathHelper.sqrt(this.motX * this.motX + this.motZ * this.motZ);
        this.pitch = (float) (MathHelper.c(this.motY, (double) var15) * 57.2957763671875D);
        this.yaw = (float) (MathHelper.c(this.motX, this.motZ) * 57.2957763671875D);

        while (this.pitch - this.lastPitch < -180.0F) {
            this.lastPitch -= 360.0F;
        }

        while (this.pitch - this.lastPitch >= 180.0F) {
            this.lastPitch += 360.0F;
        }

        while (this.yaw - this.lastYaw < -180.0F) {
            this.lastYaw -= 360.0F;
        }

        while (this.yaw - this.lastYaw >= 180.0F) {
            this.lastYaw += 360.0F;
        }

        this.pitch = this.lastPitch + (this.pitch - this.lastPitch) * 0.2F;
        this.yaw = this.lastYaw + (this.yaw - this.lastYaw) * 0.2F;
        float var16 = 0.99F;
        float var17 = 0.03F;
        if (this.isInWater()) {
            for (int var18 = 0; var18 < 4; ++var18) {
                this.world.addParticle(EnumParticle.WATER_BUBBLE, this.locX - this.motX * 0.25D, this.locY - this.motY * 0.25D, this.locZ - this.motZ * 0.25D, this.motX, this.motY, this.motZ);
            }

            var16 = 0.8F;
        }

        this.motX *= (double) var16;
        this.motY *= (double) var16;
        this.motZ *= (double) var16;
        if (!this.isNoGravity()) {
            this.motY -= (double) var17;
        }

        this.setPosition(this.locX, this.locY, this.locZ);
        checkBlockCollisions();
    }

    @Override
    public void shoot(double d0, double d1, double d2, float f, float f1) {
        float f2 = MathHelper.sqrt(d0 * d0 + d1 * d1 + d2 * d2);

        d0 /= f2;
        d1 /= f2;
        d2 /= f2;
        d0 += random.nextGaussian() * 0.007499999832361937D * f1;
        d1 += random.nextGaussian() * 0.007499999832361937D * f1;
        d2 += random.nextGaussian() * 0.007499999832361937D * f1;
        d0 *= f;
        d1 *= f;
        d2 *= f;
        motX = d0;
        motY = d1;
        motZ = d2;
        float f3 = MathHelper.sqrt(d0 * d0 + d2 * d2);

        lastYaw = yaw = (float) (Math.atan2(d0, d2) * 180.0D / 3.1415927410125732D);
        lastPitch = pitch = (float) (Math.atan2(d1, f3) * 180.0D / 3.1415927410125732D);
    }

    protected void a(MovingObjectPosition var1) {
        if (var1.entity != null) {
            var1.entity.damageEntity(DamageSource.projectile(this, this.getShooter()), 0);
        }
        this.die();
    }

    public Entity getShooter() {
        if (this.shooter == null && this.shooterName != null && this.shooterName.length() > 0) {
            this.shooter = this.world.a(this.shooterName);
        }

        return this.shooter;
    }

    @Override
    public CraftItemProjectileImpl getBukkitEntity() {
        return (CraftItemProjectileImpl) bukkitEntity;
    }
}
