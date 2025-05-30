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
import main.java.com.yutgame.model.Player;

import java.awt.*;
import java.util.List;

public class BoardPane extends Pane {

    private static final int NODE_SIZE = 35;
    private static final int PIECE_SIZE_X = 40;
    private static final int PIECE_SIZE_Y = 55;

    private final YutGameController controller;
    private final GameBoardView gameBoardView;

    public BoardPane(YutGameController controller, GameBoardView gameBoardView) {
        this.controller = controller;
        this.gameBoardView = gameBoardView;
        drawBoard(1);
    }

    public void drawBoard(int isFirst) {
        this.getChildren().clear();

        System.out.println("drawBoard");
        if (isFirst == 0) {
            drawScoreBoard();
        }

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

    private void drawScoreBoard() {
        List<Player> players = controller.getPlayers();
        for (Player p : players) {
            System.out.println("player: " + players.size() + "플레이어의 피스 개수 : " + p.getPieces().size() + "끝난 피스 개수 : " + p.getFinishedCount());
            gameBoardView.updatePlayerInforms(players.size(), p.getPieces().size(), p.getFinishedCount());
        }
    }

    private void drawNode(BoardNode node) {
        Circle circle = new Circle(node.getX() + NODE_SIZE / 2, node.getY() + NODE_SIZE / 2, NODE_SIZE / 2);
        circle.setFill(Color.LIGHTGRAY);
        circle.setStroke(Color.BLACK);
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
