package com.java.barcode;

import com.dynamsoft.dbr.*;
import com.dynamsoft.dbr.Point;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.awt.*;

import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.BoxLayout;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

public class App extends JPanel implements ActionListener {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME); // Load the native library
    }

    private JButton mStart;
    private JTextArea mTextArea;
    private CustomJLable mImage;
    private AtomicBoolean status = new AtomicBoolean(false);
    private volatile boolean isRunning = true;
    // a timer for acquiring the video stream
    private ScheduledExecutorService timer;
    // the OpenCV object that realizes the video capture
    private VideoCapture capture = new VideoCapture();
    // a flag to change the button behavior
    private boolean cameraActive = false;
    // the id of the camera to be used
    private static int cameraId = 0;
    // Dynamsoft Barcode Reader
    private BarcodeReader mBarcodeReader;
    private ScheduledExecutorService barcodeTimer;

    public App() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        mStart = new JButton("Start Camera");
        mStart.addActionListener(this);

        // button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(mStart);
        add(buttonPanel);

        // image panel
        JPanel imagePanel = new JPanel();
        imagePanel.setSize(640, 500);
        mImage = new CustomJLable();
        mImage.setSize(680, 480);
        imagePanel.add(mImage);
        add(imagePanel);

        // text panel
        JPanel textPanel = new JPanel();
        textPanel.setSize(640, 200);
        mTextArea = new JTextArea();
        mTextArea.setSize(640, 200);
        mTextArea.setText("");
        textPanel.add(mTextArea);
        add(textPanel);

        try {
            BarcodeReader.initLicense("DLS2eyJoYW5kc2hha2VDb2RlIjoiMjAwMDAxLTE2NDk4Mjk3OTI2MzUiLCJvcmdhbml6YXRpb25JRCI6IjIwMDAwMSIsInNlc3Npb25QYXNzd29yZCI6IndTcGR6Vm05WDJrcEQ5YUoifQ==");
            mBarcodeReader = new BarcodeReader();
            // Best coverage settings
            mBarcodeReader.initRuntimeSettingsWithString(
                    "{\"ImageParameter\":{\"Name\":\"BestCoverage\",\"DeblurLevel\":9,\"ExpectedBarcodesCount\":512,\"ScaleDownThreshold\":100000,\"LocalizationModes\":[{\"Mode\":\"LM_CONNECTED_BLOCKS\"},{\"Mode\":\"LM_SCAN_DIRECTLY\"},{\"Mode\":\"LM_STATISTICS\"},{\"Mode\":\"LM_LINES\"},{\"Mode\":\"LM_STATISTICS_MARKS\"}],\"GrayscaleTransformationModes\":[{\"Mode\":\"GTM_ORIGINAL\"},{\"Mode\":\"GTM_INVERTED\"}]}}",
                    EnumConflictMode.CM_OVERWRITE);
            // Best speed settings
            // mBarcodeReader.initRuntimeSettingsWithString("{\"ImageParameter\":{\"Name\":\"BestSpeed\",\"DeblurLevel\":3,\"ExpectedBarcodesCount\":512,\"LocalizationModes\":[{\"Mode\":\"LM_SCAN_DIRECTLY\"}],\"TextFilterModes\":[{\"MinImageDimension\":262144,\"Mode\":\"TFM_GENERAL_CONTOUR\"}]}}",EnumConflictMode.CM_OVERWRITE);
            // Balance settings
            // mBarcodeReader.initRuntimeSettingsWithString(
            // "{\"ImageParameter\":{\"Name\":\"Balance\",\"DeblurLevel\":5,\"ExpectedBarcodesCount\":512,\"LocalizationModes\":[{\"Mode\":\"LM_CONNECTED_BLOCKS\"},{\"Mode\":\"LM_STATISTICS\"}]}}",
            // EnumConflictMode.CM_OVERWRITE);

            PublicRuntimeSettings runtimeSettings = mBarcodeReader.getRuntimeSettings();
            runtimeSettings.barcodeFormatIds = EnumBarcodeFormat.BF_NULL;
            // runtimeSettings.barcodeFormatIds_2 = EnumBarcodeFormat_2.BF2_POSTALCODE |
            // EnumBarcodeFormat_2.BF2_DOTCODE;
            runtimeSettings.barcodeFormatIds_2 = EnumBarcodeFormat_2.BF2_DOTCODE;
            mBarcodeReader.updateRuntimeSettings(runtimeSettings);
        } catch (Exception e) {
            System.out.println(e);
            return;
        }

        startCamera();
    }

    private Mat grabFrame() {
        // init everything
        Mat frame = new Mat();

        // check if the capture is open
        if (this.capture.isOpened()) {
            try {
                // read the current frame
                this.capture.read(frame);
            } catch (Exception e) {
                // log the error
                System.err.println("Exception during the image elaboration: " + e);
            }
        }

        return frame;
    }

    public void updateViewer(final BufferedImage image) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    mImage.setIcon(new ImageIcon(image));
                }
            });
            return;
        }
    }

    private void updateResults(final TextResult[] results, final BufferedImage image) {
        if (!SwingUtilities.isEventDispatchThread()) {
            isRunning = false;
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    // toggle camera status
                    startCamera();

                    if (results != null && results.length > 0) {
                        mImage.setIcon(new ImageIcon(image));
                        for (TextResult result : results) {
                            if (result.barcodeFormat != 0) {
                                mTextArea.append("Format: " + result.barcodeFormatString + "\n");
                            } else {
                                mTextArea.append("Format: " + result.barcodeFormatString_2 + "\n");
                            }
                            mTextArea.append("Text: " + result.barcodeText + "\n");
                            Point[] points = result.localizationResult.resultPoints;
                            mImage.appendPoints(points);
                        }

                    } else {
                        mTextArea.append("No barcode found!\n");
                    }
                    status.set(false);
                    updateViewer(image);
                }
            });
            return;
        }
    }

    private void startCamera() {
        mImage.clearPoints();
        if (!this.cameraActive) {
            mTextArea.setText("");
            status.set(false);
            isRunning = true;
            // start the video capture
            this.capture.open(cameraId + 700); // CAP_DSHOW
                                               // https://docs.opencv.org/3.4/d4/d15/group__videoio__flags__base.html

            // is the video stream available?
            if (this.capture.isOpened()) {
                this.cameraActive = true;

                // grab a frame every 33 ms (30 frames/sec)
                Runnable frameGrabber = new Runnable() {

                    @Override
                    public void run() {
                        // effectively grab and process a single frame
                        Mat frame = grabFrame();
                        byte[] data = Utils.matToByteArray(frame);

                        if (!status.get()) {
                            status.set(true);
                            barcodeTimer.schedule(new BarcodeRunnable(frame, mBarcodeReader, callback, status), 0, TimeUnit.MILLISECONDS);
                        }
                    
                        BufferedImage bufferedImage = Utils.byteToBufferedImage(data, frame.width(), frame.height(), frame.channels());
                        if (isRunning) updateViewer(bufferedImage);
                    }
                };

                this.timer = Executors.newSingleThreadScheduledExecutor();
                this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);

                barcodeTimer = Executors.newSingleThreadScheduledExecutor();

                // update the button content
                this.mStart.setText("Stop Camera");
            } else {
                // log the error
                System.err.println("Impossible to open the camera connection...");
            }
        } else {
            isRunning = false;
            // the camera is not active at this point
            this.cameraActive = false;
            // update again the button content
            this.mStart.setText("Start Camera");

            // stop the timer
            this.stopAcquisition();
        }
    }

    private BarcodeCallback callback = new BarcodeCallback(){
    
        @Override
        public void onResult(TextResult[] results, BufferedImage image) {
            // TODO Auto-generated method stub
            updateResults(results, image);
        }
    };

    private void stopAcquisition() {

        if (this.timer != null && !this.timer.isShutdown()) {
            try {
                // stop the timer
                this.timer.shutdown();
                this.timer.awaitTermination(33, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                // log any exception
                System.err.println("Exception in stopping the frame capture, trying to release the camera now... " + e);
            }
        }

        if (this.capture.isOpened()) {
            // release the camera
            this.capture.release();
        }

        if (barcodeTimer != null) {
            try {
                // stop the timer
                barcodeTimer.shutdown();
                barcodeTimer.awaitTermination(0, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                // log any exception
                System.err.println("Exception in stopping the frame capture, trying to release the camera now... " + e);
            }
        }
    }

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        // System.load("D:/opencv-4.3/opencv/build/java/x64/opencv_java430.dll");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        startCamera();
    }

    private static void createAndShowGUI() {
        // Create and set up the window.
        JFrame frame = new JFrame("DotCode Reader");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Add content to the window.
        Container pane = frame.getContentPane();
        pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
        frame.add(new App());

        // Display the window.
        frame.pack();
        frame.setVisible(true);
        frame.setResizable(false);
        frame.setSize(700, 700);

        double width = Toolkit.getDefaultToolkit().getScreenSize().getWidth();
        double height = Toolkit.getDefaultToolkit().getScreenSize().getHeight();
        int frameWidth = frame.getWidth();
        int frameHeight = frame.getHeight();
        frame.setLocation((int) (width - frameWidth) / 2, (int) (height - frameHeight) / 2);
    }

    public static void main(String[] args) {
        System.out.println("OpenCV version: " + Core.VERSION);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                UIManager.put("swing.boldMetal", Boolean.FALSE);
                createAndShowGUI();
            }
        });
    }
}
