package main.java.com.yutgame.controller;

import javafx.stage.Stage;
import main.java.com.yutgame.dto.PieceDecisionResult;
import main.java.com.yutgame.model.*;
import main.java.com.yutgame.view.YutGameView;
import main.java.com.yutgame.view.fx.FXAppLauncher;
import main.java.com.yutgame.view.fx.FXYutGameView;
import main.java.com.yutgame.view.fx.router.ViewRouter;
import main.java.com.yutgame.view.swing.SwingYutGameView;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static main.java.com.yutgame.model.YutThrowResult.*;

public class YutGameController {
    private YutGame game;
    private YutGameView view;

    public YutGameController() {
        this.game = new YutGame();
        // SwingView 선택 시
        // this.view = new SwingYutGameView();
        // view.setController(this);

        // javaFX 선택 시
        launch();
    }

    public void launch() {
        FXAppLauncher.launchApp(this);   // 여기서 JavaFX UI 실행
    }

    // YutGameController.java
    public void initializeGame() {
        view.setController(this);

        int players = view.getPlayerCount();
        int pieces  = view.getPieceCount();
        int board   = view.getBoardChoice();

        this.game = createGame(players, pieces, board);
        view.initBoardPanel();
        view.setVisibleBoard(true);
    }

    /**
     * YutGame 초기화
     */
    public static YutGame createGame(int playerCount, int pieceCount, int boardChoice) {
        List<Player> players = new ArrayList<>();
        for (int i = 1; i <= playerCount; i++) {
            Player player = new Player("P" + i, new ArrayList<>(), i);
            for (int j = 0; j < pieceCount; j++) {
                Piece piece = new Piece(player);
                player.getPieces().add(piece);
            }
            players.add(player);
        }

        YutBoard board = switch (boardChoice) {
            case 0 -> SquareBoard.createStandardBoard();
            case 1 -> PentagonBoard.createPentagonBoard();
            case 2 -> HexagonBoard.createHexagonBoard();
            default -> throw new IllegalArgumentException("보드 선택이 잘못되었습니다.");
        };

        YutGame game = new YutGame();
        game.setBoard(board);
        game.setPlayers(players);

        return game;
    }

    public YutGame getGame() {
        return game;
    }

    public YutGameView getView() {
        return view;
    }

    public void setGame(YutGame game) {
        this.game = game;
    }

    public void setView(YutGameView view) {
        this.view = view;
    }

    // 윷·모가 나올 때까지 계속 던지고, 최종 결과 리스트를 반환
    public List<YutThrowResult> collectThrowResults(
            YutThrowResult firstResult,
            boolean isRandom,
            Supplier<YutThrowResult> manualThrowProvider,
            Consumer<YutThrowResult> resultDisplayer,
            Runnable promptExtraThrow
    ) {
        List<YutThrowResult> results = new ArrayList<>();
        resultDisplayer.accept(firstResult);
        results.add(firstResult);

        game.collectResults(firstResult, isRandom, manualThrowProvider, resultDisplayer, promptExtraThrow, results);

        return results;
    }

    // Game 관련 getter
    public Player getCurrentPlayer() {
        return game.getCurrentPlayer();
    }

    public YutBoard getBoard() {
        return game.getBoard();
    }

    public List<Player> getPlayers() {
        return game.getPlayers();
    }

    public boolean isGameOver() {
        return game.isGameOver();
    }

    public Player getWinner() {
        return game.getWinner();
    }

    // Game 관련 setter
    public void throwYutManual(YutThrowResult manualResult) {
        game.setLastThrowResult(manualResult);
    }

    public YutThrowResult getRandomYut() {
        return game.throwYutRandom();
    }

    public YutThrowResult getSetYut(int choice) {
        YutThrowResult sel = switch (choice) { // TODO: model로 이동
            case 0 -> YutThrowResult.BAK_DO;
            case 1 -> YutThrowResult.DO;
            case 2 -> GAE;
            case 3 -> GEOL;
            case 4 -> YUT;
            case 5 -> YutThrowResult.MO;
            default -> YutThrowResult.DO;
        };
        throwYutManual(sel);
        return sel;
    }

    public void nextTurn() {
        game.nextTurn();
        view.repaintBoard();
    }

    public void movePiece(Piece piece, BoardNode targetNode, boolean containsStart) {
        game.movePiece(piece, targetNode, containsStart);
    }

    public void checkWin() {
        game.checkWinCondition();
    }

    /**
     * 게임 시작 전, 말이 보드에 놓여있지 않은지 확인
     * player가 아직 출발은 안했는지 확인
     * @param : player: 확인할 플레이어
     * @return true: 출발 안함, false: 출발함
     * */
    public boolean getNotStarted(Player player) {
        return player.getPieces().stream()
                .allMatch(p -> p.getCurrentNode() == null);
    }

    /**
     * 영어를 한글로 변환
     * @param : results: 변환할 결과 리스트
     * @return : 한글로 바뀐 결과 String 리스트
     * */
    public String[] getChoiceLetters(List<YutThrowResult> results) {
        String[] options = new String[results.size()];
        for (int i = 0; i < results.size(); i++) {
            if (results.get(i) == BAK_DO) {
                options[i] = "백도";
            } else if (results.get(i) == DO) {
                options[i] = "도";
            } else if (results.get(i) == GAE) {
                options[i] = "개";
            } else if (results.get(i) == GEOL) {
                options[i] = "걸";
            } else if (results.get(i) == YUT) {
                options[i] = "윷";
            } else if (results.get(i) == MO) {
                options[i] = "모";
            }
        }
        return options;
    }

    // 어떤 말 움직인지에 대한 선택지 스트링 리스트를 반환
    public PieceDecisionResult getPieceDecisions(Player player, YutThrowResult chosenResult) {
        List<Piece> allPieces = player.getPieces(); //
        boolean isBakdo = checkBaekdo(chosenResult);
        BoardNode start = this.getBoard().getStartNode();

        // 선택지 리스트(말 객체)
        List<Piece>  choices = new ArrayList<>();

        // 선택지 리스트(스트링)
        List<String> decisions = new ArrayList<>();

        // 미출발 말 1개(빽도 제외)
        if (!isBakdo) {
            for (Piece p : allPieces) {
                if (!p.isFinished() && p.getCurrentNode() == null) {
                    decisions.add("새로운 말");
                    choices.add(p);
                    break;                       // 하나만
                }
            }
        }

        // 미출발 말 2개(빽도 제외)
        for (int i = 0; i < allPieces.size(); i++) {
            for (int j = i + 1; j < allPieces.size(); j++) {
                BoardNode curr = allPieces.get(i).getCurrentNode();
                if (curr != null && allPieces.get(j).getCurrentNode() != null) {
                    if (allPieces.get(j).getCurrentNode().equals(curr)) {
                        allPieces.remove(allPieces.get(j));
                    }
                }
            }
        }

        // 보드 위 말들
        for (Piece p : allPieces) {
            if (p.isFinished()) continue;
            BoardNode node = p.getCurrentNode();
            if (node == null) continue;

            if (isBakdo) {
                boolean canBack = !node.equals(start) && p.getPathHistory().size() >= 2;
                if (!canBack) continue;          // 뒤로 못 가면 제외
            }
            decisions.add("말 (" + node.getId() + ")");
            choices.add(p);
        }

        return new PieceDecisionResult(decisions, choices);
    }

    public boolean checkBaekdo(YutThrowResult chosenResult) {
        return chosenResult == YutThrowResult.BAK_DO;
    }

    public boolean allPiecesFinished(Player player) {
        return player.allPiecesFinished();
    }


    public List<List<BoardNode>> splitPath(List<BoardNode> path, int step) {
        List<List<BoardNode>> chunks = new ArrayList<>();
        for (int i = 0; i < path.size(); i += step) {
            int end = Math.min(i + step, path.size());
            chunks.add(new ArrayList<>(path.subList(i, end)));
        }
        return chunks;
    }

    public boolean isCrossroad(BoardNode node) {
        return game.isCrossroad(node);
    }


    // 완주 처리 관련 로직
    public void isFinished(Piece selected, BoardNode dest, List<BoardNode> path, int steps) {
        game.isFinished(selected, dest, path, steps);
    }

    public int getSteps(YutThrowResult chosenResult) {
        int steps = switch (chosenResult) {
            case BAK_DO -> -1;
            case DO      -> 1;
            case GAE     -> 2;
            case GEOL    -> 3;
            case YUT     -> 4;
            case MO      -> 5;
        };
        return steps;
    }

    public int checkCanFinishIndex(List<List<BoardNode>> paths, List<BoardNode> path) {
        return game.checkCanFinishIndex(paths, path);
    }

    public boolean getContainsStartNode() {
        return game.getContainStartNode();
    }
}
