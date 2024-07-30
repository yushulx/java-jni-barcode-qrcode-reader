package com.java.barcode;

import javax.swing.JLabel;

import com.dynamsoft.dbr.*;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.awt.Color;

class CustomJLable extends JLabel {
    private ArrayList<Point[]> data = new ArrayList<>();

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        if (data.size() > 0) {
            g2d.setColor(Color.RED);
            for (Point[] points : data) {
                for (int i = 0; i < points.length; ++i) {
                    if (i == 3) {
                        g2d.drawLine(points[i].x, points[i].y, points[0].x, points[0].y);
                    } else {
                        g2d.drawLine(points[i].x, points[i].y, points[i + 1].x, points[i + 1].y);
                    }
                }
            }

        }
        g2d.dispose();
    }

    public void appendPoints(Point[] points) {
        data.add(points);
    }

    public void clearPoints() {
        data.clear();
    }
}