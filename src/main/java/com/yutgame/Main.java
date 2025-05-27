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

        /*
        boolean useFx = false;
        for (String arg : args) {
            if ("fx".equalsIgnoreCase(arg) || "--fx".equalsIgnoreCase(arg)) {
                useFx = true;
                break;
            }
        }

        if (!useFx) {
            //───────── JavaFX 모드 ─────────
            Application.launch(FXAppLauncher.class, args);

        } else {
            //───────── Swing 모드 (기존) ─────────
            SwingUtilities.invokeLater(() -> {
                YutGameController controller = new YutGameController();
                controller.initializeGame();   // 내부에서 Swing View 호출
            });
        }
         */


    }
}
