package main.model;

import main.MyLogger;

import java.util.*;

/**
 * OSPFRouter
 * Created by dwson Son (20420487)
 * on 11/26/16
 * d3son@uwaterloo.ca
 */
public class LinkStateDB {

    private MyLogger logger = MyLogger.getInstance();
    private int routerId;
    private CircuitDB[] linkStateDB;

    public LinkStateDB(int routerId) {
        this.routerId = routerId;
        linkStateDB = new CircuitDB[CircuitDB.NBR_ROUTER];
    }

    public void putCircuitDB(int routerId, CircuitDB circuitDB) {
        linkStateDB[routerId - 1] = circuitDB;
    }

    public void putLinkState(int routerId, LinkCost linkCost) {
        if (linkStateDB[routerId - 1] == null){
            LinkCost[] linkCosts = new LinkCost[CircuitDB.NBR_ROUTER];
            linkCosts[0] = linkCost;
            linkStateDB[routerId - 1] = new CircuitDB(1, linkCosts);
        } else {
            linkStateDB[routerId - 1].putLinkCost(linkCost);
        }
    }

    public LinkCost findLinkCostBetween(int routerId, int minRouterId) {

        logger.log("findLinkCostBetween");
        if (linkStateDB[routerId-1] == null || linkStateDB[minRouterId-1] == null) return null;
        List<LinkCost> list1 = new ArrayList<>(Arrays.asList(linkStateDB[routerId-1].getLinkCosts()));
        List<LinkCost> list2 = new ArrayList<>(Arrays.asList(linkStateDB[minRouterId-1].getLinkCosts()));
        list1.retainAll(list2);
        list1.removeIf(Objects::isNull);

        logger.log("isNeighbor list size " + list1.size());
        for (int i=0; i<list1.size(); i+=1) {
            LinkCost linkCost = list1.get(i);
            if (linkCost == null) {
                logger.log("linkCost null");
                continue;
            }
            logger.log("L" + list1.get(i).getLink() + ", " + list1.get(i).getCost());
        }
        return list1.size() > 0 ? list1.get(0) : null;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("# Topology database\n");

        for (int i=0; i<CircuitDB.NBR_ROUTER; i+=1) {
            if (linkStateDB[i] == null) continue;
            stringBuilder.append(linkStateDB[i].toString(routerId, i+1));
        }
        return stringBuilder.toString();
    }
}
