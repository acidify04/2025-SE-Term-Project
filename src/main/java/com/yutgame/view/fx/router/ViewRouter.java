package main.java.com.yutgame.view.fx.router;

import javafx.scene.Scene;
import javafx.stage.Stage;
import main.java.com.yutgame.controller.YutGameController;
import main.java.com.yutgame.view.fx.*;

public class ViewRouter {

    private final Stage primary;
    private final YutGameController controller;
    private final FXYutGameView fxView;

    public ViewRouter(Stage stage, FXYutGameView fxView, YutGameController controller) {
        this.primary = stage;
        this.fxView = fxView;
        this.controller = controller;
    }

    public void showTitle() {
        setScene(new TitleView(this).scene());
    }

    public void showBoardSelect() {
        setScene(new BoardSelectView(this, fxView::setBoardChoice).scene());
    }

    public void showPlayerPieceSelect() {
        setScene(new PlayerPieceSelectView(
                this,
                fxView::setPlayerCount,
                fxView::setPieceCount
        ).scene());
    }

    public void showGameBoard() {
        // 보드 타입, 인원 수, 말 개수에 따라 실제 게임 뷰 구성
        int board = fxView.getBoardChoice();     // 0 : square, 1 : pentagon, 2 : hexagon
        int players = fxView.getPlayerCount();
        int pieces = fxView.getPieceCount();

        GameBoardView boardView = new GameBoardView(controller, board, players, pieces);
        setScene(boardView.scene());
    }

    public void exit() {
        primary.close();
    }

    private void setScene(Scene scene) {
        primary.setScene(scene);
        primary.sizeToScene();
        primary.centerOnScreen();
    }
}
