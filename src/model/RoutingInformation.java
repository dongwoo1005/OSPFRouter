package model;

/**
 * OSPFRouter
 * Created by dwson Son (20420487)
 * on 11/25/16
 * d3son@uwaterloo.ca
 */
public class RoutingInformation {

    public static final int LOCAL = -1;

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
        this.path = path;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        if (path == LOCAL) stringBuilder.append("Local, ");
        else {
            stringBuilder.append("R");
            stringBuilder.append(path);
            stringBuilder.append(", ");
        }
        stringBuilder.append(cost);
        return stringBuilder.toString();
    }
}
