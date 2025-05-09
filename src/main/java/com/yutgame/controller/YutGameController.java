package main.java.com.yutgame.controller;

import main.java.com.yutgame.model.*;
import main.java.com.yutgame.view.swing.SwingYutGameView;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

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
}
