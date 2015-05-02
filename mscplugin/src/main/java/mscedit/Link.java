package mscedit;

public class Link implements BMSCComponent {

    private String o_from;
    private String o_to;
    private String o_name;
    private int o_timeindex;
    private boolean o_negative = false;
    
    /** The weight of the message link*/
    private double o_weight = 0;

    public Link() {}

    public Link( String p_from , String p_to , String p_name , int p_timeindex ) {

	o_from = p_from;
	o_to = p_to;
	o_name = p_name;
	o_timeindex = p_timeindex;
    }
    
    public String getFrom() { return o_from; }
    public void setFrom( String p_from ) { o_from = p_from; }
    public String getTo() { return o_to; }
    public void setTo( String p_to ) { o_to = p_to; }
    public String getName() { return o_name; }
    public void setName( String p_name ) { o_name = p_name; }
    public String getId() { return o_name + o_timeindex; }
    public int getTimeIndex() { return o_timeindex; }
    public void setTimeIndex( int p_time ) { o_timeindex = p_time; }

    public Link setNegative() {

	o_negative = true;
	return this;
    }

    public boolean isNegative() { return o_negative; }

    public boolean isSelfTransition() { return o_from.equals( o_to ); }

    public String toString() {
	return "[Link] " + o_name + " links " + o_from + " to " + o_to + " with weight " + o_weight + ".";
    }
    
    /**
     * set the weight of the transition
     * @param the weight
     * */
    public void setWeight(double weight)
    {
        o_weight  = weight;
    }
    
    /**
     * get the weight
     * @return the weight
     * */
    public double getWeight()
    {
        return o_weight;
    }
    
    /**
     * @return true iff the link has a weight
     * */
    public boolean hasWeight()
    {
        return o_weight != 0;
    }
}

