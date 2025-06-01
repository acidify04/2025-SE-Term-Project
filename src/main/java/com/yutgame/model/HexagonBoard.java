package main.java.com.yutgame.model;

import java.util.ArrayList;
import java.util.List;

public class HexagonBoard implements YutBoard {
    /* ───── 위치/크기 조절 상수 ───── */
    private static final double SCALE = 0.79;   // 1보다 작게: 축소 / 1보다 크면 확대
    private static final int    DX    = -98;    // 좌우 이동
    private static final int    DY    = -98;    // 상하 이동
    private static final int    CX    = 300;    // 원본 중심(300,300) 기준
    private static final int    CY    = 300;

    /** 축소·이동을 적용한 노드 생성 헬퍼 */
    private static BoardNode n(String id, int x, int y) {
        int sx = (int) Math.round(CX + (x - CX) * SCALE) + DX;
        int sy = (int) Math.round(CY + (y - CY) * SCALE) + DY;
        return new BoardNode(id, sx, sy);
    }

    // 보드에 속한 모든 노드
    private List<BoardNode> nodes;
    // 완주 확인용 : path에 start node가 포함되는지
    private List<BoardNode> paths = new ArrayList<>();

    private HexagonBoard(List<BoardNode> nodes) {
        this.nodes = nodes;
    }

    public static HexagonBoard createHexagonBoard() {
        List<BoardNode> allNodes = new ArrayList<>();

        /* ───── 기본 좌표계(600×600) 정보 ───── */
        int margin    = 50;
        int boardSize = 500;
        int center    = margin + boardSize / 2; // 300

        /* ───── 꼭짓점 ───── */
        BoardNode C1      = n("START_NODE",  50, 300);
        BoardNode C2      = n("A",          175, 550);
        BoardNode C3      = n("B",          425, 550);
        BoardNode C4      = n("C",          550, 300);
        BoardNode C5      = n("D",          425,  50);
        BoardNode C6      = n("E",          175,  50);
        BoardNode CENTER  = n("CH",        center, center); // 300,300

        /* ───── 테두리 노드 ───── */
        BoardNode n1  = n("1",  75, 350);   BoardNode n2  = n("2", 100, 400);
        BoardNode n3  = n("3", 125, 450);   BoardNode n4  = n("4", 150, 500);

        BoardNode n5  = n("5", 225, 550);   BoardNode n6  = n("6", 275, 550);
        BoardNode n7  = n("7", 325, 550);   BoardNode n8  = n("8", 375, 550);

        BoardNode n9  = n("9", 450, 500);   BoardNode n10 = n("10", 475, 450);
        BoardNode n11 = n("11",500, 400);   BoardNode n12 = n("12",525, 350);

        BoardNode n16 = n("16",450, 100);   BoardNode n15 = n("15",475, 150);
        BoardNode n14 = n("14",500, 200);   BoardNode n13 = n("13",525, 250);

        BoardNode n20 = n("20",225,  50);   BoardNode n19 = n("19",275,  50);
        BoardNode n18 = n("18",325,  50);   BoardNode n17 = n("17",375,  50);

        BoardNode n21 = n("21",150, 100);   BoardNode n22 = n("22",125, 150);
        BoardNode n23 = n("23",100, 200);   BoardNode n24 = n("24", 75, 250);

        /* ───── 지름길 노드 ───── */
        BoardNode f2 = n("f2", 217, 134);   BoardNode f1 = n("f1", 258, 216);

        BoardNode e2 = n("e2", 383, 134);   BoardNode e1 = n("e1", 342, 216);

        BoardNode a2 = n("a2", 134, 300);   BoardNode a1 = n("a1", 217, 300);

        BoardNode b2 = n("b2", 217, 466);   BoardNode b1 = n("b1", 258, 384);

        BoardNode c1 = n("c1", 342, 384);   BoardNode c2 = n("c2", 383, 466);

        BoardNode d1 = n("d1", 383, 300);   BoardNode d2 = n("d2", 466, 300);

        /* ───── 간선 정의 ───── */
        C1.addNextNode(n1);  n1.addNextNode(n2);  n2.addNextNode(n3);  n3.addNextNode(n4);  n4.addNextNode(C2);
        C2.addNextNode(n5);  n5.addNextNode(n6);  n6.addNextNode(n7);  n7.addNextNode(n8);  n8.addNextNode(C3);
        C3.addNextNode(n9);  n9.addNextNode(n10); n10.addNextNode(n11); n11.addNextNode(n12); n12.addNextNode(C4);
        C4.addNextNode(n13); n13.addNextNode(n14); n14.addNextNode(n15); n15.addNextNode(n16); n16.addNextNode(C5);
        C5.addNextNode(n17); n17.addNextNode(n18); n18.addNextNode(n19); n19.addNextNode(n20); n20.addNextNode(C6);
        C6.addNextNode(n21); n21.addNextNode(n22); n22.addNextNode(n23); n23.addNextNode(n24); n24.addNextNode(C1);

        C2.addNextNode(b2);  b2.addNextNode(b1);  b1.addNextNode(CENTER);
        C3.addNextNode(c2);  c2.addNextNode(c1);  c1.addNextNode(CENTER);
        C4.addNextNode(d2);  d2.addNextNode(d1);  d1.addNextNode(CENTER);
        C5.addNextNode(e2);  e2.addNextNode(e1);  e1.addNextNode(CENTER);
        CENTER.addNextNode(f1); f1.addNextNode(f2); f2.addNextNode(C6);
        CENTER.addNextNode(a1); a1.addNextNode(a2); a2.addNextNode(C1);

        /* ───── 등록 ───── */
        BoardNode[] arr = {
                C1, C2, C3, C4, C5, C6, CENTER,
                n1, n2, n3, n4, n5, n6, n7, n8,
                n9, n10, n11, n12, n13, n14, n15, n16,
                n17, n18, n19, n20, n21, n22, n23, n24,
                a1, a2, b1, b2, c1, c2, d1, d2, e1, e2, f1, f2
        };
        for (BoardNode bn : arr) {
            allNodes.add(bn);
        }
        return new HexagonBoard(allNodes);
    }

    @Override
    public List<BoardNode> getNodes() {
        return nodes;
    }

    // 시작 노드를 반환하는 함수
    @Override
    public BoardNode getStartNode() {
        // START_NODE ID를 가진 노드를 찾아 반환
        return nodes.stream()
                .filter(n -> "START_NODE".equals(n.getId()))
                .findFirst()
                .orElse(null);
    }

    // DFS를 이용해 이동 가능한 다음 노드 찾는 함수
    @Override
    public List<BoardNode> getPossibleNextNodes(BoardNode current, int steps) {
        List<BoardNode> results = new ArrayList<>();
        if (current == null) return results;

        // 빽도(-1)은 여기서 처리하지 않고 빈 리스트 반환 (윷게임에서 특별 처리)
        if (steps < 0) {
            return results;
        }
        dfsPaths(current, steps, new ArrayList<>(), results);
        return results;
    }

    private void dfsPaths(BoardNode node, int steps, List<BoardNode> path, List<BoardNode> results) {
        path.add(node);
        paths.add(node);

        if (steps == 0) {
            // 도달
            if (!results.contains(node)) {
                results.add(node);
            }
            path.removeLast();
            return;
        }

        // CENTER 특별 처리
        if ("CH".equals(node.getId()) && !path.isEmpty()) {
            // 이전 노드가 무엇인지 확인 (path의 마지막 바로 이전 노드)
            BoardNode prevNode = path.size() > 1 ? path.get(path.size() - 2) : null;

            if (prevNode == null) {
                // 처음 시작이 CENTER이고, 이동 시작이라면 → 무조건 c2
                BoardNode nextNode = findNodeById(node.getNextNodes(), "a1");
                if (nextNode != null) {
                    dfsPaths(nextNode, steps - 1, path, results);
                    path.removeLast();
                    return;
                }
            } else if (prevNode != null) {
                String prevId = prevNode.getId();
                BoardNode nextNode = null;

                if ("b1".equals(prevId) || "c1".equals(prevId) || "d1".equals(prevId) || "e1".equals(prevId)) {
                    nextNode = findNodeById(node.getNextNodes(), "f1");
                }
                if (nextNode != null) {
                    dfsPaths(nextNode, steps - 1, path, results);
                    path.removeLast();
                    return;
                }
            }
        }

        for (BoardNode nxt : node.getNextNodes()) {
            dfsPaths(nxt, steps-1, path, results);
        }
        path.removeLast();
    }

    public List<BoardNode> getPaths() {
        if (!paths.isEmpty()) {
            paths.remove(0);
        }
        return paths;
    }

    public void pathClear(){
        paths.clear();
    }

    /**
     * ID로 노드 찾기 헬퍼 메서드
     */
    private BoardNode findNodeById(List<BoardNode> nodes, String id) {
        for (BoardNode node : nodes) {
            if (id.equals(node.getId())) {
                return node;
            }
        }
        return null;
    }


    @Override
    public List<BoardNode> getPossiblePreviousNodes(BoardNode target) {
        List<BoardNode> result = new ArrayList<>();
        if (target == null) return result;

        /* 1) 말이 올라와 있으면, 그 말의 pathHistory 기준으로 직전 칸 하나만 리턴 */
        if (!target.getOccupantPieces().isEmpty()) {
            Piece p = target.getOccupantPieces().get(0);
            List<BoardNode> hist = p.getPathHistory();
            if (hist.size() >= 2) {
                result.add(hist.get(hist.size() - 2));
                return result;          // ★ 갈림길 선택 제거
            }
        }

        /* 2) fallback – 그래프 역탐색 (첫 번째 후보만) */
        for (BoardNode nd : nodes) {
            if (nd.getNextNodes().contains(target)) {
                result.add(nd);
                break;                  // 첫 번째만
            }
        }
        return result;
    }
}
