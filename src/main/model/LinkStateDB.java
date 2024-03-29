package main.model;

import main.Logger;
import main.Router;

import java.util.*;

/**
 * OSPFRouter
 * Created by dwson Son (20420487)
 * on 11/26/16
 * d3son@uwaterloo.ca
 */
public class LinkStateDB {

    private Logger logger = Logger.getInstance();
    private int routerId;
    private CircuitDB[] linkStateDB;

    public LinkStateDB(int routerId) {
        this.routerId = routerId;
        linkStateDB = new CircuitDB[Router.NBR_ROUTER];
    }

    public void putCircuitDB(int routerId, CircuitDB circuitDB) {
        linkStateDB[routerId - 1] = circuitDB;
    }

    public boolean putLinkState(int routerId, LinkCost linkCost) {
        boolean updated;
        if (linkStateDB[routerId - 1] == null){
            LinkCost[] linkCosts = new LinkCost[Router.NBR_ROUTER];
            linkCosts[0] = linkCost;
            linkStateDB[routerId - 1] = new CircuitDB(1, linkCosts);
            updated = true;
        } else {
            updated = linkStateDB[routerId - 1].putLinkCost(linkCost);
        }
        return updated;
    }

    public LinkCost findLinkBetween(int routerId, int minRouterId) {

        if (linkStateDB[routerId-1] == null || linkStateDB[minRouterId-1] == null) return null;
        List<LinkCost> list1 = new ArrayList<>(Arrays.asList(linkStateDB[routerId-1].getLinkCosts()));
        List<LinkCost> list2 = new ArrayList<>(Arrays.asList(linkStateDB[minRouterId-1].getLinkCosts()));
        list1.retainAll(list2);
        list1.removeIf(Objects::isNull);

        for (int i=0; i<list1.size(); i+=1) {
            LinkCost linkCost = list1.get(i);
            if (linkCost == null) continue;
        }
        return list1.size() > 0 ? list1.get(0) : null;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("# Topology database\n");

        for (int i=0; i<Router.NBR_ROUTER; i+=1) {
            if (linkStateDB[i] == null) continue;
            stringBuilder.append(linkStateDB[i].toString(routerId, i+1));
        }
        return stringBuilder.toString();
    }
}
