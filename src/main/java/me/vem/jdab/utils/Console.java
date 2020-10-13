package me.vem.jdab.utils;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

import me.vem.jdab.DiscordBot;

public class Console {

    private static Console instance;
    public static boolean hasInstance() { return instance != null; }
    public static Console getInstance() {
        if(!hasInstance())
            initialize();
        
        return instance;
    }
    
    public static boolean initialize() {
        if(GraphicsEnvironment.isHeadless()) {
            System.out.println("Headless environment detected.");
            return false;
        }
        
        if(hasInstance())
            throw new IllegalStateException("Attempted to initialize Console even though it has already been initialized.");
        
        instance = new Console();
        return true;
    }
    
    //JFrame Elements
    private JFrame console;
	private JTextArea consoleOutput;
	
	//JMenu Elements
	private JMenuBar menuBar;

    private TrayIcon tray;
    
	private PrintStream out;

	private Console() {
        buildTextArea();
        buildTrayIcon();
        buildConsole();
	}
	
	private void buildConsole() {
	    if(console != null)
	        return;
	    
        console = new JFrame(Version.getVersion() + " Console");
        console.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        console.setContentPane(new JScrollPane(consoleOutput));
        console.setJMenuBar(getMenuBar());
        console.setSize(new Dimension(600, 400));
        console.setLocationRelativeTo(null);
        console.setVisible(true);
        
        console.addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent windowEvent) {
                dispose();
            }
        });
	}
	
	/**
	 * Loads the text area object and redirection System.out and System.err to print to the text area.
	 */
	private void buildTextArea() {
		if(consoleOutput != null)
		    return;
		
		consoleOutput = new JTextArea();
		consoleOutput.setEditable(false);
		
		out = new PrintStream(new OutputStream() {
			@Override public void write(int i) throws IOException {
				char c = (char)i;
				consoleOutput.append(String.valueOf(c));
				if(c == '\n') consoleOutput.update(consoleOutput.getGraphics());
			}
		});
		
		PrintThread.getInstance().addOut(out);
		PrintThread.getInstance().addErr(out);
	}
	
	/**
	 * Activates the Windows Tray icon that this application will run out of. <br>
	 * Will not do anything for non-Windows systems (To my knowledge).
	 */
	private void buildTrayIcon() {
		if(tray != null || !SystemTray.isSupported())
			return;
		
		try {
			SystemTray systray = SystemTray.getSystemTray();
			InputStream imageStream = Console.class.getClassLoader().getResourceAsStream("tray.png");
			if(imageStream == null) {
				Logger.err("tray.png resource not found! The tray has failed to load.");
				return;
			}
			Image icon = ImageIO.read(imageStream);
			tray = new TrayIcon(icon, Version.getVersion().getName(), null);
			tray.setImageAutoSize(true);
			tray.addMouseListener(new MouseAdapter() {
				@Override public void mouseClicked(MouseEvent e) {
					if(e.getButton() == MouseEvent.BUTTON1)
						buildConsole();
				}
			});
			systray.add(tray);
		}catch(AWTException | IOException e) {
			e.printStackTrace();
		}
	}

    public JMenuBar getMenuBar() {
        if(menuBar != null)
            return menuBar;
        
        menuBar = new JMenuBar();
        menuBar.setLayout(new FlowLayout(FlowLayout.RIGHT));
        
        JMenu menu = new JMenu("Options");
        menuBar.add(menu);
        
        JMenuItem shutdown = new JMenuItem("Shutdown Bot");
        shutdown.addActionListener(e -> shutdownBot());
        menu.add(shutdown);
        
        return menuBar;
    }
    
    /**
     * Closes the current console window.
     * Will also shut the bot down if the tray icon is not supported.
     */
    public void dispose() {
        if (console == null)
            return;

        if (!SystemTray.isSupported() || tray == null)
            if(!shutdownBot()) 
                return;
        
        console.dispose();
        console = null;
        
        if(DiscordBot.hasInstance())
            return;

        destroyTray();
        
        if(PrintThread.hasInstance()) {
            PrintThread printer = PrintThread.getInstance();

            out.flush();
            printer.removeOut(out);
            printer.removeErr(out);
            printer.kill();
        }
        
        out.close();
        out = null;
        consoleOutput = null;
    }
	
	public void destroyTray() {
		if(tray == null) return;
		SystemTray.getSystemTray().remove(tray);
		tray = null;
	}
	
	public boolean shutdownBot() {
	    return shutdownBot(false);
	}
	
	/**
	 * @param force Determines whether the user should be prompted for a confirmation or not. force of true will cause no confirmation and immediately shutdown the bot.
	 * @return Whether the bot has been shutdown or not. Will also return true if the bot has already been shutdown previously.
	 */
	public boolean shutdownBot(boolean force) {
	    if(!DiscordBot.hasInstance()) {
	        Logger.info("Attempted to shutdown the bot, but it seems to have already been shutdown.");
	        return true;
	    }
	    
	    if(!force) {
	        int res = JOptionPane.showConfirmDialog(console,
	                "Are you sure?", "Shutdown Bot", JOptionPane.YES_NO_OPTION,
	                JOptionPane.QUESTION_MESSAGE);
	        
	        if(res != JOptionPane.YES_OPTION)
	            return false;   
	    }

        try {
            DiscordBot.getInstance().shutdown();            
        }catch(Exception e) {
            Logger.err("An error occured while attempting to shutdown the bot. It is unsure whether the bot is still running or not. This may cause some unexpected behavior. Please notify the developer.");
            e.printStackTrace();
            return false;
        }
        
        Logger.info("The bot has been successfully shutdown! Closing this window will now terminate the program.");
		return true;
	}
}