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
 * - 말 아이콘 (플레이어별로 구분)
 */
public class BoardPanel extends JPanel {

    private YutGame game;
    private Image pieceIconP1;
    private Image pieceIconP2; // TODO: 플레이어 명수 선택 받도록 수정

    // 노드/말 원의 크기 지정
    private static final int NODE_SIZE = 40;
    private static final int PIECE_SIZE = 30;

    public BoardPanel(YutGame game) {
        this.game = game;

        // TODO: 각 플레이어마다 아이콘 이미지 추가 및 설정 필요
        pieceIconP1 = Toolkit.getDefaultToolkit().createImage("src/main/resources/piece_p1.png");
        pieceIconP2 = Toolkit.getDefaultToolkit().createImage("src/main/resources/piece_p2.png");

        // 마우스 클릭 -> 노드 범위 클릭 여부 확인, 디버그용
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                BoardNode clickedNode = findNodeAt(e.getX(), e.getY());
                if (clickedNode != null) {
                    System.out.println("Node clicked: " + clickedNode.getId());
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // 배경
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());

        // 노드 간 연결 라인
        List<BoardNode> nodes = game.getBoard().getNodes();
        g.setColor(Color.GRAY);
        for (BoardNode node : nodes) {
            drawConnections(g, node);
        }

        // 노드, 말
        for (BoardNode node : nodes) {
            drawNode(g, node);
            drawPieces(g, node);
        }
    }

    private void drawConnections(Graphics g, BoardNode node) {
        int x1 = node.getX() + NODE_SIZE/2;
        int y1 = node.getY() + NODE_SIZE/2;
        for (BoardNode next : node.getNextNodes()) {
            int x2 = next.getX() + NODE_SIZE/2;
            int y2 = next.getY() + NODE_SIZE/2;
            g.drawLine(x1, y1, x2, y2);
        }
    }

    private void drawNode(Graphics g, BoardNode node) {
        int x = node.getX();
        int y = node.getY();
        g.setColor(Color.LIGHT_GRAY);
        g.fillOval(x, y, NODE_SIZE, NODE_SIZE);

        g.setColor(Color.BLACK);
        g.drawOval(x, y, NODE_SIZE, NODE_SIZE);

        // 노드 ID 표시
        g.drawString(node.getId(), x, y - 5);
    }

    private void drawPieces(Graphics g, BoardNode node) {
        List<Piece> pieces = node.getOccupantPieces();
        for (int i = 0; i < pieces.size(); i++) {
            Piece p = pieces.get(i);
            Image icon = pickIconForPlayer(p.getOwner());

//            // 말 업었을 때 겹칠 때 오프셋
//            int offsetX = (i % 3) * 10;
//            int offsetY = (i / 3) * 10;

            int px = node.getX() + 5 + (i*10);
            int py = node.getY() + 5 + (i*10);

            g.drawImage(icon, px, py, PIECE_SIZE, PIECE_SIZE, this);
        }
    }

    // TODO: 인원수 입력 받으면 수정
    private Image pickIconForPlayer(Player owner) {
        if ("P1".equals(owner.getName())) {
            return pieceIconP1;
        } else {
            return pieceIconP2;
        }
    }

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
}
