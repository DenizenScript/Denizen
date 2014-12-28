package net.aufdemrand.denizen.utilities.entity;

import net.minecraft.server.v1_8_R1.EntityRabbit;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftRabbit;

public class Rabbit {

    public static Type getRabbitType(org.bukkit.entity.Rabbit rabbit) {
        return Type.getType(getEntityRabbit(rabbit).cl());
    }

    public static void setRabbitType(org.bukkit.entity.Rabbit rabbit, Type type) {
        getEntityRabbit(rabbit).r(type.getId());
    }

    private static EntityRabbit getEntityRabbit(org.bukkit.entity.Rabbit rabbit) {
        return (EntityRabbit) ((CraftRabbit) rabbit).getHandle();
    }

    public static enum Type {
        BROWN(0,0),
        WHITE(1,1),
        BLACK(2,2),
        WHITE_SPLOTCHED(3,3),
        GOLD(4,4),
        SALT(5,5),
        KILLER(6,99);

        private static final Type[] types = new Type[Type.values().length];
        private final int internalId;
        private final int id;

        static {
            for (Type type : values()) {
                types[type.getInternalId()] = type;
            }
        }

        private Type(int internalId, int id) {
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
        public static Type getType(int id) {
            for (Type type : types) {
                if (id == type.getId())
                    return type;
            }
            return null;
        }
    }
}
