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
import javax.swing.JMenuBar;

import java.util.List;
import java.util.Map;
import java.io.File;
import java.io.FileOutputStream;

/**
 * @author Robert Chatley - rbc@doc.ic.ac.uk
 *
 * LTSA supports an extension mechanism via plugins.
 * This is the abstract superclass of all plugins to LTSA. To make
 * a plugin which will work with LTSA, extend this class and implement
 * at least the abstract methods. LTSA's dynamic extension mechanism will
 * then be able to load and work with your plugin.
 */
public abstract class LTSAPlugin implements ILTSAPlugin {

    private LTSA o_ltsa;

    /**
     * @deprecated This method should no longer be used, two way bindings
     * between plugins and the core are formed by the plugin mechanism
     */    
    public final void setLTSA( LTSA p_ltsa ) { o_ltsa = p_ltsa; }

    /**
     * @return a reference to the LTSA interface, enabling core LTSA functionality to be accessed from the plugin. 
     **/
    public final LTSA getLTSA() { return o_ltsa; } 
    
    /**
     * Default constructor
     **/
    public LTSAPlugin(){
    
        com.chatley.magicbeans.PluginManager.getInstance().addBackDatedObserver( this );
    }
    
    /**
     * This constructor is called by the extension mechanism. You should not call it explicitly (through super(..) or otherwise).
     **/
    public LTSAPlugin( LTSA p_ltsa ) { o_ltsa = p_ltsa; initialise(); }
    
    /**
     * A name needs to be given to a plugin so that it can be identified.
     * If the plugin adds a tab to the LTSA user interface, this name will be used as the label.
     *
     * @return returns a name for the plugin.
     **/
    public abstract String getName();

    /**
     * @return true if this plugin provides a component that should be added as a tab in user interface.
     */
    public abstract boolean addAsTab();

    /**
     * @return a reference to a java.awt.Component (all swing JComponents are a subclass of awt.Component, and so can be used 
     * here) which should be added to the main UI.
     **/
    public Component getComponent() { return null; }

    /**
     * @return true if the plugin provides any buttons to be added to the LTSA toolbar.
     **/
    public abstract boolean addToolbarButtons();

    /**
     * Buttons for the LTSA toolbar together with their associated actions can be created using the
     * LTSAButton class. This method should return a java.util.List of the buttons to be added.
     * Buttons are only added when LTSA loads the plugin, so changing the contents of this list at
     * a later time will have no effect on the toolbar.
     *
     * @return a List of LTSAButtons to be added to the toolbar. 
     * @see LTSAButton
     **/
    public List getToolbarButtons() { return null; }

    /**
     * You can write a handler to be triggered when the File Open button or menu item is clicked in 
     * the LTSA GUI. If the tab for your plugin is currently selected when File Open is performed,
     * and you provide a handler for this event, your routine will be called. In order to signal that
     * you provide a handler, override the providesOpenFile() method to return true.
     *
     * @return true if you want to catch File Open commands
     **/
    public boolean providesOpenFile() { return false; }

    /**
     * If you have overridden providesOpenFile() to return true, this method will be called when
     * a file is opened using the filechooser in the LTSA GUI. A File object representing the file
     * is passed to this method. Implement your own file reading routine here.
     **/
    public void    openFile( File p_file ) {}
 
    /**
     * You can write a handler to be triggered when the File Save button or menu item is clicked in 
     * the LTSA GUI. If the tab for your plugin is currently selected when File Save is performed,
     * and you provide a handler for this event, your routine will be called. In order to signal that
     * you provide a handler, override the providesSaveFile() method to return true.
     *
     * @return true if you want to catch File Save commands
     **/
    public boolean providesSaveFile() { return false; }

    /**
     * If you have overridden providesSaveFile() to return true, this method will be called when
     * a file is saved using the filechooser in the LTSA GUI. A FileOutputStream object connected
     * to the file is passed to this method. Implement your own file saving routine here, writing
     * the output to the stream.
     **/
    public void    saveFile( FileOutputStream p_fos ) {}

   /**
     * You can write a handler to be triggered when the New File button or menu item is clicked in 
     * the LTSA GUI. If the tab for your plugin is currently selected when New is performed,
     * and you provide a handler for this event, your routine will be called. In order to signal that
     * you provide a handler, override the providesNewFile() method to return true.
     *
     * @return true if you want to catch New File commands
     **/
    public boolean providesNewFile() { return false; }
 
    /**
     * If you have overridden providesNewFile() to return true, this method will be called when
     * New File is selected in the LTSA GUI. Implement your own new file routine here.
     * the output to the stream.
     **/
    public void    newFile() {}

    /**
     * As with Open, Save and New, you can provide handlers for Cut, Copy and Paste to be triggered
     * by the corresponding buttons on the LTSA toolbar. To provide a copy handler, override providesCopy()
     * so that it returns true, and implement the handler by overriding the copy() method.
     **/
    public boolean providesCopy() { return false; }

    /**
     * Override this method to implement a copy handler for your plugin.
     **/
    public void    copy() {}

   /**
     * As with Open, Save and New, you can provide handlers for Cut, Copy and Paste to be triggered
     * by the corresponding buttons on the LTSA toolbar. To provide a cut handler, override providesCut()
     * so that it returns true, and implement the handler by overriding the cut() method.
     **/
    public boolean providesCut() { return false; }

    /**
     * Override this method to implement a cut handler for your plugin.
     **/
    public void    cut() {}

   /**
     * As with Open, Save and New, you can provide handlers for Cut, Copy and Paste to be triggered
     * by the corresponding buttons on the LTSA toolbar. To provide a paste handler, override providesPaste()
     * so that it returns true, and implement the handler by overriding the paste() method.
     **/
    public boolean providesPaste() { return false; }

    /**
     * Override this method to implement a paste handler for your plugin.
     **/
    public void    paste() {}

    /**
     * If you want to define a particular file extension to look for when loading and saving files from your
     * plugin, return that extension from this method, and it will be used in the filechooser.
     * For example if you want to load files with the extension .xml, return "xml" (no dot) from this method.
     **/
    public String  getFileExtension() { return null; }

    /**
     * You can add menu items to menus that already exist in LTSA, or create entire new menus. 
     * To signal that your plugin provides menu items to add, override addMenuItems() to return true,
     * and implement the getMenuItems() method.
     **/
    public boolean addMenuItems() { return false; }

    /**
     * This method should return a Map of menu items to add to the LTSA menus. Each key-value pair in the map 
     * should comprise a JMenuItem (to which you should attach a suitable event listener) as the key, and a 
     * String giving the name of the menu that this menu item should be in as the value. 
     * In this way you can define a number of different menu items that are all to be added to
     * the same menu. If a menu with the given name does not already exist, then a new menu will be created
     * with this name. In this way you can add entire menus.
     *
     **/
    public Map     getMenuItems() { return null; }

    public void selected() {}

    public void setBigFont( boolean p_big ) {}

    /**
     * Because the constructor of this abstract class will be called by the extension mechanism, other
     * constructors defined in subclasses will not be called. Override this method to do any initialisation.
     *
     **/
    public void initialise() {}


 

    /**
     * @deprecated
     * <b>The option to add entire menus has been discontinued - use addMenuItems() instead.</b>
     * It is possible to add new menus to the existing LSTA menubar, implementing the menu in the plugin.
     *
     * @return true if the plugin provides menus to add to the exising menubar.
     **/
    public boolean addMenusToMenuBar() { return false; }


    /**
     * @deprecated
     * <b>The option to add entire menus has been discontinued - use addMenuItems() instead.</b>
     * To add menus to the existing menubar, override this method to return a java.util.List of JMenus.
     * The menus should be implemented inside the plugin and will be hooked onto the existing menubar.
     * Menus are only added when LTSA loads the plugin, so changing the contents of this list at
     * a later time will have no effect on the menubar. This method will not be called unless addMenusToMenuBar() 
     * returns true.
     *
     * @return a List of JMenus to add to the exisiting LTSA menubar.
     * @see addMenusToMenuBar()
     * @see JMenu
     **/
    public List getMenus() { return null; }

    /**
     * @deprecated
     * <b>The option to provide your own menubar has been discontinued - use addMenuItems() instead.</b>
     * It is possible for a plugin to provide its own menu bar. The menubar should be implemented inside the
     * plugin. When the tab corresponding to the plugin is selected in the main LTSA user interface, the menubar
     * will switch to that provided by that plugin.
     *
     * @return true if the plugin provides its own menubar
    **/
    public boolean useOwnMenuBar() { return false; }
    
    /**
     * @deprecated
     * <b>The option to provide your own menubar has been discontinued - use addMenuItems() instead.</b>
     * To make LTSA switch to a new menubar when the pluign is selected, override this method to return a
     * fully constructed JMenuBar.
     *
     * @return a JMenuBar corresponding to this plugin
     **/
    public JMenuBar getMenuBar() { return null; }
}	
