package main.java.com.yutgame.view.swing;

import main.java.com.yutgame.model.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Swing 윷놀이 메인 프레임
 * - 상단에 "랜덤 윷 던지기 / 지정 윷 던지기" 버튼
 * - 중앙에 BoardPanel
 */
public class SwingYutGameView extends JFrame {

    private YutGame game;
    private BoardPanel boardPanel;

    private JButton randomThrowButton; // 랜덤 윷 던지기
    private JButton manualThrowButton; // 지정 윷 던지기

    public SwingYutGameView(YutGame game) {
        this.game = game;

        setTitle("Swing Yut Game");
        setSize(700, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 상단 버튼 영역
        JPanel topPanel = new JPanel();
        randomThrowButton = new JButton("랜덤 윷 던지기");
        manualThrowButton = new JButton("지정 윷 던지기");
        topPanel.add(randomThrowButton);
        topPanel.add(manualThrowButton);
        add(topPanel, BorderLayout.NORTH);

        // board 영역
        boardPanel = new BoardPanel(game);
        add(boardPanel, BorderLayout.CENTER);

        initButtonListeners();

        // 게임 시작
        game.startGame();
    }

    private void initButtonListeners() {
        // 랜덤 윷 던지기
        randomThrowButton.addActionListener(e -> {
            YutThrowResult result = game.throwYutRandom();
            JOptionPane.showMessageDialog(
                    SwingYutGameView.this,
                    "던진 윷 결과: " + result,
                    "윷 결과",
                    JOptionPane.INFORMATION_MESSAGE
            );
            afterThrow(result);
        });

        // 지정 윷 던지기
        manualThrowButton.addActionListener(e -> {
            String[] options = {"백도", "도", "개", "걸", "윷", "모"};
            int choice = JOptionPane.showOptionDialog(
                    SwingYutGameView.this,
                    "결과를 선택하세요",
                    "지정 윷 던지기",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    options,
                    options[1]
            );

            YutThrowResult selected = switch(choice) {
                case 0 -> YutThrowResult.BAK_DO;
                case 1 -> YutThrowResult.DO;
                case 2 -> YutThrowResult.GAE;
                case 3 -> YutThrowResult.GEOL;
                case 4 -> YutThrowResult.YUT;
                case 5 -> YutThrowResult.MO;
                default -> YutThrowResult.DO;
            };
            game.throwYutManual(selected);
            JOptionPane.showMessageDialog(
                    SwingYutGameView.this,
                    "던진 윷 결과: " + selected,
                    "윷 결과",
                    JOptionPane.INFORMATION_MESSAGE
            );
            afterThrow(selected);
        });
    }

    /**
     * 윷 결과가 나오면 말 이동
     * 1. 이동시킬 말 선택
     * 2. 갈림길이 있을 경우 갈림길 선택
     * 3. 말 이동(movePiece)
     */
    private void afterThrow(YutThrowResult result) {
        // 현재 플레이어
        Player currentPlayer = game.getCurrentPlayer();

        // 이동시킬 말 선택
        Piece selectedPiece = choosePiece(currentPlayer);
        if (selectedPiece == null) {
            // 말이 없거나 선택 취소
            return;
        }

        // 이동 칸수 추출
        int steps = switch (result) {
            case BAK_DO -> -1;
            case DO -> 1;
            case GAE -> 2;
            case GEOL -> 3;
            case YUT -> 4;
            case MO -> 5;
        };

        // 이동시킬 말의 현재 위치
        BoardNode currentNode = selectedPiece.getCurrentNode();
        if (currentNode == null) {
            currentNode = game.getBoard().getStartNode();
        }

        // 백도 처리
        if (steps < 0) {
            List<BoardNode> previous = game.getBoard().getPossiblePreviousNodes(currentNode);

            if (previous.isEmpty()) {
                JOptionPane.showMessageDialog(this, "백도 불가");
                return;
            }
            // 갈림길이 있을 경우
            BoardNode chosenPreviousNode = chooseDestination(previous, "백도 갈림길 선택");
            if (chosenPreviousNode != null) {
                game.movePiece(selectedPiece, chosenPreviousNode);
            }
        } else { // 백도 이외
            List<BoardNode> possibleNodes = game.getBoard().getPossibleNextNodes(currentNode, steps);
            if (possibleNodes.isEmpty()) {
                JOptionPane.showMessageDialog(this, "이동할 수 있는 경로가 없습니다.");
                return;
            }
            BoardNode chosenNextNode = chooseDestination(possibleNodes, "갈림길 선택");
            if (chosenNextNode != null) {
                game.movePiece(selectedPiece, chosenNextNode);
            }
        }

        // 승리 여부 확인
        if (game.isGameOver()) {
            JOptionPane.showMessageDialog(
                    this,
                    "승리자: " + game.getWinner().getName(),
                    "게임 종료",
                    JOptionPane.INFORMATION_MESSAGE
            );
            // 재시작 or 종료
            int retry = JOptionPane.showConfirmDialog(
                    this,
                    "다시 시작하시겠습니까?",
                    "재시작",
                    JOptionPane.YES_NO_OPTION
            );
            if (retry == JOptionPane.YES_OPTION) {
                game.resetGame();
            } else {
                System.exit(0);
            }
        } else {
            // 턴 종료
            game.nextTurn();
        }
        boardPanel.repaint();
    }

    /**
     * 현재 플레이어의 말 목록 중 하나를 선택시키는 Dialog.
     */
    private Piece choosePiece(Player player) {
        List<Piece> pieces = player.getPieces();
        if (pieces.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this, "이 플레이어는 말이 없습니다.",
                    "선택 불가", JOptionPane.WARNING_MESSAGE
            );
            return null;
        }

        // 말의 위치 정보 표시
        String[] pieceDescs = new String[pieces.size()];
        for (int i = 0; i < pieces.size(); i++) {
            Piece p = pieces.get(i);
            BoardNode node = p.getCurrentNode();
            String loc = (node == null) ? "미출발" : node.getId();
            pieceDescs[i] = "말 " + i + " (위치: " + loc + ")";
        }

        int choice = JOptionPane.showOptionDialog(
                this,
                "이동할 말을 선택하세요 (" + player.getName() + ")",
                "말 선택",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                pieceDescs,
                pieceDescs[0]
        );

        if (choice < 0 || choice >= pieces.size()) {
            return null; // 취소
        }
        return pieces.get(choice);
    }

    /**
     * 갈림길 후보가 여러 개일 때, 사용자가 선택하게 함
     */
    private BoardNode chooseDestination(List<BoardNode> candidates, String title) {
        if (candidates.size() == 1) {
            // 갈림길이 아닌 경우
            return candidates.get(0);
        }
        if (candidates.isEmpty()) {
            // 이동 경로가 없는 경우
            JOptionPane.showMessageDialog(this, "이동할 경로가 없습니다.", "오류", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        // 경로가 여러 개인 경우
        String[] options = new String[candidates.size()];
        for (int i = 0; i < candidates.size(); i++) {
            options[i] = candidates.get(i).getId();
        }
        int choice = JOptionPane.showOptionDialog(
                this,
                "갈림길입니다. 이동할 경로를 고르세요.",
                "갈림길 선택",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice < 0 || choice >= candidates.size()) {
            return null;
        }
        return candidates.get(choice);
    }
}
