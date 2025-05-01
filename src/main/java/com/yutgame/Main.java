package main.java.com.yutgame;

import main.java.com.yutgame.model.*;
import main.java.com.yutgame.view.swing.SwingYutGameView;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * 메인 실행 클래스.
 * 게임 시작 시 플레이어 수(2~4)와 말 개수 선택 기능 추가.
 */
public class Main {
    public static void main(String[] args) {
        // 플레이어 수 선택
        Integer playerCount = null;
        while (playerCount == null || playerCount < 2 || playerCount > 4) {
            String input = JOptionPane.showInputDialog(
                    null,
                    "플레이어 수를 입력하세요 (2~4):",
                    "게임 설정",
                    JOptionPane.QUESTION_MESSAGE
            );
            if (input == null) {
                System.exit(0);
            }
            try {
                playerCount = Integer.parseInt(input);
            } catch (NumberFormatException ex) {
                playerCount = null;
            }
        }

        // 말 개수 선택
        Integer pieceCount = null;
        while (pieceCount == null || pieceCount < 1) {
            String input = JOptionPane.showInputDialog(
                    null,
                    "각 플레이어의 말 개수를 입력하세요 (1 이상의 정수):",
                    "게임 설정",
                    JOptionPane.QUESTION_MESSAGE
            );
            if (input == null) {
                System.exit(0);
            }
            try {
                pieceCount = Integer.parseInt(input);
            } catch (NumberFormatException ex) {
                pieceCount = null;
            }
        }

        // 플레이어와 말 생성
        List<Player> players = new ArrayList<>();
        for (int i = 1; i <= playerCount; i++) {
            Player player = new Player("P" + i, new ArrayList<>());
            for (int j = 0; j < pieceCount; j++) {
                Piece piece = new Piece(player);
                player.getPieces().add(piece);
            }
            players.add(player);
        }

        // 보드 선택 (기본: 사각)
        YutBoard board = SquareBoard.createStandardBoard();
        // 원하는 보드를 사용하려면 아래 주석 해제
        // YutBoard board = PentagonBoard.createPentagonBoard();
        // YutBoard board = HexagonBoard.createHexBoard();

        // 윷게임 생성
        YutGame game = new YutGame(players, board);

        // Swing 실행
        SwingUtilities.invokeLater(() -> {
            SwingYutGameView view = new SwingYutGameView(game);
            view.setVisible(true);
        });
    }
}
