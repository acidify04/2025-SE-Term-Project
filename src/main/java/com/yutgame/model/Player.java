package main.java.com.yutgame.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 플레이어(또는 팀)를 나타내는 클래스.
 */
public class Player {

    private String name;
    private List<Piece> pieces;
    private boolean isWinner;
    private int playerIndex;

    public Player(String name, List<Piece> pieces, int playerIndex) {
        this.name = name;
        this.pieces = pieces;
        this.isWinner = false;
        this.playerIndex = playerIndex;
    }

    public int getIndex() { return playerIndex; }

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
    public int getFinishedCount() {
        Set<Piece> all = new HashSet<>();
        for (Piece piece : pieces) {
            collectGroup(piece, all);  // 재귀적으로 그룹까지 포함
        }
        return (int)all.stream().filter(Piece::isFinished).count();
    }

    // 재귀적으로 그룹 말 포함
    private void collectGroup(Piece piece, Set<Piece> visited) {
        if (visited.contains(piece)) return;
        visited.add(piece);
        for (Piece grouped : piece.getGroupedPieces()) {
            collectGroup(grouped, visited);
        }
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
