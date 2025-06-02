package main.java.com.yutgame.model;

import java.util.List;

public interface YutBoard {
    // 보드 내 모든 노드 리스트를 반환
    List<BoardNode> getNodes();

    // 보드의 시작 노드를 반환
    BoardNode getStartNode();

    // 현재 노드에서 steps 만큼 전진했을 때 가능한 모든 노드
    List<BoardNode> getPossibleNextNodes(BoardNode current, int steps);

    // 특정 노드에서 가능한 이전 노드 (백도 참고용)
    List<BoardNode> getPossiblePreviousNodes(BoardNode target);

    // 현재 노드에서 steps 만큼 전진할 때 거치는 모든 노드 (isgoal에 사용)
    List<BoardNode> getPaths();

    // path 리셋
    void pathClear();

    /**
     * 지름길 규칙이 적용된 유효한 목적지를 반환
     * @param currentNode 현재 노드
     * @param steps 이동할 칸 수
     * @return 지름길 규칙이 적용된 목적지 리스트
     */
    List<BoardNode> getValidDestinationsWithShortcutRules(BoardNode currentNode, int steps);
}
