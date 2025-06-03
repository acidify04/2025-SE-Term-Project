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
import javafx.stage.Stage;
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

import javafx.scene.control.ButtonType;
import javafx.scene.control.ButtonBar;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.effect.GaussianBlur;
import javafx.animation.ScaleTransition;
import javafx.animation.FadeTransition;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import java.util.Optional;


public class GameBoardView {
    private YutGameController controller;
    private BoardPane boardPane;
    private List<PlayerInform> allPlayerInforms = new ArrayList<>();

    private int boardType; // ★ 추가: 보드 타입 저장

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
        this.boardType = boardType; // ★ 추가: 보드 타입 저장

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

            // 3) 윷 던지기 (turnInProgress 설정을 여기서 하지 말고 성공 후에 설정)
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

            // ★ 수정: 윷 던지기가 성공적으로 처리된 후에만 설정
            // (빽도로 인한 턴 넘기기가 발생하지 않은 경우에만)
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

                // ★ 수정: endTurn() 메소드 호출로 완전한 턴 종료 처리
                System.out.println(">>> 빽도 사용 불가 - endTurn() 호출");
                endTurn();
                return; // 여기서 메서드 종료
            }
        }

        // ★ 수정: 정상적인 윷 던지기인 경우에만 turnInProgress 설정
        turnInProgress = true;

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

        // 1. 말 선택 (selectedPiece가 null이면 selectPiece 호출)
        Piece selected = selectedPiece;
        if (selected == null) {
            // New Piece 버튼으로 들어온 경우 - 컨트롤러를 통해 말 선택
            var pieceDecisions = controller.getPieceDecisions(currentPlayer, chosenResult);
            if (!pieceDecisions.choices().isEmpty()) {
                selected = pieceDecisions.choices().get(0); // 첫 번째는 새 말
            }
        }

        if (selected == null) {
            System.out.println(">>> 선택된 말이 없음");
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("말 선택 없음");
            alert.setHeaderText(null);
            alert.setContentText("이동할 말이 없습니다!");
            alert.showAndWait();
            return;
        }

        // 2. 이동 칸 수 계산
        int steps = controller.getSteps(chosenResult);
        System.out.println("- steps: " + steps);

        // 3. 현재 노드 확인
        BoardNode curr = selected.getCurrentNode();
        if (curr == null) {
            curr = controller.getBoard().getStartNode();
            System.out.println(">>> 새 말 - START_NODE로 설정");
        }
        System.out.println("- 현재 노드: " + curr.getId());

        try {
            if (steps < 0) {
                // 빽도 처리 - Swing과 동일한 로직
                List<BoardNode> prevs = controller.getBoard().getPossiblePreviousNodes(curr);
                System.out.println(">>> 빽도 이동 - 가능한 이전 노드: " + prevs.size() + "개");

                if (prevs.isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("이동 불가");
                    alert.setHeaderText(null);
                    alert.setContentText("뒤로 이동할 수 있는 곳이 없습니다!");
                    alert.showAndWait();

                    currentResults.remove(chosenResult);
                    clearSelectedYutResult();
                    repaint(1);
                    if (currentResults.isEmpty()) {
                        endTurn();
                    }
                    return;
                }

                if (prevs.size() == 1) {
                    // 선택지가 1개면 해당 노드만 하이라이팅 (Swing에서는 자동 이동)
                    highlightSingleDestination(prevs.get(0), chosenResult, selected, false);
                } else {
                    // 선택지가 여러개면 모두 하이라이팅
                    highlightDestinations(prevs, chosenResult, selected, false);
                }

            } else {
                // 정방향 이동 처리 - Swing과 동일한 로직
                List<BoardNode> cans = controller.getBoard().getPossibleNextNodes(curr, steps);
                System.out.println(">>> 정방향 이동 - 가능한 다음 노드: " + cans.size() + "개");

                if (cans.isEmpty()) {
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

                // 완주 가능 여부 확인
                List<BoardNode> path = controller.getBoard().getPaths();
                List<List<BoardNode>> paths = controller.splitPath(path, steps);
                int canFinishIndex = controller.checkCanFinishIndex(paths, path);
                boolean finishMode = canFinishIndex >= 0;

                // Swing과 동일한 조건: 갈림길이면서 선택지가 2개 이상일 때만 선택
                if (controller.isCrossroad(curr) && cans.size() > 1) {
                    // 실제 선택이 필요한 경우 - 모든 노드 하이라이팅
                    highlightDestinations(cans, chosenResult, selected, finishMode);
                } else {
                    // 선택지가 없거나 1개뿐인 경우 - 해당 노드만 하이라이팅
                    BoardNode dest = cans.get(0);
                    highlightSingleDestination(dest, chosenResult, selected, finishMode);
                }
            }

        } catch (Exception e) {
            System.err.println(">>> moveNode 실행 중 오류: " + e.getMessage());
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("이동 실패");
            alert.setHeaderText(null);
            alert.setContentText("말 이동 중 오류가 발생했습니다: " + e.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * 단일 목적지 노드를 하이라이팅 (Swing에서 자동 이동하는 경우)
     */
    private void highlightSingleDestination(BoardNode destination, YutThrowResult chosenResult,
                                            Piece selectedPiece, boolean finishMode) {
        System.out.println(">>> 단일 목적지 하이라이팅: " + destination.getId());

        if (boardPane != null) {
            boardPane.clearAllHighlights();

            // 단일 노드만 하이라이팅
            List<BoardNode> singleList = List.of(destination);
            boardPane.highlightNodes(singleList, clickedNode -> {
                System.out.println(">>> 단일 목적지 노드 클릭됨: " + clickedNode.getId());

                // 하이라이트 해제
                boardPane.unhighlightNodes(singleList);

                try {
                    // 컨트롤러를 통해 이동 처리
                    int steps = controller.getSteps(chosenResult);

                    if (steps < 0) {
                        // 빽도 이동
                        controller.movePiece(selectedPiece, clickedNode, controller.getContainsStartNode());
                    } else {
                        // 정방향 이동 (완주 처리 포함)
                        List<BoardNode> path = controller.getBoard().getPaths();
                        controller.isFinished(selectedPiece, clickedNode, path, steps);
                    }

                    handleMoveSuccess(chosenResult);

                } catch (Exception e) {
                    System.err.println(">>> 단일 노드 클릭 후 이동 실패: " + e.getMessage());
                    e.printStackTrace();

                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("이동 실패");
                    alert.setHeaderText(null);
                    alert.setContentText("말 이동에 실패했습니다!");
                    alert.showAndWait();
                }

            }, finishMode);
        }
    }

    /**
     * 목적지 노드들을 하이라이팅하고 사용자 선택을 기다립니다
     */
    private void highlightDestinations(List<BoardNode> destinations, YutThrowResult chosenResult,
                                       Piece selectedPiece, boolean finishMode) {
        System.out.println(">>> 목적지 하이라이팅 시작: " + destinations.size() + "개");

        if (boardPane != null) {
            boardPane.clearAllHighlights();

            boardPane.highlightNodes(destinations, clickedNode -> {
                System.out.println(">>> 목적지 노드 선택됨: " + clickedNode.getId());

                // 하이라이트 해제
                boardPane.unhighlightNodes(new ArrayList<>(destinations));

                try {
                    // 컨트롤러를 통해 이동 처리
                    int steps = controller.getSteps(chosenResult);

                    if (steps < 0) {
                        // 빽도 이동
                        controller.movePiece(selectedPiece, clickedNode, controller.getContainsStartNode());
                    } else {
                        // 정방향 이동 (완주 처리 포함)
                        List<BoardNode> path = controller.getBoard().getPaths();
                        controller.isFinished(selectedPiece, clickedNode, path, steps);
                    }

                    handleMoveSuccess(chosenResult);

                } catch (Exception e) {
                    System.err.println(">>> 노드 선택 후 이동 실패: " + e.getMessage());
                    e.printStackTrace();

                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("이동 실패");
                    alert.setHeaderText(null);
                    alert.setContentText("말 이동에 실패했습니다!");
                    alert.showAndWait();
                }

            }, finishMode);
        }
    }

    /**
     * 이동 성공 후 공통 처리
     */
    private void handleMoveSuccess(YutThrowResult chosenResult) {
        System.out.println(">>> 이동 성공 - 후처리 시작");

        // 승리 조건 확인
        checkGameEndCondition();

        // 사용된 윷 결과 제거
        currentResults.remove(chosenResult);
        clearSelectedYutResult();
        clearPieceSelectionState();

        // UI 업데이트
        repaint(1);
        repaint(2);
        if (boardPane != null) {
            boardPane.drawBoard();
        }

        // 다음 처리
        if (currentResults.isEmpty()) {
            endTurn();
        } else {
            updateAllPlayerButtonStates();
        }

        System.out.println(">>> 이동 후처리 완료");
    }

    /**
     * 게임 종료 조건을 검사합니다 (말 이동 후마다 호출)
     */
    private void checkGameEndCondition() {
        System.out.println("=== 게임 종료 조건 검사 시작 ===");

        // 모든 플레이어를 검사하여 승리자가 있는지 확인
        for (Player player : controller.getGame().getPlayers()) {
            System.out.println("플레이어 " + player.getName() + " 완주 검사 중...");

            // 해당 플레이어의 모든 말이 완주했는지 확인
            if (controller.allPiecesFinished(player)) {
                System.out.println("*** 승리자 발견: " + player.getName() + " ***");

                // 승리 조건 확정
                controller.checkWin();

                if (controller.isGameOver()) {
                    String winnerName = controller.getWinner().getName();
                    System.out.println(">>> 게임 종료 확정 - 승리자: " + winnerName);

                    // 고급 승리 화면 표시
                    Platform.runLater(() -> {
                        showSimpleWinnerScreen(winnerName);
                    });

                    return; // 승리자가 발견되면 더 이상 검사할 필요 없음
                }
            }
        }

        System.out.println("=== 게임 종료 조건 검사 완료 - 승리자 없음 ===");
    }

    private void endTurn() {
        System.out.println(">>> endTurn 호출 - 현재 플레이어: " + controller.getCurrentPlayer().getName());

        checkGameEndCondition();

        // ★ 핵심: 턴 종료 처리 강화
        turnInProgress = false; // 다음 플레이어가 윷을 던질 수 있도록
        clearSelectedYutResult(); // 선택된 윷 결과 초기화
        clearPieceSelectionState(); // 말 선택 상태 초기화
        currentResults.clear(); // 현재 결과 목록도 초기화

        // 하이라이트도 정리
        if (boardPane != null) {
            boardPane.clearAllHighlights();
        }

        // 현재 플레이어 턴 해제
        if (currentPlayerIndex >= 1 && currentPlayerIndex <= allPlayerInforms.size()) {
            PlayerInform currentPlayerInform = allPlayerInforms.get(currentPlayerIndex - 1);
            currentPlayerInform.setIsTurn(false);
        }

        // 다음 턴으로
        controller.nextTurn();
        currentPlayerIndex = controller.getCurrentPlayer().getIndex();

        // 다음 플레이어 턴 활성화
        if (currentPlayerIndex >= 1 && currentPlayerIndex <= allPlayerInforms.size()) {
            PlayerInform nextPlayerInform = allPlayerInforms.get(currentPlayerIndex - 1);
            nextPlayerInform.setIsTurn(true);
        }

        repaint(3);
        updateAllPlayerButtonStates();
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

    /**
     * 간단한 승리 화면을 표시합니다
     */
    private void showSimpleWinnerScreen(String winnerName) {
        // 새로운 Stage 생성
        Stage winStage = new Stage();
        winStage.initModality(Modality.APPLICATION_MODAL);
        winStage.initOwner((Stage) scene.getWindow());
        winStage.setTitle("게임 종료");
        winStage.setResizable(false);

        // 승리자 표시 라벨
        Label winnerLabel = new Label("승리자: " + winnerName);
        winnerLabel.setStyle(
                "-fx-font-size: 24px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: black; " +
                        "-fx-text-alignment: center;"
        );

        // 종료 버튼
        Button exitButton = new Button("게임 종료");
        exitButton.setStyle(
                "-fx-font-size: 16px; " +
                        "-fx-padding: 10 30 10 30; " +
                        "-fx-background-color: #f0f0f0; " +
                        "-fx-border-color: #cccccc; " +
                        "-fx-border-width: 1;"
        );

        exitButton.setOnAction(e -> {
            winStage.close();
            Platform.exit(); // 프로그램 종료
        });

        // 레이아웃 - 간단한 VBox
        VBox layout = new VBox(30);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(40));
        layout.setStyle("-fx-background-color: white;"); // 흰색 배경
        layout.getChildren().addAll(winnerLabel, exitButton);

        // Scene 생성
        Scene winScene = new Scene(layout, 300, 200);
        winStage.setScene(winScene);

        // 화면 표시
        winStage.show();
        winStage.centerOnScreen();
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
                // ★ 수정: 고급 승리 화면 호출
                Platform.runLater(() -> {
                    showSimpleWinnerScreen(controller.getWinner().getName());
                });
                // Platform.exit(); // 즉시 종료 제거
            }
            onPieceSelected.accept(null);
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
}