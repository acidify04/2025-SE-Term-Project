package main.java.com.yutgame.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 보드 상의 한 칸(노드)을 나타내는 클래스.
 * occupantPieces에 현재 이 노드 위에 있는 말들이 저장됨.
 * nextNodes로 연결되어 갈림길이나 직선 이동을 표현함.
 */
public class BoardNode {

    private final String id;  // 노드 식별자(출발, 모서리, 중앙 등)
    private List<Piece> occupantPieces;  // 현재 이 칸에 올라온 말들
    private List<BoardNode> nextNodes;   // 이동 가능한 다음 칸(갈림길 가능)

    // 화면상의 위치 좌표 추가
    private int x;
    private int y;

    public BoardNode(String id) {
        this.id = id;
        this.occupantPieces = new ArrayList<>();
        this.nextNodes = new ArrayList<>();
        // 기본값으로 초기화 (나중에 설정)
        this.x = 0;
        this.y = 0;
    }

    // 위치 정보까지 포함하는 생성자 추가
    public BoardNode(String id, int x, int y) {
        this.id = id;
        this.occupantPieces = new ArrayList<>();
        this.nextNodes = new ArrayList<>();
        this.x = x;
        this.y = y;
    }

    public String getId() {
        return id;
    }

    public List<Piece> getOccupantPieces() {
        return occupantPieces;
    }

    public List<BoardNode> getNextNodes() {
        return nextNodes;
    }

    // x, y 좌표 getter/setter 추가
    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    // 위치 한번에 설정하는 메서드 (편의용)
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * nextNodes에 연결된 노드를 추가.
     */
    public void addNextNode(BoardNode node) {
        this.nextNodes.add(node);
    }

    /**
     * 이 노드 위에 말이 올라옴.
     */
    public void addPiece(Piece piece) {
        if (!occupantPieces.contains(piece)) {
            occupantPieces.add(piece);
        }
    }

    /**
     * 이 노드에서 말이 떠남.
     */
    public void removePiece(Piece piece) {
        occupantPieces.remove(piece);
    }
}