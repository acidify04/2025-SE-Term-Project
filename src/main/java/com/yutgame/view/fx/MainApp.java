package main.java.com.yutgame.view.fx;

import main.java.com.yutgame.view.fx.router.ViewRouter;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        stage.setTitle("윷놀이");
        stage.setResizable(false);

        ViewRouter router = new ViewRouter(stage);
        router.showTitle();

        // Stage를 Scene에 정확히 맞춤
        stage.sizeToScene();

        stage.centerOnScreen();
        stage.show();
    }
}
