package main.java.com.yutgame;

import main.java.com.yutgame.model.*;
import main.java.com.yutgame.view.swing.SwingYutGameView;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * 메인 실행 클래스.
 * SwingYutGameView를 실행하여 GUI 윷놀이 게임을 시작한다.
 */
public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // 플레이어 수 입력
        int playerCount;
        do {
            System.out.print("플레이어 수를 입력하세요 (2~4): ");
            playerCount = scanner.nextInt();

            if (playerCount < 2 || playerCount > 4) {
                System.out.println("⚠ 플레이어는 2명부터 4명까지만 가능합니다.");
            }
        } while (playerCount < 2 || playerCount > 4);

        // 말 개수 입력
        int pieceCount;
        do {
            System.out.print("각 플레이어가 사용할 말의 수를 입력하세요 (2~5): ");
            pieceCount = scanner.nextInt();

            if (pieceCount < 2 || pieceCount > 5) {
                System.out.println("⚠ 말은 2개부터 5개까지만 사용할 수 있습니다.");
            }
        } while (pieceCount < 2 || pieceCount > 5);

        // 보드 선택
        System.out.print("보드 타입을 선택하세요 (1: 사각형, 2: 오각형, 3: 육각형): ");
        int boardChoice = scanner.nextInt();
        YutBoard board;
        switch (boardChoice) {
            // PentagonBoard, HexagonBoard는 이후 구현 예정
            //case 2:
            //board = PentagonBoard.createPentagonBoard();
            //break;
            //case 3:
            //board = HexagonBoard.createHexBoard();
            //break;
            //case 1:
            default:
                board = SquareBoard.createStandardBoard();
                break;
        }

        // 플레이어 및 말 생성
        List<Player> players = new ArrayList<>();
        for (int i = 1; i <= playerCount; i++) {
            Player player = new Player("P" + i, new ArrayList<>());
            for (int j = 0; j < pieceCount; j++) {
                player.getPieces().add(new Piece(player));
            }
            players.add(player);
        }

        // 게임 및 GUI 실행
        YutGame game = new YutGame(players, board);
        SwingUtilities.invokeLater(() -> {
            SwingYutGameView view = new SwingYutGameView(game);
            view.setVisible(true);
        });

    }
}