package com.nickoff.epicsaxguy.items;

import com.nickoff.epicsaxguy.inits.ItemInit;
import com.nickoff.epicsaxguy.inits.SoundInit;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.Sound;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.audio.TickableSound;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifierManager;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.extensions.IForgeBakedModel;
import net.minecraftforge.common.model.animation.AnimationStateMachine;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Random;

public class EpicSaxItem extends Item {

    private static int MAX_STEPS = 11;
    private int step = 0;
    private long lastTick = 0;

    private TickableSound sound;

    private int range;
    private boolean explode;
    private boolean ignite;
    private boolean levitation;
    private boolean slowness;

    public EpicSaxItem() {
        super((new Properties()).stacksTo(1).tab(ItemGroup.TAB_COMBAT));
        sound = null;
        range = 3;
        explode = false;
        ignite = false;
        levitation = false;
        slowness = false;
    }

    /** Fired once every 4 ticks **/
    @Override
    public ActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
        if (!level.isClientSide())
            lastTick = level.getGameTime();
        return ActionResult.pass(player.getItemInHand(hand));
    }

    /** Just used as trick to get loop without subscribing an event
     * Calculates when epic sax should be executing or not */
    @Override
    public void inventoryTick(ItemStack stack, World level, Entity player, int ticks, boolean p_77663_5_) {
        if (!level.isClientSide())
            //Replaced by 8 to avoid race condition
            if ((lastTick + 8/*4*/) < level.getGameTime()) {
                if (Minecraft.getInstance().getSoundManager().isActive(sound))
                    Minecraft.getInstance().getSoundManager().stop(sound);
                step = 0;
            }
            else {
                if (EnchantmentHelper.getFireAspect(((LivingEntity) player)) == 0)
                    stack.enchant(Enchantments.FIRE_ASPECT, 1);
                if (step <= MAX_STEPS)
                step += 1;
            }

        if (player instanceof PlayerEntity)
            useSax(level, ((PlayerEntity) player), stack);

        super.inventoryTick(stack, level, player, ticks, p_77663_5_);
    }

    /** Starts the killing procedure if the sax animation is completed
     * @param level the world it is gonna scan for entities
     * @param player the player it is gonna be used to take the starting scan position
     * **/
    private void useSax(World level, PlayerEntity player, ItemStack stack)
    {
        if (step >= MAX_STEPS)
        {
            if (level.isClientSide()) {
                instantiateSound();
                if (!Minecraft.getInstance().getSoundManager().isActive(sound))
                    Minecraft.getInstance().getSoundManager().play(sound);
            }
            else
            {
                BlockPos cornerA = new BlockPos(player.getPosition(0));
                BlockPos cornerB = new BlockPos(cornerA.getX()+3, cornerA.getY()+3, cornerA.getZ()+3);
                cornerA = new BlockPos(cornerA.getX() -3, cornerA.getY()-3, cornerA.getZ()-3);

                List<MobEntity> mobs = level.getNearbyEntities(MobEntity.class, EntityPredicate.DEFAULT, player, new AxisAlignedBB(cornerA, cornerB));

                for(MobEntity mob : mobs){
                    if (EnchantmentHelper.getFireAspect(player) > 0)
                        mob.setSecondsOnFire(10);
                    level.explode(player, mob.getX(), mob.getY(), mob.getZ(), 1, Explosion.Mode.DESTROY);
                }
            }
        }
    }

    private void instantiateSound()
    {
        if (sound == null)
            sound = new TickableSound(SoundInit.EPIC_SAX_SOUND.get(), SoundCategory.RECORDS) {
                @Override
                public void tick() {

                }
            };
    }

    /** Returns the current step property of the animation **/
    public int getStepProperty(){ return step; }

}
