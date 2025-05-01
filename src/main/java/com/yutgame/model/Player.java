package main.java.com.yutgame.model;

import java.util.List;

/**
 * 플레이어(또는 팀)를 나타내는 클래스.
 * 소유 말 목록을 관리하고, 승리 여부 등 상태를 가질 수 있음.
 */
public class Player {

    private String name;
    private List<Piece> pieces;
    private boolean isWinner;  // 최종 승리자 여부

    public Player(String name, List<Piece> pieces) {
        this.name = name;
        this.pieces = pieces;
        this.isWinner = false;
    }

    public String getName() {
        return name;
    }

    public List<Piece> getPieces() {
        return pieces;
    }

    public boolean isWinner() {
        return isWinner;
    }

    public void setWinner(boolean winner) {
        this.isWinner = winner;
    }

    /**
     * 모든 말이 골인(isFinished=true)했는지 확인.
     */
    public boolean allPiecesFinished() {
        if (pieces.isEmpty()) return false;

        for (Piece piece : pieces) {
            if (!piece.isFinished()) {
                return false;
            }
        }
        return true;
    }
}
