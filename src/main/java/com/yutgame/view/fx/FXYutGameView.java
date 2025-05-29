package main.java.com.yutgame.view.fx;

import javafx.application.Platform;
import javafx.stage.Stage;
import main.java.com.yutgame.controller.YutGameController;
import main.java.com.yutgame.view.YutGameView;
import main.java.com.yutgame.view.fx.router.ViewRouter;

public class FXYutGameView implements YutGameView {

    private final Stage primaryStage;
    private final ViewRouter router;
    private YutGameController controller;

    private int playerCount;
    private int pieceCount;
    private int boardChoice;

    public FXYutGameView(Stage stage) {
        this.primaryStage = stage;

        stage.setTitle("윷놀이");
        stage.setResizable(false);

        this.router = new ViewRouter(stage, this);
        router.showTitle(this.controller); // 앱 실행 시 Title부터 시작

        // Stage를 Scene에 정확히 맞춤
        primaryStage.sizeToScene();
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    @Override
    public void initBoardPanel(){
        router.showGameBoard();
    }

    @Override
    public void setController(YutGameController controller) {
        this.controller = controller;
        router.setController(controller);
    }

    public YutGameController getController(){
        return controller;
    }

    @Override
    public int getPlayerCount() {
        return playerCount;
    }

    @Override
    public int getPieceCount() {
        return pieceCount;
    }

    @Override
    public int getBoardChoice() {
        return boardChoice;
    }

    public void setPlayerCount(int count) {
        System.out.println("선택된 플레이어 수 : "+count);
        this.playerCount = count;
    }

    public void setPieceCount(int count) {
        System.out.println("선택된 말 개수 : "+count);
        this.pieceCount = count;
    }

    public void setBoardChoice(int boardChoice) {
        System.out.println("선택된 보드 : "+boardChoice);   // 0 : square, 1 : pentagon, 2 : hexagon
        this.boardChoice = boardChoice;
    }

    @Override
    public void repaintBoard() {
        // JavaFX는 필요시 Platform.runLater(...)로 갱신 가능
    }

    @Override
    public void setVisibleBoard(boolean visible) {
        Platform.runLater(() -> {
            if (visible) primaryStage.show();
            else primaryStage.hide();
        });
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public ViewRouter getRouter() {
        return router;
    }
}
