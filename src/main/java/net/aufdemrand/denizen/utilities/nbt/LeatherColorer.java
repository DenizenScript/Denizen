package net.aufdemrand.denizen.utilities.nbt;

import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.arguments.dItem;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.inventory.meta.LeatherArmorMeta;

public class LeatherColorer {

    public static void colorArmor(dItem item, String colorArg) {

        if (item == null) return;

        if (aH.matchesColor(colorArg)) {
        	
            try{
            	LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemStack().getItemMeta();
                meta.setColor(aH.getColorFrom(colorArg));
                item.getItemStack().setItemMeta(meta);
            } catch(Exception e) {
                dB.echoError("Unable to color '" + item.dScriptArgValue() + "'.");
            }
        }
    }
}
