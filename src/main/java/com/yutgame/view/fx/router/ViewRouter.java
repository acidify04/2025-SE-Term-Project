package main.java.com.yutgame.view.fx.router;

import javafx.scene.Scene;
import javafx.stage.Stage;
import main.java.com.yutgame.controller.YutGameController;
import main.java.com.yutgame.view.fx.*;

public class ViewRouter {

    private final Stage primary;
    private YutGameController controller;
    private final FXYutGameView fxView;
    private GameBoardView gameBoardView;

    public ViewRouter(Stage stage, FXYutGameView fxView) {
        this.primary = stage;
        this.fxView = fxView;
    }

    public FXYutGameView getFXView(){
        return fxView;
    }

    public void setController(YutGameController controller){
        this.controller = controller;
    }

    public void showTitle(YutGameController controller) {
        setScene(new TitleView(controller, this).scene());
    }

    public void showBoardSelect(YutGameController controller) {
        setScene(new BoardSelectView(controller, this, fxView::setBoardChoice).scene());
    }

    public void showPlayerPieceSelect(YutGameController controller) {
        setScene(new PlayerPieceSelectView(
                controller,
                this,
                fxView::setPlayerCount,
                fxView::setPieceCount
        ).scene());
    }

    public void showGameBoard() {
        if(controller == null) controller = fxView.getController();  // 안전장치
        // 보드 타입, 인원 수, 말 개수에 따라 실제 게임 뷰 구성
        GameBoardView boardView = new GameBoardView(
                controller,
                fxView.getBoardChoice(),    // 0 : 사각형, 1 : 오각형, 2 : 육각형
                fxView.getPlayerCount(),
                fxView.getPieceCount()
        );
        this.gameBoardView = boardView;
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

    public GameBoardView getGameBoardView(){
        return gameBoardView;
    }
}
