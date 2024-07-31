package com.java.barcode;

import com.dynamsoft.dbr.*;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.*;

import java.io.File;
import java.io.IOException;
import java.awt.image.*;
import javax.imageio.ImageIO;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JComboBox;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class App extends JPanel implements ActionListener {
    public final static String SEPARATOR = "---------------------------------------------------------------------------\n";
    private JButton mLoad;
    private JFileChooser mFileChooser;
    private JTextArea mTextArea;
	private JComboBox mSourceList;
    
    public App() {
        super(new BorderLayout());
		
        //Create a file chooser
        mFileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                ".png", "png");
        mFileChooser.setFileFilter(filter);
        mLoad = new JButton("Load File");
        mLoad.addActionListener(this);
        
		// get sources
		mSourceList = new JComboBox(new String[]{"ZXing", "Dynamsoft"});
        mSourceList.setSelectedIndex(0);
		
        // button panel
        JPanel buttonPanel = new JPanel(); 
		buttonPanel.add(mSourceList);
		buttonPanel.add(mLoad);
        add(buttonPanel, BorderLayout.PAGE_START);
        
        // result panel
        mTextArea = new JTextArea();
        mTextArea.setSize(480, 480);
        JScrollPane sp = new JScrollPane(mTextArea); 
		add(sp, BorderLayout.CENTER);
    }

    public Result[] decodefileZXing(String filename) {
        // Read an image to BufferedImage
        Result[] zxingResults = null;
        BufferedImage image = null;
        try {
            image = ImageIO.read(new File(filename));
        } catch (IOException e) {
            System.out.println(e);
            return null;
        }

        // ZXing
        BinaryBitmap bitmap = null;
        int[] pixels = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
        RGBLuminanceSource source = new RGBLuminanceSource(image.getWidth(), image.getHeight(), pixels);
        bitmap = new BinaryBitmap(new HybridBinarizer(source));
            
        MultiFormatReader reader = new MultiFormatReader();  
        GenericMultipleBarcodeReader multiReader = new GenericMultipleBarcodeReader(reader);
        try {
            zxingResults = multiReader.decodeMultiple(bitmap);
            System.out.println("ZXing result count: " + zxingResults.length);
            if (zxingResults != null) {
                for (Result zxingResult : zxingResults) {
                    System.out.println("Format: " + zxingResult.getBarcodeFormat());
                    System.out.println("Text: " + zxingResult.getText());
                    System.out.println();
                }
            }
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        pixels = null;
        bitmap = null;

       
        if (image != null) {
            image.flush();
            image = null;
        }

        return zxingResults;
    }

    public TextResult[] decodefileDynamsoft(String filename) {
        // Dynamsoft
        BarcodeReader br = null;
        try {
            // Get a license key from https://www.dynamsoft.com/customer/license/trialLicense?product=dbr
            BarcodeReader.initLicense("DLS2eyJoYW5kc2hha2VDb2RlIjoiMjAwMDAxLTE2NDk4Mjk3OTI2MzUiLCJvcmdhbml6YXRpb25JRCI6IjIwMDAwMSIsInNlc3Npb25QYXNzd29yZCI6IndTcGR6Vm05WDJrcEQ5YUoifQ==");
            br = new BarcodeReader();
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
        
        TextResult[] results = null;
        try {
            results = br.decodeFile(filename, "");
        } catch (Exception e) {
            System.out.println("decode buffered image: " + e);
        }

        if (results != null && results.length > 0) {
            System.out.println("DBR result count: " + "DBR result count: " + results.length);
            String pszTemp = null;
            for (TextResult result : results) {
                if (result.barcodeFormat != 0) {
                    pszTemp = "Format: " + result.barcodeFormatString;
                } else {
                    pszTemp = "Format: " + result.barcodeFormatString_2;
                }
                System.out.println(pszTemp);
                System.out.println("Text: " + result.barcodeText);
                System.out.println();
            }
        }

        return results;
    }

    @Override
	public void actionPerformed(ActionEvent e) {

        int returnVal = mFileChooser.showOpenDialog(App.this);
 
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = mFileChooser.getSelectedFile();     
            String filename = file.toPath().toString();          
            if (mSourceList.getSelectedItem().toString().equals("Dynamsoft")) {
                TextResult[] results = decodefileDynamsoft(filename);
                if (results != null && results.length > 0) {
                    mTextArea.append("Dynamsoft result count: " + results.length + "\n");
                    for (TextResult result : results) {
                        if (result.barcodeFormat != 0) {
                            mTextArea.append("Format: " + result.barcodeFormatString + "\n");
                        } else {
                            mTextArea.append("Format: " + result.barcodeFormatString_2 + "\n");
                        }
                        mTextArea.append("Text: " + result.barcodeText + "\n");
                    }
                    mTextArea.append(SEPARATOR);
                }
                else {
                    mTextArea.append("No barcode found!\n");
                    mTextArea.append(SEPARATOR);
                }
            }
            else {
                Result[] results = decodefileZXing(filename);
                if (results != null) {
                    mTextArea.append("ZXing result count: " + results.length + "\n");
                    for (Result zxingResult : results) {
                        mTextArea.append("Format: " + zxingResult.getBarcodeFormat() + "\n");
                        mTextArea.append("Text: " + zxingResult.getText() + "\n");
                    }
                    mTextArea.append(SEPARATOR);
                }
                else {
                    mTextArea.append("No barcode found!\n");
                    mTextArea.append(SEPARATOR);
                }
            }
        } 
    }

    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("Barcode Reader");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
        //Add content to the window.
        frame.add(new App());
 
        //Display the window.
        frame.pack();
        frame.setVisible(true);
        frame.setResizable(false);
        frame.setSize(480, 700);
        
        double width = Toolkit.getDefaultToolkit().getScreenSize().getWidth();
        double height = Toolkit.getDefaultToolkit().getScreenSize().getHeight();
        int frameWidth = frame.getWidth();
        int frameHeight = frame.getHeight();
        frame.setLocation((int)(width - frameWidth) / 2, (int)(height - frameHeight) / 2);
    }

    public static void main( String[] args )
    {

        SwingUtilities.invokeLater(new Runnable() {
            @Override
			public void run() {
                //Turn off metal's use of bold fonts
                UIManager.put("swing.boldMetal", Boolean.FALSE); 
                createAndShowGUI();
            }
        });
    }
}
