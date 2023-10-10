package cs451;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class HostsParser {

    private static final String HOSTS_KEY = "--hosts";
    private static final String SPACES_REGEX = "\\s+";

    private String filename;
    private List<Host> Hosts = new ArrayList<>();

    public boolean populate(String key, String filename) {
        if (!key.equals(HOSTS_KEY)) {
            return false;
        }

        this.filename = filename;
        try(BufferedReader br = new BufferedReader(new FileReader(filename))) {
            int lineNum = 1;
            for(String line; (line = br.readLine()) != null; lineNum++) {
                if (line.isBlank()) {
                    continue;
                }

                String[] splits = line.split(SPACES_REGEX);
                if (splits.length != 3) {
                    System.err.println("Problem with the line " + lineNum + " in the hosts file!");
                    return false;
                }

                Host newHost = new Host();
                if (!newHost.populate(splits[0], splits[1], splits[2])) {
                    return false;
                }

                Hosts.add(newHost);
            }
        } catch (IOException e) {
            System.err.println("Problem with the hosts file!");
            return false;
        }

        if (!checkIdRange()) {
            System.err.println("Hosts ids are not within the range!");
            return false;
        }

        // sort by id
        Collections.sort(Hosts, new HostsComparator());
        return true;
    }

    private boolean checkIdRange() {
        int num = Hosts.size();
        for (Host host : Hosts) {
            if (host.getId() < 1 || host.getId() > num) {
                System.err.println("Id of a host is not in the right range!");
                return false;
            }
        }

        return true;
    }

    public boolean inRange(int id) {
        return id <= Hosts.size();
    }

    public List<Host> getHosts() {
        return Hosts;
    }

    class HostsComparator implements Comparator<Host> {

        public int compare(Host a, Host b) {
            return a.getId() - b.getId();
        }

    }

}
