package main.java.com.yutgame;

import main.java.com.yutgame.controller.YutGameController;
import javax.swing.JOptionPane;

public class Main {

    public static void main(String[] args) {

        // 사용자에게 UI 선택을 묻는 다이얼로그
        String[] options = {"Swing UI", "JavaFX UI"};
        int choice = JOptionPane.showOptionDialog(
                null,
                "어떤 UI를 사용하시겠습니까?",
                "UI 선택",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        YutGameController controller = new YutGameController();

        if (choice == 1) {
            controller.launchFX();
        } else {
            controller.launchSwing();
        }



    }
}
