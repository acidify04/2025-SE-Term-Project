package main.java.com.yutgame.controller;

import main.java.com.yutgame.model.*;

import java.util.ArrayList;
import java.util.List;

// 게임 보드 및 게임 객체(YutGame)를 생성
public class YutGameFactory {
    public static YutGame createGame(int playerCount, int pieceCount, int boardChoice) {
        List<Player> players = new ArrayList<>();
        for (int i = 1; i <= playerCount; i++) {
            Player player = new Player("P" + i, new ArrayList<>());
            for (int j = 0; j < pieceCount; j++) {
                Piece piece = new Piece(player);
                player.getPieces().add(piece);
            }
            players.add(player);
        }

        YutBoard board = switch (boardChoice) {
            case 0 -> SquareBoard.createStandardBoard();
            case 1 -> PentagonBoard.createPentagonBoard();
            case 2 -> HexagonBoard.createHexagonBoard();
            default -> throw new IllegalArgumentException("보드 선택이 잘못되었습니다.");
        };

        YutGame game = new YutGame();
        game.setBoard(board);
        game.setPlayers(players);

        return game;
    }
}