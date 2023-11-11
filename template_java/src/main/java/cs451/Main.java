package cs451;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;

public class Main {
    private static ProcessManager processManager;
    private static Parser parser;

    private static void handleSignal() throws IOException {
        //immediately stop network packet processing
        System.out.println("Immediately stopping network packet processing.");

        //write/flush output file if necessary
        System.out.println("Writing output.");
        BufferedWriter writer = new BufferedWriter(new FileWriter(parser.output()));
        synchronized (processManager.getLogs()) {
            for (String log : processManager.getLogs()) {
                writer.write(log + '\n');
            }
        }
        writer.close();
    }

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
        System.out.println(parser.messageNumber() + "\n");


        System.out.println("Doing some initialization\n");
        Host curHost = Objects.requireNonNull(parser.hosts().stream().filter(x -> x.getId() == parser.myId()).findAny().orElse(null));
        processManager = new ProcessManager(curHost, parser.hosts());

        System.out.println("Broadcasting and delivering messages...\n");


        for (int i = 1; i < parser.messageNumber() + 1; i++) {
            processManager.uniformReliableBroadcast(new LightMessage(curHost.getId(), Integer.toString(i)));
        }

        // After a process finishes broadcasting,
        // it waits forever for the delivery of messages.
        while (true) {
            // Sleep for 1 hour
            Thread.sleep(60 * 60 * 1000);
        }
    }
}
