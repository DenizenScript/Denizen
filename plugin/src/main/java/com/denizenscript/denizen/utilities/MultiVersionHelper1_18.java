package com.denizenscript.denizen.utilities;

import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.entity.Player;

public class MultiVersionHelper1_18 {

    public static ElementTag getSkinModel(Player player) {
        return new ElementTag(player.getPlayerProfile().getTextures().getSkinModel());
    }
}
