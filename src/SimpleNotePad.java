import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Scanner;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

/*
 * Jonathan Wishcamper
 * CS410 Assignment 3
 * Due 6-11-2019
 * 
 * Code Smells:
 * 1) actionPerformed method too long - "Bloater". 
 * 2) Bad naming conventions. Very hard to understand what variables are 
 * 3) SimpleNotePad constructor too long and has duplicate code
 * 4) Extra code that does nothing in "paste" section of actionPerformed
 * 5) Extra undo function that is not implemented
 * 
 * Fixes:
 * 1) Moved print and save logic from actionPerformed to their own methods
 * 2) Renamed all variables to more properly reflect what they do
 * 3) Created a new setupMenu() method and several helper functions to reduce duplicate code and shrink SimpleNotePad constructor
 * 4) Removed extra pointless code in "paste" 
 * 5) Removed undo menu item and extra code
 * More misc. changes/fixes:
 * Commented code
 * Minor changes in print/save methods
 * Removed some unused imports
 */

public class SimpleNotePad extends JFrame implements ActionListener{
	//changed many variable names from 1 or 2 letter identifiers to more unique and understandable names
    JMenuBar topMenuBar = new JMenuBar();
    JMenu fileMenu = new JMenu("File");
    JMenu editMenu = new JMenu("Edit");
    JTextPane textArea = new JTextPane();
    JMenuItem newButton = new JMenuItem("New File");
    JMenuItem saveButton = new JMenuItem("Save File");
    JMenuItem printButton = new JMenuItem("Print File");
    JMenuItem openButton = new JMenuItem("Open File");
    JMenu recentMenu = new JMenu("Recent");
    JMenuItem copyButton = new JMenuItem("Copy");
    JMenuItem pasteButton = new JMenuItem("Paste");
    JMenuItem replaceButton = new JMenuItem("Replace");
    JMenuItem recentSubitems[] = new JMenuItem[5];
    File recentFiles[] = new File[5];
    JFileChooser fileChooser = new JFileChooser();
    
    public SimpleNotePad() {
        setTitle("A Simple Notepad Tool");
        setupMenu();
        add(new JScrollPane(textArea));
        setPreferredSize(new Dimension(600,600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        pack();
    }
    
    //created new method to set up menu bar, reduces bloat in constructor
    private void setupMenu() {
    	addWithSeperator(newButton);
    	addWithSeperator(saveButton);
    	addWithSeperator(printButton);
    	addWithSeperator(openButton);
    	fileMenu.add(recentMenu);
    	
    	//initialize recent submenu to (none) for all items
    	for(int i =0;i<5;i++) {
    		recentSubitems[i]=new JMenuItem("(none)");
    		recentMenu.add(recentSubitems[i]);
    		setupMenuItem(recentSubitems[i],"recent"+i);
    	}
    	
        editMenu.add(copyButton);
        editMenu.add(pasteButton);
        editMenu.add(replaceButton);
        
        topMenuBar.add(fileMenu);
        topMenuBar.add(editMenu);
        
        //changed each to setupMenuItem 
        setupMenuItem(newButton, "new");
        setupMenuItem(saveButton, "save");
        setupMenuItem(printButton, "print");
        setupMenuItem(copyButton, "copy");
        setupMenuItem(pasteButton, "paste");
        setupMenuItem(openButton, "open");
        setupMenuItem(recentMenu, "recent");
        setupMenuItem(replaceButton, "replace");
        
        setJMenuBar(topMenuBar);
    }
    //created new method to handle duplicate code in constructor
    private void setupMenuItem(JMenuItem menuItem, String name) {
    	menuItem.addActionListener(this);
    	menuItem.setActionCommand(name);
    }
    //created new method to handle duplicate code in constructor
    private void addWithSeperator(JMenuItem menuItem) {
    	fileMenu.add(menuItem);
        fileMenu.addSeparator();
    }
    //helper function to properly rearrange items on recent list
    private void fixRecentFiles(File f) {
    	//if the file is already in the list
    	if(Arrays.stream(recentFiles).anyMatch(f::equals)) {
			int index = -1;
			for(int i = 0;i<recentFiles.length;i++) {
				if(recentFiles[i]==f)
					index = i;
			}
			//now move everything before or equal to index down in array, leave everything after index
			for(int i =index;i>0;i--) {
				recentFiles[i]=recentFiles[i-1];
    	    	recentSubitems[i].setText(recentFiles[i].getName());
			}
		}
    	//if new file not in list
    	else {
	    	for(int i = 4;i>0;i--) {
	    		if(recentFiles[i-1]!=null) {
	    			recentFiles[i]=recentFiles[i-1];
	    	    	recentSubitems[i].setText(recentFiles[i].getName());
	    	    }
	    	}	
    	}
    	recentFiles[0]=f;
    	recentSubitems[0].setText(recentFiles[0].getName());
    }
    //opens the file passed as a parameter
    private void openFile(File f) {
    	textArea.setText("");
		Scanner in = null;
		try {
			in = new Scanner(f);
			while(in.hasNext()) {
				String line = in.nextLine();
				textArea.getStyledDocument().insertString(textArea.getSelectionEnd(), line+"\n", null);
			}
		} catch (Exception e) {	} 
    }
    //opens the selected file from recent list and fixes the list appropriately
    private void openRecent(File f) {
    	if(f!=null) {
    		openFile(f);
	        fixRecentFiles(f);
    	}
    }
    
    public static void main(String[] args) {
        SimpleNotePad app = new SimpleNotePad();
    }
    @Override
    //created new methods for print and save
    public void actionPerformed(ActionEvent e) {
        if(e.getActionCommand().equals("new")) {
            textArea.setText("");
        }else if(e.getActionCommand().equals("save")) {
        	saveFile();           
        }else if(e.getActionCommand().equals("print")) {
        	printFile();         
        }else if(e.getActionCommand().equals("copy")) {
            textArea.copy();
        }else if(e.getActionCommand().equals("replace")) {
            replace();
        }else if(e.getActionCommand().equals("open")) {
            open();
        }else if(e.getActionCommand().equals("recent0")) {
        	openRecent(recentFiles[0]);
        }else if(e.getActionCommand().equals("recent1")) {
        	openRecent(recentFiles[1]);
        }else if(e.getActionCommand().equals("recent2")) {
        	openRecent(recentFiles[2]);
        }else if(e.getActionCommand().equals("recent3")) {
        	openRecent(recentFiles[3]);
        }else if(e.getActionCommand().equals("recent4")) {
        	openRecent(recentFiles[4]);
        }else if(e.getActionCommand().equals("paste")) {
        	//removed extra code that prints to console
        	textArea.paste(); }
    }
    //replaces selection with input from dialog box
    private void replace() {
    	String userInput = JOptionPane.showInputDialog("Replace with:");
    	if(userInput!=null) //if user clicks cancel, showInputDialog will return null. Only replace if not null.
    		textArea.replaceSelection(userInput);
    }
    //opens dialog box then opens file if one is chosen
    private void open() {
    	if (fileChooser.showOpenDialog(null)==JFileChooser.APPROVE_OPTION) {
    		File f = fileChooser.getSelectedFile();
    		openFile(f);
	        fixRecentFiles(f);		
		}
    }
    
    private void saveFile() {
    	File fileToWrite = null;
        //combined into 1 statement instead of declaring int returnVal
        //wrapped this all in if statement so it wont attempt to save if dialog box closed
        if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            fileToWrite = fileChooser.getSelectedFile();
	        try {
	            PrintWriter out = new PrintWriter(new FileWriter(fileToWrite));
	            out.println(textArea.getText());
	            JOptionPane.showMessageDialog(null, "File is saved successfully...");
	            out.close();
	            fixRecentFiles(fileToWrite);
	        } catch (IOException ex) { }
        }
    }
    
    private void printFile() {
    	try{
            PrinterJob pjob = PrinterJob.getPrinterJob();
            pjob.setJobName("Sample Command Pattern");
            pjob.setCopies(1);
            pjob.setPrintable(new Printable() {
                public int print(Graphics pg, PageFormat pf, int pageNum) {
                    if (pageNum>0)
                        return Printable.NO_SUCH_PAGE;
                    pg.drawString(textArea.getText(), 500, 500);
                    paint(pg);
                    return Printable.PAGE_EXISTS;
                }
            });

            if (pjob.printDialog() == false)
                return;
            pjob.print();
        } catch (PrinterException pe) {
            JOptionPane.showMessageDialog(null,
                    "Printer error" + pe, "Printing error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
}