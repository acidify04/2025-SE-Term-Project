package main.java.com.yutgame.model;

import java.util.*;

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
    private int extraThrowsPending; // 남아있는 한 번 더 던지기 횟수

    private Random random;

    public YutGame(List<Player> players, YutBoard board) {
        this.players = players;
        this.board = board;
        this.currentPlayerIndex = 0;
        this.isGameOver = false;
        this.winner = null;
        this.lastThrowResult = null;
        this.extraTurnFlag = false;
        this.extraThrowsPending = 0;
        this.random = new Random();
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
     * 1) 대기 중(extraThrowsPending)이 있으면 먼저 1회 소모.
     * 2) 결과가 윷·모면 extraThrowsPending += 1.
     * 3) extraTurnFlag 는 남은 대기 여부로만 결정.
     */
    public YutThrowResult throwYutRandom() {

        /* ❶ 대기 중 기회 먼저 소모 */
        if (extraThrowsPending > 0) extraThrowsPending--;

        /* ❷ 실제 던지기 */
        int r = random.nextInt(16);
        YutThrowResult result;
        if (r == 0)                       result = YutThrowResult.BAK_DO;
        else if (r < 4)                   result = YutThrowResult.DO;
        else if (r < 8)                   result = YutThrowResult.GEOL;
        else if (r == 8)                  result = YutThrowResult.YUT;
        else if (r == 9)                  result = YutThrowResult.MO;
        else                              result = YutThrowResult.GAE;

        this.lastThrowResult = result;

        /* ❸ 윷·모면 ‘한 번 더’ */
        if (result == YutThrowResult.YUT || result == YutThrowResult.MO) {
            extraThrowsPending++;
        }

        /* ❹ 플래그 갱신 */
        extraTurnFlag = extraThrowsPending > 0;
        return result;
    }

    /**
     * 지정 결과로 윷 던지기(테스트용).
     * 내부 로직은 throwYutRandom() 과 동일하게 처리.
     */
    public void throwYutManual(YutThrowResult manualResult) {

        /* 대기 중 기회 소모 */
        if (extraThrowsPending > 0) extraThrowsPending--;

        this.lastThrowResult = manualResult;

        /* 윷/모면 추가 1회 */
        if (manualResult == YutThrowResult.YUT || manualResult == YutThrowResult.MO) {
            extraThrowsPending++;
        }

        extraTurnFlag = extraThrowsPending > 0;
    }

    public YutThrowResult getLastThrowResult() {
        return lastThrowResult;
    }

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

    /**
     * 말 이동(전진 · 빽도) + 잡기/업기/골인 처리.
     * ① 윷/모·잡기 등으로 생긴 '한 번 더 던지기'는 extraThrowsPending으로 누적 관리.
     * ② 말을 잡으면 extraThrowsPending++ 후 즉시 nextTurn() 호출 → 잡자마자 다시 윷을 던질 수 있다.
     */
    public void movePiece(Piece piece, BoardNode targetNode, boolean containsStart) {
        if (piece == null) {
            System.err.println("movePiece Error: piece is null.");
            return;
        }

        /* ──────────────────── ❶ 빽도(BAK_DO) 전용 처리 ──────────────────── */
        if (lastThrowResult == YutThrowResult.BAK_DO) {

            // 1) START 에 있거나 뒤로 갈 히스토리가 없으면 → 무효
            if (!canMoveBack(piece)) {
                System.out.println("시작지점에서 빽도를 사용하실 수 없습니다.");

                // 같은 팀에 빽도 가능한 말이 없다면 턴 종료
                boolean hasOther = false;
                for (Piece p : piece.getOwner().getPieces()) {
                    if (p != piece && canMoveBack(p)) { hasOther = true; break; }
                }
                if (!hasOther) extraTurnFlag = false;
                return;
            }

            // 2) 한 칸 뒤로 이동
            BoardNode prevNode = piece.getCurrentNode();
            piece.moveBackOneStep();
            BoardNode newNode = piece.getCurrentNode();

            // 3) 잡기
            boolean captured = captureIfNeeded(newNode, piece.getOwner());
            if (captured) {
                extraThrowsPending++;    // 잡으면 추가 1회
                extraTurnFlag = true;
            }

            // 4) 골인/업기/승리 체크
            if (isGoal(piece, prevNode, newNode, containsStart)) {
                piece.setFinished(true);
                newNode.removePiece(piece);
                if (piece.isGroup()) finishGroup(piece, newNode);
            }
            checkWinCondition();

            // 5) 잡았으면 즉시 다시 던질 수 있도록 턴 전환
            if (captured) nextTurn();

            System.out.println("[DEBUG] === movePiece end (BAK_DO) ===");
            return;   // 빽도 처리 종료
        }

        /* ──────────────── ❷ 전진 계열(DO/GAE/GEOL/YUT/MO) ──────────────── */
        if (targetNode == null) {
            System.err.println("movePiece Error: targetNode is null.");
            return;
        }

        BoardNode prevNode = piece.getCurrentNode();
        List<BoardNode> path = findShortestPath(prevNode, targetNode);

        // 1) 경로 중간 칸 이동(시각화 기록용)
        for (BoardNode step : path) piece.moveTo(step);

        // 2) 최종 칸 이동
        piece.moveTo(targetNode);

        // 3) 잡기 / 업기 / 골인
        boolean didCapture = false;
        if (!isGoal(piece, prevNode, targetNode, containsStart)) {
            didCapture = captureIfNeeded(targetNode, piece.getOwner());
        }
        groupIfSameTeam(targetNode, piece.getOwner(), piece);

        if (isGoal(piece, prevNode, targetNode, containsStart)) {
            piece.setFinished(true);
            targetNode.removePiece(piece);
            if (piece.isGroup()) finishGroup(piece, targetNode);
        }

        if (didCapture) {
            extraThrowsPending++;   // 잡으면 추가 1회
            extraTurnFlag = true;
        }

        checkWinCondition();

        // 4) 잡았을 경우 즉시 추가 던지기 기회를 주기 위해 턴 전환
        if (didCapture) nextTurn();

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
     * - 윷/모: 1회 추가
     * - 잡기(extraTurnFlag): 1회 추가
     *   ▸ 두 상황이 동시에 발생하면 총 2회가 보장돼야 하므로
     *     첫 번째 턴 후에도 extraTurnFlag를 한 번 더 유지한다.
     */
    /**
     * 턴 진행:
     * - 윷/모: 1회 추가
     * - 잡기(extraTurnFlag): 1회 추가
     *   ▸ 두 조건이 동시에 일어나면 총 2회가 필요하므로
     *     extraTurnFlag를 한 번 더 유지한다.
     * - 추가 턴을 소비했으므로 lastThrowResult는 null로 리셋.
     */
    /**
     * 턴 진행:
     * - extraThrowsPending > 0 ⇒ 같은 플레이어가 이어서 던진다.
     *   (extraTurnFlag true, lastThrowResult null 로 리셋)
     * - pending == 0 ⇒ 다음 플레이어에게 턴 넘김.
     */
    public void nextTurn() {

        if (extraThrowsPending > 0) {
            // 아직 ‘한 번 더’가 남아 있다.
            extraTurnFlag  = true;
            lastThrowResult = null;   // 직전 결과는 소진 완료
            return;
        }

        // 추가 기회가 없으면 다음 플레이어로
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        extraTurnFlag  = false;
        lastThrowResult = null;
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
    private void moveGroupWith(Piece piece, BoardNode targetNode, List<Piece> moved) {
        if (moved.contains(piece)) return;

        // 빽도 + 시작점 도달 시 -> 먼저 미출발 처리
        if (targetNode.equals(board.getStartNode()) &&
                lastThrowResult == YutThrowResult.BAK_DO) {
            piece.moveTo(null);
            moved.add(piece);
            // return 하지 말고, grouped 말도 계속 처리!
        } else {
            piece.moveTo(targetNode);
            moved.add(piece);
        }

        // 무조건 grouped 말은 재귀 처리
        for (Piece child : piece.getGroupedPieces()) {
            moveGroupWith(child, targetNode, moved);
        }
    }

    /** 현재 Piece가 빽도로 한 칸 뒤로 갈 수 있는지 검사 */
    private boolean canMoveBack(Piece p) {
        return p != null
                && p.getCurrentNode() != null
                && !p.getCurrentNode().equals(board.getStartNode())   // 이미 START면 불가
                && p.getPathHistory().size() >= 2;                    // 직전 노드가 존재해야 함
    }


    // 업힌 말 백도
    private void moveGroupBackOneStep(Piece piece) {
        Set<Piece> visited = new HashSet<>();
        moveGroupBackOneStepHelper(piece, visited);
    }

    private void moveGroupBackOneStepHelper(Piece piece, Set<Piece> visited) {
        if (visited.contains(piece)) return; // 무한 루프 방지
        visited.add(piece);

        piece.moveBackOneStep();
        for (Piece grouped : piece.getGroupedPieces()) {
            moveGroupBackOneStepHelper(grouped, visited);
        }
    }
}
