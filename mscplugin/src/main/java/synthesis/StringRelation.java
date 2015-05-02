package synthesis;

class StringRelation {
	StringMap H;
	
	public StringRelation () {
		H = new StringMap();
	}
	
	public void add(String s, String t) {
		StringSet S;
		if (!H.containsKey(s)) {
			S = new StringSet();
			H.put(s, S);
		}
		else
			S = (StringSet) H.get(s);
		S.add(t);
	}
	
	public void print(MyOutput Output) {
		StringIterator I = H.keyStringSet().stringIterator();
		StringIterator J;
		String Aux;
		
		while (I.hasNext()) {
			Aux = I.nextString();
			Output.print(Aux + " = {");
			J = ((StringSet) H.get(Aux)).stringIterator();
			while(J.hasNext()) {
				Output.print(J.nextString() + ",");
			}
			Output.println("}");
		}
	}

	public Object clone() {
		StringIterator It;
		StringRelation R = new StringRelation();
		String k;
		
		It = H.keyStringSet().stringIterator();
		while (It.hasNext()) {
			k = It.nextString();
			R.H.put(k, ((StringSet)((StringSet) H.get(k)).clone()));
		}		
		return R;
	}
	
	public void transitiveClosure() {
		while (transitiveClosureOneStep());
	}
		
	private boolean transitiveClosureOneStep() {
		String key;
		String aux;
		StringSet Saux;
		
		StringIterator I = H.keyStringSet().stringIterator();
		while (I.hasNext()) {
			key = I.nextString();
			StringIterator J = ((StringSet) H.get(key)).stringIterator();
			
			while (J.hasNext()) {
				aux = J.nextString();
				if (H.containsKey(aux)) {
					StringIterator K = ((StringSet)H.get(aux)).stringIterator();
					while (K.hasNext()) {
						aux = K.nextString();
						if (!((StringSet) H.get(key)).contains(aux)) {
							((StringSet)H.get(key)).add(aux);
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	
	public StringSet getImage(String s) {
		if (H.keyStringSet().contains(s)) 
			return (StringSet) H.get(s);
		else
			return new StringSet();
	}
	
	public StringSet domain() {
		return H.keyStringSet();
	}
	
	
}