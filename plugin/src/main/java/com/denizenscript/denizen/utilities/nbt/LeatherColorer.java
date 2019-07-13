package com.denizenscript.denizen.utilities.nbt;

import com.denizenscript.denizen.utilities.debugging.dB;
import com.denizenscript.denizen.objects.dColor;
import com.denizenscript.denizen.objects.dItem;
import org.bukkit.inventory.meta.LeatherArmorMeta;

public class LeatherColorer {

    public static void colorArmor(dItem item, String colorArg) {

        if (item == null) {
            return;
        }

        if (dColor.matches(colorArg)) {

            try {
                LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemStack().getItemMeta();
                meta.setColor(dColor.valueOf(colorArg).getColor());
                item.getItemStack().setItemMeta(meta);
            }
            catch (Exception e) {
                dB.echoError("Unable to color '" + item.identify() + "'.");
            }
        }

    }
}
