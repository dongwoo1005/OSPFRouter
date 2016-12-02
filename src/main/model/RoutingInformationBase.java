package main.model;

import main.Router;

/**
 * OSPFRouter
 * Created by dwson Son (20420487)
 * on 11/26/16
 * d3son@uwaterloo.ca
 */
public class RoutingInformationBase {

    private int routerId;
    private RoutingInformation[] routingInformationBase;

    public RoutingInformationBase(int routerId) {
        this.routerId = routerId;
        routingInformationBase = new RoutingInformation[Router.NBR_ROUTER];
        routingInformationBase[routerId - 1] = new RoutingInformation(RoutingInformation.LOCAL, 0);
    }

    public void setPath(int destRouter, int pathRouter) {
        if (routingInformationBase[destRouter - 1] == null) {
            routingInformationBase[destRouter - 1] = new RoutingInformation(pathRouter, Integer.MAX_VALUE);
        } else {
            routingInformationBase[destRouter - 1].setPath(pathRouter);
        }
    }

    public void setCost(int destRouter, int cost) {
        if (routingInformationBase[destRouter - 1] == null) {
            routingInformationBase[destRouter - 1] = new RoutingInformation(Integer.MAX_VALUE, cost);
        }
        routingInformationBase[destRouter - 1].setCost(cost);
    }

    public int getCostToDest(int destRouter) {
        if (routingInformationBase[destRouter-1] == null) return Integer.MAX_VALUE;
        return routingInformationBase[destRouter-1].getCost();
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("# RIB\n");
        for (int i = 0; i< Router.NBR_ROUTER; i+=1) {
            stringBuilder.append("R");
            stringBuilder.append(routerId);
            stringBuilder.append(" -> R");
            stringBuilder.append(i+1);
            stringBuilder.append(" -> ");
            if (routingInformationBase[i] == null) stringBuilder.append("INF");
            else stringBuilder.append(routingInformationBase[i].toString());
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }
}
