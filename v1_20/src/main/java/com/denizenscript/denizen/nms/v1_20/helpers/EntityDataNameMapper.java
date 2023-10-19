package com.denizenscript.denizen.nms.v1_20.helpers;


import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.objects.core.ElementTag;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.animal.sniffer.Sniffer;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.MinecartCommandBlock;
import net.minecraft.world.entity.vehicle.MinecartFurnace;

import java.util.HashMap;
import java.util.Map;

public class EntityDataNameMapper {

    public static final Map<Class<? extends Entity>, Map<String, Integer>> entityDataNames = new HashMap<>();

    public static void registerDataName(Class<? extends Entity> entityClass, int id, String name) {
        entityDataNames.computeIfAbsent(entityClass, k -> new HashMap<>()).put(name, id);
    }

    static {
        // Entity
        registerDataName(Entity.class, 0, "entity_flags");
        registerDataName(Entity.class, 1, "air_ticks");
        registerDataName(Entity.class, 2, "custom_name");
        registerDataName(Entity.class, 3, "custom_name_visible");
        registerDataName(Entity.class, 4, "silent");
        registerDataName(Entity.class, 5, "no_gravity");
        registerDataName(Entity.class, 6, "pose");
        registerDataName(Entity.class, 7, "frozen_ticks");

        // Interaction
        registerDataName(Interaction.class, 8, "width");
        registerDataName(Interaction.class, 9, "height");
        registerDataName(Interaction.class, 10, "responsive");

        // Display
        registerDataName(Display.class, 8, "transform_interpolation_start");
        registerDataName(Display.class, 9, "transform_interpolation_duration");
        registerDataName(Display.class, 10, "movement_interpolation_duration");
        registerDataName(Display.class, 11, "translation");
        registerDataName(Display.class, 12, "scale");
        registerDataName(Display.class, 13, "left_rotation");
        registerDataName(Display.class, 14, "right_rotation");
        registerDataName(Display.class, 15, "billboard");
        registerDataName(Display.class, 16, "brightness");
        registerDataName(Display.class, 17, "view_range");
        registerDataName(Display.class, 18, "shadow_radius");
        registerDataName(Display.class, 19, "shadow_strength");
        registerDataName(Display.class, 20, "width");
        registerDataName(Display.class, 21, "height");
        registerDataName(Display.class, 22, "glow_color");

        // Block display
        registerDataName(Display.BlockDisplay.class, 23, "material");

        // Item display
        registerDataName(Display.ItemDisplay.class, 23, "item");
        registerDataName(Display.ItemDisplay.class, 24, "model_transform");

        // Text display
        registerDataName(Display.TextDisplay.class, 23, "text");
        registerDataName(Display.TextDisplay.class, 24, "line_width");
        registerDataName(Display.TextDisplay.class, 25, "background_color");
        registerDataName(Display.TextDisplay.class, 26, "text_opacity");
        registerDataName(Display.TextDisplay.class, 27, "text_display_flags");

        // Thrown item projectile
        registerDataName(ThrowableProjectile.class, 8, "item");

        // Eye of ender
        registerDataName(EyeOfEnder.class, 8, "item");

        // Falling block
        registerDataName(FireworkRocketEntity.class, 8, "spawn_position");

        // Area effect cloud
        registerDataName(AreaEffectCloud.class, 8, "radius");
        registerDataName(AreaEffectCloud.class, 9, "color");
        registerDataName(AreaEffectCloud.class, 10, "waiting");
        registerDataName(AreaEffectCloud.class, 11, "particle");

        // Fishing hook
        registerDataName(FishingHook.class, 8, "hooked_entity_id");
        registerDataName(FishingHook.class, 9, "catchable");

        // Abstract arrow
        registerDataName(AbstractArrow.class, 8, "abstract_arrow_flags");
        registerDataName(AbstractArrow.class, 9, "piercing_level");

        // Arrow
        registerDataName(Arrow.class, 10, "color");

        // Thrown trident
        registerDataName(ThrownTrident.class, 10, "loyalty_level");
        registerDataName(ThrownTrident.class, 11, "enchantment_glint");

        // Boat
        registerDataName(Boat.class, 8, "shaking_ticks");
        registerDataName(Boat.class, 9, "shaking_direction");
        registerDataName(Boat.class, 10, "damage_taken");
        registerDataName(Boat.class, 11, "type");
        registerDataName(Boat.class, 12, "left_paddle_moving");
        registerDataName(Boat.class, 13, "right_paddle_moving");
        registerDataName(Boat.class, 14, "bubble_shaking_ticks");

        // End crystal
        registerDataName(EndCrystal.class, 8, "beam_target");
        registerDataName(EndCrystal.class, 9, "showing_bottom");

        // Small fireball
        registerDataName(SmallFireball.class, 8, "item");

        // Fireball
        registerDataName(Fireball.class, 8, "item");

        // Wither skull
        registerDataName(WitherSkull.class, 8, "invulnerable");

        // Firework rocket
        registerDataName(FireworkRocketEntity.class, 8, "item");
        registerDataName(FireworkRocketEntity.class, 9, "shooter_id");
        registerDataName(FireworkRocketEntity.class, 10, "shot_at_angle");

        // Item frame
        registerDataName(ItemFrame.class, 8, "item");
        registerDataName(ItemFrame.class, 9, "rotation");

        // Painting
        registerDataName(Painting.class, 8, "painting_variant");

        // Living entity
        registerDataName(LivingEntity.class, 8, "living_entity_flags");
        registerDataName(LivingEntity.class, 9, "health");
        registerDataName(LivingEntity.class, 10, "potion_effect_color");
        registerDataName(LivingEntity.class, 11, "is_potion_effect_ambient");
        registerDataName(LivingEntity.class, 12, "arrows_in_body");
        registerDataName(LivingEntity.class, 13, "bee_stingers_in_body");
        registerDataName(LivingEntity.class, 14, "bed_location");

        // Player
        registerDataName(Player.class, 15, "additional_hearts");
        registerDataName(Player.class, 16, "score");
        registerDataName(Player.class, 17, "skin_parts");
        registerDataName(Player.class, 18, "main_hand");
        registerDataName(Player.class, 19, "left_shoulder_entity");
        registerDataName(Player.class, 20, "right_shoulder_entity");

        // Armor stand
        registerDataName(ArmorStand.class, 15, "armor_stand_flags");
        registerDataName(ArmorStand.class, 16, "head_rotation");
        registerDataName(ArmorStand.class, 17, "body_rotation");
        registerDataName(ArmorStand.class, 18, "left_arm_rotation");
        registerDataName(ArmorStand.class, 19, "right_arm_rotation");
        registerDataName(ArmorStand.class, 20, "left_leg_rotation");
        registerDataName(ArmorStand.class, 21, "right_leg_rotation");

        // Mob
        registerDataName(Mob.class, 15, "mob_flags");

        // Bat flags
        registerDataName(Bat.class, 16, "bat_flags");

        // Dolphin
        registerDataName(Dolphin.class, 16, "treasure_location");
        registerDataName(Dolphin.class, 17, "has_fish");
        registerDataName(Dolphin.class, 18, "moisture_level");

        // Abstract Fish
        registerDataName(AbstractFish.class, 16, "from_bucket");

        // PufferFish
        registerDataName(Pufferfish.class, 17, "puff_state");

        // Tropical fish
        registerDataName(TropicalFish.class, 17, "variant");

        // Ageable mob
        registerDataName(AgeableMob.class, 16, "is_baby");

        // Sniffer
        registerDataName(Sniffer.class, 17, "sniffer_state");
        registerDataName(Sniffer.class, 18, "finish_dig_time");

        // Abstract horse
        registerDataName(AbstractHorse.class, 17, "horse_flags");

        // Horse
        registerDataName(Horse.class, 18, "variant");

        // Camel
        registerDataName(Camel.class, 18, "is_dashing");
        registerDataName(Camel.class, 19, "last_pose_change");

        // Chested horse
        registerDataName(AbstractChestedHorse.class, 18, "has_chest");

        // Llama
        registerDataName(Llama.class, 19, "strength");
        registerDataName(Llama.class, 20, "carpet_color");
        registerDataName(Llama.class, 21, "variant");

        // Axolotl
        registerDataName(Axolotl.class, 17, "variant");
        registerDataName(Axolotl.class, 18, "playing_dead");
        registerDataName(Axolotl.class, 19, "from_bucket");

        // Bee
        registerDataName(Bee.class, 17, "bee_flags");
        registerDataName(Bee.class, 18, "anger_time");

        // Fox
        registerDataName(Fox.class, 17, "type");
        registerDataName(Fox.class, 18, "fox_flags");
        registerDataName(Fox.class, 19, "first_trusted_uuid");
        registerDataName(Fox.class, 20, "second_trusted_uuid");

        // Frog
        registerDataName(Frog.class, 17, "variant");
        registerDataName(Frog.class, 18, "target_id");

        // Ocelot
        registerDataName(Ocelot.class, 17, "is_trusting");

        // Panda
        registerDataName(Panda.class, 17, "ask_for_bamboo_timer");
        registerDataName(Panda.class, 18, "sneeze_timer");
        registerDataName(Panda.class, 19, "eat_timer");
        registerDataName(Panda.class, 20, "main_gene");
        registerDataName(Panda.class, 21, "hidden_gene");
        registerDataName(Panda.class, 22, "panda_flags");

        // Pig
        registerDataName(Pig.class, 17, "has_saddle");
        registerDataName(Pig.class, 18, "boost_ticks");

        // Rabbit
        registerDataName(Rabbit.class, 17, "type");

        // Turtle
        registerDataName(Turtle.class, 17, "home_location");
        registerDataName(Turtle.class, 18, "has_egg");
        registerDataName(Turtle.class, 19, "laying_egg");
        registerDataName(Turtle.class, 20, "travel_location");
        registerDataName(Turtle.class, 21, "going_home");
        registerDataName(Turtle.class, 20, "traveling");

        // Polar bear
        registerDataName(PolarBear.class, 17, "standing_up");

        // Hoglin
        registerDataName(Hoglin.class, 17, "immune_to_zombification");

        // Mooshroom
        registerDataName(MushroomCow.class, 17, "variant");

        // Sheep
        registerDataName(Sheep.class, 17, "sheep_wool_flags");

        // Strider
        registerDataName(Strider.class, 17, "boost_ticks");
        registerDataName(Strider.class, 18, "shaking");
        registerDataName(Strider.class, 19, "has_saddle");

        // Tamable animal
        registerDataName(TamableAnimal.class, 17, "tamable_animal_flags");
        registerDataName(TamableAnimal.class, 18, "owner");

        // Cat
        registerDataName(Cat.class, 19, "variant");
        registerDataName(Cat.class, 20, "lying");
        registerDataName(Cat.class, 20, "relaxed");
        registerDataName(Cat.class, 21, "collar_color");

        // Wolf
        registerDataName(Wolf.class, 19, "begging");
        registerDataName(Wolf.class, 20, "collar_color");
        registerDataName(Wolf.class, 21, "anger_time");

        // Parrot
        registerDataName(Parrot.class, 19, "variant");

        // Abstract villager
        registerDataName(AbstractVillager.class, 17, "head_shake_ticks");

        // Villager
        registerDataName(Villager.class, 18, "villager_data");

        // Iron golem
        registerDataName(IronGolem.class, 16, "iron_golem_flags");

        // Snow golem
        registerDataName(SnowGolem.class, 16, "snow_golem_pumpkin_flags");

        // Shulker
        registerDataName(Shulker.class, 16, "attach_face");
        registerDataName(Shulker.class, 17, "attachment_location");
        registerDataName(Shulker.class, 18, "peek");
        registerDataName(Shulker.class, 19, "color");

        // Base piglin
        registerDataName(AbstractPiglin.class, 16, "immune_to_zombification");

        // Piglin
        registerDataName(Piglin.class, 17, "is_baby");
        registerDataName(Piglin.class, 18, "charging_crossbow");
        registerDataName(Piglin.class, 19, "dancing");

        // Blaze
        registerDataName(Blaze.class, 16, "blaze_flags");

        // Creeper
        registerDataName(Creeper.class, 16, "state");
        registerDataName(Creeper.class, 17, "charged");
        registerDataName(Creeper.class, 18, "ignited");

        // Goat
        registerDataName(Goat.class, 17, "screaming");
        registerDataName(Goat.class, 18, "has_left_horn");
        registerDataName(Goat.class, 19, "has_right_horn");

        // Guardian
        registerDataName(Guardian.class, 16, "spikes_retracted");
        registerDataName(Guardian.class, 17, "target_id");

        // Raider
        registerDataName(Raider.class, 16, "celebrating");

        // Pillager
        registerDataName(Pillager.class, 17, "charging_crossbow");

        // Spellcaster illager
        registerDataName(SpellcasterIllager.class, 17, "spell");

        // Witch
        registerDataName(Witch.class, 17, "drinking_potion");

        // Vex
        registerDataName(Vex.class, 16, "vex_flags");

        // Spider
        registerDataName(Spider.class, 16, "spider_flags");

        // Warden
        registerDataName(Warden.class, 16, "anger_level");

        // Wither
        registerDataName(WitherBoss.class, 16, "center_head_target");
        registerDataName(WitherBoss.class, 17, "left_head_target");
        registerDataName(WitherBoss.class, 18, "right_head_target");
        registerDataName(WitherBoss.class, 19, "invulnerable_time");

        // Zoglin
        registerDataName(Zoglin.class, 16, "is_baby");

        // Zombie
        registerDataName(Zombie.class, 16, "is_baby");
        registerDataName(Zombie.class, 17, "type"); // Unused
        registerDataName(Zombie.class, 18, "converting_in_water");

        // Zombie villager
        registerDataName(ZombieVillager.class, 19, "is_converting");
        registerDataName(ZombieVillager.class, 20, "villager_data");

        // Enderman
        registerDataName(EnderMan.class, 16, "carried_block");
        registerDataName(EnderMan.class, 17, "screaming");
        registerDataName(EnderMan.class, 18, "staring");

        // Ender dragon
        registerDataName(EnderDragon.class, 16, "phase");

        // Ghast
        registerDataName(Ghast.class, 16, "attacking");

        // Phantom
        registerDataName(Phantom.class, 16, "size");

        // Slime
        registerDataName(Slime.class, 16, "size");

        // Abstract minecart
        registerDataName(AbstractMinecart.class, 8, "shaking_ticks");
        registerDataName(AbstractMinecart.class, 9, "shaking_direction");
        registerDataName(AbstractMinecart.class, 10, "damage_taken");
        registerDataName(AbstractMinecart.class, 11, "display_block_id");
        registerDataName(AbstractMinecart.class, 12, "display_block_y");
        registerDataName(AbstractMinecart.class, 13, "show_display_block");

        // Minecraft furnace
        registerDataName(MinecartFurnace.class, 14, "has_fuel");

        // Minecraft command block
        registerDataName(MinecartCommandBlock.class, 14, "command");
        registerDataName(MinecartCommandBlock.class, 15, "last_output");

        // Primed TNT
        registerDataName(PrimedTnt.class, 8, "fuse_ticks");
    }

    public static int getIdForName(Class<? extends Entity> entityClass, String name) {
        Class<?> currentClass = entityClass;
        int id = getIdFromClass(currentClass, name);
        while (id == -1) {
            currentClass = currentClass.getSuperclass();
            if (currentClass == Object.class) {
                break;
            }
            id = getIdFromClass(currentClass, name);
        }
        return id;
    }

    private static int getIdFromClass(Class<?> entityClass, String name) {
        Map<String, Integer> nameToId = entityDataNames.get(entityClass);
        int id = nameToId != null ? nameToId.getOrDefault(name, -1) : -1;
        if (id == -1 && ArgumentHelper.matchesInteger(name)) {
            id = new ElementTag(name).asInt();
        }
        return id;
    }
}
