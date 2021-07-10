package com.nickoff.epicsaxguy.inits;

import com.nickoff.epicsaxguy.enchantments.*;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraftforge.fml.RegistryObject;

public class EnchantmentInit {

    public static final RegistryObject<Enchantment> EXPLODE = Init.ENCHANTMENTS.register("explode_enchantment",
            () -> new ExplodeEnchantment(Enchantment.Rarity.VERY_RARE, EquipmentSlotType.MAINHAND));

    public static final RegistryObject<Enchantment> LEVITATE = Init.ENCHANTMENTS.register("levitate_enchantment",
            () -> new LevitateEnchantment(Enchantment.Rarity.VERY_RARE, EquipmentSlotType.MAINHAND));

    public static final RegistryObject<Enchantment> SLOWNESS = Init.ENCHANTMENTS.register("slowness_enchantment",
            () -> new SlownessEnchantment(Enchantment.Rarity.VERY_RARE, EquipmentSlotType.MAINHAND));

    public static final RegistryObject<Enchantment> IGNITE = Init.ENCHANTMENTS.register("ignite_enchantment",
            () -> new IgniteEnchantment(Enchantment.Rarity.VERY_RARE, EquipmentSlotType.MAINHAND));

    public static final RegistryObject<Enchantment> RANGE = Init.ENCHANTMENTS.register("range_enchantment",
            () -> new RangeEnchantment(Enchantment.Rarity.VERY_RARE, EquipmentSlotType.MAINHAND));

    public static final RegistryObject<Enchantment> MEGA_RANGE = Init.ENCHANTMENTS.register("mega_range_enchantment",
            () -> new MegaRangeEnchantment(Enchantment.Rarity.VERY_RARE, EquipmentSlotType.MAINHAND));

    public static void register(){}

}
