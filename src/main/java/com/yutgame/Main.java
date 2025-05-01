package main.java.com.yutgame;

import main.java.com.yutgame.model.*;
import main.java.com.yutgame.view.swing.SwingYutGameView;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 메인 실행 클래스.
 * SwingYutGameView를 실행하여 GUI 윷놀이 게임을 시작한다.
 */
public class Main {
    public static void main(String[] args) {
        // 1) 모델(보드, 플레이어, 말) 준비
        Board board = Board.createStandardBoard();   // 좌표 부여된 표준 보드 생성

        Player p1 = new Player("P1", new ArrayList<>());
        Player p2 = new Player("P2", new ArrayList<>());

        // 각 플레이어 2개 말로 예시
        Piece p1a = new Piece(p1);
        Piece p1b = new Piece(p1);
        p1.getPieces().add(p1a);
        p1.getPieces().add(p1b);

        Piece p2a = new Piece(p2);
        Piece p2b = new Piece(p2);
        p2.getPieces().add(p2a);
        p2.getPieces().add(p2b);

        YutGame game = new YutGame(List.of(p1, p2), board);

        // 2) Swing 실행 스레드 시작
        SwingUtilities.invokeLater(() -> {
            SwingYutGameView view = new SwingYutGameView(game);
            view.setVisible(true);
        });
    }
}
