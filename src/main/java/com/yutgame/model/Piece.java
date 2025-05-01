package main.java.com.yutgame.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 하나의 말(토큰)을 나타내는 클래스.
 * 현재 노드, 골인 여부, 업기(그룹) 등을 관리.
 */
public class Piece {

    private Player owner;
    private BoardNode currentNode;
    private boolean isFinished;
    private List<Piece> groupedPieces; // 같은 팀 말과 업기(그룹)

    public Piece(Player owner) {
        this.owner = owner;
        this.isFinished = false;
        this.groupedPieces = new ArrayList<>();
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
     * 이 말이 다음 노드로 실제 이동하는 동작.
     * occupantPieces 동기화 처리(기존 노드 remove, 새 노드 add).
     */
    public void moveTo(BoardNode nextNode) {
        // 기존 노드에서 제거
        if (currentNode != null) {
            currentNode.removePiece(this);
        }
        // 새 노드에 추가
        nextNode.addPiece(this);
        // 현재 노드 갱신
        this.currentNode = nextNode;

        // 그룹 이동 로직(단순히 대표 말만 이동 or 모든 groupedPieces도 이동 등은 추후 확장 가능)
    }

    /**
     * 다른 말과 업기(그룹화).
     * 양방향으로 groupedPieces를 참조하도록 설정.
     */
    public void groupWith(Piece other) {
        if (!this.groupedPieces.contains(other)) {
            this.groupedPieces.add(other);
        }
        if (!other.groupedPieces.contains(this)) {
            other.groupedPieces.add(this);
        }
    }

    /**
     * 그룹 해제(모든 양방향 연결 제거).
     */
    public void ungroup() {
        for (Piece p : new ArrayList<>(groupedPieces)) {
            p.groupedPieces.remove(this);
        }
        groupedPieces.clear();
    }
}
