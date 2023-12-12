package cs451;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Game {
    int gameNumber;
    boolean active;
    int ackCount;
    int nackCount;
    int activeProposalNumber;
    Set<Integer> proposedValue;
    Set<Integer> acceptedValue;

    public Game(Set<Integer> proposal, int gameNumber) {
        this.gameNumber = gameNumber;
        active = true;
        ackCount = 0;
        nackCount = 0;
        activeProposalNumber = 0;
        proposedValue = proposal;
        acceptedValue = new HashSet<>();;
    }

    public Game(int gameNumber) {
        this.gameNumber = gameNumber;
        active = true;
        ackCount = 0;
        nackCount = 0;
        activeProposalNumber = 0;
        proposedValue = new HashSet<>();
        acceptedValue = new HashSet<>();;
    }

    public String getText() {
        StringBuilder concatPropose = new StringBuilder();
        for(int pr : proposedValue) {
            concatPropose.append(pr).append("@@");
        }
        return gameNumber + "@@" + activeProposalNumber + "@@" + concatPropose;
    }
}
