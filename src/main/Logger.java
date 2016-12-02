package main;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * OSPFRouter
 * Created by dwson Son (20420487)
 * on 11/26/16
 * d3son@uwaterloo.ca
 */
public class Logger {

    private PrintWriter logWriter = null;

    private static Logger instance = null;
    private Logger(String logFileName) throws IOException {
        // Exists only to defeat instantiation.
        File logFile = new File(logFileName);
        if (logFile.exists()) logFile.delete();
        logFile.createNewFile();
        logWriter = new PrintWriter(logFileName, "UTF-8");
    }

    public static Logger getInstance() {
        if(instance == null) {
            try {
                String logFileName = "router" + Router.routerId + ".log";
                instance = new Logger(logFileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return instance;
    }

    public void log(String message) {
        logWriter.println(message);
        System.out.println(message);
    }
}
