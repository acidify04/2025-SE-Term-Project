package main.java.com.yutgame.view.fx;

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
import javafx.util.Duration;
import main.java.com.yutgame.controller.YutGameController;
import main.java.com.yutgame.dto.PieceDecisionResult;
import main.java.com.yutgame.model.BoardNode;
import main.java.com.yutgame.model.Piece;
import main.java.com.yutgame.model.Player;
import main.java.com.yutgame.model.YutThrowResult;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.function.Consumer;

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

    private StackPane drawthrowButton (String imagePath, boolean isRandom){
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
            focus(index);
            if (isRandom) {
                YutThrowResult selected = controller.getRandomYut();
                processAllThrows(selected);
            }else{
                // 지정 윷 던지기
                // TODO 선택 창 만들기
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

    /**
     * 윷·모가 나올 때까지 계속 던지고, 최종 결과 리스트를 반환 (컨트롤러 이용)
     */
    private void processAllThrows(YutThrowResult firstResult) {
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
        // 현재 플레이어 인덱스를 기반으로 PlayerInform 갱신
        repaint(1);

        // 선택 적용 (예: 말 선택)
        applyThrowSelections(results);
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

    private void applyThrowSelections(List<YutThrowResult> results) {
        Player currentPlayer = controller.getCurrentPlayer();

        // 빽도 단독일 경우, 아직 출발 안한 말만 있을 때 턴 넘김
        if (results.size() == 1 && results.getFirst() == YutThrowResult.BAK_DO) {
            boolean notStarted = controller.getNotStarted(currentPlayer);

            if (notStarted) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("알림");
                alert.setHeaderText(null);
                alert.setContentText("출발하지 않은 상태에서는 빽도를 사용할 수 없습니다. 턴을 넘깁니다.");
                alert.showAndWait();

                controller.nextTurn();
                // 기존 highlight 지우기
                if (currentPlayerIndex > 0 && currentPlayerIndex <= allPlayerInforms.size()) {
                    PlayerInform playerInform = allPlayerInforms.get(currentPlayerIndex - 1);
                    playerInform.setIsTurn(false);
                }
                currentPlayerIndex = controller.getCurrentPlayer().getIndex();
                currentResults.clear();
                System.out.println("출발하지 않은 상태에서 턴 넘기기 nextTurn");
                repaint(3);
                System.out.println("플레이어 바뀜" + currentPlayerIndex);
                return;
            }
        }

        if (results.size() > 1) {
            while (!results.isEmpty()) {
                // 옵션 리스트 생성
                String[] options = controller.getChoiceLetters(results);
                List<String> optionList = Arrays.asList(options);

                ChoiceDialog<String> dialog = new ChoiceDialog<>(options[0], optionList);
                dialog.setTitle("이동 선택");
                dialog.setHeaderText(null);
                dialog.setContentText("몇 칸 이동하시겠습니까?");
                Optional<String> result = dialog.showAndWait();

                if (result.isEmpty()) continue;

                String choiceStr = result.get();
                int choiceIndex = optionList.indexOf(choiceStr);
                YutThrowResult chosen = results.get(choiceIndex);

                boolean notStarted = controller.getNotStarted(currentPlayer);
                if (chosen == YutThrowResult.BAK_DO && notStarted) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("경고");
                    alert.setHeaderText(null);
                    alert.setContentText("출발하지 않은 상태에서는 빽도를 선택할 수 없습니다.");
                    alert.showAndWait();
                    continue;
                }

                YutThrowResult chosenResult = results.remove(choiceIndex);
                moveNode(currentPlayer, chosenResult);

                // 현재 선택한 결과 인덱스를 기반으로 PlayerInform 갱신
                repaint(1);

                if (controller.isGameOver()) {
                    break;
                }
            }
        } else {
            moveNode(currentPlayer, results.get(0));
        }
    }

    private void goNext(){
        if (controller.isGameOver()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("게임 종료");
            alert.setHeaderText(null);
            alert.setContentText("승리자: " + controller.getWinner().getName());
            alert.showAndWait();
            Platform.exit(); // JavaFX용 종료
        } else {
            System.out.println("goNext()에서 nextTurn");
            controller.nextTurn();
            // 기존 highlight 지우기
            if (currentPlayerIndex > 0 && currentPlayerIndex <= allPlayerInforms.size()) {
                PlayerInform playerInform = allPlayerInforms.get(currentPlayerIndex - 1);
                playerInform.setIsTurn(false);
            }
            currentPlayerIndex = controller.getCurrentPlayer().getIndex();
            currentResults.clear();
            repaint(3);
            System.out.println("플레이어 바뀜" + currentPlayerIndex);
        }
    }

    private void moveNode(Player currentPlayer, YutThrowResult chosenResult) {
        System.out.println("moveNode 시작");

        selectPiece(currentPlayer, chosenResult, selected -> {
            if (selected == null) {
                System.out.println("selected is null");
                return;
            }
            System.out.println("getSteps 시작");
            int steps = controller.getSteps(chosenResult);
            BoardNode curr = selected.getCurrentNode();
            if (curr == null) curr = controller.getBoard().getStartNode();

            if (steps < 0) {
                List<BoardNode> prevs = controller.getBoard().getPossiblePreviousNodes(curr);
                BoardNode dest = prevs.size() == 1 ? prevs.get(0) : chooseDestination(prevs, "빽도 이동", -1);
                if (dest != null)
                    System.out.println("movePiece 시작");
                    controller.movePiece(selected, dest, controller.getContainsStartNode());
            } else {
                List<BoardNode> cans = controller.getBoard().getPossibleNextNodes(curr, steps);
                List<BoardNode> path = controller.getBoard().getPaths();
                List<List<BoardNode>> paths = controller.splitPath(path, steps);

                int canFinishIndex = controller.checkCanFinishIndex(paths, path);

                BoardNode dest;
                if (controller.isCrossroad(curr) && cans.size() > 1) {
                    dest = chooseDestination(cans, "갈림길 선택", canFinishIndex);
                } else {
                    dest = cans.isEmpty() ? null : cans.get(0);
                }

                if (dest != null) {
                    controller.isFinished(selected, dest, path, steps);
                }
            }
            currentResults.remove(chosenResult);
            repaint(1);
            repaint(2);
            boardPane.drawBoard();

            if (currentResults.size() == 0) {
                goNext();
            }
        });
    }

    /**
     * new Piece 버튼 클릭 시 실행
     */
    public void onNewPieceButtonClicked() {
        if (waitingPieceChoices != null && !waitingPieceChoices.isEmpty() && pieceSelectedCallback != null) {
            pieceSelectedCallback.accept(waitingPieceChoices.get(0));
            clearPieceSelectionState();
        }
    }

    /**
     * 노드 위의 말 선택 시 실행
     */
    public void onPieceClicked(Piece clickedPiece) {
        if (waitingPieceChoices != null && pieceSelectedCallback != null &&
                waitingPieceChoices.contains(clickedPiece)) {

            pieceSelectedCallback.accept(clickedPiece);
            clearPieceSelectionState();
        }
    }

    private void clearPieceSelectionState() {
        waitingPieceChoices = null;
        pieceSelectedCallback = null;
    }

    public void selectPiece(Player player, YutThrowResult chosenResult, Consumer<Piece> onPieceSelected) {
        PieceDecisionResult pieceDecisionResult = controller.getPieceDecisions(player, chosenResult);
        List<Piece> choices = pieceDecisionResult.choices();
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
        this.pieceSelectedCallback = onPieceSelected;
    }


    private BoardNode chooseDestination(List<BoardNode> cands, String title, int finishIndex) {
        if (cands.size() == 1) return cands.getFirst();

        List<String> options = new ArrayList<>();
        for (int i = 0; i < cands.size(); i++) {
            String name = cands.get(i).getId();
            if (i == finishIndex) name += " (Finish)";
            options.add(name);
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>(options.get(0), options);
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.setContentText("이동할 노드를 선택하세요:");

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) return null;

        int choiceIndex = options.indexOf(result.get());
        return (choiceIndex < 0 || choiceIndex >= cands.size()) ? null : cands.get(choiceIndex);
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

    private void repaint(int change){    // 1 : 결과판 2 : 모든 플레이어의 피스, 3 : 플레이어
        System.out.println("현재 플레이어 인덱스 : " + currentPlayerIndex);
        if (currentPlayerIndex > 0 && currentPlayerIndex <= allPlayerInforms.size()) {
            PlayerInform playerInform = allPlayerInforms.get(currentPlayerIndex - 1);
            if (playerInform != null) {
                switch (change){
                    case 1:
                        playerInform.setYutResults(currentResults);
                        break;
                    case 2:
                        System.out.println("setNonStartPieceNum");
                        for (int i=0; i < allPlayerInforms.size(); i++){
                            Player player = controller.getGame().getPlayers().get(i);
                            int nonStartPieceNum = player.getNonStartPiecesNum();
                            PlayerInform eachPlayer = allPlayerInforms.get(i);
                            eachPlayer.setNonStartPieceNum(nonStartPieceNum);
                        }
                        break;
                    case 3:
                        System.out.println("턴 넘어감 setIsTurn");
                        playerInform.setIsTurn(true);
                        break;
                }
            } else {
                System.err.println("플레이어 정보가 null입니다: index = " + currentPlayerIndex);
            }
        } else {
            System.err.println("유효하지 않은 플레이어 인덱스입니다: " + currentPlayerIndex);
        }
    }
}
