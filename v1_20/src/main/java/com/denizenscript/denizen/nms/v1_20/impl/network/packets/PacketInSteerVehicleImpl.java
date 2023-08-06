package com.denizenscript.denizen.nms.v1_20.impl.network.packets;

import com.denizenscript.denizen.nms.interfaces.packets.PacketInSteerVehicle;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;

public class PacketInSteerVehicleImpl implements PacketInSteerVehicle {

    private ServerboundPlayerInputPacket internal;

    public PacketInSteerVehicleImpl(ServerboundPlayerInputPacket internal) {
        this.internal = internal;
    }

    @Override
    public float getLeftwardInput() {
        return internal.getXxa();
    }

    @Override
    public float getForwardInput() {
        return internal.getZza();
    }

    @Override
    public boolean getJumpInput() {
        return internal.isJumping();
    }

    @Override
    public boolean getDismountInput() {
        return internal.isShiftKeyDown();
    }
}
