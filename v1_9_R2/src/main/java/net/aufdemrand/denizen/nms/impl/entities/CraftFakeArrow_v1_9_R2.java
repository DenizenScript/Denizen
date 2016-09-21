package net.aufdemrand.denizen.nms.impl.entities;

import net.aufdemrand.denizen.nms.interfaces.FakeArrow;
import net.minecraft.server.v1_9_R2.EntityArrow;
import org.bukkit.craftbukkit.v1_9_R2.CraftServer;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftArrow;

public class CraftFakeArrow_v1_9_R2 extends CraftArrow implements FakeArrow {

    public CraftFakeArrow_v1_9_R2(CraftServer craftServer, EntityArrow entityArrow) {
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
