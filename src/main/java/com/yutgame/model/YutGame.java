package main.java.com.yutgame.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

    private Random random = new Random();

    public YutGame(List<Player> players, YutBoard board) {
        this.players = players;
        this.board = board;
        this.currentPlayerIndex = 0;
        this.isGameOver = false;
        this.winner = null;
        this.lastThrowResult = null;
        this.extraTurnFlag = false;
    }

    /**
     * 게임 시작 시 초기화(필요하다면).
     */
    public void startGame() {
        // 말이 출발 노드에 아직 올라가지 않았거나, 향후 초기화 로직 가능
        this.isGameOver = false;
        this.currentPlayerIndex = 0;
        this.winner = null;
        this.extraTurnFlag = false;
    }

    /**
     * 무작위 윷 던지기.
     */
    public YutThrowResult throwYutRandom() {
        int r = random.nextInt(6); // 0~5
        YutThrowResult result = switch (r) {
            case 0 -> YutThrowResult.BAK_DO;
            case 1 -> YutThrowResult.DO;
            case 2 -> YutThrowResult.GAE;
            case 3 -> YutThrowResult.GEOL;
            case 4 -> YutThrowResult.YUT;
            case 5 -> YutThrowResult.MO;
            default -> YutThrowResult.DO; // fallback
        };
        this.lastThrowResult = result;
        return result;
    }

    /**
     * 지정 윷 던지기 (테스트/디버그용).
     */
    public void throwYutManual(YutThrowResult manualResult) {
        this.lastThrowResult = manualResult;
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
     * 말 이동 로직:
     * - 사용자가 선택한 최종 목적지(targetNode)로 말을 이동시킨다.
     * - 상대 말 잡기 시 extraTurnFlag = true
     * - 아군 말 업기
     * - FINISH 노드면 골인 처리 (주의: 현재 구조에서는 START_FINISH 통과 시 골인 처리 필요)
     * - 이동 후 승리 여부 체크
     */
// public void movePiece(Piece piece, int steps) { // 기존 시그니처
    public void movePiece(Piece piece, BoardNode targetNode) { // 변경된 시그니처

        // 현재 노드가 없으면 startNode로 세팅(출발 안 했을 경우)
        BoardNode currentNode = piece.getCurrentNode();
        if (currentNode == null) {
            currentNode = board.getStartNode();
            // 중요: 아직 출발 전의 말을 이동시키는 경우, targetNode가 startNode에서 steps 만큼
            // 이동한 위치인지 검증하는 로직이 추가로 필요할 수 있음.
            // 여기서는 일단 piece의 초기 위치 설정 정도로 가정.
            // piece.moveTo(currentNode); // moveTo는 아래에서 targetNode로 이동 시 처리됨.
        }

        // --- steps 기반 경로 재계산 로직 제거 ---
        // List<BoardNode> possibleDestinations = board.getPossibleNextNodes(currentNode, steps);
        // BoardNode chosenNodeInternal = null;
        // if (!possibleDestinations.isEmpty()) {
        //     chosenNodeInternal = possibleDestinations.get(0); // 항상 첫번째 선택하는 문제 로직
        // }
        // if (chosenNodeInternal == null) {
        //     return;
        // }
        // -------------------------------------

        // 사용자가 View에서 선택한 targetNode가 null이면 이동 불가
        if (targetNode == null) {
            System.err.println("Error: Target node is null in movePiece.");
            return;
        }

        // 실제 이동 (사용자가 선택한 노드로 이동)
        piece.moveTo(targetNode); // 변경: targetNode 사용

        // 잡기 (targetNode 기준으로)
        boolean didCapture = captureIfNeeded(targetNode, piece.getOwner());

        // 업기 (targetNode 기준으로)
        groupIfSameTeam(targetNode, piece.getOwner(), piece);

        // 골인 처리 (targetNode 기준으로)
        // 주의: 현재 Board 구조상 START_FINISH 노드를 통과하는 것을 감지해야 함.
        //       "FINISH" ID를 가진 노드는 없음. YutGame 레벨에서 골인 로직 수정 필요.
        // if ("FINISH".equals(targetNode.getId())) {
        if (isGoal(piece, currentNode, targetNode)) { // isGoal 같은 헬퍼 메소드 필요
            piece.setFinished(true);
            targetNode.removePiece(piece); // 골인 시 보드에서 제거
            // piece.setCurrentNode(null); // 혹은 상태 표시
        }

        // 잡았다면 "한 번 더"
        if (didCapture) {
            this.extraTurnFlag = true;
        }

        // 이동 후 승리 체크
        checkWinCondition();
    }

    // 골인 판정 헬퍼 메소드 예시 (구체적인 구현 필요)
    private boolean isGoal(Piece piece, BoardNode previousNode, BoardNode targetNode) {
        // 예: targetNode가 START_FINISH 이고, previousNode가 START_FINISH로 들어오는 마지막 노드(O19, I_D2) 중 하나인가?
        // 혹은 말이 보드 위를 충분히 돌았는지 등 상태 확인 필요
        BoardNode startFinish = board.getStartNode();
        if (targetNode.equals(startFinish)) {
            // 처음 출발하는 경우가 아닌지, 한 바퀴 돌았는지 등의 추가 조건 필요
            // 예를 들어, piece가 이미 한 번 이상 움직였고, 이전 노드가 O19 또는 I_D2 인 경우
            if (previousNode != null && (previousNode.getId().equals("O19") || previousNode.getId().equals("I_D2"))) {
                return true;
            }
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
        // 윷/모 이거나 extraTurnFlag=true => 턴 유지
        if (lastThrowResult == YutThrowResult.YUT
                || lastThrowResult == YutThrowResult.MO
                || extraTurnFlag) {
            // 같은 플레이어 턴 유지
        } else {
            // 빽도, 도, 개, 걸은 다음 플레이어
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        }
        // 다음 턴을 준비하면서 extraTurnFlag 초기화
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


}
