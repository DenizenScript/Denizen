package com.denizenscript.denizen.utilities;

import org.bukkit.block.Sign;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import org.bukkit.block.sign.Side;

public class MultiVersionHelper1_20 {
    public static String[][] getSignLines(Sign sign) {
        String[][] contents = new String[2][];
        contents[0] = sign.getSide(Side.FRONT).getLines();
        contents[1] = sign.getSide(Side.BACK).getLines();
        return contents;
    }
}
