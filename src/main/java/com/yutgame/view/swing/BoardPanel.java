package main.java.com.yutgame.view.swing;

import main.java.com.yutgame.model.Board;
import main.java.com.yutgame.model.BoardNode;
import main.java.com.yutgame.model.Piece;
import main.java.com.yutgame.model.Player;
import main.java.com.yutgame.model.YutGame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * 실제 윷놀이판(노드, 말)을 그리는 JPanel.
 * - 노드 간 라인
 * - 노드 원
 * - 말 아이콘
 */
public class BoardPanel extends JPanel {

    private YutGame game;

    // 말 아이콘 (플레이어별로 다르게 표시 가능)
    private Image pieceIconP1;
    private Image pieceIconP2;

    // 노드/말 원의 크기 지정
    private static final int NODE_SIZE = 40;
    private static final int PIECE_SIZE = 30;

    public BoardPanel(YutGame game) {
        this.game = game;

        // 아이콘 로드 (프로젝트 구조에 따라 경로 수정 필요)
        pieceIconP1 = Toolkit.getDefaultToolkit().createImage("src/main/resources/piece_p1.png");
        pieceIconP2 = Toolkit.getDefaultToolkit().createImage("src/main/resources/piece_p2.png");

        // 마우스 클릭 -> 노드 범위 클릭 여부 확인, 디버그용
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                BoardNode clickedNode = findNodeAt(e.getX(), e.getY());
                if (clickedNode != null) {
                    System.out.println("Node clicked: " + clickedNode.getId());
                    // 여기서 직접 말 이동 로직을 할 수도 있지만,
                    // MVC 설계상 SwingYutGameView가 주도하거나 Controller가 주도하는 편이 좋음.
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // 흰 배경
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());

        // 노드 간 라인 먼저 그림
        Board board = game.getBoard();
        for (BoardNode node : board.getNodes()) {
            drawNodeConnections(g, node);
        }

        // 그 다음 노드 원, occupantPieces
        for (BoardNode node : board.getNodes()) {
            drawNode(g, node);
            drawPiecesOnNode(g, node);
        }
    }

    /**
     * 노드와 nextNodes를 라인으로 연결.
     */
    private void drawNodeConnections(Graphics g, BoardNode node) {
        int x1 = node.getX() + NODE_SIZE/2;  // 원 중앙
        int y1 = node.getY() + NODE_SIZE/2;
        g.setColor(Color.GRAY);
        for (BoardNode next : node.getNextNodes()) {
            int x2 = next.getX() + NODE_SIZE/2;
            int y2 = next.getY() + NODE_SIZE/2;
            g.drawLine(x1, y1, x2, y2);
        }
    }

    /**
     * 노드 자체(원)와 ID 텍스트 표시.
     */
    private void drawNode(Graphics g, BoardNode node) {
        int x = node.getX();
        int y = node.getY();
        g.setColor(Color.LIGHT_GRAY);
        g.fillOval(x, y, NODE_SIZE, NODE_SIZE);

        g.setColor(Color.BLACK);
        g.drawOval(x, y, NODE_SIZE, NODE_SIZE);

        // 노드 ID
        g.drawString(node.getId(), x, y - 5);
    }

    /**
     * 노드 위에 있는 말들을 그린다.
     */
    private void drawPiecesOnNode(Graphics g, BoardNode node) {
        List<Piece> pieces = node.getOccupantPieces();
        for (int i = 0; i < pieces.size(); i++) {
            Piece p = pieces.get(i);
            Image icon = pickIconForPlayer(p.getOwner());

            // 말이 여러 개 겹치면 약간씩 오프셋
            int offsetX = (i % 3) * 10;
            int offsetY = (i / 3) * 10;

            int px = node.getX() + 5 + offsetX;
            int py = node.getY() + 5 + offsetY;

            g.drawImage(icon, px, py, PIECE_SIZE, PIECE_SIZE, this);
        }
    }

    /**
     * 플레이어 이름에 따라 다른 아이콘 선택.
     */
    private Image pickIconForPlayer(Player owner) {
        if ("P1".equals(owner.getName())) {
            return pieceIconP1;
        } else {
            return pieceIconP2;
        }
    }

    /**
     * (마우스) 클릭 좌표가 어느 노드의 원 범위인지 판별.
     */
    private BoardNode findNodeAt(int mx, int my) {
        for (BoardNode node : game.getBoard().getNodes()) {
            int nx = node.getX();
            int ny = node.getY();
            // 원 범위 검사
            if (mx >= nx && mx <= nx + NODE_SIZE && my >= ny && my <= ny + NODE_SIZE) {
                return node;
            }
        }
        return null;
    }

    public YutGame getGame() {
        return game;
    }
}
