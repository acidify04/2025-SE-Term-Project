package main.java.com.yutgame.view.fx;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import main.java.com.yutgame.controller.YutGameController;
import main.java.com.yutgame.view.fx.router.ViewRouter;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class PlayerPieceSelectView {
    private YutGameController controller;

    public enum CardType {PLAYER, PIECE}

    public int selectedPlayer = -1;
    public int selectedPiece = -1;

    private final Scene scene;

    public Scene scene() {
        return scene;
    }

    private StackPane playerImage = new StackPane();
    private StackPane pieceImage = new StackPane();
    private final List<ImageView> playerButtonImg = new ArrayList<>();
    private final List<ImageView> pieceButtonImg = new ArrayList<>();
    private final List<StackPane> playerButtons = new ArrayList<>();
    private final List<StackPane> pieceButtons = new ArrayList<>();

    public PlayerPieceSelectView(YutGameController controller,
                                 ViewRouter router,
                                 Consumer<Integer> onPlayerSelected,
                                 Consumer<Integer> onPieceSelected) {
        this.controller = controller;

        StackPane playertwo   = card("/fx/button/num/num_2.png", 2, CardType.PLAYER);
        StackPane playerthree = card("/fx/button/num/num_3.png", 3, CardType.PLAYER);
        StackPane playerfour  = card("/fx/button/num/num_4.png", 4, CardType.PLAYER);

        HBox player = new HBox(20, playertwo, playerthree, playerfour);
        player.setAlignment(Pos.BASELINE_LEFT);

        StackPane piecetwo   = card("/fx/button/num/num_2.png", 2, CardType.PIECE);
        StackPane piecethree = card("/fx/button/num/num_3.png", 3, CardType.PIECE);
        StackPane piecefour  = card("/fx/button/num/num_4.png", 4, CardType.PIECE);
        StackPane piecefive  = card("/fx/button/num/num_5.png", 5, CardType.PIECE);

        HBox piece = new HBox(20, piecetwo, piecethree, piecefour, piecefive);
        piece.setAlignment(Pos.BASELINE_RIGHT);

        HBox select = new HBox(160, player, piece);
        select.setTranslateX(95);
        select.setTranslateY(270);

        ImageView initPlayer = safeLoadImage("/fx/set/setPlayer/null.png");
        initPlayer.setFitWidth(360);
        initPlayer.setFitHeight(151);
        playerImage.getChildren().add(initPlayer);

        ImageView initPiece = safeLoadImage("/fx/set/setPlayer/null.png");
        initPiece.setFitWidth(360);
        initPiece.setFitHeight(151);
        pieceImage.getChildren().add(initPiece);

        HBox results = new HBox(80, playerImage, pieceImage);
        results.setTranslateX(45);
        results.setTranslateY(-10);

        // 버튼들을 적절한 위치로 조정
        ImageView back = clickableImage("/fx/button/btn_back.png",
                e -> router.showBoardSelect(controller));
        ImageView next = clickableImage("/fx/button/btn_next.png",
                e -> {
                    onPlayerSelected.accept(selectedPlayer);  // 선택된 인덱스를 외부로 전달!
                    onPieceSelected.accept(selectedPiece);
                    //router.showGameBoard(controller);
                    router.getFXView().getController().initializeGame();
                });
        back.setFitWidth(250);
        next.setFitWidth(250);

        // 처음에는 next를 비활성화 + 흐릿하게
        next.setDisable(true);
        next.setOpacity(0.5);

        HBox btnLine = new HBox(350, back, next);
        btnLine.setAlignment(Pos.CENTER);
        btnLine.setTranslateY(175);  // 적절한 위치로 조정

        VBox content = new VBox(60, select, btnLine);
        content.setAlignment(Pos.CENTER);

        // 배경 이미지
        ImageView bgImage = safeLoadImage("/fx/background/bg_player.png");
        bgImage.setFitWidth(1028);
        bgImage.setFitHeight(672);
        bgImage.setPreserveRatio(false);

        StackPane root = new StackPane(bgImage, results, content);
        this.scene = new Scene(root, 870, 570);
    }

    private StackPane card(String imagePath, int kind, CardType type) {
        ImageView img = safeLoadImage(imagePath);
        img.setFitWidth(69);
        img.setFitHeight(69);
        img.setPreserveRatio(false);
        img.setSmooth(true);

        StackPane pane = new StackPane(img);

        // 공통 리스트
        List<ImageView> buttonImgList = (type == CardType.PLAYER) ? playerButtonImg : pieceButtonImg;
        List<StackPane> buttonPaneList = (type == CardType.PLAYER) ? playerButtons : pieceButtons;

        buttonImgList.add(img);
        buttonPaneList.add(pane);
        int index = buttonImgList.size() - 1;

        // 공통 클릭 이벤트
        pane.setOnMouseClicked(e -> {
            if (type == CardType.PLAYER) selectedPlayer = kind;
            else selectedPiece = kind;
            //onSelect.accept(kind);
            focus(index, type);

            // ↓↓↓↓↓ [수정] 플레이어 수와 말의 수 모두 선택 시 next 버튼 활성화 + 선명하게
            if (selectedPlayer != -1 && selectedPiece != -1) {
                ImageView nextBtn = (ImageView)((HBox)((VBox)((StackPane)pane.getScene().getRoot())
                        .getChildren().get(2)) // content (VBox)
                        .getChildren().get(1)) // btnLine (HBox)
                        .getChildren().get(1); // next button (ImageView)
                nextBtn.setDisable(false);
                nextBtn.setOpacity(1.0);
            }
        });

        // 공통 호버 이벤트
        pane.setCursor(Cursor.HAND);
        pane.setOnMouseEntered(e -> {
            if (!isSelected(index, type)) {
                pane.setOpacity(0.8);
                pane.setScaleX(1.03);
                pane.setScaleY(1.03);
            }
        });
        pane.setOnMouseExited(e -> {
            if (!isSelected(index, type)) {
                pane.setOpacity(1.0);
                pane.setScaleX(1.0);
                pane.setScaleY(1.0);
            }
        });

        return pane;
    }

    private boolean isSelected(int index, CardType type) {
        List<ImageView> list = (type == CardType.PLAYER) ? playerButtonImg : pieceButtonImg;
        return list.get(index).getEffect() != null;
    }

    private void focus(int selectedIndex, CardType type) {
        List<ImageView> buttonImgList = (type == CardType.PLAYER) ? playerButtonImg : pieceButtonImg;
        List<StackPane> buttonPaneList = (type == CardType.PLAYER) ? playerButtons : pieceButtons;

        for (int i = 0; i < buttonImgList.size(); i++) {
            ImageView img = buttonImgList.get(i);
            img.setEffect(null);
            img.setImage(new Image("/fx/button/num/num_" + (i + 2) + ".png"));  // 원래 이미지로 되돌림

            StackPane pane = buttonPaneList.get(i);
            pane.setOpacity(1.0);
            pane.setScaleX(1.0);
            pane.setScaleY(1.0);
        }

        ImageView selectedButton = buttonImgList.get(selectedIndex);
        selectedButton.setImage(new Image("/fx/button/num/highlight_" + (selectedIndex + 2) + ".png"));

        StackPane selectedPane = buttonPaneList.get(selectedIndex);
        selectedPane.setScaleX(1.02);
        selectedPane.setScaleY(1.02);

        ImageView img;
        if (type == CardType.PLAYER) {
            img = safeLoadImage("/fx/set/setPlayer/player_" + (selectedIndex + 2) + ".png");
            playerImage.getChildren().add(img);
        } else {
            img = safeLoadImage("/fx/set/setPiece/piece_" + (selectedIndex + 2) + ".png");
            pieceImage.getChildren().add(img);
        }
        img.setFitHeight(151);
        img.setPreserveRatio(true);
        img.setSmooth(true);
    }

    // 버튼 크기 및 클릭 가능한 이미지
    private ImageView clickableImage(String path, EventHandler<ActionEvent> act) {
        ImageView iv = safeLoadImage(path);
        iv.setFitWidth(200);  // 적절한 크기로 조정
        iv.setPreserveRatio(true);
        iv.setSmooth(true);

        iv.setOnMouseClicked(e -> act.handle(new ActionEvent()));
        iv.setCursor(Cursor.HAND);

        // 호버 효과
        iv.setOnMouseEntered(e -> {
            iv.setOpacity(0.8);
            iv.setScaleX(1.05);
            iv.setScaleY(1.05);
        });

        iv.setOnMouseExited(e -> {
            iv.setOpacity(1.0);
            iv.setScaleX(1.0);
            iv.setScaleY(1.0);
        });

        return iv;
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
