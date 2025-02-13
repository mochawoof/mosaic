import javax.swing.*;
import java.awt.*;

import java.util.Random;

class Confetti {
    public boolean done;
    public int n;
    public long seed;
    public int time;
    public double speed;
    public int maxy;
    
    public int length = 12;
    public int thickness = 6;
    
    public long lastframe = 0;
    public double frametime = 1000.0 / 60;

    public Confetti(int n, double speed, int maxy) {
        done = false;
        this.n = n;
        this.speed = speed;
        this.maxy = maxy;
        time = 0;
        seed = new Random().nextLong();
    }
    
    private double clamp(double n, double min, double max) {
        if (n < min) {
            return min;
        }
        if (n > max) {
            return max;
        }
        return n;
    }
    
    private double clamp(double n, double min) {
        return clamp(n, min, Double.MAX_VALUE);
    }
    
    public void draw(JComponent c, Graphics g) {
        if (time * speed * time * 0.05 < maxy) {
            Random r = new Random(seed);
            for (int i = 0; i < n; i++) {
                int x = (int) (r.nextDouble() * c.getWidth());
                int y = (int) ((r.nextDouble() * c.getHeight()) + (time * speed));
                y *= time * 0.05;
                
                Color color = Color.getHSBColor(r.nextFloat(), 1.0f, 1.0f);
                double angle = r.nextDouble() * 360;
                g.setColor(color);
                
                int x1 = (int) ((Math.sin(angle) * length) + x);
                int y1 = (int) ((Math.cos(angle) * length) + y);
                
                for (int j = 0; j < thickness; j++) {
                    g.drawLine(x, y + j, x1, y1 + j);
                }
            }
            c.repaint();
            if (lastframe + frametime < System.currentTimeMillis()) {
                time++;
                lastframe = System.currentTimeMillis();
            }
        } else {
            done = true;
            System.out.println("Confetti done");
        }
    }
}