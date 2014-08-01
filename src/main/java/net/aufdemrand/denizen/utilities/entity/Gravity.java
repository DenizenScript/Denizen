package net.aufdemrand.denizen.utilities.entity;

public enum Gravity {

    DROPPED_ITEM("Item", 0.115),
    EXPERIENCE_ORB("XPOrb", 0.115),
    LEASH_HITCH("LeashKnot", 0.115),
    PAINTING("Painting", 0.115),
    ARROW("Arrow", 0.118),
    SNOWBALL("Snowball", 0.076),
    FIREBALL("Fireball", 0.115),
    SMALL_FIREBALL("SmallFireball", 0.115),
    ENDER_PEARL("ThrownEnderpearl", 0.115),
    ENDER_SIGNAL("EyeOfEnderSignal", 0.115),
    THROWN_EXP_BOTTLE("ThrownExpBottle", 0.157),
    ITEM_FRAME("ItemFrame", 0.115),
    WITHER_SKULL("WitherSkull", 0.115),
    PRIMED_TNT("PrimedTnt", 0.115),
    FALLING_BLOCK("FallingSand", 0.115),
    FIREWORK("FireworksRocketEntity", 0.115),
    BOAT("Boat", 0.115),
    MINECART("MinecartRideable", 0.115),
    MINECART_CHEST("MinecartChest", 0.115),
    MINECART_FURNACE("MinecartFurnace", 0.115),
    MINECART_TNT("MinecartTNT", 0.115),
    MINECART_HOPPER("MinecartHopper", 0.115),
    MINECART_MOB_SPAWNER("MinecartMobSpawner", 0.115),
    CREEPER("Creeper", 0.115),
    SKELETON("Skeleton", 0.115),
    SPIDER("Spider", 0.115),
    GIANT("Giant", 0.115),
    ZOMBIE("Zombie", 0.115),
    SLIME("Slime", 0.115),
    GHAST("Ghast", 0.115),
    PIG_ZOMBIE("PigZombie", 0.115),
    ENDERMAN("Enderman", 0.115),
    CAVE_SPIDER("CaveSpider", 0.115),
    SILVERFISH("Silverfish", 0.115),
    BLAZE("Blaze", 0.115),
    MAGMA_CUBE("LavaSlime", 0.115),
    ENDER_DRAGON("EnderDragon", 0.115),
    WITHER("WitherBoss", 0.115),
    BAT("Bat", 0.115),
    WITCH("Witch", 0.115),
    PIG("Pig", 0.115),
    SHEEP("Sheep", 0.115),
    COW("Cow", 0.115),
    CHICKEN("Chicken", 0.115),
    SQUID("Squid", 0.115),
    WOLF("Wolf", 0.115),
    MUSHROOM_COW("MushroomCow", 0.115),
    SNOWMAN("SnowMan", 0.115),
    OCELOT("Ozelot", 0.115),
    IRON_GOLEM("VillagerGolem", 0.115),
    HORSE("EntityHorse", 0.115),
    VILLAGER("Villager", 0.115),
    ENDER_CRYSTAL("EnderCrystal", 0.115),
    SPLASH_POTION(null, 0.115),
    EGG(null, 0.074),
    FISHING_HOOK(null, 0.115),
    LIGHTNING(null, 0.115),
    WEATHER(null, 0.115),
    PLAYER(null, 0.115),
    COMPLEX_PART(null, 0.115);

    private String entityName;
    private double gravity;

    Gravity(String entityName, double gravity) {
        this.entityName = entityName;
        this.gravity = gravity;
    }

    public String getName() {
        return entityName;
    }

    public double getGravity() {
        return gravity;
    }
}
