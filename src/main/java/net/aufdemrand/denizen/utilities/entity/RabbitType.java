package net.aufdemrand.denizen.utilities.entity;

import net.minecraft.server.v1_10_R1.EntityRabbit;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftRabbit;
import org.bukkit.entity.Rabbit;

public enum RabbitType {
    BROWN(0, 0),
    WHITE(1, 1),
    BLACK(2, 2),
    WHITE_SPLOTCHED(3, 3),
    GOLD(4, 4),
    SALT(5, 5),
    KILLER(6, 99);

    public static RabbitType getRabbitType(Rabbit rabbit) {
        return RabbitType.getType(getEntityRabbit(rabbit).getRabbitType());
    }

    public static void setRabbitType(Rabbit rabbit, RabbitType type) {
        getEntityRabbit(rabbit).setRabbitType(type.getId());
    }

    private static EntityRabbit getEntityRabbit(Rabbit rabbit) {
        return (EntityRabbit) ((CraftRabbit) rabbit).getHandle();
    }

    private static final RabbitType[] types = new RabbitType[values().length];
    private final int internalId;
    private final int id;

    static {
        for (RabbitType type : values()) {
            types[type.getInternalId()] = type;
        }
    }

    private RabbitType(int internalId, int id) {
        this.internalId = internalId;
        this.id = id;
    }

    /**
     * Gets the ID of this rabbit type.
     *
     * @return Type ID.
     */
    public int getId() {
        return id;
    }

    /**
     * Gets the internal ID of this rabbit type.
     *
     * @return Internal Type ID.
     */
    private int getInternalId() {
        return internalId;
    }

    /**
     * Gets a rabbit type by its ID.
     *
     * @param id ID of the rabbit type to get.
     * @return Resulting type, or null if not found.
     */
    public static RabbitType getType(int id) {
        for (RabbitType type : types) {
            if (id == type.getId()) {
                return type;
            }
        }
        return null;
    }
}
