package main.java.com.yutgame.view.fx;

import main.java.com.yutgame.controller.YutGameController;
import main.java.com.yutgame.view.fx.router.ViewRouter;
import javafx.application.Application;
import javafx.stage.Stage;

public class FXAppLauncher extends Application {

    private static YutGameController controller;


    public static void launchApp(YutGameController c) {
        controller = c;
        launch(); // JavaFX 애플리케이션 실행
    }

    @Override
    public void start(Stage stage) {
        FXYutGameView view = new FXYutGameView(stage);
        view.setController(controller);
        controller.setView(view);
    }
}