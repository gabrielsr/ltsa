package LTS;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import java.util.*;

import ic.doc.extension.*;
import ic.doc.ltsa.common.iface.ICompactState;
import ic.doc.ltsa.common.iface.ICompositeState;
import ic.doc.ltsa.common.iface.LTSOutput;
import ic.doc.ltsa.common.infra.EventClient;
import ic.doc.ltsa.common.infra.LTSEvent;
import ic.doc.ltsa.lts.PrintTransitions;

/**
 * @author Gena’na Rodrigues <g.rodrigues@cs.ucl.ac.uk>, Jonas Wolf <wolfj@inf.ethz.ch>
 * 
 * This is a plugin for LTSA to analyse reliability properties of the
 * architectural model It adds a new tab, a button and a menu item. Clicking
 * either the button or the menu item causes the names of the currently
 * available LTSs to be displayed in the new tab.
 */

public class ReliabilityPlugin extends LTSAPlugin implements ActionListener, ListSelectionListener, EventClient
{

    private JTextArea oTextArea;
    
    private JTextField oTextFieldRghtBtm;
    
    TransitionParser tp;

    private JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

	private JSplitPane splitPaneRght = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
	
	private JList list;

    private ICompactState[] sm;

    private int Nmach;

    public ReliabilityPlugin()
    {
    }

    public ReliabilityPlugin(LTSA pLtsa)
    {
        super(pLtsa);
    }
    
    public String getName()
    {
        return "Reliability Analysis";
    }

    public boolean addAsTab()
    {
        return true;
    }

    public Component getComponent()
    {
        return splitPane;
    }

    public boolean addToolbarButtons()
    {
        return true;
    }

    public List getToolbarButtons()
    {
        List<LTSAButton> xButtonList = new ArrayList<LTSAButton>();
        ImageIcon xIcon = new ImageIcon(this.getClass().getResource(
                "/icons/reliability.gif"));
        LTSAButton xButton = new LTSAButton(xIcon,
                "Compute Reliability", this);
        xButton.setActionCommand("computereliability");

        xButtonList.add(xButton);

        return xButtonList;
    }

    public boolean addMenuItems()
    {
        return true;
    }

    public Map getMenuItems()
    {

        Map<JMenuItem, String> xItems = new HashMap<JMenuItem, String>();

        JMenuItem xItem = new JMenuItem("Compute Reliability");
        xItem.setActionCommand("computereliability");
        xItem.addActionListener(this);

        // this specifies that the new item should be added to the Build menu
        xItems.put(xItem, "Build");

        return xItems;
    }
    
    @SuppressWarnings("unchecked")
    public void initialise()
    {
        // register for updates with EventManager
        getLTSA().pluginAdded(this);
        
        // moved GUI creation, is now invoked through EventClient interface
    }

    /**
     * create the interface taking the current machine
     */
    private void createGUI(ICompositeState cs)
    {
        // initalise our text area
        oTextArea = new JTextArea();

/*
        LTSA xLTSA = getLTSA();
        // first get all simple processes
        List<String> xLTSs = xLTSA.getAllProcessNames();
        // now add composite processes
        Iterator it = xLTSA.getLTSNames().iterator();
        while (it.hasNext())
        {
            xLTSs.add("||"+(String)it.next());
        }
*/
        
        List<String> xLTSs = new ArrayList<String>();
        sm = null;
        Nmach = 0;
        boolean hasC = cs != null && cs.getComposition() != null;
        if (cs != null && cs.getMachines() != null
                && cs.getMachines().size() > 0)
        {
            // get set of machines
            sm = new ICompactState[cs.getMachines().size() + (hasC ? 1 : 0)];
            Enumeration e = cs.getMachines().elements();
            for (int i = 0; e.hasMoreElements(); i++)
                sm[i] = (ICompactState) e.nextElement();
            Nmach = sm.length;
            if (hasC)
                sm[Nmach - 1] = cs.getComposition();
        }
        else
            Nmach = 0;
        for (int i = 0; i < Nmach; i++)
        {
            if (hasC && i == (Nmach - 1))
                xLTSs.add("||" + sm[i].getName());
            else
                xLTSs.add(sm[i].getName());
        }
        
        JScrollPane right = new JScrollPane(oTextArea,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 
        Font f1 = new Font("Monospaced",Font.PLAIN,12);
        Font f3 = new Font("SansSerif",Font.PLAIN,12);
         
        oTextArea.setBackground(Color.white);
        oTextArea.setBorder(new EmptyBorder(0, 5, 0, 0));
        oTextArea.setFont(f1);

        list = new JList(xLTSs.toArray());
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.addListSelectionListener(this);
        JScrollPane left = new JScrollPane(list,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        oTextFieldRghtBtm = new JTextField();
        oTextFieldRghtBtm.setBackground(Color.white);
        oTextFieldRghtBtm.setEditable(false);
        oTextFieldRghtBtm.setFont(f3);
        //oTextArea.setBorder(new EmptyBorder(0, 3, 0, 0));

        
        splitPaneRght.setTopComponent(right);
        splitPaneRght.setBottomComponent(oTextFieldRghtBtm);
        splitPaneRght.setDividerLocation(500);
        splitPane.setLeftComponent(left);
        splitPane.setRightComponent(splitPaneRght);
        splitPane.setDividerLocation(200);
        splitPane.validate();
        splitPane.repaint();
    }

    public void actionPerformed(ActionEvent e)
    {
        final String actionCommand = e.getActionCommand();
        
        if (actionCommand.equals("computereliability"))
        {
            // so from here we get the output of the Transitions Window
            // and calculate the reliablity

            String outlts = oTextArea.getText();
            if (!outlts.equals(""))
            {
                outlts += "EOF";
                tp = new TransitionParser();
                double result = tp.callParser(outlts) * 100;
                oTextFieldRghtBtm.setText("The reliability of the "+(String)list.getSelectedValue()+" is: " + result + "%");
            }
            splitPane.validate();
        }
        else
        {
            throw new RuntimeException("unknown action command "+actionCommand);
        }
    }

    public void valueChanged(ListSelectionEvent e)
    {
        JList list = (JList)e.getSource();
        if (e.getValueIsAdjusting() == false)
        {
            if (list.getSelectedIndex() == -1)
            {
                oTextArea.setText("");
            }
            else
            {
                oTextArea.setText("");
/*
                String lts = (String) list.getSelectedValue();
                oTextArea.setText(getLTSA().getTransitions(
                        lts.startsWith("||") ? lts.substring(2) : lts));
*/
                (new PrintTransitions(sm[list.getSelectedIndex()]))
                        .print(new LTSOutput()
                        {
                            public void clearOutput()
                            {
                                oTextArea.setText("");
                            }

                            public void out(String s)
                            {
                                oTextArea.setText(oTextArea.getText() + s);
                            }

                            public void outln(String s)
                            {
                                oTextArea.setText(oTextArea.getText() + s
                                        + "\n");
                            }
                        });

                splitPane.validate();
            }
        }
    }
    
    @Override
    public void selected()
    {
    }

    /**
     * EventClient implementation
     * */
    public void ltsAction(LTSEvent e)
    {
        switch (e.kind)
        {
            case LTSEvent.NEWSTATE:
                break;
            case LTSEvent.INVALID:
                createGUI((ICompositeState)e.info);
                break;
            case LTSEvent.KILL:
                // this.dispose();
                break;
            default:
                ;
        }
    }
    
}
