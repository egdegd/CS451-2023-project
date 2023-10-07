package cs451;

import java.io.IOException;
import java.util.Objects;

public class Main {

    private static void handleSignal() {
        //immediately stop network packet processing
        System.out.println("Immediately stopping network packet processing.");

        //write/flush output file if necessary
        System.out.println("Writing output.");
    }

    private static void initSignalHandlers() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                handleSignal();
            }
        });
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        Parser parser = new Parser(args);
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
        System.out.println(parser.messageNumber() + " " + parser.receiverId() + "\n");


        System.out.println("Doing some initialization\n");

        System.out.println("Broadcasting and delivering messages...\n");
        int receiverPort = Objects.requireNonNull(parser.hosts().stream().filter(x -> x.getId() == parser.receiverId()).findAny().orElse(null)).getPort();
        if (parser.myId() == parser.receiverId()) {
            System.out.println("receiver\n");
            UDPReceiver server = new UDPReceiver(receiverPort);
            server.listen();
        } else {
            System.out.println("sender\n");
            UDPSender client = new UDPSender();
            for (int i = 1; i <= parser.messageNumber(); i++) {
                client.send(Integer.toString(i), receiverPort);
                client.clientSocket.setSoTimeout(5000);
            }
        }

        // After a process finishes broadcasting,
        // it waits forever for the delivery of messages.
        while (true) {
            // Sleep for 1 hour
            Thread.sleep(60 * 60 * 1000);
        }
    }
}
