package com.nickoff.epicsaxguy.networking;

import com.nickoff.epicsaxguy.inits.NetworkInit;
import com.nickoff.epicsaxguy.items.EpicSaxItem;
import net.minecraft.client.world.ClientWorld;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Used by the client to handle server messages related to the sax
 **/
public class SaxUseClientHandler {
    /**
     * Executed when a message is received by the server.
     * If a message doesn't have any problems, it processes it
     * @param message
     * @param ctxSupplier
     **/
    public static void onMessageReceived(final SaxUseToClient message, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        LogicalSide sideReceived = ctx.getDirection().getReceptionSide();
        ctx.setPacketHandled(true);

        if (sideReceived != LogicalSide.CLIENT) {
            System.out.println("Message sent to wrong side " + ctx.getDirection().getReceptionSide());
            return;
        }
        if (!message.isMessageValid()) {
            System.out.println("Message invalid " + message.toString());
            return;
        }

        Optional<ClientWorld> clientWorld = LogicalSidedProvider.CLIENTWORLD.get(sideReceived);
        if (!clientWorld.isPresent()) {
            System.out.println("No client world");
            return;
        }

        ctx.enqueueWork(() -> processMessage(clientWorld.get(), message));
    }

    /**
     * Processes a message, i.e. makes the sax play the song if a user used it
     * @param worldClient
     * @param message
     * */
    private static void processMessage(ClientWorld worldClient, SaxUseToClient message)
    {
        EpicSaxItem.setPlayerUsing(message.getUUID(), message.isUsing());
    }

    /**
     * Checks if a message uses the same protocol between client/server
     * @param protocolVersion
     * */
    public static boolean isThisProtocolAcceptedByClient(String protocolVersion) {
        return NetworkInit.MESSAGE_PROTOCOL_VERSION.equals(protocolVersion);
    }
}
