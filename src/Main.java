import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Lunatic");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setUndecorated(true);

            GraphicsDevice device = GraphicsEnvironment
                    .getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice();
            device.setFullScreenWindow(frame);

            GamePanel game = new GamePanel();
            frame.add(game);
            frame.setVisible(true);
            game.requestFocusInWindow();
        });
    }
}