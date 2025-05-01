package main.java.com.yutgame.view.swing;

import main.java.com.yutgame.model.*;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static main.java.com.yutgame.model.YutThrowResult.*;

public class SwingYutGameView extends JFrame {

    private YutGame game;
    private BoardPanel boardPanel;
    private JButton randomThrowButton;
    private JButton manualThrowButton;

    public SwingYutGameView(YutGame game) {
        this.game = game;

        setTitle("Swing Yut Game");
        setSize(700, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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

        game.startGame();
    }

    private void initButtonListeners() {
        randomThrowButton.addActionListener(e -> {
            YutThrowResult first = game.throwYutRandom();
            processAllThrows(first);
        });

        manualThrowButton.addActionListener(e -> {
            String[] options = {"빽도", "도", "개", "걸", "윷", "모"};
            int choice = JOptionPane.showOptionDialog(
                    this,
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
                case 2 -> GAE;
                case 3 -> GEOL;
                case 4 -> YUT;
                case 5 -> YutThrowResult.MO;
                default -> YutThrowResult.DO;
            };
            game.throwYutManual(sel);
            processAllThrows(sel);
        });
    }

    /**
     * 윷·모가 나올 때까지 계속 던지고, 최종 결과 리스트를 반환
     */
    private void processAllThrows(YutThrowResult firstResult) {
        List<YutThrowResult> results = new ArrayList<>();
        results.add(firstResult);
        // 윷, 모 또는 잡기까지 연속 던지기
        while (game.getLastThrowResult() == YUT
        || game.getLastThrowResult() == YutThrowResult.MO
        || game.hasExtraTurnFlag()) {
            YutThrowResult nextResult = game.throwYutRandom();
            results.add(nextResult);
        }
        for (YutThrowResult result : results) {
            System.out.println("리스트에 저장된 것" + result);
        }
        applyThrowSelections(results);
    }

    /**
     * 누적된 결과들에 대해 차례로 말/경로 선택 후 이동 처리
     */
    private void applyThrowSelections(List<YutThrowResult> results) {
        Player currentPlayer = game.getCurrentPlayer();
        //boolean isFirst = (results.get(0) == YutThrowResult.YUT || results.get(0) == YutThrowResult.MO);
        for (YutThrowResult result : results) {
            System.out.println(results.size());
            System.out.println("현재 실행하는 것" + result);
            JOptionPane.showMessageDialog(this, "던진 윷 결과: " + result);
            if (results.size() > 1 && (result == YUT || result == YutThrowResult.MO)) {
                JOptionPane.showMessageDialog(this, "윷을 한 번 더 던지세요.");
            }
//            for (int i = 1; i < results.size(); i++) {
//                if (i > 1) {
//                    JOptionPane.showMessageDialog(this, "윷을 한 번 더 던지세요.");
//                }
//                JOptionPane.showMessageDialog(this, "던진 윷 결과: " + results.get(i));
//            }
        }
        if (results.size() > 1) {
            while (!results.isEmpty()) {
                String[] options = results.stream()
                        .map(Enum::name) // 또는 .map(e -> e.toString()) - 커스터마이즈 했으면 toString 추천
                        .toArray(String[]::new);

                int choice = JOptionPane.showOptionDialog(
                        this,
                        "몇 칸 이동하시겠습니까?",
                        "이동 선택",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        options,
                        options[0]
                );
                YutThrowResult chosenResult = results.remove(choice);
                Piece selected = selectPiece(currentPlayer);
                if (selected != null) {
                    int steps = switch (chosenResult) {
                        case BAK_DO -> -1;
                        case DO      -> 1;
                        case GAE     -> 2;
                        case GEOL    -> 3;
                        case YUT     -> 4;
                        case MO      -> 5;
                    };

                    BoardNode curr = selected.getCurrentNode();
                    if (curr == null) curr = game.getBoard().getStartNode();

                    if (steps < 0) {
                        List<BoardNode> prevs = game.getBoard().getPossiblePreviousNodes(curr);
                        BoardNode dest = prevs.size() == 1 ? prevs.get(0) : chooseDestination(prevs, "빽도 이동");
                        if (dest != null) game.movePiece(selected, dest);
                    } else {
                        List<BoardNode> cans = game.getBoard().getPossibleNextNodes(curr, steps);
                        BoardNode dest;
                        if (isCrossroad(curr) && cans.size() > 1) {
                            dest = chooseDestination(cans, "갈림길 선택");
                        } else {
                            dest = cans.isEmpty() ? null : cans.get(0);
                        }
                        if (dest != null) game.movePiece(selected, dest);
                    }
                    boardPanel.repaint();
                }
            }


        } else {
            Piece selected = selectPiece(currentPlayer);
            if (selected != null) {
                int steps = switch (results.get(0)) {
                    case BAK_DO -> -1;
                    case DO      -> 1;
                    case GAE     -> 2;
                    case GEOL    -> 3;
                    case YUT     -> 4;
                    case MO      -> 5;
                };

                BoardNode curr = selected.getCurrentNode();
                if (curr == null) curr = game.getBoard().getStartNode();

                if (steps < 0) {
                    List<BoardNode> prevs = game.getBoard().getPossiblePreviousNodes(curr);
                    BoardNode dest = prevs.size() == 1 ? prevs.get(0) : chooseDestination(prevs, "빽도 이동");
                    if (dest != null) game.movePiece(selected, dest);
                } else {
                    List<BoardNode> cans = game.getBoard().getPossibleNextNodes(curr, steps);
                    BoardNode dest;
                    if (isCrossroad(curr) && cans.size() > 1) {
                        dest = chooseDestination(cans, "갈림길 선택");
                    } else {
                        dest = cans.isEmpty() ? null : cans.get(0);
                    }
                    if (dest != null) game.movePiece(selected, dest);
                }
                boardPanel.repaint();
            }
        }

        if (game.isGameOver()) {
            JOptionPane.showMessageDialog(this, "승리자: " + game.getWinner().getName());
        } else {
            game.nextTurn();
        }
    }

    private boolean isCrossroad(BoardNode node) {
        String id = node.getId();
        return "CORNER_NE".equals(id) || "CORNER_NW".equals(id) || "CENTER_NODE".equals(id);
    }

    private Piece selectPiece(Player player) {
        List<Piece> plist = player.getPieces();
        if (plist.isEmpty()) {
            JOptionPane.showMessageDialog(this, "이 플레이어는 말이 없습니다.", "선택 불가", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        String[] descs = new String[plist.size()];
        for (int i = 0; i < plist.size(); i++) {
            BoardNode cn = plist.get(i).getCurrentNode();
            descs[i] = "말" + i + "(" + (cn == null ? "미출발" : cn.getId()) + ")";
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
        return (ch < 0 || ch >= plist.size()) ? null : plist.get(ch);
    }

    private BoardNode chooseDestination(List<BoardNode> cands, String title) {
        if (cands.size() == 1) return cands.get(0);
        String[] opts = cands.stream().map(BoardNode::getId).toArray(String[]::new);
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
        return (ch < 0 || ch >= cands.size()) ? null : cands.get(ch);
    }
}
