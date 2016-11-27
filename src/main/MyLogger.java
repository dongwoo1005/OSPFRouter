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
public class MyLogger {

    private PrintWriter logWriter = null;

    private static MyLogger instance = null;
    private MyLogger(String logFileName) throws IOException {
        // Exists only to defeat instantiation.
        File logFile = new File(logFileName);
        if (logFile.exists()) logFile.delete();
        logFile.createNewFile();
        logWriter = new PrintWriter(logFileName, "UTF-8");
    }

    public static MyLogger getInstance(String logFileName) throws IOException {
        if(instance == null) {
            instance = new MyLogger(logFileName);
        }
        return instance;
    }

    public static MyLogger getInstance() {
        return instance;
    }

    public void log(String message) {
        logWriter.println(message);
        System.out.println(message);
    }
}
