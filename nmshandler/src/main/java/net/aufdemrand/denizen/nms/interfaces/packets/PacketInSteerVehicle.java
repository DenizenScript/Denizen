package net.aufdemrand.denizen.nms.interfaces.packets;

public interface PacketInSteerVehicle {

    float getLeftwardInput();

    float getForwardInput();

    boolean getJumpInput();

    boolean getDismountInput();
}
