package main.java.com.yutgame.view.fx;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import main.java.com.yutgame.controller.YutGameController;
import main.java.com.yutgame.model.Player;
import main.java.com.yutgame.model.YutThrowResult;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 플레이어 각각의 정보를 하나의 판으로 생성
 */
public class PlayerInform extends Pane{
    private final YutGameController controller;
    private final GameBoardView gameBoardView;

    private boolean isTurn;
    private final int playerIndex;
    private final int totalPieces;
    private int nonStartPieceNum;
    private List<YutThrowResult> results = new ArrayList<>();

    private StackPane playerPane = new StackPane();         // 플레이어 이미지 저장할 판
    private StackPane piecePane = new StackPane();          // 플레이어의 모든 피스 저장할 판
    private VBox playerPiecesResult;
    private StackPane resultPane = new StackPane();         // 윷 결과판 전체가 저장될 공간
    private HBox resultItemsBox = new HBox(5);           // 윷 결과를 일렬로 저장할 박스
    private ScrollPane scrollContainer = new ScrollPane();  // 윷 결과가 많아질 때 스크롤 할 수 있는 판

    public PlayerInform(YutGameController controller, GameBoardView gameBoardView, boolean isTurn, int playerNum, int totalPieces, int nonStartPieceNum, List<YutThrowResult> results) {
        this.controller = controller;
        this.gameBoardView = gameBoardView;
        this.isTurn = isTurn;
        this.playerIndex = (playerNum + 1);
        this.totalPieces = totalPieces;
        this.nonStartPieceNum = nonStartPieceNum;
        this.results = results;
        drawPlayerInform();
    }

    public void setNonStartPieceNum (int nonStartPieceNum){
        this.nonStartPieceNum = nonStartPieceNum;
        System.out.println("nonStart : " + nonStartPieceNum);
        drawPieces();
    }

    public void setYutResults (List<YutThrowResult> results){
        this.results = results;
        drawYutResult();
    }

    public void setIsTurn(boolean isTurn){
        this.isTurn = isTurn;
        drawPlayer();
    }

    public Pane drawPlayerInform() {
        drawPlayer();
        drawPieces();
        drawResultPane();

        // 사용자와 말, 윷 결과판 수직박스로 묶기
        if (playerIndex < 3){   // 윗 줄은 플레이어 이미지가 위로 오게
            playerPiecesResult = new VBox(playerPane, piecePane, resultPane);
            playerPiecesResult.setSpacing(10);
        }else if (playerIndex >= 3){   // 아래 줄은 피스 이미지가 위로 오게
            playerPiecesResult = new VBox(resultPane, piecePane, playerPane);
            playerPiecesResult.setSpacing(10);
        }
        else{
            System.out.println("플레이어 수 선택 오류");
        }

        this.getChildren().add(playerPiecesResult);

        return this;
    }

    private void drawPlayer(){
        playerPane.getChildren().clear();   // 판 초기화
        if (isTurn){  // 현재 플레이어의 턴인 경우
            ImageView img = safeLoadImage("/fx/player/player_" + playerIndex + "_highlight.png");
            img.setFitWidth(100);
            img.setFitHeight(100);
            img.setPreserveRatio(false);
            img.setSmooth(true);
            playerPane.getChildren().add(img);
        }else {
            ImageView img = safeLoadImage("/fx/player/player_" + playerIndex + ".png");
            img.setFitWidth(100);
            img.setFitHeight(100);
            img.setPreserveRatio(false);
            img.setSmooth(true);
            playerPane.getChildren().add(img);
        }
    }

    private void drawPieces() {
        piecePane.setPrefSize(143, 63);
        int nonStartIndex = 0;
        for (nonStartIndex = 0 ; nonStartIndex < nonStartPieceNum; nonStartIndex++) {
            ImageView pieceImg = safeLoadImage("/fx/piece/piece_" + playerIndex + ".png");
            pieceImg.setFitWidth(28);
            pieceImg.setFitHeight(38);
            pieceImg.setPreserveRatio(false);
            pieceImg.setSmooth(true);

            // 중앙 정렬을 유지하면서 좌우로 퍼지게
            double offset = (nonStartIndex - (totalPieces - 1) / 2.0) * 30;
            pieceImg.setTranslateX(offset);

            piecePane.getChildren().add(pieceImg);
        }
        for (int i = nonStartIndex; i < totalPieces; i++){
            ImageView pieceImg = safeLoadImage("/fx/piece/piece_goal.png");
            pieceImg.setFitWidth(28);
            pieceImg.setFitHeight(38);
            pieceImg.setPreserveRatio(false);
            pieceImg.setSmooth(true);

            // 중앙 정렬을 유지하면서 좌우로 퍼지게
            double offset = (i - (totalPieces - 1) / 2.0) * 30;
            pieceImg.setTranslateX(offset);

            piecePane.getChildren().add(pieceImg);
        }
    }

    private StackPane drawResultPane (){
        ImageView pane = safeLoadImage("/fx/result/blank.png");
        pane.setFitWidth(153);
        pane.setFitHeight(38);
        pane.setPreserveRatio(false);
        pane.setSmooth(true);

        resultItemsBox.setAlignment(Pos.CENTER_LEFT);
        resultItemsBox.setPadding(new Insets(3, 5, 0, 5));

        scrollContainer.setContent(resultItemsBox);
        scrollContainer.setPrefSize(153, 30); // 전체 결과 박스 영역
        scrollContainer.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollContainer.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollContainer.setPannable(true); // 드래그로 스와이프 가능
        scrollContainer.setStyle("""
            -fx-background-color: transparent;
            -fx-control-inner-background: transparent;
            -fx-background-insets: 0;
            -fx-padding: 0;
            -fx-border-color: transparent;
            -fx-box-border: transparent;
            -fx-focus-color: transparent;
            -fx-faint-focus-color: transparent;
            -fx-background: transparent;
        """);
        scrollContainer.setTranslateY(3);

        resultPane.getChildren().clear();
        resultPane.getChildren().addAll(pane, scrollContainer);
        resultPane.setPrefSize(153, 38);
        resultPane.setStyle("-fx-background-color: transparent;");

        return resultPane;
    }

    private void drawYutResult (){
        resultItemsBox.getChildren().clear(); // 기존 결과 초기화

        for (YutThrowResult yutThrowResult : results) {
            ImageView img = switch (yutThrowResult) {
                case DO -> safeLoadImage("/fx/result/do.png");
                case GAE -> safeLoadImage("/fx/result/gae.png");
                case GEOL -> safeLoadImage("/fx/result/geol.png");
                case YUT -> safeLoadImage("/fx/result/yut.png");
                case MO -> safeLoadImage("/fx/result/mo.png");
                case BAK_DO -> safeLoadImage("/fx/result/back.png");
            };

            img.setFitWidth(25);
            img.setFitHeight(25);
            img.setPreserveRatio(false);
            img.setSmooth(true);

            resultItemsBox.getChildren().add(img);
        }
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
