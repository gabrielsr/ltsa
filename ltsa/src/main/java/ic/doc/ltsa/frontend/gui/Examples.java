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

package ic.doc.ltsa.frontend.gui;

/*
 * Creates the Example menu
 */
import ic.doc.ltsa.frontend.HPWindow;

import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.util.*;
import java.util.jar.JarFile;

public class Examples {

    JMenu parent;
    HPWindow out;
    JarFile o_ex;

    public Examples(JMenu parent, HPWindow out) {

        this.parent = parent;
        this.out = out;

        try {
            o_ex = new JarFile("examples.jar");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getExamples() {

        List<String> chapters = getContents("examples/book/contents.txt");
        Iterator<String> i = chapters.iterator();
        while (i.hasNext()) {
            String s = i.next();
            JMenu chapter = new JMenu(s.substring(0, s.indexOf('_')));
            parent.add(chapter);
            List<String> examples = getContents("examples/book/" + s + "/contents.txt");
            Iterator<String> j = examples.iterator();
            while (j.hasNext()) {
                String es = j.next();
                int dot = es.indexOf('.');
                String exs = dot > 0 ? es.substring(0, dot) : es;
                JMenuItem example = new JMenuItem(exs);
                example.addActionListener(new ExampleAction("examples/book/" + s + "/", es));
                chapter.add(example);
            }
        }
    }

    private List<String> getContents(String resource) {

        List<String> xContents = new ArrayList<String>(16);
		InputStream xFileInput = null;
		
        try {
           
            xFileInput = o_ex.getInputStream( o_ex.getEntry(resource) );

            BufferedReader myInput = new BufferedReader(new InputStreamReader(xFileInput));
            String thisLine;
            while ((thisLine = myInput.readLine()) != null) {
                if (!thisLine.equals(""))
                    xContents.add(thisLine);
            }
			
        } catch (Exception e) {
            e.printStackTrace();
            out.outln("Error getting resource: " + resource);
        } finally {
			
			try {
				xFileInput.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
		
        return xContents;
    }

    class ExampleAction implements ActionListener {
        String dir, ex;

        ExampleAction(String dir, String ex) {

            this.dir = dir;
            this.ex = ex;
        }

        public void actionPerformed(ActionEvent e) {

            out.newExample(dir, ex);
        }
    }

}

