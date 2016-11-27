package main.model;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * OSPFRouter
 * Created by dwson Son (20420487)
 * on 11/25/16
 * d3son@uwaterloo.ca
 */
public class HelloPacket extends Packet {

    private int linkId;

    public HelloPacket(int routerId, int linkId) {
        super(routerId);
        this.linkId = linkId;
    }

    public int getLinkId() {
        return linkId;
    }

    public byte[] getUDPdata() {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(routerId);
        buffer.putInt(linkId);
        return buffer.array();
    }

    public static HelloPacket parseUDPdata(byte[] UDPdata) throws Exception {
        ByteBuffer buffer = ByteBuffer.wrap(UDPdata);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        int routerId = buffer.getInt();
        int linkId = buffer.getInt();
        return new HelloPacket(routerId, linkId);
    }
}
