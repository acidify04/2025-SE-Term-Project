package main.java.com.yutgame;



import main.java.com.yutgame.controller.YutGameController;
import main.java.com.yutgame.view.swing.SwingYutGameView;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            YutGameController controller = new YutGameController();
            controller.initializeGame();  // 내부에서 createGame 호출
        });
    }
}