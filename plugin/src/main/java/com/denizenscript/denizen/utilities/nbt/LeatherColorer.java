package com.denizenscript.denizen.utilities.nbt;

import com.denizenscript.denizen.objects.properties.bukkit.BukkitColorExtensions;
import com.denizenscript.denizencore.objects.core.ColorTag;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.inventory.meta.LeatherArmorMeta;

public class LeatherColorer {

    public static void colorArmor(ItemTag item, String colorArg) {
        if (item == null) {
            return;
        }
        if (ColorTag.matches(colorArg)) {
            try {
                LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
                meta.setColor(BukkitColorExtensions.getColor(ColorTag.valueOf(colorArg, CoreUtilities.basicContext)));
                item.setItemMeta(meta);
            }
            catch (Exception e) {
                Debug.echoError("Unable to color '" + item.identify() + "'.");
            }
        }
    }
}
