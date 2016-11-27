package main.model;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * OSPFRouter
 * Created by dwson Son (20420487)
 * on 11/25/16
 * d3son@uwaterloo.ca
 */
public class LSPDUPacket extends Packet {

    private int sender, linkId, cost, via;

    public LSPDUPacket(int sender, int routerId, int linkId, int cost, int via) {
        super(routerId);
        this.sender = sender;
        this.linkId = linkId;
        this.cost = cost;
        this.via = via;
    }

    public int getSender() {
        return sender;
    }

    public int getLinkId() {
        return linkId;
    }

    public int getCost() {
        return cost;
    }

    public int getVia() {
        return via;
    }

    public void setSender(int sender) {
        this.sender = sender;
    }

    public void setVia(int via) {
        this.via = via;
    }

    public byte[] getUDPdata() {
        ByteBuffer buffer = ByteBuffer.allocate(20);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(sender);
        buffer.putInt(routerId);
        buffer.putInt(linkId);
        buffer.putInt(cost);
        buffer.putInt(via);
        return buffer.array();
    }

    public static LSPDUPacket parseUDPdata(byte[] UDPdata) throws Exception {
        ByteBuffer buffer = ByteBuffer.wrap(UDPdata);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        int sender = buffer.getInt();
        int routerId = buffer.getInt();
        int linkId = buffer.getInt();
        int cost = buffer.getInt();
        int via = buffer.getInt();
        return new LSPDUPacket(sender, routerId, linkId, cost, via);
    }
}
