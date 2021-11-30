package com.denizenscript.denizen.nms.v1_18.impl.network.handlers;

import com.denizenscript.denizen.utilities.blocks.FakeBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.craftbukkit.v1_18_R1.block.data.CraftBlockData;

public class FakeBlockHelper {

    public static BlockState getNMSState(FakeBlock block) {
        return ((CraftBlockData) block.material.getModernData()).getState();
    }
}
