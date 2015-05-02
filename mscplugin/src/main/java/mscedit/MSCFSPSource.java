package mscedit;

public class MSCFSPSource implements darwin.FSPSource {

    public String getFSPforComponent(String p_name, java.util.Map p_mapping) {

        return MSCPlugin.getInstance().getFSPforComponent(p_name, p_mapping);
    }

    public java.util.Set getMessageLabels(String p_name) {

        return MSCPlugin.getInstance().getMessageLabels(p_name);
    }
}