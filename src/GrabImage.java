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
    private static Point avgPoint;
    private static int savedXYScanPadding = 0;


    public static void main(String[] args) {
        GrabImage grabImage = new GrabImage();
        grabImage.mainIm();
    }

    private int toGrayScale(Color color) {
        return (color.getRed() + color.getBlue() + color.getGreen()) / 3;
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
        int frameCounter = 0;
        boolean firstTime = true;
        int prevTotalPixels = 0;
        int xyScanPadding = 50;

        BufferedImage lastImage = new BufferedImage(600, 600, BufferedImage.TYPE_INT_ARGB);
        boolean dryRun = false;
        while(true) {
            BufferedImage image = webcam.getImage();
            if(firstTime) {
                avgPoint = new Point(image.getWidth() / 2, image.getHeight() / 2);
                firstTime = false;
            }
            BufferedImage beforeModImage = new BufferedImage(600, 600, BufferedImage.TYPE_INT_ARGB);
            beforeModImage.getGraphics().drawImage(image, 0, 0, null);
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
                    skinColor = new Color(image.getRGB(avgPoint.x, avgPoint.y));
//                    skinColor = new Color(totalRed / 255.0f, totalGreen / 255.0f, totalBlue / 255.0f);
                    eval = false;
                    System.out.println("Beginning to Track");
                    track = true;
                }
                if (track) {
                    frameCounter++;
                    long x = 0, y = 0;
                    int totalPixels = 0;
                    int bluePadding = 5;
                    int redPadding = 10;
                    int greenPadding = 5;
                    totalGreen = 0;
                    totalRed = 0;
                    totalBlue = 0;
                    for (int row = 0; row < image.getHeight(); row++) {
                        for (int col = 0; col < image.getWidth(); col++) {
                            //Get the color of the current pixel
                            Color color = new Color(image.getRGB(col, row));
                            //Within Color Ranges?
                            if (isWithin(col, avgPoint.x, xyScanPadding) && isWithin(row, avgPoint.y, xyScanPadding) && isWithin(color.getRed(), skinColor.getRed(), redPadding) && isWithin(color.getGreen(), skinColor.getGreen(), greenPadding) && isWithin(color.getBlue(), skinColor.getBlue(), bluePadding) && isChanged(image, lastImage, col, row)) {
                                    x += col;
                                    y += row;
                                    totalPixels++;
                                    //Add up the totals of nearby similars;
                                    image.setRGB(col, row, 0);

                                    totalRed += color.getRed();
                                    totalGreen += color.getGreen();
                                    totalBlue += color.getBlue();
                            }
//                            else {
////                                image.setRGB(col, row, 0);
//                            }
                        }
                    }
                    if(totalPixels != 0) {
                        dryRun = false;
                        System.out.println("XY Scan Padding " + xyScanPadding);
                        int newX = (int) x / totalPixels;
                        int newY = (int) y / totalPixels;
                        avgPoint = new Point(newX, newY);
//                        skinColor = new Color(totalRed / totalPixels / 255.0f, totalGreen / totalPixels / 255.0f, totalBlue / totalPixels / 255.0f);
//                        if(totalPixels > prevTotalPixels) {
//                            xyScanPadding-=10;
//                            if(xyScanPadding < 10) {
//                                xyScanPadding = 10;
//                            }
//                        } else if(totalPixels < prevTotalPixels) {
//                            xyScanPadding+=10;
//                            if(xyScanPadding > 50) {
//                                xyScanPadding = 50;
//                            }
//                        }
                        prevTotalPixels = totalPixels;
                    } else {
                        dryRun = true;
                    }
                    image.getGraphics().fillRect(avgPoint.x, avgPoint.y, 2, 2);
                    image.getGraphics().drawRect(avgPoint.x - xyScanPadding, avgPoint.y - xyScanPadding, xyScanPadding * 2, xyScanPadding * 2);
                }
                panel.getGraphics().drawImage(image.getScaledInstance(600, 600, Image.SCALE_DEFAULT), 0, 0, null);
                lastImage = image;
                //EVAL BREAKS THE NEW MOVEMENT METHOD.
                if(frameCounter != 0 && frameCounter % 5 == 0 && dryRun) {
                    frameCounter = 0;
                    eval = true;
                }
                lastImage = beforeModImage;
            }
            firstTime = false;
        }

    }

    private boolean isWithin(int targetColor, int baseColor, int padding) {
        return targetColor > baseColor - padding && targetColor < baseColor + padding;
    }

    private int map(int val, int valMin, int valMax, int targetMin, int targetMax) {
        int diff = targetMax - targetMin;
        int valDiff = valMax - valMin;
        float percentage = (float)(val - valMin) / valDiff;
        float targetVal = diff * percentage;
        return (int) (targetMin + targetVal);

    }

    private boolean isChanged(BufferedImage image, BufferedImage lastImage, int col, int row) {
        Color color = new Color(lastImage.getRGB(col, row));
        int greyScale = (color.getRed() + color.getBlue() + color.getGreen()) / 3;
        Color newColor = new Color(image.getRGB(col, row));
        int newGreyScale = (newColor.getRed() + newColor.getBlue() + newColor.getGreen()) / 3;
        if (isWithin(greyScale, newGreyScale, 30)) {
            return false;
        }
        return true;
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
