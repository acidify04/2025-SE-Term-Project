package main.java.com.yutgame.view;

import main.java.com.yutgame.controller.YutGameController;

public interface YutGameView {
    void setController(YutGameController controller);

    int getPlayerCount();
    int getPieceCount();
    int getBoardChoice();

    void repaintBoard();

    void setVisible(boolean visible);
}
