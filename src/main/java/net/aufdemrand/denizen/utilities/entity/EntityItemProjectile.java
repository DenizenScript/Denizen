package net.aufdemrand.denizen.utilities.entity;

import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;

import java.util.List;

public class EntityItemProjectile extends EntityItem implements IProjectile {

    public EntityLiving shooter;
    public String shooterName;
    private int age;

    public EntityItemProjectile(CraftWorld craftWorld, Location location, org.bukkit.inventory.ItemStack itemStack) {
        super(craftWorld.getHandle());
        this.pickupDelay = Integer.MAX_VALUE;
        setPositionRotation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        this.setSize(0.25F, 0.25F);
        this.setItemStack(CraftItemStack.asNMSCopy(itemStack));
        world.addEntity(this);
        bukkitEntity = new CraftItemProjectile((CraftServer) Bukkit.getServer(), this);
    }

    @Override
    public void t_() {
        K();
        BlockPosition blockposition = new BlockPosition(locX, locY, locZ);
        IBlockData iblockdata = world.getType(blockposition);
        Block block = iblockdata.getBlock();

        if (block.getMaterial() != Material.AIR) {
            AxisAlignedBB axisalignedbb = block.a(world, blockposition, iblockdata);

            if ((axisalignedbb != null) && (axisalignedbb.a(new Vec3D(locX, locY, locZ)))) {
                CraftEventFactory.callProjectileHitEvent(this);
                this.die();
            }
        }
        age += 1;
        Vec3D vec3d = new Vec3D(locX, locY, locZ);
        Vec3D vec3d1 = new Vec3D(locX + motX, locY + motY, locZ + motZ);
        MovingObjectPosition movingobjectposition = world.rayTrace(vec3d, vec3d1, false, true, false);

        vec3d = new Vec3D(locX, locY, locZ);
        vec3d1 = new Vec3D(locX + motX, locY + motY, locZ + motZ);
        if (movingobjectposition != null) {
            vec3d1 = new Vec3D(movingobjectposition.pos.a, movingobjectposition.pos.b, movingobjectposition.pos.c);
        }

        Entity entity = null;
        List list = world.getEntities(this, getBoundingBox().a(motX, motY, motZ).grow(1.0D, 1.0D, 1.0D));
        double d0 = 0.0D;

        for (Object aList : list) {
            Entity entity1 = (Entity) aList;

            if ((entity1.ad()) && ((entity1 != shooter) || (age >= 5))) {
                float f1 = 0.3F;
                AxisAlignedBB axisalignedbb1 = entity1.getBoundingBox().grow(f1, f1, f1);
                MovingObjectPosition movingobjectposition1 = axisalignedbb1.a(vec3d, vec3d1);

                if (movingobjectposition1 != null) {
                    double d1 = vec3d.distanceSquared(movingobjectposition1.pos);

                    if ((d1 < d0) || (d0 == 0.0D)) {
                        entity = entity1;
                        d0 = d1;
                    }
                }
            }
        }
        if (entity != null) {
            movingobjectposition = new MovingObjectPosition(entity);
        }
        if ((movingobjectposition != null) && (movingobjectposition.entity != null) && ((movingobjectposition.entity instanceof EntityHuman))) {
            EntityHuman entityhuman = (EntityHuman) movingobjectposition.entity;
            if ((entityhuman.abilities.isInvulnerable) || (((shooter instanceof EntityHuman)) && (!((EntityHuman) shooter).a(entityhuman)))) {
                movingobjectposition = null;
            }
        }
        if (movingobjectposition != null) {
            if (movingobjectposition.entity != null && movingobjectposition.entity instanceof EntityLiving) {
                movingobjectposition.entity.damageEntity(DamageSource.projectile(this, this.getShooter()), 0F);
                this.die();
            }
            else if (movingobjectposition.a() != null) {
                if (block.getMaterial() != Material.AIR) {
                    motX = ((float) (movingobjectposition.pos.a - locX));
                    motY = ((float) (movingobjectposition.pos.b - locY));
                    motZ = ((float) (movingobjectposition.pos.c - locZ));
                    float f3 = MathHelper.sqrt(motX * motX + motY * motY + motZ * motZ);
                    locX -= motX / f3 * 0.0500000007450581D;
                    locY -= motY / f3 * 0.0500000007450581D;
                    locZ -= motZ / f3 * 0.0500000007450581D;
                    CraftEventFactory.callProjectileHitEvent(this);
                    this.die();
                }
            }
        }

        locX += motX;
        locY += motY;
        locZ += motZ;
        float f3 = 0.99F;
        float f1 = 0.05F;
        motX *= f3;
        motY *= f3;
        motZ *= f3;
        motY -= f1;
        setPosition(locX, locY, locZ);
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

    public EntityLiving getShooter() {
        if (this.shooter == null && this.shooterName != null && this.shooterName.length() > 0) {
            this.shooter = this.world.a(this.shooterName);
        }

        return this.shooter;
    }
}
