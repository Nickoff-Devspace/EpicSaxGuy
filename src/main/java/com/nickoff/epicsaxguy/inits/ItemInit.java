package com.nickoff.epicsaxguy.inits;

import com.nickoff.epicsaxguy.EpicSaxGuy;
import com.nickoff.epicsaxguy.items.EpicSaxItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemModelsProperties;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.RegistryObject;

public class ItemInit {

    public static final RegistryObject<Item> EPIC_SAX =
            Init.ITEMS.register("epicsax", () -> new EpicSaxItem());

    /** Empty function just to make sure Java is instantiating its constants right **/
    public static void register()
    {
    }

    /** Register items animation properties **/
    public static void register_animations()
    {
        ItemModelsProperties.register(EPIC_SAX.get(),
                new ResourceLocation(EpicSaxGuy.MOD_ID, "loading"),
                (stack, world, entity) -> {
                    if (entity != null && entity.getMainHandItem() == stack &&
                        stack.getItem() instanceof EpicSaxItem)
                        return ((EpicSaxItem) stack.getItem()).getStepProperty();
                    else
                        return 0;
                });
    }

}
