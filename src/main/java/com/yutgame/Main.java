/*  ───────────────────────────────────────────────────────────────
 *  Main.java ― 런처
 *  ▸ 기본값  : Swing UI 실행
 *  ▸ 옵션    : 프로그램 실행 시 인자에 "fx" 또는 "--fx" 가 있으면 JavaFX UI 실행
 *  ─────────────────────────────────────────────────────────────── */
package main.java.com.yutgame;

import main.java.com.yutgame.controller.YutGameController;
import main.java.com.yutgame.view.fx.FXAppLauncher;            // JavaFX Application
import javax.swing.SwingUtilities;
import javafx.application.Application;

public class Main {

    public static void main(String[] args) {

        YutGameController controller = new YutGameController();
        // javaFX 호출 시 주석 제거
        // controller.initializeGame();

        // swing 호출 시 주석 제거

        SwingUtilities.invokeLater(() -> {
            controller.initializeGame();   // 내부에서 Swing View 호출
        });

    }
}
