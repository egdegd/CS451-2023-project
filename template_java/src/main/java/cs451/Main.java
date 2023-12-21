package cs451;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static java.lang.Thread.sleep;

public class Main {
    private static ProcessManager processManager;
    private static Parser parser;
    static FileWriter writer;

    private static void handleSignal() throws IOException {
        //immediately stop network packet processing
        System.out.println("Immediately stopping network packet processing.");
        timer.cancel();

        //write/flush output file if necessary
        System.out.println("Writing output.");
        synchronized (processManager.logs) {
            for (String line : processManager.logs) {
                if (line == null) break;
                writer.write(line + "\n");
            }
        }
        writer.close();
    }
    static TimerTask writeLogs = new TimerTask() {
        public void run() {
            synchronized (processManager.logs) {
                int lastLogWritten = -1;
                for (int i = 0; i < processManager.logs.size(); i++) {
                    String log = processManager.logs.get(i);
                    if (log == null) break;
                    try {
                        writer.write(log + "\n");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    lastLogWritten = i;
                }
                processManager.logs.subList(0, lastLogWritten + 1).clear();
            }
        }
    };
    static Timer timer = new Timer("Timer");

    private static void initSignalHandlers() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    handleSignal();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public static void main(String[] args) throws InterruptedException, IOException, ClassNotFoundException {
        parser = new Parser(args);
        parser.parse();
        try {
            writer = new FileWriter(parser.output());
        } catch (IOException ignored){}

        initSignalHandlers();

        // example
        long pid = ProcessHandle.current().pid();
        System.out.println("My PID: " + pid + "\n");
        System.out.println("From a new terminal type `kill -SIGINT " + pid + "` or `kill -SIGTERM " + pid + "` to stop processing packets\n");

        System.out.println("My ID: " + parser.myId() + "\n");
        System.out.println("List of resolved hosts is:");
        System.out.println("==========================");
        for (Host host: parser.hosts()) {
            System.out.println(host.getId());
            System.out.println("Human-readable IP: " + host.getIp());
            System.out.println("Human-readable Port: " + host.getPort());
            System.out.println();
        }
        System.out.println();

        System.out.println("Path to output:");
        System.out.println("===============");
        System.out.println(parser.output() + "\n");

        System.out.println("Path to config:");
        System.out.println("===============");
        System.out.println(parser.config() + "\n");


        System.out.println("Doing some initialization\n");
        Host curHost = Objects.requireNonNull(parser.hosts().stream().filter(x -> x.getId() == parser.myId()).findAny().orElse(null));
        processManager = new ProcessManager(curHost, parser.hosts());

        System.out.println("Broadcasting and delivering messages...\n");
        timer.scheduleAtFixedRate(writeLogs, 500, 2000);
        for (int i = 0; i <  parser.getProposals().size(); i++) {
            while(!processManager.la.propose(parser.getProposals().get(i), i)) {
                sleep(10);
            }
        }

        // After a process finishes broadcasting,
        // it waits forever for the delivery of messages.
        while (true) {
            // Sleep for 1 hour
            sleep(60 * 60 * 1000);
        }
    }
}
