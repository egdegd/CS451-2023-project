package cs451;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class LatticeAgreement {
    Set<Game> games = Collections.synchronizedSet(new HashSet<>());
    ProcessManager processManager;
    int majority;
    public LatticeAgreement(ProcessManager pm) {
        processManager = pm;
        if (processManager.getHostsList().size() % 2 == 0) {
            majority = processManager.getHostsList().size() / 2;
        } else {
            majority = (processManager.getHostsList().size() - 1) / 2;
        }

    }
    public boolean propose(Set<Integer> proposal, int gameNumber) {
        Game game;
        synchronized (games) {
            if (games.stream().filter(it-> (it.active) && (!it.proposedValue.isEmpty())).count() > 10) {
               return false;
            }
            game = games.stream().filter(it->it.gameNumber == gameNumber).findAny().orElse(null);
            if (game == null) {
                game = new Game(proposal, gameNumber);
                games.add(game);
            } else {
                game.proposedValue.addAll(proposal);
            }
        }
        LAMessage m = new LAMessage(game, processManager.getHost().getId());
//        System.out.println("Propose " + game.proposedValue + ' ' + game.gameNumber + ' ' + game.acceptedValue + ' ' + game.activeProposalNumber);
        processManager.bestEffortBroadCast(m);
        return true;
    }
    public void receiveAck(int gameNumber, int proposalNumber) {
//        TODO: replace list by hashset by number of the game?
        Game game;
        synchronized (games) {
            game = games.stream().filter(it->it.gameNumber == gameNumber).findAny().orElse(null);
        }
        if (game == null) {
            return;
        }
//        System.out.println("ReceiveAck " + game.proposedValue + ' ' + game.gameNumber + ' ' + game.acceptedValue + ' ' + game.activeProposalNumber);
        if (proposalNumber == game.activeProposalNumber) {
//            TODO: maintain counting in old rounds
            game.ackCount++;
            checkGame(game);
        }
    }
    public void receiveNack(int gameNumber, int proposalNumber, Set<Integer> value) {
        Game game;
        synchronized (games) {
            game = games.stream().filter(it->it.gameNumber == gameNumber).findAny().orElse(null);
        }
        if (game == null) {
            return;
        }
//        System.out.println("ReceiveNack " + game.proposedValue + ' ' + game.gameNumber + ' ' + game.acceptedValue + ' ' + game.activeProposalNumber);
//        System.out.println(proposalNumber);
        if (proposalNumber == game.activeProposalNumber) {
            game.nackCount++;
            game.proposedValue.addAll(value);
            checkGame(game);
        }

    }

    public void receiveProposal(int gameNumber, int proposalNumber, Set<Integer> proposedValue, Host senderHost) {
        Game game;
        synchronized (games) {
            game = games.stream().filter(it->it.gameNumber == gameNumber).findAny().orElse(null);
            if (game == null) {
                game = new Game(gameNumber);
                games.add(game);
            }
        }
        if (proposedValue.containsAll(game.acceptedValue)) {
//            System.out.println("SendAck " + gameNumber + ' ' + game.acceptedValue + ' ' + proposalNumber + ' ' + senderHost.getId());
            game.acceptedValue.addAll(proposedValue);
            sendAck(gameNumber, proposalNumber, senderHost);
        } else {
//            System.out.println("SendNack " + gameNumber + ' ' + proposalNumber + ' ' + game.acceptedValue + ' ' + proposedValue + ' ' + senderHost.getId());
            game.acceptedValue.addAll(proposedValue);
            Set<Integer> diff = new HashSet<>(game.acceptedValue);
            diff.removeAll(proposedValue);
            sendNack(gameNumber, proposalNumber, diff, senderHost);
        }
    }

    public void sendNack(int gameNumber, int proposalNumber, Set<Integer> diff, Host senderHost) {
//        TODO: send only diff
        String text = "LANack@@" + gameNumber + "@@" + proposalNumber + "@@" + textByProposed(diff);
        Message message = new Message(text, processManager.getHost(), senderHost);
        processManager.addToStubbornLink(message);
    }

    private String textByProposed(Set<Integer> proposedValue) {
        StringBuilder concatPropose = new StringBuilder();
        for(int pr : proposedValue) {
            concatPropose.append(pr).append("@@");
        }
        return concatPropose.toString();
    }

    public void sendAck(int gameNumber, int proposalNumber, Host senderHost) {
        String text = "LAAck@@" + gameNumber + "@@" + proposalNumber + "@@";
        Message message = new Message(text, processManager.getHost(), senderHost);
        processManager.addToStubbornLink(message);
    }

    public void checkGame(Game game) {
//        TODO: compare the difference
//        if (game.nackCount > 0 && game.ackCount + game.nackCount >= maxFaulty + 1 && game.active) {
        if (game.nackCount > 0 && game.active) {
            game.activeProposalNumber++;
            game.nackCount = 0;
            game.ackCount = 0;
            LAMessage m = new LAMessage(game, processManager.getHost().getId());
            processManager.bestEffortBroadCast(m);
//            System.out.println("NewPropose " + game.proposedValue + ' ' + game.gameNumber + ' ' + game.acceptedValue + ' ' + game.activeProposalNumber);
        }
        if (game.active && game.ackCount >= majority + 1)
        {
//            System.out.println(maxFaulty + " " +  game.ackCount);
            decide(game);
        }

    }

    public void decide(Game game) {
        game.active = false;
        processManager.decide(game);
//        System.out.println("DECIDE. GAME " + game.gameNumber + " VALUE " + game.proposedValue);
    }
}
