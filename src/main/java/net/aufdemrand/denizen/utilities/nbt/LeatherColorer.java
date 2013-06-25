package net.aufdemrand.denizen.utilities.nbt;

import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.inventory.meta.LeatherArmorMeta;

public class LeatherColorer {

    public static void colorArmor(dItem item, String colorArg) {

        if (item == null) return;

        /*
         * TODO: Create dColor object
         * 
        if (aH.matchesColor(colorArg)) {
            
            try{
                LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemStack().getItemMeta();
                meta.setColor(aH.getColorFrom(colorArg));
                item.getItemStack().setItemMeta(meta);
            } catch(Exception e) {
                dB.echoError("Unable to color '" + item.dScriptArgValue() + "'.");
            }
        }
        */
    }
}