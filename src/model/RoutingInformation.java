package model;

/**
 * OSPFRouter
 * Created by dwson Son (20420487)
 * on 11/25/16
 * d3son@uwaterloo.ca
 */
public class RoutingInformation {

    private int path;   // adjacent routerId
    private int cost;

    public RoutingInformation(int path, int cost) {
        this.path = path;
        this.cost = cost;
    }

    public int getPath() {
        return path;
    }

    public int getCost() {
        return cost;
    }

    public void setPath(int path) {
        this.path = cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }
}
