package model;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * OSPFRouter
 * Created by dwson Son (20420487)
 * on 11/25/16
 * d3son@uwaterloo.ca
 */
public class Packet {

    private final int NBR_ROUTER = 5;

    protected int routerId;

    public Packet(int routerId) {
        this.routerId = routerId;
    }

    public int getRouterId() {
        return routerId;
    }

    public byte[] getUDPdata() {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(routerId);
        return buffer.array();
    }

    public static Packet parseUDPdata(byte[] UDPdata) throws Exception {
        ByteBuffer buffer = ByteBuffer.wrap(UDPdata);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        int routerId = buffer.getInt();
        return new Packet(routerId);
    }
}
