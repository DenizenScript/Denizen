package net.aufdemrand.denizen.utilities.entity;

import org.bukkit.entity.Rabbit;

public enum RabbitType {
    BROWN(Rabbit.Type.BROWN),
    WHITE(Rabbit.Type.WHITE),
    BLACK(Rabbit.Type.BLACK),
    WHITE_SPLOTCHED(Rabbit.Type.BLACK_AND_WHITE),
    GOLD(Rabbit.Type.GOLD),
    SALT(Rabbit.Type.SALT_AND_PEPPER),
    KILLER(Rabbit.Type.THE_KILLER_BUNNY);

    private Rabbit.Type type;

    RabbitType(Rabbit.Type type) {
        this.type = type;
    }

    public Rabbit.Type getType() {
        return type;
    }
}
