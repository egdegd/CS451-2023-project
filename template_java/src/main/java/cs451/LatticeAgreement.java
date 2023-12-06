package cs451;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class LatticeAgreement {
    Set<Game> games = Collections.synchronizedSet(new HashSet<>());
    ProcessManager processManager;
    int maxFaulty;
    public LatticeAgreement(ProcessManager pm) {
        processManager = pm;
        maxFaulty = (processManager.getHostsList().size() - 1) / 2;
    }
    public void propose(Set<Integer> proposal, int gameNumber) {
        Game game = new Game(proposal, gameNumber);
        games.add(game);
        LAMessage m = new LAMessage(game);
        processManager.bestEffortBroadCast(m);
    }
    public void receiveAck(int gameNumber, int proposalNumber) {
        Game game = games.stream().filter(it->it.gameNumber == gameNumber).findAny().orElse(null);
        if (game == null) {
            return;
        }
        if (proposalNumber == game.activeProposalNumber) {
//            TODO: maintain counting in old rounds
            game.ackCount++;
            checkGame(game);
        }
    }
    public void receiveNack(int gameNumber, int proposalNumber, Set<Integer> value) {
        Game game = games.stream().filter(it->it.gameNumber == gameNumber).findAny().orElse(null);
        if (game == null) {
            return;
        }
        if (proposalNumber == game.activeProposalNumber) {
            game.nackCount++;
            game.proposedValue.addAll(value);
            checkGame(game);
        }
    }

    public void receiveProposal(int gameNumber, int proposalNumber, Set<Integer> proposedValue, Host senderHost) {
        Game game = games.stream().filter(it->it.gameNumber == gameNumber).findAny().orElse(null);
        if (proposedValue.containsAll(game.proposedValue)) {
            game.proposedValue.addAll(proposedValue);
            sendAck(proposalNumber, senderHost);
        } else {
            game.proposedValue.addAll(proposedValue);
            sendNack(game.proposedValue, senderHost);
        }
    }

    private void sendNack(Set<Integer> proposedValue, Host senderHost) {
        String text = "";
        Message message = new Message(text, processManager.getHost(), senderHost);
        processManager.addToStubbornLink(message);
    }

    private void sendAck(int proposalNumber, Host senderHost) {
        String text = "";
        Message message = new Message(text, processManager.getHost(), senderHost);
        processManager.addToStubbornLink(message);
    }

    private void checkGame(Game game) {
//        TODO: compare the difference
//        if (game.nackNumber > 0 && game.ackNumber + game.nackNumber >= maxFaulty + 1 && game.active) {
        if (game.nackCount > 0 && game.active) {
            game.activeProposalNumber++;
            game.nackCount = 0;
            game.ackCount = 0;
            LAMessage m = new LAMessage(game);
            processManager.bestEffortBroadCast(m);
        }
        if (game.active && game.ackCount >= maxFaulty + 1)
        {
            decide(game);
        }

    }

    private void decide(Game game) {
        game.active = false;
//        TODO: do something
    }
}
