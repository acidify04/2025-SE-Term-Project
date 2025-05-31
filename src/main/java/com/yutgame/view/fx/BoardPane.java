package main.java.com.yutgame.view.fx;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
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
    private final GameBoardView gameBoardView;

    public BoardPane(YutGameController controller, GameBoardView gameBoardView) {
        this.controller = controller;
        this.gameBoardView = gameBoardView;
        this.setPrefSize(440, 440);
        this.setMaxSize(440, 440);
        this.setMinSize(440, 440);
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
            String path = switch (playerName) {
                case "P1" -> "/fx/piece/piece_1.png";
                case "P2" -> "/fx/piece/piece_2.png";
                case "P3" -> "/fx/piece/piece_3.png";
                case "P4" -> "/fx/piece/piece_4.png";
                default -> "/fx/piece/piece_1.png";
            };

            int px = node.getX() + (idx * 10);
            int py = node.getY() - 13 + (idx * 10);
            StackPane stackPane = clickableImage(px, py, path, e ->gameBoardView.onPieceClicked(p));

            idx++;
        }
    }

    private StackPane clickableImage(int px, int py, String path, EventHandler<ActionEvent> act) {
        StackPane stackPane = new StackPane();
        stackPane.setMaxSize(PIECE_SIZE_X, PIECE_SIZE_Y);

        ImageView iv = new ImageView(new Image(path));
        iv.setFitWidth(PIECE_SIZE_X);
        iv.setFitHeight(PIECE_SIZE_Y);

        stackPane.getChildren().add(iv);
        stackPane.setLayoutX(px);
        stackPane.setLayoutY(py);

        this.getChildren().add(stackPane);

        // 마우스 이벤트 설정
        stackPane.setOnMouseClicked(e -> act.handle(new ActionEvent()));

        // 마우스 커서 변경
        stackPane.setCursor(Cursor.HAND);

        // 마우스 호버 효과 (선택사항)
        stackPane.setOnMouseEntered(e -> {
            stackPane.setOpacity(0.8);  // 살짝 투명하게
            stackPane.setScaleX(1.05);  // 살짝 확대
            stackPane.setScaleY(1.05);
        });

        stackPane.setOnMouseExited(e -> {
            stackPane.setOpacity(1.0);  // 원래대로
            stackPane.setScaleX(1.0);   // 원래 크기
            stackPane.setScaleY(1.0);
        });

        return stackPane;
    }

    private void onPieceClicked(Piece piece) {
        gameBoardView.onPieceClicked(piece);
    }
}
