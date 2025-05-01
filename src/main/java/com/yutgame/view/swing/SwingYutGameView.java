package main.java.com.yutgame.view.swing;

import main.java.com.yutgame.model.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Swing 윷놀이 메인 프레임 (MVC의 View+Controller 성격)
 * - 빽도(-1) 처리 개선
 */
public class SwingYutGameView extends JFrame {

    private YutGame game;
    private BoardPanel boardPanel;

    private JButton randomThrowButton;
    private JButton manualThrowButton;

    public SwingYutGameView(YutGame game) {
        this.game = game;

        setTitle("Swing Yut Game");

        setSize(700, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();
        randomThrowButton = new JButton("랜덤 윷 던지기");
        manualThrowButton = new JButton("지정 윷 던지기");
        topPanel.add(randomThrowButton);
        topPanel.add(manualThrowButton);

        add(topPanel, BorderLayout.NORTH);

        boardPanel = new BoardPanel(game);
        add(boardPanel, BorderLayout.CENTER);

        initButtonListeners();

        // 게임 시작
        game.startGame();
    }

    private void initButtonListeners() {
        randomThrowButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                YutThrowResult result = game.throwYutRandom();
                JOptionPane.showMessageDialog(
                        SwingYutGameView.this,
                        "던진 윷 결과: " + result,
                        "윷 결과",
                        JOptionPane.INFORMATION_MESSAGE
                );
                afterThrow(result);
            }
        });

        manualThrowButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String[] options = {"빽도", "도", "개", "걸", "윷", "모"};
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
                YutThrowResult sel = switch (choice) {
                    case 0 -> YutThrowResult.BAK_DO;
                    case 1 -> YutThrowResult.DO;
                    case 2 -> YutThrowResult.GAE;
                    case 3 -> YutThrowResult.GEOL;
                    case 4 -> YutThrowResult.YUT;
                    case 5 -> YutThrowResult.MO;
                    default -> YutThrowResult.DO;
                };
                game.throwYutManual(sel);
                JOptionPane.showMessageDialog(
                        SwingYutGameView.this,
                        "던진 윷 결과: " + sel,
                        "윷 결과",
                        JOptionPane.INFORMATION_MESSAGE
                );
                afterThrow(sel);
            }
        });
    }

    /**
     * 윷 결과 이후 말 이동 처리 -> 말 선택 -> 갈림길 선택 -> movePiece
     */
    private void afterThrow(YutThrowResult result) {
        Player currentPlayer = game.getCurrentPlayer();
        Piece selectedPiece = selectPiece(currentPlayer);
        if (selectedPiece == null) {
            return;
        }

        int steps = switch (result) {
            case BAK_DO -> -1;
            case DO -> 1;
            case GAE -> 2;
            case GEOL -> 3;
            case YUT -> 4;
            case MO -> 5;
        };

        BoardNode curr = selectedPiece.getCurrentNode();
        if (curr == null) {
            curr = game.getBoard().getStartNode();
        }

        // **빽도(-1) 처리 개선**
        if (steps < 0) {
            List<BoardNode> prevs = game.getBoard().getPossiblePreviousNodes(curr);
            if (prevs.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "빽도 불가 (이전 노드 없음)",
                        "알림",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }
            // 후보가 하나면 바로, 여러 개면 선택
            BoardNode dest = prevs.size() == 1
                    ? prevs.get(0)
                    : chooseDestination(prevs, "빽도 이동");
            if (dest != null) {
                game.movePiece(selectedPiece, dest);
            }

        } else {
            // 이하 기존 전진 처리 로직(생략)
            List<BoardNode> candidates = game.getBoard().getPossibleNextNodes(curr, steps);
            if (candidates.isEmpty()) {
                JOptionPane.showMessageDialog(this, "이동할 수 있는 경로가 없습니다.", "알림", JOptionPane.ERROR_MESSAGE);
                return;
            }
            BoardNode chosen = chooseDestination(candidates, "갈림길 선택");
            if (chosen != null) {
                game.movePiece(selectedPiece, chosen);
            }
        }

        // 승리 여부 및 턴 전환
        if (game.isGameOver()) {
            JOptionPane.showMessageDialog(
                    this,
                    "승리자: " + game.getWinner().getName(),
                    "게임 종료",
                    JOptionPane.INFORMATION_MESSAGE
            );
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
            game.nextTurn();
        }
        boardPanel.repaint();
    }

    private Piece selectPiece(Player player) {
        List<Piece> plist = player.getPieces();
        if (plist.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this, "이 플레이어는 말이 없습니다.",
                    "선택 불가", JOptionPane.WARNING_MESSAGE
            );
            return null;
        }
        String[] descs = new String[plist.size()];
        for (int i = 0; i < plist.size(); i++) {
            BoardNode cn = plist.get(i).getCurrentNode();
            String loc = (cn == null) ? "미출발" : cn.getId();
            descs[i] = "말" + i + "(" + loc + ")";
        }

        int ch = JOptionPane.showOptionDialog(
                this,
                "이동할 말을 선택하세요 (" + player.getName() + ")",
                "말 선택",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                descs,
                descs[0]
        );
        if (ch < 0 || ch >= plist.size()) {
            return null;
        }
        return plist.get(ch);
    }

    private BoardNode chooseDestination(List<BoardNode> candidates, String title) {
        if (candidates.size() == 1) {
            return candidates.get(0);
        }
        String[] opts = new String[candidates.size()];
        for (int i = 0; i < candidates.size(); i++) {
            opts[i] = candidates.get(i).getId();
        }
        int ch = JOptionPane.showOptionDialog(
                this,
                "이동할 노드를 선택하세요",
                title,
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                opts,
                opts[0]
        );
        if (ch < 0 || ch >= candidates.size()) {
            return null;
        }
        return candidates.get(ch);
    }
}