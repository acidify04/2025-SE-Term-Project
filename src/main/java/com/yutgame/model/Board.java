package main.java.com.yutgame.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 윷놀이 보드(노드들의 집합)를 나타내는 클래스.
 * 표준 보드를 구성하거나, 오각형/육각형 등의 다른 보드를 구성할 수도 있음.
 */
public class Board {

    private List<BoardNode> nodes;

    public Board(List<BoardNode> nodes) {
        this.nodes = nodes;
    }

    /**
     * 표준 윷놀이판(29개 지점)을 구성하는 정적 메서드.
     * 경로는 시작(좌하단) -> 우하단 코너 -> 우상단 코너 -> 좌상단 코너 -> 시작(좌하단) 순서.
     * 좌표는 600x600 패널 기준 예시이며, 실제 UI 크기에 맞게 조정 필요.
     */
    public static Board createStandardBoard() {

        // --- 1. 모든 노드 생성 및 좌표 설정 (29개) ---
        // 좌표 예시: (0,0)이 좌상단, 600x600 기준, 여백 50
        int margin = 50;
        int boardSize = 600 - 2 * margin; // 500
        int center = margin + boardSize / 2; // 300
        int minX = margin; // 50
        int maxY = margin + boardSize; // 550
        int maxX = margin + boardSize; // 550
        int minY = margin; // 50

        // 시작 및 코너 노드
        BoardNode startNode = new BoardNode("START_NODE", maxX, maxY);     // 우하단 (550, 550)
        BoardNode cornerNE = new BoardNode("CORNER_NE", maxX, minY);       // 우상단 (550, 50)
        BoardNode cornerNW = new BoardNode("CORNER_NW", minX, minY);       // 좌상단 (50, 50)
        BoardNode cornerSW = new BoardNode("CORNER_SW", minX, maxY);       // 좌하단 (50, 550)
        BoardNode centerNode = new BoardNode("CENTER_NODE", center, center); // 중앙 (300, 300)

        // 외곽 경로 노드 (시계 방향)
        // 동쪽 (E1 ~ E4)
        BoardNode e1 = new BoardNode("E1", maxX, maxY - boardSize * 1 / 5); // (550, 450)
        BoardNode e2 = new BoardNode("E2", maxX, maxY - boardSize * 2 / 5); // (550, 350)
        BoardNode e3 = new BoardNode("E3", maxX, maxY - boardSize * 3 / 5); // (550, 250)
        BoardNode e4 = new BoardNode("E4", maxX, maxY - boardSize * 4 / 5); // (550, 150)
        // 북쪽 (N1 ~ N4)
        BoardNode n1 = new BoardNode("N1", maxX - boardSize * 1 / 5, minY); // (450, 50)
        BoardNode n2 = new BoardNode("N2", maxX - boardSize * 2 / 5, minY); // (350, 50)
        BoardNode n3 = new BoardNode("N3", maxX - boardSize * 3 / 5, minY); // (250, 50)
        BoardNode n4 = new BoardNode("N4", maxX - boardSize * 4 / 5, minY); // (150, 50)
        // 서쪽 (W1 ~ W4)
        BoardNode w1 = new BoardNode("W1", minX, minY + boardSize * 1 / 5); // (50, 150)
        BoardNode w2 = new BoardNode("W2", minX, minY + boardSize * 2 / 5); // (50, 250)
        BoardNode w3 = new BoardNode("W3", minX, minY + boardSize * 3 / 5); // (50, 350)
        BoardNode w4 = new BoardNode("W4", minX, minY + boardSize * 4 / 5); // (50, 450)
        // 남쪽 (S1 ~ S4)
        BoardNode s1 = new BoardNode("S1", minX + boardSize * 1 / 5, maxY); // (150, 550)
        BoardNode s2 = new BoardNode("S2", minX + boardSize * 2 / 5, maxY); // (250, 550)
        BoardNode s3 = new BoardNode("S3", minX + boardSize * 3 / 5, maxY); // (350, 550)
        BoardNode s4 = new BoardNode("S4", minX + boardSize * 4 / 5, maxY); // (450, 550)

        // 내부 경로 노드 (지름길)
        // CORNER_NE -> CENTER (NE1, NE2) - 우상단에서 중앙
        BoardNode ne1 = new BoardNode("NE1", (int)(cornerNE.getX() * 2.0 / 3.0 + centerNode.getX() * 1.0 / 3.0),
                (int)(cornerNE.getY() * 2.0 / 3.0 + centerNode.getY() * 1.0 / 3.0)); // (467, 133) approx.
        BoardNode ne2 = new BoardNode("NE2", (int)(cornerNE.getX() * 1.0 / 3.0 + centerNode.getX() * 2.0 / 3.0),
                (int)(cornerNE.getY() * 1.0 / 3.0 + centerNode.getY() * 2.0 / 3.0)); // (383, 217) approx.
        // CORNER_NW -> CENTER (NW1, NW2) - 좌상단에서 중앙
        BoardNode nw1 = new BoardNode("NW1", (int)(cornerNW.getX() * 2.0 / 3.0 + centerNode.getX() * 1.0 / 3.0),
                (int)(cornerNW.getY() * 2.0 / 3.0 + centerNode.getY() * 1.0 / 3.0)); // (133, 133) approx.
        BoardNode nw2 = new BoardNode("NW2", (int)(cornerNW.getX() * 1.0 / 3.0 + centerNode.getX() * 2.0 / 3.0),
                (int)(cornerNW.getY() * 1.0 / 3.0 + centerNode.getY() * 2.0 / 3.0)); // (217, 217) approx.
        // CENTER -> CORNER_SW (SW1, SW2) - 중앙에서 좌하단
        BoardNode sw1 = new BoardNode("SW1", (int)(centerNode.getX() * 2.0 / 3.0 + cornerSW.getX() * 1.0 / 3.0),
                (int)(centerNode.getY() * 2.0 / 3.0 + cornerSW.getY() * 1.0 / 3.0)); // (217, 383) approx.
        BoardNode sw2 = new BoardNode("SW2", (int)(centerNode.getX() * 1.0 / 3.0 + cornerSW.getX() * 2.0 / 3.0),
                (int)(centerNode.getY() * 1.0 / 3.0 + cornerSW.getY() * 2.0 / 3.0)); // (133, 467) approx.
        // CENTER -> START_NODE (SE1, SE2) - 중앙에서 우하단
        BoardNode se1 = new BoardNode("SE1", (int)(centerNode.getX() * 2.0 / 3.0 + startNode.getX() * 1.0 / 3.0),
                (int)(centerNode.getY() * 2.0 / 3.0 + startNode.getY() * 1.0 / 3.0)); // (383, 383) approx.
        BoardNode se2 = new BoardNode("SE2", (int)(centerNode.getX() * 1.0 / 3.0 + startNode.getX() * 2.0 / 3.0),
                (int)(centerNode.getY() * 1.0 / 3.0 + startNode.getY() * 2.0 / 3.0)); // (467, 467) approx.


        // --- 2. 노드 간 연결 설정 (addNextNode) ---
        // 외곽 경로 연결 (시계 방향)
        startNode.addNextNode(e1);
        e1.addNextNode(e2);
        e2.addNextNode(e3);
        e3.addNextNode(e4);
        e4.addNextNode(cornerNE); // -> 우상단 코너

        cornerNE.addNextNode(n1); // 직진
        n1.addNextNode(n2);
        n2.addNextNode(n3);
        n3.addNextNode(n4);
        n4.addNextNode(cornerNW); // -> 좌상단 코너

        cornerNW.addNextNode(w1); // 직진
        w1.addNextNode(w2);
        w2.addNextNode(w3);
        w3.addNextNode(w4);
        w4.addNextNode(cornerSW); // -> 좌하단 코너

        cornerSW.addNextNode(s1); // 직진
        s1.addNextNode(s2);
        s2.addNextNode(s3);
        s3.addNextNode(s4);
        s4.addNextNode(startNode); // -> 시작점으로 복귀 (골인 경로)

        // 내부 경로 연결 (지름길)
        cornerNE.addNextNode(ne1); // 우상단에서 중앙 방향 분기
        ne1.addNextNode(ne2);
        ne2.addNextNode(centerNode); // -> 중앙

        cornerNW.addNextNode(nw1); // 좌상단에서 중앙 방향 분기
        nw1.addNextNode(nw2);
        nw2.addNextNode(centerNode); // -> 중앙

        centerNode.addNextNode(sw1); // 중앙에서 좌하단 코너 방향 분기
        sw1.addNextNode(sw2);
        sw2.addNextNode(cornerSW); // -> 좌하단 코너 합류

        centerNode.addNextNode(se1); // 중앙에서 우하단(시작점) 방향 분기
        se1.addNextNode(se2);
        se2.addNextNode(startNode); // -> 시작점 합류

        // --- 3. 모든 노드를 리스트에 담아 Board 객체 생성 ---
        List<BoardNode> allNodes = List.of(
                startNode, cornerNE, cornerNW, cornerSW, centerNode,
                e1, e2, e3, e4, n1, n2, n3, n4, w1, w2, w3, w4, s1, s2, s3, s4,
                ne1, ne2, nw1, nw2, sw1, sw2, se1, se2
        );

        // 노드 개수 확인 (29개여야 함)
        if (allNodes.size() != 29) {
            System.err.println("Warning: Expected 29 nodes, but found " + allNodes.size());
        }

        return new Board(new ArrayList<>(allNodes)); // 가변 리스트로 반환
    }    /**
     * 현재 보드 내 모든 노드 리스트 반환.
     */
    public List<BoardNode> getNodes() {
        return nodes;
    }

    /**
     * "START_FINISH"라는 id의 노드를 찾는 헬퍼.
     */
    public BoardNode getStartNode() {
        return nodes.stream()
                .filter(n -> "START_NODE".equals(n.getId()))
                .findFirst()
                .orElse(null);
    }

    // DFS 기반 경로 탐색 (기존 코드 유지)
    /**
     * DFS 기반으로 steps만큼 이동 가능한 모든 마지막 노드를 찾는 로직.
     * 갈림길이 여러 개라면 여러 후보가 반환될 수 있음.
     *
     * 참고: 이 메소드는 빽도(-1) 이동을 직접 처리하지 않습니다.
     * 빽도 처리는 YutGame.movePiece 등 상위 레벨에서 별도 로직 필요.
     */
    public List<BoardNode> getPossibleNextNodes(BoardNode current, int steps) {
        List<BoardNode> results = new ArrayList<>();
        // 빽도(음수 steps)는 여기서 처리하지 않음. YutGame.movePiece 에서 별도 처리 필요.
        if (steps < 0) {
            // 빽도 로직은 이 메소드 범위 밖
            // 필요하다면 getPossiblePreviousNodes 같은 별도 메소드나,
            // YutGame 클래스에서 현재 노드의 previous 연결 (미구현) 또는
            // 전체 노드 검색 등으로 구현해야 함.
            // 임시로 현재 노드 반환 또는 빈 리스트 반환 등을 고려할 수 있으나,
            // 게임 규칙상 빽도는 특수 처리되므로 여기서는 빈 리스트 반환.
            return results;
        }
        dfsPaths(current, steps, new ArrayList<>(), results); // 경로 추적용 visited 추가
        return results;
    }

    // DFS 수정: 무한 루프 방지 및 경로 추적 (빽도 미지원)
    private void dfsPaths(BoardNode node, int steps, List<BoardNode> visited, List<BoardNode> results) {
        // 현재 노드를 방문 목록에 추가
        visited.add(node);

        if (steps == 0) {
            // 최종 목적지 도달
            if (!results.contains(node)) { // 중복 추가 방지
                results.add(node);
            }
            // 현재 경로 탐색 종료, 방문 목록에서 현재 노드 제거 (백트래킹)
            visited.removeLast();
            return;
        }

        for (BoardNode next : node.getNextNodes()) {
            // 다음 노드가 현재 경로상에 이미 있다면 무한 루프 방지 (단순 사이클 방지)
            // 주의: 윷놀이에서는 같은 노드를 다시 방문하는 경우가 많으므로,
            //       단순히 visited.contains(next) 로 체크하면 안될 수 있음.
            //       여기서는 기본적인 탐색 예시로 남겨둠.
            //       더 정확한 규칙(예: 특정 노드 통과 시 경로 변경 등)은 YutGame 레벨에서 필요.
            // if (!visited.contains(next)) { // -> 이 조건은 윷놀이에 부적합할 수 있음
            dfsPaths(next, steps - 1, visited, results);
            // }
        }

        // 모든 다음 노드 탐색 후, 현재 노드를 방문 목록에서 제거 (백트래킹)
        if (!visited.isEmpty()) {
            visited.removeLast();
        }
    }

    // --- (옵션) 빽도 처리를 위한 이전 노드 찾는 로직 ---
    // BoardNode에 previousNodes 리스트를 추가하거나,
    // 이 메소드처럼 전체 노드를 순회하며 찾아야 함.
    /**
     * 지정된 노드(target)로 이동할 수 있는 모든 이전 노드를 찾는 메소드.
     * 빽도(-1) 계산 시 사용될 수 있음.
     * 성능: 매번 전체 노드를 순회하므로 노드가 많으면 비효율적일 수 있음.
     */
    public List<BoardNode> getPossiblePreviousNodes(BoardNode target) {
        List<BoardNode> previousNodes = new ArrayList<>();
        if (target == null) return previousNodes;

        for (BoardNode node : this.nodes) {
            for (BoardNode next : node.getNextNodes()) {
                if (next.equals(target)) {
                    previousNodes.add(node);
                    break; // 같은 노드에서 여러 번 연결될 일은 없다고 가정
                }
            }
        }
        return previousNodes;
    }

}