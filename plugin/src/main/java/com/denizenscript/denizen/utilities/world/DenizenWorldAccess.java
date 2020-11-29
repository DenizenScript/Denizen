package com.denizenscript.denizen.utilities.world;

import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.events.entity.EntityDespawnScriptEvent;
import com.denizenscript.denizen.nms.interfaces.WorldAccess;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.scripts.commands.player.GlowCommand;
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
            if (EntityTag.isCitizensNPC(entity)) {
                return;
            }
            if (entity instanceof LivingEntity && !((LivingEntity) entity).getRemoveWhenFarAway()) {
                return;
            }
            EntityTag.rememberEntity(entity);
            EntityDespawnScriptEvent.instance.entity = new EntityTag(entity);
            EntityDespawnScriptEvent.instance.cause = new ElementTag("OTHER");
            EntityDespawnScriptEvent.instance.cancelled = false;
            EntityDespawnScriptEvent.instance.fire();
            EntityTag.forgetEntity(entity);
        }
        catch (Exception e) {
            Debug.echoError(e);
        }
    }
}
