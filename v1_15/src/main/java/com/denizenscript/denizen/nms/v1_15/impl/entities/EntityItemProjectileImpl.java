package com.denizenscript.denizen.nms.v1_15.impl.entities;

import com.denizenscript.denizen.nms.v1_15.Handler;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.minecraft.server.v1_15_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_15_R1.CraftServer;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_15_R1.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;

import java.util.Iterator;
import java.util.UUID;

public class EntityItemProjectileImpl extends EntityItem implements IProjectile {

    public Entity shooter;
    public UUID shooterId;
    public Entity c;
    private int aw;

    public boolean inGround;
    public int shake;

    public EntityItemProjectileImpl(CraftWorld craftWorld, Location location, org.bukkit.inventory.ItemStack itemStack) {
        super(EntityTypes.ITEM, craftWorld.getHandle());
        try {
            Handler.ENTITY_BUKKITYENTITY.set(this, new CraftItemProjectileImpl((CraftServer) Bukkit.getServer(), this));
        }
        catch (Exception ex) {
            Debug.echoError(ex);
        }
        this.pickupDelay = Integer.MAX_VALUE;
        setPositionRotation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        //this.setSize(0.25F, 0.25F); as of 1.14, part of EntityTypes
        this.setItemStack(CraftItemStack.asNMSCopy(itemStack));
        world.addEntity(this);
    }

    @Override
    public void tick() {
        this.E = this.locX();
        this.F = this.locY();
        this.G = this.locZ();
        super.tick();
        if (this.shake > 0) {
            --this.shake;
        }

        if (this.inGround) {
            this.inGround = false;
            this.setMot(this.getMot().d((double) (this.random.nextFloat() * 0.2F), (double) (this.random.nextFloat() * 0.2F), (double) (this.random.nextFloat() * 0.2F)));
        }

        AxisAlignedBB axisalignedbb = this.getBoundingBox().a(this.getMot()).g(1.0D);
        Iterator iterator = this.world.getEntities(this, axisalignedbb, (entityx) -> {
            return !entityx.isAlive() && entityx.isInteractable();
        }).iterator();

        while (iterator.hasNext()) {
            Entity entity = (Entity) iterator.next();
            if (entity == this.c) {
                ++this.aw;
                break;
            }

            if (this.shooter != null && this.ticksLived < 2 && this.c == null && this.shooter == entity) {
                this.c = entity;
                this.aw = 3;
                break;
            }
        }

        MovingObjectPosition movingobjectposition = ProjectileHelper.a(this, axisalignedbb, (entity1) -> {
            return !entity1.isAlive() && entity1.isInteractable() && entity1 != this.c;
        }, RayTrace.BlockCollisionOption.OUTLINE, true);
        if (this.c != null && this.aw-- <= 0) {
            this.c = null;
        }

        if (movingobjectposition.getType() != MovingObjectPosition.EnumMovingObjectType.MISS) {
            if (movingobjectposition.getType() == MovingObjectPosition.EnumMovingObjectType.BLOCK && this.world.getType(((MovingObjectPositionBlock) movingobjectposition).getBlockPosition()).getBlock() == Blocks.NETHER_PORTAL) {
                this.c(((MovingObjectPositionBlock) movingobjectposition).getBlockPosition());
            }
            else {
                this.a(movingobjectposition);
                if (this.dead) {
                    CraftEventFactory.callProjectileHitEvent(this, movingobjectposition);
                }
            }
        }

        Vec3D vec3d = this.getMot();
        this.setPositionRaw(this.locX() + vec3d.x, this.locY() + vec3d.y, this.locZ() + vec3d.z);
        float f = MathHelper.sqrt(b(vec3d));
        this.yaw = (float) (MathHelper.d(vec3d.x, vec3d.z) * 57.2957763671875D);

        // TODO: what is this???
        for (this.pitch = (float) (MathHelper.d(vec3d.y, (double) f) * 57.2957763671875D); this.pitch - this.lastPitch < -180.0F; this.lastPitch -= 360.0F) {
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

        this.pitch = MathHelper.g(0.2F, this.lastPitch, this.pitch);
        this.yaw = MathHelper.g(0.2F, this.lastYaw, this.yaw);
        float f1;
        if (this.isInWater()) {
            for (int i = 0; i < 4; ++i) {
                float f2 = 0.25F;
                this.world.addParticle(Particles.BUBBLE, this.locX() - vec3d.x * 0.25D, this.locY() - vec3d.y * 0.25D, this.locZ() - vec3d.z * 0.25D, vec3d.x, vec3d.y, vec3d.z);
            }

            f1 = 0.8F;
        }
        else {
            f1 = 0.99F;
        }

        this.setMot(vec3d.a((double) f1));
        if (!this.isNoGravity()) {
            Vec3D vec3d1 = this.getMot();
            this.setMot(vec3d1.x, vec3d1.y - 0.03, vec3d1.z);
        }

        this.setPosition(this.locX(), this.locY(), this.locZ());
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
        setMot(d0, d1, d2);
        float f3 = MathHelper.sqrt(d0 * d0 + d2 * d2);

        lastYaw = yaw = (float) (Math.atan2(d0, d2) * 180.0D / 3.1415927410125732D);
        lastPitch = pitch = (float) (Math.atan2(d1, f3) * 180.0D / 3.1415927410125732D);
    }

    protected void a(MovingObjectPosition var1) {
        if (var1 instanceof MovingObjectPositionEntity) {
            ((MovingObjectPositionEntity) var1).getEntity().damageEntity(DamageSource.projectile(this, this.getShooter()), 0);
        }
        this.die();
    }

    public Entity getShooter() {
        if (this.shooter == null && this.shooterId != null && this.world instanceof WorldServer) {
            Entity entity = ((WorldServer) this.world).getEntity(this.shooterId);
            if (entity instanceof EntityLiving) {
                this.shooter = entity;
            }
            else {
                this.shooterId = null;
            }
        }
        return this.shooter;
    }

    @Override
    public CraftItemProjectileImpl getBukkitEntity() {
        return (CraftItemProjectileImpl) super.getBukkitEntity();
    }
}
