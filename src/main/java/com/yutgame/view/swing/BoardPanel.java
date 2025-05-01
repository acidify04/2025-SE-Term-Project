package main.java.com.yutgame.view.swing;

import main.java.com.yutgame.model.*;

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

        // 상단에 점수판
        drawScoreBoard(g);

        // 노드 연결선
        g.setColor(Color.GRAY);
        for (BoardNode node : game.getBoard().getNodes()) {
            int x1 = node.getX() + NODE_SIZE/2;
            int y1 = node.getY() + NODE_SIZE/2;
            for (BoardNode nxt : node.getNextNodes()) {
                int x2 = nxt.getX() + NODE_SIZE/2;
                int y2 = nxt.getY() + NODE_SIZE/2;
                g.drawLine(x1, y1, x2, y2);
            }
        }

        // 노드와 말
        for (BoardNode node : game.getBoard().getNodes()) {
            drawNode(g, node);
            drawPieces(g, node);
        }
    }

    private void drawScoreBoard(Graphics g) {
        List<Player> players = game.getPlayers();
        g.setColor(Color.BLACK);
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        int x = 10, y = 20;
        for (Player p : players) {
            String text = p.getName() + ": " + p.getFinishedCount() + "개 완주";
            g.drawString(text, x, y);
            x += 150;
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
        int idx = 0;
        for (Piece p : node.getOccupantPieces()) {
            // 캡처되거나 완주된 말은 그리지 않음
            if (p.isFinished() || p.getCurrentNode() == null) continue;
            Image img = "P1".equals(p.getOwner().getName()) ? pieceIconP1 : pieceIconP2;
            int px = node.getX() + 5 + (idx * 10);
            int py = node.getY() + 5 + (idx * 10);
            g.drawImage(img, px, py, PIECE_SIZE, PIECE_SIZE, this);
            idx++;
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
