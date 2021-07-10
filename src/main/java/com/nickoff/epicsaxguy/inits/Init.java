package com.nickoff.epicsaxguy.inits;

import com.nickoff.epicsaxguy.EpicSaxGuy;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class Init {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, EpicSaxGuy.MOD_ID);

    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, EpicSaxGuy.MOD_ID);

    public static final DeferredRegister<Enchantment> ENCHANTMENTS =
            DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, EpicSaxGuy.MOD_ID);

    /** Registers all mod artifacts **/
    public static void register()
    {
        ItemInit.register();
        SoundInit.register();
        EnchantmentInit.register();
        IEventBus ebus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(ebus);
        SOUNDS.register(ebus);
        ENCHANTMENTS.register(ebus);
        HandlerInit.register();
    }

    /** Register post artifacts, such as animations **/
    public static void register_client()
    {
        ItemInit.register_animations();
    }

}
