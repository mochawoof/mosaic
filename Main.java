import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.*;
import java.awt.event.*;

import java.io.File;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

class Main {
    private static File file;
    private static BufferedImage image;
    private static int[] pieces;
    private static int npiecesx;
    
    private static Point mouseclickpoint;
    private static Point mouse;
    private static int moving;
    private static boolean showlines = false;
    
    private static JFrame f;
    
    public static void update() {
        int oldnpiecesx = npiecesx;
        try {
            npiecesx = (int) Math.sqrt(Integer.parseInt(Settings.get("Difficulty")));
        } catch (Exception e) {
            npiecesx = 4;
        }
        if (npiecesx <= 0 || npiecesx > 16) {
            npiecesx = 4;
        }
        if (file != null && oldnpiecesx != npiecesx) {reload();}
        
        try {
            if (Settings.get("Theme").equals("System")) {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } else {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            }
            SwingUtilities.updateComponentTreeUI(f);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        if (Settings.get("Show_Lines").toLowerCase().equals("yes")) {
            showlines = true;
        } else {
            showlines = false;
        }
    }
    
    private static void shuffle() {
        for (int i = 0; i < pieces.length; i++) {
            int rdm = (int) (Math.random() * pieces.length);
            int tmp = pieces[rdm];
            pieces[rdm] = pieces[i];
            pieces[i] = tmp;
        }
    }
    
    private static void reload() {
        load(file);
    }
    
    private static void load(File fl) {
        try {
            image = ImageIO.read(fl);
            Settings.set(".Last", fl.getAbsolutePath());
            file = fl;
            
            pieces = new int[npiecesx * npiecesx];
            for (int i = 0; i < pieces.length; i++) {
                pieces[i] = i;
            }
            shuffle();
            f.repaint();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        file = null;
        image = null;
        pieces = null;
        mouseclickpoint = null;
        mouse = null;
        moving = -1;
        
        f = new JFrame("Mosaic");
        f.setSize(300, 400);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setIconImage(Res.getAsImage("icon.png"));
        
        update();
        
        JMenuBar mb = new JMenuBar();
        f.setJMenuBar(mb);
        
        JMenu filem = new JMenu("File");
        filem.setMnemonic(KeyEvent.VK_F);
        mb.add(filem);
        
        JMenuItem reshuffle = new JMenuItem("Reshuffle");
        reshuffle.setMnemonic(KeyEvent.VK_R);
        reshuffle.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (file != null) {
                    reload();
                }
            }
        });
        filem.add(reshuffle);
        
        JMenuItem open = new JMenuItem("Open");
        open.setMnemonic(KeyEvent.VK_O);
        open.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser c = new JFileChooser();
                c.setFileFilter(new FileNameExtensionFilter("png, jpg, jpeg, bmp, wbmp", "png", "jpg", "jpeg", "bmp", "wbmp"));
                if (file != null) {
                    c.setCurrentDirectory(file);
                }
                if (c.showOpenDialog(f) == JFileChooser.APPROVE_OPTION) {
                    load(c.getSelectedFile());
                }
            }
        });
        filem.add(open);
        
        JMenu settings = Settings.generateJMenu("Settings");
        settings.setMnemonic(KeyEvent.VK_S);
        mb.add(settings);
        
        f.add(new JComponent() {
            {
                addMouseListener(new MouseListener() {
                    public void mouseEntered(MouseEvent e) {}
                    public void mouseExited(MouseEvent e) {}
                    public void mouseClicked(MouseEvent e) {}
                    
                    public void mousePressed(MouseEvent e) {
                        if (e.getButton() == MouseEvent.BUTTON1 && image != null) {
                            int w = getWidth() / npiecesx;
                            int h = getHeight() / npiecesx;
                            
                            int x = e.getX() / w;
                            int y = e.getY() / h;
                            
                            mouse = e.getPoint();
                            mouseclickpoint = mouse;
                            moving = (y * npiecesx) + x;
                            
                            f.repaint();
                        }
                    }
                    public void mouseReleased(MouseEvent e) {
                        if (image != null && moving != -1) {
                            int w = getWidth() / npiecesx;
                            int h = getHeight() / npiecesx;
                            
                            int x = e.getX() / w;
                            int y = e.getY() / h;
                            
                            int movedto = (y * npiecesx) + x;
                            if (movedto < pieces.length && movedto >= 0) {
                                int tmp = pieces[movedto];
                                pieces[movedto] = pieces[moving];
                                pieces[moving] = tmp;
                            }
                            
                            mouse = null;
                            mouseclickpoint = null;
                            moving = -1;
                            
                            f.repaint();
                        }
                    }
                });
                
                addMouseMotionListener(new MouseMotionListener() {
                    public void mouseDragged(MouseEvent e) {
                        if (moving != -1) {
                            mouse = e.getPoint();
                            f.repaint();
                        }
                    }
                    public void mouseMoved(MouseEvent e) {}
                });
            }
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                
                if (image != null) {
                    BufferedImage simage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
                    Graphics sg = simage.getGraphics();
                    sg.setColor(Color.WHITE);
                    sg.fillRect(0, 0, getWidth(), getHeight());
                    sg.drawImage(image.getScaledInstance(getWidth(), getHeight(), Image.SCALE_SMOOTH), 0, 0, null);
                    
                    BufferedImage out = new BufferedImage(simage.getWidth(), simage.getHeight(), BufferedImage.TYPE_INT_ARGB);
                    Graphics outg = out.getGraphics();
                    
                    int w = simage.getWidth() / npiecesx;
                    int h = simage.getHeight() / npiecesx;
                    
                    Image movsub = null;
                    
                    for (int i = 0; i < pieces.length; i++) {
                        int y = i / npiecesx;
                        int x = i - (y * npiecesx);
                        
                        int ysub = pieces[i] / npiecesx;
                        int xsub = pieces[i] - (ysub * npiecesx);
                        
                        Image sub = simage.getSubimage(xsub * w, ysub * h, w, h);
                        
                        int rx = x * w;
                        int ry = y * h;
                        
                        if (i == moving) {
                            movsub = sub;
                            
                            outg.setColor(Color.BLACK);
                            outg.fillRect(rx, ry, w, h);
                        } else {
                            outg.drawImage(sub, rx, ry, null);
                            
                            if (showlines) {
                                outg.setColor(Color.BLACK);
                                outg.fillRect(rx, ry, w, 1);
                                outg.fillRect(rx + w, ry, 1, h);
                                outg.fillRect(rx, ry + h, w, 1);
                                outg.fillRect(rx, ry, 1, h);
                            }
                        }
                    }
                    
                    if (movsub != null) {
                        int y = moving / npiecesx;
                        int x = moving - (y * npiecesx);
                        
                        int gripx = (int) (mouseclickpoint.getX()) - (x * w);
                        int gripy = (int) (mouseclickpoint.getY()) - (y * h);
                        
                        outg.drawImage(movsub, (int) mouse.getX() - gripx, (int) mouse.getY() - gripy, null);
                    }
                    
                    g.drawImage(out, 0, 0, null);
                }
            }
        }, BorderLayout.CENTER);
        
        String last = Settings.get(".Last");
        if (last != null && new File(last).exists()) {
            load(new File(last));
        }
        
        f.setVisible(true);
    }
}