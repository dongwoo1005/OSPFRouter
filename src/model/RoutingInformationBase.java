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
        routingInformationBase = new RoutingInformation[CircuitDB.NBR_ROUTER];
        routingInformationBase[routerId - 1] = new RoutingInformation(routerId, 0);
    }

    public void setPath(int destRouter, int pathRouter) {
        routingInformationBase[destRouter - 1].setPath(pathRouter);
    }

    public void setCost(int destRouter, int cost) {
        routingInformationBase[destRouter - 1].setCost(cost);
    }

    public int getCostToDest(int destRotuer) {
        return routingInformationBase[destRotuer-1].getCost();
    }
}
