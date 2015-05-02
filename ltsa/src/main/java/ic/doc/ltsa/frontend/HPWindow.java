/******************************************************************************
 * LTSA (Labelled Transition System Analyser) - LTSA is a verification tool   *
 * for concurrent systems. It mechanically checks that the specification of a *
 * concurrent system satisfies the properties required of its behaviour.      *
 * Copyright (C) 2001-2004 Jeff Magee (with additions by Robert Chatley)      *
 *                                                                            *
 * This program is free software; you can redistribute it and/or              *
 * modify it under the terms of the GNU General Public License                *
 * as published by the Free Software Foundation; either version 2             *
 * of the License, or (at your option) any later version.                     *
 *                                                                            *
 * This program is distributed in the hope that it will be useful,            *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the              *
 * GNU General Public License for more details.                               *
 *                                                                            *
 * You should have received a copy of the GNU General Public License          *
 * along with this program; if not, write to the Free Software                *
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA *
 *                                                                            *
 * The authors can be contacted by email at {jnm,rbc}@doc.ic.ac.uk            *
 *                                                                            *
 ******************************************************************************/

// This is an experimental version with progress & LTL property check
package ic.doc.ltsa.frontend;

import ic.doc.extension.*;
import ic.doc.ltsa.frontend.dclap.Gr2PICT;
import ic.doc.ltsa.frontend.editor.ColoredEditorKit;
import ic.doc.ltsa.frontend.gui.*;
import ic.doc.ltsa.lts.AnalysisFactory;
import ic.doc.ltsa.common.*;
import ic.doc.ltsa.common.iface.IAnalysisFactory;
import ic.doc.ltsa.common.iface.IAssertDefinition;
import ic.doc.ltsa.common.iface.IAutomata;
import ic.doc.ltsa.common.iface.ICompactState;
import ic.doc.ltsa.common.iface.ICompositeState;
import ic.doc.ltsa.common.iface.ISuperTrace;
import ic.doc.ltsa.common.iface.ITransitionPrinter;
import ic.doc.ltsa.common.iface.LTSOutput;
import ic.doc.ltsa.common.infra.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.*;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.jar.JarFile;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;
import javax.swing.undo.*;

/**
 * This is the main GUI class for LTSA. It creates the main window and searches
 * for plugins. HPWindow interrogates plugins for menu/toolbar items and tabs
 * for the display, before installing them.
 */
public class HPWindow extends JFrame implements LTSA, LTSOutput, Runnable { //LTSInput,

    private static final String VERSION = "V3.0, plugin support";
    private static final String DEFAULT = "DEFAULT";

    private static HPWindow oHPWindow;

    static {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}

        oHPWindow = new HPWindow(null);
    }

    private CompileManager   oCompileManager;
    private IAnalysisFactory oAnalysisFactory;

    private JTextArea      oOutput;
    private JEditorPane    oInput;
    private JEditorPane    oManual;
    private JEditorPane    oSynthesis;
    private JScrollPane    oSynthScroller;
    private AlphabetWindow oAlphabet;
    private PrintWindow    oPrintWnd;
    private LTSDrawWindow  oDrawWnd;
    private JTabbedPane    oTabs;
    private JToolBar       oToolBar;
    private JMenuBar       oMenuBar;
    private JComboBox      oTargetChoice;
    
    private JFrame oAnimator = null;
        
    private Map<LTSAButton, ILTSAPlugin> oPluginButtons;
    private Map<String,JMenu> oMenus;

    private EventManager eman = new EventManager();
    private Collection<EventClient> oEventListeners;
    private ICompositeState oCurrent = null;
    
    private String run_menu = DEFAULT;
    private String asserted = null;

    private StringBuffer oInternalBuffer;

    private Initialisable oScenebeansInit = null;

    // Listener for the edits on the current document.
    protected UndoableEditListener undoHandler = new UndoHandler();

    // UndoManager that we add edits to.
    protected UndoManager undo = new UndoManager();

    private JMenu file, edit, check, build, window, help, option;
    private JMenuItem file_new, file_open, file_save, file_saveAs, file_export, file_exit, edit_cut, edit_copy, edit_paste, edit_undo, edit_redo, check_safe,
            check_progress, check_reachable, check_stop, build_parse, build_compile, build_compose, build_minimise, help_about, supertrace_options;

    private JMenu check_run, file_example, check_liveness;
    private JMenuItem default_run;
    private JMenuItem[] run_items, assert_items;
    private String[] run_names, assert_names;
    private boolean[] run_enabled;
    private JCheckBoxMenuItem setWarnings, setWarningsAreErrors, setFair, setPartialOrder;
    private JCheckBoxMenuItem setObsEquiv, setReduction, setBigFont, setDisplayName;
    private JCheckBoxMenuItem setNewLabelFormat, setAutoRun, setMultipleLTS, help_manual;
    private JCheckBoxMenuItem window_alpha, window_print, window_draw, window_hidden, setProbabilistic;

    private Map<String, ILTSAPlugin> oPlugins;
    private List<ILTSAPlugin> oCurrentPlugins;

    // tool bar buttons - that need to be enabled and disabled
    private JButton stopTool, parseTool, safetyTool, progressTool, copyTool, cutTool, pasteTool, newFileTool, openFileTool, saveFileTool, compileTool, composeTool,
            minimizeTool, undoTool, redoTool;

    private Font fixed = new Font("Monospaced", Font.PLAIN, 12);
    private Font big   = new Font("Monospaced", Font.BOLD, 18);

    private int oStatesExplored;
    private int oTransitionsTaken;
    private int oTabIndex = 0;
    
    private AppletButton isApplet = null;

    public HPWindow(AppletButton isap) {

        oPlugins = new HashMap<String, ILTSAPlugin>();
        oCurrentPlugins = new ArrayList<ILTSAPlugin>();
        oEventListeners = new ArrayList<EventClient>();

        oCompileManager = new CompileManager( this );
        
        isApplet = isap;
  
        getContentPane().setLayout(new BorderLayout());

        oMenus = new HashMap<String,JMenu>();
        oTabs = new JTabbedPane();

        initialiseEditors();

        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension screen = tk.getScreenSize();
        oTabs.setPreferredSize(new Dimension((int) (screen.getWidth() * 0.8), (int) (screen.getHeight() * 0.8)));

        getContentPane().add("Center", oTabs);

        oPluginButtons = new HashMap<LTSAButton, ILTSAPlugin>();
        window = new JMenu("Window");

        // status field used to name the composition we are wrking on
        oTargetChoice = new JComboBox();
        oTargetChoice.setEditable(false);
        oTargetChoice.addItem(DEFAULT);
        oTargetChoice.setToolTipText("Target Composition");
        oTargetChoice.setRequestFocusEnabled(false);
        oTargetChoice.addActionListener(new TargetAction());

        initialiseMenus();
        initaliseToolBar();
       
        // enable menus
        menuEnable(true);
        file_save.setEnabled(isApplet == null);
        file_saveAs.setEnabled(isApplet == null);
        file_export.setEnabled(isApplet == null);
        saveFileTool.setEnabled(isApplet == null);
        updateDoState();

        // switch to edit tab
        LTSCanvas.setDisplayName(setDisplayName.isSelected());
        LTSCanvas.seyNewLabelFormat(setNewLabelFormat.isSelected());
        LTSDrawWindow.singleMode = !setMultipleLTS.isSelected();
        newDrawWindow(window_draw.isSelected());

        // switch to edit tab
        swapto(0);

        setEditControls();

        // close window action
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new CloseWindow());

        oInternalBuffer = new StringBuffer();
        
        com.chatley.magicbeans.PluginManager.getInstance().addBackDatedObserver(this);
    }

    //----------------------------------------------------------------------

    private void initialiseEditors() {

        // edit window for specification source

        oInput = new JEditorPane();
        oInput.setEditorKit(new ColoredEditorKit());
        oInput.setFont(fixed);
        oInput.setBackground(Color.white);
        oInput.getDocument().addUndoableEditListener(undoHandler);
        undo.setLimit(10); //set maximum undo edits

        JScrollPane inp = new JScrollPane(oInput, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        oSynthesis = new JEditorPane();
        oSynthesis.setEditorKit(new ColoredEditorKit());
        oSynthesis.setFont(fixed);
        oSynthesis.setBackground(Color.white);
       	oSynthesis.setBorder(new EmptyBorder(0,5,0,0));

        oSynthScroller = new JScrollPane
            (oSynthesis,
             JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
             JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
            );

        oTabs.addTab("Edit", inp);
        oTabs.setBackgroundAt(0, Color.green);
        // results window
        oOutput = new JTextArea("", 30, 100);
        oOutput.setEditable(false);
        oOutput.setFont(fixed);
        oOutput.setBackground(Color.white);
        oOutput.setLineWrap(true);
        oOutput.setWrapStyleWord(true);
        oOutput.setBorder(new EmptyBorder(0, 5, 0, 0));
        JScrollPane outp = new JScrollPane(oOutput, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        oTabs.addTab("Output", outp);
        oTabs.setBackgroundAt(1, Color.lightGray);
        oTabs.addChangeListener(new TabChange());
        oTabs.setRequestFocusEnabled(false);
    }

    private void initialiseMenus() {

        //Build the menu bars.
        oMenuBar = new JMenuBar();
        setJMenuBar(oMenuBar);

        // file menu
        file = new JMenu("File");
        oMenuBar.add(file);
        oMenus.put("File", file);

        file_new = new JMenuItem("New");
        file_new.addActionListener(new NewFileAction());
        file.add(file_new);

        file_open = new JMenuItem("Open...");
        file_open.addActionListener(new OpenFileAction());
        file.add(file_open);

        file_save = new JMenuItem("Save");
        file_save.addActionListener(new SaveFileAction());
        file.add(file_save);

        make_file_menu();

        file_export = new JMenuItem("Export...");
        file_export.addActionListener(new ExportFileAction());
        file.add(file_export);

        file_example = new JMenu("Examples");
        new Examples(file_example, this).getExamples();
        file.add(file_example);

        file_exit = new JMenuItem("Quit");
        file_exit.addActionListener(new ExitFileAction());
        file.add(file_exit);

        //edit menu
        edit = new JMenu("Edit");
        oMenuBar.add(edit);
        oMenus.put("Edit", edit);

        edit_cut = new JMenuItem("Cut");
        edit_cut.addActionListener(new EditCutAction());
        edit.add(edit_cut);
        edit_copy = new JMenuItem("Copy");
        edit_copy.addActionListener(new EditCopyAction());
        edit.add(edit_copy);
        edit_paste = new JMenuItem("Paste");
        edit_paste.addActionListener(new EditPasteAction());
        edit.add(edit_paste);
        edit.addSeparator();
        edit_undo = new JMenuItem("Undo");
        edit_undo.addActionListener(new UndoAction());
        edit.add(edit_undo);
        edit_redo = new JMenuItem("Redo");
        edit_redo.addActionListener(new RedoAction());
        edit.add(edit_redo);

        // check menu
        check = new JMenu("Check");
        oMenuBar.add(check);
        oMenus.put("Check", check);

        check_safe = new JMenuItem("Safety");
        check_safe.addActionListener(new DoAction(DO_safety));
        check.add(check_safe);
        check_progress = new JMenuItem("Progress");
        check_progress.addActionListener(new DoAction(DO_progress));
        check.add(check_progress);
        check_liveness = new JMenu("LTL property");
        if (hasLTL2BuchiJar())
            check.add(check_liveness);
        check_run = new JMenu("Run");
        check.add(check_run);
        default_run = new JMenuItem(DEFAULT);
        default_run.addActionListener(new ExecuteAction(DEFAULT));
        check_run.add(default_run);
        check_reachable = new JMenuItem("Supertrace");
        check_reachable.addActionListener(new DoAction(DO_reachable));
        check.add(check_reachable);
        check_stop = new JMenuItem("Stop");
        check_stop.addActionListener(new StopAction());
        check_stop.setEnabled(false);
        check.add(check_stop);

        //build menu
        build = new JMenu("Build");
        oMenuBar.add(build);
        oMenus.put("Build", build);

        build_parse = new JMenuItem("Parse");
        build_parse.addActionListener(new DoAction(DO_parse));
        build.add(build_parse);
        build_compile = new JMenuItem("Compile");
        build_compile.addActionListener(new DoAction(DO_compile));
        build.add(build_compile);
        build_compose = new JMenuItem("Compose");
        build_compose.addActionListener(new DoAction(DO_doComposition));
        build.add(build_compose);
        build_minimise = new JMenuItem("Minimise");
        build_minimise.addActionListener(new DoAction(DO_minimiseComposition));
        build.add(build_minimise);

        //window menu
        oMenuBar.add(window);
        oMenus.put("Window", window);

        window_alpha = new JCheckBoxMenuItem("Alphabet");
        window_alpha.setSelected(false);
        window_alpha.addActionListener(new WinAlphabetAction());
        window.add(window_alpha);
        window_print = new JCheckBoxMenuItem("Transitions");
        window_print.setSelected(false);
        window_print.addActionListener(new WinPrintAction());
        window.add(window_print);
        window_draw = new JCheckBoxMenuItem("Draw");
        window_draw.setSelected(true);
        window_draw.addActionListener(new WinDrawAction());
        window.add(window_draw);
        window_hidden = new JCheckBoxMenuItem("Synthesis Buffer");
        window_hidden.setSelected(false);
        window_hidden.addActionListener(new WinHiddenAction());
        window.add(window_hidden);

        //help menu
        help = new JMenu("Help");
        oMenuBar.add(help);
        oMenus.put("Help", help);

        help_about = new JMenuItem("About");
        help_about.addActionListener(new HelpAboutAction());
        help.add(help_about);
        help_manual = new JCheckBoxMenuItem("Manual");
        help_manual.setSelected(false);
        help_manual.addActionListener(new HelpManualAction());
        help.add(help_manual);

        //option menu
        OptionAction opt = new OptionAction();
        option = new JMenu("Options");
        oMenuBar.add(option);
        oMenus.put("Options", option);

        setWarnings = new JCheckBoxMenuItem("Display warning messages");
        setWarnings.addActionListener(opt);
        option.add(setWarnings);
        setWarnings.setSelected(true);
        setWarningsAreErrors = new JCheckBoxMenuItem("Treat warnings as errors");
        setWarningsAreErrors.addActionListener(opt);
        option.add(setWarningsAreErrors);
        setWarningsAreErrors.setSelected(false);
        setFair = new JCheckBoxMenuItem("Fair Choice for LTL check");
        setFair.addActionListener(opt);
        option.add(setFair);
        setFair.setSelected(true);
        setPartialOrder = new JCheckBoxMenuItem("Partial Order Reduction");
        setPartialOrder.addActionListener(opt);
        option.add(setPartialOrder);
        setPartialOrder.setSelected(false);
        setObsEquiv = new JCheckBoxMenuItem("Preserve OE for POR composition");
        setObsEquiv.addActionListener(opt);
        option.add(setObsEquiv);
        setObsEquiv.setSelected(true);
        setReduction = new JCheckBoxMenuItem("Enable Tau Reduction");
        setReduction.addActionListener(opt);
        option.add(setReduction);
        setReduction.setSelected(true);
        supertrace_options = new JMenuItem("Set Supertrace parameters");
        supertrace_options.addActionListener(new SuperTraceOptionListener());
        option.add(supertrace_options);
        option.addSeparator();
        setBigFont = new JCheckBoxMenuItem("Use big font");
        setBigFont.addActionListener(opt);
        option.add(setBigFont);
        setBigFont.setSelected(false);
        setDisplayName = new JCheckBoxMenuItem("Display name when drawing LTS");
        setDisplayName.addActionListener(opt);
        option.add(setDisplayName);
        setDisplayName.setSelected(true);
        setNewLabelFormat = new JCheckBoxMenuItem("Use V2.0 label format when drawing LTS");
        setNewLabelFormat.addActionListener(opt);
        option.add(setNewLabelFormat);
        setNewLabelFormat.setSelected(false);
        setMultipleLTS = new JCheckBoxMenuItem("Multiple LTS in Draw window");
        setMultipleLTS.addActionListener(opt);
        option.add(setMultipleLTS);
        setMultipleLTS.setSelected(false);
        option.addSeparator();
        setAutoRun = new JCheckBoxMenuItem("Auto run actions in Animator");
        setAutoRun.addActionListener(opt);
        option.add(setAutoRun);
        setAutoRun.setSelected(false);
        option.addSeparator();
    }

	private void make_file_menu() {
		file_saveAs = new JMenuItem("Save as...");
		file_saveAs.addActionListener(new SaveAsFileAction());
		file.add(file_saveAs);
	}

    private void initaliseToolBar() {

        oToolBar = new JToolBar();
        oToolBar.setFloatable(false);
        oToolBar.add(newFileTool = createTool("new.gif", "New file", new NewFileAction()));
        oToolBar.add(openFileTool = createTool("open.gif", "Open file", new OpenFileAction()));
        oToolBar.add(saveFileTool = createTool("save.gif", "Save File", new SaveFileAction()));
        oToolBar.addSeparator();
        oToolBar.add(cutTool = createTool("cut.gif", "Cut", new EditCutAction()));
        oToolBar.add(copyTool = createTool("copy.gif", "Copy", new EditCopyAction()));
        oToolBar.add(pasteTool = createTool("paste.gif", "Paste", new EditPasteAction()));
        oToolBar.add(undoTool = createTool("undo.gif", "Undo", new UndoAction()));
        oToolBar.add(redoTool = createTool("redo.gif", "Redo", new RedoAction()));
        oToolBar.addSeparator();
        oToolBar.add(parseTool = createTool("parse.gif", "Parse", new DoAction(DO_parse)));
        oToolBar.add(compileTool = createTool("compile.gif", "Compile", new DoAction(DO_compile)));
        oToolBar.add(composeTool = createTool("compose.gif", "Compose", new DoAction(DO_doComposition)));
        oToolBar.add(minimizeTool = createTool("minimize.gif", "Minimize", new DoAction(DO_minimiseComposition)));
        oToolBar.addSeparator();
        oToolBar.add(oTargetChoice);
        oToolBar.addSeparator();
        oToolBar.add(safetyTool = createTool("safety.gif", "Check safety", new DoAction(DO_safety)));
        oToolBar.add(progressTool = createTool("progress.gif", "Check Progress", new DoAction(DO_progress)));
        oToolBar.add(stopTool = createTool("stop.gif", "Stop", new StopAction()));
        stopTool.setEnabled(false);
        oToolBar.addSeparator();
        oToolBar.add(createTool("alphabet.gif", "Run DEFAULT Animation", new ExecuteAction(DEFAULT)));
        oToolBar.add(createTool("blanker.gif", "Blank Screen", new BlankAction()));
   
        getContentPane().add("North", oToolBar);
    }

    public static void centre(Component c) {

        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension screen = tk.getScreenSize();
        Dimension ltsa = c.getSize();
        double x = (screen.getWidth() - ltsa.getWidth()) / 2;
        double y = (screen.getHeight() - ltsa.getHeight()) / 2;
        c.setLocation((int) x, (int) y);
    }

    //----------------------------------------------------------------------
    void left(Component c) {

        Point ltsa = getLocationOnScreen();
        ltsa.translate(10, 100);
        c.setLocation(ltsa);
    }

    //-----------------------------------------------------------------------
    protected JButton createTool(String icon, String tip, ActionListener act) {

        String xIconFile = "/ic/doc/ltsa/frontend/gui/icon/" + icon;
        
        JButton b = new JButton(new ImageIcon(this.getClass().getResource(xIconFile))) {
            public float getAlignmentY() {

                return 0.5f;
            }
        };
        b.setRequestFocusEnabled(false);
        b.setMargin(new Insets(0, 0, 0, 0));
        b.setToolTipText(tip);
        b.addActionListener(act);
        return b;
    }

    //------------------------------------------------------------------------

    void menuEnable(boolean flag) {

        String pp = oTabs.getTitleAt(oTabIndex = oTabs.getSelectedIndex());
        boolean b = (oTabIndex == 0);
        ILTSAPlugin x_plugin = oPlugins.get(pp);
        boolean application = (isApplet == null);

        file_new.setEnabled(flag && (b || (x_plugin != null && x_plugin.providesNewFile())));
        file_example.setEnabled(flag && b);
        file_open.setEnabled(flag && (b || (x_plugin != null && x_plugin.providesOpenFile())));
        file_exit.setEnabled(flag);
        check_safe.setEnabled(flag);
        check_progress.setEnabled(flag);
        check_run.setEnabled(flag);
        check_reachable.setEnabled(flag);
        build_parse.setEnabled(flag);
        build_compile.setEnabled(flag);
        build_compose.setEnabled(flag);
        build_minimise.setEnabled(flag);
        parseTool.setEnabled(flag);
        safetyTool.setEnabled(flag);
        progressTool.setEnabled(flag);
        compileTool.setEnabled(flag);
        composeTool.setEnabled(flag);
        minimizeTool.setEnabled(flag);

        for (Iterator<LTSAButton> i = oPluginButtons.keySet().iterator(); i.hasNext();) {

            LTSAButton x_lb = i.next();
            ILTSAPlugin x_pl = oPluginButtons.get(x_lb);

            // if a button is related to a tab other than that currently
            // selected, grey it out
            if (x_pl != null && x_pl.addAsTab()) {
                x_lb.setEnabled(x_plugin == x_pl);
            } else {
                x_lb.setEnabled(true);
            }
        }
    }

    //------------------------------------------------------------------------
    private final static int DO_safety = 1;
    private final static int DO_execute = 3;
    private final static int DO_reachable = 4;
    private final static int DO_compile = 5;
    private final static int DO_doComposition = 6;
    private final static int DO_minimiseComposition = 7;
    private final static int DO_progress = 8;
    private final static int DO_liveness = 9;
    private final static int DO_parse = 10;

    private int theAction = 0;
    private Thread executer;

    private void do_action(int action) {

        menuEnable(false);
        check_stop.setEnabled(true);
        stopTool.setEnabled(true);
        theAction = action;
        executer = new Thread(this);
        executer.setPriority(Thread.NORM_PRIORITY - 1);
        executer.start();
    }

    public void run() {

        try {
            switch (theAction) {
            case DO_safety:
                showOutput();
                safety();
                break;
            case DO_execute:
                execute();
                break;
            case DO_reachable:
                showOutput();
                reachable();
                break;
            case DO_compile:
                showOutput();
                compile();
                break;
            case DO_doComposition:
                showOutput();
                doComposition();
                break;
            case DO_minimiseComposition:
                showOutput();
                minimiseComposition();
                break;
            case DO_progress:
                showOutput();
                progress();
                break;
            case DO_liveness:
                showOutput();
                liveness();
                break;
            case DO_parse:
                parse();
                break;
            }
        } catch (Throwable e) {
            showOutput();
            outln("**** Runtime Exception: " + e);
            e.printStackTrace();
        }
        menuEnable(true);
        check_stop.setEnabled(false);
        stopTool.setEnabled(false);
    }

    //------------------------------------------------------------------------

    class CloseWindow extends WindowAdapter {
        public void windowClosing(WindowEvent e) {

            quitAll();
        }

        public void windowActivated(WindowEvent e) {

            if (oAnimator != null)
                oAnimator.toFront();
        }
    }

    //------------------------------------------------------------------------

    //changed from private so can be called from plugin
    public void invalidateState() {

        oCurrent = null;
        oTargetChoice.removeAllItems();
        oTargetChoice.addItem(DEFAULT);
        check_run.removeAll();
        check_run.add(default_run);
        run_items = null;
        assert_items = null;
        run_names = null;
        check_liveness.removeAll();
        validate();
        eman.post(new LTSEvent(LTSEvent.INVALID, null));
        if (oAnimator != null) {
            oAnimator.dispose();
            oAnimator = null;
        }
    }

    //changed to public from private so can be called from plugins
    public void postState(ICompositeState m) {

        if (oAnimator != null) {
            oAnimator.dispose();
            oAnimator = null;
        }
        eman.post(new LTSEvent(LTSEvent.INVALID, m));
    }

    //------------------------------------------------------------------------
    // File handling
    //-----------------------------------------------------------------------

    private final static String fileType = "*.lts";
    private String openFile = fileType;
    private String oCurrentDir;
    private String savedText = "";

    private void newFile() {

        if (checkSave()) {
            setTitle("LTS Analyser");
            savedText = "";
            openFile = fileType;
            oInput.setText("");
            swapto(0);
            oOutput.setText("");
            invalidateState();
        }
        repaint(); //hack to solve display problem
    }

    public void newExample(String dir, String ex) {

        undo.discardAllEdits();
        oInput.getDocument().removeUndoableEditListener(undoHandler);
        if (checkSave()) {
            invalidateState();
            clearOutput();
            doOpenFile(dir, ex, true);
        }
        oInput.getDocument().addUndoableEditListener(undoHandler);
        updateDoState();
        repaint();
    }

    //------------------------------------------------------------------------
    private void openAFile() {

        if (checkSave()) {
            invalidateState();
            clearOutput();
            FileDialog fd = new FileDialog(this, "Select source file:");
            if (oCurrentDir != null)
                fd.setDirectory(oCurrentDir);

            // if the currently selected plugin provides a file extension
            // use that, otherwise look for lts files

            String x_tab = oTabs.getTitleAt(oTabs.getSelectedIndex());
            ILTSAPlugin x_plugin = oPlugins.get(x_tab);

            if (x_plugin != null && x_plugin.getFileExtension() != null) {
                fd.setFile("*." + x_plugin.getFileExtension());
            } else {
                fd.setFile(fileType);
            }

            fd.setVisible(true);
            doOpenFile(oCurrentDir = fd.getDirectory(), fd.getFile(), false);
        }
        repaint(); //hack to solve display problem
    }

    private void doOpenFile(String dir, String f, boolean resource) {

        if (f != null) {
			
			BufferedReader xInput = null;
			
            try {
                openFile = f;
                setTitle("LTSA - " + openFile);
                InputStream xIs;
                if (!resource) {

                    // if the currently selected plugin provides an open file
                    // routine, call that, otherwise proceed with the default
                    // lts behaviour

                    String xTab = oTabs.getTitleAt(oTabs.getSelectedIndex());
                    ILTSAPlugin xPlugin = oPlugins.get(xTab);
                    if (xPlugin != null && xPlugin.providesOpenFile()) {
                        xPlugin.openFile(new File(dir + openFile));
                        return;

                    } else {
                        xIs = new FileInputStream(dir + openFile);
                    }

                } else {

                    JarFile xJf = new JarFile("examples.jar");
                    xIs = xJf.getInputStream(xJf.getEntry(dir + openFile));

                    // now turn the FileInputStream into a DataInputStream
                }

                xInput = new BufferedReader(new InputStreamReader(xIs));
                String xLine;
                StringBuffer xBuf = new StringBuffer();
                while ((xLine = xInput.readLine()) != null) {
                    xBuf.append(xLine + "\n");
                }
                savedText = xBuf.toString();
                oInput.setText(savedText);
                parse();
            }
            // end try
            catch (IOException e) {
                outln("Error creating FileInputStream: " + e);
                e.printStackTrace();
            }
			finally {
				try {
					if ( xInput != null ) xInput.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
        }

    }   
    //------------------------------------------------------------------------

    private void saveAsFile() {

        String xTab = oTabs.getTitleAt(oTabs.getSelectedIndex());
        if (xTab.equals("Draw")) {
            oDrawWnd.saveFile();
            return;
        }

        FileDialog fd = new FileDialog(this, "Save file in:", FileDialog.SAVE);
        if (oCurrentDir != null)
            fd.setDirectory(oCurrentDir);

        ILTSAPlugin x_plugin = oPlugins.get(xTab);

        if (x_plugin != null && x_plugin.getFileExtension() != null) {
            fd.setFile("*." + x_plugin.getFileExtension());
        } else {
            fd.setFile(openFile);
        }

        fd.setVisible(true);
        String tmp = fd.getFile();
        if (tmp != null) { //if not cancelled

            if (x_plugin != null && x_plugin.providesSaveFile()) {

                try {
                    x_plugin.saveFile(new FileOutputStream(fd.getDirectory() + fd.getFile()));
                } catch (IOException p_ioe) {
                    outln("File not found : " + fd.getDirectory() + fd.getFile());
                }
                return;

            } else {

                oCurrentDir = fd.getDirectory();
                openFile = tmp;
                setTitle("LTSA - " + openFile);
                saveFile();
            }
        }
    }

    private void saveFile() {

        String x_tab = oTabs.getTitleAt(oTabs.getSelectedIndex());
        ILTSAPlugin x_plugin = oPlugins.get(x_tab);

        if (x_plugin != null && x_plugin.providesSaveFile()) {
            saveAsFile();
        } else if (openFile != null && openFile.equals("*.lts"))
            saveAsFile();
        else if (openFile != null)
            try {
                int i = openFile.indexOf('.', 0);
                if (i > 0)
                    openFile = openFile.substring(0, i) + "." + "lts";
                else
                    openFile = openFile + ".lts";
                String tempname = (oCurrentDir == null) ? openFile : oCurrentDir + openFile;
                FileOutputStream fout = new FileOutputStream(tempname);
                // now convert the FileOutputStream into a PrintStream
                PrintStream myOutput = new PrintStream(fout);
                
                if ( x_tab.equals( "Synthesis" )) {
                    savedText = oSynthesis.getText();
                } else {
                    savedText = oInput.getText();
                }
                
                myOutput.print(savedText);
                myOutput.close();
                fout.close();
                outln("Saved in: " + tempname);
            } catch (IOException e) {
                outln("Error saving file: " + e);
            }
    }

    //-------------------------------------------------------------------------

    private void exportFile() {

        String message = "Export as Aldebaran format (.aut) to:";
        FileDialog fd = new FileDialog(this, message, FileDialog.SAVE);
        if (oCurrent == null || oCurrent.getComposition() == null) {
            JOptionPane.showMessageDialog(this, "No target composition to export");
            return;
        }
        String fname = oCurrent.getComposition().getName();
        fd.setFile(fname + ".aut");
        fd.setDirectory(oCurrentDir);
        fd.setVisible(true);
        String sn;
        if ((sn = fd.getFile()) != null)
            try {
                int i = sn.indexOf('.', 0);
                sn = sn.substring(0, i) + ".aut";
                File file = new File(fd.getDirectory(), sn);
                FileOutputStream fout = new FileOutputStream(file);
                // now convert the FileOutputStream into a PrintStream
                PrintStream myOutput = new PrintStream(fout);
                oCurrent.getComposition().printAUT(myOutput);
                myOutput.close();
                fout.close();
                outln("Exported to: " + fd.getDirectory() + file);
            } catch (IOException e) {
                outln("Error exporting file: " + e);
            }
    }

    //------------------------------------------------------------------------
    // return false if operation cancelled otherwise true
    private boolean checkSave() {

        if (isApplet != null)
            return true;
        if (!savedText.equals(oInput.getText())) {
            int result = JOptionPane.showConfirmDialog(this, "Do you want to save the contents of " + openFile);
            if (result == JOptionPane.YES_OPTION) {
                saveFile();
                return true;
            } else if (result == JOptionPane.NO_OPTION)
                return true;
            else if (result == JOptionPane.CANCEL_OPTION)
                return false;
        }
        return true;
    }

    //------------------------------------------------------------------------
    private void doFont() {

        if (setBigFont.getState()) {
            oInput.setFont(big);
            oOutput.setFont(big);
			oSynthesis.setFont(big);
        } else {
            oInput.setFont(fixed);
            oOutput.setFont(fixed);
			oSynthesis.setFont(fixed);
        }
        pack();
        setVisible(true);
    }

    //------------------------------------------------------------------------

    private void quitAll() {

        if (isApplet != null) {
            this.dispose();
            isApplet.ended();
        } else {
            if (checkSave())
                System.exit(0);
        }
    }

    //----Event
    // Handling-----------------------------------------------------------

    class NewFileAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {

            String x_tab = oTabs.getTitleAt(oTabs.getSelectedIndex());
            ILTSAPlugin x_plugin = oPlugins.get(x_tab);
            if (x_plugin != null && x_plugin.providesNewFile()) {
                x_plugin.newFile();
                return;
            }

            undo.discardAllEdits();
            oInput.getDocument().removeUndoableEditListener(undoHandler);
            newFile();
            oInput.getDocument().addUndoableEditListener(undoHandler);
            updateDoState();
        }
    }

    class OpenFileAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {

            undo.discardAllEdits();
            oInput.getDocument().removeUndoableEditListener(undoHandler);
            openAFile();
            oInput.getDocument().addUndoableEditListener(undoHandler);
            updateDoState();
        }
    }

    class SaveFileAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {

            String x_tab = oTabs.getTitleAt(oTabs.getSelectedIndex());

            if (x_tab.equals("Edit") || x_tab.equals("Output"))
                saveFile();
            else if (x_tab.equals("Alphabet"))
                oAlphabet.saveFile();
            else if (x_tab.equals("Transitions"))
                oPrintWnd.saveFile(oCurrentDir, ".txt");
            else if (x_tab.equals("Draw"))
                oDrawWnd.saveFile();
            else
                saveFile();
        }
    }

    class SaveAsFileAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {

            // String pp = textIO.getTitleAt(textIO.getSelectedIndex());
            //if (pp.equals("Edit"))
            saveAsFile();
        }
    }

    class ExportFileAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {

            String pp = oTabs.getTitleAt(oTabs.getSelectedIndex());
            if (pp.equals("Edit"))
                exportFile();
            else if (pp.equals("Transitions"))
                // temporary
                //prints.saveFile(currentDirectory,".aut");
                oPrintWnd.saveAsPrism(oCurrentDir, ".pm");
        }
    }

    class ExitFileAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {

            quitAll();
        }
    }

    class DoAction implements ActionListener {
        int actionCode;

        DoAction(int a) {

            actionCode = a;
        }

        public void actionPerformed(ActionEvent e) {

            do_action(actionCode);
            ;
        }
    }

    class OptionAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {

            Object source = e.getSource();
            if (source == setWarnings)
                Diagnostics.warningFlag = setWarnings.isSelected();
            else if (source == setWarningsAreErrors)
                Diagnostics.warningsAreErrors = setWarningsAreErrors.isSelected();
            else if (source == setFair)
                oAnalysisFactory.createProgressCheck().setStrongFairFlag( setFair.isSelected() );
            else if (source == setPartialOrder)
                oAnalysisFactory.createAutomata().setPartialOrderReduction( setPartialOrder.isSelected() );
            else if (source == setObsEquiv)
                oAnalysisFactory.createAutomata().setPreserveObsEquiv( setObsEquiv.isSelected() );
            else if (source == setReduction)
                oAnalysisFactory.createCompositeState().setReduction( setReduction.isSelected() );
            else if (source == setBigFont) {
                AnimWindow.fontFlag = setBigFont.isSelected();
                AlphabetWindow.fontFlag = setBigFont.isSelected();
                if (oAlphabet != null)
                    oAlphabet.setBigFont(setBigFont.isSelected());
                PrintWindow.fontFlag = setBigFont.isSelected();
                if (oPrintWnd != null)
                    oPrintWnd.setBigFont(setBigFont.isSelected());
                LTSDrawWindow.fontFlag = setBigFont.isSelected();
                if (oDrawWnd != null)
                    oDrawWnd.setBigFont(setBigFont.isSelected());
                LTSCanvas.setFontFlag(setBigFont.isSelected());

                for (Iterator<ILTSAPlugin> i = oPlugins.values().iterator(); i.hasNext();) {

                    (i.next()).setBigFont(setBigFont.isSelected());
                }

                doFont();
            } else if (source == setDisplayName) {
                if (oDrawWnd != null)
                    oDrawWnd.setDrawName(setDisplayName.isSelected());
                LTSCanvas.setDisplayName(setDisplayName.isSelected());
            } else if (source == setMultipleLTS) {
                LTSDrawWindow.singleMode = !setMultipleLTS.isSelected();
                if (oDrawWnd != null)
                    oDrawWnd.setMode(LTSDrawWindow.singleMode);
            } else if (source == setNewLabelFormat) {
                if (oDrawWnd != null)
                    oDrawWnd.setNewLabelFormat(setNewLabelFormat.isSelected());
                LTSCanvas.seyNewLabelFormat(setNewLabelFormat.isSelected());
            } 
        }
    }

    class SuperTraceOptionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {

            setSuperTraceOption();
        }
    }

    class WinAlphabetAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {

            newAlphabetWindow(window_alpha.isSelected());
        }
    }

    class WinPrintAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {

            newPrintWindow(window_print.isSelected());
        }
    }

    class WinDrawAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {

            newDrawWindow(window_draw.isSelected());
        }
    }

    class WinHiddenAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {

            newSynthWindow(window_hidden.isSelected());
        }
    }

    class HelpAboutAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {

            aboutDialog();
        }
    }

    class BlankAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {

            blankit();
        }
    }

    class HelpManualAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {

            displayManual(help_manual.isSelected());
        }
    }

    class StopAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {

            if (executer != null) {
                executer.stop();
                menuEnable(true);
                check_stop.setEnabled(false);
                stopTool.setEnabled(false);
                outln("\n\t-- stopped");
                executer = null;
            }
        }
    }

    class ExecuteAction implements ActionListener {
        String runtarget;

        ExecuteAction(String s) {

            runtarget = s;
        }

        public void actionPerformed(ActionEvent e) {

            run_menu = runtarget;
            do_action(DO_execute);
        }
    }

    class LivenessAction implements ActionListener {
        String asserttarget;

        LivenessAction(String s) {

            asserttarget = s;
        }

        public void actionPerformed(ActionEvent e) {

            asserted = asserttarget;
            do_action(DO_liveness);
        }
    }

    class EditCutAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {

            String x_tab = oTabs.getTitleAt(oTabs.getSelectedIndex());
            ILTSAPlugin x_plugin = oPlugins.get(x_tab);
            if (x_plugin != null && x_plugin.providesCut()) {
                x_plugin.cut();
            } else {
                oInput.cut();
            }
        }
    }

    class EditCopyAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {

            String x_tab = oTabs.getTitleAt(oTabs.getSelectedIndex());
            ILTSAPlugin x_plugin = oPlugins.get(x_tab);
            if (x_plugin != null && x_plugin.providesCopy()) {
                x_plugin.copy();
            } else {

                if (x_tab.equals("Edit"))
                    oInput.copy();
                else if (x_tab.equals("Output"))
                    oOutput.copy();
                else if (x_tab.equals("Manual"))
                    oManual.copy();
                else if (x_tab.equals("Alphabet"))
                    oAlphabet.copy();
                else if (x_tab.equals("Transitions"))
                    oPrintWnd.copy();
            }
        }
    }

    class EditPasteAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {

            String x_tab = oTabs.getTitleAt(oTabs.getSelectedIndex());
            ILTSAPlugin x_plugin = oPlugins.get(x_tab);
            if (x_plugin != null && x_plugin.providesPaste()) {
                x_plugin.paste();
            } else {
                oInput.paste();
            }
        }
    }

    class TargetAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {

            String choice = (String) oTargetChoice.getSelectedItem();
            if (choice == null)
                return;
            run_enabled = oAnalysisFactory.getMenuDefinition().enabled(choice);
            if (run_items != null && run_enabled != null) {
                if (run_items.length == run_enabled.length)
                    for (int i = 0; i < run_items.length; ++i)
                        run_items[i].setEnabled(run_enabled[i]);
            }
        }
    }

    class ToggleWindowAction implements ActionListener {

        private String o_name;

        ToggleWindowAction(String p_name) {

            o_name = p_name;
        }

        public void actionPerformed(ActionEvent p_ae) {

            JCheckBoxMenuItem x_mi = (JCheckBoxMenuItem) p_ae.getSource();

            if (!x_mi.isSelected()) {

                oTabs.remove(oTabs.indexOfTab(o_name));

            } else {

                oTabs.add(o_name, (oPlugins.get(o_name)).getComponent());
            }
        }
    }
    
    //--------------------------------------------------------------------
    //  undo editor stuff

    class UndoHandler implements UndoableEditListener {
        public void undoableEditHappened(UndoableEditEvent e) {

            undo.addEdit(e.getEdit());
            updateDoState();
        }
    }

    class UndoAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {

            try {
                undo.undo();
            } catch (CannotUndoException ex) {
            }
            updateDoState();
        }
    }

    class RedoAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {

            try {
                undo.redo();
            } catch (CannotUndoException ex) {
            }
            updateDoState();
        }
    }

    //changed from protected so can be called from plugin
    public void updateDoState() {

        edit_undo.setEnabled(undo.canUndo());
        undoTool.setEnabled(undo.canUndo());
        edit_redo.setEnabled(undo.canRedo());
        redoTool.setEnabled(undo.canRedo());
    }

    //------------------------------------------------------------------------
 
    private void swapto(int i) {

        if (i == oTabIndex)
            return;
        oTabs.setBackgroundAt(i, Color.green);
        if (oTabIndex != i && oTabIndex < oTabs.getTabCount())
            oTabs.setBackgroundAt(oTabIndex, Color.lightGray);
        oTabIndex = i;
        setEditControls();
        oTabs.setSelectedIndex(i);
    }

    //public version of swapto, taking a tab name, to be called from plugins

    public void swapto(String p_tabname) {

        swapto(oTabs.indexOfTab(p_tabname));
    }

    class TabChange implements ChangeListener {
        public void stateChanged(ChangeEvent e) {

            int i = oTabs.getSelectedIndex();
            if (i == oTabIndex)
                return;
            oTabs.setBackgroundAt(i, Color.green);
            oTabs.setBackgroundAt(oTabIndex, Color.lightGray);
            oTabIndex = i;
            setEditControls();
        }
    }

    private void setEditControls() {

        boolean app = (isApplet == null);
        String pp = oTabs.getTitleAt( oTabs.getSelectedIndex());
        boolean b = (pp.equals("Edit") || pp.equals("Synthesis"));

        ILTSAPlugin xPlugin = oPlugins.get(pp);

        if (xPlugin != null) {
            xPlugin.selected();
        }

        edit_cut.setEnabled(b || (xPlugin != null && xPlugin.providesCut()));
        cutTool.setEnabled(b || (xPlugin != null && xPlugin.providesCut()));
        edit_paste.setEnabled(b || (xPlugin != null && xPlugin.providesPaste()));
        pasteTool.setEnabled(b || (xPlugin != null && xPlugin.providesPaste()));
        edit_copy.setEnabled(b || (xPlugin != null && xPlugin.providesCopy()));
        copyTool.setEnabled(b || (xPlugin != null && xPlugin.providesCopy()));

        file_new.setEnabled(b || (xPlugin != null && xPlugin.providesNewFile()));
        file_example.setEnabled(b);
        file_open.setEnabled(app && (b || (xPlugin != null && xPlugin.providesOpenFile())));
        file_save.setEnabled(app && (b || (xPlugin != null && xPlugin.providesSaveFile())));
        file_saveAs.setEnabled(app && (b || pp.equals("Draw") || (xPlugin != null && xPlugin.providesSaveFile())));
        file_export.setEnabled(app && (xPlugin == null));
        newFileTool.setEnabled(b || (xPlugin != null && xPlugin.providesNewFile()));
        openFileTool.setEnabled(app && (b || (xPlugin != null && xPlugin.providesOpenFile())));
        saveFileTool.setEnabled(app && (b || pp.equals("Draw") || (xPlugin != null && xPlugin.providesSaveFile())));
        edit_undo.setEnabled(b && undo.canUndo());
        undoTool.setEnabled(b && undo.canUndo());
        edit_redo.setEnabled(b && undo.canRedo());
        redoTool.setEnabled(b && undo.canRedo());
        if (b)
            oInput.requestFocusInWindow();

        for (Iterator<LTSAButton> i = oPluginButtons.keySet().iterator(); i.hasNext();) {

            LTSAButton xButton = i.next();
            ILTSAPlugin xPlg = oPluginButtons.get(xButton);

            // if a button is related to a tab other than that currently
            // selected, grey it out
            if (xPlg != null && xPlg.addAsTab()) {

                xButton.setEnabled(xPlugin == xPlg);

            } else {
                xButton.setEnabled(true);
            }
        }

        if (b) {
            SwingUtilities.invokeLater(new RequestFocus());
        }
    }

    class RequestFocus implements Runnable {

        public void run() {

            oInput.requestFocusInWindow();
        }
    }

    //------------------------------------------------------------------------

    public void out(String str) {

        SwingUtilities.invokeLater(new OutputAppend(str));
    }

    public void outln(String str) {

        SwingUtilities.invokeLater(new OutputAppend(str + "\n"));
    }

    public void clearOutput() {

        SwingUtilities.invokeLater(new OutputClear());
    }

    public void showOutput() {

        SwingUtilities.invokeLater(new OutputShow());
    }

    class OutputAppend implements Runnable {
        String text;

        OutputAppend(String text) {

            this.text = text;
        }

        public void run() {

            oOutput.append(text);
        }
    }

    class OutputClear implements Runnable {
        public void run() {

            oOutput.setText("");
        }
    }

    class OutputShow implements Runnable {
        public void run() {

            swapto(1);
        }
    }

    static class ScheduleOpenFile implements Runnable {
        HPWindow window;
        String arg;

        ScheduleOpenFile(HPWindow window, String arg) {

            this.window = window;
            this.arg = arg;
        }

        public void run() {

            window.doOpenFile("", arg, false);
        }
    }

    private boolean hasLTL2BuchiJar() {

        try {
            new gov.nasa.ltl.graph.Graph();
            return true;
        } catch (NoClassDefFoundError e) {
            return false;
        }
    }

    /* now returns a boolean to indicate success of compilation - jew01 */
 
    private void displayError(LTSException x) {

        String xFileName = null;
        int i = 0;
        int line = 0;    
                
        if (x.marker != null) {
            i = ((Integer) (x.marker)).intValue();
            
          xFileName = oCompileManager.getSourceNameForMarker(i);
           String xSrc = oCompileManager.getSourceByName( xFileName ).getCompleteSource();
            
            int l =  oCompileManager.getLocalMarker(i);
            
            line = 1;
            for (int j = 0; j < l; ++j) {
                if (xSrc.charAt(j) == '\n') {
                    ++line;
                }
            }
        } 
        
        if ( xFileName != null && xFileName.equals("Edit") && x.marker != null ) {            
            outln("ERROR line:" + line + " - " + x.getMessage());
            oInput.select(i, i + 1);       
        } else if ( xFileName != null && x.marker != null ) {          
            outln("ERROR in " + xFileName + " at line:" + line + " - " + x.getMessage());
        } else if ( xFileName != null && x.marker == null ) {        
            outln("ERROR in " + xFileName + " - " + x.getMessage());
        } else {
            outln("ERROR - " + x.getMessage());
       	}	
   	}
      
    private String getSource( String pBuffer ) {
        
        if ( pBuffer.equals( "Edit" ) ) { return oInput.getText(); }
        if ( pBuffer.equals( "Synthesis" ) ) { return oSynthesis.getText(); } 
        
        return "";
    }
    
    private CompositeInputSource getSources() {
        
        CompositeInputSource xSources = new CompositeInputSource();
        
        xSources.add( new InputSource( "Edit" , oInput.getText() + "\n"));
        xSources.add( new InputSource( "Synthesis" , oSynthesis.getText() + "\n" ));
        
        return xSources;
    }
    
    private boolean compile() {

        clearOutput();
        if (!parse())
            return false;
        
        try {
            oCurrent = oCompileManager.compile( (String)oTargetChoice.getSelectedItem() );
        } catch ( LTSException pEx ) {
            displayError( pEx );
        }
        
        if (oCurrent != null)
            postState(oCurrent);
        else
            return false;
        return true;
    }
    
    private boolean compileIfChange() {

        if ( oCurrent == null || ! oCurrent.getName().equals(oTargetChoice.getSelectedItem()))
            return compile();
        return true;
    }
    
    //------------------------------------------------------------------------

    private void safety() {

        clearOutput();
        if (compileIfChange() && oCurrent != null) {
            oCurrent.analyse(this);
        }
    }

    //------------------------------------------------------------------------

    private void progress() {

        clearOutput();
        if (compileIfChange() && oCurrent != null) {
            oCurrent.checkProgress(this);
        }
    }

    //------------------------------------------------------------------------

    private void liveness() {

        clearOutput();
        if (!compileIfChange())
            return;
        
        IAssertDefinition xAssertDef = oAnalysisFactory.createAssertDefinition();
        
        ICompositeState ltl_property = xAssertDef.compile(this, asserted);
        if (oCurrent != null && ltl_property != null) {
            oCurrent.checkLTL(this, ltl_property);
            postState(oCurrent);
        }
    }

    //------------------------------------------------------------------------

    private void minimiseComposition() {

        clearOutput();
        if (compileIfChange() && oCurrent != null) {
            if (oCurrent.getComposition() == null)
                oCurrent.compose(this);
            oCurrent.minimise(this);
            postState(oCurrent);
        }
    }

    //------------------------------------------------------------------------

    private void doComposition() {

        clearOutput();
        if (compileIfChange() && oCurrent != null) {
            oCurrent.compose(this);
            postState(oCurrent);
        }
    }

    // >>> seb

    public void compileNoClear() {

        if (!parse())
            return;
        try {
            oCurrent = oCompileManager.compile( (String) oTargetChoice.getSelectedItem() );
        } catch ( LTSException pEx ) {
            displayError( pEx );
        }
        if (oCurrent != null) {
            postState(oCurrent);
        }
    }

    // <<<

    //------------------------------------------------------------------------
    private boolean checkReplay(IAnimator a) {

        if (a.hasErrorTrace()) {
            int result = JOptionPane.showConfirmDialog(this, "Do you want to replay the error trace?");
            if (result == JOptionPane.YES_OPTION) {
                return true;
            } else if (result == JOptionPane.NO_OPTION)
                return false;
            else if (result == JOptionPane.CANCEL_OPTION)
                return false;
        }
        return false;
    }

    private void execute() {

        clearOutput();
        compileIfChange();
        boolean replay;
        if (oCurrent != null) {
            //            Analyser a =
            // Analyser.getInstance(current,this,eman,setProbabilistic.isSelected());

            IAnimator a = oAnalysisFactory.createAnimator(oCurrent, this, eman);

            replay = checkReplay(a);
            if (oAnimator != null) {
                oAnimator.dispose();
                oAnimator = null;
            }
            RunMenu r = null;
            if (RunMenu.sMenus != null)
                r = (RunMenu) RunMenu.sMenus.get(run_menu);
            if (r != null && r.isCustom()) {
      
                oAnimator = createCustom(a, r.getParams(), r.getActions(), r.getControls(), replay);
            } else {
                oAnimator = new AnimWindow(a, r, setAutoRun.getState(), replay);
            }
            if (oAnimator != null) {
            
                oAnimator.pack();
                left(oAnimator);
                oAnimator.setVisible(true);
            } 
        }
    }

    private JFrame createCustom(IAnimator anim, String params, Relation actions, Relation controls, boolean replay) {

        ILTSAPlugin x_scenebeans = oPlugins.get("SceneBeans");

        if (x_scenebeans != null) {

            try {
                
                oScenebeansInit.initialise(new Object[] { anim, params, actions, controls, Boolean.valueOf(replay), oCurrentDir });
               
                return (JFrame) (x_scenebeans.getComponent());

            } catch (Exception e) {
                outln( e + e.getMessage());
                e.printStackTrace();
            }
        }
        outln("** Failed to create instance of Scene Animator ");
        return null;
    }

    /*
     * private Frame createCustom(Animator anim, String params, Relation
     * actions, Relation controls, boolean replay ) { CustomAnimator window =
     * null; try { window = new SceneAnimator(); File f; if (params!=null) f =
     * new File(currentDirectory,params); else f = null;
     * window.init(anim,f,actions,controls,replay); //give it the Animator
     * interface return window; } catch (Exception e) { outln("** Failed to
     * create instance of Scene Animator"+e); return null; } }
     */

    //------------------------------------------------------------------------
    private void reachable() {

        clearOutput();
        compileIfChange();
        if (oCurrent != null && oCurrent.getMachines().size() > 0) {
            IAutomata a = oAnalysisFactory.createAutomata(oCurrent, this, null);
            ISuperTrace xSuper = AnalysisFactory.createSuperTrace();
            xSuper.setAnalyser( a );
            xSuper.setOutput(this);
            oCurrent.setErrorTrace(xSuper.getErrorTrace());
        }
    }

    //------------------------------------------------------------------------

    private void newDrawWindow(boolean disp) {

        if (disp && oTabs.indexOfTab("Draw") < 0) {
            // create Text window
            oDrawWnd = new LTSDrawWindow(oCurrent, eman, oAnalysisFactory);
            oTabs.addTab("Draw", oDrawWnd);
            oTabs.setBackgroundAt(oTabs.indexOfTab("Draw"), Color.lightGray);
            swapto(oTabs.indexOfTab("Draw"));
        } else if (!disp && oTabs.indexOfTab("Draw") > 0) {
            swapto(0);
            oTabs.removeTabAt(oTabs.indexOfTab("Draw"));
            oDrawWnd.removeClient();
            oDrawWnd = null;
        }
    }

    private void newSynthWindow(boolean disp) {

        if (disp && oTabs.indexOfTab("Compile Buffer") < 0) {

            oTabs.addTab("Synthesis", oSynthScroller);
            swapto(oTabs.indexOfTab("Synthesis"));

        } else if (!disp && oTabs.indexOfTab("Synthesis") > 0) {
            swapto(0);
            oTabs.removeTabAt(oTabs.indexOfTab("Synthesis"));
        }
    }

    //------------------------------------------------------------------------

    private void newPrintWindow(boolean disp) {

        if (disp && oTabs.indexOfTab("Transitions") < 0) {
            // create Text window
            oPrintWnd = new PrintWindow(oCurrent, eman , oAnalysisFactory);
            oTabs.addTab("Transitions", oPrintWnd);
            swapto(oTabs.indexOfTab("Transitions"));
        } else if (!disp && oTabs.indexOfTab("Transitions") > 0) {
            swapto(0);
            oTabs.removeTabAt(oTabs.indexOfTab("Transitions"));
            oPrintWnd.removeClient();
            oPrintWnd = null;
        }
    }

   //------------------------------------------------------------------------

    private void newAlphabetWindow(boolean disp) {

        if (disp && oTabs.indexOfTab("Alphabet") < 0) {
            // create Alphabet window
            oAlphabet = new AlphabetWindow(oCurrent, eman, oAnalysisFactory );
            oTabs.addTab("Alphabet", oAlphabet);
            swapto(oTabs.indexOfTab("Alphabet"));
        } else if (!disp && oTabs.indexOfTab("Alphabet") > 0) {
            swapto(0);
            oTabs.removeTabAt(oTabs.indexOfTab("Alphabet"));
            oAlphabet.removeClient();
            oAlphabet = null;
        }
    }

  

    //------------------------------------------------------------------------

    private void aboutDialog() {

        LTSASplash d = new LTSASplash(this);
        d.setVisible(true);
    }

    //------------------------------------------------------------------------

    private void blankit() {

        LTSABlanker d = new LTSABlanker(this);
        d.setVisible(true);
    }

    //------------------------------------------------------------------------

    private void setSuperTraceOption() {

        ISuperTrace xSuper = AnalysisFactory.createSuperTrace();
        
        try {
            String o = (String) JOptionPane.showInputDialog(this, "Enter Hashtable size (Kilobytes):", "Supertrace parameters", JOptionPane.PLAIN_MESSAGE,
                    null, null, "" + xSuper.getHashSize());
            if (o == null)
                return;
            xSuper.setHashSize(Integer.parseInt(o));
            o = (String) JOptionPane.showInputDialog(this, "Enter bound for search depth size:", "Supertrace parameters", JOptionPane.PLAIN_MESSAGE, null,
                    null, "" + xSuper.getDepthBound());
            if (o == null)
                return;
            xSuper.setDepthBound(Integer.parseInt(o));
        } catch (NumberFormatException e) {
        }
    }

    //------------------------------------------------------------------------

    // changed from private so can be called from plugin
    
    public boolean parse() {

        String oldChoice = (String) oTargetChoice.getSelectedItem();
                    
        oCompileManager.setCurrentDirectory( oCurrentDir );
        Hashtable cs = null;
        
        try {
             cs = oCompileManager.parse( getSources() );
        } catch ( LTSException pEx ) {
            displayError( pEx );
        }
        
        if ( cs == null ) { 
        
        	// error parsing FSP    	
        	return false;      
        }  
        
        oTargetChoice.removeAllItems();
        
        if ( cs.isEmpty() ) {
        
            oTargetChoice.addItem(DEFAULT);
            
        } else {
            
            List forSort = new ArrayList();
            forSort.addAll( cs.keySet() );

            Collections.sort(forSort);

            for (Iterator i = forSort.iterator(); i.hasNext();) {
                oTargetChoice.addItem((String) i.next());
            }
        }

        if ( oldChoice != null ) {
            if ((!oldChoice.equals(DEFAULT)) && cs.containsKey(oldChoice))
                oTargetChoice.setSelectedItem(oldChoice);
        }
        
        oCurrent = null;
        
        // deal with run menu
        check_run.removeAll();
        run_names = oAnalysisFactory.getMenuDefinition().names();
        run_enabled = oAnalysisFactory.getMenuDefinition().enabled((String) oTargetChoice.getSelectedItem());
        check_run.add(default_run);
        if (run_names != null) {
            run_items = new JMenuItem[run_names.length];
            for (int i = 0; i < run_names.length; ++i) {
                run_items[i] = new JMenuItem(run_names[i]);
                run_items[i].setEnabled(run_enabled[i]);
                run_items[i].addActionListener(new ExecuteAction(run_names[i]));
                check_run.add(run_items[i]);
            }
        }
        
        //deal with assert menu
        check_liveness.removeAll();
        assert_names = oAnalysisFactory.createAssertDefinition().getNames();
        if (assert_names != null) {
            assert_items = new JMenuItem[assert_names.length];
            for (int i = 0; i < assert_names.length; ++i) {
                assert_items[i] = new JMenuItem(assert_names[i]);
                assert_items[i].addActionListener(new LivenessAction(assert_names[i]));
                check_liveness.add(assert_items[i]);
            }
        }
        
        validate();
        return true;
    }

    //------------------------------------------------------------------------

    private void displayManual(boolean dispman) {

        if (dispman && oTabs.indexOfTab("Manual") < 0) {
            // create Manual window
            oManual = new JEditorPane();
            oManual.setEditable(false);
            oManual.addHyperlinkListener(new Hyperactive());
            JScrollPane mm = new JScrollPane(oManual, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            oTabs.addTab("Manual", mm);
            swapto(oTabs.indexOfTab("Manual"));
            URL man = this.getClass().getResource("doc/User-manual.html");
            try {
                oManual.setPage(man);
                //outln("URL: "+man);
            } catch (java.io.IOException e) {
                outln("" + e);
            }
        } else if (!dispman && oTabs.indexOfTab("Manual") > 0) {
            swapto(0);
            oTabs.removeTabAt(oTabs.indexOfTab("Manual"));
            oManual = null;
        }
    }

    class Hyperactive implements HyperlinkListener {

        public void hyperlinkUpdate(HyperlinkEvent e) {

            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                JEditorPane pane = (JEditorPane) e.getSource();
                try {
                    URL u = e.getURL();
                    //outln("URL: "+u);
                    pane.setPage(u);
                } catch (Throwable t) {
                    outln("" + e);
                }
            }
        }
    }

    public int getStatesExplored() {

        return oStatesExplored;
    }

    public void setStatesExplored(int p_exp) {

        oStatesExplored = p_exp;
    }

    public int getTransitionsTaken() {

        return oTransitionsTaken;
    }

    public void setTransitionsTaken(int p_trans) {

        oTransitionsTaken = p_trans;
    }

    //------------------------------------------------------------------------

    public static HPWindow getInstance() {

        return oHPWindow;
    }

    public static void main(String[] args) {

        HPWindow window = getInstance(); // new HPWindow(null);
        window.setTitle("LTS Analyser");
        window.pack();
   
        HPWindow.centre(window);
        window.setVisible(true);
        if (args.length > 0) {
            SwingUtilities.invokeLater(new ScheduleOpenFile(window, args[0]));
        } else {
            window.oCurrentDir = System.getProperty("user.dir");
        }
    }

    
    /***************************************************************************
     * Get methods for stuff used by plugins *
     **************************************************************************/

    public String getTransitions(String p_machine) {

        if (oCurrent.getComposition() != null && oCurrent.getComposition().getName().equals(p_machine)) {

            OutputBuffer x_ob = new OutputBuffer();

            ITransitionPrinter xTransPrint = oAnalysisFactory.createTransitionPrinter(oCurrent.getComposition());
            xTransPrint.print(x_ob);

            //current.composition.printTransitions( x_ob , 400 , false );
            
            return x_ob.toString();
        }

        if (oCurrent != null && oCurrent.getMachines() != null && oCurrent.getMachines().size() > 0) {

            for (Enumeration e = oCurrent.getMachines().elements(); e.hasMoreElements();) {

                ICompactState x_cs = (ICompactState) e.nextElement();
                if (!x_cs.getName().equals(p_machine)) {
                    continue;
                }

                OutputBuffer x_ob = new OutputBuffer();

                ITransitionPrinter xTransPrint = oAnalysisFactory.createTransitionPrinter(oCurrent.getComposition());
                xTransPrint.print(x_ob);

                //		x_cs.printTransitions( x_ob , 400 , false );
                return x_ob.toString();
            }
        }

        return null;
    }

    public List<String> getAlphabet(String p_machine) {

        List<String> xAlphabet = new ArrayList<String>();
        Map<String, ICompactState> xCompactStates = new HashMap<String, ICompactState>();

        if (oCurrent != null && oCurrent.getMachines() != null && oCurrent.getMachines().size() > 0) {

            for (Enumeration e = oCurrent.getMachines().elements(); e.hasMoreElements();) {
                ICompactState x_cs = (ICompactState) e.nextElement();
                xCompactStates.put(x_cs.getName(), x_cs);
            }

            if (oCurrent.getComposition() != null) {
                xCompactStates.put(oCurrent.getComposition().getName(), oCurrent.getComposition());
            }

            if (xCompactStates.get(p_machine) != null) {

                String[] xLabels = ((IAutomata) xCompactStates.get(p_machine)).getAlphabet();

                for (int i = 0; i < xLabels.length; i++) {

                    if (!xLabels[i].equals("tau"))
                        xAlphabet.add(rewriteName(xLabels[i]));
                }
            }
        }

        return xAlphabet;
    }

    private String rewriteName(String p_name) {

        StringBuffer x_buf = new StringBuffer(p_name);

        for (int i = 0; i < x_buf.length(); i++) {

            if (x_buf.charAt(i) == '.' && Character.isDigit(x_buf.charAt(i + 1))) {

                x_buf.insert(i + 1, '[');
                x_buf.deleteCharAt(i);

                i++;
                while (i < x_buf.length() && Character.isDigit(x_buf.charAt(i)))
                    i++;

                x_buf = x_buf.insert(i, ']');
            }
        }

        return x_buf.toString();
    }

    public List<String> getLTSNames() {

        List<String> xList = new ArrayList<String>();
        
        Map xParseResults = null;
        
        try{
            xParseResults = oCompileManager.parse( getSources() );
        } catch ( LTSException pEx ) {
            displayError( pEx );
        }

        if (xParseResults != null) {

            for (Iterator i = xParseResults.keySet().iterator(); i.hasNext();) {
            	
                xList.add((String)i.next());
            }
        }
        
        return xList;
    }

    public List getAllProcessNames() {
        
        try {
            return oCompileManager.getAllProcessNames( getSources() );
        } catch ( LTSException pEx ) {
            displayError( pEx );
        }
        
        return null;
    }
    
    public String getCurrentDirectory() {

        return oCurrentDir;
    }

    public void setCurrentDirectory(String p_directory) {

        oCurrentDir = p_directory;
    }

    public UndoManager getUndoManager() {

        return undo;
    }

    public JEditorPane getInputPane() {

        return oSynthesis;
    }
    
    public PrintWindow getTransitionsPane() {
		
		return oPrintWnd;
    }

    public void setTargetChoice(String p_choice) {

        oTargetChoice.setSelectedItem(p_choice);
    }

    public boolean isCurrentStateNull() {

        return oCurrent == null;
    }

    public boolean isCurrentStateComposed() {

        return oCurrent.getComposition() != null;
    }

    public void composeCurrentState() {

        oCurrent.compose(this);
    }

    public void analyseCurrentState() {

        oCurrent.analyse(this);
    }

    public void analyseCurrentStateNoDeadlockCheck() {

        oCurrent.analyse(this, false);
    }

    public Vector<String> getCurrentStateErrorTrace() {

        List x_list = oCurrent.getErrorTrace();
        Vector<String> x_return = new Vector<String>();

        if (x_list == null) {
            return null;
        }

        for (Iterator i = x_list.iterator(); i.hasNext();) {

            x_return.add(rewriteName((String) i.next()));
        }

        return x_return;
    }

    public void postCurrentState() {

        postState(oCurrent);
    }

    public void exportGraphic(Exportable p_exp) {

        FileDialog x_fd = new FileDialog(this /* (Frame)getTopLevelAncestor() */, "Save file in:", FileDialog.SAVE);
        x_fd.setVisible(true);

        String x_file = x_fd.getFile();
        if (file != null)

            try {
                int i = x_file.indexOf('.', 0);
                x_file = x_file.substring(0, i) + "." + "pct";
                FileOutputStream x_fout = new FileOutputStream(x_fd.getDirectory() + x_file);

                // get picture
                ByteArrayOutputStream x_baos = new ByteArrayOutputStream(10000);
                java.awt.Rectangle r = new java.awt.Rectangle(0, 0, oInput.getSize().width, oInput.getSize().height);

                Gr2PICT x_pict = new Gr2PICT(x_baos, oInput.getGraphics(), r);
                p_exp.fileDraw(x_pict);
                x_pict.finalize(); // make sure pict end is written
                x_fout.write(x_baos.toByteArray());
                x_fout.flush();
                x_fout.close();
                //outln("Saved in: "+ fd.getDirectory()+file);
            } catch (IOException e) {
                System.out.println("Error saving file: " + e);
            }

        // new GraphicsExporter().export( p_exp );
    }

    public IAnimator getAnimator() {

        return oAnalysisFactory.createAnimator(oCurrent, this, eman);
    }

    public void clearInternalBuffer() {

        oInternalBuffer = new StringBuffer(); 
        oSynthesis.setText(""); 
	}

    public void appendInternalBuffer(String p_str) {

    	oInternalBuffer.append( p_str );
    	oSynthesis.setText( oInternalBuffer.toString());
    }

    public Set getAllFluentDefinitions() {

        if ( oAnalysisFactory.getPredicateDefinition().getAll() == null) {
            return new HashSet();
        }

        return new HashSet( oAnalysisFactory.getPredicateDefinition().getAll());
    }

    public void pluginAdded(ILTSAPlugin pPlugin) {

        System.out.println("adding " + pPlugin.getName() );

        pPlugin.setLTSA( this );
        pPlugin.initialise();
        
        oPlugins.put(pPlugin.getName(), pPlugin);
        oCurrentPlugins.add(pPlugin);

        if (pPlugin.addAsTab()) {

            String x_name = pPlugin.getName();
            oTabs.addTab(x_name, pPlugin.getComponent());
            oTabs.setBackgroundAt(oTabs.indexOfTab(x_name), Color.lightGray);

            JCheckBoxMenuItem x_menuitem = new JCheckBoxMenuItem(x_name);
            x_menuitem.addActionListener(new ToggleWindowAction(x_name));
            x_menuitem.setSelected(true);
            window.add(x_menuitem);
        }

        if (pPlugin.addToolbarButtons()) {

            oToolBar.addSeparator();

            List x_buttons = pPlugin.getToolbarButtons();
            for (Iterator j = x_buttons.iterator(); j.hasNext();) {

                LTSAButton x_lb = (LTSAButton) j.next();

                oToolBar.add(x_lb);
                oPluginButtons.put(x_lb, pPlugin);
            }

        }

        if (pPlugin.addMenuItems()) {

            Map xMenuItems = pPlugin.getMenuItems();
            Map<String, List> xTempMenus = new HashMap<String, List>();

            for (Iterator<String> j = xMenuItems.keySet().iterator(); j.hasNext();) {

                String xItem = j.next();
                String xName = (String)xMenuItems.get(xItem);

                if (xTempMenus.get(xName) == null) {

                    xTempMenus.put(xName, new ArrayList());
                }

                List<String> xList = xTempMenus.get(xName);
                xList.add(xItem);
            }

            for (Iterator<String> j = xTempMenus.keySet().iterator(); j.hasNext();) {

                String x_name = j.next();
                List x_list = xTempMenus.get(x_name);
                JMenu x_menu;

                if (oMenus.get(x_name) == null) {

                    x_menu = new JMenu(x_name);
                    oMenus.put(x_name, x_menu);
                    oMenuBar.add(x_menu);

                } else {

                    x_menu = oMenus.get(x_name);
                    x_menu.addSeparator();
                }

                for (Iterator k = x_list.iterator(); k.hasNext();) {

                    x_menu.add((JMenuItem) k.next());
                }
            }
        }

        pack();
    }

    public void pluginAdded(EventClient pEc) {

        oEventListeners.add(pEc);
        eman.addClient(pEc);
    }

    public void pluginAdded(Initialisable pInit) {

        oScenebeansInit = pInit;
    }
    
    public void pluginAdded(IAnalysisFactory pAnalFact) {
        
        System.out.println("AnaylsisFactory connected to HPWindow");
        oAnalysisFactory = pAnalFact;
    }

}