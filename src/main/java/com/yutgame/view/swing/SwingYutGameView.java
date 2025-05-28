package main.java.com.yutgame.view.swing;

import main.java.com.yutgame.controller.YutGameController;
import main.java.com.yutgame.dto.PieceDecisionResult;
import main.java.com.yutgame.model.*;
import main.java.com.yutgame.view.YutGameView;

import javax.swing.*;
import java.awt.*;
import java.util.List;

import static main.java.com.yutgame.model.YutThrowResult.*;

public class SwingYutGameView extends JFrame implements YutGameView {

    private BoardPanel boardPanel;
    private JButton randomThrowButton;
    private JButton manualThrowButton;
    private boolean isRandomThrow = false;
    private YutGameController controller;
    private int playerCount;
    private int pieceCount;
    private int boardChoice;


    public SwingYutGameView() {
        // --- 게임 세팅 다이얼로그 (이전 YutGame.initializeGame 역할) ---
        String[] playerOptions = {"2인", "3인", "4인"};
        String[] pieceOptions = {"2개", "3개", "4개", "5개"};
        String[] boardOptions = {"사각형", "오각형", "육각형"};

        JComboBox<String> playerCombo = new JComboBox<>(playerOptions);
        JComboBox<String> pieceCombo = new JComboBox<>(pieceOptions);
        JComboBox<String> boardCombo = new JComboBox<>(boardOptions);

        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.add(new JLabel("플레이어 수:"));
        panel.add(playerCombo);
        panel.add(new JLabel("말 수:"));
        panel.add(pieceCombo);
        panel.add(new JLabel("보드 형태:"));
        panel.add(boardCombo);

        int result = JOptionPane.showConfirmDialog(
                null,
                panel,
                "기본 설정",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            playerCount = playerCombo.getSelectedIndex() + 2; // 2인부터 시작
            pieceCount = pieceCombo.getSelectedIndex() + 2;   // 2개부터 시작
            boardChoice = boardCombo.getSelectedIndex(); // 선택된 보드 타입

            System.out.println("선택된 플레이어 수: " + playerCount);
            System.out.println("선택된 말 수: " + pieceCount);
            System.out.println("선택된 보드 형태: " + boardChoice);
        } else {
            // 취소 또는 X 누른 경우
            System.exit(0);  // 프로그램 종료
        }

        // --- UI 세팅 (기존 SwingYutGameView 생성자 본문) ---
        setTitle("Swing Yut Game");
        setSize(700, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();
        randomThrowButton = new JButton("랜덤 윷 던지기");
        manualThrowButton = new JButton("지정 윷 던지기");
        topPanel.add(randomThrowButton);
        topPanel.add(manualThrowButton);

        add(topPanel, BorderLayout.NORTH);
        initButtonListeners();
    }

    @Override
    public int getPlayerCount() {
        return playerCount;
    }

    @Override
    public int getPieceCount() {
        return pieceCount;
    }

    @Override
    public int getBoardChoice() {
        return boardChoice;
    }

    private void initButtonListeners() {
        randomThrowButton.addActionListener(e -> {
            isRandomThrow = true;
            YutThrowResult selected = controller.getRandomYut();
            processAllThrows(selected);
        });

        manualThrowButton.addActionListener(e -> {
            isRandomThrow = false;
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
            YutThrowResult selected = controller.getSetYut(choice);
            processAllThrows(selected);
        });
    }

    /**
     * 윷·모가 나올 때까지 계속 던지고, 최종 결과 리스트를 반환 (컨트롤러 이용)
     */
    private void processAllThrows(YutThrowResult firstResult) {
        List<YutThrowResult> results = controller.collectThrowResults(
                firstResult,
                isRandomThrow,
                () -> getSetYutResult(),
                this::showResult,
                () -> JOptionPane.showMessageDialog(this, "윷을 한 번 더 던지세요.")
        );

        applyThrowSelections(results); // 아직은 뷰에 남겨둠
    }


    private void showResult(YutThrowResult result) {
        if (result == BAK_DO) {
            JOptionPane.showMessageDialog(this, "던진 윷 결과: 백도");
        } else if (result == DO) {
            JOptionPane.showMessageDialog(this, "던진 윷 결과: 도");
        } else if (result == GAE) {
            JOptionPane.showMessageDialog(this, "던진 윷 결과: 개");
        } else if (result == GEOL) {
            JOptionPane.showMessageDialog(this, "던진 윷 결과: 걸");
        } else if (result == YUT) {
            JOptionPane.showMessageDialog(this, "던진 윷 결과: 윷");
        } else if (result == MO) {
            JOptionPane.showMessageDialog(this, "던진 윷 결과: 모");
        }
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
        controller.throwYutManual(sel);
        return sel;
    } // 지정 윷 던지기

    /**
     * 누적된 결과들에 대해 차례로 말/경로 선택 후 이동 처리
     */
    private void applyThrowSelections(List<YutThrowResult> results) {
        Player currentPlayer = controller.getCurrentPlayer();

        // controller로 이동

        // 첫 번째 결과가 빽도이고, 모든 말이 아직 출발하지 않은 경우 → 턴 넘기기
        if (results.size() == 1 && results.getFirst() == YutThrowResult.BAK_DO) {
            boolean notStarted = controller.getNotStarted(currentPlayer);

            if (notStarted) {

                JOptionPane.showMessageDialog(this, "출발하지 않은 상태에서는 빽도를 사용할 수 없습니다. 턴을 넘깁니다.");

                controller.nextTurn();
                boardPanel.repaint();
                return;
            }
        }

        // 윷이나 모가 나온 경우 / 잡은 경우
        if (results.size() > 1) { // TODO: 얘 좀 이상함
            while (!results.isEmpty()) {
                // TODO: 한글로 바꾸는 부분 매개변수: results, return -> options String[]
                 String[] options = controller.getChoiceLetters(results);

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

                YutThrowResult chosen = results.get(choice);
                // results > 1이고 빽도가 포함되어 있을 때 처리
                boolean notStarted = controller.getNotStarted(currentPlayer);

                if (chosen == YutThrowResult.BAK_DO && notStarted) {
                    JOptionPane.showMessageDialog(this, "출발하지 않은 상태에서는 빽도를 선택할 수 없습니다.");
                    continue; // 다시 선택창을 띄움
                }


                YutThrowResult chosenResult = results.remove(choice);
                moveNode(currentPlayer, chosenResult);

                if (controller.isGameOver()) {
                    break;
                }
            }

        } else moveNode(currentPlayer, results.get(0));

        if (controller.isGameOver()) {
            JOptionPane.showMessageDialog(this, "승리자: " + controller.getWinner().getName());
            System.exit(0);
        } else {
            controller.nextTurn();
        }
    }

    private void moveNode(Player currentPlayer, YutThrowResult chosenResult) {
        Piece selected = selectPiece(currentPlayer, chosenResult);

        if (selected != null) {
            int steps = controller.getSteps(chosenResult);

            BoardNode curr = selected.getCurrentNode();
            if (curr == null) curr = controller.getBoard().getStartNode();

            if (steps < 0) {
                List<BoardNode> prevs = controller.getBoard().getPossiblePreviousNodes(curr);
                BoardNode dest = prevs.size() == 1 ? prevs.get(0) : chooseDestination(prevs, "빽도 이동", -1);
                if (dest != null) controller.movePiece(selected, dest, controller.getContainsStartNode());
            } else {
                List<BoardNode> cans = controller.getBoard().getPossibleNextNodes(curr, steps);
                List<BoardNode> path = controller.getBoard().getPaths();
                List<List<BoardNode>> paths = controller.splitPath(path, steps);


                int canFinishIndex = controller.checkCanFinishIndex(paths, path);

                BoardNode dest;
                if (controller.isCrossroad(curr) && cans.size() > 1) {
                    dest = chooseDestination(cans, "갈림길 선택", canFinishIndex);
                } else {
                    dest = cans.isEmpty() ? null : cans.get(0);
                }
                if (dest != null) {
                    // 완주 처리 관련 로직 : path에 start가 있는가
                    controller.isFinished(selected, dest, path, steps);
                }
            }
            boardPanel.repaint();
        }
    }



    /**
     * 이동할 말을 선택한다.
     * - 빽도(BAK_DO)일 때는 "뒤로 갈 수 있는 말"만 선택지로 노출
     *   (START_NODE에 있거나 히스토리가 1칸 이하인 말은 제외)
     * - 선택지가 없으면:
     *      · 빽도  → "시작지점에서 빽도를 사용하실 수 없습니다." UI 출력 후 턴 패스
     *      · 그 외 → 기존 안내 후 턴 패스
     */
    private Piece selectPiece(Player player, YutThrowResult chosenResult) {
         PieceDecisionResult pieceDecisionResult  = controller.getPieceDecisions(player, chosenResult);
        List<Piece> choices = pieceDecisionResult.choices();
        List<String> pieceDecisions = pieceDecisionResult.decisions();


        if (controller.allPiecesFinished(player)){
            controller.checkWin();
            if (controller.isGameOver()) {
                JOptionPane.showMessageDialog(this, "승리자: " + controller.getWinner().getName());
                System.exit(0);
            }
            return null;
        }else {
            /* 2) 선택 가능한 말이 없다 → UI 메시지 & 턴 패스 */
            if (pieceDecisions.isEmpty()) {
                String msg = controller.checkBaekdo(chosenResult)
                        ? "시작지점에서 빽도를 사용하실 수 없습니다."
                        : "이 플레이어는 이동 가능한 말이 없습니다.";
                JOptionPane.showMessageDialog(this, msg, "선택 불가", JOptionPane.WARNING_MESSAGE);

                // 턴 넘기기
                controller.nextTurn();
                boardPanel.repaint();
                return null;
            }

            /* 3) 실제 선택 다이얼로그 */
            int choice = JOptionPane.showOptionDialog(
                    this,
                    "이동할 말을 선택하세요 (" + player.getName() + ")",
                    "말 선택",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    pieceDecisions.toArray(new String[0]),
                    pieceDecisions.getFirst()
            );
            return (choice < 0 || choice >= choices.size()) ? null : choices.get(choice);
        }
    }

    private BoardNode chooseDestination(List<BoardNode> cands, String title, int index) {
        if (cands.size() == 1) return cands.getFirst();
        String[] opts = cands.stream().map(BoardNode::getId).toArray(String[]::new);
        if (index >= 0) {
            opts[index] = "Finish";
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
        return (ch < 0 || ch >= cands.size()) ? null : cands.get(ch);
    }

    @Override
    public void setController(YutGameController controller) {
        this.controller = controller;
        this.boardPanel = new BoardPanel(controller);

        add(boardPanel, BorderLayout.CENTER);
        revalidate();  // 레이아웃 다시 계산
        repaint();     // 화면 갱신
    }

    // SwingYutGameView.java
    @Override
    public void initBoardPanel() {
        this.boardPanel = new BoardPanel(controller);
        add(boardPanel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    @Override
    public void repaintBoard() {
        this.repaint();
    }

    @Override
    public void setVisibleBoard(boolean visible) {
        this.setVisible(visible);
    }
}
