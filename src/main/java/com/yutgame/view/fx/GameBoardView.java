package main.java.com.yutgame.view.fx;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
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

public class GameBoardView {
    private YutGameController controller;
    private BoardPane boardPane;
    private VBox playerUIRoot;

    private final Scene scene;

    public Scene scene() {
        return scene;
    }

    private boolean isRandomThrow = false;

    private final List<StackPane> allPlayers = new ArrayList<>();
    private List<StackPane> allPieces = new ArrayList<>();
    private List<VBox> playerPieces = new ArrayList<>();
    private List<ImageView> buttonImg = new ArrayList<>();
    private List<StackPane> buttonPane = new ArrayList<>();
    private List<StackPane> allResultPanes = new ArrayList<>();

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

        // 노드 그리기
        this.boardPane = new BoardPane(controller, this);

        // Players, Pieces 그리기
        this.playerUIRoot = drawPlayerInforms(playerCount, pieceCount, 0);   // isFirst == 1 : 초기 세팅, 0 : 게임 진행 중 업데이트

        // 윷 던지기 버튼 그리기
        StackPane randomThrow = throwButton("/fx/button/game/randomBtn.png", true);
        StackPane manualThrow = throwButton("/fx/button/game/selectBtn.png", false);

        HBox Buttons = new HBox(randomThrow, manualThrow);
        Buttons.setTranslateX(470);
        Buttons.setTranslateY(30);

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

        StackPane root = new StackPane(bgImage, playerUIRoot, board, boardPane, Buttons);
        this.scene = new Scene(root, 870, 570);
    }

    public BoardPane getBoardPane(){
        return boardPane;
    }

    public void updatePlayerInforms(int playerCount, int allPieceCount, int finishedPieceCount) {
        playerUIRoot.getChildren().clear(); // 기존 요소 제거
        playerPieces.clear();
        allPlayers.clear();
        allPieces.clear();
        allResultPanes.clear();
        System.out.println("playerUIRoot 초기화");

        VBox updated = drawPlayerInforms(playerCount, allPieceCount, finishedPieceCount);
        playerUIRoot.getChildren().addAll(updated.getChildren());
    }

    public VBox drawPlayerInforms(int playerCount, int AllPieceCount, int finishedPieceCount) {

        System.out.println("drawPlayerInforms");
        Player currentPlayer = controller.getCurrentPlayer();
        int currentPlayerIndex = currentPlayer.getIndex();   // 인덱스 1부터 시작
        System.out.println("currentPlayerIndex: " + currentPlayerIndex);

        // 각 캐릭터 이미지, 말 이미지, 윷 결과판 이미지 생성
        for (int i = 0; i < playerCount; i++) {
            allPlayers.add(drawPlayer(i, currentPlayerIndex));
            allPieces.add(drawOwnPieces(AllPieceCount, finishedPieceCount, i, currentPlayerIndex));
            allResultPanes.add(drawResultPane());
        }

        // 사용자와 말, 윷 결과판 수직박스로 묶기
        for (int i = 0; i < playerCount; i++) {
            if (i<=1){   // 윗 줄은 플레이어 이미지가 위로 오게
                VBox connect = new VBox(allPlayers.get(i), allPieces.get(i), allResultPanes.get(i));
                connect.setSpacing(10);
                playerPieces.add(connect);
            }else if (i>1){   // 아래 줄은 피스 이미지가 위로 오게
                VBox connect = new VBox(allResultPanes.get(i), allPieces.get(i), allPlayers.get(i));
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
        upPlayers.setTranslateY(0);

        HBox underPlayers = new HBox();
        if (playerCount == 3){
            underPlayers.getChildren().add(playerPieces.get(2));
            underPlayers.setTranslateX(35);
            underPlayers.setTranslateY(0);
        }else if (playerCount == 4){
            underPlayers.getChildren().addAll(playerPieces.get(2), playerPieces.get(3));
            underPlayers.setSpacing(500);
            upPlayers.setAlignment(Pos.CENTER);
            underPlayers.setTranslateX(35);
            underPlayers.setTranslateY(0);
        }
        VBox Players = new VBox(100, upPlayers, underPlayers);
        Players.setAlignment(Pos.CENTER);

        return Players;
    }

    private StackPane drawOwnPieces(int AllPieceCount, int finishedPieceCount, int playerNum, int currentPlayerIndex) {
        playerNum += 1;
        StackPane pieces = new StackPane();
        pieces.setPrefSize(143, 63);
        int j = 0;
        System.out.println("finished : " + finishedPieceCount);
        for (j = 0 ; j < AllPieceCount - finishedPieceCount; j++) {
            ImageView pieceImg = safeLoadImage("/fx/piece/piece_" + playerNum + ".png");
            System.out.println("piece"+ j);
            pieceImg.setFitWidth(28);
            pieceImg.setFitHeight(38);
            pieceImg.setPreserveRatio(false);
            pieceImg.setSmooth(true);

            // 중앙 정렬을 유지하면서 좌우로 퍼지게
            double offset = (j - (AllPieceCount - 1) / 2.0) * 30;
            pieceImg.setTranslateX(offset);

            pieces.getChildren().add(pieceImg);
        }
        if (currentPlayerIndex == playerNum){   // 현재 턴인 플레이어
            for (int i = j; i < j+finishedPieceCount; i++) {
                ImageView pieceImg = safeLoadImage("/fx/piece/piece_goal.png");
                System.out.println("goal piece"+ i);
                pieceImg.setFitWidth(28);
                pieceImg.setFitHeight(38);
                pieceImg.setPreserveRatio(false);
                pieceImg.setSmooth(true);

                // 중앙 정렬을 유지하면서 좌우로 퍼지게
                double offset = (i - (AllPieceCount - 1) / 2.0) * 30;
                pieceImg.setTranslateX(offset);

                pieces.getChildren().add(pieceImg);
            }
        }else {
            for (int i = j; i < finishedPieceCount; i++) {
                ImageView pieceImg = safeLoadImage("/fx/piece/piece_" + playerNum + ".png");
                System.out.println("piece"+ i);
                pieceImg.setFitWidth(28);
                pieceImg.setFitHeight(38);
                pieceImg.setPreserveRatio(false);
                pieceImg.setSmooth(true);

                // 중앙 정렬을 유지하면서 좌우로 퍼지게
                double offset = (i - (AllPieceCount - 1) / 2.0) * 30;
                pieceImg.setTranslateX(offset);

                pieces.getChildren().add(pieceImg);
            }
        }
        return pieces;
    }

    private StackPane drawPlayer(int currentPlayerCount, int currentTurnPlayerIndex){  // currentPlayerCount : 현재 그리려는 플레이어, currentTurnPlayerIndex : 현재 턴인 플레이어
        StackPane player = new StackPane();
        int playerNum = currentPlayerCount + 1;
        if (currentTurnPlayerIndex != playerNum){
            ImageView img = safeLoadImage("/fx/player/player_" + playerNum + ".png");
            System.out.println("player"+ playerNum);
            img.setFitWidth(100);
            img.setFitHeight(100);
            img.setPreserveRatio(false);
            img.setSmooth(true);
            player.getChildren().add(img);
        }else {   // 지금 그리려는 플레이어가 현재 턴인 플레이어인 경우
            ImageView img = safeLoadImage("/fx/player/player_" + playerNum + "_highlight.png");
            System.out.println("player_highlight"+ playerNum);
            img.setFitWidth(100);
            img.setFitHeight(100);
            img.setPreserveRatio(false);
            img.setSmooth(true);
            player.getChildren().add(img);
        }
        return player;
    }

    private StackPane drawResultPane (){
        ImageView pane = safeLoadImage("/fx/result/blank.png");
        pane.setFitWidth(153);
        pane.setFitHeight(38);
        pane.setPreserveRatio(false);
        pane.setSmooth(true);
        StackPane resultPane = new StackPane(pane);
        resultPane.setPrefSize(153, 38);

        return resultPane;
    }

    private StackPane throwButton (String imagePath, boolean isRandom){
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

        applyThrowSelections(results); // 아직은 뷰에 남겨둠
    }

    private void showResult(YutThrowResult result) {
        String message = switch (result) {
            case BAK_DO -> "던진 윷 결과: 백도";
            case DO     -> "던진 윷 결과: 도";
            case GAE    -> "던진 윷 결과: 개";
            case GEOL   -> "던진 윷 결과: 걸";
            case YUT    -> "던진 윷 결과: 윷";
            case MO     -> "던진 윷 결과: 모";
        };

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("윷 던지기 결과");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

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
        return sel;
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
                boardPane.drawBoard(0);
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

                if (controller.isGameOver()) {
                    break;
                }
            }
        } else {
            moveNode(currentPlayer, results.get(0));
        }

        if (controller.isGameOver()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("게임 종료");
            alert.setHeaderText(null);
            alert.setContentText("승리자: " + controller.getWinner().getName());
            alert.showAndWait();
            Platform.exit(); // JavaFX용 종료
        } else {
            controller.nextTurn();
        }
    }

    private void moveNode(Player currentPlayer, YutThrowResult chosenResult) {
        Piece selected = selectPiece(currentPlayer, chosenResult);

        if (selected != null) {
            int steps = controller.getSteps(chosenResult);

            BoardNode curr = selected.getCurrentNode();
            if (curr == null) curr = controller.getBoard().getStartNode();

            if (steps < 0) {
                List<BoardNode> prevs = controller.getBoard().getPossiblePreviousNodes(curr);
                BoardNode dest = prevs.size() == 1 ? prevs.get(0) : chooseDestination(prevs, "빽도 이동", -1);
                if (dest != null)
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

            boardPane.drawBoard(0); // JavaFX에서는 직접 만든 메서드로 redraw
        }
    }

    private Piece selectPiece(Player player, YutThrowResult chosenResult) {
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
            return null;
        } else {
            // 선택 가능한 말이 없음 → 메시지 출력 후 턴 넘김
            if (pieceDecisions.isEmpty()) {
                String msg = controller.checkBaekdo(chosenResult)
                        ? "시작지점에서 빽도를 사용하실 수 없습니다."
                        : "이 플레이어는 이동 가능한 말이 없습니다.";

                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("선택 불가");
                alert.setHeaderText(null);
                alert.setContentText(msg);
                alert.showAndWait();

                controller.nextTurn();
                boardPane.drawBoard(0); // JavaFX용 redraw
                return null;
            }

            // 실제 선택창
            ChoiceDialog<String> dialog = new ChoiceDialog<>(pieceDecisions.getFirst(), pieceDecisions);
            dialog.setTitle("말 선택");
            dialog.setHeaderText(null);
            dialog.setContentText("이동할 말을 선택하세요 (" + player.getName() + "):");

            Optional<String> result = dialog.showAndWait();
            if (result.isEmpty()) return null;

            int choiceIndex = pieceDecisions.indexOf(result.get());
            return (choiceIndex < 0 || choiceIndex >= choices.size()) ? null : choices.get(choiceIndex);
        }
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
