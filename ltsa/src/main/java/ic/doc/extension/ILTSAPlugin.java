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

import java.awt.Component;
import java.util.List;
import java.util.Map;
import java.io.File;
import java.io.FileOutputStream;

/**
 * This is the required interface exposed by the LTSA core for all plugins.
 * One class within each plugin component must implement this interface in
 * order to be connected to the LTSA core.
 * 
 * The normal way to create a class that implements this interface is to
 * extend the LTSAPlugin class.
 * 
 * @author rbc
 * @see ic.doc.extension.LTSAPlugin;
 */
public interface ILTSAPlugin {

    public void setLTSA( LTSA p_ltsa );

    public LTSA getLTSA();
        
    public String getName();

    public boolean addAsTab();

    public Component getComponent();

    public boolean addToolbarButtons();

    public List getToolbarButtons();

    public boolean providesOpenFile();

    public void    openFile( File p_file );
 
    public boolean providesSaveFile();

    public void    saveFile( FileOutputStream p_fos );

    public boolean providesNewFile();
 
    public void    newFile();

    public boolean providesCopy();

    public void    copy();

    public boolean providesCut();

    public void    cut();

    public boolean providesPaste();

    public void    paste();

    public String  getFileExtension();

    public boolean addMenuItems();

    public Map     getMenuItems();

    public void selected();

    public void setBigFont( boolean p_big );

    public void initialise();
}	
