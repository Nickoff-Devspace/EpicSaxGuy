package com.nickoff.epicsaxguy.sounds;

import com.nickoff.epicsaxguy.inits.SoundInit;
import com.nickoff.epicsaxguy.items.EpicSaxItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.TickableSound;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Tickable sound for playing the sax sound in loop
 * **/
@OnlyIn(Dist.CLIENT)
public class SaxSound extends TickableSound {

    private ClientPlayerEntity client;
    private PlayerEntity holder;

    public SaxSound(PlayerEntity holder) {
        super(SoundInit.EPIC_SAX_SOUND.get(), SoundCategory.PLAYERS);
        this.client = Minecraft.getInstance().player;
        this.holder = holder;
        this.looping = true;
    }

    /**
     * Checks if the client distance to a playing sax sound is greater than its range
     * Also, stops the song if the player dropped it, was died or just changed the main hand item
     * */
    @Override
    public void tick() {
        ItemStack stack = holder.getMainHandItem();
        if (stack.getItem() instanceof EpicSaxItem) {
            double max_distance = (double) ((EpicSaxItem) stack.getItem()).calculateRange(stack, holder);
            double distance = client.position().distanceTo(holder.position());
            if (distance < max_distance)
                this.volume = (float) (1.0 - distance / max_distance);
            else
                this.volume = 0.0f;
        } else {
            Minecraft.getInstance().getSoundManager().stop(this);
        }
    }
}
