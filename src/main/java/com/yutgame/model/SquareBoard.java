package main.java.com.yutgame.model;

import java.util.ArrayList;
import java.util.List;

public class SquareBoard implements YutBoard {

    // 보드에 속한 모든 노드
    private List<BoardNode> nodes;

    // 완주 확인용 : path에 start node가 포함되는지
    private List<BoardNode> paths = new ArrayList<>();

    private SquareBoard(List<BoardNode> nodes) {
        this.nodes = nodes;
    }

    public static SquareBoard createStandardBoard() {
        // 노드 생성
        List<BoardNode> allNodes = new ArrayList<>();

        int margin = 10;
        int boardSize = 380; // 380 - 2*margin
        int center = margin + boardSize / 2;  // 190
        int minX = margin;      // 50
        int maxX = margin + boardSize; // 330
        int minY = margin;
        int maxY = margin + boardSize;

        BoardNode startNode  = new BoardNode("START_NODE", maxX, maxY);
        BoardNode cornerNE   = new BoardNode("A", maxX, minY);
        BoardNode cornerNW   = new BoardNode("B", minX, minY);
        BoardNode cornerSW   = new BoardNode("C", minX, maxY);
        BoardNode centerNode = new BoardNode("CENTER", center, center);

        // 테두리 노드 (각 70px 간격)
        BoardNode e1 = new BoardNode("E1", maxX, maxY - 75);
        BoardNode e2 = new BoardNode("E2", maxX, maxY - 150);
        BoardNode e3 = new BoardNode("E3", maxX, maxY - 225);
        BoardNode e4 = new BoardNode("E4", maxX, maxY - 300);

        BoardNode n1 = new BoardNode("N1", maxX - 75, minY);
        BoardNode n2 = new BoardNode("N2", maxX - 150, minY);
        BoardNode n3 = new BoardNode("N3", maxX - 225, minY);
        BoardNode n4 = new BoardNode("N4", maxX - 300, minY);

        BoardNode w1 = new BoardNode("W1", minX, minY + 75);
        BoardNode w2 = new BoardNode("W2", minX, minY + 150);
        BoardNode w3 = new BoardNode("W3", minX, minY + 225);
        BoardNode w4 = new BoardNode("W4", minX, minY + 300);

        BoardNode s1 = new BoardNode("S1", minX + 75, maxY);
        BoardNode s2 = new BoardNode("S2", minX + 150, maxY);
        BoardNode s3 = new BoardNode("S3", minX + 225, maxY);
        BoardNode s4 = new BoardNode("S4", minX + 300, maxY);

        // 노드 이름 설정 및 생성 (지름길)
        BoardNode ne1 = new BoardNode("NE1", (cornerNE.getX()*2 + centerNode.getX())/3,
                (cornerNE.getY()*2 + centerNode.getY())/3);
        BoardNode ne2 = new BoardNode("NE2", (cornerNE.getX() + centerNode.getX()*2)/3,
                (cornerNE.getY() + centerNode.getY()*2)/3);

        BoardNode nw1 = new BoardNode("NW1", (cornerNW.getX()*2 + centerNode.getX())/3,
                (cornerNW.getY()*2 + centerNode.getY())/3);
        BoardNode nw2 = new BoardNode("NW2", (cornerNW.getX() + centerNode.getX()*2)/3,
                (cornerNW.getY() + centerNode.getY()*2)/3);

        BoardNode sw1 = new BoardNode("SW1", (centerNode.getX()*2 + cornerSW.getX())/3,
                (centerNode.getY()*2 + cornerSW.getY())/3);
        BoardNode sw2 = new BoardNode("SW2", (centerNode.getX() + cornerSW.getX()*2)/3,
                (centerNode.getY() + cornerSW.getY()*2)/3);

        BoardNode se1 = new BoardNode("SE1", (centerNode.getX()*2 + startNode.getX())/3,
                (centerNode.getY()*2 + startNode.getY())/3);
        BoardNode se2 = new BoardNode("SE2", (centerNode.getX() + startNode.getX()*2)/3,
                (centerNode.getY() + startNode.getY()*2)/3);

        // 노드 간 연결 설정 (테두리 반시계 방향)
        startNode.addNextNode(e1);  e1.addNextNode(e2);
        e2.addNextNode(e3);        e3.addNextNode(e4);
        e4.addNextNode(cornerNE);

        cornerNE.addNextNode(n1);  n1.addNextNode(n2);
        n2.addNextNode(n3);        n3.addNextNode(n4);
        n4.addNextNode(cornerNW);

        cornerNW.addNextNode(w1);  w1.addNextNode(w2);
        w2.addNextNode(w3);        w3.addNextNode(w4);
        w4.addNextNode(cornerSW);

        cornerSW.addNextNode(s1);  s1.addNextNode(s2);
        s2.addNextNode(s3);        s3.addNextNode(s4);
        s4.addNextNode(startNode);

        // 내부 지름길 노드 간 연결 설정
        cornerNE.addNextNode(ne1);  ne1.addNextNode(ne2);  ne2.addNextNode(centerNode);
        cornerNW.addNextNode(nw1);  nw1.addNextNode(nw2);  nw2.addNextNode(centerNode);
        centerNode.addNextNode(sw1);  sw1.addNextNode(sw2);  sw2.addNextNode(cornerSW);
        centerNode.addNextNode(se1);  se1.addNextNode(se2);  se2.addNextNode(startNode);

        // 모든 노드 담는 배열 설정
        BoardNode[] arr = {
                startNode,
                cornerNE, cornerNW, cornerSW, centerNode,
                e1, e2, e3, e4,
                n1, n2, n3, n4,
                w1, w2, w3, w4,
                s1, s2, s3, s4,
                ne1, ne2, nw1,nw2,
                sw1, sw2, se1, se2
        };

        for (BoardNode bn : arr) {
            allNodes.add(bn);
        }

        return new SquareBoard(allNodes);
    }

    // 노드 리스트를 반환하는 함수
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

        // CENTER_NODE 특별 처리
        if ("CENTER".equals(node.getId()) && !path.isEmpty()) {
            // 이전 노드가 무엇인지 확인 (path의 마지막 바로 이전 노드)
            BoardNode prevNode = path.size() > 1 ? path.get(path.size() - 2) : null;

            if (prevNode != null) {
                String prevId = prevNode.getId();
                BoardNode nextNode = null;

                // NE2에서 온 경우 SW1로 진행
                if ("NE2".equals(prevId)) {
                    nextNode = findNodeById(node.getNextNodes(), "SW1");
                }
                // NW2에서 온 경우 SE1로 진행
                else if ("NW2".equals(prevId)) {
                    nextNode = findNodeById(node.getNextNodes(), "SE1");
                }

                // 특별 경로가 결정된 경우
                if (nextNode != null) {
                    dfsPaths(nextNode, steps-1, path, results);
                    path.removeLast();
                    return; // 다른 경로는 탐색하지 않음
                }
            }
        }


        // 갈림길 (nextNodes) 탐색
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

        // 1) pathHistory 기준
        if (!target.getOccupantPieces().isEmpty()) {
            Piece p = target.getOccupantPieces().get(0);
            List<BoardNode> hist = p.getPathHistory();
            if (hist.size() >= 2) {
                result.add(hist.get(hist.size() - 2));
                return result;          // 갈림길 제거
            }
        }

        // 2) 역탐색 fallback (첫 번째만)
        for (BoardNode nd : nodes) {
            if (nd.getNextNodes().contains(target)) {
                result.add(nd);
                break;
            }
        }
        return result;
    }
}