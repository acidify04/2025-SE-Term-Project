package main.java.com.yutgame.model;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 윷놀이 전체 로직(게임 시작, 말 이동, 윷 던지기, 턴/승리 관리 등)을 담당하는 클래스.
 * - 윷/모면 한 번 더
 * - 상대 말 잡으면 한 번 더
 * - 갈림길 선택: board.getPossibleNextNodes() 결과 중 하나를 선택(지금은 임시 로직)
 * - 승리 판정
 */
public class YutGame {

    private YutBoard board;               // 윷놀이판
    private List<Player> players;      // 플레이어 목록
    private int currentPlayerIndex;    // 현재 턴의 플레이어
    private boolean isGameOver;        // 게임 종료 여부
    private Player winner;             // 승리한 플레이어
    private YutThrowResult lastThrowResult; // 최근 윷 결과

    private boolean extraTurnFlag;     // "잡으면 한 번 더" 발생 플래그
    private boolean containsStartNode;
    private Random random;

    public YutGame() {
        this.currentPlayerIndex = 0;
        this.isGameOver = false;
        this.winner = null;
        this.lastThrowResult = null;
        this.extraTurnFlag = false;
        this.random = new Random();
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public void setBoard(YutBoard board) {
        this.board = board;
    }

    public void setLastThrowResult(YutThrowResult lastThrowResult) {
        this.lastThrowResult = lastThrowResult;
    }

    public YutThrowResult getLastThrowResult() {
        return lastThrowResult;
    }

    /**
     * 게임 시작 시 초기화(필요하다면).
     */
    public void startGame() {
        // 말이 출발 노드에 아직 올라가지 않았거나, 향후 초기화 로직 가능
        this.isGameOver = false;
        this.currentPlayerIndex = 0;
        this.winner = null;
        this.lastThrowResult = null;
        this.extraTurnFlag = false;
    }

    /**
     * 무작위 윷 던지기.
     */
    public YutThrowResult throwYutRandom() {
        int r = random.nextInt(16);
        YutThrowResult result;
        if (r == 0) {
            result = YutThrowResult.BAK_DO;
        } else if (r == 1 || r == 2 || r == 3) {
            result = YutThrowResult.DO;
        } else if (r == 4 || r == 5 || r == 6 || r == 7) {
            result = YutThrowResult.GEOL;
        } else if (r == 8) {
            result = YutThrowResult.YUT;
        } else if (r == 9) {
            result = YutThrowResult.MO;
        } else {
            result = YutThrowResult.GAE;
        }
        this.lastThrowResult = result;
        return result;
    }

    /**
     * 지정 윷 던지기 (테스트/디버그용).
     */


    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    public boolean isGameOver() {
        return isGameOver;
    }

    public Player getWinner() {
        return winner;
    }

    /** 외부에서 extraTurnFlag 확인용 */
    public boolean hasExtraTurnFlag() {
        return extraTurnFlag;
    }

    public List<YutThrowResult> collectResults(
            YutThrowResult firstResult,
            boolean isRandom,
            Supplier<YutThrowResult> manualThrowProvider,
            Consumer<YutThrowResult> resultDisplayer,
            Runnable promptExtraThrow,
            List<YutThrowResult> results
    ) {
        int extraThrows = 0;

        // 첫 결과 포함
        if (firstResult == YutThrowResult.YUT || firstResult == YutThrowResult.MO)
            extraThrows++;
        if (extraTurnFlag)  // 잡았으면 true임
            extraThrows++;

        extraTurnFlag = false;  // 초기화 중요

        while (extraThrows-- > 0) {
            promptExtraThrow.run();
            YutThrowResult next = isRandom ? throwYutRandom() : manualThrowProvider.get();
            resultDisplayer.accept(next);
            results.add(next);

            // 던진 결과가 또 윷/모거나 잡았으면 추가
            if (next == YutThrowResult.YUT || next == YutThrowResult.MO)
                extraThrows++;
            if (extraTurnFlag)
                extraThrows++;

            extraTurnFlag = false;  // 한 번만 적용되도록
        }

        return results;
    }

    /**
     * 말 이동 로직
     * ------------------------------------------------------------
     * ▸ 전진 계열(DO·GAE·GEOL·YUT·MO)
     *   · prevNode→targetNode 최단 경로(BFS) 중간 칸을 모두 기록
     *   · 실제 이동 후 잡기·업기·골인 처리
     *
     * ▸ 빽도(BAK_DO = -1칸)
     *   · Piece.moveBackOneStep() 으로 히스토리 pop + 한 칸 뒤 이동
     *   · 뒤로 간 자리에서 잡기 / 업기 / 골인 여부만 체크
     *   · 추가 이동 경로 기록은 하지 않는다
     */
    public void movePiece(Piece piece, BoardNode targetNode, boolean containsStart) {
        if (piece == null) {
            System.err.println("movePiece Error: piece is null.");
            return;
        }

        /* ──────────────────── ❶ 빽도(BAK_DO) 전용 처리 ──────────────────── */
        if (lastThrowResult == YutThrowResult.BAK_DO) {

            /* 1) 현재 말이 START_NODE에 있거나 뒤로 갈 히스토리가 없으면 → 오류 메시지 */
            if (!canMoveBack(piece)) {
                System.out.println("시작지점에서 빽도를 사용하실 수 없습니다.");

                // 1-A) 같은 팀 말 중 빽도 가능한 말이 하나라도 있으면: 턴 유지(선택 다시)
                boolean hasOther = false;
                for (Piece p : piece.getOwner().getPieces()) {
                    if (p != piece && canMoveBack(p)) { hasOther = true; break; }
                }

                // 1-B) 아무 말도 빽도 불가 → 턴 자동 패스
                if (!hasOther) {
                    extraTurnFlag = false;      // 추가 턴 무효
                    nextTurn();                 // 즉시 다음 플레이어에게 넘김
                }
                return;                         // 더 이상 처리하지 않음
            }

            /* 2) 정상적인 빽도 처리 */
            moveGroupBackOneStep(piece);
            BoardNode newNode = piece.getCurrentNode();

            // ② 잡기 / 업기 / 골인 체크 (전진과 동일 로직 재사용)
            boolean didCapture = captureIfNeeded(newNode, piece.getOwner());
            groupIfSameTeam(newNode, piece.getOwner(), piece);
            if (isGoal(piece, null, newNode, containsStart)) {   // prevNode 필요 X
                piece.setFinished(true);
                newNode.removePiece(piece);
                if (piece.isGroup()) {
                    finishGroup(piece, newNode);
                }
            }
            if (didCapture) extraTurnFlag = true;

            checkWinCondition();
            System.out.println("[DEBUG] === movePiece end (BAK_DO) ===");
            return;   // 빽도 처리 종료
        }

        /* ──────────────── ❷ 전진 계열(DO/GAE/GEOL/YUT/MO) ──────────────── */
        if (targetNode == null) {
            System.err.println("movePiece Error: targetNode is null.");
            return;
        }

        BoardNode prevNode = piece.getCurrentNode();
        if (prevNode == null) prevNode = board.getStartNode();   // 미출발 말

        // 1) prevNode → targetNode 최단 경로를 BFS 로 계산해 전부 기록
        List<BoardNode> fullPath = findShortestPath(prevNode, targetNode);
        for (BoardNode node : fullPath) {
            piece.recordNode(node);   // 중복 자동 방지 + 실시간 디버그
        }

        // 2) 실제 이동
        piece.moveTo(targetNode);

        // 3) 그룹된 말 동시 이동
        List<Piece> movedGroup = new ArrayList<>();
        moveGroupWith(piece, prevNode, targetNode, movedGroup);

        // 4) 잡기 / 업기 / 골인
        boolean didCapture = false;
        if (!isGoal(piece, prevNode, targetNode, containsStart)) {
            didCapture = captureIfNeeded(targetNode, piece.getOwner());
        }
        groupIfSameTeam(targetNode, piece.getOwner(), piece);

        if (isGoal(piece, prevNode, targetNode, containsStart)) {
            piece.setFinished(true);
            targetNode.removePiece(piece);
            if (piece.isGroup()) {
                finishGroup(piece, targetNode);
            }
        }
        if (didCapture) extraTurnFlag = true;

        checkWinCondition();

        // 디버그용 출력
        piece.getOwner().printAllPathHistories();

        // ─ 디버그용 종결선
        System.out.println("[DEBUG] === movePiece end ===");
    }

    /*─────────────────────────────────────────────────────────*/

    /** BFS : 두 노드 사이의 최단(칸 수) 경로 반환 */
    private List<BoardNode> findShortestPath(BoardNode start, BoardNode end) {
        List<BoardNode> path = new ArrayList<>();
        if (start == null || end == null) return path;

        Map<BoardNode, BoardNode> parent = new HashMap<>();
        Queue<BoardNode> q = new ArrayDeque<>();
        q.add(start);
        parent.put(start, null);

        while (!q.isEmpty()) {
            BoardNode cur = q.poll();
            if (cur.equals(end)) break;
            for (BoardNode nxt : cur.getNextNodes()) {
                if (!parent.containsKey(nxt)) {
                    parent.put(nxt, cur);
                    q.add(nxt);
                }
            }
        }

        if (!parent.containsKey(end)) {         // 경로 없음
            path.add(start);
            return path;
        }
        for (BoardNode n = end; n != null; n = parent.get(n)) {
            path.add(0, n);                      // 역추적
        }
        return path;
    }

    private void finishGroup(Piece piece, BoardNode targetNode) {
        List<Piece> queue = new ArrayList<>();
        List<Piece> visited = new ArrayList<>();

        queue.add(piece);

        while (!queue.isEmpty()) {
            Piece current = queue.remove(0);
            if (visited.contains(current)) continue;

            current.setFinished(true);
            targetNode.removePiece(current);
            visited.add(current);

            // 연결된 그룹 말들을 큐에 추가
            for (Piece grouped : current.getGroupedPieces()) {
                if (!visited.contains(grouped)) {
                    queue.add(grouped);
                }
            }
        }
    }

    // 골인 판정 헬퍼 메소드 예시 (구체적인 구현 필요)
    private boolean isGoal(Piece piece, BoardNode prevNode, BoardNode targetNode, boolean containsStart) {
        if ((targetNode.getId().contains("START_NODE") && prevNode != null) || containsStart == true) {
            return true;
        }
        return false;
    }

    /**
     * 상대 말 잡기 -> 출발 노드로 이동. 하나라도 잡으면 true 반환.
     */
    private boolean captureIfNeeded(BoardNode node, Player currentOwner) {
        List<Piece> toCapture = new ArrayList<>();
        for (Piece occupant : node.getOccupantPieces()) {
            if (!occupant.getOwner().equals(currentOwner)) {
                toCapture.add(occupant);
            }
        }
        if (toCapture.isEmpty()) {
            return false;
        }

        for (Piece captured : toCapture) {
            // 잡힌 말은 보드에서 제거
            captured.getCurrentNode().removePiece(captured);
            captured.ungroup();                        // 그룹 해제 (업기 상태 제거)
            // 출발 전 상태로 되돌림
            captured.moveTo(null);                     // moveTo(null)은 아래처럼 만들어야 함
        }
        return true;
    }

    /**
     * 아군 말 있으면 groupWith.
     * 여기서는 "첫 번째 아군 말"과 업기만 예시.
     */
    private void groupIfSameTeam(BoardNode node, Player currentOwner, Piece movingPiece) {
        for (Piece occupant : node.getOccupantPieces()) {
            if (!occupant.equals(movingPiece) && occupant.getOwner().equals(currentOwner)) {
                movingPiece.groupWith(occupant);
                break;
            }
        }
    }

    /**
     * 턴 진행:
     * - 윷/모: 연속 턴
     * - 잡으면 한 번 더(extraTurnFlag=true)도 연속 턴
     * - 그 외(bakdo/do/gae/geol) -> 다음 플레이어
     */
    public void nextTurn() {
        if (lastThrowResult == YutThrowResult.YUT
                || lastThrowResult == YutThrowResult.MO
                || extraTurnFlag) {
            // 추가 턴 유지
        } else {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
            System.out.println("[DEBUG] === nextTurn end ===");
            System.out.println("현재 턴 : " + currentPlayerIndex);
        }
        extraTurnFlag = false;
    }

    /**
     * 모든 말이 골인했는지 확인 -> 승리자 설정 -> 게임 종료
     */
    public void checkWinCondition() {
        for (Player p : players) {
            if (p.allPiecesFinished()) {
                this.isGameOver = true;
                this.winner = p;
                p.setWinner(true);
                break;
            }
        }
    }

    /**
     * 외부에서 플레이어 목록을 가져갈 때 사용
     */
    public List<Player> getPlayers() {
        return players;
    }

    /**
     * 외부에서 보드 객체를 가져갈 때 사용
     */
    public YutBoard getBoard() {
        return this.board;
    }


    // 말 그룹 이동
    private void moveGroupWith(Piece piece, BoardNode prevNode, BoardNode targetNode, List<Piece> moved) {
        if (moved.contains(piece)) return;

        // 빽도 + 시작점 도달 시 -> 먼저 미출발 처리
        if (targetNode.equals(board.getStartNode()) &&
                lastThrowResult == YutThrowResult.BAK_DO) {
            piece.moveTo(null);
            moved.add(piece);
            // return 하지 말고, grouped 말도 계속 처리!
        } else {
            piece.moveTo(targetNode);
            for (BoardNode node : findShortestPath(piece.getCurrentNode(), targetNode)) {
                piece.recordNode(node); // 경로 기록 추가
            }
            moved.add(piece);
        }

        // 무조건 grouped 말은 재귀 처리
        for (Piece child : piece.getGroupedPieces()) {
            moveGroupWith(child, prevNode, targetNode, moved);
        }
    }

    /** 현재 Piece가 빽도로 한 칸 뒤로 갈 수 있는지 검사 */
    private boolean canMoveBack(Piece p) {
        return p != null
                && p.getCurrentNode() != null
                && !p.getCurrentNode().equals(board.getStartNode())   // 이미 START면 불가
                && p.getPathHistory().size() >= 2;                    // 직전 노드가 존재해야 함
    }

    private void moveGroupBackOneStep(Piece piece) {
        piece.moveBackOneStep();
    }



    public boolean isCrossroad(BoardNode node) {
        String id = node.getId();
        return "CENTER".equals(id) || "A".equals(id) || "B".equals(id) || "C".equals(id) || "D".equals(id) || "E".equals(id);
    }


    // 완주 처리 관련 로직
    public void isFinished(Piece selected, BoardNode dest, List<BoardNode> path, int steps) {
        String destId = dest.getId();  // 선택된 목적지 노드의 ID
        int destIndex = -1;

        for (int i = 0; i < path.size(); i++) {
            if (destId.equals(path.get(i).getId())) {
                destIndex = i;
                break;
            }
        }
        if (destIndex >= 0 && destIndex == steps -1) {   // 갈림길 1 선택
            List<BoardNode> trimmed = new ArrayList<>(path.subList(0, steps));
            path.clear();
            path.addAll(trimmed);
        } else if (destIndex >= 0 && destIndex > steps -1) {  // 이외의 갈림길 선택
            List<BoardNode> trimmed = new ArrayList<>(path.subList(destIndex - steps + 1, destIndex + 1));
            path.clear();
            path.addAll(trimmed);
        } else {
            System.err.println("dest가 path에 없거나 steps 길이가 부족함.");
        }

        this.containsStartNode = checkContainsStartNode(path);
        this.getBoard().pathClear();

        this.movePiece(selected, dest, containsStartNode);
    }

    public boolean checkContainsStartNode(List<BoardNode> path) {
        return this.containsStartNode = path.stream()
                .anyMatch(node -> "START_NODE".equals(node.getId()));
    }

    public boolean getContainStartNode() {
        return this.containsStartNode;
    }

    public int checkCanFinishIndex(List<List<BoardNode>> paths, List<BoardNode> path) {
        int canFinishIndex = -1; // 완주 가능한 버튼 index
        for (int i = 0; i < paths.size(); i++) {
            for (BoardNode boardNode : path) {
                if (boardNode.getId().equals("START_NODE")) {
                    canFinishIndex = i;
                }
            }
        }
        return canFinishIndex;
    }

    /**
     * 필요 시 게임 재시작 로직(말 위치 초기화 등) 구현 가능
     */
    public void resetGame() {
        // 예: 각 말 currentNode=null, finished=false
        // BoardNode start = board.getStartNode();
        for (Player p : players) {
            p.setWinner(false);
            for (Piece piece : p.getPieces()) {
                piece.setFinished(false);
                piece.ungroup();
                if (piece.getCurrentNode() != null) {
                    piece.getCurrentNode().removePiece(piece);
                }
                piece.moveTo(board.getStartNode());
            }
        }
        this.currentPlayerIndex = 0;
        this.isGameOver = false;
        this.winner = null;
        this.lastThrowResult = null;
        this.extraTurnFlag = false;
    }
}
