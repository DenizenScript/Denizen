package net.aufdemrand.denizen.utilities.nbt;

import net.aufdemrand.denizen.objects.dColor;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.inventory.meta.LeatherArmorMeta;

public class LeatherColorer {

    public static void colorArmor(dItem item, String colorArg) {

        if (item == null) return;

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
