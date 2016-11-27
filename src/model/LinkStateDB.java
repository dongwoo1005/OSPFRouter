package model;

/**
 * OSPFRouter
 * Created by dwson Son (20420487)
 * on 11/26/16
 * d3son@uwaterloo.ca
 */
public class LinkStateDB {

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
