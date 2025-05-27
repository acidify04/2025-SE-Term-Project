package main.java.com.yutgame.view.fx;

import main.java.com.yutgame.view.fx.router.ViewRouter;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.Cursor;

import java.util.*;
import java.io.InputStream;
import java.util.function.Consumer;

public class BoardSelectView {

    public enum BoardChoice { SQUARE, PENTAGON, HEXAGON }
    private BoardChoice selected = BoardChoice.SQUARE;

    private final Scene scene;
    public Scene scene() { return scene; }
    private final List<ImageView> boardImages = new ArrayList<>();
    private final List<ImageView> textImages = new ArrayList<>();
    private final List<StackPane> cardPanes = new ArrayList<>();

    public BoardSelectView(ViewRouter router, Consumer<Integer> onBoardSelected) {

        StackPane square   = card("/fx/board/setting/board_square.png", "/fx/text/txt_square.png",
                BoardChoice.SQUARE);
        StackPane pentagon = card("/fx/board/setting/board_pentagon.png", "/fx/text/txt_pentagon.png",
                BoardChoice.PENTAGON);
        StackPane hexagon  = card("/fx/board/setting/board_hexagon.png", "/fx/text/txt_hexagon.png",
                BoardChoice.HEXAGON);

        // 보드 카드들을 더 아래로 이동하고 크기 증가
        HBox cardBox = new HBox(40, square, pentagon, hexagon);  // 간격 증가
        cardBox.setAlignment(Pos.CENTER);
        cardBox.setTranslateY(180);  // 더 아래로 이동

        // 버튼들을 적절한 위치로 조정
        ImageView back = clickableImage("/fx/button/btn_back.png",
                e -> router.showTitle());
        ImageView next = clickableImage("/fx/button/btn_next.png",
                e -> {
                    onBoardSelected.accept(selected.ordinal());  // 선택된 보드 인덱스를 외부로 전달!
                    router.showPlayerPieceSelect();
                });
        back.setFitWidth(250);
        next.setFitWidth(250);

        HBox btnLine = new HBox(350, back, next);
        btnLine.setAlignment(Pos.CENTER);
        btnLine.setTranslateY(60);  // 적절한 위치로 조정

        VBox content = new VBox(60, cardBox, btnLine);
        content.setAlignment(Pos.CENTER);

        // 배경 이미지
        ImageView bgImage = safeLoadImage("/fx/background/bg_board.png");
        bgImage.setFitWidth(1028);
        bgImage.setFitHeight(672);
        bgImage.setPreserveRatio(false);

        StackPane root = new StackPane(bgImage, content);
        this.scene = new Scene(root, 870, 570);
    }

    private StackPane card(String boardImg, String txtImg, BoardChoice kind) {
        // 보드 이미지 - 완전히 동일한 크기로 강제 설정
        ImageView board = safeLoadImage(boardImg);
        board.setFitWidth(230);   // 크기 증가
        board.setFitHeight(230);  // 정사각형으로 강제 설정
        board.setPreserveRatio(false);  // 비율 무시하고 강제 크기 적용
        board.setSmooth(true);
        boardImages.add(board);

        // 텍스트 이미지 - 완전히 동일한 크기로 강제 설정
        ImageView txt = safeLoadImage(txtImg);
        txt.setFitWidth(220);   // 보드와 동일한 너비
        txt.setFitHeight(60);   // 높이 고정으로 균일하게
        txt.setPreserveRatio(false);  // 비율 무시하고 강제 크기 적용
        txt.setSmooth(true);
        textImages.add(txt);

        // VBox도 고정 크기로 설정
        VBox box = new VBox(10, board, txt);  // 간격 증가
        box.setAlignment(Pos.CENTER);
        box.setPrefWidth(240);   // VBox 너비 고정
        box.setPrefHeight(280);  // VBox 높이 고정

        StackPane pane = new StackPane(box);
        pane.setPrefWidth(220);   // StackPane 크기도 고정
        pane.setPrefHeight(280);
        cardPanes.add(pane);

        // 클릭 이벤트
        int index = boardImages.size() - 1;
        pane.setOnMouseClicked(e -> {
            selected = kind;
            focus(index);
        });
        pane.setCursor(Cursor.HAND);

        // 호버 효과
        pane.setOnMouseEntered(e -> {
            if (!isSelected(index)) {
                pane.setOpacity(0.8);
                pane.setScaleX(1.03);
                pane.setScaleY(1.03);
            }
        });

        pane.setOnMouseExited(e -> {
            if (!isSelected(index)) {
                pane.setOpacity(1.0);
                pane.setScaleX(1.0);
                pane.setScaleY(1.0);
            }
        });

        return pane;
    }

    private void focus(int selectedIndex) {
        // 모든 카드 초기화
        for (int i = 0; i < boardImages.size(); i++) {
            ImageView board = boardImages.get(i);
            board.setEffect(null);
            board.setImage(new Image("/fx/board/setting/" + getBoardImageName(i, 0)));
            ImageView text = textImages.get(i);
            StackPane pane = cardPanes.get(i);

            // 크기와 투명도 초기화
            pane.setOpacity(1.0);
            pane.setScaleX(1.0);
            pane.setScaleY(1.0);

            // 텍스트 투명도 조정
            text.setOpacity(0.5);
            text.setImage(new Image("/fx/text/txt_" + getBoardImageName(i, 1)));
        }

        // 선택된 보드 효과 적용
        ImageView selectedBoard = boardImages.get(selectedIndex);
        selectedBoard.setImage(new Image("/fx/board/setting/" + getBoardImageName(selectedIndex, 2)));

        // 선택된 텍스트에 효과 적용
        ImageView selectedText = textImages.get(selectedIndex);
        selectedText.setImage(new Image("/fx/text/txt_" + getBoardImageName(selectedIndex, 2)));

        // 선택된 카드 전체에 살짝 확대 효과
        StackPane selectedPane = cardPanes.get(selectedIndex);
        selectedPane.setScaleX(1.02);
        selectedPane.setScaleY(1.02);
    }

    private String getBoardImageName(int index, int change) {  // change = 2 : highlight
        String[] baseNames = { "square", "pentagon", "hexagon" };
        String base = baseNames[index];  // 0=사각형, 1=오각형, 2=육각형
        if (change == 1) return base + ".png";
        else if (change == 2) return base + "_highlight.png";
        else return "board_" + base + ".png";
    }

    private boolean isSelected(int index) {
        return boardImages.get(index).getEffect() != null;
    }

    // 버튼 크기 및 클릭 가능한 이미지
    private ImageView clickableImage(String path, EventHandler<ActionEvent> act) {
        ImageView iv = safeLoadImage(path);
        iv.setFitWidth(200);  // 적절한 크기로 조정
        iv.setPreserveRatio(true);
        iv.setSmooth(true);

        iv.setOnMouseClicked(e -> act.handle(new ActionEvent()));
        iv.setCursor(Cursor.HAND);

        // 호버 효과
        iv.setOnMouseEntered(e -> {
            iv.setOpacity(0.8);
            iv.setScaleX(1.05);
            iv.setScaleY(1.05);
        });

        iv.setOnMouseExited(e -> {
            iv.setOpacity(1.0);
            iv.setScaleX(1.0);
            iv.setScaleY(1.0);
        });

        return iv;
    }

    // 안전한 이미지 로딩
    private ImageView safeLoadImage(String path) {
        try {
            InputStream imageStream = getClass().getResourceAsStream(path);
            if (imageStream == null) {
                System.err.println("이미지를 찾을 수 없습니다: " + path);
                return new ImageView();
            }
            return new ImageView(new Image(imageStream));
        } catch (Exception e) {
            System.err.println("이미지 로딩 실패: " + path + " - " + e.getMessage());
            return new ImageView();
        }
    }
}
