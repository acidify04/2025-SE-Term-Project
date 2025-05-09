package main.java.com.yutgame.model;

import java.util.ArrayList;
import java.util.List;

public class HexagonBoard implements YutBoard {
    // 보드에 속한 모든 노드
    private List<BoardNode> nodes;
    // 완주 확인용 : path에 start node가 포함되는지
    private List<BoardNode> paths = new ArrayList<>();

    private HexagonBoard(List<BoardNode> nodes) {
        this.nodes = nodes;
    }

    public static HexagonBoard createHexagonBoard() {
        // 노드 생성
        List<BoardNode> allNodes = new ArrayList<>();

        // 600x600 좌표 위치 설정
        int margin = 50;
        int boardSize = 500;
        int center = margin + boardSize / 2;  // 300
        int minX = margin;     // 50
        int maxX = margin + 500; // 550
        int minY = margin;     // 50
        int maxY = margin + 500; // 550

        BoardNode C1 = new BoardNode("START_NODE", 50, 300);
        BoardNode C2 = new BoardNode("A", 175, 550);
        BoardNode C3 = new BoardNode("B", 425, 550);
        BoardNode C4 = new BoardNode("C", 550, 300);
        BoardNode C5 = new BoardNode("D", 425, 50);
        BoardNode C6 = new BoardNode("E", 175, 50);
        BoardNode CENTER = new BoardNode("CENTER_HEXAGON", center, center);

        // 노드 이름 설정 및 생성 (테두리)
        BoardNode n1 = new BoardNode("1", 75, 350);
        BoardNode n2 = new BoardNode("2", 100, 400);
        BoardNode n3 = new BoardNode("3", 125, 450);
        BoardNode n4 = new BoardNode("4", 150, 500);

        BoardNode n5 = new BoardNode("5", 225, 550);
        BoardNode n6 = new BoardNode("6", 275, 550);
        BoardNode n7 = new BoardNode("7", 325, 550);
        BoardNode n8 = new BoardNode("8", 375, 550);

        BoardNode n9 = new BoardNode("9", 450, 500);
        BoardNode n10 = new BoardNode("10", 475, 450);
        BoardNode n11 = new BoardNode("11", 500, 400);
        BoardNode n12 = new BoardNode("12", 525, 350);

        BoardNode n16 = new BoardNode("16", 425 + 25, 50 + 50);
        BoardNode n15 = new BoardNode("15", 425 + 50, 50 + 100);
        BoardNode n14 = new BoardNode("14", 425 + 75, 50 + 150);
        BoardNode n13 = new BoardNode("13", 425 + 100, 50 + 200);

        BoardNode n20 = new BoardNode("20", 225, 50);
        BoardNode n19 = new BoardNode("19", 275, 50);
        BoardNode n18 = new BoardNode("18", 325, 50);
        BoardNode n17 = new BoardNode("17", 375, 50);

        BoardNode n21 = new BoardNode("21", 150, 100);
        BoardNode n22 = new BoardNode("22", 125, 150);
        BoardNode n23 = new BoardNode("23", 100, 200);
        BoardNode n24 = new BoardNode("24", 75, 250);

        // 노드 이름 설정 및 생성 (지름길)
        BoardNode f2 = new BoardNode("f2", 217, 134);
        BoardNode f1 = new BoardNode("f1", 258, 216);

        BoardNode e2 = new BoardNode("e2", 383, 134);
        BoardNode e1 = new BoardNode("e1", 342, 216);

        BoardNode a2 = new BoardNode("a2", 134, 300);
        BoardNode a1 = new BoardNode("a1", 217, 300);

        BoardNode b2 = new BoardNode("b2", 217, 466);
        BoardNode b1 = new BoardNode("b1", 258, 384);

        BoardNode c1 = new BoardNode("c1", 600-258, 384);
        BoardNode c2 = new BoardNode("c2", 600-217, 466);

        BoardNode d1 = new BoardNode("d1", 383, 300);
        BoardNode d2 = new BoardNode("d2", 466, 300);

        // 노드 간 연결 설정
        C1.addNextNode(n1);  n1.addNextNode(n2);  n2.addNextNode(n3);  n3.addNextNode(n4);  n4.addNextNode(C2);
        C2.addNextNode(n5);  n5.addNextNode(n6);  n6.addNextNode(n7);  n7.addNextNode(n8);  n8.addNextNode(C3);
        C3.addNextNode(n9);  n9.addNextNode(n10);  n10.addNextNode(n11);  n11.addNextNode(n12);  n12.addNextNode(C4);
        C4.addNextNode(n13);  n13.addNextNode(n14);  n14.addNextNode(n15);  n15.addNextNode(n16);  n16.addNextNode(C5);
        C5.addNextNode(n17);  n17.addNextNode(n18);  n18.addNextNode(n19);  n19.addNextNode(n20);  n20.addNextNode(C6);
        C6.addNextNode(n21);  n21.addNextNode(n22);  n22.addNextNode(n23);  n23.addNextNode(n24);  n24.addNextNode(C1);

        C2.addNextNode(b2);  b2.addNextNode(b1);  b1.addNextNode(CENTER);
        C3.addNextNode(c2);  c2.addNextNode(c1);  c1.addNextNode(CENTER);
        C4.addNextNode(d2);  d2.addNextNode(d1);  d1.addNextNode(CENTER);
        C5.addNextNode(e2);  e2.addNextNode(e1);  e1.addNextNode(CENTER);
        CENTER.addNextNode(f1);  f1.addNextNode(f2);  f2.addNextNode(C6);
        CENTER.addNextNode(a1);  a1.addNextNode(a2);  a2.addNextNode(C1);

        // 모든 노드 담는 배열 설정
        BoardNode[] arr = {
                C1, C2, C3, C4, C5, C6, CENTER,
                n1, n2, n3, n4, n5, n6, n7, n8, n9, n10, n11, n12,
                n13, n14, n15, n16, n17, n18, n19, n20, n21, n22, n23, n24,
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

// ✅ CENTER 먼저
        // ✅ "CENTER에서 출발"은 무조건 a1로만 나간다
        if ("CENTER_HEXAGON".equals(node.getId()) && path.size() == 1) {
            // 처음 시작이 CENTER이고, 이동 시작이라면 → 무조건 a1
            BoardNode nextNode = findNodeById(node.getNextNodes(), "a1");
            if (nextNode != null) {
                dfsPaths(nextNode, steps - 1, path, results);
                path.removeLast();
                return;
            }
        }

        // steps == 0 도착 지점
        if (steps == 0) {
            if (!results.contains(node)) {
                results.add(node);
            }
            path.removeLast();
            return;
        }

        // 일반 nextNodes 탐색
        for (BoardNode nxt : node.getNextNodes()) {
            dfsPaths(nxt, steps - 1, path, results);
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
