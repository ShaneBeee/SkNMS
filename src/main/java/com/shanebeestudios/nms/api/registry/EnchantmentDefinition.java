package com.shanebeestudios.nms.api.registry;

import com.shanebeestudios.nms.api.util.RegistryUtils;
import io.papermc.paper.adventure.PaperAdventure;
import net.kyori.adventure.text.Component;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
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

    private final NamespacedKey id;
    private final Enchantment enchantment;
    private final TagData tagData;

    private EnchantmentDefinition(NamespacedKey id, Enchantment enchantment, TagData tagData) {
        this.id = id;
        this.enchantment = enchantment;
        this.tagData = tagData;
    }

    public NamespacedKey getId() {
        return id;
    }

    public Enchantment getEnchantment() {
        return enchantment;
    }

    public TagData getTagData() {
        return tagData;
    }

    public org.bukkit.enchantments.Enchantment register() {
        return RegistryUtils.registerEnchantment(this);
    }

    @SuppressWarnings("UnstableApiUsage")
    public static class Builder {
        NamespacedKey id;
        Component description;
        List<org.bukkit.enchantments.Enchantment> exclusiveSet = new ArrayList<>();
        TagKey<Enchantment> exclusiveSetTag;
        List<Material> supportedItems = new ArrayList<>();
        TagKey<Item> supportedItemsTag;
        List<Material> primaryItems = new ArrayList<>();
        TagKey<Item> primaryItemsTag;
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

        public void id(NamespacedKey id) {
            this.id = id;
        }

        public void description(Component description) {
            this.description = description;
        }

        public void addExclusiveSet(org.bukkit.enchantments.Enchantment exclusiveSet) {
            this.exclusiveSet.add(exclusiveSet);
        }

        public boolean exclusiveSetTag(NamespacedKey tag) {
            Registry<Enchantment> enchantRegistry = RegistryUtils.getEnchantRegistry();
            TagKey<Enchantment> tagKey = RegistryUtils.getTagKey(enchantRegistry, tag.toString());
            if (tagKey != null) {
                Optional<HolderSet.Named<Enchantment>> holders = enchantRegistry.get(tagKey);
                if (holders.isPresent()) {
                    this.exclusiveSetTag = tagKey;
                    return true;
                }
            }
            return false;
        }

        public boolean supportedItemTag(NamespacedKey tag) {
            Registry<Item> itemRegistry = RegistryUtils.getItemRegistry();
            TagKey<Item> tagKey = RegistryUtils.getTagKey(itemRegistry, tag.toString());
            if (tagKey != null) {
                Optional<HolderSet.Named<Item>> holders = itemRegistry.get(tagKey);
                if (holders.isPresent()) {
                    this.supportedItemsTag = tagKey;
                    return true;
                }
            }
            return false;
        }

        public void addSupportedItem(Material item) {
            this.supportedItems.add(item);
        }

        public boolean primaryItemTag(NamespacedKey tag) {
            Registry<Item> itemRegistry = RegistryUtils.getItemRegistry();
            TagKey<Item> tagKey = RegistryUtils.getTagKey(itemRegistry, tag.toString());
            if (tagKey != null) {
                Optional<HolderSet.Named<Item>> holders = itemRegistry.get(tagKey);
                if (holders.isPresent()) {
                    this.primaryItemsTag = tagKey;
                    return true;
                }
            }
            return false;
        }

        public void addPrimaryItem(Material item) {
            this.primaryItems.add(item);
        }

        public void weight(int weight) {
            this.weight = weight;
        }

        public void maxLevel(int maxLevel) {
            this.maxLevel = maxLevel;
        }

        public void minCostBase(int minCostBase) {
            this.minCostBase = minCostBase;
        }

        public void minCostPerLevelAboveFirst(int minCostPerLevelAboveFirst) {
            this.minCostPerLevelAboveFirst = minCostPerLevelAboveFirst;
        }

        public void maxCostBase(int maxCostBase) {
            this.maxCostBase = maxCostBase;
        }

        public void maxCostPerLevelAboveFirst(int maxCostPerLevelAboveFirst) {
            this.maxCostPerLevelAboveFirst = maxCostPerLevelAboveFirst;
        }

        public void anvilCost(int anvilCost) {
            this.anvilCost = anvilCost;
        }

        public void addSlot(org.bukkit.inventory.EquipmentSlotGroup slot) {
            this.slots.add(slot);
        }

        public void isCursed(boolean cursed) {
            this.isCursed = cursed;
        }

        public void isTreasure(boolean treasure) {
            this.isTreasure = treasure;
        }

        public void isTradeable(boolean tradeable) {
            this.isTradeable = tradeable;
        }

        public void isDiscoverable(boolean discoverable) {
            this.isDiscoverable = discoverable;
        }

        public void isOnRandomLoot(boolean onRandomLoot) {
            this.isOnRandomLoot = onRandomLoot;
        }

        public void isOnMobSpawnEquipment(boolean onMobSpawnEquipment) {
            this.isOnMobSpawnEquipment = onMobSpawnEquipment;
        }

        public void isOnTradedEquipment(boolean onTradedEquipment) {
            this.isOnTradedEquipment = onTradedEquipment;
        }

        private HolderSet<Enchantment> createExclusiveSet() {
            HolderSet<Enchantment> exclusiveSet = HolderSet.empty();
            Registry<Enchantment> enchantRegistry = RegistryUtils.getEnchantRegistry();
            if (this.exclusiveSetTag != null) {
                Optional<HolderSet.Named<Enchantment>> holders = enchantRegistry.get(this.exclusiveSetTag);
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

        private HolderSet<Item> createItemSet(TagKey<Item> tagKey, List<Material> sets) {
            HolderSet<Item> itemSet = HolderSet.empty();
            Registry<Item> itemRegistry = RegistryUtils.getItemRegistry();
            if (tagKey != null) {
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
        public boolean isCursed;
        public boolean isTreasure;
        public boolean isTradeable;
        public boolean isDiscoverable;
        public boolean isOnRandomLoot;
        public boolean isOnMobSpawnEquipment;
        public boolean isOnTradedEquipment;

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
