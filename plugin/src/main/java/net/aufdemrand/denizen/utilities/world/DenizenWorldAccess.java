package net.aufdemrand.denizen.utilities.world;

import net.aufdemrand.denizen.events.entity.EntityDespawnScriptEvent;
import net.aufdemrand.denizen.nms.interfaces.WorldAccess;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.scripts.commands.player.GlowCommand;
import net.aufdemrand.denizen.scripts.containers.core.EntityScriptHelper;
import net.aufdemrand.denizen.utilities.debugging.dB;
import com.denizenscript.denizencore.objects.Element;
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
            EntityDespawnScriptEvent.instance.cause = new Element("OTHER");
            EntityDespawnScriptEvent.instance.cancelled = false;
            EntityDespawnScriptEvent.instance.fire();
            dEntity.forgetEntity(entity);
        }
        catch (Exception e) {
            dB.echoError(e);
        }
        EntityScriptHelper.unlinkEntity(entity);
    }
}
