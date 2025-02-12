import java.util.Properties;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;

import javax.swing.*;
import java.awt.*;
import java.util.Enumeration;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;

class Settings {
    public static Properties props;
    private static String filename = ".settings";
    private static final boolean DEBUG = false;
    
    // Consts for show
    public static final int OK = 0;
    public static final int CANCEL = 1;
    public static final int RESET = 2;
    
    // Settings with a . preceding their names will not be shown to the user
    // Underscores will be shown to the user as spaces
    // When generateJMenu is used Yes/No values will be treated as checkboxes
    public static HashMap<String, String[]> defaults = new HashMap<String, String[]>() {{
        put("Theme", new String[] {"System", "Cross-Platform"});
        put("Performance", new String[] {"Fast", "Pretty"});
        put("Difficulty", new String[] {"16", "4", "64", "256"});
        put("Show_Lines", new String[] {"Yes", "No"});
        put(".Last", new String[] {""});
    }};

    private static void update() {
        Main.update();
    }
    
    private static void applyDefaults() {
        for (HashMap.Entry<String, String[]> e : defaults.entrySet()) {
            props.put(e.getKey(), e.getValue()[0]);
        }
    }
    
    static {
        props = new Properties();
        applyDefaults();
        
        try {
            FileInputStream in = new FileInputStream(filename);
            props.load(in);
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static String get(String key) {
        return (String) props.get(key);
    }
    
    public static void reset() {
        props = new Properties();
        applyDefaults();
        save();
    }
    
    private static void save() {
        try {
            FileOutputStream out = new FileOutputStream(filename);
            props.store(out, "");
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void set(String key, String value) {
        props.put(key, value);
        save();
    }
    
    public static JMenu generateJMenu(String name) {
        JMenu menu = new JMenu(name);
        
        Enumeration en = Settings.props.propertyNames();
        
        while (en.hasMoreElements()) {
            String key = (String) en.nextElement();
            String[] defaultVals = defaults.get(key);
            
            if (defaultVals != null && (!key.startsWith(".") || DEBUG)) {
                if (defaultVals.length == 2 && defaultVals[0].toLowerCase().equals("yes") && defaultVals[1].toLowerCase().equals("no")) {
                    JCheckBoxMenuItem it = new JCheckBoxMenuItem(key.replace("_", " "));
                    if (Settings.get(key).toLowerCase().equals("yes")) {
                        it.setState(true);
                    }
                    it.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            Settings.set(key, (it.getState() == true) ? "Yes" : "No");
                            update();
                        }
                    });
                    menu.add(it);
                } else {
                    JMenu it = new JMenu(key.replace("_", " "));
                    for (String s : defaultVals) {
                        JCheckBoxMenuItem opt = new JCheckBoxMenuItem(s);
                        if (Settings.get(key).equals(s)) {
                            opt.setState(true);
                        }
                        
                        opt.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                Settings.set(key, s);
                                for (Component c : it.getMenuComponents()) {
                                    JCheckBoxMenuItem j = (JCheckBoxMenuItem) c;
                                    if (!j.getText().equals(opt.getText())) {
                                        j.setState(false);
                                    }
                                }
                                opt.setState(true);
                                
                                update();
                            }
                        });
                        
                        it.add(opt);
                    }
                    menu.add(it);
                }
            }
        }
        
        return menu;
    }
    
    public static int show(JFrame parent) {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(0, 2));
        
        Enumeration en = Settings.props.propertyNames();
        
        HashMap<String, JComboBox> boxes = new HashMap<String, JComboBox>();
        
        while (en.hasMoreElements()) {
            String key = (String) en.nextElement();
            
            String[] defaultVals = defaults.get(key);
            
            // Make sure default values exist
            if (defaultVals != null && (!key.startsWith(".") || DEBUG)) {
                panel.add(new JLabel(key.replace("_", " ")));
                            
                JComboBox comboBox = new JComboBox(defaultVals);
                comboBox.setEditable(true);
                comboBox.setSelectedItem(Settings.get(key));
                
                panel.add(comboBox);
                boxes.put(key, comboBox);
            }
        }
        
        int opt = JOptionPane.showOptionDialog(parent, new JScrollPane(panel), "Settings", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, new String[] {"OK", "Cancel", "Reset"}, "OK");

        if (opt == OK) {
            for (HashMap.Entry<String, JComboBox> e : boxes.entrySet()) {
                set(e.getKey().replace(" ", "_"), (String) e.getValue().getSelectedItem());
            }
        } else if (opt == RESET) {
            reset();
        }
        
        update();
        return opt;
    }
}