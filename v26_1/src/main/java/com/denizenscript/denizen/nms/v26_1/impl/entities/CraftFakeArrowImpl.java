package com.denizenscript.denizen.nms.v26_1.impl.entities;

import com.denizenscript.denizen.nms.interfaces.FakeArrow;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftAbstractArrow;

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
