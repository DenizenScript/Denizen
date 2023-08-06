package com.denizenscript.denizen.npc;

import com.denizenscript.denizen.npc.traits.*;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.trait.TraitInfo;

public class TraitRegistry {

    public static void registerMainTraits() {
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(AssignmentTrait.class).withName("assignment"));
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(ConstantsTrait.class).withName("constants"));
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(DenizenFlagsTrait.class).withName("denizen_flags"));
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(FishingTrait.class).withName("fishing"));
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(HealthTrait.class).withName("health"));
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(HungerTrait.class).withName("hunger"));
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(InvisibleTrait.class).withName("invisible"));
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(MirrorEquipmentTrait.class).withName("mirrorequipment"));
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(MirrorNameTrait.class).withName("mirrorname"));
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(MirrorTrait.class).withName("mirror"));
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(MobproxTrait.class).withName("mobprox"));
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(NicknameTrait.class).withName("nickname"));
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(PushableTrait.class).withName("pushable"));
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(SittingTrait.class).withName("sitting"));
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(SleepingTrait.class).withName("sleeping"));
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(SneakingTrait.class).withName("sneaking"));
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(TriggerTrait.class).withName("triggers"));
    }
}
