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

import javax.swing.JButton;
import javax.swing.ImageIcon;
import java.awt.event.ActionListener;
import java.awt.Insets;

/**
 * @author Robert Chatley - rbc@doc.ic.ac.uk
 *
 * This class wraps javax.swing.JButton to provide a more convenient way of constructing buttons.
 * All buttons provided by LTSA plugins should use LTSAButtons so that the extension mechanism can
 * dynamically add them to the LTSA toolbar.
 **/
public class LTSAButton extends JButton {

    /**
     * The constructor takes and icon, some tooltip text and an ActionListener to respond to events
     * triggered by this button being pressed.
     **/
    public LTSAButton(ImageIcon p_icon, String p_tooltip, ActionListener p_actionlistener) {

        setIcon(p_icon);
        setRequestFocusEnabled(false);
        setMargin(new Insets(0, 0, 0, 0));
        setToolTipText(p_tooltip);
        addActionListener(p_actionlistener);
    }
    
    /**
     * This method overrides a method in JButton to specify alignment.
     **/
    public float getAlignmentY() { return 0.5f; }
}
