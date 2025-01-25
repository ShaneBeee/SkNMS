package com.shanebeestudios.nms.elements.sections;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SectionExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.util.Kleenean;
import com.shanebeestudios.nms.api.registry.EnchantmentDefinition;
import com.shanebeestudios.skbee.api.util.SimpleEntryValidator;
import com.shanebeestudios.skbee.api.util.Util;
import com.shanebeestudios.skbee.api.wrapper.ComponentWrapper;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Event;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.entry.EntryValidator;

import java.util.List;

@Name("Register Enchantment")
@Description({"Register a new custom enchantment.",
    "There are a LOT of entries for this, so please refer to the " +
        "[**Enchantment Definition**](https://minecraft.wiki/w/Enchantment_definition) page on McWiki for all the details.",
    "More examples and detailed information provided in the [**SkNMS Wiki**](https://github.com/ShaneBeee/SkNMS/wiki/Custom-Enchantments).",
    "",
    "**NOTES**:",
    "- Custom enchantments cannot be removed at runtime (a restart is the only way to get rid of them or change them after they're registered).",
    "- If you make a change to your custom enchantment, you'll have to restart your server (reloading the script just won't cut it).",
    "- At the time of parsing scripts, your custom enchantment won't be acknowledged (in Skript), " +
        "that will only happen after it actually registers, this is why this returns itself as an enchantment you can save in a variable.",
    "- The parsed as expression will work, ex: `\"custom:my_enchant\" parsed as enchantment`.",
    "- I did not add an `effects` entry as it's super duper convoluted, and you can handle what your enchantment does via code.",
    "",
    "**DEFINITION ENTRIES**:",
    "These entries are directly related to the Enchantment Definition in Minecraft (as seen in the above mentioned wiki).",
    "I'm only going to touch on a few here, as there are so many to type out, see the above mentioned wiki for full details.",
    "- `id` = Takes in a string to identify your new enchantment, think vanilla \"minecraft:sharpness\".",
    "- `description` = Takes in a text component (from SkBee) or string, this is how your enchantment will show up in lore.",
    "- `exclusive_set` = The enchantments your enchantment will not work with. Either a single string (enchantment tag) or a list of enchantments.",
    "- `supported_items/primary_items` = See wiki for explanations. Either a single string (item tag) or a list of items.",
    "- `slots` = I don't think this is needed as it would be handled by the effects in Minecraft, which we aren't using here.",
    "",
    "**TAG ENTRIES**:",
    "These entries are related to the Minecraft Enchantment tags that this enchantment will be added to (all default to false).",
    "- `is_cursed` = Will add to the `#minecraft:curse` tag making your item a cursed item (These enchantments have red colored description and cannot be removed with a grindstone).",
    "- `is_treasure` = Will add to the `#minecraft:treasure` tag.",
    "- `is_tradeable` = Will add to the `#minecraft:treasure` and `#minecraft:double_trade_price` tags.",
    "- `is_discoverable` = Will add to the `#minecraft:in_enchanting_table` tag if not cursed or a treasure.",
    "- `is_on_random_loot` = Will add to the `#minecraft:on_random_loot` tag and can be found on naturally generated equipment from loot tables.",
    "- `is_on_mob_spawn_equipment` = If not a treasure, will add to the `#minecraft:on_mob_spawn_equipment` tag and can be found on spawned mobs' equipment.",
    "- `is_on_traded_equipment` = If not a treasure, will add to the `#minecraft:on_traded_equipment` tag and can be found on equipment sold by villagers.",
    "",
    "**WARNINGS**:",
    "Enchantments are not supposed to be created at runtime. This method is super hacky and I highly HIGHLY recommend just using a datapack instead.",
    "You must ensure 1 of 2 things:",
    "- If your spawn keeps loaded in your world, you must make sure no items are in any chests or anything in that area that contain these enchantments.",
    "- Or just make sure to turn off your spawn chunk radius (set the gamerule `spawnChunkRadius` to 0 for all worlds).",
    "This is due to these enchantments will register to Minecraft via Skript AFTER your world/spawn chunks load.",
    "Do not, I repeat... DO NOT save custom enchantments to variables (Skript will panic trying to load enchantments that arent registered yet). " +
        "RAM/Memory variables are safe!",
    "That said, enjoy your new custom enchantments."})
@Examples({"# Wither Sword Enchantment",
    "on load:",
    "\tif {-enchantment::wither} is not set:",
    "\t\tset {-enchantment::wither} to register enchantment:",
    "\t\t\tid: \"my_pack:wither\"",
    "\t\t\tdescription: mini message from \"<red>Wither\"",
    "\t\t\tsupported_items: \"minecraft:swords\"",
    "\t\t\tmax_level: 5",
    "",
    "on damage of mob by player:",
    "\tset {_level} to enchantment level of {-enchantment::wither} of attacker's tool",
    "\tif {_level} > 0:",
    "\t\tset {_time} to \"%{_level} * 3% seconds\" parsed as timespan",
    "\t\tapply wither to victim for {_time}"})
@Since("1.0.0")
@SuppressWarnings({"UnstableApiUsage", "unchecked"})
public class SecEnchantmentRegister extends SectionExpression<Enchantment> {

    private static final EntryValidator VALIDATOR;

    static {
        Class<Object>[] somethingClasses = new Class[]{ComponentWrapper.class,String.class};
        Class<Object>[] exclusiveSetClasses = new Class[]{Enchantment.class, String.class};
        Class<Object>[] itemAndTagClasses = new Class[]{ItemType.class, String.class};
        VALIDATOR = SimpleEntryValidator.builder()
            // Required
            .addRequiredEntry("id", String.class)
            .addRequiredEntry("description", somethingClasses)
            .addRequiredEntry("supported_items", itemAndTagClasses)
            // Optional
            .addOptionalEntry("exclusive_set", exclusiveSetClasses)
            .addOptionalEntry("primary_items", itemAndTagClasses)
            .addOptionalEntry("weight", Integer.class)
            .addOptionalEntry("max_level", Integer.class)
            .addOptionalEntry("min_cost_base", Integer.class)
            .addOptionalEntry("min_cost_per_level_above_first", Integer.class)
            .addOptionalEntry("max_cost_base", Integer.class)
            .addOptionalEntry("max_cost_per_level_above_first", Integer.class)
            .addOptionalEntry("anvil_cost", Integer.class)
            .addOptionalEntry("slots", EquipmentSlotGroup.class)
            .addOptionalEntry("is_cursed", Boolean.class)
            .addOptionalEntry("is_treasure", Boolean.class)
            .addOptionalEntry("is_tradeable", Boolean.class)
            .addOptionalEntry("is_discoverable", Boolean.class)
            .addOptionalEntry("is_on_random_loot", Boolean.class)
            .addOptionalEntry("is_on_mob_spawn_equipment", Boolean.class)
            .addOptionalEntry("is_on_traded_equipment", Boolean.class)
            .build();
        Skript.registerExpression(SecEnchantmentRegister.class, Enchantment.class, ExpressionType.COMBINED,
            "register [new] [custom] enchantment");
    }

    private Expression<String> id;
    private Expression<?> description;
    private Expression<?> exclusiveSet;
    private Expression<?> supportedItems;
    private Expression<?> primaryItems;
    private Expression<Integer> weight;
    private Expression<Integer> maxLevel;
    private Expression<Integer> minCostBase;
    private Expression<Integer> minCostPerLevelAboveFirst;
    private Expression<Integer> maxCostBase;
    private Expression<Integer> maxCostPerLevelAboveFirst;
    private Expression<Integer> anvilCost;
    private Expression<EquipmentSlotGroup> slots;
    private Expression<Boolean> isCursed;
    private Expression<Boolean> isTreasure;
    private Expression<Boolean> isTradeable;
    private Expression<Boolean> isDiscoverable;
    private Expression<Boolean> isOnRandomLoot;
    private Expression<Boolean> isOnMobSpawnEquipment;
    private Expression<Boolean> isOnTradedEquipment;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult, SectionNode sectionNode, List<TriggerItem> triggerItems) {
        if (sectionNode == null) return false;
        EntryContainer container = VALIDATOR.validate(sectionNode);
        if (container == null) return false;

        // Definition
        this.id = (Expression<String>) container.getOptional("id", false);
        this.description = (Expression<?>) container.getOptional("description", false);
        this.exclusiveSet = (Expression<?>) container.getOptional("exclusive_set", false);
        this.supportedItems = (Expression<?>) container.getOptional("supported_items", false);
        this.primaryItems = (Expression<?>) container.getOptional("primary_items", false);
        this.weight = (Expression<Integer>) container.getOptional("weight", false);
        this.maxLevel = (Expression<Integer>) container.getOptional("max_level", false);
        this.minCostBase = (Expression<Integer>) container.getOptional("min_cost_base", false);
        this.minCostPerLevelAboveFirst = (Expression<Integer>) container.getOptional("min_cost_per_level_above_first", false);
        this.maxCostBase = (Expression<Integer>) container.getOptional("max_cost_base", false);
        this.maxCostPerLevelAboveFirst = (Expression<Integer>) container.getOptional("max_cost_per_level_above_first", false);
        this.anvilCost = (Expression<Integer>) container.getOptional("anvil_cost", false);
        this.slots = (Expression<EquipmentSlotGroup>) container.getOptional("slots", false);

        // Tag stuff
        this.isCursed = (Expression<Boolean>) container.getOptional("is_cursed", false);
        this.isTreasure = (Expression<Boolean>) container.getOptional("is_treasure", false);
        this.isTradeable = (Expression<Boolean>) container.getOptional("is_tradeable", false);
        this.isDiscoverable = (Expression<Boolean>) container.getOptional("is_discoverable", false);
        this.isOnRandomLoot = (Expression<Boolean>) container.getOptional("is_on_random_loot", false);
        this.isOnMobSpawnEquipment = (Expression<Boolean>) container.getOptional("is_on_mob_spawn_equipment", false);
        this.isOnTradedEquipment = (Expression<Boolean>) container.getOptional("is_on_traded_equipment", false);
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    protected Enchantment @Nullable [] get(Event event) {
        if (this.id == null || this.description == null || this.supportedItems == null) return null;

        NamespacedKey namespacedKey = Util.getNamespacedKey(this.id.getSingle(event), false);
        if (namespacedKey == null || Registry.ENCHANTMENT.get(namespacedKey) != null) return null;

        EnchantmentDefinition.Builder builder = new EnchantmentDefinition.Builder();
        builder.id(namespacedKey);

        Component descriptionComponent = Component.text("Unnamed");
        Object description = this.description.getSingle(event);
        if (description != null) {
            if (description instanceof ComponentWrapper cw) descriptionComponent = cw.getComponent();
            // ComponentWrapper -> Component = Make sure to properly parse colors
            else if (description instanceof String string) descriptionComponent = ComponentWrapper.fromText(string).getComponent();
        }
        builder.description(descriptionComponent);

        if (this.exclusiveSet != null) {
            for (Object object : this.exclusiveSet.getArray(event)) {
                if (object instanceof String string) {
                    builder.exclusiveSetTag(string);
                    break;
                } else if (object instanceof Enchantment enchantment) {
                    builder.addExclusiveSet(enchantment);
                }
            }
        }

        for (Object object : this.supportedItems.getArray(event)) {
            if (object instanceof String string) {
                builder.supportedItemTag(string);
            }
            if (object instanceof ItemType itemType) {
                builder.addSupportedItem(itemType.getMaterial());
            }
        }

        if (this.primaryItems != null) {
            for (Object object : this.primaryItems.getArray(event)) {
                if (object instanceof String string) {
                    builder.primaryItemTag(string);
                }
                if (object instanceof ItemType itemType) {
                    builder.addPrimaryItem(itemType.getMaterial());
                }
            }
        }

        if (this.weight != null) {
            this.weight.getOptionalSingle(event).ifPresent(builder::weight);
        }

        if (this.maxLevel != null) {
            this.maxLevel.getOptionalSingle(event).ifPresent(builder::maxLevel);
        }

        if (this.minCostBase != null && this.minCostPerLevelAboveFirst != null) {
            this.minCostBase.getOptionalSingle(event).ifPresent(builder::minCostBase);
            this.minCostPerLevelAboveFirst.getOptionalSingle(event).ifPresent(builder::minCostPerLevelAboveFirst);
        }

        if (this.maxCostBase != null && this.maxCostPerLevelAboveFirst != null) {
            this.maxCostBase.getOptionalSingle(event).ifPresent(builder::maxCostBase);
            this.maxCostPerLevelAboveFirst.getOptionalSingle(event).ifPresent(builder::maxCostPerLevelAboveFirst);
        }

        if (this.anvilCost != null) {
            this.anvilCost.getOptionalSingle(event).ifPresent(builder::anvilCost);
        }

        if (this.slots != null) {
            for (EquipmentSlotGroup slot : this.slots.getArray(event)) {
                builder.addSlot(slot);
            }
        }

        if (this.isCursed != null) {
            this.isCursed.getOptionalSingle(event).ifPresent(builder::isCursed);
        }

        if (this.isTreasure != null) {
            this.isTreasure.getOptionalSingle(event).ifPresent(builder::isTreasure);
        }

        if (this.isTradeable != null) {
            this.isTradeable.getOptionalSingle(event).ifPresent(builder::isTradeable);
        }

        if (this.isDiscoverable != null) {
            this.isDiscoverable.getOptionalSingle(event).ifPresent(builder::isDiscoverable);
        }

        if (this.isOnRandomLoot != null) {
            this.isOnRandomLoot.getOptionalSingle(event).ifPresent(builder::isOnRandomLoot);
        }

        if (this.isOnMobSpawnEquipment != null) {
            this.isOnMobSpawnEquipment.getOptionalSingle(event).ifPresent(builder::isOnMobSpawnEquipment);
        }

        if (this.isOnTradedEquipment != null) {
            this.isOnTradedEquipment.getOptionalSingle(event).ifPresent(builder::isOnTradedEquipment);
        }

        return new Enchantment[]{builder.build().register()};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Enchantment> getReturnType() {
        return Enchantment.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "register enchantment";
    }

}
