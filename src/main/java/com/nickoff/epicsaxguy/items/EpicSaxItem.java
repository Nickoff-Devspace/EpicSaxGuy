package com.nickoff.epicsaxguy.items;

import com.nickoff.epicsaxguy.inits.EnchantmentInit;
import com.nickoff.epicsaxguy.inits.SoundInit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.TickableSound;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

import java.util.List;

public class EpicSaxItem extends Item {

    private static int MAX_STEPS = 11;
    private int step = 0;
    private long lastTick = 0;

    private TickableSound sound;

    private static final int defaultRange = 3;

    public EpicSaxItem() {
        super((new Properties()).stacksTo(1).tab(ItemGroup.TAB_COMBAT));
        sound = null;
    }

    /** Fired once every 4 ticks **/
    @Override
    public ActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
        if (!level.isClientSide())
            lastTick = level.getGameTime();
        return ActionResult.pass(player.getItemInHand(hand));
    }

    /** Just used as trick to get loop without subscribing an event
     * Calculates when epic sax procedure should be executing or not */
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
                if (step <= MAX_STEPS)
                step += 1;
            }

        if (player instanceof PlayerEntity)
            useSax(level, ((PlayerEntity) player), stack);

        super.inventoryTick(stack, level, player, ticks, p_77663_5_);
    }

    /** Starts the sax procedure if the sax animation is completed
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
                int range = defaultRange;
                if (EnchantmentHelper.getEnchantmentLevel(EnchantmentInit.MEGA_RANGE.get(), (LivingEntity) player) > 0)
                    range = 120;
                else
                    range *= (EnchantmentHelper.getEnchantmentLevel(EnchantmentInit.RANGE.get(),
                        (LivingEntity) player)+1);

                BlockPos cornerA = new BlockPos(player.getPosition(0));
                BlockPos cornerB = new BlockPos(cornerA.getX()+range, cornerA.getY()+range, cornerA.getZ()+range);
                cornerA = new BlockPos(cornerA.getX()-range, cornerA.getY()-range, cornerA.getZ()-range);

                List<MobEntity> mobs = level.getNearbyEntities(MobEntity.class, EntityPredicate.DEFAULT, player, new AxisAlignedBB(cornerA, cornerB));

                for(MobEntity mob : mobs){
                    if (EnchantmentHelper.getEnchantmentLevel(EnchantmentInit.IGNITE.get(), (LivingEntity) player) > 0)
                        mob.setSecondsOnFire(10);
                    if (EnchantmentHelper.getEnchantmentLevel(EnchantmentInit.EXPLODE.get(), (LivingEntity) player) > 0)
                        level.explode(player, mob.getX(), mob.getY(), mob.getZ(), 1, Explosion.Mode.DESTROY);
                    if (EnchantmentHelper.getEnchantmentLevel(EnchantmentInit.LEVITATE.get(), (LivingEntity) player) > 0)
                        mob.addEffect(new EffectInstance(Effects.LEVITATION, 200));
                    if (EnchantmentHelper.getEnchantmentLevel(EnchantmentInit.SLOWNESS.get(), (LivingEntity) player) > 0)
                        mob.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 200));
                }
            }
        }
    }

    /** Instantiates the sound object **/
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
