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
}
