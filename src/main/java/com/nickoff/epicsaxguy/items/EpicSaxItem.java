package com.nickoff.epicsaxguy.items;

import com.nickoff.epicsaxguy.inits.EnchantmentInit;
import com.nickoff.epicsaxguy.inits.NetworkInit;
import com.nickoff.epicsaxguy.networking.SaxUseToClient;
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
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

class SaxClientData
{
    private int step;
    private long lastTick;

    public SaxClientData()
    {
        this.step = 0;
        this.lastTick = 0L;
    }

    public int getStep() {
        return step;
    }
    public void setStep(int step) {
        this.step = step;
    }
    public long getLastTick() { return lastTick; }
    public void setLastTick(long lastTick) { this.lastTick = lastTick; }
}

class SaxServerData
{
    private boolean lastMessage;
    private long lastTick;
    private int step;

    public SaxServerData()
    {
        this.lastTick = 0L;
        this.step = 0;
    }

    public boolean getLastMessage() {
        return lastMessage;
    }
    public void setLastMessage(boolean updated) {
        this.lastMessage = updated;
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

    private static Map<UUID, SaxSound> sounds = new HashMap<>();
    private static SaxClientData clientData = new SaxClientData();

    private static final Map<UUID, SaxServerData> serverData = new HashMap<>();

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
        clientData.setLastTick(level.getGameTime());
    }

    /** If not exists, generate server data for a saxophonist, and/or sets its last tick
     * @param level the world the player is in
     * @param player the player is using the sax
     * **/
    private void handleServerSaxData(World level, PlayerEntity player)
    {
        if(!serverData.containsKey(player.getUUID()))
            serverData.put(player.getUUID(), new SaxServerData());
        SaxServerData sdata = serverData.get(player.getUUID());
        sdata.setLastTick(level.getGameTime());
    }

    /** Just used as trick to get loop without subscribing an event
     * Calculates when epic sax procedure should be executing or not */
    @Override
    public void inventoryTick(ItemStack stack, World level, Entity player, int ticks, boolean p_77663_5_) {
        if (player instanceof PlayerEntity &&
           ((PlayerEntity) player).getMainHandItem() == stack)
        {
            if (level.isClientSide())
                clientNextStep(stack, level, (PlayerEntity) player);
            else if (serverData.containsKey(player.getUUID()))
                serverNextStep(stack, level, (PlayerEntity) player);
        }

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
    private void clientNextStep(ItemStack stack, World level, PlayerEntity player)
    {
        int step = calculateNextStep(clientData.getStep(), clientData.getLastTick(), level.getGameTime());
        clientData.setStep(step);
        if (step > STEPS_UNTIL_USE)
            clientUseSax(level, player, stack);
        else if (step == 0)
            clientUnuseSax(level, player, stack);
    }

    /** Loads a sax server and starts its killing procedure if loaded
     * @param stack the sax stack
     * @param level the world the player is in
     * @param player the player is holding the sax
     * **/
    private void serverNextStep(ItemStack stack, World level, PlayerEntity player)
    {
        SaxServerData sdata = serverData.get(player.getUUID());
        int step = calculateNextStep(sdata.getStep(), sdata.getLastTick(), level.getGameTime());
        sdata.setStep(step);
        if (step > STEPS_UNTIL_USE) {
            serverUseSax(level, player, stack);
            sendUseMessageToClients(true, player);
        }
        else if (step == 0)
        {
            sendUseMessageToClients(false, player);
            serverUnuseSax(level, player, stack);
        }
    }

    /** Sends message from server to client to notify clients of a sax usage
     * @param using a flag indicating whether the player is using the sax
     * @param player the player is using the sax
     * **/
    private void sendUseMessageToClients(boolean using, PlayerEntity player)
    {
        SaxServerData sdata = serverData.get(player.getUUID());
        if (using != sdata.getLastMessage()) {
            NetworkInit.simpleChannel.send(PacketDistributor.ALL.noArg(),
                    new SaxUseToClient(using, player.getUUID()));
            sdata.setLastMessage(using);
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
    }

    /** Plays the sax song in a player's poisiton
     * @param holder the player who just use the sax
     * **/
    @OnlyIn(Dist.CLIENT)
    private static void playSound(UUID holder)
    {
        TickableSound sound = sounds.get(holder);
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
        clientData.setStep(0);
    }

    /** Stops the sax song of a player who is using a sax
     * @param holder the player who stopped using a sax
     * **/
    @OnlyIn(Dist.CLIENT)
    private static void stopSound(UUID holder) {
        TickableSound sound = sounds.get(holder);
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
        SaxServerData sdata = serverData.get(player.getUUID());
        sdata.setStep(0);
    }

    /** Gets sax animation step for a player
     * @param player the player
     * **/
    public static int getAnimStep(PlayerEntity player)
    {
        if (player == Minecraft.getInstance().player)
            return clientData.getStep();
        return 0;
    }

    /** Makes a player to start listening to a song when the server alerts it
     * of sax using
     * @param playerId the UUID of the player
     * @param using sets the UUID of the player
     * **/
    public static void setPlayerUsing(UUID playerId, boolean using)
    {
        PlayerEntity player = Minecraft.getInstance().level.getPlayerByUUID(playerId);

        if (sounds.containsKey(playerId))
            stopSound(playerId);
        sounds.put(playerId, new SaxSound(player));

        if (using)
            playSound(playerId);
    }

}
