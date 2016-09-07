package net.aufdemrand.denizen.utilities.world;

import net.aufdemrand.denizen.events.entity.EntityDespawnScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.scripts.commands.player.GlowCommand;
import net.aufdemrand.denizen.scripts.containers.core.EntityScriptHelper;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.objects.Element;
import net.minecraft.server.v1_10_R1.*;
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
            org.bukkit.entity.Entity bukkitEntity = entity.getBukkitEntity();
            if (bukkitEntity instanceof LivingEntity) {
                GlowCommand.unGlow((LivingEntity) bukkitEntity);
            }
            if (dEntity.isCitizensNPC(bukkitEntity)) {
                return;
            }
            if (bukkitEntity instanceof LivingEntity && !((LivingEntity) bukkitEntity).getRemoveWhenFarAway()) {
                return;
            }
            dEntity.rememberEntity(bukkitEntity);
            EntityDespawnScriptEvent.instance.entity = new dEntity(bukkitEntity);
            EntityDespawnScriptEvent.instance.cause = new Element("OTHER");
            EntityDespawnScriptEvent.instance.cancelled = false;
            EntityDespawnScriptEvent.instance.fire();
            dEntity.forgetEntity(bukkitEntity);
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
