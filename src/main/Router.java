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

    private static int routerId, nsePort, routerPort;
    private static String nseHost;

    private static String logMessage;
    private static MyLogger logger;

    private static DatagramSocket udpSocket;
    private static byte[] receiveData = new byte[1024];

    private static CircuitDB circuitDB;
    private static LinkStateDB linkStateDB;
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

        String logFileName = "router" + routerId + ".log";
        logger = MyLogger.getInstance(logFileName);

        linkStateDB = new LinkStateDB(routerId);

        routingInformationBase = new RoutingInformationBase(routerId);
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
        logger.log(routingInformationBase.toString());
        logger.log(linkStateDB.toString());
    }

    private static void sendInit() throws IOException {
        // First
        // Each router must send an INIT packet to the NSE (Network State Emulator)
        // containing the router's id
        Packet sendInitPacket = new Packet(routerId);   // INIT packet
        logMessage = "R" + routerId + " sends an INIT";
        sendPacket(udpSocket, sendInitPacket, logMessage);
    }

    private static void receiveCircuitDB() throws Exception {
        // After NSE receives an INIT packet from each router,
        // the NSE will send to each router the circuit database associated with that router.
        // The circuit database will be sent in a circuit_DB structure
        DatagramPacket receiveCircuitDBPacket = new DatagramPacket(receiveData, receiveData.length);
        udpSocket.receive(receiveCircuitDBPacket);            // wait until received
        circuitDB = CircuitDB.parseUDPdata(receiveData);

        logMessage = "R" + routerId + " receives a circuitDB";
        logger.log(logMessage);

        linkStateDB.putCircuitDB(routerId, circuitDB);
        printDBs();
    }

    private static void sendHello() throws IOException {
        // Then each router must send a HELLO packet to all its neighbors.
        for (int i=0; i<circuitDB.getNbrLink(); i+=1) {
            int linkId = circuitDB.getLinkCostAt(i).getLink();
            HelloPacket sendHelloPacket = new HelloPacket(routerId, linkId);
            logMessage = "R" + routerId + " sends a HELLO: linkId " + linkId;
            sendPacket(udpSocket, sendHelloPacket, logMessage);
        }
    }

    private static void receiveHello() throws Exception {
        // Receive HELLO packet
        helloPacket = HelloPacket.parseUDPdata(receiveData);
        logMessage = "R" + routerId + " receives a HELLO from R" + helloPacket.getRouterId() + " via L" + helloPacket.getLinkId();
        logger.log(logMessage);
    }

    private static void updateRIBWithHello() {
        // Update RIB
        routingInformationBase.setPath(helloPacket.getRouterId(), helloPacket.getRouterId());
        routingInformationBase.setCost(helloPacket.getRouterId(), circuitDB.findCostByLink(helloPacket.getLinkId()));
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
            logMessage = "R" + routerId + " sends an LS PDU: sender " + routerId + ", routerId " + routerId
                    + ", linkId " + linkId + ", cost " + cost + ", via " + via;
            sendPacket(udpSocket, sendLSPDUPacket, logMessage);
        }
    }

    private static void receiveLSPDU() throws Exception {
        // Receive LS PDU packet
        lspduPacket = LSPDUPacket.parseUDPdata(receiveData);
        logMessage = "R" + routerId + " receives an LS PDU: sender " + lspduPacket.getSender()
                + ", routerId " + lspduPacket.getRouterId() + ", linkId " + lspduPacket.getLinkId()
                + ", cost " + lspduPacket.getCost() + ", via " + lspduPacket.getVia();
        logger.log(logMessage);
    }

    private static void updateLinkStateDB() {
        // Update its link state database
        LinkCost linkCost = new LinkCost(lspduPacket.getLinkId(), lspduPacket.getCost());
        linkStateDB.putLinkState(lspduPacket.getRouterId(), linkCost);
        printDBs();
    }

    private static void sendNeighborsTheLSPDU() throws IOException {
        // Send to all its neighbors the LS PDU
        for (int i=0; i<circuitDB.getNbrLink(); i+=1) {
            int linkId = circuitDB.getLinkCostAt(i).getLink();
            // except the one that sends the LS PDU and those from which the router did not receive a HELLO
            if (linkId == lspduPacket.getVia() || !circuitDB.didReceiveHelloFrom(linkId)) continue;
            lspduPacket.setSender(routerId);
            lspduPacket.setVia(linkId);
            logMessage = "R" + routerId + " sends an LS PDU: sender " + lspduPacket.getSender()
                    + ", routerId " + lspduPacket.getRouterId() + ", linkId " + lspduPacket.getLinkId()
                    + ", cost " + lspduPacket.getCost() + ", via " + lspduPacket.getVia();
            sendPacket(udpSocket, lspduPacket, logMessage);
        }
    }

    private static void updateRIBWithSPUsingDijkstra() {
        // Use the Dijkstra algorithm using its link state database
        // to determine the shortest (minimum) path cost to each destination R

        HashSet<Integer> N = new HashSet<>();
        N.add(routerId);

        while (N.size() != CircuitDB.NBR_ROUTER) {
            // find minRouterId not in N such that RIB.getCostToDest(minRouterId) is a minimum
            int min = Integer.MAX_VALUE, minRouterId = 0;
            for (int i=0; i<CircuitDB.NBR_ROUTER; i+=1) {
                int destCost = routingInformationBase.getCostToDest(i+1);
                logger.log("costToDest: " + destCost);
                if (N.contains(i+1)) continue;

                if (destCost <= min) {
                    min = destCost;
                    minRouterId = i+1;
                }
            }
            logger.log("min found: " + minRouterId + ": " + min);
            // Add minRouterId to N
            if (minRouterId > 0) N.add(minRouterId);

            // update routingInformationBase of neighbors of minRouter, which are not in N
            for (int i=0; i<CircuitDB.NBR_ROUTER; i+=1) {
                if (N.contains(i+1)) continue;

                int destCost = routingInformationBase.getCostToDest(i+1);
                if (destCost == Integer.MAX_VALUE) continue;

                LinkCost link = linkStateDB.findLinkCostBetween(i+1, minRouterId);
                if (link != null) {
                    int newCost = routingInformationBase.getCostToDest(minRouterId) + link.getCost();
                    if (newCost < destCost) {
                        logger.log("newCost < destCost: update!");
                        routingInformationBase.setCost(i+1, newCost);
//                                routingInformationBase.setPath(i+1, linkStateDB.findSenderOf());
                    }
                }
            }
        }
    }

    private static void listen() throws Exception {

        while (true) {

            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            udpSocket.receive(receivePacket);            // wait until received

            if (receivePacket.getLength() == 8) {
                receiveHello();
                updateRIBWithHello();
                circuitDB.setReceivedHelloFrom(helloPacket.getLinkId());
                respondToHelloWithSetOfLSPDUs();
            } else if (receivePacket.getLength() == 20) {
                receiveLSPDU();
                updateLinkStateDB();
                sendNeighborsTheLSPDU();
                updateRIBWithSPUsingDijkstra();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        checkArgs(args);
        initialize(args);

        sendInit();
        receiveCircuitDB();
        sendHello();

        listen();
    }
}