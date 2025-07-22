package org.tea;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        Window frame = Window.getInstance();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (frame.isPingWork()) Ping.stop();
        }));
    }
}
