package com.shanebeestudios.nms.elements;

import com.github.shanebeee.skr.Registration;
import com.shanebeestudios.nms.api.util.Utils;
import com.shanebeestudios.nms.elements.effects.EffApplyBiomeCarver;
import com.shanebeestudios.nms.elements.effects.EffApplyBiomeFeature;
import com.shanebeestudios.nms.elements.effects.EffApplyBiomeSpawner;
import com.shanebeestudios.nms.elements.effects.EffApplyBiomeTag;
import com.shanebeestudios.nms.elements.effects.EffApplyEnvironmentalAttribute;
import com.shanebeestudios.nms.elements.effects.EffBiomeFill;
import com.shanebeestudios.nms.elements.effects.EffBlockFill;
import com.shanebeestudios.nms.elements.effects.EffPlaceFeature;
import com.shanebeestudios.nms.elements.effects.EffRegistrySerialization;
import com.shanebeestudios.nms.elements.expressions.ExprAvailableKeys;
import com.shanebeestudios.nms.elements.expressions.ExprBlockDataForPlacement;
import com.shanebeestudios.nms.elements.expressions.ExprLocalDifficulty;
import com.shanebeestudios.nms.elements.expressions.ExprParticleOption;
import com.shanebeestudios.nms.elements.sections.SecBiomeFeatures;
import com.shanebeestudios.nms.elements.sections.SecBiomeRegister;
import com.shanebeestudios.nms.elements.sections.SecBiomeSpawners;
import com.shanebeestudios.nms.elements.sections.SecBiomeSpecialEffects;
import com.shanebeestudios.nms.elements.sections.SecEnchantmentRegister;
import com.shanebeestudios.nms.elements.structures.StructRegistryRegistration;
import com.shanebeestudios.nms.elements.type.Types;

public class ElementRegistration {

    public static void register(Registration reg) {
        // EFFECTS
        EffApplyBiomeCarver.register(reg);
        EffApplyBiomeFeature.register(reg);
        EffApplyBiomeSpawner.register(reg);
        EffApplyBiomeTag.register(reg);
        EffApplyEnvironmentalAttribute.register(reg);
        EffBiomeFill.register(reg);
        EffBlockFill.register(reg);
        EffPlaceFeature.register(reg);
        EffRegistrySerialization.register(reg);

        // EXPRESSIONS
        ExprAvailableKeys.register(reg);
        ExprBlockDataForPlacement.register(reg);
        ExprLocalDifficulty.register(reg);
        ExprParticleOption.register(reg);

        // SECTIONS
        SecBiomeFeatures.register(reg);
        SecBiomeRegister.register(reg);
        SecBiomeSpecialEffects.register(reg);
        SecBiomeSpawners.register(reg);
        SecEnchantmentRegister.register(reg);

        // STRUCTURES
        StructRegistryRegistration.register(reg);

        // TYPES
        Types.register(reg);

        // FINALIZE
        reg.finalizeRegistration();

        // ELEMENT COUNT
        int typeCount = reg.getTypes().size();
        int structureCount = reg.getStructures().size();
        int eventCount = reg.getEvents().size();
        int sectionCount = reg.getSections().size();
        int effectCount = reg.getEffects().size();
        int expressionCount = reg.getExpressions().size();
        int conditionCount = reg.getConditions().size();
        int total = eventCount + effectCount + expressionCount + conditionCount + sectionCount + typeCount + structureCount;

        Utils.log("Loaded SkNMS (%s) elements:", total);
        Utils.log(" - %s types", typeCount);
        Utils.log(" - %s structures", structureCount);
        Utils.log(" - %s events", eventCount);
        Utils.log(" - %s sections", sectionCount);
        Utils.log(" - %s effects", effectCount);
        Utils.log(" - %s expressions", expressionCount);
        Utils.log(" - %s conditions", conditionCount);
    }

}
