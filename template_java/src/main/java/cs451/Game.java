package cs451;

import java.util.Set;

public class Game {
    int gameNumber;
    boolean active;
    int ackCount;
    int nackCount;
    int activeProposalNumber;
    Set<Integer> proposedValue;

    public Game(Set<Integer> proposal, int gameNumber) {
        this.gameNumber = gameNumber;
        active = true;
        ackCount = 0;
        nackCount = 0;
        activeProposalNumber = 0;
        proposedValue = proposal;
    }
}
