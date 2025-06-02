package main.java.com.yutgame.view.fx;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.animation.FadeTransition;
import javafx.util.Duration;
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

    private StackPane newPieceButton = new StackPane();
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
        updateNewPieceButtonState(); // 버튼 상태 업데이트 추가
    }

    public void setYutResults (List<YutThrowResult> results){
        this.results = results;
        drawYutResult();
        updateNewPieceButtonState(); // ★ 추가: 윷 결과 변경 시 버튼 상태도 업데이트
    }

    public void setIsTurn(boolean isTurn){
        System.out.println("setIsTurn실행");
        this.isTurn = isTurn;
        drawPlayer();
        updateNewPieceButtonState(); // 버튼 상태 업데이트 추가
    }

    public Pane drawPlayerInform() {
        drawPlayer();
        drawPieces();
        drawResultPane();
        drawNewButton();

        // 사용자와 말, 윷 결과판 수직박스로 묶기
        if (playerIndex < 3){   // 윗 줄은 플레이어 이미지가 위로 오게
            playerPiecesResult = new VBox(playerPane, piecePane, resultPane, newPieceButton);
            playerPiecesResult.setSpacing(5);
        }else if (playerIndex >= 3){   // 아래 줄은 피스 이미지가 위로 오게
            playerPiecesResult = new VBox(newPieceButton, resultPane, piecePane, playerPane);
            playerPiecesResult.setSpacing(5);
        }
        else{
            System.out.println("플레이어 수 선택 오류");
        }

        this.getChildren().add(playerPiecesResult);

        return this;
    }

    private void drawPlayer(){
        System.out.println("drawPlayer");
        playerPane.getChildren().clear();   // 판 초기화
        if (isTurn){  // 현재 플레이어의 턴인 경우
            ImageView img = safeLoadImage("/fx/player/player_" + playerIndex + "_highlight.png");
            img.setFitWidth(100);
            img.setFitHeight(100);
            img.setTranslateY(5);
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
        piecePane.getChildren().clear(); // 이전 피스 초기화
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

    // =========================== [수정 시작: drawYutResult()] ============================
    private void drawYutResult() {
        resultItemsBox.getChildren().clear(); // 기존 결과 초기화

        // (1) iconList: 아이콘들을 담아두고, 나중에 unselect 시 참조
        List<ImageView> iconList = new ArrayList<>();

        // (2) selectedIndex: 현재 선택된 아이콘 (없으면 -1)
        final int[] selectedIndex = {-1};

        for (int i = 0; i < results.size(); i++) {
            YutThrowResult yutThrowResult = results.get(i);
            final int idx = i; // 람다에서 사용

            // 기본 아이콘(선택 해제 상태)
            ImageView img = switch (yutThrowResult) {
                case DO     -> safeLoadImage("/fx/result/do.png");
                case GAE    -> safeLoadImage("/fx/result/gae.png");
                case GEOL   -> safeLoadImage("/fx/result/geol.png");
                case YUT    -> safeLoadImage("/fx/result/yut.png");
                case MO     -> safeLoadImage("/fx/result/mo.png");
                case BAK_DO -> safeLoadImage("/fx/result/back.png");
            };
            img.setFitWidth(25);
            img.setFitHeight(25);
            img.setPreserveRatio(false);
            img.setSmooth(true);

            // '선택됨' 아이콘
            String selectedPath = switch (yutThrowResult) {
                case DO     -> "/fx/result/do_selected.png";
                case GAE    -> "/fx/result/gae_selected.png";
                case GEOL   -> "/fx/result/geol_selected.png";
                case YUT    -> "/fx/result/yut_selected.png";
                case MO     -> "/fx/result/mo_selected.png";
                case BAK_DO -> "/fx/result/back_selected.png";
            };

            img.setOnMouseClicked(e -> {
                if (selectedIndex[0] == idx) {
                    // 이미 선택된 아이콘을 다시 누르면 해제
                    selectedIndex[0] = -1;

                    // 원본 이미지 복원
                    Image originalImg = switch (yutThrowResult) {
                        case DO     -> safeLoadImage("/fx/result/do.png").getImage();
                        case GAE    -> safeLoadImage("/fx/result/gae.png").getImage();
                        case GEOL   -> safeLoadImage("/fx/result/geol.png").getImage();
                        case YUT    -> safeLoadImage("/fx/result/yut.png").getImage();
                        case MO     -> safeLoadImage("/fx/result/mo.png").getImage();
                        case BAK_DO -> safeLoadImage("/fx/result/back.png").getImage();
                    };
                    img.setImage(originalImg);

                    // GameBoardView에도 윷 선택 해제
                    gameBoardView.clearSelectedYutResult();

                } else {
                    // 새롭게 다른 아이콘을 선택
                    // 1) 이전 선택 아이콘 해제
                    if (selectedIndex[0] != -1) {
                        int old = selectedIndex[0];
                        YutThrowResult oldTr = results.get(old);
                        ImageView oldIcon = iconList.get(old);

                        // 원본 복귀
                        Image oldOriginal = switch (oldTr) {
                            case DO     -> safeLoadImage("/fx/result/do.png").getImage();
                            case GAE    -> safeLoadImage("/fx/result/gae.png").getImage();
                            case GEOL   -> safeLoadImage("/fx/result/geol.png").getImage();
                            case YUT    -> safeLoadImage("/fx/result/yut.png").getImage();
                            case MO     -> safeLoadImage("/fx/result/mo.png").getImage();
                            case BAK_DO -> safeLoadImage("/fx/result/back.png").getImage();
                        };
                        oldIcon.setImage(oldOriginal);
                    }

                    // 2) 현재 아이콘을 선택된 이미지로
                    selectedIndex[0] = idx;
                    Image selImg = new Image(getClass().getResourceAsStream(selectedPath));
                    img.setImage(selImg);

                    // GameBoardView에 "이 윷을 선택했다" 통보
                    gameBoardView.setSelectedYutResult(yutThrowResult);
                }
            });

            // 호버 시 반짝
            FadeTransition hoverBlink = new FadeTransition(Duration.millis(600), img);
            hoverBlink.setFromValue(1.0);
            hoverBlink.setToValue(0.5);
            hoverBlink.setCycleCount(FadeTransition.INDEFINITE);
            hoverBlink.setAutoReverse(true);

            img.setOnMouseEntered(e -> hoverBlink.playFromStart());
            img.setOnMouseExited(e -> {
                hoverBlink.stop();
                img.setOpacity(1.0);
            });

            img.setCursor(Cursor.HAND);

            iconList.add(img);
            resultItemsBox.getChildren().add(img);
        }
    }
    // =========================== [수정 끝] ==============================================

    private void drawNewButton() {
        // 기존 코드: newBtn 생성
        ImageView newBtn = switch (playerIndex) {
            case 1 -> clickableImage("/fx/button/new_1.png",
                    e -> gameBoardView.onNewPieceButtonClicked());
            case 2 -> clickableImage("/fx/button/new_2.png",
                    e -> gameBoardView.onNewPieceButtonClicked());
            case 3 -> clickableImage("/fx/button/new_3.png",
                    e -> gameBoardView.onNewPieceButtonClicked());
            case 4 -> clickableImage("/fx/button/new_4.png",
                    e -> gameBoardView.onNewPieceButtonClicked());
            default -> throw new IllegalStateException("Unexpected value: " + playerIndex);
        };

        // 플레이어가 현재 턴이고 + 아직 새 말이 남아 있으면 활성
        // 아니라면 disable
        //boolean canUseNewPiece = (isTurn && nonStartPieceNum > 0);
        //newBtn.setDisable(!canUseNewPiece);

        if (playerIndex < 3){
            newBtn.setTranslateY(5);
        }
        newPieceButton.getChildren().clear();  // 혹시 기존 버튼 제거
        newPieceButton.getChildren().add(newBtn);

        // ⭐ 초기 상태 설정
        updateNewPieceButtonState();
    }

    private ImageView clickableImage(String path, EventHandler<ActionEvent> act) {
        ImageView iv = new ImageView(new Image(path));
        iv.setFitWidth(152);
        iv.setFitHeight(32);
        iv.setPreserveRatio(false);
        iv.setSmooth(true);

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


    public void updateNewPieceButtonState() {
        if (newPieceButton != null && !newPieceButton.getChildren().isEmpty()) {
            // ★ 수정: 윷 선택 여부와 백도 여부 체크
            boolean hasSelectedYut = gameBoardView.getCurrentlySelectedYutResult() != null;
            YutThrowResult selectedYut = gameBoardView.getCurrentlySelectedYutResult();
            boolean isBackDoSelected = (selectedYut == YutThrowResult.BAK_DO);

            // ★ 수정: 백도가 선택되었으면 New Piece 버튼 비활성화
            boolean shouldEnable = isTurn && nonStartPieceNum > 0 && hasSelectedYut && !isBackDoSelected;

            System.out.println("Player" + playerIndex + " 새 말 버튼 상태 업데이트 - " +
                    "Turn: " + isTurn +
                    ", PieceNum: " + nonStartPieceNum +
                    ", HasYut: " + hasSelectedYut +
                    ", SelectedYut: " + selectedYut +
                    ", IsBackDoSelected: " + isBackDoSelected +
                    ", Enable: " + shouldEnable);

            // StackPane disable/enable
            newPieceButton.setDisable(!shouldEnable);
            newPieceButton.setOpacity(shouldEnable ? 1.0 : 0.5);

            // ImageView도 함께 disable/enable
            if (newPieceButton.getChildren().get(0) instanceof ImageView imageView) {
                imageView.setDisable(!shouldEnable);
            }
        }
    }
}