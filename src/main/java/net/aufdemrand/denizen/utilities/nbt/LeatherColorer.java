package net.aufdemrand.denizen.utilities.nbt;

import net.aufdemrand.denizen.utilities.arguments.dItem;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.Color;
import org.bukkit.inventory.meta.LeatherArmorMeta;

public class LeatherColorer {

    // Thanks GnomeffinWay in #denizen-dev

    public static void colorArmor(dItem item, String colorArg) {

        if (item == null) return;

        LeatherArmorMeta meta;

        int red = -1;
        int green = -1;
        int blue = -1;

        String[] regex = colorConvert(colorArg).split(":");

        // Try to parse the color
        try{
            if (regex.length == 3) {
                red = Integer.parseInt(regex[0]);
                green =Integer.parseInt(regex[1]);
                blue = Integer.parseInt(regex[2]);
            }
        } catch (Exception e) {
            dB.echoError("Unable to color '" + item.dScriptArgValue() + "'");
            return;
        }

        // Try to apply the color
        try{
            meta = (LeatherArmorMeta) item.getItemStack().getItemMeta();
            meta.setColor(Color.fromRGB(red, green, blue));
            item.getItemStack().setItemMeta(meta);
        } catch(Exception e) {
            dB.echoError("Unable to color '" + item.dScriptArgValue() + "'.");
        }
    }

    private static String colorConvert(String str){
        if(str.toUpperCase().equals("RED"))
            return "153:51:51";
        if(str.toUpperCase().equals("BLACK"))
            return "25:25:25";
        if(str.toUpperCase().equals("GREEN"))
            return "102:127:51";
        if(str.toUpperCase().equals("BROWN"))
            return "127:102:76";
        if(str.toUpperCase().equals("BLUE"))
            return "51:76:178";
        if(str.toUpperCase().equals("PURPLE"))
            return "127:63:178";
        if(str.toUpperCase().equals("CYAN"))
            return "76:127:153";
        if(str.toUpperCase().equals("LIGHTGRAY"))
            return "153:153:153";
        if(str.toUpperCase().equals("GRAY"))
            return "76:76:76";
        if(str.toUpperCase().equals("LIGHTGREY"))
            return "153:153:153";
        if(str.toUpperCase().equals("GREY"))
            return "76:76:76";
        if(str.toUpperCase().equals("PINK"))
            return "242:127:165";
        if(str.toUpperCase().equals("LIME"))
            return "127:204:25";
        if(str.toUpperCase().equals("YELLOW"))
            return "229:229:51";
        if(str.toUpperCase().equals("LIGHTBLUE"))
            return "102:153:216";
        if(str.toUpperCase().equals("MAGENTA"))
            return "178:76:216";
        if(str.toUpperCase().equals("ORANGE"))
            return "216:127:51";
        if(str.toUpperCase().equals("WHITE"))
            return "255:255:255";
        return str;
    }

}
