package main.model;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * OSPFRouter
 * Created by dwson Son (20420487)
 * on 11/25/16
 * d3son@uwaterloo.ca
 */
public class LinkCost {

    private int link, cost;

    public LinkCost(int link, int cost) {
        this.link = link;
        this.cost = cost;
    }

    public int getLink() {
        return link;
    }

    public int getCost() {
        return cost;
    }

    public void setLink(int link) {
        this.link = link;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LinkCost linkCost = (LinkCost) o;

        if (link != linkCost.link) return false;
        return cost == linkCost.cost;
    }

    public byte[] getUDPdata() {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(link);
        buffer.putInt(cost);
        return buffer.array();
    }

    public static LinkCost parseUDPdata(byte[] UDPdata) throws Exception {
        ByteBuffer buffer = ByteBuffer.wrap(UDPdata);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        int link = buffer.getInt();
        int cost = buffer.getInt();
        return new LinkCost(link, cost);
    }
}
