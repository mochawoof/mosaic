    import javax.swing.*;
    import javax.swing.filechooser.FileNameExtensionFilter;
    
    import java.awt.*;
    import java.awt.event.*;
    
    import java.io.File;
    import javax.imageio.ImageIO;
    import java.awt.image.BufferedImage;
    
    import java.util.ArrayList;
    
    class Main {
        private static File file;
        private static BufferedImage image;
        private static int[] pieces;
        private static int npiecesx;
        
        private static Point mouseclickpoint;
        private static Point mouse;
        private static int moving;
        
        private static boolean showlines;
        private static int performance;
        
        private static final String acceptedfiles = "png,jpg,jpeg,bmp,wbmp";
        private static final String version = "1.2";
        private static final int windowheight = 500;
        
        private static JFrame f;
        private static Confetti confetti = null;
        private static long timestarted = -1;
        
        public static void update() {
            int oldnpiecesx = npiecesx;
            try {
                npiecesx = (int) Math.sqrt(Integer.parseInt(Settings.get("#Difficulty")));
            } catch (Exception e) {
                npiecesx = 4;
            }
            if (npiecesx <= 0 || npiecesx > 16) {
                npiecesx = 4;
            }
            if (file != null && oldnpiecesx != npiecesx) {
                reload();
                shuffle();
            }
            
            // List look and feels
            // for (javax.swing.UIManager.LookAndFeelInfo l : javax.swing.UIManager.getInstalledLookAndFeels()) {System.out.println(l.getName() + " " + l.getClassName());}
            try {
                if (Settings.get("Theme").equals("System")) {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } else {
                    for (UIManager.LookAndFeelInfo laf : UIManager.getInstalledLookAndFeels()) {
                        if (laf.getName().equals(Settings.get("Theme"))) {
                            UIManager.setLookAndFeel(laf.getClassName());
                        }
                    }
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
    
            if (Settings.get("Performance").equals("Pretty")) {
                performance = Image.SCALE_SMOOTH;
            } else {
                performance = Image.SCALE_FAST;
            }
        }
        
        private static void doconfetti() {
            confetti = new Confetti((int) (f.getWidth() * 0.5), 6.0, f.getHeight());
        }
        
        private static boolean check() {
            for (int i = 0; i < pieces.length; i++) {
                if (pieces[i] != i) {
                    return false;
                }
            }
            return true;
        }
        
        private static void shuffle() {
            for (int i = 0; i < pieces.length; i++) {
                int rdm = (int) (Math.random() * pieces.length);
                int tmp = pieces[rdm];
                pieces[rdm] = pieces[i];
                pieces[i] = tmp;
            }
            f.repaint();
            savemove();
            timestarted = System.currentTimeMillis();
        }
        
        private static void reload() {
            load(file);
        }
        
        private static void load(File fl) {
            try {
                image = ImageIO.read(fl);
                
                int w = -1;
                int h = -1;
                boolean shouldresize = false;
                if (image.getWidth() > 1000 || image.getHeight() > 1000 && performance == Image.SCALE_SMOOTH) {
                    shouldresize = true;
                    w = 1000;
                    h = 1000;
                } else if (image.getWidth() > 500 || image.getHeight() > 500) {
                    shouldresize = true;
                    w = 500;
                    h = 500;
                }

                if (shouldresize) {
                    if (image.getWidth() > image.getHeight()) {
                        h = (int) (((double) image.getHeight() / image.getWidth()) * 1000);
                    } else {
                        w = (int) (((double) image.getWidth() / image.getHeight()) * 1000);
                    }
                    System.out.println("Resizing image from " + image.getWidth() + "x" + image.getHeight() + " to " + w + "x" + h);
                    
                    Image tempimage = image.getScaledInstance(w, h, Image.SCALE_SMOOTH);
                    image = new BufferedImage(tempimage.getWidth(null), tempimage.getHeight(null), BufferedImage.TYPE_INT_ARGB);
                    image.getGraphics().drawImage(tempimage, 0, 0, null);
                }
                
                Settings.set(".Last", fl.getAbsolutePath());
                file = fl;
                double imageasp = ((double) image.getWidth() / image.getHeight());
                f.setSize((int) (imageasp * windowheight), windowheight);
                
                pieces = new int[npiecesx * npiecesx];
                for (int i = 0; i < pieces.length; i++) {
                    pieces[i] = i;
                }
                
                f.repaint();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        
        private static JMenu generatePicturesJMenu() {
            JMenu menu = new JMenu("Pictures");
            File picturesfolder = new File("pictures/");
            ArrayList<File> allpictures = new ArrayList<File>();
            if (!picturesfolder.exists()) {
                picturesfolder.mkdirs();
            } else {
                for (File f : picturesfolder.listFiles()) {
                    if (f.isDirectory()) {
                        JMenu categorymenu = new JMenu(f.getName());
                        for (File sf : f.listFiles()) {
                            boolean accepted = false;
                            for (String af : acceptedfiles.split(",")) {
                                if (sf.getName().endsWith("." + af)) {
                                    accepted = true;
                                }
                            }
                            if (accepted) {
                                JMenuItem it = new JMenuItem(sf.getName());
                                it.addActionListener(new ActionListener() {
                                    public void actionPerformed(ActionEvent e) {
                                        if (askendgame()) {
                                            load(sf);
                                            shuffle();
                                        }
                                    }
                                });
                                categorymenu.add(it);
                                allpictures.add(sf);
                            }
                        }
                        menu.add(categorymenu);
                    }
                }
            }
            
            menu.addSeparator();
            
            JMenuItem random = new JMenuItem("Random");
            random.setMnemonic(KeyEvent.VK_R);
            random.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (askendgame()) {
                        int rdm = (int) (((double) Math.random()) * allpictures.size());
                        load(allpictures.get(rdm));
                        shuffle();
                    }
                }
            });
            menu.add(random);
            
            JMenuItem openfolder = new JMenuItem("Open Folder");
            openfolder.setMnemonic(KeyEvent.VK_O);
            openfolder.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        Desktop.getDesktop().open(picturesfolder);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
            menu.add(openfolder);
                
            return menu;
        }
        
        public static boolean askendgame() {
            if (file != null && file.exists()) {
                int opt = JOptionPane.showConfirmDialog(f, "Are you sure you want to end your current game?", "Confirm", JOptionPane.WARNING_MESSAGE);
                if (opt == JOptionPane.OK_OPTION) {
                    return true;
                }
                return false;
            }
            return true;
        }
        
        private static void savemove() {
            String g = serializegame();
            Settings.set(".Game", g);
            Settings.set(".Time", (System.currentTimeMillis() - timestarted) + "");
        }
        
        private static String serializegame() {
            String g = "";
            for (int p : pieces) {
                if (!g.equals("")) {
                    g += ",";
                }
                g += p;
            }
            return g;
        }
        
        private static void loadgame(String game) {
            String[] splgame = game.split(",");
            if (splgame.length == pieces.length) {
                for (int i = 0; i < splgame.length; i++) {
                    int change = pieces[i];
                    try {
                        change = Integer.parseInt(splgame[i]);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    pieces[i] = change;
                }
                f.repaint();
                if (!check()) {
                    timestarted = System.currentTimeMillis();
                } else {
                    timestarted = -1;
                }
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
            
            Image icon = Res.getAsImage("res/icon.png");
            f.setIconImage(icon);
            
            Image background = Res.getAsImage("res/background.png");
            BufferedImage bufferedbackground = new BufferedImage(background.getWidth(null), background.getHeight(null), BufferedImage.TYPE_INT_ARGB);
            bufferedbackground.getGraphics().drawImage(background, 0, 0, null);
            
            update();
            
            JMenuBar mb = new JMenuBar();
            f.setJMenuBar(mb);
            
            JMenu gamem = new JMenu("Game");
            gamem.setMnemonic(KeyEvent.VK_G);
            mb.add(gamem);
            
            JMenuItem reshuffle = new JMenuItem("Reshuffle");
            reshuffle.setMnemonic(KeyEvent.VK_R);
            reshuffle.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (file != null) {
                        shuffle();
                    }
                }
            });
            gamem.add(reshuffle);
            
            JMenu pictures = generatePicturesJMenu();
            pictures.setMnemonic(KeyEvent.VK_P);
            
            mb.add(pictures);
            
            JMenu settings = Settings.generateJMenu("Settings");
            settings.setMnemonic(KeyEvent.VK_S);
            mb.add(settings);
            
            JMenu help = new JMenu("Help");
            help.setMnemonic(KeyEvent.VK_H);
            mb.add(help);
            
                JMenuItem howtoplay = new JMenuItem("How To Play");
                howtoplay.setMnemonic(KeyEvent.VK_H);
                howtoplay.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        JLabel helplabel = new JLabel(Res.getAsString("res/howtoplay.html"));
                        
                        JScrollPane scroll = new JScrollPane(helplabel);
                        scroll.setPreferredSize(new Dimension(300, 300));
                        scroll.getVerticalScrollBar().setUnitIncrement(10);
                        
                        JOptionPane.showOptionDialog(f, scroll, "How To Play", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, new String[] {"OK"}, "OK");
                    }
                });
                help.add(howtoplay);
                
                JMenuItem about = new JMenuItem("About Mosaic");
                about.setMnemonic(KeyEvent.VK_A);
                about.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        JOptionPane.showMessageDialog(f, "Mosaic v" + version + "\nCasual mosaic puzzle game\nhttps://github.com/mochawoof/mosaic\n\nJava " +
                         System.getProperty("java.version") + " " + System.getProperty("java.vendor") +
                          "\n" + System.getProperty("os.name") + " " + System.getProperty("os.version") + " " + System.getProperty("os.arch"), "About Mosaic", JOptionPane.PLAIN_MESSAGE, new ImageIcon(f.getIconImage()));
                    }
                });
                help.add(about);
                
            JMenu time = new JMenu("0:00");
            mb.add(Box.createGlue());
            mb.add(time);

            Timer timer = new Timer(200, new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (timestarted != -1) {
                        long diff = System.currentTimeMillis() - timestarted;
                        int min = (int) ((diff / 1000) / 60);
                        int sec = (int) ((diff / 1000) - (min * 60));
                        time.setText(min + ":" + ((sec <= 9) ? "0" + sec : sec));
                    }
                }
            });
            timer.start();
            
            f.add(new JComponent() {
                {
                    addMouseListener(new MouseListener() {
                        public void mouseEntered(MouseEvent e) {}
                        public void mouseExited(MouseEvent e) {}
                        public void mouseClicked(MouseEvent e) {}
                        
                        public void mousePressed(MouseEvent e) {
                            if (e.getButton() == MouseEvent.BUTTON1 && image != null && timestarted != -1) {
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
                                                                
                                if (moving != movedto && check()) {
                                    doconfetti();
                                    timestarted = -1;
                                }
                                
                                mouse = null;
                                mouseclickpoint = null;
                                moving = -1;
                                
                                f.repaint();
                                savemove();
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
                        
                        sg.drawImage(bufferedbackground.getScaledInstance(getWidth(), getHeight(), Image.SCALE_FAST), 0, 0, null);
                        sg.drawImage(image.getScaledInstance(getWidth(), getHeight(), performance), 0, 0, null);
                        
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
                        
                        if (confetti != null && !confetti.done) {
                            confetti.draw(this, g);
                        }
                }
            }
        }, BorderLayout.CENTER);
        
        f.setVisible(true);
        
        // Load saved game
        String last = Settings.get(".Last");
        if (last != null && new File(last).exists()) {
            load(new File(last));
            
            loadgame(Settings.get(".Game"));
            if (!check()) {
                try {
                    timestarted = System.currentTimeMillis() - Integer.parseInt(Settings.get(".Time"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                timestarted = -1;
            }
        }
    }
}