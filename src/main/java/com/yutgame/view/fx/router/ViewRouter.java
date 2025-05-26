package main.java.com.yutgame.view.fx.router;

import javafx.scene.Scene;
import javafx.stage.Stage;
import main.java.com.yutgame.view.fx.TitleView;
import main.java.com.yutgame.view.fx.BoardSelectView;          // ★ 추가

public class ViewRouter {

    private final Stage primary;
    public ViewRouter(Stage stage) { this.primary = stage; }

    public void showTitle()       { setScene(new TitleView(this).scene()); }
    public void showBoardSelect() { setScene(new BoardSelectView(this).scene()); }   // ★ 연결
    public void exit()            { primary.close(); }

    private void setScene(Scene s) {
        primary.setScene(s);
        primary.sizeToScene();
        primary.centerOnScreen();
    }
}
