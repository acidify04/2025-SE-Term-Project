package main.java.com.yutgame.view.fx;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import main.java.com.yutgame.controller.YutGameController;
import main.java.com.yutgame.model.BoardNode;
import main.java.com.yutgame.model.Piece;

/**
 * 실제 윷놀이 판
 */
public class BoardPane extends Pane {

    private static final int NODE_SIZE = 35;
    private static final int PIECE_SIZE_X = 40;
    private static final int PIECE_SIZE_Y = 55;

    private final YutGameController controller;

    public BoardPane(YutGameController controller) {
        this.controller = controller;
        drawBoard();
    }

    public void drawBoard() {
        this.getChildren().clear();

        // 노드 간 연결선
        for (BoardNode node : controller.getBoard().getNodes()) {
            int x1 = node.getX() + NODE_SIZE / 2;
            int y1 = node.getY() + NODE_SIZE / 2;
            for (BoardNode nxt : node.getNextNodes()) {
                int x2 = nxt.getX() + NODE_SIZE / 2;
                int y2 = nxt.getY() + NODE_SIZE / 2;

                Line line = new Line(x1, y1, x2, y2);
                line.setStroke(Color.GRAY);
                this.getChildren().add(line);
            }
        }

        // 노드와 말 그리기
        for (BoardNode node : controller.getBoard().getNodes()) {
            drawNode(node);
            drawPieces(node);
        }
    }

    private void drawNode(BoardNode node) {
        String id = node.getId();

        double radius;

        // default가 아닌 경우 노드 크기를 40으로
        switch (id) {
            case "A", "B", "C", "D", "E", "START_NODE", "CENTER" -> radius = 40 / 2.0;
            default -> radius = NODE_SIZE / 2.0; // 기본 35
        }

        Circle circle = new Circle(node.getX() + radius, node.getY() + radius, radius);
        switch (id) {
            case "A" -> circle.setFill(Color.web("FF7A7C"));
            case "B" -> circle.setFill(Color.web("7AABFF"));
            case "C" -> circle.setFill(Color.web("7AFF87"));
            case "D" -> circle.setFill(Color.web("FF7AFD"));
            case "E" -> circle.setFill(Color.web("9548E7"));
            case "START_NODE" -> circle.setFill(Color.web("FFF67A"));
            case "CENTER" -> circle.setFill(Color.LIGHTSLATEGREY);
            default -> circle.setFill(Color.LIGHTGRAY);
        }
        this.getChildren().add(circle);
    }

    private void drawPieces(BoardNode node) {
        int idx = 0;
        for (Piece p : node.getOccupantPieces()) {
            if (p.isFinished() || p.getCurrentNode() == null) continue;

            String playerName = p.getOwner().getName();
            Image image = switch (playerName) {
                case "P1" -> new Image("/fx/piece/piece_1.png");
                case "P2" -> new Image("/fx/piece/piece_2.png");
                case "P3" -> new Image("/fx/piece/piece_3.png");
                case "P4" -> new Image("/fx/piece/piece_4.png");
                default -> new Image("/fx/piece/piece_1.png");
            };

            int px = node.getX() + (idx * 10);
            int py = node.getY() - 13 + (idx * 10);
            ImageView iv = new ImageView(image);
            iv.setFitWidth(PIECE_SIZE_X);
            iv.setFitHeight(PIECE_SIZE_Y);
            iv.setX(px);
            iv.setY(py);
            this.getChildren().add(iv);
            idx++;
        }
    }
}
