package com.denizenscript.denizen.utilities.world;

import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.events.entity.EntityDespawnScriptEvent;
import com.denizenscript.denizen.nms.interfaces.WorldAccess;
import com.denizenscript.denizen.objects.dEntity;
import com.denizenscript.denizen.scripts.commands.player.GlowCommand;
import com.denizenscript.denizen.scripts.containers.core.EntityScriptHelper;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public class DenizenWorldAccess implements WorldAccess {

    @Override
    public void despawn(Entity entity) {
        try {
            if (entity instanceof LivingEntity) {
                GlowCommand.unGlow((LivingEntity) entity);
            }
            if (dEntity.isCitizensNPC(entity)) {
                return;
            }
            if (entity instanceof LivingEntity && !((LivingEntity) entity).getRemoveWhenFarAway()) {
                return;
            }
            dEntity.rememberEntity(entity);
            EntityDespawnScriptEvent.instance.entity = new dEntity(entity);
            EntityDespawnScriptEvent.instance.cause = new ElementTag("OTHER");
            EntityDespawnScriptEvent.instance.cancelled = false;
            EntityDespawnScriptEvent.instance.fire();
            dEntity.forgetEntity(entity);
        }
        catch (Exception e) {
            Debug.echoError(e);
        }
        EntityScriptHelper.unlinkEntity(entity);
    }
}
