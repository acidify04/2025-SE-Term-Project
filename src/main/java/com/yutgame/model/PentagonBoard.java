package main.java.com.yutgame.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 오각형 윷놀이판 구현체 - 노드 36개
 * "중심점에서 멈추면 가장 가까운 path(1번)로 진행, 그렇지 않으면 2번 path" 등의
 * 규칙을 (getPossibleNextNodes)에서 간단히 시뮬레이션한다.
 */
public class PentagonBoard implements YutBoard {
    /** 화면에서 전체 보드를 이동시키기 위한 오프셋 */
    private static final int DX = -38;   // 왼쪽으로 30 픽셀
    private static final int DY = -34;   // 위로   30 픽셀

    private List<BoardNode> nodes;
    // 완주 확인용 : path에 start node가 포함되는지
    private List<BoardNode> paths = new ArrayList<>();

    private PentagonBoard(List<BoardNode> nodes) {
        this.nodes = nodes;
    }

    /** 좌표 보정을 한 노드를 생성하는 헬퍼 */
    private static BoardNode n(String id, int x, int y) {
        return new BoardNode(id, x + DX, y + DY);
    }

    public static PentagonBoard createPentagonBoard() {
        List<BoardNode> allNodes = new ArrayList<>();

        /* ───── 꼭짓점 ───── */
        BoardNode B     = n("B",           362, 421);
        BoardNode C     = n("C",           438, 174);
        BoardNode D     = n("D",           237,  21);
        BoardNode start = n("START_NODE",   38, 174);
        BoardNode A     = n("A",           114, 421);

        /* ───── 외곽 ───── */
        BoardNode B1 = n("B1", 377, 371);   BoardNode B2 = n("B2", 392, 321);
        BoardNode B3 = n("B3", 408, 272);   BoardNode B4 = n("B4", 423, 222);

        BoardNode C1 = n("C1", 398, 143);   BoardNode C2 = n("C2", 358, 113);
        BoardNode C3 = n("C3", 319,  83);   BoardNode C4 = n("C4", 278,  53);

        BoardNode D1 = n("D1", 199,  53);   BoardNode D2 = n("D2", 158,  83);
        BoardNode D3 = n("D3", 119, 113);   BoardNode D4 = n("D4",  79, 143);

        BoardNode s1 = n("s1",  54, 222);   BoardNode s2 = n("s2",  69, 272);
        BoardNode s3 = n("s3",  85, 321);   BoardNode s4 = n("s4",  99, 371);

        BoardNode A1 = n("A1", 164, 421);   BoardNode A2 = n("A2", 213, 421);
        BoardNode A3 = n("A3", 263, 421);   BoardNode A4 = n("A4", 312, 421);

        /* ───── 중앙 및 지름길 ───── */
        BoardNode center = n("CENTER", 237, 242);

        BoardNode c5  = n("c5", 315, 355);  BoardNode c6  = n("c6", 283, 309);
        BoardNode c7  = n("c7", 364, 199);  BoardNode c8  = n("c8", 312, 217);
        BoardNode c9  = n("c9", 237, 101);  BoardNode c10 = n("c10",237, 157);
        BoardNode c1  = n("c1", 112, 199);  BoardNode c2  = n("c2", 164, 217);
        BoardNode c3  = n("c3", 160, 355);  BoardNode c4  = n("c4", 192, 309);

        /* ──────────── 간선 정의 ──────────── */
        start.addNextNode(s1);       s1.addNextNode(s2);
        s2.addNextNode(s3);          s3.addNextNode(s4);
        s4.addNextNode(A);

        A.addNextNode(A1);           A1.addNextNode(A2);
        A2.addNextNode(A3);          A3.addNextNode(A4);
        A4.addNextNode(B);

        B.addNextNode(B1);           B1.addNextNode(B2);
        B2.addNextNode(B3);          B3.addNextNode(B4);
        B4.addNextNode(C);

        C.addNextNode(C1);           C1.addNextNode(C2);
        C2.addNextNode(C3);          C3.addNextNode(C4);
        C4.addNextNode(D);

        D.addNextNode(D1);           D1.addNextNode(D2);
        D2.addNextNode(D3);          D3.addNextNode(D4);
        D4.addNextNode(start);

        /* ───── 지름길 ───── */
        A.addNextNode(c3);           c3.addNextNode(c4);     c4.addNextNode(center);
        B.addNextNode(c5);           c5.addNextNode(c6);     c6.addNextNode(center);
        C.addNextNode(c7);           c7.addNextNode(c8);     c8.addNextNode(center);
        center.addNextNode(c10);     c10.addNextNode(c9);    c9.addNextNode(D);
        center.addNextNode(c2);      c2.addNextNode(c1);     c1.addNextNode(start);

        /* ───── 등록 ───── */
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
        if ("CP".equals(node.getId()) && !path.isEmpty()) {
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

        // 1) pathHistory 우선
        if (!target.getOccupantPieces().isEmpty()) {
            Piece p = target.getOccupantPieces().get(0);
            List<BoardNode> hist = p.getPathHistory();
            if (hist.size() >= 2) {
                result.add(hist.get(hist.size() - 2));
                return result;          // 단일 결과
            }
        }

        // 2) 그래프 역탐색(첫 번째만)
        for (BoardNode nd : nodes) {
            if (nd.getNextNodes().contains(target)) {
                result.add(nd);
                break;
            }
        }
        return result;
    }

    @Override
    public List<BoardNode> getValidDestinationsWithShortcutRules(BoardNode currentNode, int steps) {
        List<BoardNode> allDestinations;

        if (steps < 0) {
            allDestinations = getPossiblePreviousNodes(currentNode);
        } else {
            allDestinations = getPossibleNextNodes(currentNode, steps);
        }

        return filterByShortcutRules(currentNode, allDestinations);
    }

    private List<BoardNode> filterByShortcutRules(BoardNode currentNode, List<BoardNode> destinations) {
        String currentNodeId = currentNode.getId();

        if (isShortcutForbiddenPosition(currentNodeId)) {
            return destinations.stream()
                    .filter(dest -> !isShortcutNode(dest.getId()))
                    .collect(Collectors.toList());
        }

        return destinations;
    }

    private boolean isShortcutForbiddenPosition(String nodeId) {
        Set<String> forbiddenPositions = Set.of(
                "s1", "s2", "s3", "s4",
                "A1", "A2", "A3", "A4",
                "B1", "B2", "B3", "B4",
                "C1", "C2", "C3", "C4",
                "D1", "D2", "D3", "D4"
        );
        return forbiddenPositions.contains(nodeId);
    }

    private boolean isShortcutNode(String nodeId) {
        Set<String> shortcutNodes = Set.of(
                "c1", "c2", "c3", "c4", "c5",
                "c6", "c7", "c8", "c9", "c10"
        );
        return shortcutNodes.contains(nodeId);
    }
}