package com.nickoff.epicsaxguy.items;

import com.nickoff.epicsaxguy.inits.EnchantmentInit;
import com.nickoff.epicsaxguy.inits.SoundInit;
import com.nickoff.epicsaxguy.sounds.SaxSound;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.TickableSound;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.world.NoteBlockEvent;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EpicSaxItem extends Item {

    private static final int STEPS_UNTIL_USE = 11;
    private static final int USE_ANIM_START = 12;
    private static final int MAX_STEPS = 20;

    private static final Map<Integer, TickableSound> sounds = new HashMap<>();

    private static final int defaultRange = 3;

    //to be removed
    private int step = 0;

    public EpicSaxItem() {
        super((new Properties()).stacksTo(1).tab(ItemGroup.TAB_COMBAT));
    }

    /** Fired once every 4 ticks **/
    @Override
    public ActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
        setLastTicks(level, player.getMainHandItem());
        return ActionResult.pass(player.getItemInHand(hand));
    }

    /** Updates lastSavedTicks for server and client side
     * @param level the world the ticks are gonna be extracted
     * @param stack a saxophone the player is holding
     * **/
    private void setLastTicks(World level, ItemStack stack)
    {
        if (!level.isClientSide())
            setLastSavedTick(stack, level.getGameTime());
    }

    /** Just used as trick to get loop without subscribing an event
     * Calculates when epic sax procedure should be executing or not */
    @Override
    public void inventoryTick(ItemStack stack, World level, Entity player, int ticks, boolean p_77663_5_) {
        if (player instanceof PlayerEntity)
            nextStep(stack, level, (PlayerEntity) player);

        super.inventoryTick(stack, level, player, ticks, p_77663_5_);
    }

    /** Loads a sax and starts its killing procedure if loaded
     * @param stack the sax stack
     * @param level the world the player is in
     * @param player the player is holding the sax
     * **/
    private void nextStep(ItemStack stack, World level, PlayerEntity player)
    {
        if (!level.isClientSide())
        {
            int lastStep = getUseStep(stack);
            int newStep = calculateNextStep(lastStep, getLastSavedTick(stack), level.getGameTime());
            setUseStep(stack, newStep);
        }

        if (getUseStep(stack) > STEPS_UNTIL_USE)
            useSax(level, player, stack);
        else
            unuseSax(level, player, stack);
    }

    /** Calculates next use step
     * @param step the current step [0-MAX_STEPS]
     * @param lastTick the last saved tick
     * @param currTick the game current tick
     * **/
    private int calculateNextStep(int step, long lastTick, long currTick)
    {
        //8 because it is only fired once every 4 ticks + 4 of an error margin
        if ((lastTick + 8) < currTick)
            return 0;
        else {
            if (step == MAX_STEPS)
                return USE_ANIM_START;
            return step+1;
        }
    }

    /** Starts the sax procedure if the sax animation is completed
     * @param level the world it is gonna scan for entities
     * @param player the player it is gonna be used to take the starting scan position
     * **/
    private void useSax(World level, PlayerEntity player, ItemStack stack)
    {
        if (level.isClientSide())
            playSound(player);
        else
        {
            int range = calculateRange(stack, player);

            BlockPos cornerA = new BlockPos(player.position());
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

    /** Plays the sax song in a player's poisiton
     * @param holder the player who just use the sax
     * **/
    @OnlyIn(Dist.CLIENT)
    private void playSound(PlayerEntity holder)
    {
        TickableSound sound = getOrCreateSound(holder);
        if (!Minecraft.getInstance().getSoundManager().isActive(sound))
            Minecraft.getInstance().getSoundManager().play(sound);
    }

    /** Executes the stopping procedures when player stopped using a sax
     * @param level the level the player is in
     * @param player the player who stopped using the sax
     * @param stack the sax stack
     * **/
    private void unuseSax(World level, PlayerEntity player, ItemStack stack)
    {
        if (level.isClientSide())
            stopSound(player);
    }

    /** Stops the sax song of a player who is using a sax
     * @param holder the player who stopped using a sax
     * **/
    @OnlyIn(Dist.CLIENT)
    private void stopSound(PlayerEntity holder) {
        TickableSound sound = getOrCreateSound(holder);
        if (Minecraft.getInstance().getSoundManager().isActive(sound))
            Minecraft.getInstance().getSoundManager().stop(sound);
    }

    /** Returns the player's respective sax sound
     * @param holder the player who is holding a sax
     * **/
    @OnlyIn(Dist.CLIENT)
    private TickableSound getOrCreateSound(PlayerEntity holder)
    {
        if (!sounds.containsKey(holder.getId())) {
            sounds.put(holder.getId(), new SaxSound(holder));
        }
        return sounds.get(holder.getId());
    }

    /** Calculates the range of the sax
     * @param stack the sax stack
     * @param player the player is holding the sax
     * **/
    public int calculateRange(ItemStack stack, PlayerEntity player)
    {
        int range = defaultRange;
        if (EnchantmentHelper.getEnchantmentLevel(EnchantmentInit.MEGA_RANGE.get(), (LivingEntity) player) > 0)
            range = 120;
        else
            range *= (EnchantmentHelper.getEnchantmentLevel(EnchantmentInit.RANGE.get(),
                    (LivingEntity) player)+1);
        return range;
    }

    /** Returns the current step property of the animation **/
    public int getStepProperty(){ return step; }

    private int getUseStep(ItemStack stack){ return stack.getOrCreateTag().getInt("step"); }
    private long getLastSavedTick(ItemStack stack){ return stack.getOrCreateTag().getLong("serverTick"); }

    private void setUseStep(ItemStack stack, int val){ stack.getOrCreateTag().putInt("step", val); }
    private void setLastSavedTick(ItemStack stack, long tick){ stack.getOrCreateTag().putLong("serverTick", tick); }

}
