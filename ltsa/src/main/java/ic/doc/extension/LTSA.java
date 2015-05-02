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

package ic.doc.extension;

import javax.swing.JEditorPane;
import javax.swing.undo.UndoManager;
import java.util.Vector;
import java.util.List;
import java.util.Set;

import ic.doc.ltsa.common.iface.LTSOutput;
import ic.doc.ltsa.common.infra.EventClient;
import ic.doc.ltsa.frontend.gui.PrintWindow;

/**
 * @author Robert Chatley - rbc@doc.ic.ac.uk
 *
 * This interface provides a way of accessing the core functionality of LTSA from within
 * a plugin. A reference to this interface can be acquired by calling getLTSA() in any 
 * subclass of LTSAPlugin. 
 * 
 * @see ic.doc.extension.LTSAPlugin;
 **/

public interface LTSA extends LTSOutput {

    public void compileNoClear();

    public UndoManager getUndoManager();

    public JEditorPane getInputPane();

    public void setTargetChoice( String p_choice ); 

    public void showOutput();

    public boolean parse();

    public void invalidateState();

    public void updateDoState();

    public boolean isCurrentStateNull();

    public boolean isCurrentStateComposed();

    public void composeCurrentState(); 

    public void analyseCurrentState();

    public void analyseCurrentStateNoDeadlockCheck();

    public Vector getCurrentStateErrorTrace(); 

    public void postCurrentState();

    public void swapto( String p_tab );

    public void exportGraphic( Exportable p_exp );

    public IAnimator getAnimator();

    public String getCurrentDirectory();

    public void setCurrentDirectory( String p_directory );

    public List getLTSNames();

    public List getAlphabet( String p_machine );

    public List getAllProcessNames();
    
    public PrintWindow getTransitionsPane();
    
    //  public StochasticAutomata getStochasticAutomata();

    public int getStatesExplored();

    public int getTransitionsTaken();

    public String getTransitions( String p_machine );

    public void clearInternalBuffer();

    public void appendInternalBuffer( String p_str );

    public Set getAllFluentDefinitions();
    
    /**
     * expose the pluginAdded method so that plugins can register themselves
     * as an EventClient 
     * */
    public void pluginAdded(EventClient pEc);
    
}
