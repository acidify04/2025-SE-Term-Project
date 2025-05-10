package main.java.com.yutgame.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        // System.out.print("[DEBUG] " + owner.getName() + " pathHistory : ");
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
        // 1. 그룹 수집
        Set<Piece> group = new HashSet<>();
        collectGroup(this, group);

        // 2. 공유된 pathHistory
        List<BoardNode> history = this.getPathHistory();
        if (history.size() < 2) return;

        // ✅ 하나라도 currentNode가 null이면 안 됨
        BoardNode current = null;
        for (Piece p : group) {
            if (p.getCurrentNode() != null) {
                current = p.getCurrentNode();
                break;
            }
        }
        if (current == null) return;

        int idx = history.lastIndexOf(current);
        if (idx <= 0) return;

        BoardNode prev = history.get(idx - 1);
        history.remove(idx); // 한 번만 제거!

        // 3. 그룹 전체 이동
        for (Piece p : group) {
            if (p.currentNode != null) p.currentNode.removePiece(p);
            prev.addPiece(p);
            p.currentNode = prev;

            // 디버그 로그
            System.out.print("[DEBUG] " + p.getOwner().getName() + " pathHistory(after BAK_DO) : ");
            for (BoardNode n : history) {
                System.out.print(n.getId() + " ");
            }
            System.out.println();
        }
    }


    // 경로 확인용 getter
    public List<BoardNode> getPathHistory() {
        return pathHistory;
    }


    /**
     * 아군 말과 업기(그룹화) 처리
     */
    public void groupWith(Piece other) {
        if (this == other) return;

        // 두 말이 포함된 전체 그룹 수집
        Set<Piece> union = new HashSet<>();
        collectGroup(this, union);
        collectGroup(other, union);

        // 모든 말의 groupedPieces를 일괄 초기화
        for (Piece p : union) {
            p.groupedPieces = new ArrayList<>();
        }

        // 모든 말의 groupedPieces를 다시 설정
        for (Piece p : union) {
            for (Piece q : union) {
                if (p != q) {
                    p.groupedPieces.add(q);
                }
            }
            p.isGrouped = true;
        }

        // 모든 말이 동일한 pathHistory를 공유하도록 설정
        List<BoardNode> sharedHistory = new ArrayList<>(this.pathHistory);
        for (Piece p : union) {
            p.pathHistory = sharedHistory;
        }
    }

    // 현재 말과 연결된 모든 그룹 구성원을 재귀적으로 수집
    private void collectGroup(Piece piece, Set<Piece> collected) {
        if (collected.contains(piece)) return;
        collected.add(piece);
        for (Piece p : piece.getGroupedPieces()) {
            collectGroup(p, collected);
        }
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
