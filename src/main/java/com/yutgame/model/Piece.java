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

    public Piece(Player owner) {
        this.owner = owner;
        this.currentNode = null;
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
     * 노드 이동 시 occupantPieces와 동기화.
     * nextNode가 null인 경우, 말이 보드에서 제거됨(숨김 처리).
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

        // 그룹 이동 로직(다른 말과 함께 이동 등)은 필요시 구현
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
