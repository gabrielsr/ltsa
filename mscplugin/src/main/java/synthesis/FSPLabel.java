package synthesis;

import java.util.*;


class FSPLabel {
	private String s;

	public FSPLabel() {

		s = new String("");

	}

	public void setMessageLabel (String label, Map PortNames, String instance) throws Exception  {


		//System.out.println("FSPLabel.setMessageLabel: called!");
		s = label.replace(',', '.');
		//System.out.println("FSPLabel.setMessageLabel: s = "+s);
		if (PortNames != null) {
			Map M = (Map) PortNames.get(label);
			if (M !=null) {
				String ret = (String) M.get(instance);
				//System.out.println("FSPLabel.setMessageLabel: ret= "+ret);
				if (ret !=null)
					s = new String(ret);
			}
		}

/*		else {
			int FirstDot = label.indexOf('.');
			int SecondDot = label.indexOf('.', FirstDot+1);

			if (label.lastIndexOf('.') != SecondDot || FirstDot < 0 || SecondDot < 0)
				if (!label.startsWith("s_"))
					throw new Exception("Invalid message label format," + label);

			s = new String(label.replace('.', '_'));
		}
		*/
	}

	public void setComponentLabel (String label) throws Exception  {
		int FirstColon = label.indexOf(':');
		//System.out.println("FSPLabel.setComponentLabel: label = "+label);
		if (label.lastIndexOf(':') != FirstColon)
			throw new Exception("Invalid component label format," + label + ": First colon: " + FirstColon + ", lastColon: " + label.lastIndexOf(':'));
		s = new String(label.replace('.', '_'));
		s = new String(s.replace(':', '_'));
		s = new String(s.substring(0,1).toUpperCase() + s.substring(1));
	}

	public String getLabel() {
		return s;
	}

}
