import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

// Custom JPanel that creates a scrollable image
public class ScrollPanel extends JPanel {
    public ScrollPanel(BufferedImage image, int num) {
        JPanel canvas = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(image, 0, 0, null);

                int parts = image.getHeight() / num;

                for (int i = parts; i < image.getHeight() - (parts/2); i += parts) {
                    g.setColor(Color.RED);
                    g.drawLine(0, i, image.getWidth(), i);
                }
            }
        };
        canvas.setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
        JScrollPane sp = new JScrollPane(canvas);
        setLayout(new BorderLayout());
        add(sp, BorderLayout.CENTER);
    }
}