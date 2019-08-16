package com.denizenscript.denizen.nms.v1_13.impl.entities;

import com.denizenscript.denizen.nms.interfaces.FakeArrow;
import net.minecraft.server.v1_13_R2.EntityArrow;
import org.bukkit.craftbukkit.v1_13_R2.CraftServer;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftArrow;

public class CraftFakeArrowImpl extends CraftArrow implements FakeArrow {

    public CraftFakeArrowImpl(CraftServer craftServer, EntityArrow entityArrow) {
        super(craftServer, entityArrow);
    }

    @Override
    public void remove() {
        if (getPassenger() != null) {
            return;
        }
        super.remove();
    }

    @Override
    public String getEntityTypeName() {
        return "FAKE_ARROW";
    }
}
