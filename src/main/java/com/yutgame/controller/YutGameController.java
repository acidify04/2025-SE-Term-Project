package main.java.com.yutgame.controller;

import main.java.com.yutgame.model.*;
import main.java.com.yutgame.view.swing.SwingYutGameView;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static main.java.com.yutgame.model.YutThrowResult.*;

public class YutGameController {
    private YutGame game;
    private SwingYutGameView view;

    public YutGameController() {
        this.game = new YutGame();
        this.view = new SwingYutGameView();
        view.setController(this);
    }

    public YutGame getGame() {
        return game;
    }

    public SwingYutGameView getView() {
        return view;
    }

    public void setGame(YutGame game) {
        this.game = game;
    }

    // 윷·모가 나올 때까지 계속 던지고, 최종 결과 리스트를 반환
    public List<YutThrowResult> collectThrowResults(
            YutThrowResult firstResult,
            boolean isRandom,
            Supplier<YutThrowResult> manualThrowProvider,
            Consumer<YutThrowResult> resultDisplayer,
            Runnable promptExtraThrow
    ) {
        List<YutThrowResult> results = new ArrayList<>();
        resultDisplayer.accept(firstResult);
        results.add(firstResult);

        while (game.getLastThrowResult() == YutThrowResult.YUT
                || game.getLastThrowResult() == YutThrowResult.MO) {
            promptExtraThrow.run();
            YutThrowResult next = isRandom ? game.throwYutRandom() : manualThrowProvider.get();
            resultDisplayer.accept(next);
            results.add(next);
        }

        return results;
    }

    // Game 관련 getter
    public Player getCurrentPlayer() {
        return game.getCurrentPlayer();
    }

    public YutBoard getBoard() {
        return game.getBoard();
    }

    public List<Player> getPlayers() {
        return game.getPlayers();
    }

    public boolean isGameOver() {
        return game.isGameOver();
    }

    public Player getWinner() {
        return game.getWinner();
    }

    // Game 관련 setter
    public void throwYutManual(YutThrowResult manualResult) {
        game.setLastThrowResult(manualResult);
    }

    public void startGame() {
        game.startGame();
    }

    public YutThrowResult getRandomYut() {
        return game.throwYutRandom();
    }

    public YutThrowResult getSetYut(int choice) {
        YutThrowResult sel = switch (choice) { // TODO: model로 이동
            case 0 -> YutThrowResult.BAK_DO;
            case 1 -> YutThrowResult.DO;
            case 2 -> GAE;
            case 3 -> GEOL;
            case 4 -> YUT;
            case 5 -> YutThrowResult.MO;
            default -> YutThrowResult.DO;
        };
        throwYutManual(sel);
        return sel;
    }

    public void nextTurn() {
        game.nextTurn();
        view.repaint();
    }

    public void movePiece(Piece piece, BoardNode targetNode, boolean containsStart) {
        game.movePiece(piece, targetNode, containsStart);
    }

    public void checkWin() {
        game.checkWinCondition();
    }
}
