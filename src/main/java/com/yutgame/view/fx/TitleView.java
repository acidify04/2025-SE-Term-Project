package main.java.com.yutgame.view.fx;

import main.java.com.yutgame.view.fx.router.ViewRouter;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.geometry.Insets;
import javafx.scene.Cursor;

public class TitleView {

    private final Scene scene;
    public Scene scene() { return scene; }

    public TitleView(ViewRouter router) {

        /* 배경 */
        ImageView bg = new ImageView(new Image("/fx/bg_start.png"));
        bg.setFitWidth(1049);
        bg.setFitHeight(686);
        bg.setPreserveRatio(false);

        /* 버튼들을 ImageView로 직접 만들기 */
        ImageView startBtn = clickableImage("/fx/btn_start.png",
                e -> router.showBoardSelect());
        ImageView exitBtn = clickableImage("/fx/btn_exit.png",
                e -> router.exit());

        // 시작 버튼 위치 설정
        startBtn.setTranslateX(220);
        startBtn.setTranslateY(50);

        // 종료 버튼 위치 설정
        exitBtn.setTranslateX(220);
        exitBtn.setTranslateY(120);  // 간격 조정

        StackPane root = new StackPane(bg, startBtn, exitBtn);

        root.setPadding(Insets.EMPTY);
        root.setStyle("-fx-background-color: null; " +
                "-fx-padding: 0; " +
                "-fx-background-insets: 0; " +
                "-fx-border-insets: 0;");

        this.scene = new Scene(root, 870, 570);
        this.scene.setFill(null);
    }

    private ImageView clickableImage(String path, EventHandler<ActionEvent> act) {
        ImageView iv = new ImageView(new Image(path));
        iv.setFitWidth(280);  // 버튼 크기 더 크게
        iv.setPreserveRatio(true);
        iv.setSmooth(true);  // 부드러운 확대

        // 마우스 이벤트 설정
        iv.setOnMouseClicked(e -> act.handle(new ActionEvent()));

        // 마우스 커서 변경
        iv.setCursor(Cursor.HAND);

        // 마우스 호버 효과 (선택사항)
        iv.setOnMouseEntered(e -> {
            iv.setOpacity(0.8);  // 살짝 투명하게
            iv.setScaleX(1.05);  // 살짝 확대
            iv.setScaleY(1.05);
        });

        iv.setOnMouseExited(e -> {
            iv.setOpacity(1.0);  // 원래대로
            iv.setScaleX(1.0);   // 원래 크기
            iv.setScaleY(1.0);
        });

        return iv;
    }
}
