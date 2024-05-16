package com.denizenscript.denizen.nms.v1_20.impl.entities;

import com.denizenscript.denizen.nms.interfaces.FakeArrow;
import net.minecraft.world.entity.projectile.AbstractArrow;
import org.bukkit.craftbukkit.v1_20_R4.CraftServer;
import org.bukkit.craftbukkit.v1_20_R4.entity.CraftAbstractArrow;
import org.bukkit.craftbukkit.v1_20_R4.entity.CraftArrow;

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
