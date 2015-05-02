package darwin;

public interface FSPSource {

    public String getFSPforComponent( String p_name , java.util.Map p_mapping );
    
    public java.util.Set getMessageLabels( String p_name );
}
