package main.java.com.yutgame.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 오각형 윷놀이판 구현체 - 노드 36개
 * "중심점에서 멈추면 가장 가까운 path(1번)로 진행, 그렇지 않으면 2번 path" 등의
 * 규칙을 (getPossibleNextNodes)에서 간단히 시뮬레이션한다.
 */
public class PentagonBoard implements YutBoard {
    private List<BoardNode> nodes;

    // 완주 확인용 : path에 start node가 포함되는지
    private List<BoardNode> paths = new ArrayList<>();

    private PentagonBoard(List<BoardNode> nodes) {
        this.nodes = nodes;
    }

    public static PentagonBoard createPentagonBoard() {
        // 노드 생성
        List<BoardNode> allNodes = new ArrayList<>();

        // 꼭짓점 노드

        BoardNode start = new BoardNode("START_NODE", 459, 518);
        BoardNode A = new BoardNode("A", 557, 217);
        BoardNode B = new BoardNode("B", 300, 30);
        BoardNode C = new BoardNode("C", 43, 217);
        BoardNode D = new BoardNode("D", 141, 518);

        // 외각 노드
        BoardNode s1 = new BoardNode("s1", 479, 458);
        BoardNode s2 = new BoardNode("s2", 498, 398);
        BoardNode s3 = new BoardNode("s3", 518, 337);
        BoardNode s4 = new BoardNode("s4", 537, 277);

        BoardNode A1 = new BoardNode("A1", 506, 180);
        BoardNode A2 = new BoardNode("A2", 454, 142);
        BoardNode A3 = new BoardNode("A3", 403, 105);
        BoardNode A4 = new BoardNode("A4", 351, 67);

        BoardNode B1 = new BoardNode("B1", 249, 67);
        BoardNode B2 = new BoardNode("B2", 197, 105);
        BoardNode B3 = new BoardNode("B3", 146, 142);
        BoardNode B4 = new BoardNode("B4", 94, 180);

        BoardNode C1 = new BoardNode("C1", 63, 277);
        BoardNode C2 = new BoardNode("C2", 82, 337);
        BoardNode C3 = new BoardNode("C3", 102, 398);
        BoardNode C4 = new BoardNode("C4", 121, 458);

        BoardNode D1 = new BoardNode("D1", 205, 518);
        BoardNode D2 = new BoardNode("D2", 268, 518);
        BoardNode D3 = new BoardNode("D3", 332, 518);
        BoardNode D4 = new BoardNode("D4", 395, 518);

        // 지름길 노드
        BoardNode center = new BoardNode("CENTER_PENTAGON", 300, 300);

        // start to center
        BoardNode c1 = new BoardNode("c1", 400, 437);
        BoardNode c2 = new BoardNode("c2", 359, 381);

        // A to center
        BoardNode c3 = new BoardNode("c3", 462, 248);
        BoardNode c4 = new BoardNode("c4", 395, 269);

        // B to center
        BoardNode c5 = new BoardNode("c5", 300, 130);
        BoardNode c6 = new BoardNode("c6", 300, 200);

        // C to center
        BoardNode c7 = new BoardNode("c7", 138, 248);
        BoardNode c8 = new BoardNode("c8", 205, 269);

        // D to center
        BoardNode c9 = new BoardNode("c9", 200, 437);
        BoardNode c10 = new BoardNode("c10", 241, 381);

        // 노드 간 연결 설정 (테두리)
        start.addNextNode(s1);       s1.addNextNode(s2);
        s2.addNextNode(s3);     s3.addNextNode(s4);
        s4.addNextNode(A);

        A.addNextNode(A1);     A1.addNextNode(A2);
        A2.addNextNode(A3);   A3.addNextNode(A4);
        A4.addNextNode(B);

        B.addNextNode(B1);     B1.addNextNode(B2);
        B2.addNextNode(B3);   B3.addNextNode(B4);
        B4.addNextNode(C);

        C.addNextNode(C1);     C1.addNextNode(C2);
        C2.addNextNode(C3);   C3.addNextNode(C4);
        C4.addNextNode(D);

        D.addNextNode(D1);     D1.addNextNode(D2);
        D2.addNextNode(D3);   D3.addNextNode(D4);
        D4.addNextNode(start);

        // 내부 지름길 노드 간 연결 설정
        A.addNextNode(c3);        c3.addNextNode(c4);      c4.addNextNode(center);
        B.addNextNode(c5);        c5.addNextNode(c6);      c6.addNextNode(center);
        C.addNextNode(c7);        c7.addNextNode(c8);      c8.addNextNode(center);
        center.addNextNode(c10);  c10.addNextNode(c9);     c9.addNextNode(D);
        center.addNextNode(c2);   c2.addNextNode(c1);      c1.addNextNode(start);

        // 리스트 등록
        BoardNode[] arr = {
                start, A, B, C, D, center,
                s1, s2, s3, s4,
                A1, A2, A3, A4,
                B1, B2, B3, B4,
                C1, C2, C3, C4,
                D1, D2, D3, D4,
                c1, c2, c3, c4, c5,
                c6, c7, c8, c9, c10
        };
        for (BoardNode bn : arr) {
            allNodes.add(bn);
        }

        return new PentagonBoard(allNodes);
    }

    @Override
    public List<BoardNode> getNodes() {
        return nodes;
    }

    @Override
    public BoardNode getStartNode() {
        // "START_NODE"가 출발
        return nodes.stream()
                .filter(n -> "START_NODE".equals(n.getId()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<BoardNode> getPossibleNextNodes(BoardNode current, int steps) {
        System.out.println("step: " + steps);
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
        if ("CENTER_PENTAGON".equals(node.getId()) && !path.isEmpty()) {
            // 이전 노드가 무엇인지 확인 (path의 마지막 바로 이전 노드)
            BoardNode prevNode = path.size() > 1 ? path.get(path.size() - 2) : null;

            if (prevNode == null) {
                // 처음 시작이 CENTER이고, 이동 시작이라면 → 무조건 c2
                BoardNode nextNode = findNodeById(node.getNextNodes(), "c2");
                if (nextNode != null) {
                    dfsPaths(nextNode, steps - 1, path, results);
                    path.removeLast();
                    return;
                }
            } else if (prevNode != null) {
                String prevId = prevNode.getId();
                BoardNode nextNode = null;

                // NE2에서 온 경우 SW1로 진행
                if ("c4".equals(prevId) || "c6".equals(prevId) || "c8".equals(prevId)) {
                    nextNode = findNodeById(node.getNextNodes(), "c10");
                }
                if (nextNode != null) {
                    dfsPaths(nextNode, steps - 1, path, results);
                    path.removeLast();
                    return;
                }
            }
        }
        // 갈림길 (nextNodes) 탐색
        for (BoardNode nxt : node.getNextNodes()) {
            dfsPaths(nxt, steps-1, path, results);
        }
        path.removeLast();
    }

    @Override
    public List<BoardNode> getPaths(){
        if (!paths.isEmpty()) {
            paths.remove(0);
        }
        return paths;
    }

    @Override
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
        for (BoardNode nd : nodes) {
            if (nd.getNextNodes().contains(target)) {
                result.add(nd);
            }
        }
        return result;
    }
}