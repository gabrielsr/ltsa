package synthesis;

import java.io.*;
import java.util.Vector;
import java.util.ArrayList;
import java.util.Map;
import ic.doc.ltsa.common.iface.LTSOutput;

public class Node {
	public int Id;
	private ArrayList History;
	private Vector Positions;
	private int first;
	private int size;
	private Vector bMSCPositions;
	
	private Node() {
	}

	private void setLocation(int c, BasicMSC b)  {
		bMSCPositions.set(c, b);
	}
	
	private void  setPosition(int c, int p)  {
		Positions.set(c, new Integer(p));
	}
	
	public BasicMSC getLocation(int c)  {
		return (BasicMSC) bMSCPositions.get(c);
	}
	
	private int getPosition(int c)  {
		return ((Integer) Positions.get(c)).intValue();
	}

	public int SizeOfDestiny()  {
		return 1+first;
	}

	public Node(int s, BasicMSC Init)  {
		size = s;
		History = new ArrayList();
		Positions = new Vector(size);
		bMSCPositions = new Vector(size);
		first = 0;
		History.add(first, Init);
		for (int i=0; i<size; i++)  {
			Positions.add(i, new Integer(0));
			bMSCPositions.add(i, Init);
		}
	}

	public void print (LTSOutput o)  {
		String aux="";
		String line = "Node : " + Id + " History : ";		
		for (int i = 0; i<History.size();i++)
			line = line + ((BasicMSC) History.get(i)).name + ", ";
		o.outln(line);
		line = "";
		for (int i = 0; i<size;i++)
			line = line + getPosition(i) + ", ";
		o.outln(line);
		line = "";
		for (int i = 0; i<size;i++)
			line = line + getLocation(i).name + ", ";
		o.outln(line);
	}

	public boolean  Move(int c, Map Components, PrintStream BenchmarkOutput, LTSOutput o)  {
		boolean dbg = false;
		boolean retVal = false;
		
		int myPos = getPosition(c);
		BasicMSC NewLocation = (BasicMSC) History.get(myPos+1);
		
		if (isFirst(c))
			throw new Error();
		
		if (dbg) o.outln("Checking if valid");	
		retVal = validMove(c, NewLocation, Components, BenchmarkOutput, o);

		
		if (dbg) o.outln("Valid");	
		boolean found0 = false;
		if (myPos == 0)  {
			for (int i=0; i<size && !found0; i++) 
				if (i != c) 
					found0 = (getPosition(i)==0);			
		}
		

		if (myPos == 0 && !found0)  {
			//Apply T3
			for (int i=0;i<size;i++)  {
				if (i != c)  {
					setPosition(i, getPosition(i)-1);
				}
			}
			History.remove(0);							
			setLocation(c, (BasicMSC) History.get(0));
			first--;
		}
		else  {
			//Apply T2
			myPos++;
			setPosition(c, myPos);
			setLocation(c, NewLocation);
		}
		
		
		return retVal;
	}
	

	public boolean Move(int c, BasicMSC b, Map Components, PrintStream BenchmarkOutput, LTSOutput o)  {
		boolean dbg = false;
		boolean retVal = false;
		
		if (!isFirst(c))
			throw new Error();
		
		if (dbg) o.outln("Checking if valid " + c + " - " + b.name);	
		retVal = validMove(c, b, Components, BenchmarkOutput, o);
		
		if (dbg) o.outln("Valid");	
		first++;
		History.add(first, b);
		setPosition(c, first);
		setLocation(c, b);		
		return retVal;
	}
	
	
	public boolean isFirst(int c)  {
		return (getPosition(c) == first);
	}
	
	public Node Clone()  {
		Node N = new Node();
		N.first = this.first;
		N.size = this.size;
		N.Positions = (Vector) this.Positions.clone();
		N.bMSCPositions = (Vector) this.bMSCPositions.clone();
		N.History = (ArrayList) this.History.clone();
		return N;
	}

	private boolean equalLocations(Node N)  {
		boolean equals = true;
		for (int i = 0; i<size && equals;i++)  
			equals = (getLocation(i) == N.getLocation(i)); 
		return equals;	
	}


	private boolean equalPositions(Node N)  {
		boolean equals = true;
		for (int i = 0; i<size && equals;i++)  
			equals = (getPosition(i) == N.getPosition(i)); 
		return equals;	
	}


	private boolean equalHistory(Node N)  {
		boolean equals = true;
		if (History.size() != N.History.size())
			return false;
			
		for (int i = 0; i<History.size() && equals;i++)  
			equals = (History.get(i) == N.History.get(i)); 
		return equals;	
	}


	
	public boolean Equals(Node N)  {
		boolean equals = true;
		
		if (!equalLocations(N))
			return false;
		
		if (!equalPositions(N))
			return false;
		
		if (!equalHistory(N))
			return false;
		
		return (first == N.first && size == N.size);	
	}
	
	
	public Node eliminateLoop(LTSOutput o) {
		boolean dbg = false;
		boolean dbg2 = false;
		
		for (int p1 = 0; p1 <= first; p1++)  {
			BasicMSC b = (BasicMSC) History.get(p1);
			for (int p2 = p1+1; p2 <=first;p2++)  {
				if ((BasicMSC) History.get(p2) == b)  {
					//found loop
					boolean EmptyLoop = true;
					for(int check = 0; check<size && EmptyLoop;check++)  
						EmptyLoop = !(getPosition(check)>=p1 && getPosition(check)<p2);
						
					if (EmptyLoop)  {
						//Found a loop to eliminate
						Node Short = this.Clone();
					
						int dif = p2-p1;
						for(int i=0; i<size; i++)  {
							if (getPosition(i) >= p2)  {
								int newPos = getPosition(i)-dif;
								Short.setPosition(i, newPos);
							}
						}
										
						Short.first = first-dif;
						for (int i=0;i<dif;i++) 
							Short.History.remove(p1); 
						
						if(dbg) o.outln("Eliminated loop!!");						
						if(dbg) this.print(o);
						if(dbg)	Short.print(o);
						
						Node NewShort = Short.eliminateLoop(o);
						if (NewShort == null)
							return Short;	
						else
							return NewShort;		
					}
				}
			}
		}
		//Didnt find loops...
		return null;	
	}

	
	private boolean validMove(int c, BasicMSC b, Map Components, PrintStream BenchmarkOutput, LTSOutput o)  {
		boolean dbg = false;
		boolean benchmark = (BenchmarkOutput!=null);
		
		boolean do1 = true; //Rule 1 in paper //Rule 1 in Thesis
		boolean do2 = true; //Rule 3 in paper //Rule 2 in Thesis
		boolean do3 = false; //Rule 2 in paper THIS SEEMS TO BE WRONG. 
		boolean do4 = true;	//Rule 4 in paper //Rule 3 in Thesis
		boolean do5 = true; //not in paper
		boolean do6 = true; //not in paper

		if (benchmark) {
			boolean b1 = validMove1(c, Components, o);  
			boolean b2 = validMove2(c, Components, o);  
			boolean b3 = true; //validMove3(c, Components, o);  
			boolean b4 = validMove4(c, Components, o);  
			boolean b5 = validMove5(c, Components, o);  
			boolean b6 = validMove6(c, Components, o);  					
			BenchmarkOutput.println(b1 + "," + b2 + "," + b3 + "," + b4 + "," + b5 + "," + b5);
			return (b1 && b2 && b3 && b4 && b5 && b6);
		}
		

	
	//ordered in estimated reduction rate....
		if (!do1 || validMove1(c, Components, o)) {
			if (!do2 || validMove2(c, Components, o))  {
				if (!do3 || validMove3(c, Components, o))  {
					if (!do4 || validMove4(c, Components, o))    {
						if (!do5 || validMove5(c, Components, o))    {
							if (!do6 || validMove6(c, Components, o))  {
								return true;
							}
							else
								if (dbg) o.outln("Node " + Id + ": " + c + " - " + b.name + " violates Rule 6");	
						}
						else  
							if (dbg) o.outln("Node " + Id + ": " + c + " - " + b.name + " violates Rule 5");	
					}
					else
						if (dbg) o.outln("Node " + Id + ": " + c + " - " + b.name + " violates Rule 4");	
				}	
				else
					if (dbg) o.outln("Node " + Id + ": " + c + " - " + b.name + " violates Rule 3");	
			}
			else
				if (dbg) o.outln("Node " + Id + ": " + c + " - " + b.name + " violates Rule 2");
		}	
		else
			if (dbg) o.outln("Node " + Id + ": " + c + " - " + b.name + " violates Rule 1");	
					
					
						
		return false;
		
	}



//NOT IN PAPER
	private boolean validMove5(int c, Map Components, LTSOutput o )  {
		boolean dbg = false;
		//First component cannot be further moved more than one bMSCs away
		
		boolean found = false;
		if (isFirst(c))  {
			int myPos = getPosition(c);			
			//No picar en punta.....: 		
			for (int i=0; i<size && !found; i++)  {
				found = (i!=c && myPos == getPosition(i)) ;
			} 
			if (!found)  {
				//o.outln("ENCONTRE MOVE5 : " + c);
				//print(o);		
				return false;
			}
		}		
		
		return true;
	}


//NOT IN PAPER
	private boolean validMove6(int c, Map Components, LTSOutput o )  {
		boolean dbg = false;
		//Last component cannot be left to lag by more than one bMSCs away
		//assumes there is always a component in 0;
		
		int TotalIn_0 = 0;
		int TotalIn_1 = 0;
		int myPos = getPosition(c);			
		if (myPos == 1)  {
			for (int i=0; i<size && TotalIn_0 + TotalIn_1 <= 2; i++)  {
				if (getPosition(i) == 1)  
					TotalIn_1++;
				if (getPosition(i) == 0)  
					TotalIn_0++;
			} 
			if (TotalIn_0 + TotalIn_1 <= 2)  {
				//o.outln("ENCONTRE MOVE6: " + c);
				//print(o);
				return false;
			}
		}
		
		return true;
	}


	
	//Seems to be wrong: 
		// Sc1 A says hello to B, 
		// Sc2 C says goodbye to D.
		// hMSC: Init -> {Sc1, Sc2}, Sc1->Sc2, Sc2->Sc1.

	//Trimming rule 2 in paper.
	private boolean validMove3(int c, Map Components, LTSOutput o )  {
		boolean dbg = false;
		//No Overtaking rule.
		
		//Can be extended to those c that are not first.
		//if (isFirst(c))  {
			BasicMSC current = getLocation(c);
		
			if (dbg) print(o);
			int myPos = getPosition(c);
		
			//No overtaking: 		
			for (int i=0; i<size; i++)  {
				if (myPos > getPosition(i) && getLocation(i) == current)  
					return false;
			} 
		//}
		return true;
	}


	//Trimming Rule 1 in Paper.
	private boolean validMove1(int c, Map Components, LTSOutput o )  {
		boolean dbg = false;
		BasicMSC current = getLocation(c);
		
		if (dbg) print(o);
		if (dbg) o.outln("Get components needed in " + current.name + " so " + Components.get(new Integer(c)) + "can move to next.");
		StringSet S = (StringSet) (current.getDependencies(o)).get((String) Components.get(new Integer(c))); 

		if (dbg) o.outln("Check if they are any components that haven't reached current and which c depends on.");
		int myPos = getPosition(c);
		
		
		for (int i=0; i<size; i++)  {
			if (myPos > getPosition(i))  {
				if (dbg) o.outln(Components.get(new Integer(i)) + "is needed");
				if (S.contains(Components.get(new Integer(i))))   {
					if (dbg) o.outln("AND IS NOT AVAILABLE");
					return false;
				}
			}	
		}
		return true;
	}

	//trimming rule 3 in paper
	private boolean validMove2(int c, Map Components, LTSOutput o )  {
		boolean dbg = false;
		boolean dbg2 = false;		
		
		BasicMSC current = getLocation(c);
		
		//given c it returns those c' for which c canfinish before c'
		Map M = current.getCanFinishBefore(o); 
		String myCompName = (String) Components.get(new Integer(c));
		StringSet myDependencies = (StringSet) M.get(myCompName);
		
		
		for (int i=0; i<Components.keySet().size(); i++)  {
			if (i!=c && getPosition(i) == getPosition(c))  {  //changed <= for ==

				String CompName = (String) Components.get(new Integer(i));
				StringSet Dependencies = (StringSet) M.get(CompName);
				
				if (!myDependencies.contains(CompName) && Dependencies.contains(myCompName))  {
					if (dbg) o.outln("ValidMove2: C = " + c);
					if (dbg) print(o);
					if (dbg) o.outln("Component " + myCompName + ". Dependencies");
					if (dbg) myDependencies.print(o);
					if (dbg) o.outln("Against Component " + CompName + ". Dependencies");
					if (dbg) Dependencies.print(o);
					
				
					return false;								
				}
			}
		}
		return true;				
	}


	//trimming rule 4 in paper.
	private boolean validMove4(int c, Map Components, LTSOutput o )  {
		boolean dbg = false;
		
		BasicMSC current = getLocation(c);
		String myCompName = (String) Components.get(new Integer(c));
		
		for (int i=0; i<Components.keySet().size(); i++)  {
			if (i!=c && getPosition(i) > getPosition(c))  {
			
				String CompName = (String) Components.get(new Integer(i));
				String Partner = current.getLastDependency(CompName, o);
				
				if (Partner != null)  {
					if (CompName.equals(current.getLastDependency(Partner, o)))
					if (!Partner.equals(myCompName))   {
						for (int j=0; j<Components.keySet().size(); j++)  {
							if (((String) Components.get(new Integer(j))).equals(Partner))  {
								if (getPosition(j) <= getPosition(c))   {
									if (dbg) o.outln("Moving " + myCompName);
									if (dbg) print(o);
									if (dbg) o.outln("Found someone (" + CompName + ") who is more advanced and whose last partner (" + Partner + ") has not moved on");
									return false;
								}
							}
						}
					}
				}
			}
		}
		return true;
	}


// MORE RULES: If c's lastpartner is at current, then priorise alphabetically.
// The leading component cannot be more than one away from the second...



/*
	public boolean isLast(int c)  {
		boolean minimum = true;
		int myPos = getPosition(c);
		for (int i=0; i<size && minimum; i++)  {
			minimum = (myPos <= getPosition(i));
		}
		return minimum;
	}

*/	
	//Assumes N.history es un prefijo de history.
/*	//Might be missing a "+1"
	private Vector getHistoryDifference(Node N)  {
		int dif = first-N.first;
		Vector ret = new Vector(dif);
		for (int i = 0;i<dif;i++)
			ret.add(i, History.get(i+N.first));
		return ret;
	}

	public Vector getDestinyDifference(Node N)  {
		int dif = SizeOfDestiny()-N.SizeOfDestiny();
		
		Vector ret = new Vector(dif);
		for (int i = 0;i<dif;i++)
			ret.add(i, History.get(i+getLast()+N.SizeOfDestiny()));
		return ret;
	}

*/
	




	

	/*

	public void MovePartner(int c, Map Components, LTSOutput o)  {
		boolean dbg = true;
		
		if(dbg) o.outln(c + "-" + (String) Components.get(new Integer(c)));
		
		if(dbg) o.outln("Pos" + getPosition(c));
		BasicMSC BAnterior = (BasicMSC) History.get(getPosition(c)-1);
		if(dbg) o.outln("BANT = " + BAnterior.name);
		
		String Partner = BAnterior.getLastDependency((String) Components.get(new Integer(c)), o);
		if(dbg) o.outln("Part = " + Partner);
		
		if (dbg) o.outln("In " + BAnterior.name + " " + (String) Components.get(new Integer(c)) + " has as partner " + Partner);
		for (int i=0; i<size; i++)  {
			String CurrComp = (String) Components.get(new Integer(i));
			if (CurrComp.equals(Partner))   {
				if (dbg) o.outln("CurrComp = " + i + "-" + CurrComp + "Pos =" + getPosition(i));
				
				if (getLocation(i) != BAnterior || getPosition(i) != getPosition(c)-1)  {
					o.outln("1-Partner in wrong place!");
					print(o);
					throw new Error();
				}
				else  {
					if (Move(i, Components, o))  {
						o.outln("2- Partner in wrong place!");
						print(o);
						throw new Error();
					}
					else
						return;
				}
			}	
		}
	}
	

	
	
	



	


	


	
			  
	


	public int getPositionInDestiny(int c)  {
		return ((Integer) Positions.get(c)).intValue() - getLast();
	}

	
	public int SizeOfHistory()  {
		return first;
	}
	
	
	
	
	private boolean historyIsAPrefixOf(Node N)  {
		if (first > N.first)
			return false;
			
		boolean prefix = true;
		for (int i = 0; i<=first && prefix;i++)  
			prefix = (History.get(i) == N.History.get(i)); 
		return prefix;	
	}


	public boolean DestinyIsAPrefixOf(Node N, LTSOutput o)  {
		boolean dbg = false; //relevant();
		if (SizeOfDestiny() > N.SizeOfDestiny())
			return false;
		if (dbg) o.outln("This:");
		if (dbg) print(o);
		if (dbg) o.outln("SizeofDestiny = " + SizeOfDestiny() + "  last=" + getLast());
		
		if (dbg) o.outln("Other:");
		if (dbg) N.print(o);
		if (dbg) o.outln("SizeofDestiny = " + N.SizeOfDestiny() + "  last=" + N.getLast());
		
		boolean prefix = true;
		for (int i = 0; i<SizeOfDestiny() && prefix;i++)   {
			BasicMSC b = (BasicMSC) History.get(i+getLast()); 
			BasicMSC N_b =  (BasicMSC)N.History.get(i+N.getLast());
			if (dbg) o.outln("Comparing for i=" + i + ": " + b.name + " vs " + N_b.name);
			prefix = (History.get(i+getLast()) == N.History.get(i+N.getLast())); 
		}
		return prefix;	
	}

	private int getLast()  {
		int min = getPosition(0);
		for (int i = 0; i<size;i++)  {
			if (min > getPosition(i))
				min = getPosition(i);
		}	
		return min;
	}
	
	public BasicMSC getFirstLocation() {
		return (BasicMSC) History.get(first);
	}
	
	
	
	public boolean relevant()  {
		boolean isTwo = true;
		for (int i = 0; i<size && isTwo ;i++)
			isTwo = (getPosition(i) ==4);
		return isTwo;
	}
	
	*/
}
