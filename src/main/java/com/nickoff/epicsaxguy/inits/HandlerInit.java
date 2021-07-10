package com.nickoff.epicsaxguy.inits;

import com.nickoff.epicsaxguy.events.SaxAnvilRecipesHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;

public class HandlerInit {

    public static final SaxAnvilRecipesHandler SAX_ANVIL_RECIPES = new SaxAnvilRecipesHandler();

    public static void register()
    {
        IEventBus ebus = MinecraftForge.EVENT_BUS;

        ebus.register(SAX_ANVIL_RECIPES);
    }

}
