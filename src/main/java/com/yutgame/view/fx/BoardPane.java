package main.java.com.yutgame.view.fx;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.util.Duration;
import main.java.com.yutgame.controller.YutGameController;
import main.java.com.yutgame.model.BoardNode;
import main.java.com.yutgame.model.Piece;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javafx.animation.ScaleTransition;
import javafx.animation.ParallelTransition;

import javafx.animation.ScaleTransition;
import javafx.animation.FadeTransition;
import javafx.animation.Animation;
import javafx.util.Duration;
import javafx.application.Platform;
import main.java.com.yutgame.model.YutThrowResult;


/**
 * 실제 윷놀이 판
 */
public class BoardPane extends Pane {

    // 노드(연결선)와 말(피스)을 구분하기 위한 2개 레이어
    private Pane nodeLayer = new Pane();   // 노드 & 연결선
    private Pane pieceLayer = new Pane();  // 말(피스) 아이콘

    private static final int NODE_SIZE = 35;
    private static final int PIECE_SIZE_X = 40;
    private static final int PIECE_SIZE_Y = 55;

    private final YutGameController controller;
    private final GameBoardView gameBoardView;

    // Node -> Circle 매핑
    private Map<BoardNode, Circle> nodeCircleMap = new HashMap<>();

    // 하이라이트 중인 노드 목록 & 클릭 콜백
    private List<BoardNode> highlightedNodes = new ArrayList<>();
    private Consumer<BoardNode> nodeClickCallback;

    // ★ 추가: 각 노드별 FadeTransition 관리
    private Map<BoardNode, FadeTransition> activeTransitions = new HashMap<>();

    public BoardPane(YutGameController controller, GameBoardView gameBoardView) {
        this.controller = controller;
        this.gameBoardView = gameBoardView;
        this.setPrefSize(440, 440);
        this.setMaxSize(440, 440);
        this.setMinSize(440, 440);

        // 레이어 두 개를 Pane에 추가
        this.getChildren().addAll(nodeLayer, pieceLayer);

        drawBoard();
    }

    /**
     * 전체 보드를 다시 그린다
     */
    public void drawBoard() {
        System.out.println("=== drawBoard 시작 ===");

        // ★ 추가: 기존 클릭 영역과 하이라이트 완전 정리
        clearAllHighlights();
        clearAllClickAreas();

        // 레이어 초기화
        nodeLayer.getChildren().clear();
        pieceLayer.getChildren().clear();

        // 맵 & 하이라이트 상태도 초기화
        nodeCircleMap.clear();
        highlightedNodes.clear();
        nodeClickCallback = null;

        System.out.println(">>> 노드 간 연결선 그리기");
        // 1) 노드 간 연결선 (nodeLayer에 그린다)
        for (BoardNode node : controller.getBoard().getNodes()) {
            for (BoardNode nxt : node.getNextNodes()) {
                int x1 = node.getX() + NODE_SIZE / 2;
                int y1 = node.getY() + NODE_SIZE / 2;
                int x2 = nxt.getX() + NODE_SIZE / 2;
                int y2 = nxt.getY() + NODE_SIZE / 2;

                Line line = new Line(x1, y1, x2, y2);
                line.setStroke(Color.GRAY);

                nodeLayer.getChildren().add(line);
            }
        }

        System.out.println(">>> 노드 그리기");
        // 2) 노드 그리기 (nodeLayer)
        for (BoardNode node : controller.getBoard().getNodes()) {
            drawNode(node);
        }

        System.out.println(">>> 말 그리기");
        // 3) 말(피스) 그리기 (pieceLayer)
        for (BoardNode node : controller.getBoard().getNodes()) {
            drawPieces(node);
        }

        // ★ 추가: 보드 그리기 완료 후 말 클릭 권한 업데이트
        gameBoardView.updateAllPlayerButtonStates();
        System.out.println("=== drawBoard 완료 ===");
    }

    /**
     * 모든 클릭 영역 제거 (투명한 Circle들)
     */
    private void clearAllClickAreas() {
        System.out.println(">>> clearAllClickAreas 호출");

        // nodeLayer에서 투명한 클릭 영역들 제거
        nodeLayer.getChildren().removeIf(child -> {
            if (child instanceof Circle circle) {
                // 투명한 클릭 영역인지 확인 (fill이 TRANSPARENT이고 stroke도 TRANSPARENT)
                boolean isClickArea = (circle.getFill() == Color.TRANSPARENT &&
                        circle.getStroke() == Color.TRANSPARENT);
                if (isClickArea) {
                    System.out.println("  - 클릭 영역 제거: " + circle.getCenterX() + ", " + circle.getCenterY());
                    // 이벤트 핸들러도 제거
                    circle.setOnMouseClicked(null);
                    circle.setOnMouseEntered(null);
                    circle.setOnMouseExited(null);
                }
                return isClickArea;
            }
            return false;
        });

        System.out.println(">>> clearAllClickAreas 완료");
    }

    /**
     * 노드(Circle) 생성 & nodeCircleMap에 저장
     */
    private void drawNode(BoardNode node) {
        String id = node.getId();
        Color color = switch (id) {
            case "A" -> Color.web("FF7A7C");
            case "B" -> Color.web("7AABFF");
            case "C" -> Color.web("7AFF87");
            case "D" -> Color.web("FF7AFD");
            case "E" -> Color.web("9548E7");
            case "START_NODE" -> Color.web("FFF67A");
            case "CENTER" -> Color.LIGHTSLATEGREY;
            default -> Color.LIGHTGRAY;
        };

        Circle circle = new Circle(node.getX() + NODE_SIZE / 2, node.getY() + NODE_SIZE / 2, NODE_SIZE / 2, color);

        // ★ 디버깅: Circle 생성 확인
        System.out.println("Circle 생성: " + id + " at (" + circle.getCenterX() + ", " + circle.getCenterY() + ") radius=" + circle.getRadius());

        nodeLayer.getChildren().add(circle);
        nodeCircleMap.put(node, circle);

        // ★ 디버깅: nodeCircleMap 저장 확인
        System.out.println("nodeCircleMap에 저장됨: " + id + " -> " + (nodeCircleMap.get(node) != null));
    }

    /**
     * 해당 노드에 있는 말(피스)들을 pieceLayer에 그린다
     */
    private void drawPieces(BoardNode node) {
        int idx = 0;
        for (Piece p : node.getOccupantPieces()) {
            if (p.isFinished() || p.getCurrentNode() == null) continue;

            String playerName = p.getOwner().getName();
            String path = switch (playerName) {
                case "P1" -> "/fx/piece/piece_1.png";
                case "P2" -> "/fx/piece/piece_2.png";
                case "P3" -> "/fx/piece/piece_3.png";
                case "P4" -> "/fx/piece/piece_4.png";
                default -> "/fx/piece/piece_1.png";
            };

            int px = node.getX() + (idx * 10);
            int py = node.getY() - 13 + (idx * 10);

            StackPane stackPane = clickableImage(px, py, path, e -> {
                // 말 클릭 시
                gameBoardView.onPieceClicked(p);
            });

            // ★ 수정: 클릭 권한 체크 강화된 디버깅
            int ownerIndex = p.getOwner().getIndex();
            int uiCurrentIndex = gameBoardView.getCurrentPlayerIndex();
            boolean isMyTurn = (ownerIndex == uiCurrentIndex);
            YutThrowResult selectedYutResult = gameBoardView.getCurrentlySelectedYutResult();
            boolean hasSelectedYut = (selectedYutResult != null);
            boolean canClick = isMyTurn && hasSelectedYut;

            System.out.println("말 상태 체크 - Node: " + node.getId() +
                    ", Piece Owner: " + ownerIndex +
                    ", Current Player: " + uiCurrentIndex +
                    ", IsMyTurn: " + isMyTurn +
                    ", Selected Yut: " + selectedYutResult +
                    ", HasSelectedYut: " + hasSelectedYut +
                    ", CanClick: " + canClick);

            if (isMyTurn) {
                if (canClick) {
                    // ★ 윷 선택했고 내 턴 - 완전히 활성화
                    stackPane.setDisable(false);
                    stackPane.setOpacity(1.0);
                    stackPane.setCursor(Cursor.HAND);
                    stackPane.setStyle("-fx-effect: dropshadow(gaussian, gold, 10, 0.7, 0, 0);");

                    System.out.println(">>> 말 활성화: " + p + " (클릭 가능)");

                    // 클릭 가능한 말에 호버 효과
                    stackPane.setOnMouseEntered(e -> {
                        if (!stackPane.isDisabled()) {
                            stackPane.setScaleX(1.15);
                            stackPane.setScaleY(1.15);
                            stackPane.setStyle("-fx-effect: dropshadow(gaussian, yellow, 15, 0.9, 0, 0);");
                        }
                    });

                    stackPane.setOnMouseExited(e -> {
                        stackPane.setScaleX(1.0);
                        stackPane.setScaleY(1.0);
                        stackPane.setStyle("-fx-effect: dropshadow(gaussian, gold, 10, 0.7, 0, 0);");
                    });

                } else {
                    // ★ 내 턴이지만 윷 미선택 - 클릭 불가, 안내 효과
                    stackPane.setDisable(true);
                    stackPane.setOpacity(0.8);
                    stackPane.setCursor(Cursor.DEFAULT);
                    stackPane.setStyle("-fx-effect: dropshadow(gaussian, lightgray, 3, 0.3, 0, 0);");

                    System.out.println(">>> 말 비활성화: " + p + " (윷 선택 필요)");

                    // 윷 선택 안내를 위한 호버 효과
                    stackPane.setOnMouseEntered(e -> {
                        stackPane.setStyle("-fx-effect: dropshadow(gaussian, orange, 5, 0.5, 0, 0);");
                    });

                    stackPane.setOnMouseExited(e -> {
                        stackPane.setStyle("-fx-effect: dropshadow(gaussian, lightgray, 3, 0.3, 0, 0);");
                    });
                }
            } else {
                // ★ 다른 플레이어의 말들 - 클릭 불가, 많이 흐리게
                stackPane.setDisable(true);
                stackPane.setOpacity(0.3);
                stackPane.setCursor(Cursor.DEFAULT);
                stackPane.setStyle("");

                System.out.println(">>> 다른 플레이어 말: " + p + " (비활성화)");
            }

            pieceLayer.getChildren().add(stackPane);

            if (gameBoardView.isHighlightActive()) {
                if (isMyTurn && canClick) {
                    // 내 말은 그대로 두거나 살짝만 뒤로
                } else {
                    stackPane.toBack(); // 다른 말들은 뒤로
                }
            }

            idx++;
        }
    }

    /**
     * 말(피스) 아이콘을 그리는 StackPane 생성
     */
    private StackPane clickableImage(int px, int py, String path, EventHandler<ActionEvent> act) {
        StackPane stackPane = new StackPane();
        stackPane.setMaxSize(PIECE_SIZE_X, PIECE_SIZE_Y);

        ImageView iv = new ImageView(new Image(path));
        iv.setFitWidth(PIECE_SIZE_X);
        iv.setFitHeight(PIECE_SIZE_Y);

        stackPane.getChildren().add(iv);
        stackPane.setLayoutX(px);
        stackPane.setLayoutY(py);

        // 클릭 이벤트
        stackPane.setOnMouseClicked(e -> act.handle(new ActionEvent()));
        stackPane.setCursor(Cursor.HAND);

        // 호버 효과 (선택사항)
        stackPane.setOnMouseEntered(e -> {
            stackPane.setOpacity(0.8);
            stackPane.setScaleX(1.05);
            stackPane.setScaleY(1.05);
        });
        stackPane.setOnMouseExited(e -> {
            stackPane.setOpacity(1.0);
            stackPane.setScaleX(1.0);
            stackPane.setScaleY(1.0);
        });

        return stackPane;
    }

    // ====================== 노드 하이라이트 기능 =========================

    /**
     * 노드 목록에 대해 하이라이트(노란 테두리, 깜빡임) 효과를 주고,
     * 노드 클릭 시 callback을 호출
     */
    public void highlightNodes(List<BoardNode> nodes, Consumer<BoardNode> callback, boolean finishMode) {
        System.out.println("=== highlightNodes 진입 ===");
        System.out.println("- 노드 개수: " + nodes.size());

        // ★ 수정: 기존 클릭 영역과 하이라이트 완전 정리
        clearAllClickAreas();
        clearAllHighlights();

        // ★ 수정: 말 클릭을 완전히 차단하고 노드 클릭을 우선
        pieceLayer.setMouseTransparent(true);
        System.out.println("- pieceLayer mouseTransparent 설정됨");

        this.nodeClickCallback = callback;
        this.highlightedNodes.addAll(nodes);

        System.out.println(">>> 노드별 처리 시작");
        for (int i = 0; i < nodes.size(); i++) {
            BoardNode node = nodes.get(i);
            System.out.println(">>> [" + i + "] 노드 처리: " + node.getId());

            Circle circle = nodeCircleMap.get(node);
            if (circle == null) {
                System.err.println(">>> Circle이 null! 노드: " + node.getId());
                continue;
            }

            // ★ 수정: 부드러운 하이라이트 효과
            circle.setVisible(true);
            circle.setDisable(false);
            circle.setMouseTransparent(false);

            // ★ 수정: 부드러운 색상 (연한 파란색 배경 + 파란색 테두리)
            circle.setFill(Color.LIGHTBLUE.deriveColor(0, 1, 1, 0.6));
            circle.setStroke(Color.DODGERBLUE);
            circle.setStrokeWidth(4.0);
            circle.setRadius(20);

            // ★ 수정: 부드러운 깜빡임 애니메이션
            FadeTransition fadeTransition = new FadeTransition(Duration.millis(800), circle);
            fadeTransition.setFromValue(1.0);
            fadeTransition.setToValue(0.4);
            fadeTransition.setCycleCount(Animation.INDEFINITE);
            fadeTransition.setAutoReverse(true);
            fadeTransition.play();

            // ★ 수정: 크기 변화 애니메이션
            ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(800), circle);
            scaleTransition.setFromX(1.0);
            scaleTransition.setFromY(1.0);
            scaleTransition.setToX(1.2);
            scaleTransition.setToY(1.2);
            scaleTransition.setCycleCount(Animation.INDEFINITE);
            scaleTransition.setAutoReverse(true);
            scaleTransition.play();

            // ★ 중요: Circle을 맨 앞으로 가져와서 말보다 위에 배치
            circle.toFront();

            // ★ 수정: 클릭 영역을 더 크고 확실하게 만들기
            Circle clickArea = new Circle(circle.getCenterX(), circle.getCenterY(), 30); // 더 크게
            clickArea.setFill(Color.TRANSPARENT);
            clickArea.setStroke(Color.TRANSPARENT);
            clickArea.setMouseTransparent(false);
            clickArea.setId("clickArea_" + node.getId());

            // ★ 수정: 클릭 이벤트 강화 - Platform.runLater 사용
            clickArea.setOnMouseClicked(e -> {
                System.out.println("!!! 클릭 영역 이벤트 발생 !!! 노드: " + node.getId());
                System.out.println("!!! 이벤트 소스: " + e.getSource());
                System.out.println("!!! 클릭 위치: (" + e.getX() + ", " + e.getY() + ")");

                if (nodeClickCallback != null) {
                    System.out.println("!!! 콜백 실행 시작");

                    // ★ 추가: Platform.runLater로 콜백 실행 지연
                    Platform.runLater(() -> {
                        try {
                            // 애니메이션 중지
                            fadeTransition.stop();
                            scaleTransition.stop();
                            nodeClickCallback.accept(node);
                            System.out.println("!!! 콜백 실행 완료");
                        } catch (Exception ex) {
                            System.err.println("!!! 콜백 실행 중 오류: " + ex.getMessage());
                            ex.printStackTrace();
                        }
                    });
                } else {
                    System.out.println("!!! 콜백이 null");
                }
                e.consume();
            });

            // ★ 마우스 호버 효과
            clickArea.setOnMouseEntered(e -> {
                System.out.println("### 마우스 진입: " + node.getId() + " (ID: " + clickArea.getId() + ")");
                circle.setFill(Color.LIGHTGREEN.deriveColor(0, 1, 1, 0.8));
                clickArea.setCursor(Cursor.HAND);
            });

            clickArea.setOnMouseExited(e -> {
                System.out.println("### 마우스 종료: " + node.getId() + " (ID: " + clickArea.getId() + ")");
                circle.setFill(Color.LIGHTBLUE.deriveColor(0, 1, 1, 0.6));
            });

            clickArea.setCursor(Cursor.HAND);
            clickArea.toFront(); // 클릭 영역을 맨 앞에

            // ★ 클릭 영역을 nodeLayer에 추가
            nodeLayer.getChildren().add(clickArea);
            System.out.println(">>> 클릭 영역 추가됨: " + clickArea.getId() + " at (" + clickArea.getCenterX() + ", " + clickArea.getCenterY() + ")");

            System.out.println(">>> 노드 처리 완료: " + node.getId());
        }

        System.out.println("=== highlightNodes 완료 ===");
        System.out.println(">>> nodeLayer 자식 개수: " + nodeLayer.getChildren().size());
    }

    /**
     * 노드 하이라이트 해제 (테두리 제거, 클릭 이벤트 제거 등)
     */
    public void unhighlightNodes(List<BoardNode> nodes) {
        System.out.println("하이라이트 해제 - 노드 개수: " + nodes.size());

        // ★ 수정: 말 클릭 가능 복원
        pieceLayer.setMouseTransparent(false);

        for (BoardNode node : nodes) {
            Circle circle = nodeCircleMap.get(node);
            if (circle != null) {
                // ★ 수정: 모든 애니메이션 중지
                circle.getTransforms().clear();

                // ★ 수정: 크기와 opacity 복원
                circle.setScaleX(1.0);
                circle.setScaleY(1.0);
                circle.setOpacity(1.0);

                // ★ 수정: 원래 색상으로 복원
                String id = node.getId();
                Color originalColor = switch (id) {
                    case "A" -> Color.web("FF7A7C");
                    case "B" -> Color.web("7AABFF");
                    case "C" -> Color.web("7AFF87");
                    case "D" -> Color.web("FF7AFD");
                    case "E" -> Color.web("9548E7");
                    case "START_NODE" -> Color.web("FFF67A");
                    case "CENTER" -> Color.LIGHTSLATEGREY;
                    default -> Color.LIGHTGRAY;
                };

                circle.setFill(originalColor);
                circle.setStroke(Color.TRANSPARENT);
                circle.setStrokeWidth(0);
                circle.setRadius(NODE_SIZE / 2); // 원래 크기로 복원

                // 이벤트 핸들러 제거
                circle.setOnMouseClicked(null);
                circle.setOnMouseEntered(null);
                circle.setOnMouseExited(null);
                circle.setCursor(Cursor.DEFAULT);

                System.out.println("노드 하이라이트 해제: " + node.getId());
            }
        }

        // ★ 수정: 해당 노드들의 클릭 영역만 제거
        for (BoardNode node : nodes) {
            String targetId = "clickArea_" + node.getId();
            nodeLayer.getChildren().removeIf(child -> {
                if (child instanceof Circle clickArea && targetId.equals(clickArea.getId())) {
                    System.out.println("  - 특정 클릭 영역 제거: " + targetId);
                    // 이벤트 핸들러 제거
                    clickArea.setOnMouseClicked(null);
                    clickArea.setOnMouseEntered(null);
                    clickArea.setOnMouseExited(null);
                    return true;
                }
                return false;
            });
        }

        nodeClickCallback = null;
        System.out.println("하이라이트 해제 완료");
    }

    // ★ 추가: highlightedNodes 접근을 위한 public 메소드들
    public boolean hasHighlightedNodes() {
        return !highlightedNodes.isEmpty();
    }

    /**
     * 모든 하이라이트 해제
     */
    public void clearAllHighlights() {
        System.out.println(">>> clearAllHighlights 호출");
        if (!highlightedNodes.isEmpty()) {
            System.out.println(">>> 해제할 하이라이트 노드 개수: " + highlightedNodes.size());

            // ★ 수정: 각 노드의 애니메이션도 중지
            for (BoardNode node : highlightedNodes) {
                Circle circle = nodeCircleMap.get(node);
                if (circle != null) {
                    // 모든 진행 중인 애니메이션 중지
                    circle.getTransforms().clear();
                    circle.setScaleX(1.0);
                    circle.setScaleY(1.0);
                    circle.setOpacity(1.0);
                }
            }

            unhighlightNodes(new ArrayList<>(highlightedNodes));
            highlightedNodes.clear();
        } else {
            System.out.println(">>> 해제할 하이라이트 노드 없음");
        }
    }

    public List<BoardNode> getHighlightedNodes() {
        return new ArrayList<>(highlightedNodes); // 방어적 복사
    }
}