package org.tea;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class Window extends JFrame {

    private final JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    private final JTextField textFieldForHost = new JTextField(10);
    private final JButton buttonOn = new JButton("включить");
    private final JTextArea logArea = new JTextArea(5, 6);
    private final JScrollPane logPanel = new JScrollPane(logArea);
    private boolean pingIsWork = false;
    private static Window instance;
    private JLabel labelWithImage = new JLabel();
    private ImageIcon imageIcon;

    public static Window getInstance() {
        if (instance == null) {
            instance = new Window();
        }
        return instance;
    }

    public boolean isPingWork() {
        return pingIsWork;
    }

    public void addLog(String message) {
        logArea.append(message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    public void drawImage(BufferedImage image) {
        Image scaledimage = image.getScaledInstance(
                400,
                200,
                Image.SCALE_SMOOTH
        );
        if (imageIcon == null) {
            imageIcon = new ImageIcon(scaledimage);
            labelWithImage.setIcon(imageIcon);
            getContentPane().add(labelWithImage, BorderLayout.SOUTH);
        } else {
            imageIcon.setImage(scaledimage);
        }
        Window.getInstance().pack();
        labelWithImage.repaint();
    }

    private Window() throws HeadlessException {
        super("Ping");
        setSize(420, 355);
        textFieldForHost.setToolTipText("Введи хост");
        textFieldForHost.setHorizontalAlignment(SwingConstants.CENTER);
        centerPanel.add(textFieldForHost);
        centerPanel.add(buttonOn);

        buttonOn.addActionListener(e -> {
            if (pingIsWork) {
                Ping.stop();
                textFieldForHost.setEditable(true);
                textFieldForHost.setFocusable(true);
                buttonOn.setBackground(new Color(255,0,0));
                buttonOn.setText("включить");

            } else {
                textFieldForHost.setEditable(false);
                textFieldForHost.setFocusable(false);
                Ping.start(textFieldForHost.getText());
                buttonOn.setBackground(new Color(0,255,0));
                buttonOn.setText("выключить");
            }
            pingIsWork = !pingIsWork;
        });
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospace",Font.PLAIN,12));
        getContentPane().add(logPanel, BorderLayout.NORTH);
        getContentPane().add(centerPanel, BorderLayout.CENTER);

    }

}
