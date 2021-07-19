package com.nickoff.epicsaxguy.items;

import com.nickoff.epicsaxguy.inits.EnchantmentInit;
import com.nickoff.epicsaxguy.sounds.SaxSound;
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
import net.minecraft.item.UseAction;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
class SaxClientData
{
    private long lastTick;
    private int step;
    private SaxSound song;

    public SaxClientData(PlayerEntity holder)
    {
        this.step = 0;
        this.song = new SaxSound(holder);
        this.lastTick = 0L;
    }

    public long getLastTick() {
        return lastTick;
    }

    public void setLastTick(long lastTick) {
        this.lastTick = lastTick;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public SaxSound getSong() {
        return song;
    }

    public void setSong(SaxSound song) {
        this.song = song;
    }
}

class SaxServerData
{
    private long lastTick;
    private int step;

    public SaxServerData()
    {
        this.lastTick = 0L;
        this.step = 0;
    }

    public long getLastTick() {
        return lastTick;
    }

    public void setLastTick(long lastTick) {
        this.lastTick = lastTick;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }
}

public class EpicSaxItem extends Item {

    private static final int STEPS_UNTIL_USE = 11;
    private static final int USE_ANIM_START = 12;
    private static final int MAX_STEPS = 20;

    private static final Map<Integer, SaxClientData> clientData = new HashMap<>();
    private static final Map<Integer, SaxServerData> serverData = new HashMap<>();

    private static final int defaultRange = 3;

    public EpicSaxItem() {
        super((new Properties()).stacksTo(1).tab(ItemGroup.TAB_COMBAT));
    }

    /** Fired once every 4 ticks **/
    @Override
    public ActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
        if (level.isClientSide())
            handleClientSaxData(level, player);
        else
            handleServerSaxData(level, player);
        return ActionResult.pass(player.getItemInHand(hand));
    }

    /** If not exists, generate client data for a saxophonist, and/or sets it to using
     * @param player the player is holding a sax
     * **/
    private void handleClientSaxData(World level, PlayerEntity player)
    {
        if (!clientData.containsKey(player.getId()))
            clientData.put(player.getId(), new SaxClientData(player));
        SaxClientData sdata = clientData.get(player.getId());
        sdata.setLastTick(level.getGameTime());
    }

    /** If not exists, generate server data for a saxophonist, and/or sets its last tick
     * @param level the world the player is in
     * @param player the player is using the sax
     * **/
    private void handleServerSaxData(World level, PlayerEntity player)
    {
        if(!serverData.containsKey(player.getId()))
            serverData.put(player.getId(), new SaxServerData());
        SaxServerData sdata = serverData.get(player.getId());
        sdata.setLastTick(level.getGameTime());
    }

    /** Just used as trick to get loop without subscribing an event
     * Calculates when epic sax procedure should be executing or not */
    @Override
    public void inventoryTick(ItemStack stack, World level, Entity player, int ticks, boolean p_77663_5_) {
        if (player instanceof PlayerEntity &&
                (clientData.containsKey(player.getId()) || serverData.containsKey(player.getId())))
            nextStep(stack, level, (PlayerEntity) player);

        super.inventoryTick(stack, level, player, ticks, p_77663_5_);
    }

    /** Sets its animation to none **/
    @Override
    public UseAction getUseAnimation(ItemStack p_77661_1_) {
        return UseAction.NONE;
    }

    /** Loads a sax and starts its killing procedure if loaded
     * @param stack the sax stack
     * @param level the world the player is in
     * @param player the player is holding the sax
     * **/
    private void nextStep(ItemStack stack, World level, PlayerEntity player)
    {
        if (level.isClientSide())
        {
            SaxClientData sdata = clientData.get(player.getId());
            int step = calculateNextStep(sdata.getStep(), sdata.getLastTick(), level.getGameTime());
            sdata.setStep(step);
            if (step > STEPS_UNTIL_USE)
                clientUseSax(level, player, stack);
            else if (step == 0)
                clientUnuseSax(level, player, stack);
        }
        else
        {
            SaxServerData sdata = serverData.get(player.getId());
            int step = calculateNextStep(sdata.getStep(), sdata.getLastTick(), level.getGameTime());
            sdata.setStep(step);
            if (step > STEPS_UNTIL_USE)
                serverUseSax(level, player, stack);
            else if (step == 0)
                serverUnuseSax(level, player, stack);
        }
    }

    /** Calculates next use step
     * @param step the current step [0-MAX_STEPS]
     * @param lastTick the last saved tick
     * @param currTick the game current tick
     * **/
    private static int calculateNextStep(int step, long lastTick, long currTick)
    {
        //8 because it is only fired once every 4 ticks + 4 of an error margin
        if ((lastTick + 8) < currTick)
            return 0;
        else {
            return calculateNextStep(step);
        }
    }

    /** Calculates next use step
     * @param step the current step [0-MAX_STEPS]
     * **/
    private static int calculateNextStep(int step)
    {
        if (step == MAX_STEPS)
            return USE_ANIM_START;
        return step+1;
    }

    /** Starts the client sax procedure
     * @param level the world it is gonna scan for entities
     * @param player the player it is gonna be used to take the starting scan position
     * **/
    private void clientUseSax(World level, PlayerEntity player, ItemStack stack)
    {
        if (level.isClientSide())
            playSound(player);
    }

    /** Plays the sax song in a player's poisiton
     * @param holder the player who just use the sax
     * **/
    @OnlyIn(Dist.CLIENT)
    private void playSound(PlayerEntity holder)
    {
        TickableSound sound = clientData.get(holder.getId()).getSong();
        if (!Minecraft.getInstance().getSoundManager().isActive(sound))
            Minecraft.getInstance().getSoundManager().play(sound);
    }

    /** Executes the client stopping procedures when player stopped using a sax
     * @param level the level the player is in
     * @param player the player who stopped using the sax
     * @param stack the sax stack
     * **/
    private void clientUnuseSax(World level, PlayerEntity player, ItemStack stack)
    {
        stopSound(player);
        SaxClientData sdata = clientData.get(player.getId());
        sdata.setStep(0);
    }

    /** Stops the sax song of a player who is using a sax
     * @param holder the player who stopped using a sax
     * **/
    @OnlyIn(Dist.CLIENT)
    private void stopSound(PlayerEntity holder) {
        TickableSound sound = clientData.get(holder.getId()).getSong();
        if (Minecraft.getInstance().getSoundManager().isActive(sound))
            Minecraft.getInstance().getSoundManager().stop(sound);
    }

    /** Starts the server sax procedure
     * @param level the world it is gonna scan for entities
     * @param player the player it is gonna be used to take the starting scan position
     * **/
    private void serverUseSax(World level, PlayerEntity player, ItemStack stack)
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

    /** Executes the server stopping procedures when player stopped using a sax
     * @param level the level the player is in
     * @param player the player who stopped using the sax
     * @param stack the sax stack
     * **/
    private void serverUnuseSax(World level, PlayerEntity player, ItemStack stack)
    {
        SaxServerData sdata = serverData.get(player.getId());
        sdata.setStep(0);
    }

    /** Gets sax animation step for a player
     * @param player the player
     * **/
    public static int getAnimStep(PlayerEntity player)
    {
        if (clientData.containsKey(player.getId()))
            return clientData.get(player.getId()).getStep();
        return 0;
    }
}
