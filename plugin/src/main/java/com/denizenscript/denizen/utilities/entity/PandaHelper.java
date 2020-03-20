package com.denizenscript.denizen.utilities.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.entity.Panda;

public class PandaHelper {

    public static String getColor(EntityTag pandaTag) {
        Panda panda = (Panda) pandaTag.getBukkitEntity();
        return panda.getMainGene().name() + "|" + panda.getHiddenGene().name();
    }

    public static void setColor(EntityTag pandaTag, String color) {
        Panda panda = (Panda) pandaTag.getBukkitEntity();
        ListTag list = ListTag.valueOf(color, CoreUtilities.basicContext);
        panda.setMainGene(Panda.Gene.valueOf(list.get(0).toUpperCase()));
        panda.setHiddenGene(Panda.Gene.valueOf(list.get(1).toUpperCase()));
    }
}
