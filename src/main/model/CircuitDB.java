package main.model;

import main.Router;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashSet;

/**
 * OSPFRouter
 * Created by dwson Son (20420487)
 * on 11/25/16
 * d3son@uwaterloo.ca
 */
public class CircuitDB {

    private int nbrLink;
    private LinkCost[] linkCosts = new LinkCost[Router.NBR_ROUTER];

    HashSet<Integer> receivedHello;

    public CircuitDB(int nbrLink, LinkCost[] linkCosts) {
        this.nbrLink = nbrLink;
        this.linkCosts = linkCosts;

        receivedHello = new HashSet<>();
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
        return receivedHello.contains(linkId);
    }

    public void setReceivedHelloFrom(int linkId) {
        receivedHello.add(linkId);
    }

    public boolean putLinkCost(LinkCost linkCost) {
        boolean updated = false;
        for (int i=0; i<nbrLink; i+=1) {
            if (linkCosts[i].getLink() == linkCost.getLink()) {
                if (linkCost.getCost() != linkCosts[i].getCost()) {
                    linkCosts[i].setCost(linkCost.getCost());
                    updated = true;
                } else {
                    updated = false;
                }
                return updated;
            }
        }
        if (nbrLink != Router.NBR_ROUTER - 1) {
            linkCosts[nbrLink] = linkCost;
            nbrLink += 1;
            updated = true;
        }
        return updated;
    }

    public static CircuitDB parseUDPdata(byte[] UDPdata) throws Exception {
        ByteBuffer buffer = ByteBuffer.wrap(UDPdata);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        int nbrLink = buffer.getInt();
        LinkCost[] linkCosts = new LinkCost[Router.NBR_ROUTER];
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

    public String toString(int routerId, int destRouterId) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("R");
        stringBuilder.append(routerId);
        stringBuilder.append(" -> R");
        stringBuilder.append(destRouterId);
        stringBuilder.append(" nbr link ");
        stringBuilder.append(nbrLink);
        stringBuilder.append("\n");
        for (int i=0; i<nbrLink; i+=1) {
            stringBuilder.append("R");
            stringBuilder.append(routerId);
            stringBuilder.append(" -> R");
            stringBuilder.append(destRouterId);
            stringBuilder.append(" link ");
            stringBuilder.append(linkCosts[i].getLink());
            stringBuilder.append(" cost ");
            stringBuilder.append(linkCosts[i].getCost());
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }
}
