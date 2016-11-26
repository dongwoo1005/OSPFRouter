package model;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * OSPFRouter
 * Created by dwson Son (20420487)
 * on 11/25/16
 * d3son@uwaterloo.ca
 */
public class CircuitDB {

    public static final int NBR_ROUTER = 5;

    private int nbrLink;
    private LinkCost[] linkCosts = new LinkCost[NBR_ROUTER];

    private boolean[] receivedHello;

    public CircuitDB(int nbrLink, LinkCost[] linkCosts) {
        this.nbrLink = nbrLink;
        this.linkCosts = linkCosts;

        receivedHello = new boolean[NBR_ROUTER];
    }

    public int getNbrLink() {
        return nbrLink;
    }

    public LinkCost[] getLinkCosts() {
        return linkCosts;
    }

    public LinkCost getLinkCostAt(int index) {
        return linkCosts[index];
    }

    public boolean didReceiveHelloFrom(int linkId) {
        return receivedHello[linkId - 1];
    }

    public void setReceivedHelloFrom(int linkId) {
        receivedHello[linkId - 1] = true;
    }

    public void putLinkCost(LinkCost linkCost) {
        for (int i=0; i<NBR_ROUTER; i+=1) {
            if (linkCosts[i].getLink() == linkCost.getLink()) {
                linkCosts[i].setCost(linkCost.getCost());
                return;
            }
        }
        if (nbrLink != NBR_ROUTER - 1) {
            linkCosts[nbrLink] = linkCost;
            nbrLink += 1;
        }
    }

    public byte[] getUDPdata() {
        ByteBuffer buffer = ByteBuffer.allocate(4 + 8 * NBR_ROUTER);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(nbrLink);
        for (int i=0; i<NBR_ROUTER; i+=1) {
            buffer.put(linkCosts[i].getUDPdata());
        }
        return buffer.array();
    }

    public static CircuitDB parseUDPdata(byte[] UDPdata) throws Exception {
        ByteBuffer buffer = ByteBuffer.wrap(UDPdata);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        int nbrLink = buffer.getInt();
        LinkCost[] linkCosts = new LinkCost[NBR_ROUTER];
        for (int i=0; i<nbrLink; i+=1) {
            byte data[] = new byte[8];
            buffer.get(data, 0, 8);
            linkCosts[i] = LinkCost.parseUDPdata(data);
        }
        return new CircuitDB(nbrLink, linkCosts);
    }

    public int findCostByLink(int link) {

        int cost = Integer.MAX_VALUE;
        for (int i=0; i<nbrLink; i+=1) {
            if (linkCosts[i].getLink() == link ) return linkCosts[i].getCost();
        }
        return cost;
    }
}