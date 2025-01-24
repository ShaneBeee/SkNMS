package com.shanebeestudios.nms.util;

import io.papermc.paper.adventure.PaperAdventure;
import net.kyori.adventure.text.Component;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.enchantments.CraftEnchantment;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class EnchantmentDefinition {

    NamespacedKey id;
    Enchantment enchantment;
    TagData tagData;

    private EnchantmentDefinition(NamespacedKey id, Enchantment enchantment, TagData tagData) {
        this.id = id;
        this.enchantment = enchantment;
        this.tagData = tagData;
    }

    public org.bukkit.enchantments.Enchantment register() {
        return RegistryUtils.registerEnchantment(this);
    }

    @SuppressWarnings({"UnusedReturnValue", "UnstableApiUsage"})
    public static class Builder {
        NamespacedKey id;
        Component description;
        List<org.bukkit.enchantments.Enchantment> exclusiveSet = new ArrayList<>();
        String exclusiveSetTag;
        List<Material> supportedItems = new ArrayList<>();
        String supportedItemsTag;
        List<Material> primaryItems = new ArrayList<>();
        String primaryItemsTag;
        int weight = 1;
        int maxLevel = 1;
        int minCostBase = 1;
        int minCostPerLevelAboveFirst = 1;
        int maxCostBase = 1;
        int maxCostPerLevelAboveFirst = 1;
        int anvilCost = 1;
        boolean isCursed = false;
        boolean isTreasure = false;
        boolean isTradeable = false;
        boolean isDiscoverable = false;
        boolean isOnRandomLoot = false;
        boolean isOnMobSpawnEquipment = false;
        boolean isOnTradedEquipment = false;
        List<org.bukkit.inventory.EquipmentSlotGroup> slots = new ArrayList<>();

        public Builder id(NamespacedKey id) {
            this.id = id;
            return this;
        }

        public Builder description(Component description) {
            this.description = description;
            return this;
        }

        public Builder addExclusiveSet(org.bukkit.enchantments.Enchantment exclusiveSet) {
            this.exclusiveSet.add(exclusiveSet);
            return this;
        }

        public Builder exclusiveSetTag(String exclusiveSetTag) {
            this.exclusiveSetTag = exclusiveSetTag.replace("#", "");
            return this;
        }

        public Builder supportedItemTag(String supportedItemTag) {
            this.supportedItemsTag = supportedItemTag.replace("#", "");
            return this;
        }

        public Builder addSupportedItem(Material item) {
            this.supportedItems.add(item);
            return this;
        }

        public Builder addPrimaryItem(Material item) {
            this.primaryItems.add(item);
            return this;
        }

        public Builder primaryItemTag(String primaryItemTag) {
            this.primaryItemsTag = primaryItemTag.replace("#", "");
            return this;
        }

        public Builder weight(int weight) {
            this.weight = weight;
            return this;
        }

        public Builder maxLevel(int maxLevel) {
            this.maxLevel = maxLevel;
            return this;
        }

        public Builder minCostBase(int minCostBase) {
            this.minCostBase = minCostBase;
            return this;
        }

        public Builder minCostPerLevelAboveFirst(int minCostPerLevelAboveFirst) {
            this.minCostPerLevelAboveFirst = minCostPerLevelAboveFirst;
            return this;
        }

        public Builder maxCostBase(int maxCostBase) {
            this.maxCostBase = maxCostBase;
            return this;
        }

        public Builder maxCostPerLevelAboveFirst(int maxCostPerLevelAboveFirst) {
            this.maxCostPerLevelAboveFirst = maxCostPerLevelAboveFirst;
            return this;
        }

        public Builder anvilCost(int anvilCost) {
            this.anvilCost = anvilCost;
            return this;
        }

        public Builder addSlot(org.bukkit.inventory.EquipmentSlotGroup slot) {
            this.slots.add(slot);
            return this;
        }

        public Builder isCursed(boolean cursed) {
            this.isCursed = cursed;
            return this;
        }

        public Builder isTreasure(boolean treasure) {
            this.isTreasure = treasure;
            return this;
        }

        public Builder isTradeable(boolean tradeable) {
            this.isTradeable = tradeable;
            return this;
        }

        public Builder isDiscoverable(boolean discoverable) {
            this.isDiscoverable = discoverable;
            return this;
        }

        public Builder isOnRandomLoot(boolean onRandomLoot) {
            this.isOnRandomLoot = onRandomLoot;
            return this;
        }

        public Builder isOnMobSpawnEquipment(boolean onMobSpawnEquipment) {
            this.isOnMobSpawnEquipment = onMobSpawnEquipment;
            return this;
        }

        public Builder isOnTradedEquipment(boolean onTradedEquipment) {
            this.isOnTradedEquipment = onTradedEquipment;
            return this;
        }

        private HolderSet<Enchantment> createExclusiveSet() {
            HolderSet<Enchantment> exclusiveSet = HolderSet.empty();
            MappedRegistry<Enchantment> enchantRegistry = RegistryUtils.getEnchantRegistry();
            if (this.exclusiveSetTag != null) {
                TagKey<Enchantment> tagKey = RegistryUtils.getTagKey(enchantRegistry, this.exclusiveSetTag);
                Optional<HolderSet.Named<Enchantment>> holders = enchantRegistry.get(tagKey);
                if (holders.isPresent()) {
                    exclusiveSet = holders.get();
                }
            } else if (!this.exclusiveSet.isEmpty()) {
                List<Holder<Enchantment>> enchants = new ArrayList<>();
                for (org.bukkit.enchantments.Enchantment bukkitEnchant : this.exclusiveSet) {
                    Enchantment enchantment = CraftEnchantment.bukkitToMinecraft(bukkitEnchant);
                    enchants.add(enchantRegistry.wrapAsHolder(enchantment));
                }
                exclusiveSet = HolderSet.direct(enchants);
            }
            return exclusiveSet;
        }

        private HolderSet<Item> createItemSet(String tag, List<Material> sets) {
            HolderSet<Item> itemSet = HolderSet.empty();
            MappedRegistry<Item> itemRegistry = RegistryUtils.getItemRegistry();
            if (tag != null) {
                TagKey<Item> tagKey = RegistryUtils.getTagKey(itemRegistry, tag);
                Optional<HolderSet.Named<Item>> holders = itemRegistry.get(tagKey);
                if (holders.isPresent()) {
                    itemSet = holders.get();
                }
            } else if (!sets.isEmpty()) {
                List<Holder<Item>> itemSetList = new ArrayList<>();
                for (Material material : sets) {
                    if (!material.isItem()) continue;

                    Item item = CraftMagicNumbers.getItem(material);
                    itemSetList.add(itemRegistry.wrapAsHolder(item));
                }
                itemSet = HolderSet.direct(itemSetList);
            }
            return itemSet;
        }

        private HolderSet<Item> createSupportedItems() {
            return createItemSet(this.supportedItemsTag, this.supportedItems);
        }

        private Optional<HolderSet<Item>> createPrimaryItems() {
            HolderSet<Item> itemSet;
            if (this.primaryItems.isEmpty() && this.primaryItemsTag == null) {
                itemSet = createSupportedItems();
            } else {
                itemSet = createItemSet(this.primaryItemsTag, this.primaryItems);
            }
            return Optional.of(itemSet);
        }

        private List<EquipmentSlotGroup> createSlots() {
            List<EquipmentSlotGroup> groups = new ArrayList<>();
            for (org.bukkit.inventory.EquipmentSlotGroup slot : this.slots) {
                groups.add(EquipmentSlotGroup.valueOf(slot.toString().toUpperCase(Locale.ROOT)));
            }
            return groups;
        }

        public EnchantmentDefinition build() {
            Enchantment.EnchantmentDefinition definition = new Enchantment.EnchantmentDefinition(
                createSupportedItems(),
                createPrimaryItems(),
                Math.clamp(this.weight, 1, 1024),
                Math.clamp(this.maxLevel, 1, 255),
                new Enchantment.Cost(this.minCostBase, this.minCostPerLevelAboveFirst),
                new Enchantment.Cost(this.maxCostBase, this.maxCostPerLevelAboveFirst),
                this.anvilCost,
                createSlots());

            net.minecraft.network.chat.Component vanilla = PaperAdventure.asVanilla(this.description);
            Enchantment enchantment = new Enchantment(
                vanilla,
                definition,
                createExclusiveSet(),
                DataComponentMap.EMPTY);

            TagData tagData = new TagData(this.isCursed, this.isTreasure, this.isTradeable, this.isDiscoverable, this.isOnRandomLoot, this.isOnMobSpawnEquipment, this.isOnTradedEquipment);
            return new EnchantmentDefinition(this.id, enchantment, tagData);
        }
    }

    public static class TagData {
        boolean isCursed;
        boolean isTreasure;
        boolean isTradeable;
        boolean isDiscoverable;
        boolean isOnRandomLoot;
        boolean isOnMobSpawnEquipment;
        boolean isOnTradedEquipment;

        public TagData(boolean isCursed, boolean isTreasure, boolean isTradeable, boolean isDiscoverable,
                       boolean isOnRandomLoot, boolean isOnMobSpawnEquipment, boolean isOnTradedEquipment) {
            this.isCursed = isCursed;
            this.isTreasure = isTreasure;
            this.isTradeable = isTradeable;
            this.isDiscoverable = isDiscoverable;
            this.isOnRandomLoot = isOnRandomLoot;
            this.isOnMobSpawnEquipment = isOnMobSpawnEquipment;
            this.isOnTradedEquipment = isOnTradedEquipment;
        }
    }

}
