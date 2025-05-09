package main.java.com.yutgame.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 플레이어의 말 하나를 나타내는 클래스.
 * currentNode: 현재 위치
 * groupedPieces: 업힌 말들 (그룹)
 */
public class Piece {

    private Player owner;         // 소유 플레이어
    private BoardNode currentNode; // 현재 노드
    private boolean isFinished;    // 골인 여부
    private List<Piece> groupedPieces; // 업기(그룹)된 말 목록
    private boolean isGrouped;  //그룹핑 여부
    private List<BoardNode> pathHistory = new ArrayList<>(); // 말이 지나온 모든 칸 기록

    public Piece(Player owner) {
        this.owner = owner;
        this.currentNode = null;
        this.isFinished = false;
        this.groupedPieces = new ArrayList<>();
        this.isGrouped = false;
        this.pathHistory = new ArrayList<>();   // 경로 기록 리스트
    }

    public Player getOwner() {
        return owner;
    }

    public BoardNode getCurrentNode() {
        return currentNode;
    }

    public boolean isFinished() {
        return isFinished;
    }

    public void setFinished(boolean finished) {
        this.isFinished = finished;
    }

    public List<Piece> getGroupedPieces() {
        return groupedPieces;
    }

    /**
     * 노드 이동 시 occupantPieces와 동기화.
     * nextNode가 null인 경우, 말이 보드에서 제거됨(숨김 처리).
     */

    /**
     * 말이 실제로 이동한다. (occupantPieces 동기화만 담당)
     * 경로 기록은 YutGame.movePiece() 쪽에서 일괄 수행한다.
     */
    public void moveTo(BoardNode nextNode) {
        // 기존 노드에서 제거
        if (currentNode != null) {
            currentNode.removePiece(this);
        }

        // 새 노드에 추가 (nextNode가 null이면 추가 안 함)
        if (nextNode != null) {
            nextNode.addPiece(this);
        }

        this.currentNode = nextNode;
    }

    // 히스토리에 노드 추가(중복 방지) + 디버그 출력
    public void recordNode(BoardNode node) {
        if (node == null) return;
        if (pathHistory.isEmpty() || pathHistory.get(pathHistory.size() - 1) != node) {
            pathHistory.add(node);
        }
        // ── 디버그: 현재 히스토리 출력
        System.out.print("[DEBUG] " + owner.getName() + " pathHistory : ");
        for (BoardNode n : pathHistory) {
            System.out.print(n.getId() + " ");
        }
        System.out.println();
    }

    /**
     * 빽도: 히스토리 상에서 한 칸 뒤로 이동.
     * pop → 직전 노드로 실제 이동 → 히스토리 디버그 출력
     */
    public void moveBackOneStep() {
        if (pathHistory.size() < 2) return;           // 뒤로 갈 곳이 없음
        // 현재 위치 제거
        pathHistory.remove(pathHistory.size() - 1);
        BoardNode prev = pathHistory.get(pathHistory.size() - 1);

        if (currentNode != null) currentNode.removePiece(this);
        prev.addPiece(this);
        this.currentNode = prev;

        // 디버그 출력
        System.out.print("[DEBUG] " + owner.getName() + " pathHistory(after BAK_DO) : ");
        for (BoardNode n : pathHistory) {
            System.out.print(n.getId() + " ");
        }
        System.out.println();
    }

    // 경로 확인용 getter
    public List<BoardNode> getPathHistory() {
        return pathHistory;
    }


    /**
     * 아군 말과 업기(그룹화) 처리
     */
    public void groupWith(Piece other) {
        if (!this.groupedPieces.contains(other)) {
            this.groupedPieces.add(other);
        }
        if (!other.groupedPieces.contains(this)) {
            other.groupedPieces.add(this);
        }
        isGrouped = true;
    }

    public boolean isGroup(){
        return isGrouped;
    }

    /**
     * 그룹 해제
     */
    public void ungroup() {
        for (Piece p : new ArrayList<>(groupedPieces)) {
            p.groupedPieces.remove(this);
        }
        groupedPieces.clear();
    }
}
