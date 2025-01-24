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
import com.shanebeestudios.nms.util.EnchantmentDefinition;
import com.shanebeestudios.skbee.api.util.SimpleEntryValidator;
import com.shanebeestudios.skbee.api.util.Util;
import com.shanebeestudios.skbee.api.wrapper.ComponentWrapper;
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
    "",
    "**NOTES**:",
    "- Custom enchantments cannot be removed at runtime (a restart is the only way to get rid of them).",
    "- If you make a change to your custom enchantment, you'll have to restart your server (reloading the script just won't cut it).",
    "- At the time of parsing scripts, your custom enchantment won't be acknowledged (in Skript), " +
        "that will only happen after it actually registers, this is why this returns itself as an enchantment you can save in a variable.",
    "- The parsed as expression will work, ex: `\"custom:my_enchant\" parsed as enchantment`.",
    "",
    "**ENTRIES**:",
    "I'm only going to touch on a few here, as there are so many to type out, see the above mentioned wiki for full details.",
    "- `id` = Takes in a string to identify your new enchantment, think vanilla \"minecraft:sharpness\".",
    "- `description` = Takes in a text component (from SkBee), this is how your enchantment will show up in lore.",
    "- `exclusive_set` = The enchantments your enchantment will not work with. Either a single string (enchantment tag) or a list of enchantments.",
    "- `supported_items` = The items this enchantment will work on. Either a single string (item tag) or a list of items.",
    "",
    "**WARNINGS**:",
    "Enchantments are not supposed to be created at runtime. This method is super hacky and I highly HIGHLY recommend just using a datapack.",
    "You must ensure 1 of 2 things:",
    "- If your spawn keeps loaded in your world, you must make sure no items are in any chests or anything in that area that contain these enchantments.",
    "- Or just make sure to turn off your spawn chunk radius (set the gamerule `spawnChunkRadius` to 0 for all worlds).",
    "This is due to these enchantments will register to Minecraft via Skript AFTER your world/spawn chunks load.",
    "Do not, I repeat... DO NOT save custom enchantments to variables (Skript will panic trying to load enchantments that arent registered yet). " +
        "RAM/Memory variables are safe!",
    "This will most like cause some great harm, so you've been warned!",
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
@Since("INSERT VERSION")
@SuppressWarnings({"UnstableApiUsage", "unchecked"})
public class SecEnchantmentRegister extends SectionExpression<Enchantment> {

    private static final ComponentWrapper UNNAMED = ComponentWrapper.fromText("Unnamed");
    private static final EntryValidator VALIDATOR;

    static {
        Class<Object>[] exclusiveSetClasses = new Class[]{Enchantment.class, String.class};
        Class<Object>[] itemAndTagClasses = new Class[]{ItemType.class, String.class};
        VALIDATOR = SimpleEntryValidator.builder()
            .addRequiredEntry("id", String.class)
            .addRequiredEntry("description", ComponentWrapper.class)
            .addOptionalEntry("exclusive_set", exclusiveSetClasses)
            .addRequiredEntry("supported_items", itemAndTagClasses)
            .addOptionalEntry("primary_items", itemAndTagClasses)
            .addOptionalEntry("weight", Integer.class)
            .addOptionalEntry("max_level", Integer.class)
            .addOptionalEntry("min_cost_base", Integer.class)
            .addOptionalEntry("min_cost_per_level_above_first", Integer.class)
            .addOptionalEntry("max_cost_base", Integer.class)
            .addOptionalEntry("max_cost_per_level_above_first", Integer.class)
            .addOptionalEntry("anvil_cost", Integer.class)
            .addOptionalEntry("slots", EquipmentSlotGroup.class)
            .build();
        Skript.registerExpression(SecEnchantmentRegister.class, Enchantment.class, ExpressionType.COMBINED,
            "register [new] [custom] enchantment");
    }

    private Expression<String> id;
    private Expression<ComponentWrapper> description;
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

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult, SectionNode sectionNode, List<TriggerItem> triggerItems) {
        if (sectionNode == null) return false;
        EntryContainer container = VALIDATOR.validate(sectionNode);
        if (container == null) return false;

        this.id = (Expression<String>) container.getOptional("id", false);
        this.description = (Expression<ComponentWrapper>) container.getOptional("description", false);
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
        builder.description(this.description.getOptionalSingle(event).orElse(UNNAMED).getComponent());
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
