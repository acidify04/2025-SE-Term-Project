package main.java.com.yutgame.view.swing;

import main.java.com.yutgame.model.*;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * 실제 윷놀이판(노드, 말)을 그리는 JPanel.
 * - 노드 간 라인
 * - 노드 원
 * - 말 아이콘 (플레이어별로 구분)
 */
public class BoardPanel extends JPanel {

    private static final int NODE_SIZE = 40;
    private static final int PIECE_SIZE = 30;

    private final YutGame game;
    private final Image pieceIconP1;
    private final Image pieceIconP2;
    private final Image pieceIconP3;
    private final Image pieceIconP4;

    public BoardPanel(YutGame game) {
        this.game = game;
        pieceIconP1 = Toolkit.getDefaultToolkit().createImage("src/main/resources/piece1.png");
        pieceIconP2 = Toolkit.getDefaultToolkit().createImage("src/main/resources/piece2.png");
        pieceIconP3 = Toolkit.getDefaultToolkit().createImage("src/main/resources/piece3.png");
        pieceIconP4 = Toolkit.getDefaultToolkit().createImage("src/main/resources/piece4.png");
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // 배경
        if (game.getCurrentPlayer().getName().equals("P1")) {
            g.setColor(Color.decode("#C5EEF9"));
        } else if (game.getCurrentPlayer().getName().equals("P2")) {
            g.setColor(Color.decode("#F5CEEA"));
        } else if (game.getCurrentPlayer().getName().equals("P3")) {
            g.setColor(Color.decode("#CEF5CC"));
        } else if (game.getCurrentPlayer().getName().equals("P4")) {
            g.setColor(Color.decode("#F6F0D2"));
        } else {
            g.setColor(Color.white);
        }
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

    private void drawNode(Graphics g, BoardNode node) {
        int x = node.getX(), y = node.getY();
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

            // 플레이어 번호에 따라 다른 이미지 사용 (switch문 적용)
            String playerName = p.getOwner().getName();
            Image img = switch (playerName) {
                case "P1" -> pieceIconP1;
                case "P2" -> pieceIconP2;
                case "P3" -> pieceIconP3;
                case "P4" -> pieceIconP4;
                default -> pieceIconP1; // 기본값
            };

            int px = node.getX() + 5 + (idx * 10);
            int py = node.getY() + 5 + (idx * 10);
            g.drawImage(img, px, py, PIECE_SIZE, PIECE_SIZE, this);
            idx++;
        }
    }

    private void drawRemainPieces(Graphics g, BoardNode node) {
        int idx = 0;
        for (Piece p : node.getOccupantPieces()) {
            
            if (p.isFinished()){
                int x = node.getX(), y = node.getY();
                g.setColor(Color.BLACK);
                g.drawOval(x, y, NODE_SIZE, NODE_SIZE);
            }else{

            }
            // 캡처되거나 완주된 말은 그리지 않음
            if (p.isFinished() || p.getCurrentNode() == null) continue;

            // 플레이어 번호에 따라 다른 이미지 사용 (switch문 적용)
            String playerName = p.getOwner().getName();
            Image img = switch (playerName) {
                case "P1" -> pieceIconP1;
                case "P2" -> pieceIconP2;
                case "P3" -> pieceIconP3;
                case "P4" -> pieceIconP4;
                default -> pieceIconP1; // 기본값
            };

            int px = node.getX() + 5 + (idx * 10);
            int py = node.getY() + 5 + (idx * 10);
            g.drawImage(img, px, py, PIECE_SIZE, PIECE_SIZE, this);
            idx++;
        }
    }
}
