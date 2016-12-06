import com.github.sarxos.webcam.Webcam;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;

/**
 * Created by john_bachman on 12/6/16.
 */
public class GrabImage implements KeyListener {

    private static boolean eval = false;
    private static boolean track = false;
    private static BufferedImage lastImage = new BufferedImage(600, 600, BufferedImage.TYPE_INT_ARGB);

    public static void main(String[] args) {
        GrabImage grabImage = new GrabImage();
        grabImage.mainIm();
    }

    public void mainIm() {
        JFrame jFrame = new JFrame("Fun stuff");
        jFrame.setSize(600, 600);
        jFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        JPanel panel = new JPanel();
        jFrame.addKeyListener(this);
        jFrame.add(panel);
        jFrame.setVisible(true);

        Webcam webcam = Webcam.getDefault();
        webcam.open();
        long totalRed = 0;
        long totalBlue = 0;
        long totalGreen = 0;
        Color skinColor = new Color(0,0,0);
        while(true) {
            BufferedImage image = webcam.getImage();
            if(image != null) {
                if (eval) {
//                    System.out.println("Evaluating Skin Tone");
//                    for (int row = 0; row < image.getHeight(); row++) {
//                        for (int col = 0; col < image.getWidth(); col++) {
//                            Color color = new Color(image.getRGB(col, row));
//                            totalRed += color.getRed();
//                            totalBlue += color.getBlue();
//                            totalGreen += color.getGreen();
//                        }
//                    }
//                    totalRed = totalRed / (image.getWidth() * image.getHeight());
//                    totalBlue = totalBlue / (image.getWidth() * image.getHeight());
//                    totalGreen = totalGreen / (image.getWidth() * image.getHeight());
//                    System.out.println(totalRed + " " + totalGreen + " " + totalBlue);
                     skinColor = new Color(image.getRGB(image.getWidth() / 2, image.getHeight() / 2));
//                    skinColor = new Color(totalRed / 255.0f, totalGreen / 255.0f, totalBlue / 255.0f);
                    eval = false;
                    System.out.println("Beginning to Track");
                    track = true;
                    panel.setBackground(skinColor);
                }
                if (track) {
                    long x = 0, y = 0;
                    int totalPixels = 0;
                    int bluePadding = 5;
                    int Redpadding = 10;
                    int greenPadding = 5;
                    Point smallestXY = new Point(600,600);
                    Point largestXY = new Point(0,0);
                    for (int row = 0; row < image.getHeight(); row++) {
                        for (int col = 0; col < image.getWidth(); col++) {
                            Color color = new Color(image.getRGB(col, row));
                            if (color.getRed() > skinColor.getRed() - Redpadding && color.getRed() < skinColor.getRed() + Redpadding) {
                                if (color.getGreen() > skinColor.getGreen() - greenPadding && color.getGreen() < skinColor.getGreen() + greenPadding) {
                                    if (color.getBlue() > skinColor.getBlue() - bluePadding && color.getBlue() < skinColor.getBlue() + bluePadding) {
                                        x += col;
                                        y += row;
                                        totalPixels++;
                                        if(col < smallestXY.x && row < smallestXY.y) {
                                            smallestXY = new Point(col, row);
                                        } else if(col > largestXY.x && row > largestXY.y) {
                                            largestXY = new Point(col, row);
                                        }
                                    } else {
                                        image.setRGB(col, row, 0);
                                    }
                                }
                                else {
                                    image.setRGB(col, row, 0);
                                }
                            }
                            else {
                                image.setRGB(col, row, 0);
                            }
                        }
                    }
                    if(totalPixels != 0) {
                        panel.getGraphics().setColor(new Color(1f,1f,1f));
                        System.out.println("Drawing at " + (x/totalPixels) + " " + (y /totalPixels));
                        image.getGraphics().setColor(Color.RED);
                        image.getGraphics().fillRect((int) ((x / totalPixels)), (int) ((y / totalPixels)), 2, 2);
//                        image.getGraphics().drawRect(smallestXY.x, smallestXY.y, largestXY.x - smallestXY.x, largestXY.y - smallestXY.y);
                    }
                }
                panel.getGraphics().drawImage(image.getScaledInstance(600, 600, Image.SCALE_DEFAULT), 0, 0, null);
                lastImage = image;
            }
        }

    }


    public void keyTyped(KeyEvent e) {

    }

    public void keyPressed(KeyEvent e) {
        System.out.println("Eval key pressed");
        eval = true;
    }

    public void keyReleased(KeyEvent e) {

    }
}
