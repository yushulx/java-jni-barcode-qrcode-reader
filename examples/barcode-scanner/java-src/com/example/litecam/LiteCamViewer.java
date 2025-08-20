package com.example.litecam;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

public class LiteCamViewer extends JPanel {
    private final LiteCam cam = new LiteCam();
    private BufferedImage img;
    private ByteBuffer buffer;

    public LiteCamViewer(int index) {
        String[] devs = LiteCam.listDevices();
        System.out.println("Devices:");
        for (int i=0;i<devs.length;i++) System.out.println(i+": "+devs[i]);
        cam.openDevice(index);
        int w = cam.getWidth();
        int h = cam.getHeight();
        img = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
        buffer = ByteBuffer.allocateDirect(w * h * 3);
        new Timer(33, e -> updateFrame()).start(); // ~30fps
    }

    private void updateFrame() {
        if (!cam.isOpen()) return;
        if (cam.grabFrame(buffer)) {
            int w = cam.getWidth();
            int h = cam.getHeight();
            byte[] data = ((java.awt.image.DataBufferByte)img.getRaster().getDataBuffer()).getData();
            buffer.rewind();
            int len = Math.min(data.length, buffer.remaining());
            buffer.get(data, 0, len);
            // NOTE: Native Windows implementation already outputs BGR order.
            // If running on a platform that outputs RGB, you would need to swap here.
            buffer.rewind();
            repaint();
        }
    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (img != null) g.drawImage(img, 0, 0, getWidth(), getHeight(), null);
    }

    @Override public Dimension getPreferredSize() { return img==null?new Dimension(640,480):new Dimension(img.getWidth(), img.getHeight()); }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("LiteCam Viewer");
            LiteCamViewer v = new LiteCamViewer(0);
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setContentPane(v);
            f.pack();
            f.setVisible(true);
        });
    }
}
