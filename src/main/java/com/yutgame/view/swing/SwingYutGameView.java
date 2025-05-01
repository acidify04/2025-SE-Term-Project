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
        setSize(700, 800);
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
     * 윷을 던진 뒤, 현재 플레이어가 어떤 말을 이동할지 선택하고
     * 갈림길이 있으면 방향을 물어본 뒤 movePiece(...)를 실행한다.
     */
    private void afterThrow(YutThrowResult result) {
        // 현재 플레이어
        Player currentPlayer = game.getCurrentPlayer();

        // (A) 말 선택
        Piece selectedPiece = askPieceSelection(currentPlayer);
        if (selectedPiece == null) {
            // 말이 없거나 선택 취소
            return;
        }

        // (B) 이동 칸수 추출
        int steps = getStepsFromResult(result);

        // (C) 갈림길 있는지 확인
        BoardNode currentNode = selectedPiece.getCurrentNode();
        if (currentNode == null) {
            currentNode = game.getBoard().getStartNode();
        }
        List<BoardNode> possibleDests = game.getBoard().getPossibleNextNodes(currentNode, steps);
        BoardNode chosenNode = chooseForkDestination(possibleDests);
        if (chosenNode == null) {
            // 사용자가 취소했거나 경로 없음
            return;
        }

        // (D) 이동 -> 실제는 game.movePiece(...)가 잡기/업기/골인 처리
        // 여기서는 갈림길만 선택하고, movePiece에 맡긴다
        game.movePiece(selectedPiece, chosenNode);
        // **주의**: 위에서 chosenNode를 직접 이동해도 되지만,
        // MVC 관점에서는 game 내부 로직(갈림길 선택)을 한 번 더 조정할 수 있음.
        // 간단히 여기서는 "첫번째 갈림길만" 이동이라면 chosenNode 쪽 커스텀 구현 가능.

        // (E) 승리 여부 확인
        if (game.isGameOver()) {
            JOptionPane.showMessageDialog(
                    this,
                    "승리자: " + game.getWinner().getName(),
                    "게임 종료",
                    JOptionPane.INFORMATION_MESSAGE
            );
            // 재시작 or 종료
            int ret = JOptionPane.showConfirmDialog(
                    this,
                    "다시 시작하시겠습니까?",
                    "재시작",
                    JOptionPane.YES_NO_OPTION
            );
            if (ret == JOptionPane.YES_OPTION) {
                game.resetGame();
            } else {
                System.exit(0);
            }
        } else {
            // 턴 종료 -> nextTurn
            game.nextTurn();
        }

        boardPanel.repaint();
    }

    /**
     * 현재 플레이어의 말 목록 중 하나를 선택시키는 Dialog.
     */
    private Piece askPieceSelection(Player player) {
        List<Piece> pieces = player.getPieces();
        if (pieces.isEmpty()) {
            JOptionPane.showMessageDialog(this, "이 플레이어는 말이 없습니다.", "알림", JOptionPane.WARNING_MESSAGE);
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
                "이동할 말을 선택하세요",
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
     * 갈림길 후보가 여러 개일 때, 사용자에게 경로 선택을 묻는다.
     */
    private BoardNode chooseForkDestination(List<BoardNode> candidates) {
        if (candidates.isEmpty()) {
            JOptionPane.showMessageDialog(this, "이동할 경로가 없습니다.", "오류", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        if (candidates.size() == 1) {
            // 갈림길 아님
            return candidates.get(0);
        }

        // 여러 개 경로 -> 사용자에게 선택 Dialog
        String[] options = new String[candidates.size()];
        for (int i = 0; i < candidates.size(); i++) {
            options[i] = candidates.get(i).getId(); // 혹은 "노드명(x,y)"
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

    /**
     * YutThrowResult -> 이동 칸 수로 변환.
     */
    private int getStepsFromResult(YutThrowResult result) {
        return switch (result) {
            case BAK_DO -> -1;
            case DO -> 1;
            case GAE -> 2;
            case GEOL -> 3;
            case YUT -> 4;
            case MO -> 5;
        };
    }
}
