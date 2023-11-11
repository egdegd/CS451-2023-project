package cs451;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class ConfigParser {

    private static final String SPACES_REGEX = "\\s+";
    private String path;
    private int messageNumber;
//    private int receiverId;

    public boolean populate(String value) {
        File file = new File(value);
        path = file.getPath();
        try(BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine();
            String[] splits = line.split(SPACES_REGEX);
            if (splits.length != 1) {
                System.err.println("The config file must consist of 1 integers!");
                return false;
            }
            try {
                messageNumber = Integer.parseInt(splits[0]);
//                receiverId = Integer.parseInt(splits[1]);
            } catch (NumberFormatException e) {
                System.err.println("The config file must consist of 2 integers!");
                return false;
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
    public int getMessageNumber() {
        return messageNumber;
    }
//    public int getReceiverId() {
//        return receiverId;
//    }

}
