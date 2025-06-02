package main.java.com.yutgame.view.fx;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import main.java.com.yutgame.controller.YutGameController;
import main.java.com.yutgame.dto.PieceDecisionResult;
import main.java.com.yutgame.model.BoardNode;
import main.java.com.yutgame.model.Piece;
import main.java.com.yutgame.model.Player;
import main.java.com.yutgame.model.YutThrowResult;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.function.Consumer;
import javafx.application.Platform;

import java.util.stream.Collectors;
import java.util.Set;
import java.util.HashSet;

public class GameBoardView {
    private YutGameController controller;
    private BoardPane boardPane;
    private List<PlayerInform> allPlayerInforms = new ArrayList<>();

    private final Scene scene;

    public Scene scene() {
        return scene;
    }

    private boolean isRandomThrow = false;
    private int currentPlayerIndex = 1;
    private List<YutThrowResult> currentResults = new ArrayList<>();
    private int currentPlayerPieceNum = 0;

    private List<ImageView> buttonImg = new ArrayList<>();
    private List<StackPane> buttonPane = new ArrayList<>();

    // 현재 사용자에게 보여주고 있는 선택 가능한 말 목록
    private List<Piece> waitingPieceChoices;

    // 사용자가 Piece를 클릭했을 때 호출할 콜백 함수
    private Consumer<Piece> pieceSelectedCallback;

    private YutThrowResult selectedYutResult = null;
    private Piece selectedPiece = null; // onPieceClicked()에서 토글로 쓰는 경우

    private boolean turnInProgress = false; // 이번 턴에 윷을 아직 안 던졌으면 false, 던지면 true

    private boolean isCurrentPlayerTurn() {
        // controller.getCurrentPlayer()의 index가 currentPlayerIndex와 같은지 등
        return controller.getCurrentPlayer().getIndex() == currentPlayerIndex;
    }

    public void setSelectedYutResult(YutThrowResult result) {
        System.out.println(">>> setSelectedYutResult 호출: " + result);
        System.out.println(">>> 이전 선택된 윷: " + this.selectedYutResult);

        // ★ 수정: 기존에 선택된 윷이 있고 다른 윷을 선택하는 경우에만 하이라이트 해제
        if (this.selectedYutResult != null && !this.selectedYutResult.equals(result) && boardPane != null) {
            System.out.println(">>> 다른 윷 선택 - 기존 하이라이트 해제: " + this.selectedYutResult + " -> " + result);
            boardPane.clearAllHighlights();
        }

        this.selectedYutResult = result;
        clearPieceSelectionState();
        selectedPiece = null;

        // ★ 추가: 윷 선택 후 즉시 말 상태 업데이트
        System.out.println(">>> 윷 선택 완료 - 말 상태 업데이트 시작");
        updateAllPlayerButtonStates();

        // ★ 추가: 보드도 다시 그려서 말 클릭 상태 업데이트
        if (boardPane != null) {
            boardPane.drawBoard();
        }

        System.out.println("선택된 윷 결과: " + result);
    }

    public void clearSelectedYutResult() {
        this.selectedYutResult = null;
        // ★ 추가: 말 선택도 해제하고 노드 하이라이트도 해제
        clearPieceSelectionState();
        selectedPiece = null;

        // ★ 수정: BoardPane의 public 메소드 사용
        if (boardPane != null && boardPane.hasHighlightedNodes()) {
            boardPane.clearAllHighlights();
        }

        // ★ 추가: 윷 선택 해제 시 모든 플레이어의 버튼 상태 업데이트
        updateAllPlayerButtonStates();

        System.out.println("윷 선택 해제됨 - 모든 상태 초기화");
    }

    // PlayerInform가 아닌 GameBoardView 쪽에서 현재 선택된 윷 결과를 가져가기 위해
    public YutThrowResult getCurrentlySelectedYutResult() {
        System.out.println(">>> getCurrentlySelectedYutResult 호출 - 현재 선택: " + selectedYutResult);
        return selectedYutResult;
    }

    /**
     * 윷놀이판 초기 세팅
     */
    public GameBoardView(YutGameController controller, int boardType, int playerCount, int pieceCount) {
        this.controller = controller;

        // 게임판 이미지 그리기
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
        //board.setStyle("-fx-border-color: red; -fx-border-width: 2px;");

        // 노드 그리기
        this.boardPane = new BoardPane(controller, this);
        boardPane.setTranslateY(35);
        //boardPane.setStyle("-fx-border-color: red; -fx-border-width: 2px;");

        // Players, Pieces, ResultPane 초기화
        for (int i = 0; i < playerCount; i++) {
            PlayerInform playerInform;
            if (i==0){
                playerInform = new PlayerInform(controller, this, true, i, pieceCount, pieceCount, null);
            }else{
                playerInform = new PlayerInform(controller, this, false, i, pieceCount, pieceCount, null);
            }
            allPlayerInforms.add(playerInform);
        }
        // playerInform 위치 설정
        HBox upPlayers = new HBox(500, allPlayerInforms.get(0), allPlayerInforms.get(1));
        upPlayers.setAlignment(Pos.CENTER);
        upPlayers.setTranslateY(5);
        //upPlayers.setStyle("-fx-border-color: red; -fx-border-width: 2px;");

        HBox underPlayers = new HBox();
        if (playerCount == 3){
            underPlayers.getChildren().add(allPlayerInforms.get(2));
            underPlayers.setTranslateX(35);
            underPlayers.setTranslateY(-30);
        }else if (playerCount == 4){
            underPlayers.getChildren().addAll(allPlayerInforms.get(2), allPlayerInforms.get(3));
            underPlayers.setSpacing(500);
            upPlayers.setAlignment(Pos.CENTER);
            underPlayers.setTranslateX(35);
            underPlayers.setTranslateY(-30);
        }
        //underPlayers.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
        VBox Players = new VBox(100, upPlayers, underPlayers);
        Players.setAlignment(Pos.CENTER);
        //Players.setStyle("-fx-border-color: red; -fx-border-width: 2px;");

        // 윷 던지기 버튼 그리기
        StackPane randomThrow = drawthrowButton("/fx/button/game/randomBtn.png", true);
        StackPane manualThrow = drawthrowButton("/fx/button/game/selectBtn.png", false);
        //randomThrow.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
        //manualThrow.setStyle("-fx-border-color: red; -fx-border-width: 2px;");

        HBox Buttons = new HBox(randomThrow, manualThrow);
        Buttons.setTranslateX(120);
        Buttons.setTranslateY(-235);
        Buttons.setMaxSize(176, 45);
        //Buttons.setStyle("-fx-border-color: red; -fx-border-width: 2px;");

        // 배경 이미지 그리기
        ImageView bgImage = switch (playerCount) {
            case 2 -> safeLoadImage("/fx/background/game/bg_game_2.png");
            case 3 -> safeLoadImage("/fx/background/game/bg_game_3.png");
            case 4 -> safeLoadImage("/fx/background/game/bg_game_4.png");
            default -> safeLoadImage("/fx/background/game/bg_game_2.png");
        };
        bgImage.setFitWidth(870);
        bgImage.setFitHeight(570);
        bgImage.setPreserveRatio(false);

        StackPane root = new StackPane(bgImage, board, Players, boardPane,Buttons);
        this.scene = new Scene(root, 870, 570);
    }

    public BoardPane getBoardPane(){
        return boardPane;
    }

    private StackPane drawthrowButton(String imagePath, boolean isRandom) {
        ImageView img = safeLoadImage(imagePath);
        img.setFitWidth(87);
        img.setFitHeight(43);
        img.setPreserveRatio(false);
        img.setSmooth(true);
        buttonImg.add(img);

        StackPane pane = new StackPane(img);
        pane.setMaxSize(90, 45);
        buttonPane.add(pane);

        int index = buttonImg.size() - 1;
        pane.setOnMouseClicked(e -> {
            // 0) 이미 이번 턴에 윷을 던졌다면
            if (turnInProgress) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("이미 윷을 던졌습니다");
                alert.setHeaderText(null);
                alert.setContentText("이번 턴에 이미 윷을 던졌습니다. 말을 이동하세요!");
                alert.showAndWait();
                return;
            }

            // 1) 자기 턴인지 체크
            if (!isCurrentPlayerTurn()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("턴 아님");
                alert.setHeaderText(null);
                alert.setContentText("지금은 당신의 턴이 아닙니다!");
                alert.showAndWait();
                return;
            }

            // 2) 버튼 시각 효과
            focus(index);

            // 3) 윷 던지기
            if (isRandom) {
                YutThrowResult selected = controller.getRandomYut();
                isRandomThrow = true;
                processAllThrows(selected);
            } else {
                List<String> options = List.of("빽도", "도", "개", "걸", "윷", "모");
                ChoiceDialog<String> dialog = new ChoiceDialog<>("도", options);
                dialog.setTitle("지정 윷 던지기");
                dialog.setHeaderText(null);
                dialog.setContentText("결과를 선택하세요:");

                Optional<String> result = dialog.showAndWait();
                result.ifPresent(choiceStr -> {
                    int choiceIndex = options.indexOf(choiceStr);
                    YutThrowResult selected = controller.getSetYut(choiceIndex);
                    processAllThrows(selected);
                });
            }

            // 4) 이번 턴에는 더 이상 윷을 던질 수 없도록 플래그 설정
            turnInProgress = true;
        });

        // 호버 이벤트
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

    /**
     * 윷·모가 나올 때까지 계속 던지고, 최종 결과 리스트를 반환 (컨트롤러 이용)
     */
    private void processAllThrows(YutThrowResult firstResult) {
        Player currentPlayer = controller.getCurrentPlayer();

        // 빽도 단독이고 출발하지 않은 말만 있는 경우 미리 체크
        if (firstResult == YutThrowResult.BAK_DO) {
            boolean notStarted = controller.getNotStarted(currentPlayer);
            if (notStarted) {
                // 빽도 사용 불가 안내 및 즉시 턴 넘기기
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("알림");
                alert.setHeaderText(null);
                alert.setContentText("출발하지 않은 상태에서는 빽도를 사용할 수 없습니다. 턴을 넘깁니다.");
                alert.showAndWait();

                // ★ 수정: 간단한 턴 넘기기 처리
                System.out.println(">>> 빽도 사용 불가 - 턴 넘기기");

                turnInProgress = false; // ★ 추가: 다음 플레이어가 윷을 던질 수 있도록

                // currentResults에 추가하지 않고 바로 턴 넘기기
                // ★ 수정: turnInProgress는 건드리지 않고 단순히 다음 턴으로
                isRandomThrow = false;

                // 현재 플레이어 턴 해제
                if (currentPlayerIndex >= 1 && currentPlayerIndex <= allPlayerInforms.size()) {
                    PlayerInform playerInform = allPlayerInforms.get(currentPlayerIndex - 1);
                    playerInform.setIsTurn(false);
                }

                // 다음 턴으로
                controller.nextTurn();
                currentPlayerIndex = controller.getCurrentPlayer().getIndex();
                repaint(3);

                System.out.println("빽도 사용 불가로 턴 넘기기 완료 - 다음 플레이어: " + controller.getCurrentPlayer().getName());
                return; // 여기서 메서드 종료
            }
        }

        // 빽도 사용 가능하거나 다른 결과인 경우 기존 로직 진행
        List<YutThrowResult> results = controller.collectThrowResults(
                firstResult,
                isRandomThrow,
                this::getSetYutResult,
                this::showResult,
                () -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("다시 던지기");
                    alert.setHeaderText(null);
                    alert.setContentText("윷을 한 번 더 던지세요.");
                    alert.showAndWait();
                }
        );

        repaint(1);
        // ★ 추가: 윷 던지기 완료 후 버튼 상태 업데이트
        updateAllPlayerButtonStates();

        isRandomThrow = false;
        applyThrowSelections();
    }

    /**
     * 윷 계속 던져서 결과 모으기
     */
    private YutThrowResult getSetYutResult() {
        List<String> options = List.of("빽도", "도", "개", "걸", "윷", "모");

        ChoiceDialog<String> dialog = new ChoiceDialog<>("도", options);
        dialog.setTitle("지정 윷 던지기");
        dialog.setHeaderText(null);
        dialog.setContentText("결과를 선택하세요:");

        Optional<String> result = dialog.showAndWait();
        String choiceStr = result.orElse("도");  // 아무 것도 선택하지 않으면 기본값 "도"

        YutThrowResult sel = switch (choiceStr) {
            case "빽도" -> YutThrowResult.BAK_DO;
            case "도"   -> YutThrowResult.DO;
            case "개"   -> YutThrowResult.GAE;
            case "걸"   -> YutThrowResult.GEOL;
            case "윷"   -> YutThrowResult.YUT;
            case "모"   -> YutThrowResult.MO;
            default     -> YutThrowResult.DO; // fallback
        };
        controller.throwYutManual(sel);
        repaint(1);
        return sel;
    }

    /**
     * 윷 결과 표시
     */
    private void showResult(YutThrowResult result) {
        // 배경판
        ImageView background = safeLoadImage("/fx/result/yutpane.png");
        background.setFitWidth(500);
        background.setFitHeight(500);
        background.setPreserveRatio(false);

        // GIF 이미지 (윷 던지기 애니메이션)
        ImageView gifView = switch (result) {
            case DO -> new ImageView(new Image(getClass().getResource("/fx/result/resultDo.gif").toExternalForm()));
            case GAE -> new ImageView(new Image(getClass().getResource("/fx/result/resultGae.gif").toExternalForm()));
            case GEOL -> new ImageView(new Image(getClass().getResource("/fx/result/resultGeol.gif").toExternalForm()));
            case YUT -> new ImageView(new Image(getClass().getResource("/fx/result/resultYut.gif").toExternalForm()));
            case MO -> new ImageView(new Image(getClass().getResource("/fx/result/resultMo.gif").toExternalForm()));
            case BAK_DO -> new ImageView(new Image(getClass().getResource("/fx/result/resultBackDo.gif").toExternalForm()));
        };
        gifView.setFitWidth(300);
        gifView.setFitHeight(300);
        gifView.setPreserveRatio(true);

        background.setTranslateY(30);
        gifView.setTranslateY(30);

        // 오버레이 구성: 배경 → GIF → 결과 이미지
        StackPane overlay = new StackPane(background, gifView);
        overlay.setAlignment(Pos.CENTER);
        overlay.setPrefSize(500, 500);

        Platform.runLater(() -> {
            if (scene != null && scene.getRoot() instanceof StackPane rootPane) {
                rootPane.getChildren().add(overlay);

                // 애니메이션 후 결과 이미지 표시
                PauseTransition remove = new PauseTransition(Duration.seconds(1.5));
                remove.setOnFinished(ev -> rootPane.getChildren().remove(overlay));
                remove.play();
            } else {
                System.err.println("Scene is null or root is not StackPane – overlay not added.");
            }
        });
        currentResults.add(result);
        repaint(1);
    }

    private void applyThrowSelections() {
        Player currentPlayer = controller.getCurrentPlayer();

        // 빽도 단독일 경우, 아직 출발 안한 말만 있을 때 턴 넘김
        if (currentResults.size() == 1 && currentResults.get(0) == YutThrowResult.BAK_DO) {
            boolean notStarted = controller.getNotStarted(currentPlayer);

            if (notStarted) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("알림");
                alert.setHeaderText(null);
                alert.setContentText("출발하지 않은 상태에서는 빽도를 사용할 수 없습니다. 턴을 넘깁니다.");
                alert.showAndWait();

                // ★ 중요: turnInProgress 초기화 추가
                turnInProgress = false;

                controller.nextTurn();
                // 기존 highlight 지우기
                if (currentPlayerIndex >= 1 && currentPlayerIndex <= allPlayerInforms.size()) {
                    PlayerInform playerInform = allPlayerInforms.get(currentPlayerIndex - 1);
                    playerInform.setIsTurn(false);
                }
                currentPlayerIndex = controller.getCurrentPlayer().getIndex();
                currentResults.clear();
                repaint(3);
                System.out.println("출발하지 않은 상태에서 턴 넘기기");
                System.out.println("플레이어 바뀜 => " + currentPlayerIndex);
                return;
            }
        }

        System.out.println("applyThrowSelections() - 윷/말 선택을 기다리는 상태입니다.");
    }

    public void goNext() {
        // ★ 수정: 간단한 다음 턴 처리
        System.out.println(">>> goNext 호출");

        currentPlayerIndex = controller.getCurrentPlayer().getIndex();
        repaint(3);

        // ★ 수정: turnInProgress는 건드리지 않음 (자연스러운 턴 진행 유지)
        System.out.println(">>> goNext 완료 - 현재 플레이어: " + controller.getCurrentPlayer().getName());
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    private void moveNode(Player currentPlayer, YutThrowResult chosenResult, Piece selectedPiece) {
        System.out.println("=== moveNode 시작 ===");
        System.out.println("- chosenResult: " + chosenResult);
        System.out.println("- selectedPiece: " + selectedPiece);
        System.out.println("- currentPlayer: " + currentPlayer.getName());

        final int steps = controller.getSteps(chosenResult);
        System.out.println("- steps: " + steps);

        // ★ 완전히 새로운 접근: 새 말 처리를 위한 특별 로직
        var pieceDecisions = controller.getPieceDecisions(currentPlayer, chosenResult);

        try {
            var choicesField = pieceDecisions.getClass().getDeclaredField("choices");
            choicesField.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<Piece> choices = (List<Piece>) choicesField.get(pieceDecisions);

            System.out.println(">>> Controller에서 제공하는 선택지:");
            for (int i = 0; i < choices.size(); i++) {
                Piece piece = choices.get(i);
                System.out.println("  [" + i + "] " + piece + " (currentNode: " + piece.getCurrentNode() + ")");
            }

            // 타겟 말 결정
            Piece targetPiece;
            boolean isNewPiece = false;

            if (selectedPiece == null) {
                // New Piece 버튼으로 들어온 경우
                targetPiece = choices.get(0); // 첫 번째는 항상 "새로운 말"
                isNewPiece = true;
                System.out.println(">>> New Piece 버튼으로 진입 - 새 말 사용: " + targetPiece);
            } else {
                // 기존 말 클릭으로 들어온 경우
                targetPiece = selectedPiece;
                isNewPiece = (targetPiece.getCurrentNode() == null);
                System.out.println(">>> 기존 말 클릭으로 진입 - " + (isNewPiece ? "새 말" : "기존 말") + ": " + targetPiece);
            }

            // 목적지 계산
            // 기존 코드에서 possibleDestinations 계산 부분을 수정
            List<BoardNode> possibleDestinations;
            if (isNewPiece) {
                BoardNode startNode = controller.getBoard().getStartNode();
                List<BoardNode> allDestinations = controller.getBoard().getPossibleNextNodes(startNode, steps);
                // ★ 뷰에서 지름길 규칙 적용
                possibleDestinations = applyShortcutRulesInView(startNode, allDestinations, steps);
                System.out.println(">>> 새 말: START_NODE에서 " + steps + "칸 이동 (뷰 레벨 지름길 규칙 적용)");
            } else {
                BoardNode curr = targetPiece.getCurrentNode();
                List<BoardNode> allDestinations = (steps < 0)
                        ? controller.getBoard().getPossiblePreviousNodes(curr)
                        : controller.getBoard().getPossibleNextNodes(curr, steps);

                // ★ 핵심: 뷰에서 지름길 규칙 적용
                possibleDestinations = applyShortcutRulesInView(curr, allDestinations, steps);
                System.out.println(">>> 기존 말: " + curr.getId() + "에서 " + steps + "칸 이동 (뷰 레벨 지름길 규칙 적용)");
            }

            System.out.println("- 가능한 목적지 개수: " + possibleDestinations.size());
            for (BoardNode dest : possibleDestinations) {
                System.out.println("  -> " + dest.getId());
            }

            if (possibleDestinations.isEmpty()) {
                System.out.println(">>> 이동할 노드가 없음");
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("이동 불가");
                alert.setHeaderText(null);
                alert.setContentText("이동할 수 있는 곳이 없습니다!");
                alert.showAndWait();

                currentResults.remove(chosenResult);
                clearSelectedYutResult();
                repaint(1);
                if (currentResults.isEmpty()) {
                    endTurn();
                }
                return;
            }

            final boolean finishMode = possibleDestinations.contains(controller.getBoard().getStartNode());
            final Piece finalTargetPiece = targetPiece;
            final boolean finalIsNewPiece = isNewPiece;

            System.out.println(">>> highlightNodes 호출 시작");
            if (boardPane != null) {
                boardPane.clearAllHighlights();
            }

            // moveNode 메소드의 노드 클릭 콜백 부분 수정
            boardPane.highlightNodes(possibleDestinations, clickedNode -> {
                System.out.println("=== 노드 클릭 콜백 실행 ===");
                System.out.println("- 클릭된 노드: " + clickedNode.getId());

                boardPane.unhighlightNodes(new ArrayList<>(possibleDestinations));

                try {
                    boolean moveSuccess;

                    if (finalIsNewPiece) {
                        // ★ Controller의 새 말 전용 메소드 사용
                        moveSuccess = controller.moveNewPieceToNode(finalTargetPiece, clickedNode, steps);
                    } else {
                        // ★ 기존 말은 기존 Controller 메소드 사용
                        try {
                            if (finishMode && clickedNode.getId().equals("START_NODE")) {
                                controller.isFinished(finalTargetPiece, clickedNode,
                                        controller.getBoard().getPaths(), steps);
                            } else {
                                if (steps < 0) {
                                    controller.movePiece(finalTargetPiece, clickedNode, controller.getContainsStartNode());
                                } else {
                                    controller.isFinished(finalTargetPiece, clickedNode,
                                            controller.getBoard().getPaths(), steps);
                                }
                            }
                            moveSuccess = true;
                        } catch (Exception e) {
                            System.err.println("기존 말 이동 실패: " + e.getMessage());
                            moveSuccess = false;
                        }
                    }

                    if (!moveSuccess) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("이동 실패");
                        alert.setHeaderText(null);
                        alert.setContentText("말 이동에 실패했습니다!");
                        alert.showAndWait();
                        return;
                    }

                    // 이동 성공 후 처리
                    System.out.println(">>> 이동 후 정리 작업");
                    currentResults.remove(chosenResult);
                    clearSelectedYutResult();
                    clearPieceSelectionState();

                    repaint(1);
                    repaint(2);
                    boardPane.drawBoard();

                    if (currentResults.isEmpty()) {
                        endTurn();
                    } else {
                        updateAllPlayerButtonStates();
                    }

                } catch (Exception e) {
                    System.err.println(">>> 말 이동 실패: " + e.getMessage());
                    e.printStackTrace();
                }

            }, finishMode);


            System.out.println(">>> highlightNodes 호출 완료");

        } catch (Exception e) {
            System.err.println(">>> getPieceDecisions 처리 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void endTurn() {
        System.out.println(">>> endTurn 호출 - 현재 플레이어: " + controller.getCurrentPlayer().getName());

        // ★ 수정: 턴 종료 처리 강화
        turnInProgress = false; // 다음 플레이어가 윷을 던질 수 있도록
        clearSelectedYutResult(); // 선택된 윷 결과 초기화
        clearPieceSelectionState(); // 말 선택 상태 초기화
        currentResults.clear(); // 현재 결과 목록도 초기화

        // 하이라이트도 정리
        if (boardPane != null) {
            boardPane.clearAllHighlights();
        }

        // ★ 추가: 현재 플레이어 턴 해제
        if (currentPlayerIndex >= 1 && currentPlayerIndex <= allPlayerInforms.size()) {
            PlayerInform currentPlayerInform = allPlayerInforms.get(currentPlayerIndex - 1);
            currentPlayerInform.setIsTurn(false);
            System.out.println(">>> 현재 플레이어 턴 해제: Player" + currentPlayerIndex);
        }

        // ★ 수정: 확실한 다음 턴 처리
        controller.nextTurn();
        currentPlayerIndex = controller.getCurrentPlayer().getIndex();

        // 다음 플레이어 턴 활성화
        if (currentPlayerIndex >= 1 && currentPlayerIndex <= allPlayerInforms.size()) {
            PlayerInform nextPlayerInform = allPlayerInforms.get(currentPlayerIndex - 1);
            nextPlayerInform.setIsTurn(true);
            System.out.println(">>> 다음 플레이어 턴 활성화: Player" + currentPlayerIndex);
        }

        repaint(3);
        updateAllPlayerButtonStates();

        System.out.println(">>> endTurn 완료 - 새로운 현재 플레이어: " + controller.getCurrentPlayer().getName());
    }

    /**
     * new Piece 버튼 클릭 시 실행
     */
    public void onNewPieceButtonClicked() {
        System.out.println("[New Piece] 버튼 클릭됨");

        // 0) 자기 턴인지 체크
        if (!isCurrentPlayerTurn()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("턴 아님");
            alert.setHeaderText(null);
            alert.setContentText("지금은 당신의 턴이 아닙니다!");
            alert.showAndWait();
            return;
        }

        // 1) 윷 결과가 없다면 경고
        if (currentResults.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("이동 불가");
            alert.setHeaderText(null);
            alert.setContentText("윷을 먼저 던져주세요!");
            alert.showAndWait();
            return;
        }

        // 2) 윷을 선택했는지 확인
        YutThrowResult chosenResult = getCurrentlySelectedYutResult();
        if (chosenResult == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("이동 불가");
            alert.setHeaderText(null);
            alert.setContentText("사용할 윷(도/개/걸/윷/모/빽도)을 먼저 선택해주세요!");
            alert.showAndWait();
            return;
        }

        // ★ 추가: 백도 선택 시 새 말 꺼내기 불가
        if (chosenResult == YutThrowResult.BAK_DO) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("새 말 꺼내기 불가");
            alert.setHeaderText(null);
            alert.setContentText("백도로는 새 말을 꺼낼 수 없습니다!\n이미 보드에 있는 말을 뒤로 이동시켜주세요.");
            alert.showAndWait();
            return;
        }

        // 3) '새 말'이 남아 있는지 검사
        Player currentPlayer = controller.getCurrentPlayer();
        if (currentPlayer.getNonStartPiecesNum() <= 0) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("새 말 없음");
            alert.setHeaderText(null);
            alert.setContentText("더 이상 출발할 수 있는 말이 없습니다!");
            alert.showAndWait();
            return;
        }

        // ★ 수정: null을 전달해서 selectPiece가 새 말을 선택하도록 함
        System.out.println(">>> 새 말로 moveNode 호출");
        moveNode(currentPlayer, chosenResult, null); // null = 새 말 선택 필요
    }

    /**
     * 노드 위의 말 선택 시 실행
     */
    public void onPieceClicked(Piece clickedPiece) {
        System.out.println("=== 말 클릭 이벤트 ===");
        System.out.println("- 클릭된 말: " + clickedPiece + " (Player " + clickedPiece.getOwner().getIndex() + ")");
        System.out.println("- 현재 플레이어: " + controller.getCurrentPlayer().getIndex());

        // 0) 자기 턴인지 체크
        if (!isCurrentPlayerTurn()) {
            System.out.println(">>> 턴 아님 - 경고");
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("턴 아님");
            alert.setHeaderText(null);
            alert.setContentText("지금은 당신의 턴이 아닙니다!");
            alert.showAndWait();
            return;
        }

        // ★ 내 말인지 확인
        if (clickedPiece.getOwner().getIndex() != getCurrentPlayerIndex()) {
            System.out.println(">>> 다른 플레이어의 말 - 경고");
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("다른 플레이어의 말");
            alert.setHeaderText(null);
            alert.setContentText("자신의 말만 선택할 수 있습니다!");
            alert.showAndWait();
            return;
        }

        // 1) 윷 결과가 없다면 경고
        if (currentResults.isEmpty()) {
            System.out.println(">>> 윷 결과 없음 - 경고");
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("이동 불가");
            alert.setHeaderText(null);
            alert.setContentText("윷을 먼저 던져주세요!");
            alert.showAndWait();
            return;
        }

        // 2) ★ 수정: 윷을 고르지 않았다면 명확한 안내
        YutThrowResult chosenResult = getCurrentlySelectedYutResult();
        if (chosenResult == null) {
            System.out.println(">>> 윷 선택 안됨 - 명확한 안내");
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("윷 선택 필요");
            alert.setHeaderText("먼저 윷을 선택해주세요!");
            alert.setContentText("화면 우측에 있는 윷 결과 버튼(도/개/걸/윷/모) 중 하나를 클릭한 후,\n다시 말을 선택해주세요.");
            alert.showAndWait();
            return;
        }

        System.out.println(">>> 말 클릭 검증 통과 - moveNode 호출");
        System.out.println(">>> 플로우: 윷 던지기 ✓ → 윷 선택 ✓ → 말 선택 ✓ → 노드 선택 대기");
        moveNode(controller.getCurrentPlayer(), chosenResult, clickedPiece);
    }

    private void clearPieceSelectionState() {
        waitingPieceChoices = null;
        pieceSelectedCallback = null;
    }

    public void selectPiece(Player player, YutThrowResult chosenResult, Consumer<Piece> onPieceSelected) {
        System.out.println("onNewPieceButtonClicked 인스턴스 해시: " + this);
        PieceDecisionResult pieceDecisionResult = controller.getPieceDecisions(player, chosenResult);
        List<Piece> choices = pieceDecisionResult.choices();
        System.out.println("choices: " + choices.size());
        List<String> pieceDecisions = pieceDecisionResult.decisions();

        if (controller.allPiecesFinished(player)) {
            controller.checkWin();
            if (controller.isGameOver()) {
                Alert winAlert = new Alert(Alert.AlertType.INFORMATION);
                winAlert.setTitle("게임 종료");
                winAlert.setHeaderText(null);
                winAlert.setContentText("승리자: " + controller.getWinner().getName());
                winAlert.showAndWait();
                Platform.exit();
            }
            onPieceSelected.accept(null); // 아무 것도 선택할 수 없는 경우
            return;
        }

        if (pieceDecisions.isEmpty()) {
            String msg = controller.checkBaekdo(chosenResult)
                    ? "시작지점에서 빽도를 사용하실 수 없습니다."
                    : "이 플레이어는 이동 가능한 말이 없습니다.";

            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("선택 불가");
            alert.setHeaderText(null);
            alert.setContentText(msg);
            alert.showAndWait();

            // ★ 중요: turnInProgress 초기화 추가
            turnInProgress = false;

            System.out.println("이동가능한 말 없음 : nextTurn");
            controller.nextTurn();
            if (currentPlayerIndex > 0 && currentPlayerIndex <= allPlayerInforms.size()) {
                allPlayerInforms.get(currentPlayerIndex - 1).setIsTurn(false);
            }
            currentPlayerIndex = controller.getCurrentPlayer().getIndex();
            currentResults.clear();
            System.out.println("이동가능한 말 없음");
            repaint(3);

            onPieceSelected.accept(null); // 선택할 수 없음 → null 반환
            return;
        }

        // 선택 가능한 피스 저장 및 콜백 설정
        this.waitingPieceChoices = choices;
        System.out.println("선택 가능한 말: " + waitingPieceChoices.size());
        this.pieceSelectedCallback = onPieceSelected;
    }


    private BoardNode chooseDestination(List<BoardNode> cands, String title, int finishIndex) {
        // 팝업 대신 PlayerInform 등의 UI를 통해 선택되도록 변경
        // 여기서는 임시로 첫 번째 노드를 반환
        if (cands.isEmpty()) return null;
        return cands.get(0);
    }

    private boolean isSelected(int index) {
        return buttonImg.get(index).getEffect() != null;
    }

    /**
     * 버튼 클릭 시 강조 효과
     */
    private void focus(int selectedIndex) {
        // 버튼 초기화
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

        // 선택된 버튼에 살짝 확대 효과
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

    private void repaint(int change){
        System.out.println("현재 플레이어 인덱스 : " + currentPlayerIndex);
        if (currentPlayerIndex >= 1 && currentPlayerIndex <= allPlayerInforms.size()) {
            PlayerInform playerInform = allPlayerInforms.get(currentPlayerIndex - 1);
            if (playerInform != null) {
                switch (change){
                    case 1:
                        playerInform.setYutResults(currentResults);
                        break;
                    case 2:
                        System.out.println("setNonStartPieceNum");
                        for (int i = 0; i < allPlayerInforms.size(); i++){
                            Player player = controller.getGame().getPlayers().get(i);
                            int nonStartPieceNum = player.getNonStartPiecesNum();
                            PlayerInform eachPlayer = allPlayerInforms.get(i);
                            eachPlayer.setNonStartPieceNum(nonStartPieceNum);
                        }
                        break;
                    case 3:
                        System.out.println("턴 넘어감 setIsTurn");
                        // 모든 플레이어 턴 해제 후 현재 플레이어만 활성화
                        for (PlayerInform info : allPlayerInforms) {
                            info.setIsTurn(false);
                        }
                        playerInform.setIsTurn(true);
                        // ★ 추가: 턴 전환 시 모든 플레이어 버튼 상태 업데이트
                        updateAllPlayerButtonStates();
                        break;
                }
            } else {
                System.err.println("플레이어 정보가 null입니다: index = " + currentPlayerIndex);
            }
        } else {
            System.err.println("유효하지 않은 플레이어 인덱스입니다: " + currentPlayerIndex);
        }
    }

    /**
     * 모든 플레이어의 New Piece 버튼 상태를 업데이트
     */
    public void updateAllPlayerButtonStates() {
        for (PlayerInform playerInform : allPlayerInforms) {
            if (playerInform != null) {
                playerInform.updateNewPieceButtonState();
            }
        }
        System.out.println("모든 플레이어 버튼 상태 업데이트 완료");
    }

    /**
     * 현재 하이라이트가 활성화되어 있는지 확인
     */
    public boolean isHighlightActive() {
        return boardPane != null && boardPane.hasHighlightedNodes();
    }

    /**
     * 뷰에서 지름길 규칙을 체크하여 목적지를 필터링
     * @param currentNode 현재 노드
     * @param allDestinations Controller에서 받은 모든 목적지
     * @param steps 이동 칸 수
     * @return 지름길 규칙이 적용된 목적지
     */
    private List<BoardNode> applyShortcutRulesInView(BoardNode currentNode, List<BoardNode> allDestinations, int steps) {
        String currentNodeId = currentNode.getId();

        System.out.println(">>> 뷰에서 지름길 규칙 적용: " + currentNodeId);
        System.out.println(">>> 필터링 전 목적지: " + allDestinations.size() + "개");

        // 지름길 사용 금지 위치에서는 지름길 노드 제외
        if (isShortcutForbiddenPositionInView(currentNodeId)) {
            List<BoardNode> filteredDestinations = allDestinations.stream()
                    .filter(dest -> !isShortcutNodeInView(dest.getId()))
                    .collect(Collectors.toList());

            System.out.println(">>> 지름길 필터링 적용됨 - 결과: " + filteredDestinations.size() + "개");
            for (BoardNode dest : filteredDestinations) {
                System.out.println("  -> " + dest.getId() + " (허용)");
            }
            for (BoardNode dest : allDestinations) {
                if (!filteredDestinations.contains(dest)) {
                    System.out.println("  -> " + dest.getId() + " (지름길 제외)");
                }
            }

            return filteredDestinations;
        }

        System.out.println(">>> 지름길 제한 없음 - 모든 경로 허용");
        return allDestinations;
    }

    /**
     * 지름길 사용이 금지된 위치인지 확인
     */
    private boolean isShortcutForbiddenPositionInView(String nodeId) {
        // ★ 현재 보드 타입에 따라 다르게 처리
        Set<String> forbiddenPositions = new HashSet<>();

        // 사각형 보드 (기본)
        forbiddenPositions.addAll(Set.of(
                "E1", "E2", "E3", "E4",  // 동쪽 변
                "N1", "N2", "N3", "N4",  // 북쪽 변
                "W1", "W2", "W3", "W4",  // 서쪽 변
                "S1", "S2", "S3", "S4"   // 남쪽 변
        ));

        // 오각형 보드
        forbiddenPositions.addAll(Set.of(
                "s1", "s2", "s3", "s4",    // START_NODE에서 A로 가는 변
                "A1", "A2", "A3", "A4",    // A에서 B로 가는 변
                "B1", "B2", "B3", "B4",    // B에서 C로 가는 변
                "C1", "C2", "C3", "C4",    // C에서 D로 가는 변
                "D1", "D2", "D3", "D4"     // D에서 START_NODE로 가는 변
        ));

        // 육각형 보드
        forbiddenPositions.addAll(Set.of(
                "1", "2", "3", "4", "5", "6", "7", "8",
                "9", "10", "11", "12", "13", "14", "15", "16",
                "17", "18", "19", "20", "21", "22", "23", "24"
        ));

        return forbiddenPositions.contains(nodeId);
    }

    /**
     * 지름길 노드인지 확인
     */
    private boolean isShortcutNodeInView(String nodeId) {
        // 사각형 보드 지름길
        if (nodeId.startsWith("NE") || nodeId.startsWith("NW") ||
                nodeId.startsWith("SE") || nodeId.startsWith("SW")) {
            return true;
        }

        // 오각형 보드 지름길 (c로 시작)
        if (nodeId.startsWith("c") && nodeId.length() <= 3) {
            return true;
        }

        // 육각형 보드 지름길
        Set<String> hexShortcuts = Set.of(
                "a1", "a2", "b1", "b2", "c1", "c2",
                "d1", "d2", "e1", "e2", "f1", "f2"
        );
        if (hexShortcuts.contains(nodeId)) {
            return true;
        }

        return false;
    }
}