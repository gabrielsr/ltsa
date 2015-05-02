/*
*  Created by Gena’na Nunes Rodrigues
*  Date: 02/August/2004
*  This class reads from the transition file generated from LTSA and calculates the sytem reliability based on the Architecture Model LTS
*/

package LTS;

import helper.FileInput;
import helper.FileOutput;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.StringReader;
import java.io.FileReader;

import determinant.Determinant;

public class TransitionParser{

    String model = null;
    int dim = 0;
    ArrayList parsedTrans = new ArrayList();  //the file with the parsed transitions
    double[][] matrix;  //the matrix to be saved into the file
    boolean swapEnds = false;
    
   void setModel(String model){
      this.model = model;
   }
   
   String getModel(){
     return this.model;
   };
   
   void setMatrixDim(int dim){
     this.dim = dim;
   };
   
   int getMatrixDim(){
      return this.dim;
   };

   void initMatrix(){
        matrix = new double[dim][dim];
        for (int i = 0; i< dim; i++)
            for (int j = 0; j< dim; j++)
                matrix[i][j] = 0;
   };
   
   void setSwapEnds(boolean swap){
       this.swapEnds = swap;
   };
   
   boolean getSwapEnds(){
       return this.swapEnds;
   };
   
   //Gets the values of the transition reliability values
   String[] setARelValues(String inStr){
       String[] relValues = new String[2];
       String[] aux = inStr.split("\\s+");
       if (aux.length ==3){            //Why this aux has lenght 3 instead of 2 ???????????????
           relValues[0] = aux[1];      //This shouldn't happen. It should only be relValues = aux. But there's a strange character I can't recognize and filter
           relValues[1] = aux[2];
       }
       else if (aux.length ==2) //As it should be 
           relValues = aux; 
       //aRelValues.add(relValues);
       return(relValues);
   };
   
   //Adds the content to the ArrayList given the line String[] to add, the length shift of the line and the initial state
   String addToArray(String[] line, int len, String state){
        String [] shortLine = new String[4];
        for (int i=0; i<shortLine.length;i++)
            shortLine[i] = line[i+len];
        //printStates(stateFrom);
        String[] assembledStates = assembleState(shortLine, state);
        parsedTrans.add(assembledStates);
        return (assembledStates[3]);
   };

 // Calculates the number of spaces between the columns to align them
   String formatSpaces(String value, int spaces){ 
      int size =  value.length();
      String aux = "";
      for(int j = size; j<spaces;j++)
          aux = " "+ aux;
       return aux;
   }
   
   void saveMatrixToFile(String file){
       //String print;
       FileOutput out = new FileOutput(file);
       out.writeString(dim+"");
       out.writeNewline();
       for (int i=0; i<dim; i++){
            for (int j=0; j<dim; j++)
                out.writeString(matrix[i][j]+formatSpaces(matrix[i][j]+"", 25));
            out.writeNewline();
        }
       out.close();
   };
 
 // Saves the content of the parsed Transitions into the outputfile named with the prefix "Out" and the name of file
   void saveArrayToFile(String file){ //Print the content of the parsed transitions into the output file
      FileOutput out = new FileOutput(file); //the absolute or relative path
      String[] line;
      for (int i= 0;i<parsedTrans.size(); i++){
          line = (String[])(parsedTrans.get(i));
          for (int l = 0; l<line.length; l++){
             out.writeString(line[l]+formatSpaces(line[l], 25));
          }
          out.writeNewline();
      }
      out.close();
   };
   
   
 //As the states have a "Q" with them, this method removes it and leaves only the number
   String finalState(String state){ 
       String eState = "";
       if ((state.length()>=5) && (state.substring(0,5).equals("ERROR"))) {
          //eState = state.substring(0);  //The state where this transition is going to...
                                     //....It has to exclude the ")," end of the production
         int errorState = dim-1;
         eState = errorState+""; //this is an update in order to include the ERROR state into the matrix
                        //in this case it will the last column and the last row of the matrix
       }
       else
           eState = state.substring(1);
       return eState;
   };
   
  //Supports the method parse to assemble the states into  
   String[] assembleState(String[] line, String state){
         String[] stateFrom = new String[4];
         stateFrom[0] = state;                                     //the initial state
         stateFrom[1] = line[0].substring(1,line[0].length()-1);  //the probability value
         stateFrom[2] = line[1];                                 //the label name, equivalently, the transition in the bMSC
         if (line[3].charAt(line[3].length() - 1) ==',' || line[3].charAt(line[3].length() - 1) =='.'){ // if it's in the end of a production, delimited by a comma 
             stateFrom[3] = finalState(line[3].substring(0,line[3].length()-2));   //the final state
             //if ((!stateFrom[3].equals("ERROR")) && (Integer.parseInt(stateFrom[3]) < Integer.parseInt(state) && stateFrom[2].equals("endAction")))  //in case the final state, END, is not the greater state number
             //    setSwapEnds(true);
             //else setSwapEnds(false);
             //if (stateFrom[2].equals("endAction"))  //in case the final state, END, is not the greater state number
         }
         else
             stateFrom[3] = finalState(line[3]);
         //printStates(stateFrom);
         return stateFrom;
           
   };
   
 //Prints the content of the ArrayList of the Transitions on the screen
   void printStates(String[] states){     
       for (int i = 0; i<states.length; i++)
           System.out.print(i+"  "+states[i]+"  "+formatSpaces(states[i], 10));
       System.out.println();
   }

   //Changes the transition names for the probability values in the Reliability field
   void changeTransitionNameForValue(ArrayList aRelValues){
          for (int i=0; i < aRelValues.size(); i++){
            for (int j=0; j < parsedTrans.size(); j++){
                //if(((String[]) parsedTrans.get(j))[2].equals(((String[])aRelValues.get(i))[0])){
                //   ((String[])parsedTrans.get(j))[2] = ((String[])aRelValues.get(i))[1];
            	        ((String[])parsedTrans.get(j))[2] = "1"; //this is temporary, just to remove the "Reliabilities:" block from the text
            	}
        }
   };
   
   //In case there are more states than the one with label END, they will have to switch so that the highest state has the END label
   //The same is intended to happen to the endAction transition. In this case, it will be 1 transition before END
   void invertFinalStates(String finalValue, String tempValue){
        for (int j=0; j < parsedTrans.size(); j++){
            if(((String[]) parsedTrans.get(j))[3].equals(finalValue))
                ((String[])parsedTrans.get(j))[3] = tempValue;
            else if(((String[]) parsedTrans.get(j))[3].equals(tempValue))
                ((String[])parsedTrans.get(j))[3] = finalValue;
            if(((String[]) parsedTrans.get(j))[0].equals(finalValue))
                ((String[])parsedTrans.get(j))[0] = tempValue;
            else if(((String[]) parsedTrans.get(j))[0].equals(tempValue))
                ((String[])parsedTrans.get(j))[0] = finalValue;

         };
   };
  
/* The is an important method to parse the content of the file containing the transitions generated from the LTS
*  The format of the parsed file is on the format STATE_FROM, PROB_OF_TRANSITION, LABEL, STATE_TO
*/
   void parse(BufferedReader content) throws java.io.IOException{  

	      ArrayList aRelValues = new ArrayList();
	      String str = null;
	      str = content.readLine(); //reading the first line "Process:"
	      setModel(content.readLine()); //reading the type of Process
	      str = content.readLine(); //reading the line "States:"
	      int dim = Integer.parseInt(content.readLine().split("\\s+")[1]);
	      dim = dim + 1; // this is because we are adding the ERROR state
	      setMatrixDim(dim); //reading the number of states
	      
	      //reading the reliability values
	      str = content.readLine();
	      
	      while(!str.equals("Transitions:"))
	    	  	str = content.readLine(); 
	      
	  	  str = content.readLine(); //reading the first line in transitions e.g. ArchitectureModel = Q0,"

	      String[] longLine;
	      String stateFrom = "NIS";  //Not Identified State
	      String tempEND = ""; //Holds the temporary value of END, when it's not the greater state of the FSP
	      String finalEND = "0";    //This variable stores what should be the final value for the transition END, which is the last transition
	      String tempEndAction = ""; //Holds the temporary value of endAction, when it's not the greater state -1 of the FSP
	      String finalEndAction = "0";    //This variable stores what should be the final value for the transition endAction, which is the last transition before END
	      String endActionTo = "0";
	      boolean swap = false;
	      boolean endActionExists = true;

	      str = content.readLine(); //reading now a real state transition

	      while (!str.equals("EOF"))
	      {
	         longLine = str.split("\\s+");
	         if ((longLine[1].charAt(0) == 'Q') && (longLine.length > 7))
	         {   
	        	 // Analyse if the special case of 'endAction' transition happens 
	        	 	stateFrom = longLine[1].substring(1);  //the current initial state
	             if (longLine[5].equals("endAction")){
	            	 	//The longLine[7].length()-2 is to subtract the ). or ), termination of endAction
	            	 	int len = 0;
	            	 	if (longLine[7].length()>2)
	            	 		len = 2;
	            	 	endActionTo = longLine[7].substring(1,longLine[7].length()-len);
	            	 	endActionExists = true;
	             }

	             // The case where there is a production like Qy = ( (1.0) {a, b, c} -> Qx),
	             // Then concatenate a, b and c as one state.
	             if (longLine.length > 8) {
	            	 		while (!longLine[6].equals("->")){
	                   	  	longLine[5] = longLine[5] + longLine[6];
	                   	  	for (int pos=0; pos < longLine.length - 7; pos++){
	                   	  		longLine[6+pos] = longLine[7+pos];
	                   	  	}   
	                     }

	            	 		String[] aux = new String[8];
		                     for(int pos=0;pos<8;pos++)
		                    	 	aux[pos] = longLine[pos];
		                longLine = aux;
	             }
		         
	             addToArray(longLine,4,stateFrom);
	             
	         }
	      
	         else if (longLine[1].equals("|")){  //In this case, it's reading a subproduction of a transition, so the stateFrom has to be same as the if statement above
	          
	        	 		//Analyse again if the special case of 'endAction' transition happens 
	        	 		if (longLine[3].equals("endAction")){
	        	 			//The longLine[5].length()-2 is to subtract the ). or ), termination of endAction
	        	 			int len = 0;
	        	 			if (longLine[5].length()>2)
	        	 				len = 2;
	        	 			endActionTo = longLine[5].substring(1,longLine[5].length()-len);
	        	 			endActionExists = true;
	        	 		}
	        	 
	                 if (longLine.length > 6) {
	            	 		while (!longLine[4].equals("->")){
	                   	  	longLine[3] = longLine[3] + longLine[4];
	                   	  	for (int pos=0; pos < longLine.length - 5; pos++){
	                   	  		longLine[4+pos] = longLine[5+pos];
	                   	  	}   
	                     }

	            	 		String[] aux = new String[6];
		                     for(int pos=0;pos<6;pos++)
		                    	 	aux[pos] = longLine[pos];
		                longLine = aux;
	                 }
		    
	                 addToArray(longLine,2,stateFrom);
	         }
	     	 else  if (longLine.length ==4){ 
	             //for the END transition
	             if ((longLine[3].equals("END.")) || (longLine[3].equals("STOP.")))
	                 finalEND = longLine[1].substring(1); 
	             
	             else if ((longLine[3].equals("END,")) || (longLine[3].equals("STOP,"))){
	               tempEND = longLine[1].substring(1);
	               swap = true;
	             }
	         }
	      
	         if (Integer.parseInt(stateFrom) > Integer.parseInt(finalEND)){
	            int endActionState = Integer.parseInt(stateFrom);
	            endActionState = endActionState - 1; //the endAction state will be one before the final state of the LTS
	            finalEndAction = String.valueOf(endActionState);
	            finalEND = stateFrom;
	         }
          	str = content.readLine(); //reading now a real state transition
	      }//end of while loop    
	      
	      if ((!tempEND.equals(endActionTo)) && (endActionExists)){
	    	  		tempEND = endActionTo;
	      }

	      if (Integer.parseInt(finalEND) != dim-2)  // Here, it will be used to verify if the dimension of the matrix and the number of states match.
	      { 
	           System.err.println("The dimension of the matrix is different from the number of states generated!!!");
	      }
	      
	      if (swap){
	             invertFinalStates(finalEND, tempEND);
	      }
	      //the steps below are to add the transitions of CORRECT and ERROR states to themselves, as it's not included in the generated FSP file
	      // Q_DIM-2 = ( (1.0) endAction -> Q_DIM-2),  -> FOR THE CORRECT STATE
	      //int auxDim = dim-2;
	      //stateFrom =  auxDim+"";
	      //String[] CORRECTLine = {stateFrom,"=","(","(1.0)","1","->","Q"+stateFrom+"),"};
	      //addToArray(CORRECTLine,3,stateFrom);
	      // Q_DIM-1 = ( (1.0) endAction -> Q_DIM-1). -> FOR THE ERROR STATE
	      int auxDim = dim-1;
	      stateFrom = auxDim+"";
	      String[] ERRORLine = {stateFrom,"=","(","(1.0)","1","->","Q"+stateFrom+"),"};
	      addToArray(ERRORLine,3,stateFrom);
	      
	      //The call below is just in case we want to check if the parsing is correct
	      //saveArrayToFile("../output/"+"Out"+file);
	   };//end of method parse
   
   //The file one wants to save the content of the matrix
   String setMatrix(String file){
        int r, c;
        double value;
           for (int i=0; i < parsedTrans.size(); i++){
               if (!((String[])parsedTrans.get(i))[3].equals("ERROR")){
                   r = Integer.parseInt(((String[])parsedTrans.get(i))[0]);
                   c = Integer.parseInt(((String[])parsedTrans.get(i))[3]);
                   value = Double.parseDouble(((String[])parsedTrans.get(i))[1]);
                   matrix[r][c] = matrix[r][c] + value; //in case there are other previously computed values for the same matrix element, such as the ERROR 
            }
        }
        String matFile = "../output/"+"Mat"+file;
        saveMatrixToFile(matFile);
        return matFile;
   };
   
   //In case one doesn't want to save the matrix into a file and compute the result from the memory.
   double[][] setMatrix(){
       int r, c;
       double value;
          for (int i=0; i < parsedTrans.size(); i++){
              if (!((String[])parsedTrans.get(i))[3].equals("ERROR")){
                  r = Integer.parseInt(((String[])parsedTrans.get(i))[0]);
                  c = Integer.parseInt(((String[])parsedTrans.get(i))[3]);
                  //value = Double.parseDouble(((String[])parsedTrans.get(i))[1]) * Double.parseDouble(((String[])parsedTrans.get(i))[2]);
                  value = Double.parseDouble(((String[])parsedTrans.get(i))[1]);
                  matrix[r][c] = matrix[r][c] + value; //This converts into primitive double, and in case there are other previously computed values for the same matrix element, such as the ERROR 
           }
       }
       return matrix;
  };
  /*
  public double[] multipleReliability(String fileName, Determinant determinant){
  	   double[] reliability = new double[11];
       double val = 0.0;
       String message = new String();
       String traces = "";       
       String[] aux = new String[4];
       String temp = "";
       BufferedReader content = new BufferedReader(new FileReader(matFile));
       parse(content);
       initMatrix();
       ArrayList tempParsedTrans = new ArrayList();// = parsedTrans;
       for (int i = 0; i<parsedTrans.size(); i++){
            aux = (String[])parsedTrans.get(i);
            tempParsedTrans.add(i, aux); 
       }
       
       //for (int i=0; i<parsedTrans.size(); i++)
       //   printStates((String[])parsedTrans.get(i));
       
       aux = new String[4];
       for (int j = 0; j<=10; j++){
            val = j/10.0;
            for (int i=0; i < tempParsedTrans.size(); i++){
                if (((String[])tempParsedTrans.get(i))[2].equals("0")){
                         //temp= ((String[])tempParsedTrans.get(i))[2];
                         //if (j==0)
                         //      message = "["+((String[])parsedTrans.get(i))[0]+","+((String[])parsedTrans.get(i))[3]+"]";
                        traces = traces + ", " + "["+((String[])parsedTrans.get(i))[0]+","+((String[])parsedTrans.get(i))[3]+"]";
                        aux = new String[4];
                        aux[0] = ((String[])parsedTrans.get(i))[0];
                        aux[1] = ((String[])parsedTrans.get(i))[1];
                        aux[2] = String.valueOf(val);
                        //System.out.println("ValueAux: "+aux[2]);
                        aux[3] = ((String[])parsedTrans.get(i))[3]; 
                        //((String[])parsedTrans.get(i))[2] = String.valueOf(val);                                                                      
                        parsedTrans.set(i,aux); 
                        saveArrayToFile("../output/"+"Parsed"+j+fileName);
                }        
            }
            parse(content);
            temp = setMatrix(fileName);
            reliability[j] = determinant.calculateReliability(temp);   
       }
       traces = "[" + traces + "]";
       
       //Save the statistics values into a file
       String multRelFile = "../output/"+"Multiple"+fileName;
       FileOutput out = new FileOutput(multRelFile);
       out.writeString("Traces: "+traces);
       out.writeNewline();
       out.writeString("FromTo: "+message);
       out.writeNewline();
       for (int i=0; i<=10; i++){
           val = i/10.0;
           out.writeString(val+"\t"+reliability[i]);
           out.writeNewline();
       }
       out.close();
       return(reliability);
   };
   */
   
   public double setReliability(String file, Determinant determinant){
	   double d_setReliability = 0;
	   try{
		   BufferedReader content = new BufferedReader(new FileReader(file));
		   d_setReliability =  setReliability(content, determinant);
	   } catch(Exception e){
		   System.err.println("Could not read string buffer");
	   }
	   return d_setReliability;
	   //parse(content);
       //initMatrix();
       //double reliability = determinant.calculateReliability(setMatrix(file));   
       //double reliability = determinant.calculateReliability(dim, setMatrix());   
       //return(reliability);
   };
   
   public double setReliability(BufferedReader content, Determinant determinant){
       try{
    	   		parse(content);
    	   		initMatrix();
       }
       catch(Exception e){
    	   		System.err.println("Could not read string buffer");
       }
    	   double reliability = determinant.calculateReliability(dim, setMatrix());   
       return(reliability);
   };
   
   public double callParser(String lts){
       	//System.out.println("In Transition Parser: "+lts);
       	Determinant determinant = new Determinant();
       	//verify if the BufferedReader is being parsed properly
       	BufferedReader content = new BufferedReader(new StringReader(lts));
       	double result = setReliability(content, determinant);
       return result;
   }//end of main
   
   
   public static void main(String[] args){
       TransitionParser tParser = new TransitionParser();
       Determinant determinant = new Determinant();
       String matFile;
       BufferedReader content;
       /*
       if (args[0].equals("-multi")){
              matFile = args[1];
            	  	if ((args.length > 2) && (args[2].equals("inter"))){
            	  		int inter = Integer.parseInt(args[3]);
            	  		determinant.setInterval(inter);
            	  	}
            	  	double[] reliability = tParser.multipleReliability(matFile, determinant);
       }
       else{*/
             matFile = args[0];
             if ((args.length > 1) && (args[1].equals("-inter"))){
                 ArrayList relInterval = new ArrayList();
                 String relIntervalElement = "";
                 int inter = Integer.parseInt(args[2]);
                 for (int i=1; i<= inter;){
                     determinant = new Determinant();
                     tParser = new TransitionParser();
                     determinant.setInterval(i);
                     relIntervalElement = i+"\t"+tParser.setReliability(matFile, determinant);                     
                     relInterval.add(relIntervalElement);
                     if (i == 1)
                         i = i+9; //this quick fix because i=0 means the identity matrix, therefore it has to start with 1 and not 0! 
                     else if (i<100)  //... and from then on, increment by 10....
                            i = i+10;
                          else if (i >= 100) //...or by 100...
                                    i = i + 100;
                               else if (i >= 1000) //...or by 1000
                                       i = i + 1000;
                 }
                 String relIntervalFile = ".\\parsed\\"+"Interval"+matFile;
                 FileOutput out = new FileOutput(relIntervalFile);
                 out.writeString("Interval \t");
                 out.writeString("System Reliability ("+matFile+")");
                 out.writeNewline();
                 for (int i=0; i<relInterval.size(); i++){
                     out.writeString((String)relInterval.get(i));
                     out.writeNewline();
                 }
                 out.close();
             }//end of if interval
             else{
                 //if no interval, then compute as a single reliability value and exit
            	    	tParser.setReliability(matFile, determinant);
             }//end of latter else
       //}//end of first else
   }//end of main
 
}//end of the class
