package main.java.com.yutgame.view.fx;

import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import main.java.com.yutgame.controller.YutGameController;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class GameBoardView {
    private YutGameController controller;

    private final Scene scene;

    public Scene scene() {
        return scene;
    }

    private enum ThrowType {RANDOM, MANUAL};
    private ThrowType selectedThrow = ThrowType.RANDOM;

    private final List<StackPane> allPlayers = new ArrayList<>();
    private List<StackPane> allPieces = new ArrayList<>();
    private List<VBox> playerPieces = new ArrayList<>();
    private List<ImageView> buttonImg = new ArrayList<>();
    private List<StackPane> buttonPane = new ArrayList<>();

    public GameBoardView(YutGameController controller, int boardType, int playerCount, int pieceCount) {
        // 캐릭터, 말
        for (int i = 0; i < playerCount; i++) {
            int playerNum = i + 1;
            ImageView img = safeLoadImage("/fx/player/player_" + playerNum + ".png");
            img.setFitWidth(100);
            img.setFitHeight(100);
            img.setPreserveRatio(false);
            img.setSmooth(true);
            StackPane player = new StackPane(img);
            allPlayers.add(player);

            StackPane pieces = new StackPane();
            pieces.setPrefSize(143, 63);
            for (int j = 0; j < pieceCount; j++) {
                ImageView pieceImg = safeLoadImage("/fx/piece/piece_" + playerNum + ".png");
                pieceImg.setFitWidth(28);
                pieceImg.setFitHeight(38);
                pieceImg.setPreserveRatio(false);
                pieceImg.setSmooth(true);

                // 중앙 정렬을 유지하면서 좌우로 퍼지게
                double offset = (j - (pieceCount - 1) / 2.0) * 30;
                pieceImg.setTranslateX(offset);

                pieces.getChildren().add(pieceImg);
            }
            allPieces.add(pieces);
        }

        // 사용자와 말 수직박스로 묶기
        for (int i = 0; i < playerCount; i++) {
            if (i<=1){
                VBox connect = new VBox(allPlayers.get(i), allPieces.get(i));
                connect.setSpacing(10);
                playerPieces.add(connect);
            }else if (i>1){
                VBox connect = new VBox(allPieces.get(i), allPlayers.get(i));
                connect.setSpacing(10);
                playerPieces.add(connect);
            }
            else{
                System.out.println("플레이어 수 선택 오류");
            }
        }

        // 사용자 캐릭터 이미지
        HBox upPlayers = new HBox(500, playerPieces.get(0), playerPieces.get(1));
        upPlayers.setAlignment(Pos.CENTER);
        upPlayers.setTranslateY(-50);

        HBox underPlayers = new HBox();
        if (playerCount == 3){
            underPlayers.getChildren().add(playerPieces.get(2));
            underPlayers.setTranslateX(35);
            underPlayers.setTranslateY(50);
        }else if (playerCount == 4){
            underPlayers.getChildren().addAll(playerPieces.get(2), playerPieces.get(3));
            underPlayers.setSpacing(500);
            upPlayers.setAlignment(Pos.CENTER);
            underPlayers.setTranslateX(45);
            underPlayers.setTranslateY(50);
        }
        VBox Players = new VBox(100, upPlayers, underPlayers);
        Players.setAlignment(Pos.CENTER);

        // 윷 던지기 버튼
        StackPane randomThrow = throwButton("/fx/button/game/randomBtn.png", ThrowType.RANDOM);
        StackPane manualThrow = throwButton("/fx/button/game/selectBtn.png", ThrowType.MANUAL);

        HBox Buttons = new HBox(randomThrow, manualThrow);
        Buttons.setTranslateX(470);
        Buttons.setTranslateY(30);

        // 게임판 이미지
        ImageView board = switch (boardType) {
            case 0 -> safeLoadImage("/fx/board/game/square_board_empty.png");
            case 1 -> safeLoadImage("/fx/board/game/pentagon_board_empty.png");
            case 2 -> safeLoadImage("/fx/board/game/hexagon_board_empty.png");
            default -> safeLoadImage("/fx/board/game/square_board_empty.png");
        };

        switch (boardType) {
            case 0 -> { // 사각형
                board.setFitWidth(450);
                board.setFitHeight(450);
            }
            case 1 -> { // 오각형
                board.setFitWidth(500);
                board.setFitHeight(500);
            }
            case 2 -> { // 육각형
                board.setFitWidth(500);
                board.setFitHeight(450);
            }
        }
        board.setTranslateY(42);
        board.setPreserveRatio(false);

        // 배경 이미지
        ImageView bgImage = switch (playerCount) {
            case 2 -> safeLoadImage("/fx/background/game/bg_game_2.png");
            case 3 -> safeLoadImage("/fx/background/game/bg_game_3.png");
            case 4 -> safeLoadImage("/fx/background/game/bg_game_4.png");
            default -> safeLoadImage("/fx/background/game/bg_game_2.png");
        };
        bgImage.setFitWidth(870);
        bgImage.setFitHeight(570);
        bgImage.setPreserveRatio(false);

        StackPane root = new StackPane(bgImage, Players, board, Buttons);
        this.scene = new Scene(root, 870, 570);
    }

    private StackPane throwButton (String imagePath, ThrowType type){
        ImageView img = safeLoadImage(imagePath);
        img.setFitWidth(87);
        img.setFitHeight(43);
        img.setPreserveRatio(false);
        img.setSmooth(true);
        buttonImg.add(img);

        StackPane pane = new StackPane(img);
        pane.setMaxSize(90, 45);
        buttonPane.add(pane);

        // 공통 클릭 이벤트
        int index = buttonImg.size() - 1;
        pane.setOnMouseClicked(e -> {
            selectedThrow = type;
            focus(index);
            if (type == ThrowType.RANDOM) {
                controller.getRandomYut();
            }else{
                // TODO 컨트롤러랑 연결 부분
            }
        });

        // 공통 호버 이벤트
        pane.setCursor(Cursor.HAND);
        pane.setOnMouseEntered(e -> {
            if (!isSelected(index)) {
                pane.setOpacity(0.8);
                pane.setScaleX(1.03);
                pane.setScaleY(1.03);
            }
        });
        pane.setOnMouseExited(e -> {
            if (!isSelected(index)) {
                pane.setOpacity(0.8);
                pane.setScaleX(1.03);
                pane.setScaleY(1.03);
            }
        });

        return pane;
    }

    private boolean isSelected(int index) {
        return buttonImg.get(index).getEffect() != null;
    }

    private void focus(int selectedIndex) {
        // 모든 카드 초기화
        for (int i = 0; i < buttonImg.size(); i++) {
            ImageView board = buttonImg.get(i);
            board.setEffect(null);
            StackPane pane = buttonPane.get(i);

            // 크기와 투명도 초기화
            pane.setOpacity(1.0);
            pane.setScaleX(1.0);
            pane.setScaleY(1.0);
        }

        // 선택된 버튼 그림자 효과
        DropShadow shadow = new DropShadow();
        shadow.setRadius(10.0);
        shadow.setOffsetX(0.0);
        shadow.setOffsetY(4.0);
        shadow.setColor(Color.rgb(0, 0, 0, 0.3));  // 약간 투명한 회색 그림자

        buttonImg.get(selectedIndex).setEffect(shadow);

        // 선택된 카드 전체에 살짝 확대 효과
        StackPane selectedPane = buttonPane.get(selectedIndex);
        selectedPane.setScaleX(1.02);
        selectedPane.setScaleY(1.02);
    }

    // 안전한 이미지 로딩
    private ImageView safeLoadImage(String path) {
        try {
            InputStream imageStream = getClass().getResourceAsStream(path);
            if (imageStream == null) {
                System.err.println("이미지를 찾을 수 없습니다: " + path);
                return new ImageView();
            }
            return new ImageView(new Image(imageStream));
        } catch (Exception e) {
            System.err.println("이미지 로딩 실패: " + path + " - " + e.getMessage());
            return new ImageView();
        }
    }
}
