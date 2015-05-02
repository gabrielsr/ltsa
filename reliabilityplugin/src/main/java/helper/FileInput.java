package helper;

import java.io.* ;
/**
 *  A simple input class to read values from a file of characters.
 *  If any file errors occur, methods in this class will display
 *  an error message and terminate the program.
 * @version $Id: FileInput.java,v 1.2 2003/12/15 14:07:18 ucacpco Exp $
 *  @author Graham Roberts
 */
public class FileInput
{
    /** 
     * readString reads the next line and puts its results here. This allows it to set eof when
     * the last line is found, and avoid returning a blank line.
     * 
     */
    private String nextLine = "";

    /** 
     * Construct FileInput object given a file name.
     */
    public FileInput(String fname)
    {
      filename = fname ;
      try
      {
        reader = new BufferedReader(new FileReader(filename)) ;
      }
      catch (FileNotFoundException e)
      {
	error("Can't open file: " + filename) ;
      }

      // initialise nextLine
      try
	  {
	      nextLine = reader.readLine() ;
	      if (nextLine == null)
		  {
		      eof = true ;
		  }
	  }
      catch (IOException e) 
	  {
	      error("readString failed for file: " + filename) ;
	  }


    }

    /**
     *  Close the file when finished
     */
    public void close()
    {
      try
      {
        reader.close() ;
      } 
      catch (IOException e)
      {
        error("Can't close file: " + filename) ;
      }
    }

    /**
     *  Return true if the end of file has been reached.
     */
    public boolean eof()
    {
      return eof ;
    }

    /**
     *  Read an int value from file.
     */
    public final synchronized int readInteger()
    {
        //
        //  The place to hold the data received from the file so
        //  that we can parse it to find the int value.
        //
        String input = nextLine;
        int val = 0 ;
 
        //
        //  Read a String of characters from the file.
        //
        try
        {
          nextLine = reader.readLine() ;
          if (nextLine == null)
	  {
            eof = true ;
            return val ;
          }
        }
        catch (IOException e) 
        {
          error("readInteger failed for file: " + filename) ;
        }

        //
        //  Parse the String to construct an int value.
        //
        try
        {
            val = Integer.parseInt(input) ;
        }
        catch (NumberFormatException e) {}
        return val ;
    }

    /**
    *  Read a long value from file.
    */
    public final synchronized long readLong()
    {
        //
        //  The place to hold the data received from the file so
        //  that we can parse it to find the long value.
        //
        String input = nextLine ;
        long val = 0L ;
        //
        //  Read a String of characters from the file.
        //
        try
        {
          nextLine = reader.readLine() ;
          if (nextLine == null)
	  {
            eof = true ;
            return val ;
	  }
        }
        catch (IOException e) 
        {
          error("readLong failed for file: " + filename) ;
        }
        //
        //  Parse the String to construct a long value.
        //
        try
        {
            val = Long.parseLong(input) ;
        }
        catch (NumberFormatException e) {}
        return val ;
     }

    /**
     *  Read a double value from file.
     */
    public final synchronized double readDouble()
    {
        //
        //  The place to hold the data received from the file so
        //  that we can parse it to find the double value.
        //
        String input = nextLine ;
        double val = 0.0D ;
        //
        //  Read a String of characters from the file.
        //
        try
        {
          nextLine = reader.readLine() ;
          if (nextLine == null)
	  {
            eof = true ;
            return val ;
	  }
        }
        catch (IOException e) 
        {
          error("readDouble failed for file: " + filename) ;
        }
        //
        //  Parse the String to construct a double value.
        //
        try
        {
            val = (Double.valueOf(input)).doubleValue() ;
        }
        catch (NumberFormatException e) {}
        return val ;
    }

    /**
     *  Read a float value from file.
     */
    public final synchronized float readFloat()
    {
        //
        //  The place to hold the data received from the file so
        //  that we can parse it to find the float value.
        //
        String input = nextLine ;
        float val = 0.0F ;
        //
        //  Read a String of characters from the file.
        //
        try
        {
          nextLine = reader.readLine() ;
          if (nextLine == null)
	  {
            eof = true ;
            return val ;
	  }
        }
        catch (IOException e) 
        {
          error("readFloat failed for file: " + filename) ;
        }
        //
        //  Parse the String to construct a float value.
        //
        try
        {
            val = (Float.valueOf(input)).floatValue() ;
        }
        catch (NumberFormatException e) {}
        return val ;
    }

    // (PAC) This won't work any more.

//     /**
//      *  Read a char value from file.
//      */
//     public final synchronized char readCharacter()
//     {
//         //
//         //  No need to parse anything, just get a character and return
//         //  it..
//         //
//         char c = ' ' ;
//         try
//         {
//           int n = reader.read() ;
//           if (n == -1)
// 	  {
//             eof = true ;
//           }
//           else
// 	  {
//             c = (char)n ;
// 	  }
//         }
//         catch (IOException e) 
//         {
//           error("readCharacter failed for file: " + filename) ;
//         }
//         return c ;
//     }

    /**
     *  Read an String value from file.
     */
    public final synchronized String readString()
    {
        //
        //  No need to parse anything, just get a string and return
        //  it..
        //
        String s = nextLine;

        try
	    {
		nextLine = reader.readLine() ;
		if (nextLine == null)
		    {
			eof = true ;
		    }
	    }
        catch (IOException e) 
	    {
		error("readString failed for file: " + filename) ;
	    }
	
        return s;
    }
    
    /**
     * Deal with a file error
     */
    private void error(String msg)
    {
      System.out.println(msg) ;
      System.out.println("Unable to continue executing program.") ;
      System.exit(0) ;
    }

    /**
     * Instance variables to store file details.
     */
    private String filename = "" ;
    private BufferedReader reader = null ; 
    private boolean eof = false ;
}

