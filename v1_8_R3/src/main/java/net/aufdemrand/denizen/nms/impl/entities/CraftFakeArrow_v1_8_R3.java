package net.aufdemrand.denizen.nms.impl.entities;

import net.aufdemrand.denizen.nms.interfaces.FakeArrow;
import net.minecraft.server.v1_8_R3.EntityArrow;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftArrow;
import org.bukkit.entity.LivingEntity;

public class CraftFakeArrow_v1_8_R3 extends CraftArrow implements FakeArrow {

    public CraftFakeArrow_v1_8_R3(CraftServer craftServer, EntityArrow entityArrow) {
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

    @Override
    public LivingEntity _INVALID_getShooter() {
        return null;
    }

    @Override
    public void _INVALID_setShooter(LivingEntity livingEntity) {
    }
}
