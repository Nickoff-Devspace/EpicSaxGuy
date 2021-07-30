package com.nickoff.epicsaxguy.networking;

import net.minecraft.network.PacketBuffer;

import java.util.UUID;

/**
 * Sax Message class that is sent to the clients by the server
 * */
public class SaxUseToClient {
    /** Player is using a sax **/
    private boolean using;
    /** Player's id **/
    private UUID uuid;
    private boolean messageIsValid;

    public SaxUseToClient(boolean using, UUID uuid)
    {
        this.using = using;
        this.uuid = uuid;
        this.messageIsValid = true;
    }

    public SaxUseToClient()
    {
        this.using = false;
        this.uuid = null;
        this.messageIsValid = true;
    }

    public boolean isMessageValid(){ return this.messageIsValid; }
    public UUID getUUID(){ return this.uuid; }
    public boolean isUsing(){ return this.using; }

    public static SaxUseToClient decode(PacketBuffer buf)
    {
        SaxUseToClient retval = new SaxUseToClient();
        try {
            boolean using = buf.readBoolean();
            UUID uuid = buf.readUUID();
            retval.uuid = uuid;
            retval.using = using;
        } catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            System.out.println("Exception while reading TargetEffectMessageToClient: " + e);
            return retval;
        }
        retval.messageIsValid = true;
        return retval;
    }

    public void encode(PacketBuffer buf)
    {
        if (!messageIsValid) return;
        buf.writeBoolean(this.using);
        buf.writeUUID(this.uuid);
    }

}
