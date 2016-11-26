import model.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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

    private static BufferedWriter logWriter = null;

    private static CircuitDB circuitDB;
    private static CircuitDB[] linkStateDB;
    private static RoutingInformationBase routingInformationBase;    // RIB

    private static void checkArgs(String[] args) {

        if (args.length != 4) {
            System.err.println("Usage: router <router_id> <nse_host> <nse_port> <router_port>");
            System.exit(1);
        }
    }

    private static void initialize(String[] args) throws IOException {

        System.out.println("Begin Initializing");

        routerId = Integer.valueOf(args[0]);
        nseHost = args[1];
        nsePort = Integer.valueOf(args[2]);
        routerPort = Integer.valueOf(args[3]);

        String logFileName = "router" + routerId + ".log";
        File logFile = new File(logFileName);
        if (logFile.exists()) logFile.delete();
        logFile.createNewFile();
        logWriter = Files.newBufferedWriter(Paths.get(logFileName), StandardOpenOption.WRITE);

        linkStateDB = new CircuitDB[CircuitDB.NBR_ROUTER];

        routingInformationBase = new RoutingInformationBase(routerId);
    }

    private static void sendPacket(DatagramSocket udpSocket, Packet packet, String message) {

        // Create a UDP socket
        try {
            // Send packet with message
            byte[] sendData = packet.getUDPdata();
            InetAddress ipAddress = InetAddress.getByName(nseHost);
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ipAddress, nsePort);
            udpSocket.send(sendPacket);

            logWriter.write(message);

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {

        checkArgs(args);
        initialize(args);

        DatagramSocket udpSocket = new DatagramSocket(routerPort);
        byte[] receiveData = new byte[1024];

        // First
        // Each router must send an INIT packet to the NSE (Network State Emulator)
        // containing the router's id
        Packet sendInitPacket = new Packet(routerId);   // INIT packet
        sendPacket(udpSocket, sendInitPacket, "R" + routerId + " sends an INIT");

        // After NSE receives an INIT packet from each router,
        // the NSE will send to each router the circuit database associated with that router.
        // The circuit database will be sent in a circuit_DB structure
        DatagramPacket receiveCircuitDBPacket = new DatagramPacket(receiveData, receiveData.length);
        udpSocket.receive(receiveCircuitDBPacket);            // wait until received
        circuitDB = CircuitDB.parseUDPdata(receiveData);
        logWriter.write("R" + routerId + " receives a circuitDB");

        linkStateDB[routerId - 1] = circuitDB;

        // Then each router must send a HELLO packet to all its neighbors.
        for (int i=0; i<circuitDB.getNbrLink(); i+=1) {
            HelloPacket sendHelloPacket = new HelloPacket(routerId, circuitDB.getLinkCostAt(i).getLink());
            sendPacket(udpSocket, sendHelloPacket, "R" + routerId + " sends a HELLO");
        }

        // Each router will respond to each HELLO packet by a set of LS PDUs containing its circuit database.
        while (true) {

            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            udpSocket.receive(receivePacket);            // wait until received

            if (receivePacket.getLength() == 8) {

                // Receive HELLO packet
                HelloPacket helloPacket = HelloPacket.parseUDPdata(receiveData);
                logWriter.write("R" + routerId + " receives a HELLO");

                // Update RIB
                routingInformationBase.setPath(helloPacket.getRouterId(), helloPacket.getRouterId());
                routingInformationBase.setCost(helloPacket.getRouterId(), circuitDB.findCostByLink(helloPacket.getLinkId()));

                circuitDB.setReceivedHelloFrom(helloPacket.getLinkId());

                // Respond by sending a LSPDU packet
                for (int i=0; i<circuitDB.getNbrLink(); i+=1) {
                    LSPDUPacket sendLSPDUPacket =
                            new LSPDUPacket(routerId, routerId, circuitDB.getLinkCostAt(i).getLink(),
                                    circuitDB.getLinkCostAt(i).getCost(), helloPacket.getLinkId());
                    sendPacket(udpSocket, sendLSPDUPacket, "R" + routerId + " sends an LS PDU");
                }

            } else if (receivePacket.getLength() == 20) {

                // Receive LS PDU packet
                LSPDUPacket lspduPacket = LSPDUPacket.parseUDPdata(receiveData);
                logWriter.write("R" + routerId + " receives an LS PDU: sender " + lspduPacket.getSender()
                        + ", routerId " + lspduPacket.getRouterId() + ", linkId " + lspduPacket.getLinkId()
                        + ", cost " + lspduPacket.getCost() + ", via " + lspduPacket.getVia());

                // Update its link state database
                LinkCost linkCost = new LinkCost(lspduPacket.getLinkId(), lspduPacket.getCost());
                if (linkStateDB[lspduPacket.getRouterId() - 1] == null){
                    LinkCost[] linkCosts = new LinkCost[CircuitDB.NBR_ROUTER];
                    linkCosts[0] = linkCost;
                    linkStateDB[lspduPacket.getRouterId() - 1] = new CircuitDB(1, linkCosts);
                } else {
                    CircuitDB currentDB = linkStateDB[lspduPacket.getRouterId() - 1];
                    currentDB.putLinkCost(linkCost);
                }

//                // Use the Dijkstra algorithm using its link state database
//                // to determine the shortest (minimum) path cost to each destination R
//                HashSet<Integer> N = new HashSet<>();
//                N.add(routerId);
//
//                // find minRouterId not in N such that RIB.getCostToDest(minRouterId) is a minimum
//                int min = Integer.MAX_VALUE, minRouterId = 0;
//                for (int i=0; i<CircuitDB.NBR_ROUTER; i+=1) {
//                    int destCost = routingInformationBase.getCostToDest(i+1);
//                    if (!N.contains(i+1) && destCost < min) {
//                        min = destCost;
//                        minRouterId = i+1;
//                    }
//                }
//                // Add minRouterId to N
//                if (minRouterId > 0) N.add(minRouterId);
//
//
//
//                // update RIB[v] for all routerId v adjacent to minRouterId and not in N
//                for (int i=0; i<CircuitDB.NBR_ROUTER; i+=1) {
//                    int destCost = routingInformationBase.getCostToDest(i+1);
//                    if (N.contains(i+1)) continue;
//                    linkStateDB[]
//
//                    for (int j=0; j<linkStateDB[i].getNbrLink(); j+=1) {
//
//                    }
//                }

                // Send to all its neighbors the LS PDU
                for (int i=0; i<circuitDB.getNbrLink(); i+=1) {
                    int linkId = circuitDB.getLinkCostAt(i).getLink();
                    // except the one that sends the LS PDU and those from which the router did not receive a HELLO
                    if (linkId == lspduPacket.getVia() || !circuitDB.didReceiveHelloFrom(linkId)) continue;
                    lspduPacket.setSender(routerId);
                    lspduPacket.setVia(linkId);
                    String logMessage = "R" + routerId + " sends an LS PDU: sender " + lspduPacket.getSender()
                            + ", routerId " + lspduPacket.getRouterId() + ", linkId " + lspduPacket.getLinkId()
                            + ", cost " + lspduPacket.getCost() + ", via " + lspduPacket.getVia();
                    sendPacket(udpSocket, lspduPacket, logMessage);
                }
            }
        }

        // The router program should generate a log file, named as router(id).log
        // The routers must record in the log file all the messages that they receive and all messages that they send
        // They must also record their topology database every time this later changes
        // The log file should contain the corresponding RIB for each topology
        // Before each line of trace, the router must write its id
    }
}
