package com.java.barcode;

import com.dynamsoft.core.EnumErrorCode;
import com.dynamsoft.cvr.CaptureVisionRouter;
import com.dynamsoft.cvr.CapturedResult;
import com.dynamsoft.cvr.EnumPresetTemplate;
import com.dynamsoft.dbr.BarcodeResultItem;
import com.dynamsoft.dbr.DecodedBarcodesResult;
import com.dynamsoft.license.LicenseError;
import com.dynamsoft.license.LicenseException;
import com.dynamsoft.license.LicenseManager;

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

    public BarcodeResultItem[] decodeFileDynamsoft(String filename) {
        // Dynamsoft
        // Get a 30-day free trial license from https://www.dynamsoft.com/customer/license/trialLicense/?product=dcv&package=cross-platform
        String licenseKey = System.getenv().getOrDefault("DBR_LICENSE_KEY", "LICENSE-KEY");
        try {
            LicenseError licenseError = LicenseManager.initLicense(licenseKey);
            if (licenseError.getErrorCode() != EnumErrorCode.EC_OK) {
                System.out.println("License initialization failed: " + licenseError.getErrorString());
                return null;
            }
        } catch (LicenseException e) {
            System.out.println("License initialization failed: " + e.getErrorString());
            return null;
        }
        
        CaptureVisionRouter cvRouter = new CaptureVisionRouter();
        CapturedResult result = cvRouter.capture(filename, EnumPresetTemplate.PT_READ_BARCODES);

        if (result.getErrorCode() != EnumErrorCode.EC_OK) {
            System.out.println("Error: " + result.getErrorCode() + ", " + result.getErrorString());
            return null;
        }

        DecodedBarcodesResult barcodeResult = result.getDecodedBarcodesResult();
        BarcodeResultItem[] items = barcodeResult != null ? barcodeResult.getItems() : null;

        if (items != null && items.length > 0) {
            System.out.println("Dynamsoft result count: " + items.length);
            for (BarcodeResultItem item : items) {
                System.out.println("Format: " + item.getFormatString());
                System.out.println("Text: " + item.getText());
                System.out.println();
            }
        }

        return items;
    }

    @Override
	public void actionPerformed(ActionEvent e) {

        int returnVal = mFileChooser.showOpenDialog(App.this);
 
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = mFileChooser.getSelectedFile();     
            String filename = file.toPath().toString();          
            if (mSourceList.getSelectedItem().toString().equals("Dynamsoft")) {
                BarcodeResultItem[] items = decodeFileDynamsoft(filename);
                if (items != null && items.length > 0) {
                    mTextArea.append("Dynamsoft result count: " + items.length + "\n");
                    for (BarcodeResultItem item : items) {
                        mTextArea.append("Format: " + item.getFormatString() + "\n");
                        mTextArea.append("Text: " + item.getText() + "\n");
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

    public void decodeAndDisplay(String filename) {
        mTextArea.append("File: " + filename + "\n");
        mTextArea.append(SEPARATOR);

        Result[] zxingResults = decodefileZXing(filename);
        if (zxingResults != null && zxingResults.length > 0) {
            mTextArea.append("ZXing result count: " + zxingResults.length + "\n");
            for (Result zxingResult : zxingResults) {
                mTextArea.append("Format: " + zxingResult.getBarcodeFormat() + "\n");
                mTextArea.append("Text: " + zxingResult.getText() + "\n");
            }
        }
        else {
            mTextArea.append("ZXing found no barcode!\n");
        }
        mTextArea.append(SEPARATOR);

        BarcodeResultItem[] items = decodeFileDynamsoft(filename);
        if (items != null && items.length > 0) {
            mTextArea.append("Dynamsoft result count: " + items.length + "\n");
            for (BarcodeResultItem item : items) {
                mTextArea.append("Format: " + item.getFormatString() + "\n");
                mTextArea.append("Text: " + item.getText() + "\n");
            }
        }
        else {
            mTextArea.append("Dynamsoft found no barcode!\n");
        }
        mTextArea.append(SEPARATOR);
    }

    private static void createAndShowGUI(String filename) {
        //Create and set up the window.
        JFrame frame = new JFrame("Barcode Reader");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
        //Add content to the window.
        final App app = new App();
        frame.add(app);
 
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

        //Optionally decode an image passed from the command line with both engines
        if (filename != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    app.decodeAndDisplay(filename);
                }
            }).start();
        }
    }

    public static void main( String[] args )
    {
        final String filename = args.length > 0 ? args[0] : null;

        SwingUtilities.invokeLater(new Runnable() {
            @Override
			public void run() {
                //Turn off metal's use of bold fonts
                UIManager.put("swing.boldMetal", Boolean.FALSE); 
                createAndShowGUI(filename);
            }
        });
    }
}
