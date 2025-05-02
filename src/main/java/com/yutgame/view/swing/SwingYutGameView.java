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
    private boolean isRandomThrow = false;
    private boolean containsStart = false;

    public SwingYutGameView() {
        // --- 게임 세팅 다이얼로그 (이전 YutGame.initializeGame 역할) ---
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

        Integer pieceCount = null;
        while (pieceCount == null || pieceCount < 2 || pieceCount > 5) {
            String input = JOptionPane.showInputDialog(
                    null,
                    "각 플레이어의 말 개수를 입력하세요 (2~5):",
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

        List<Player> players = new ArrayList<>();
        for (int i = 1; i <= playerCount; i++) {
            Player player = new Player("P" + i, new ArrayList<>());
            for (int j = 0; j < pieceCount; j++) {
                Piece piece = new Piece(player);
                player.getPieces().add(piece);
            }
            players.add(player);
        }

        YutBoard board = SquareBoard.createStandardBoard();
        this.game = new YutGame(players, board);
        // ----------------------------------------------------------

        // --- UI 세팅 (기존 SwingYutGameView 생성자 본문) ---
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
        // ----------------------------------------------------------
    }

    private void initButtonListeners() {
        randomThrowButton.addActionListener(e -> {
            isRandomThrow = true;
            YutThrowResult selected = game.throwYutRandom();
            processAllThrows(selected);
        });

        manualThrowButton.addActionListener(e -> {
            isRandomThrow = false;
            YutThrowResult selected = getSetYutResult();
            processAllThrows(selected);
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
        || game.getLastThrowResult() == YutThrowResult.MO) {
            YutThrowResult nextResult;
            if (isRandomThrow) {
                nextResult = game.throwYutRandom();
            } else {
                nextResult = getSetYutResult();
            }
            results.add(nextResult);
        }
        applyThrowSelections(results);
    }

    private YutThrowResult getSetYutResult() {
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
        return sel;
    } // 지정 윷 던지기

    /**
     * 누적된 결과들에 대해 차례로 말/경로 선택 후 이동 처리
     */
    private void applyThrowSelections(List<YutThrowResult> results) {
        Player currentPlayer = game.getCurrentPlayer();

        // 첫 번째 결과가 빽도이고, 모든 말이 아직 출발하지 않은 경우 → 턴 넘기기
        if (results.size() == 1 && results.get(0) == YutThrowResult.BAK_DO) {
            boolean allUnstarted = currentPlayer.getPieces().stream()
                    .allMatch(p -> p.getCurrentNode() == null);
            if (allUnstarted) {
                JOptionPane.showMessageDialog(this, "출발하지 않은 상태에서는 빽도를 사용할 수 없습니다. 턴을 넘깁니다.");
                game.nextTurn();
                boardPanel.repaint();
                return;
            }
        }

        for (YutThrowResult result : results) {
            JOptionPane.showMessageDialog(this, "던진 윷 결과: " + result);
            if (results.size() > 1 && (result == YUT || result == YutThrowResult.MO)) {
                JOptionPane.showMessageDialog(this, "윷을 한 번 더 던지세요.");
            }
        }
        if (results.size() > 1) {
            while (!results.isEmpty()) {
                String[] options = results.stream()
                        .map(Enum::name)
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
                moveNode(currentPlayer, chosenResult);
            }


        } else moveNode(currentPlayer, results.get(0));

        if (game.isGameOver()) {
            JOptionPane.showMessageDialog(this, "승리자: " + game.getWinner().getName());
        } else {
            game.nextTurn();
        }
    }

    private void moveNode(Player currentPlayer, YutThrowResult chosenResult) {
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
                if (dest != null) game.movePiece(selected, dest, containsStart);
            } else {
                List<BoardNode> cans = game.getBoard().getPossibleNextNodes(curr, steps);
                List<BoardNode> path = game.getBoard().getPaths();

                BoardNode dest;
                if (isCrossroad(curr) && cans.size() > 1) {
                    dest = chooseDestination(cans, "갈림길 선택");
                } else {
                    dest = cans.isEmpty() ? null : cans.get(0);
                }
                if (dest != null) {
                    // 완주 처리 관련 로직 : path에 start가 있는가
                    String destId = dest.getId();  // 선택된 목적지 노드의 ID
                    int destIndex = -1;

                    for (int i = 0; i < path.size(); i++) {
                        if (destId.equals(path.get(i).getId())) {
                            destIndex = i;
                            break;
                        }
                    }
                    if (destIndex >= 0 && destIndex == steps -1) {   // 갈림길 1 선택
                        List<BoardNode> trimmed = new ArrayList<>(path.subList(0, steps));
                        path.clear();
                        path.addAll(trimmed);
                    } else if (destIndex >= 0 && destIndex > steps -1) {  // 이외의 갈림길 선택
                        List<BoardNode> trimmed = new ArrayList<>(path.subList(destIndex - steps + 1, destIndex + 1));
                        path.clear();
                        path.addAll(trimmed);
                    } else {
                        System.err.println("dest가 path에 없거나 steps 길이가 부족함.");
                    }
                    // 콘솔 출력용 추가
                    /*
                    * System.out.println("노드 탐색 결과 (선택 길)");
                    for (BoardNode cur : path) {
                        System.out.println(cur.getId());
                    }*/
                    containsStart = path.stream()
                            .anyMatch(node -> "START_NODE".equals(node.getId()));
                    // System.out.println("START_NODE 포함 여부: " + containsStart);  //디버깅용
                    game.getBoard().pathClear();

                    game.movePiece(selected, dest, containsStart);
                }
            }
            boardPanel.repaint();
        }
    }

    private boolean isCrossroad(BoardNode node) {
        String id = node.getId();
        return "CORNER_NE".equals(id) || "CORNER_NW".equals(id) || "CENTER_NODE".equals(id);
    }

    private Piece selectPiece(Player player) {
        List<Piece> allPieces = player.getPieces();

        // 완주한 말 제외
        List<Piece> nonfinished = new ArrayList<>();
        for (Piece p : allPieces) {
            if (!p.isFinished()) {
                nonfinished.add(p);
            }
        }

        // 현재 윷 결과가 빽도인지 확인
        boolean isBakdo = game.getLastThrowResult() == YutThrowResult.BAK_DO;

        // 빽도일 경우 출발한 말만 필터링
        List<Piece> selectable = new ArrayList<>();
        for (Piece p : nonfinished) {
            if (!isBakdo || p.getCurrentNode() != null) {
                selectable.add(p);
            }
        }

        // 선택할 수 있는 말이 없는 경우
        if (selectable.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    isBakdo ? "출발하지 않은 상태에서는 빽도를 사용할 수 없습니다. 턴을 넘깁니다."
                            : "이 플레이어는 말이 없습니다.",
                    "선택 불가",
                    JOptionPane.WARNING_MESSAGE);
            game.nextTurn(); // 턴 넘김
            boardPanel.repaint();
            return null;
        }

        // 선택지 구성
        String[] descs = new String[selectable.size()];
        for (int i = 0; i < selectable.size(); i++) {
            BoardNode cn = selectable.get(i).getCurrentNode();
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

        return (ch < 0 || ch >= selectable.size()) ? null : selectable.get(ch);
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
