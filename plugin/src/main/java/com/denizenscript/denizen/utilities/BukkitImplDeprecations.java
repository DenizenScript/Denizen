package com.denizenscript.denizen.utilities;

import com.denizenscript.denizencore.utilities.debugging.*;

public class BukkitImplDeprecations {

    // ==================== STRONG deprecations ====================
    // These show up every time, and warn any online ops. These are made clear they need to be fixed ASAP.

    // Added on 2019/08/11
    // Recommend removal 2023 or later.
    public static Warning oldEconomyTags = new StrongWarning("oldEconomyTags", "player.money.currency* tags are deprecated in favor of server.economy.currency* tags.");

    // In Bukkit impl, Added on 2019/08/19
    // Bad candidate for functionality removal - sometimes used by accident (when misreading the escape-tag docs)
    public static Warning pointlessTextTags = new StrongWarning("pointlessTextTags", "Several text tags like '&dot' or '&cm' are pointless (there's no reason you can't just directly write them in). Please replace them with the actual intended text.");

    // Added on 2019/09/18, but was deprecated earlier.
    // 2022-year-end commonality: #27
    public static Warning playerRightClicksEntityContext = new StrongWarning("playerRightClicksEntityContext", "'context.location' in event 'on player right clicks entity' is deprecated: use 'context.entity.location'.");

    // Added on 2019/09/25, but was deprecated earlier.
    // Bad candidate for functionality removal - used to be commonly used
    // 2022-year-end commonality: #13
    public static Warning qtyTags = new StrongWarning("qtyTags", "'qty' in a tag or command is deprecated: use 'quantity'.");

    // In Bukkit impl, Relevant as of 2019/09/25, made current on 2020/02/12, made strong 2022/12/31.
    private static String pointlessSubtagPrefix = "Most pointless sub-tags are deprecated in favor of explicit unique tags. ";
    public static Warning npcNicknameTag = new StrongWarning("npcNicknameTag", pointlessSubtagPrefix + "npc.name.nickname is now just npc.nickname. Note that this historically appeared in the config.yml file, so check there if you're unsure what's using this tag.");
    public static Warning npcPreviousLocationTag = new StrongWarning("npcPreviousLocationTag", pointlessSubtagPrefix + "npc.location.previous_location is now just npc.previous_location.");
    public static Warning npcAnchorListTag = new StrongWarning("npcAnchorListTag", pointlessSubtagPrefix + "npc.anchor.list is now just npc.list_anchors.");
    public static Warning playerMoneyFormatTag = new StrongWarning("playerMoneyFormatTag", pointlessSubtagPrefix + "player.money.format is now just player.formatted_money.");
    public static Warning playerFoodLevelFormatTag = new StrongWarning("playerFoodLevelFormatTag", pointlessSubtagPrefix + "player.food_level.format is now just player.formatted_food_level.");
    public static Warning playerBanInfoTags = new StrongWarning("playerBanInfoTags", pointlessSubtagPrefix + "player.ban_info.* tags are now just player.ban_*.");
    public static Warning playerNameTags = new StrongWarning("playerNameTags", pointlessSubtagPrefix + "player.name.* tags are now just player.*_name.");
    public static Warning playerSidebarTags = new StrongWarning("playerSidebarTags", pointlessSubtagPrefix + "player.sidebar.* tags are now just player.sidebar_*.");
    public static Warning playerAttackCooldownTags = new StrongWarning("playerAttackCooldownTags", pointlessSubtagPrefix + "player.attack_cooldown.* tags are now just player.attack_cooldown_*.");
    public static Warning playerXpTags = new StrongWarning("playerXpTags", pointlessSubtagPrefix + "player.xp.* tags are now just player.xp_*.");
    public static Warning entityHealthTags = new StrongWarning("entityHealthTags", pointlessSubtagPrefix + "entity.health.* tags are now just entity.health_*.");
    public static Warning entityMaxOxygenTag = new StrongWarning("entityMaxOxygenTag", pointlessSubtagPrefix + "entity.oxygen.max is now just entity.max_oxygen.");
    public static Warning itemBookTags = new StrongWarning("itemBookTags", pointlessSubtagPrefix + "item.book.* tags are now just item.book_*.");
    public static Warning playerItemInHandSlotTag = new StrongWarning("playerItemInHandSlotTag", pointlessSubtagPrefix + "player.item_in_hand_slot is now just player.held_item_slot.");

    // Added on 2019/09/24, made normal 2021/11/2021, made strong 2022/12/31.
    public static Warning oldRecipeScript = new StrongWarning("oldRecipeScript", "Item script single-recipe format is outdated. Use the modern 'recipes' list key (see meta docs).");

    // Added 2020/04/24, made strong 2022/12/31.
    public static Warning itemInventoryTag = new StrongWarning("itemInventoryTag", "The tag 'item.inventory' is deprecated: use inventory_contents instead.");

    // Added 2020/05/21, made strong 2022/12/31.
    public static Warning itemSkinFullTag = new StrongWarning("itemSkinFullTag", pointlessSubtagPrefix + "item.skin.full is now item.skull_skin.");

    // Added 2020/06/03 but deprecated long ago, made strong 2022/12/31.
    public static Warning oldBossBarMech = new StrongWarning("oldBossBarMech", "The show_boss_bar mechanism is deprecated: use the bossbar command instead.");
    public static Warning oldTimeMech = new StrongWarning("oldTimeMech", "The player.*time mechanisms are deprecated: use the time command instead.");
    public static Warning oldWeatherMech = new StrongWarning("oldWeatherMech", "The player.*weather mechanisms are deprecated: use the weather command instead.");
    public static Warning oldKickMech = new StrongWarning("oldKickMech", "The player.kick mechanism is deprecated: use the kick command instead.");
    public static Warning oldMoneyMech = new StrongWarning("oldMoneyMech", "The player.money mechanism is deprecated: use the money command instead.");

    // added 2020/07/04, made normal 2021/11/2021, made strong 2022/12/31.
    public static Warning cuboidFullTag = new StrongWarning("cuboidFullTag", "The tag cuboid.full is deprecated: this should just never be used.");
    public static Warning furnaceTimeTags = new StrongWarning("furnaceTimeTags", "The furnace_burn_time, cook time, and cook total time tag/mechs have been replaced by _duration instead of _time equivalents (using DurationTag now).");
    public static Warning playerTimePlayedTags = new StrongWarning("playerTimePlayedTags", "The tags player.first_played, last_played, ban_expiration, and ban_created have been replaced by tags of the same name with '_time' added to the end (using TimeTag now).");

    // added 2020/07/19, made normal 2021/11/2021, made strong 2022/12/31.
    public static Warning airLevelEventDuration = new StrongWarning("airLevelEventDuration", "The 'entity changes air level' event uses 'air_duration' context now instead of the old tick count number.");
    public static Warning damageEventTypeMap = new StrongWarning("damageEventTypeMap", "The 'entity damaged' context 'damage_[TYPE]' is deprecated in favor of 'damage_type_map', which is operated as a MapTag.");

    // added 2020/07/28, made normal 2021/11/2021, made strong 2022/12/31.
    public static Warning headCommand = new StrongWarning("headCommand", "The 'head' command is deprecated: use the 'equip' command with a 'player_head' item using the 'skull_skin' mechanism.");

    // added 2020/08/01, made normal 2021/11/2021, made strong 2022/12/31.
    public static Warning entityRemoveWhenFar = new StrongWarning("entityRemoveWhenFar", "The EntityTag remove_when_far_away property is deprecated in favor of the persistent property (which is the exact inverse).");
    public static Warning entityPlayDeath = new StrongWarning("entityPlayDeath", "The EntityTag 'play_death' mechanism is deprecated: use the animate command.");

    // added 2020/08/19, made normal 2021/11/2021, made strong 2022/12/31.
    public static Warning npcSpawnMechanism = new StrongWarning("npcSpawnMechanism", "The NPCTag 'spawn' mechanism is deprecated: use the spawn command.");

    // ==================== Normal deprecations ====================
    // These show up every time, and should get the server owner's attention quickly if they check their logs.

    // Added on 2018/12/23
    // Bad candidate for functionality removal - a bit handy to use in "/ex", despite being clearly bad in standard scripts.
    // Recommend never removing.
    // 2022-year-end commonality: #17
    public static Warning playerByNameWarning = new Warning("playerByNameWarning", "Warning: loading player by name - use the UUID instead (or use tag server.match_player)!");

    // Added 2020/05/17, made current on 2020/10/24.
    // 2022-year-end commonality: #28
    public static Warning itemFlagsProperty = new StrongWarning("itemFlagsProperty", "The item.flags property has been renamed to item.hides, to avoid confusion with the new flaggable itemtags system.");

    // Added 2020/11/22, made current 2021/11/2021.
    public static Warning biomeSpawnableTag = new Warning("biomeSpawnableTag", pointlessSubtagPrefix + "The tag BiomeTag.spawnable_entities.(type) is deprecated: the type is now an input context instead.");

    // Added 2020/11/30, made current 2021/11/2021.
    public static Warning npcDespawnMech = new Warning("npcDespawnMech", "The NPCTag despawn mechanism is deprecated: use the despawn command.");

    // Added 2021/02/25, made current 2022/12/31.
    public static Warning zapPrefix = new Warning("zapPrefix", "The 'zap' command should be used with the scriptname and step as two separate arguments, not just one.");

    // Added 2020/03/05, made current on 2021/04/16, made current 2022/12/31.
    public static Warning oldPlayEffectSpecials = new Warning("oldPlayEffectSpecials", "The playeffect input of forms like 'iconcrack_' have been deprecated in favor of using the special_data input (refer to meta docs).");

    // Added 2020/04/16, made current 2022/12/31.
    public static Warning entityStandingOn = new Warning("entityStandingOn", pointlessSubtagPrefix + "entity.location.standing_on is now just entity.standing_on.");

    // Added 2021/05/05, made current 2022/12/31.
    public static Warning materialLit = new Warning("materialLit", "The MaterialTag property 'lit' is deprecated in favor of 'switched'.");
    public static Warning materialCampfire = new Warning("materialCampfire", "The MaterialTag property 'campfire' are deprecated in favor of 'type'.");
    public static Warning materialDrags = new Warning("materialDrags", "The MaterialTag property 'drags' are deprecated in favor of 'mode'.");

    // Added 2021/06/15, but was irrelevant years earlier, made current 2022/12/31.
    public static Warning itemMessage = new Warning("itemMessage", "The PlayerTag mechanism 'item_message' is deprecated in favor of using the actionbar.");

    // Added 2021/11/14, made current 2022/12/31.
    public static Warning blockSpreads = new Warning("blockSpreads", "There are two '<block> spreads' events - use 'block spreads type:<block>' or 'liquid spreads type:<block>'");

    // Added 2021/11/15, made current 2022/12/31.
    public static Warning horseJumpsFormat = new Warning("horseJumpsFormat", "The '<color> horse jumps' event is deprecated: don't put the color in the event line. (Deprecated for technical design reasons).");

    // Added 2019/11/11, made slow 2021/11/2021, made current 2022/12/31.
    public static Warning entityLocationCursorOnTag = new Warning("entityLocationCursorOnTag", "entity.location.cursor_on tags should be replaced by entity.cursor_on (be careful with the slight differences though).");

    // Added 2021/05/05, made current 2022/12/31.
    public static Warning locationDistanceTag = new Warning("locationDistanceTag", "locationtag.tree_distance is deprecated in favor of location.material.distance");

    // ==================== SLOW deprecations ====================
    // These aren't spammed, but will show up repeatedly until fixed. Server owners will probably notice them.

    // Added 2021/09/08, but was irrelevant years earlier.
    // 2022-year-end commonality: #31
    public static Warning isValidTag = new SlowWarning("isValidTag", "The 'server.x_is_valid' style tags are deprecated: use '.exists', '.is_spawned.if_null[false]', etc.");

    // Added 2022/05/07.
    public static Warning armorStandRawSlot = new SlowWarning("armorStandRawSlot", "The EntityTag.disabled_slots.raw tag and EntityTag.disabled_slots_raw mechanism are deprecated, use the EntityTag.disabled_slots_data tag and EntityTag.disabled_slots mechanism instead.");

    // Added 2022/07/28
    public static Warning internalEventReflectionContext = new SlowWarning("internalEventReflectionContext", "The context.field_<name> and fields special tags for 'internal bukkit event' are deprecated in favor of the 'reflect_event' global context.");

    // Added 2022/10/14
    public static Warning skeletonSwingArm = new SlowWarning("skeletonSwingArm", "The 'SKELETON_START/STOP_SWING_ARM' animations are deprecated in favor of the 'EntityTag.aggressive' property.");
    public static Warning entityArmsRaised = new SlowWarning("entityArmsRaised", "The 'EntityTag.arms_raised' property is deprecated in favor of 'EntityTag.aggressive'.");

    // Added 2022/12/16
    public static Warning entitySkeletonArmsRaised = new SlowWarning("entitySkeletonArmsRaised", "The 'EntityTag.skeleton_arms_raised' mechanism is deprecated in favor of 'EntityTag.aggressive'.");

    // Added 2020/04/19, Relevant for many years now, made slow 2022/12/31.
    // 2022-year-end commonality: #35
    public static Warning interactScriptPriority = new SlowWarning("interactScriptPriority", "Assignment script 'interact scripts' section should not have numbered priority values, these were removed years ago. Check https://guide.denizenscript.com/guides/troubleshooting/updates-since-videos.html#assignment-script-updates for more info.");

    // Added 2021/10/24, made slow 2022/12/31.
    public static Warning entityArmorPose = new SlowWarning("entityArmorPose", "The old EntityTag.armor_pose and armor_pose_list tags are deprecated in favor of armor_pose_map.");

    // Added 2020/06/13, made slow 2022/12/31.
    public static Warning listStyleTags = new SlowWarning("listStyleTags", "'list_' tags are deprecated: just remove the 'list_' prefix.");

    // Added 2020/07/03, made slow 2022/12/31.
    public static Warning attachToMech = new SlowWarning("attachToMech", "The entity 'attach_to' mechanism is deprecated: use the new 'attach' command instead!");

    // Added 2020/07/12, made slow 2022/12/31.
    public static Warning entityEquipmentSubtags = new SlowWarning("entityEquipmentSubtags", pointlessSubtagPrefix + " 'entity.equipment.slotname' is deprecated: use 'entity.equipment_map.get[slotname]' instead.");

    // Added 2020/12/25, made slow 2022/12/31.
    // 2022-year-end commonality: #36
    public static Warning itemEnchantmentTags = new SlowWarning("itemEnchantmentTags", pointlessSubtagPrefix + "The ItemTag.enchantments.* tags are deprecated: use enchantment_map and relevant MapTag subtags.");

    // Added 2021/04/13, made slow 2022/12/31.
    public static Warning materialHasDataPackTag = new SlowWarning("materialHasDataPackTag", "The tag 'MaterialTag.has_vanilla_data_tag[...]' is deprecated in favor of MaterialTag.vanilla_tags.contains[<name>]");
    public static Warning materialPropertyTags = new SlowWarning("materialPropertyTags", "Old MaterialTag.is_x property tags are deprecated in favor of PropertyHolderObject.supports[property-name]");

    // In Paper module, Added 2022/03/20
    // // bump to normal warning and/or past warning after 1.18 is the minimum supported version (change happened in MC 1.18)
    public static Warning paperNoTickViewDistance = new SlowWarning("paperNoTickViewDistance", "Paper's 'no_tick_view_distance' is deprecated in favor of modern minecraft's 'simulation_distance' and 'view_distance' separation");

    // ==================== VERY SLOW deprecations ====================
    // These are only shown minimally, so server owners are aware of them but not bugged by them. Only servers with active scripters (using 'ex reload') will see them often.

    // Added 2021/02/05, made very-slow 2022/12/31.
    public static Warning itemProjectile = new VerySlowWarning("itemProjectile", "The item_projectile custom entity type is deprecated: modern minecraft lets you set the item of any projectile, like 'snowball[item=stick]'");

    // Added 2021/03/02, made very-slow 2022/12/31.
    public static Warning itemScriptColor = new VerySlowWarning("itemScriptColor", "The item script 'color' key is deprecated: use the 'color' mechanism under the 'mechanisms' key instead.");

    // In multiple places, Added 2021/11/20, made very-slow 2022/12/31.
    // 2022-year-end commonality: #15
    public static Warning pseudoTagBases = new VerySlowWarning("pseudoTagBases", "Pseudo-tags like '<text>', '<name>', '<amount>', and '<permission>' are deprecated in favor of definitions: just replace <text> with <[text]> or similar.");

    // Added 2020/10/18, made very-slow 2022/12/31.
    // Bad candidate for functionality removal due to frequency of use and likelihood of pre-existing data in save files.
    // 2022-year-end commonality: #2
    public static Warning itemDisplayNameMechanism = new VerySlowWarning("itemDisplayNameMechanism", "The item 'display_name' mechanism is now just the 'display' mechanism.");

    // Added 2020/12/05, made very-slow 2022/12/31.
    // Bad candidate for functionality removal due to frequency of use and likelihood of pre-existing data remaining in world data.
    // 2022-year-end commonality: #4
    public static Warning itemNbt = new VerySlowWarning("itemNbt", "The item 'nbt' property is deprecated: use ItemTag flags instead!");

    // Added 2021/02/03, made very-slow 2022/12/31.
    // Bad candidate for functional removal due to the "scriptname" variant being useful for debugging sometimes.
    // 2022-year-end commonality: #3
    public static Warning hasScriptTags = new VerySlowWarning("hasScriptTags", "The ItemTag.scriptname and EntityTag.scriptname and ItemTag.has_script and NPCTag.has_script tags are deprecated: use '.script.name' or a null check on .script.");

    // Added 2021/10/18, made very-slow 2022/12/31.
    // 2022-year-end commonality: #10
    public static Warning entityMechanismsFormat = new VerySlowWarning("entityMechanismsFormat", "Entity script containers previously allowed mechanisms in the script's root, however they should now be under a 'mechanisms' key.");

    // Added 2021/03/29, made very-slow 2022/12/31.
    // 2022-year-end commonality: #7
    public static Warning legacyAttributeProperties = new VerySlowWarning("legacyAttributeProperties", "The 'attribute' properties are deprecated in favor of the 'attribute_modifiers' properties which more fully implement the attribute system.");

    // Added 2021/07/26, made very-slow 2022/12/31.
    public static Warning itemEnchantmentsLegacy = new VerySlowWarning("itemEnchantmentsLegacy", "The tag 'ItemTag.enchantments' is deprecated: use enchantments_map, or enchantment_types.");
    public static Warning echantmentTagUpdate = new VerySlowWarning("echantmentTagUpdate", "Several legacy enchantment-related tags are deprecated in favor of using EnchantmentTag.");

    // Added 2021/08/30, made very-slow 2022/12/31.
    // 2022-year-end commonality: #23
    public static Warning giveTakeMoney = new VerySlowWarning("giveTakeMoney", "The 'take' and 'give' commands option for 'money' are deprecated in favor of using the 'money' command.");

    // Added 2022/01/30, made very-slow 2022/12/31.
    public static Warning entityItemEnderman = new VerySlowWarning("entityItemEnderman", "The property 'entity.item' for endermen has been replaced by 'entity.material' due to usage of block materials.");

    // Added 2021/06/19, made very-slow 2022/12/31.
    public static Warning entityMapTraceTag = new VerySlowWarning("entityMapTraceTag", "The tag 'EntityTag.map_trace' is deprecated in favor of EntityTag.trace_framed_map");

    // Added 2021/06/27, made very-slow 2022/12/31.
    public static Warning serverUtilTags = new VerySlowWarning("serverUtilTags", "Some 'server.' tags for core features are deprecated in favor of 'util.' equivalents, including 'java_version', '*_file', 'ram_*', 'disk_*', 'notes', 'last_reload', 'scripts', 'sql_connections', '*_time_*', ...");

    // Added 2021/06/27, made very-slow 2022/12/31.
    public static Warning serverObjectExistsTags = new VerySlowWarning("serverObjectExistsTags", "The 'object_is_valid' tag is a historical version of modern '.exists' or '.is_truthy' fallback tags.");

    // Added 2021/06/27, made very-slow 2022/12/31.
    public static Warning hsbColorGradientTag = new VerySlowWarning("hsbColorGradientTag", "The tag 'ElementTag.hsb_color_gradient' is deprecated: use 'color_gradient' with 'style=hsb'");

    // Added 2021/11/07, made very-slow 2022/12/31.
    public static Warning assignmentRemove = new VerySlowWarning("assignmentRemove", "'assignment remove' without a script is deprecated: use 'clear' to clear all scripts, or 'remove' to remove one at a time.");
    public static Warning npcScriptSingle = new VerySlowWarning("npcScriptSingle", "'npc.script' is deprecated in favor of 'npc.scripts' (plural).");

    // ==================== FUTURE deprecations ====================

    // Added 2021/03/27, deprecate officially by 2024.
    // 2022-year-end commonality: #6
    public static Warning locationFindEntities = new FutureWarning("locationFindEntities", "The tag 'LocationTag.find.entities.within' and 'blocks' tags are replaced by the 'find_entities' and 'find_blocks' versions. They are mostly compatible, but now have advanced matcher options.");
    // 2022-year-end commonality: #16
    public static Warning inventoryNonMatcherTags = new FutureWarning("inventoryNonMatcherTags", "The 'InventoryTag' tags 'contains', 'quantity', 'find', 'exclude' with raw items are deprecated and replaced by 'contains_item', 'quantity_item', 'find_item', 'exclude_item' that use advanced matcher logic.");
    // 2022-year-end commonality: #14
    public static Warning takeRawItems = new FutureWarning("takeRawItems", "The 'take' command's ability to remove raw items without any command prefix, and the 'material' and 'scriptname' options are deprecated: use the 'item:<matcher>' option.");

    // Added 2021/08/30, deprecate officially by 2024.
    // 2022-year-end commonality: #26
    public static Warning playerResourcePackMech = new FutureWarning("playerResourcePackMech", "The 'resource_pack' mechanism is deprecated in favor of using the 'resourcepack' command.");

    // Added 2022/02/21, deprecate officially by 2024.
    // 2022-year-end commonality: #8
    public static Warning oldPotionEffects = new FutureWarning("oldPotionEffects", "The comma-separated-list potion effect tags like 'list_effects' are deprecated in favor of MapTag based tags - 'effects_data'. Refer to meta documentation for details.");

    // Added 2022/05/07, deprecate officially by 2024.
    // 2022-year-end commonality: #37
    public static Warning armorStandDisabledSlotsOldFormat = new FutureWarning("armorStandDisabledSlotsOldFormat", "The EntityTag.disabled_slots tag and the SLOT/ACTION format in the EntityTag.disabled_slots mechanism are deprecated in favour of the EntityTag.disabled_slots_data tag and the MapTag format.");

    // Added 2021/06/15, deprecate officially by 2024.
    // Bad candidate for functionality removal - tags have been around a long time and some were used often.
    public static Warning locationOldCursorOn = new FutureWarning("locationOldCursorOn", "Several of the old 'LocationTag.cursor_on', 'precise_target_position', 'precise_impact_normal' variants are deprecated in favor of the 'ray_trace' tags.");

    // Added 2021/06/17, deprecate officially by 2024.
    // 2022-year-end commonality: #18
    public static Warning debugBlockAlpha = new FutureWarning("debugBlockAlpha", "The 'alpha' argument for the 'debugblock' command is deprecated: put the alpha in the color input instead.");

    // Added 2023/01/15, deprecate officially by 2026
    public static Warning entityShootsMaterialEvent = new FutureWarning("entityShootsMaterialEvent", "The '<entity> shoots <material>' event is deprecated in favor of '<projectile> hits' with the 'block' and 'shooter' switches.");
    public static Warning projectileHitsBlockLocationContext = new FutureWarning("projectileHitsBlockLocationContext", "'context.location' in the '<projectile> hits' event is deprecated in favor of 'context.hit_block'.");
    public static Warning projectileHitsEventMatchers = new FutureWarning("projectileHitsEventMatchers", "The block/entity matchers in '<projectile> hits <block>/<entity>' are deprecated in favor of the 'block' and 'entity' switches.");
    // Bump once 1.19 is the minimum supported version, as the change happened on that version.
    public static Warning projectileCollideEvent = new FutureWarning("projectileCollideEvent", "The '<projectile> collides with <entity>' event is deprecated in favor of '<projectile> hits' with the 'entity' switch.");

    // Added 2023/03/05, deprecate officially by 2026
    public static Warning serverSystemMechanisms = new FutureWarning("serverSystemMechanisms", "Some 'server' mechanisms for core features are deprecated in favor of 'system' equivalents.");

    // Added 2023/03/27, deprecate officially by 2026
    public static Warning oldAgeLockedControls = new FutureWarning("oldAgeLockedControls", "Several old ways of controlling whether an entity's age is locked are deprecated in favor of the 'EntityTag.age_locked' tag/mech pair.");

    // ==================== PAST deprecations of things that are already gone but still have a warning left behind ====================

    // Added on 2019/10/13
    public static Warning versionScripts = new StrongWarning("versionScripts", "Version script containers are deprecated due to the old script repo no longer being active.");

    // Added on 2019/03/08, removed 2020/10/24.
    public static Warning boundWarning = new StrongWarning("boundWarning", "Item script 'bound' functionality has never been reliable and should not be used. Consider replicating the concept with world events.");

    // Deprecated 2019/02/06, removed 2022/03/19.
    public static Warning globalTagName = new StrongWarning("globalTagName", "Using 'global' as a base tag is a deprecated alternate name. Please use 'server' instead.");

}
