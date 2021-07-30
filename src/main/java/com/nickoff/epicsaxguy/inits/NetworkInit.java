package com.nickoff.epicsaxguy.inits;

import com.nickoff.epicsaxguy.EpicSaxGuy;
import com.nickoff.epicsaxguy.networking.SaxUseClientHandler;
import com.nickoff.epicsaxguy.networking.SaxUseToClient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.util.Optional;

import static net.minecraftforge.fml.network.NetworkDirection.PLAY_TO_CLIENT;

public class NetworkInit {

    public static SimpleChannel simpleChannel;

    public static final byte SAXUSE_MESSAGE_ID = 12;

    public static final String MESSAGE_PROTOCOL_VERSION = "1.0";

    public static final ResourceLocation simpleChannelRL = new ResourceLocation(EpicSaxGuy.MOD_ID,
            "saxuse_message");

    public static void register(){

        simpleChannel = NetworkRegistry.newSimpleChannel(simpleChannelRL, () -> MESSAGE_PROTOCOL_VERSION,
                SaxUseClientHandler::isThisProtocolAcceptedByClient,
                SaxUseClientHandler::isThisProtocolAcceptedByClient);

        simpleChannel.registerMessage(SAXUSE_MESSAGE_ID, SaxUseToClient.class,
                SaxUseToClient::encode, SaxUseToClient::decode,
                SaxUseClientHandler::onMessageReceived,
                Optional.of(PLAY_TO_CLIENT));
    }
}
