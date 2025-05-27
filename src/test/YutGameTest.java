package test;

import main.java.com.yutgame.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class YutGameTest {

    private YutGame game;
    private Player player1;
    private Player player2;
    private YutBoard board;

    @BeforeEach
    void setUp() { // 게임 세팅
        game = new YutGame();

        // 간단한 시작 노드 설정
        //board = SquareBoard.createStandardBoard();
        //board = PentagonBoard.createPentagonBoard();
        board = HexagonBoard.createHexagonBoard();


        // 간단한 플레이어 + 말 설정
        List<Player> players = new ArrayList<>();
        for (int i = 1; i <= 2; i++) {
            Player player = new Player("P" + i, new ArrayList<>());
            for (int j = 0; j < 4; j++) {
                Piece piece = new Piece(player);
                player.getPieces().add(piece);
            }
            players.add(player);
        }
        player1 = players.get(0);
        player2 = players.get(1);

        game.setBoard(board);
        game.setPlayers(players);
        game.startGame();
    }

    @Test
    void testThrowYutRandom_NotNullAndValid() { // 윷을 랜덤으로 던졌을 때 결과는 null이 아니고 YutThrowResult 중 하나여야 함
        YutThrowResult result = game.throwYutRandom();
        assertNotNull(result);
        assertTrue(EnumSet.allOf(YutThrowResult.class).contains(result));
    }

    @Test
    void testNextTurn_ChangesPlayer() { // 윷을 던진 후에는 턴이 넘어가야 함
        int initialIndex = getCurrentPlayerIndex();
        game.setLastThrowResult(YutThrowResult.GAE);
        game.nextTurn();
        assertNotEquals(initialIndex, getCurrentPlayerIndex());
    }

    @Test
    void testNextTurn_YutKeepsTurn() { // 윷이나 모가 나온 후에는 턴이 유지되어야 함
        int initialIndex = getCurrentPlayerIndex();
        game.setLastThrowResult(YutThrowResult.YUT);
        game.nextTurn();
        assertEquals(initialIndex, getCurrentPlayerIndex());
    }

    @Test
    void testCapturePiece_ShouldResetOpponentPieceToStart() { // 말을 잡은 경우 잡힌 말은 보드에서 제거 및 턴 유지
        Piece attacker = player1.getPieces().get(0); // 잡는 말
        Piece victim = player2.getPieces().get(0); // 잡히는 말

        // victim이 먼저 이동해서 말 하나 올라감
        BoardNode e1 = new BoardNode("E1", 50, 450);
        game.movePiece(victim, e1, false);
        BoardNode occupiedNode = victim.getCurrentNode();
        assertEquals(1, occupiedNode.getOccupantPieces().size());
        assertTrue(occupiedNode.getOccupantPieces().contains(victim));

        int previousIndex = getCurrentPlayerIndex();
        // attacker도 같은 위치로 이동해서 capture 발생
        game.movePiece(attacker, e1, false);
        game.nextTurn();
        int nextIndex = getCurrentPlayerIndex();

        assertNull(victim.getCurrentNode()); // victim은 보드에서 제거
        assertFalse(occupiedNode.getOccupantPieces().contains(victim));
        assertEquals(previousIndex, nextIndex); // 턴이 유지되어야 함

    }

    @Test
    void testGroupingPieces_ShouldGroupTwoSamePlayerPieces() { // 같은 팀의 말을 업은 경우
        Piece piece1 = player1.getPieces().get(0);
        Piece piece2 = player1.getPieces().get(1);

        BoardNode e1 = new BoardNode("E1", 50, 450);

        // piece1과 piece2를 같은 위치로 이동시킴
        game.movePiece(piece1, e1, false); // 먼저 도착
        game.movePiece(piece2, e1, false); // 나중에 도착해서 업힘

        // 둘 다 같은 노드에 있는지 확인
        BoardNode node = piece1.getCurrentNode();
        assertEquals(node, piece2.getCurrentNode());

        // 그룹핑되었는지 확인
        assertTrue(piece1.isGroup() && piece2.isGroup());
        assertTrue(piece1.getGroupedPieces().contains(piece2) || piece2.getGroupedPieces().contains(piece1));
    }

    @Test
    void testGroupedPiecesMoveTogether() { // 업은 말들이 함께 움직여야 함
        Piece piece1 = player1.getPieces().get(0);
        Piece piece2 = player1.getPieces().get(1);

        BoardNode e1 = new BoardNode("E1", 50, 450);
        // piece1과 piece2를 같은 위치로 이동시켜 그룹핑
        game.movePiece(piece1, e1, false);
        game.movePiece(piece2, e1, false);

        // 그룹핑되었는지 먼저 체크
        assertEquals(piece1.getCurrentNode(), piece2.getCurrentNode());
        assertTrue(piece1.isGroup() || piece2.isGroup());

        // 그룹핑된 상태에서 piece1을 움직이면 piece2도 같이 움직임
        BoardNode beforeMove = piece1.getCurrentNode();
        BoardNode e2 = new BoardNode("E2", 50, 350);
        game.movePiece(piece1, e2, false); // 그룹으로 이동

        assertNotEquals(beforeMove, piece1.getCurrentNode());
        assertEquals(piece1.getCurrentNode(), piece2.getCurrentNode());
    }


    @Test
    void testGameOverCondition_AllPiecesFinished() { // 모든 말이 완주한 경우 게임 종료 및 승자 체크
        for (Piece p : player1.getPieces()) {
            p.setFinished(true);
        }
        game.checkWinCondition();
        assertTrue(game.isGameOver());
        assertEquals(player1, game.getWinner());
    }

    @Test
    void testResetGame_ResetsAllPieces() { // 게임 재시작 시 게임 초기화
        for (Piece p : player1.getPieces()) {
            p.setFinished(true);
            p.moveTo(new BoardNode("Dummy"));
        }

        game.resetGame();

        for (Piece p : player1.getPieces()) {
            assertFalse(p.isFinished());
            assertEquals(board.getStartNode(), p.getCurrentNode());
        }
    }

    private int getCurrentPlayerIndex() {
        List<Player> players = game.getPlayers();
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).equals(game.getCurrentPlayer())) {
                return i;
            }
        }
        return -1;
    }
}
