package model;

/**
 * OSPFRouter
 * Created by dwson Son (20420487)
 * on 11/26/16
 * d3son@uwaterloo.ca
 */
public class RoutingInformationBase {

    RoutingInformation[] routingInformationBase;

    public RoutingInformationBase(int routerId) {
        routingInformationBase = new RoutingInformation[6];
        routingInformationBase[routerId - 1] = new RoutingInformation(routerId, 0);
    }

    public void setPath(int destRouter, int pathRouter) {
        System.out.println("setPath: " + destRouter + " " + pathRouter);
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
        return routingInformationBase[destRouter-1].getCost();
    }
}
