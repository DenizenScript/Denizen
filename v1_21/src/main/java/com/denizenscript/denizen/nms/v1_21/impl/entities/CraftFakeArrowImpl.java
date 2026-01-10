package com.denizenscript.denizen.nms.v1_21.impl.entities;

import com.denizenscript.denizen.nms.interfaces.FakeArrow;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import org.bukkit.craftbukkit.v1_21_R7.CraftServer;
import org.bukkit.craftbukkit.v1_21_R7.entity.CraftAbstractArrow;

public class CraftFakeArrowImpl extends CraftAbstractArrow implements FakeArrow {

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
