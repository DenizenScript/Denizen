package net.aufdemrand.denizen.utilities.world;

import net.aufdemrand.denizen.events.entity.EntityDespawnScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.scripts.containers.core.EntityScriptHelper;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.objects.Element;
import net.minecraft.server.v1_9_R1.*;
import org.bukkit.entity.LivingEntity;

public class DenizenWorldAccess implements IWorldAccess {
    @Override
    public void a(World world, BlockPosition blockPosition, IBlockData iBlockData, IBlockData iBlockData1, int i) {

    }

    @Override
    public void a(BlockPosition blockPosition) {
    }

    @Override
    public void a(int i, int i1, int i2, int i3, int i4, int i5) {
    }

    @Override
    public void a(EntityHuman entityHuman, SoundEffect soundEffect, SoundCategory soundCategory, double v, double v1, double v2, float v3, float v4) {

    }

    @Override
    public void a(SoundEffect soundEffect, BlockPosition blockPosition) {

    }

    @Override
    public void a(int i, boolean b, double v, double v1, double v2, double v3, double v4, double v5, int... ints) {
    }

    @Override
    public void a(Entity entity) {

    }

    // Entity despawn
    @Override
    public void b(Entity entity) {
        try {
            if (dEntity.isCitizensNPC(entity.getBukkitEntity())) {
                return;
            }
            if (entity.getBukkitEntity() instanceof LivingEntity && !((LivingEntity) entity.getBukkitEntity()).getRemoveWhenFarAway()) {
                return;
            }
            EntityDespawnScriptEvent.instance.entity = new dEntity(entity.getBukkitEntity());
            EntityDespawnScriptEvent.instance.cause = new Element("OTHER");
            EntityDespawnScriptEvent.instance.cancelled = false;
            EntityDespawnScriptEvent.instance.fire();
        }
        catch (Exception e) {
            dB.echoError(e);
        }
        EntityScriptHelper.unlinkEntity(entity.getBukkitEntity());
    }

    @Override
    public void a(int i, BlockPosition blockPosition, int i1) {

    }

    @Override
    public void a(EntityHuman entityHuman, int i, BlockPosition blockPosition, int i1) {
    }

    @Override
    public void b(int i, BlockPosition blockPosition, int i1) {

    }
}
