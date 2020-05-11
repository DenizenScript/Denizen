package com.denizenscript.denizen.paper.events;

import com.denizenscript.denizen.events.entity.EntityShootsBowEvent;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.utilities.DenizenAPI;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class EntityShootsBowPaperScriptEventImpl extends EntityShootsBowEvent {

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        if (determinationObj instanceof ElementTag && !isDefaultDetermination(determinationObj)) {
            String determination = determinationObj.toString();
            String lower = CoreUtilities.toLowerCase(determination);
            if (lower.equals("keep_item")) {
                event.setConsumeArrow(false);
                if (entity.isPlayer()) {
                    final Player p = entity.getPlayer();
                    Bukkit.getScheduler().scheduleSyncDelayedTask(DenizenAPI.getCurrentInstance(), new Runnable() {
                        @Override
                        public void run() {
                            p.updateInventory();
                        }
                    }, 1);
                }
                return true;
            }
        }
        return super.applyDetermination(path, determinationObj);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("item")) {
            return new ItemTag(event.getArrowItem());
        }
        return super.getContext(name);
    }
}
