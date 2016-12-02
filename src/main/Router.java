package main;

import main.model.*;

import java.io.IOException;
import java.net.*;
import java.util.HashSet;

/**
 * OSPFRouter
 * Created by dwson Son (20420487)
 * on 11/25/16
 * d3son@uwaterloo.ca
 */
public class Router {

    public static final int NBR_ROUTER = 5;

    public static int routerId;

    private static int nsePort;
    private static int routerPort;
    private static String nseHost;

    private static String logMessage;
    private static Logger logger;

    private static DatagramSocket udpSocket;
    private static byte[] receiveData = new byte[1024];

    private static CircuitDB circuitDB;
    private static LinkStateDB linkStateDB;
    private static boolean linkStateDBUpdated;
    private static RoutingInformationBase routingInformationBase;    // RIB

    private static HelloPacket helloPacket;
    private static LSPDUPacket lspduPacket;

    private static void checkArgs(String[] args) {

        if (args.length != 4) {
            System.err.println("Usage: router <router_id> <nse_host> <nse_port> <router_port>");
            System.exit(1);
        }
    }

    private static void initialize(String[] args) throws IOException {

        routerId = Integer.valueOf(args[0]);
        nseHost = args[1];
        nsePort = Integer.valueOf(args[2]);
        routerPort = Integer.valueOf(args[3]);

        udpSocket = new DatagramSocket(routerPort);
        linkStateDB = new LinkStateDB(routerId);
        routingInformationBase = new RoutingInformationBase(routerId);

        logger = Logger.getInstance();
    }

    private static void sendPacket(DatagramSocket udpSocket, Packet packet, String message) throws IOException {

        // Send packet with message
        byte[] sendData = packet.getUDPdata();
        InetAddress ipAddress = InetAddress.getByName(nseHost);
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ipAddress, nsePort);
        udpSocket.send(sendPacket);
        logger.log(message);
    }

    private static void printDBs() {
        logger.log("");
        logger.log(linkStateDB.toString());
        logger.log(routingInformationBase.toString());
    }

    private static void sendInit() throws IOException {
        // First
        // Each router must send an INIT packet to the NSE (Network State Emulator)
        // containing the router's id
        Packet sendInitPacket = new Packet(routerId);   // INIT packet
        logMessage = "R" + routerId + " : ---> INIT";
        sendPacket(udpSocket, sendInitPacket, logMessage);
    }

    private static void receiveCircuitDB() throws Exception {
        // After NSE receives an INIT packet from each router,
        // the NSE will send to each router the circuit database associated with that router.
        // The circuit database will be sent in a circuit_DB structure
        DatagramPacket receiveCircuitDBPacket = new DatagramPacket(receiveData, receiveData.length);
        udpSocket.receive(receiveCircuitDBPacket);            // wait until received
        circuitDB = CircuitDB.parseUDPdata(receiveData);

        logMessage = "R" + routerId + " : <--- circuit database";
        logger.log(logMessage);

        linkStateDB.putCircuitDB(routerId, circuitDB);
        logger.log("");
        logger.log("## Update LinkStateDB");
        printDBs();
    }

    private static void sendHello() throws IOException {
        // Then each router must send a HELLO packet to all its neighbors.
        for (int i=0; i<circuitDB.getNbrLink(); i+=1) {
            int linkId = circuitDB.getLinkCostAt(i).getLink();
            HelloPacket sendHelloPacket = new HelloPacket(routerId, linkId);
            logMessage = "R" + routerId + " : ---> HELLO via link " + linkId;
            sendPacket(udpSocket, sendHelloPacket, logMessage);
        }
    }

    private static void receiveHello() throws Exception {
        // Receive HELLO packet

        helloPacket = HelloPacket.parseUDPdata(receiveData);
        logMessage = "R" + routerId + " : <--- HELLO via link " + + helloPacket.getLinkId() + " from router " + helloPacket.getRouterId();
        logger.log(logMessage);
    }

    private static void updateRIBWithHello() {
        // Update RIB
        routingInformationBase.setPath(helloPacket.getRouterId(), helloPacket.getRouterId());
        routingInformationBase.setCost(helloPacket.getRouterId(), circuitDB.findCostByLink(helloPacket.getLinkId()));
        logger.log("");
        logger.log("## Update RIB");
        printDBs();
    }

    private static void respondToHelloWithSetOfLSPDUs() throws IOException {
        // Respond by sending a set of LS PDU packets
        for (int i=0; i<circuitDB.getNbrLink(); i+=1) {
            int linkId = circuitDB.getLinkCostAt(i).getLink();
            int cost = circuitDB.getLinkCostAt(i).getCost();
            int via = helloPacket.getLinkId();
            LSPDUPacket sendLSPDUPacket =
                    new LSPDUPacket(routerId, routerId, linkId, cost, via);
            logMessage = "R" + routerId + " : ---> LS PDU via link " + via
                    + ": routerId " + routerId + ", linkId " + linkId + ", cost " + cost;
            sendPacket(udpSocket, sendLSPDUPacket, logMessage);
        }
    }

    private static void receiveLSPDU() throws Exception {
        // Receive LS PDU packet
        lspduPacket = LSPDUPacket.parseUDPdata(receiveData);
        logMessage = "R" + routerId + " : <--- LS PDU via link " + lspduPacket.getVia()
                + " from router " + lspduPacket.getSender() + ": routerId " + lspduPacket.getRouterId()
                + ", linkId " + lspduPacket.getLinkId() + ", cost " + lspduPacket.getCost();
    }

    private static void updateLinkStateDB() {
        // Update its link state database
        LinkCost linkCost = new LinkCost(lspduPacket.getLinkId(), lspduPacket.getCost());
        linkStateDBUpdated = linkStateDB.putLinkState(lspduPacket.getRouterId(), linkCost);
        logMessage += linkStateDBUpdated ? " - Updated" : " - Duplicate - Not Updated";
        logger.log(logMessage);
        if (linkStateDBUpdated) {
            logger.log("");
            logger.log("## Update LinkStateDB");
            printDBs();
        }
    }

    private static void sendNeighborsTheLSPDU() throws IOException {
        // Send to all its neighbors the LS PDU
        for (int i=0; i<circuitDB.getNbrLink(); i+=1) {
            int linkId = circuitDB.getLinkCostAt(i).getLink();
            // except the one that sends the LS PDU and those from which the router did not receive a HELLO
            if (linkId == lspduPacket.getVia() || !circuitDB.didReceiveHelloFrom(linkId) ) continue;
            lspduPacket.setSender(routerId);
            lspduPacket.setVia(linkId);
            logMessage = "R" + routerId + " : ---> LS PDU via link " + lspduPacket.getVia()
                    + ": routerId " + lspduPacket.getRouterId() + ", linkId " + lspduPacket.getLinkId()
                    + ", cost " + lspduPacket.getCost();
            sendPacket(udpSocket, lspduPacket, logMessage);
        }
    }

    private static void updateRIBWithSPUsingDijkstra() {
        // Use the Dijkstra algorithm using its link state database
        // to determine the shortest (minimum) path cost to each destination R

        // Initialization:
        HashSet<Integer> N = new HashSet<>();
        N.add(routerId);
        RoutingInformationBase D = new RoutingInformationBase(routerId);    // cost to Destination with predecessor router

        for (int v=1; v<=NBR_ROUTER; v+=1) {
            if (routerId == v) continue;
            LinkCost link = linkStateDB.findLinkBetween(routerId, v);
            if (link != null) {     // if v adjacent to R routerId
                D.setPath(v, routerId);
                D.setCost(v, link.getCost());
            } else {
                D.setCost(v, Integer.MAX_VALUE);
            }
        }

        // Loop:
        while (N.size() != NBR_ROUTER) {

            // find w not in N such that D(w) is a minimum:
            int w=-1;
            int costToW=0;
            for(int i=1; i<=NBR_ROUTER; i+=1) {
                if (N.contains(i)) continue;
                int costToDest = D.getCostToDest(i);
                if (w == -1 || costToDest < costToW) {
                    w = i;
                    costToW = costToDest;
                }
            }

            N.add(w);

            // update D(v) for all v adjacent to w and not in N:
            for (int v=1; v<=NBR_ROUTER; v+=1) {
                if (N.contains(v)) continue;
                LinkCost link = linkStateDB.findLinkBetween(v, w);
                if (link == null) continue;
                int newCost = D.getCostToDest(w) + link.getCost();
                if (newCost < D.getCostToDest(v)) {
                    D.setPath(v, w);
                    D.setCost(v, newCost);
                }
            }
        }

        // backtrack predecessor router to find the shortest path to take from routerId
        for (int dest=1; dest<=NBR_ROUTER; dest+=1) {
            if (dest == routerId) continue;

            int newCostToDest = D.getCostToDest(dest);
            if (newCostToDest == Integer.MAX_VALUE || newCostToDest == Integer.MIN_VALUE) continue;

            int oldCostToDest = routingInformationBase.getCostToDest(dest);
            if (oldCostToDest < newCostToDest) continue;

            int newPathToDest = D.getPath(dest);
            if (newPathToDest == Integer.MAX_VALUE) continue;

            int prevRouter = dest;
            int cost = newCostToDest;
            for ( ;; ) {
                int currDest = prevRouter;
                int currCost = cost;
                logger.log("DEBUG Router.updateRIBWithSPUsingDijkstra() - currDest: " + currDest +", currCost: " + currCost);
                prevRouter = D.getPath(currDest);
                currCost = D.getCostToDest(currDest);
                if (prevRouter == routerId) {
                    routingInformationBase.setPath(dest, currDest);
                    routingInformationBase.setCost(dest, D.getCostToDest(dest));

                    logger.log("DEBUG Router.updateRIBWithSPUsingDijkstra() - update routing information R" + routerId
                            + " -> R" + dest + " -> R" + currDest + ", " + D.getCostToDest(dest));
                   break;
                }
            }
        }

        logger.log("");
        logger.log("## Update RIB");
        printDBs();
    }

    private static void listenLSandHello() throws Exception {

        while (true) {

            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            udpSocket.receive(receivePacket);            // wait until received

            if (receivePacket.getLength() == 8) {
                receiveHello();
                updateRIBWithHello();
                circuitDB.setReceivedHelloFrom(helloPacket.getLinkId());
                respondToHelloWithSetOfLSPDUs();
            } else if (receivePacket.getLength() == 20) {
                linkStateDBUpdated = false;
                receiveLSPDU();
                updateLinkStateDB();
                if (linkStateDBUpdated) {
                    updateRIBWithSPUsingDijkstra();
                    sendNeighborsTheLSPDU();
                }

            }
        }
    }

    public static void main(String[] args) throws Exception {

        checkArgs(args);
        initialize(args);

        sendInit();
        receiveCircuitDB();
        sendHello();

        listenLSandHello();
    }
}
