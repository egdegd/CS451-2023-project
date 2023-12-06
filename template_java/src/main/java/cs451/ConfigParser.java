package cs451;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ConfigParser {

    private static final String SPACES_REGEX = "\\s+";
    private String path;
    private int proposalNumber;
    private int maxNumberOfElementsInProposal;
    private int maxNumberOfDistinctElements;
    ArrayList<Set<Integer>> proposals = new ArrayList<>();

    public boolean populate(String value) {
        File file = new File(value);
        path = file.getPath();
        try(BufferedReader br = new BufferedReader(new FileReader(file))) {
            int lineNum = 1;
            for(String line; (line = br.readLine()) != null; lineNum++) {
                if (line.isBlank()) {
                    continue;
                }
                String[] splits = line.split(SPACES_REGEX);
                if (lineNum == 1) {
                    if (splits.length != 3) {
                        System.err.println("The first line of config file must consist of 3 integers!");
                        return false;
                    }
                    proposalNumber = Integer.parseInt(splits[0]);
                    maxNumberOfElementsInProposal = Integer.parseInt(splits[1]);
                    maxNumberOfDistinctElements = Integer.parseInt(splits[2]);

                } else {
                    Set<Integer> proposal = new HashSet<>();
                    for (String pr: splits ) {
                        proposal.add(Integer.parseInt(pr));
                    }
                    proposals.add(proposal);
                }
            }



        } catch (IOException e) {
            System.err.println("Problem with the config file!");
            return false;
        }
        return true;
    }

    public String getPath() {
        return path;
    }
    public ArrayList<Set<Integer>> getProposals() {
        return proposals;
    }
//    public int getReceiverId() {
//        return receiverId;
//    }

}
