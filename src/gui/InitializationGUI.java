package gui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import javax.swing.JFrame;
import javax.swing.JLabel;
import krist.miner.Utils;

public class InitializationGUI extends JFrame {
    public static final int WINDOW_WIDTH = 200;
    public static final int WINDOW_HEIGHT = 100;
    
    private JLabel loadingLabel = null;
    
    public InitializationGUI() {
        super("Grim's Krist Miner");
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        
        setLayout(new FlowLayout(FlowLayout.CENTER, 30, 10));
        
        loadingLabel = new JLabel("Loading data...");
        
        add(loadingLabel);
    }
    
    public void setLoadingText(String text) {
        loadingLabel.setText(text == null ? "Loading..." : text);
    }
    
    public static void main(String[] args) {
        InitializationGUI loadingScreen = new InitializationGUI();
        loadingScreen.setLoadingText("Reading config file...");
        
        // Start the window in the center of the screen.
        Dimension screenDimensions = Toolkit.getDefaultToolkit()
                .getScreenSize();
        loadingScreen.setLocation(
                screenDimensions.width / 2 - loadingScreen.getSize().width / 2,
                screenDimensions.height / 2 - loadingScreen.getSize().height
                        / 2);
        loadingScreen.setVisible(true);
        
        ManagerGUI.setCoreLimit(Utils.getConfiguredCoreLimit());
        loadingScreen.dispose();
        
        // Create the actual program GUI.
        final ManagerGUI gui = new ManagerGUI();
        
        gui.setLocation(screenDimensions.width / 2 - gui.getSize().width / 2,
                screenDimensions.height / 2 - gui.getSize().height / 2);
        gui.setVisible(true);
    }
}
