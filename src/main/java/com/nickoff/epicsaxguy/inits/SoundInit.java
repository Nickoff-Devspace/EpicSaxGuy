package com.nickoff.epicsaxguy.inits;

import com.nickoff.epicsaxguy.EpicSaxGuy;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.RegistryObject;

public class SoundInit {

    public static RegistryObject<SoundEvent> EPIC_SAX_SOUND = Init.SOUNDS.register("item.epic_sax_sound",
            () -> new SoundEvent(new ResourceLocation(EpicSaxGuy.MOD_ID, "item.epic_sax_sound")));

    public static void register() {}

}
