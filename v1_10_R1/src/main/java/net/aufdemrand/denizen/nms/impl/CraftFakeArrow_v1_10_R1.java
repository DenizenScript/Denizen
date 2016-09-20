package net.aufdemrand.denizen.nms.impl;

import net.aufdemrand.denizen.nms.interfaces.FakeArrow;
import net.minecraft.server.v1_10_R1.EntityArrow;
import org.bukkit.craftbukkit.v1_10_R1.CraftServer;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftArrow;

public class CraftFakeArrow_v1_10_R1 extends CraftArrow implements FakeArrow {

    public CraftFakeArrow_v1_10_R1(CraftServer craftServer, EntityArrow entityArrow) {
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
