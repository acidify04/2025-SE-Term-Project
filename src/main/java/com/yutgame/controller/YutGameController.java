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

    /**
     * 말을 완전히 시작 상태로 되돌리는 메소드
     * @param piece 되돌릴 말
     * @return 성공 여부
     */
    private boolean resetPieceToStart(Piece piece) {
        try {
            System.out.println(">>> 말 초기화 시작: " + piece + " (소유자: " + piece.getOwner().getName() + ")");

            // 1. currentNode를 null로 설정 (새로 출발 가능한 상태)
            if (!setCurrentNodeForPieceSafely(piece, null)) {
                return false;
            }

            // 2. pathHistory 완전 초기화
            try {
                var pathHistoryField = piece.getClass().getDeclaredField("pathHistory");
                pathHistoryField.setAccessible(true);
                pathHistoryField.set(piece, new ArrayList<>());
                System.out.println(">>> pathHistory 초기화 완료");
            } catch (NoSuchFieldException e) {
                System.out.println(">>> pathHistory 필드 없음 - 건너뜀");
            }

            // 3. started 상태 초기화 (만약 해당 필드가 있다면)
            try {
                var startedField = piece.getClass().getDeclaredField("started");
                startedField.setAccessible(true);
                startedField.set(piece, false); // 시작하지 않은 상태로
                System.out.println(">>> started 상태 초기화 완료");
            } catch (NoSuchFieldException e) {
                System.out.println(">>> started 필드 없음 - 건너뜀");
            }

            // 4. finished 상태도 확인 (혹시 모를 경우)
            try {
                var finishedField = piece.getClass().getDeclaredField("finished");
                finishedField.setAccessible(true);
                finishedField.set(piece, false); // 완주하지 않은 상태로
                System.out.println(">>> finished 상태 초기화 완료");
            } catch (NoSuchFieldException e) {
                System.out.println(">>> finished 필드 없음 - 건너뜀");
            }

            System.out.println(">>> 말 완전 초기화 성공: " + piece);
            return true;

        } catch (Exception e) {
            System.err.println(">>> 말 초기화 실패: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 새 말 전용 이동 메소드 (기존 movePiece의 버그 우회)
     * @param piece 이동할 새 말 (getCurrentNode() == null)
     * @param destination 목적지 노드
     * @param steps 이동할 칸 수
     * @return 이동 성공 여부
     */
    public boolean moveNewPieceToNode(Piece piece, BoardNode destination, int steps) {
        try {
            System.out.println(">>> Controller - 새 말 전용 이동 처리");
            System.out.println(">>> 목적지: " + destination.getId() + ", 이동 칸수: " + steps);

            // 1. 새 말인지 확인
            if (piece.getCurrentNode() != null) {
                System.err.println("이미 보드에 있는 말입니다. movePiece를 사용하세요.");
                return false;
            }

            // ★ 추가: 말 잡기 & 업기 처리
            List<Piece> caughtPieces = new ArrayList<>();  // 상대방 말
            List<Piece> myPieces = new ArrayList<>();      // 본인 말 (업기용)

            for (Piece occupant : destination.getOccupantPieces()) {
                if (!occupant.getOwner().equals(piece.getOwner())) {
                    // 상대방 말 - 잡기
                    caughtPieces.add(occupant);
                    System.out.println(">>> 말 잡기 감지: " + occupant);
                } else {
                    // 본인 말 - 업기
                    myPieces.add(occupant);
                    System.out.println(">>> 본인 말 업기 감지: " + occupant);
                }
            }

            // ★ 말 잡기 처리: 상대방 말들을 시작점으로 되돌리기
            for (Piece caughtPiece : caughtPieces) {
                destination.getOccupantPieces().remove(caughtPiece);

                // ★ 수정: resetPieceToStart 메소드 사용 (완전한 초기화)
                if (!resetPieceToStart(caughtPiece)) {
                    System.err.println(">>> 말 초기화 실패: " + caughtPiece);
                } else {
                    System.out.println(">>> 말 잡기 완료: " + caughtPiece + " -> 시작점으로 이동");
                }
            }

            // ★ 본인 말 업기는 그대로 두기 (같은 노드에 여러 말 존재 허용)
            if (!myPieces.isEmpty()) {
                System.out.println(">>> 본인 말 업기 처리: " + myPieces.size() + "개 말과 함께 이동");
                // 본인 말들은 그대로 두고 새 말만 추가 (업기 효과)
            }

            // 2. 목적지 노드에 새 말 추가
            if (!addPieceToNodeSafely(piece, destination)) {
                System.err.println(">>> 목적지 노드에 말 추가 실패");
                return false;
            }

            // 3. 말의 currentNode 설정
            if (!setCurrentNodeForPieceSafely(piece, destination)) {
                System.err.println(">>> currentNode 설정 실패");
                destination.getOccupantPieces().remove(piece);
                return false;
            }

            // 4. 말의 started 상태 설정
            setPieceStartedSafely(piece, true);

            // 5. pathHistory 설정
            if (!setPathHistoryForNewPieceSafely(piece, destination, steps)) {
                System.err.println(">>> pathHistory 설정 실패");
                destination.getOccupantPieces().remove(piece);
                setCurrentNodeForPieceSafely(piece, null);
                return false;
            }

            System.out.println(">>> 새 말 이동 완료: " + destination.getId());

            // ★ 추가: 결과 정보 출력
            if (!caughtPieces.isEmpty()) {
                System.out.println(">>> 말 잡기 발생! 잡힌 말 개수: " + caughtPieces.size());
            }
            if (!myPieces.isEmpty()) {
                System.out.println(">>> 본인 말 업기 발생! 업힌 말 개수: " + myPieces.size());
            }

            return true;

        } catch (Exception e) {
            System.err.println(">>> 새 말 이동 실패: " + e.getMessage());
            e.printStackTrace();

            // 실패 시 롤백
            try {
                destination.getOccupantPieces().remove(piece);
                setCurrentNodeForPieceSafely(piece, null);
            } catch (Exception rollbackException) {
                System.err.println(">>> 롤백 실패: " + rollbackException.getMessage());
            }

            return false;
        }
    }
    /**
     * 노드에 말을 안전하게 추가 (중복 체크)
     */
    private boolean addPieceToNodeSafely(Piece piece, BoardNode node) {
        try {
            var occupantPiecesField = node.getClass().getDeclaredField("occupantPieces");
            occupantPiecesField.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<Piece> occupantPieces = (List<Piece>) occupantPiecesField.get(node);

            // ★ 중복 체크
            if (occupantPieces.contains(piece)) {
                System.out.println(">>> 말이 이미 노드에 있음 - 중복 추가 방지");
                return true; // 이미 있으면 성공으로 처리
            }

            occupantPieces.add(piece);
            System.out.println(">>> 노드에 말 추가 완료: " + node.getId());
            return true;

        } catch (Exception e) {
            System.err.println(">>> 노드에 말 추가 실패: " + e.getMessage());
            return false;
        }
    }

    /**
     * currentNode를 안전하게 설정
     */
    private boolean setCurrentNodeForPieceSafely(Piece piece, BoardNode node) {
        try {
            var currentNodeField = piece.getClass().getDeclaredField("currentNode");
            currentNodeField.setAccessible(true);
            currentNodeField.set(piece, node);
            System.out.println(">>> currentNode 설정 완료: " + (node != null ? node.getId() : "null"));
            return true;
        } catch (Exception e) {
            System.err.println(">>> currentNode 설정 실패: " + e.getMessage());
            return false;
        }
    }

    /**
     * started 상태를 안전하게 설정
     */
    private void setPieceStartedSafely(Piece piece, boolean started) {
        try {
            var startedField = piece.getClass().getDeclaredField("started");
            startedField.setAccessible(true);
            startedField.set(piece, started);
            System.out.println(">>> started 상태 설정 완료: " + started);
        } catch (NoSuchFieldException e) {
            System.out.println(">>> started 필드 없음 - 건너뜀");
        } catch (Exception e) {
            System.err.println(">>> started 필드 설정 실패: " + e.getMessage());
        }
    }

    /**
     * pathHistory를 안전하게 설정 (개선된 버전)
     */
    private boolean setPathHistoryForNewPieceSafely(Piece piece, BoardNode destination, int steps) {
        try {
            var pathHistoryField = piece.getClass().getDeclaredField("pathHistory");
            pathHistoryField.setAccessible(true);

            List<BoardNode> pathHistory = new ArrayList<>();
            BoardNode startNode = this.getBoard().getStartNode();

            System.out.println(">>> pathHistory 설정 시작 - steps: " + steps);

            // ★ 수정: 더 안전한 경로 생성
            if (steps <= 0) {
                // steps가 0 이하인 경우 (빽도 등) 간단하게 처리
                pathHistory.add(startNode);
                pathHistory.add(destination);
            } else {
                // 정상적인 전진 이동
                pathHistory.add(startNode);

                // ★ 개선: 직접 경로 계산 대신 단순하게 처리
                BoardNode currentNode = startNode;
                for (int step = 1; step < steps; step++) {
                    List<BoardNode> nextNodes = this.getBoard().getPossibleNextNodes(currentNode, 1);
                    if (!nextNodes.isEmpty()) {
                        // 첫 번째 가능한 노드 선택
                        currentNode = nextNodes.get(0);
                        pathHistory.add(currentNode);
                        System.out.println(">>> pathHistory[" + step + "]: " + currentNode.getId());
                    } else {
                        // 다음 노드가 없으면 현재 노드 유지
                        pathHistory.add(currentNode);
                    }
                }

                // 마지막에 목적지 추가
                pathHistory.add(destination);
            }

            pathHistoryField.set(piece, pathHistory);

            System.out.println(">>> pathHistory 설정 완료: " + pathHistory.size() + "개 노드");
            for (int i = 0; i < pathHistory.size(); i++) {
                System.out.println("  [" + i + "] " + pathHistory.get(i).getId());
            }

            return true;

        } catch (NoSuchFieldException e) {
            System.out.println(">>> pathHistory 필드 없음 - 건너뜀");
            return true; // 필드가 없어도 성공으로 처리
        } catch (Exception e) {
            System.err.println(">>> pathHistory 설정 실패: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 지름길 규칙이 적용된 유효한 목적지를 반환 (Model 위임)
     */
    public List<BoardNode> getValidDestinations(BoardNode currentNode, int steps) {
        // Controller는 단순히 Model에 위임 (비즈니스 로직은 Model에)
        return getBoard().getValidDestinationsWithShortcutRules(currentNode, steps);
    }

    /**
     * 말 잡기 상황인지 확인 (Controller가 판단)
     */
    public boolean checkIfPieceCaught(Piece movedPiece, BoardNode destination) {
        // Controller가 비즈니스 로직 처리
        for (Piece occupant : destination.getOccupantPieces()) {
            if (!occupant.getOwner().equals(movedPiece.getOwner())) {
                return true; // 말 잡기 상황
            }
        }
        return false;
    }

    /**
     * 업기된 말들을 함께 이동시키는 메소드
     * @param selectedPiece 선택된 말 (업기된 말들 중 하나)
     * @param destination 목적지 노드
     * @param steps 이동할 칸 수
     * @return 이동 성공 여부
     */
    public boolean moveStackedPieces(Piece selectedPiece, BoardNode destination, int steps) {
        try {
            System.out.println(">>> Controller - 업기된 말들 함께 이동 처리");

            BoardNode currentNode = selectedPiece.getCurrentNode();
            if (currentNode == null) {
                System.err.println("선택된 말이 보드에 없습니다.");
                return false;
            }

            // 1. 같은 노드에 있는 본인 말들 찾기 (업기된 말들)
            List<Piece> stackedPieces = new ArrayList<>();
            Player owner = selectedPiece.getOwner();

            for (Piece piece : currentNode.getOccupantPieces()) {
                if (piece.getOwner().equals(owner)) {
                    stackedPieces.add(piece);
                    System.out.println(">>> 함께 이동할 말 발견: " + piece);
                }
            }

            System.out.println(">>> 총 " + stackedPieces.size() + "개 말이 함께 이동");

            // 2. 목적지에서 상대방 말 잡기 처리
            List<Piece> caughtPieces = new ArrayList<>();
            for (Piece occupant : destination.getOccupantPieces()) {
                if (!occupant.getOwner().equals(owner)) {
                    caughtPieces.add(occupant);
                    System.out.println(">>> 잡힐 상대방 말: " + occupant);
                }
            }

            // 상대방 말들을 시작점으로 되돌리기
            for (Piece caughtPiece : caughtPieces) {
                destination.getOccupantPieces().remove(caughtPiece);

                // ★ 수정: resetPieceToStart 메소드 사용 (완전한 초기화)
                if (!resetPieceToStart(caughtPiece)) {
                    System.err.println(">>> 말 초기화 실패: " + caughtPiece);
                } else {
                    System.out.println(">>> 상대방 말 잡기 완료: " + caughtPiece);
                }
            }

            // 3. 모든 업기된 말들을 함께 이동
            for (Piece piece : stackedPieces) {
                // 현재 노드에서 제거
                currentNode.getOccupantPieces().remove(piece);

                // 목적지로 이동
                if (!addPieceToNodeSafely(piece, destination)) {
                    System.err.println(">>> 말 이동 실패: " + piece);
                    return false;
                }

                // currentNode 업데이트
                if (!setCurrentNodeForPieceSafely(piece, destination)) {
                    System.err.println(">>> currentNode 설정 실패: " + piece);
                    return false;
                }

                // pathHistory 업데이트
                updatePathHistoryForMove(piece, destination);

                System.out.println(">>> 말 이동 완료: " + piece + " -> " + destination.getId());
            }

            System.out.println(">>> 업기된 말들 함께 이동 완료!");
            if (!caughtPieces.isEmpty()) {
                System.out.println(">>> 상대방 말 잡기도 발생!");
            }

            return true;

        } catch (Exception e) {
            System.err.println(">>> 업기된 말들 이동 실패: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 말의 pathHistory를 업데이트
     */
    private void updatePathHistoryForMove(Piece piece, BoardNode destination) {
        try {
            var pathHistoryField = piece.getClass().getDeclaredField("pathHistory");
            pathHistoryField.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<BoardNode> pathHistory = (List<BoardNode>) pathHistoryField.get(piece);

            if (pathHistory != null) {
                pathHistory.add(destination);
                System.out.println(">>> pathHistory 업데이트 완료: " + piece);
            }
        } catch (Exception e) {
            System.err.println(">>> pathHistory 업데이트 실패: " + e.getMessage());
        }
    }
}
