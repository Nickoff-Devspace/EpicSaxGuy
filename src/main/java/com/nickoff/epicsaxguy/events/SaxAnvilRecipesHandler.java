package com.nickoff.epicsaxguy.events;

import com.nickoff.epicsaxguy.inits.EnchantmentInit;
import com.nickoff.epicsaxguy.inits.ItemInit;
import com.nickoff.epicsaxguy.items.EpicSaxItem;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;

import java.util.HashMap;
import java.util.Map;

public class SaxAnvilRecipesHandler {

    /** A hashmap containing the enchantments for each item when an anvil event is fired with a sax **/
    public static final HashMap<Item, RegistryObject<Enchantment>> enchantList = new HashMap<>();
    static {
        enchantList.put(Items.NETHER_STAR, EnchantmentInit.MEGA_RANGE);
        enchantList.put(Items.TNT, EnchantmentInit.EXPLODE);
        enchantList.put(Items.FLINT_AND_STEEL, EnchantmentInit.IGNITE);
        enchantList.put(Items.FEATHER, EnchantmentInit.LEVITATE);
        enchantList.put(Items.GOLD_BLOCK, EnchantmentInit.RANGE);
        enchantList.put(Items.SOUL_SAND, EnchantmentInit.SLOWNESS);
    }

    /** Increases an enchantment level whenever possible and returns a new updated instance of a sax
     * @param sax the sax stack used for it
     * @param enchant the enchantment to be applied
     * **/
    public ItemStack anvilGetEnchantedOutput(ItemStack sax, Enchantment enchant)
    {
        ItemStack output = ItemStack.EMPTY;;
        Map<Enchantment, Integer> enchants = EnchantmentHelper.getEnchantments(sax);

        int enchantLevel = 0;
        if (enchants.containsKey(enchant))
            enchantLevel = enchants.get(enchant);

        if (enchantLevel < enchant.getMaxLevel())
        {
            enchants.put(enchant, enchantLevel + 1);
            output = ItemInit.EPIC_SAX.get().getDefaultInstance();
            for (Enchantment key : enchants.keySet())
                output.enchant(key, enchants.get(key));
        }
        return output;
    }

    /** Adds anvil recipes for a sax **/
    @SubscribeEvent
    public void anvilEnchants(AnvilUpdateEvent ev)
    {
        if (ev.getLeft().getItem() instanceof EpicSaxItem)
        {
            Item right = ev.getRight().getItem();
            if (enchantList.containsKey(right)) {
                ev.setOutput(anvilGetEnchantedOutput(ev.getLeft(), enchantList.get(right).get()));
                ev.setMaterialCost(1);
                ev.setCost(1);
                ev.setResult(Event.Result.ALLOW);
            }
            else
                ev.setResult(Event.Result.DENY);
        }
    }

}
