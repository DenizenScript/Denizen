package com.denizenscript.denizen.nms.v1_19.impl.entities;

import com.denizenscript.denizen.nms.interfaces.FakeArrow;
import net.minecraft.world.entity.projectile.AbstractArrow;
import org.bukkit.craftbukkit.v1_19_R3.CraftServer;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftArrow;

public class CraftFakeArrowImpl extends CraftArrow implements FakeArrow {

    public CraftFakeArrowImpl(CraftServer craftServer, AbstractArrow entityArrow) {
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
