package main.java.com.yutgame.model;

import java.util.List;

/**
 * 플레이어(또는 팀)를 나타내는 클래스.
 */
public class Player {

    private String name;
    private List<Piece> pieces;
    private boolean isWinner;

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
     * 모든 말이 골인했는지 체크.
     */
    public boolean allPiecesFinished() {
        if (pieces.isEmpty()) return false;
        for (Piece p : pieces) {
            if (!p.isFinished()) {
                return false;
            }
        }
        return true;
    }

    /**
     * 완주한 말의 개수를 반환
     */
    public long getFinishedCount() {
        return pieces.stream().filter(Piece::isFinished).count();
    }

    // 디버깅 용 함수: 사용자 모든 말의 히스토리를 보여줌
    public void printAllPathHistories() {
        System.out.println("[" + getName() + "] 모든 말의 경로 기록:");
        int i = 1;
        for (Piece p : pieces) {
            System.out.print("말 " + i + ": ");
            for (BoardNode node : p.getPathHistory()) {
                System.out.print(node.getId() + " ");
            }
            System.out.println();
            i++;
        }
    }
}
