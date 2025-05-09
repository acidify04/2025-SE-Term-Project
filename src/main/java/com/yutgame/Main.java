package main.java.com.yutgame;



import main.java.com.yutgame.view.swing.SwingYutGameView;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SwingYutGameView view = new SwingYutGameView();
            view.setVisible(true);
        });
    }
}