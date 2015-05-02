package helper;

import java.io.*;
/**
 *  A simple output class to write values to a file of characters.
 *  If any file errors occur, methods in this class will display
 *  an error message and terminate the program.
 *  @version 1.1 11.02
 *  @author Graham Roberts
 */
public class FileOutput
{
    /** 
     * Construct FileOutput object given a file name.
     */
    public FileOutput(String fname)
    {
      this(fname,false);
    }

    /** 
     * Construct FileOutput object given a file name and append mode.
     * If append is true then data will be appended to the end of an
     * existing file.
     */
    public FileOutput(String fname, boolean append)
    {
      filename = fname;
      try
      {
        writer = new BufferedWriter(new FileWriter(filename,append));
      }
      catch (IOException e)
      {
	error("Can't open file: " + filename);
      }
    }

    /**
     *  Close the file when finished
     */
    public void close()
    {
      try
      {
        writer.close();
      } 
      catch (IOException e)
      {
        error("Can't close file: " + filename);
      }
    }

    /**
     *  Write an int value to a file.
     */
    public final synchronized void writeInteger(int i)
    {
        try
        {
          writer.write(Integer.toString(i));
        }
        catch (IOException e) 
        {
          error("writeInteger failed for file: " + filename);
        }
    }

    /**
    *  Write a long value to a file.
    */
    public final synchronized void writeLong(long l)
    {
        try
        {
          writer.write(Long.toString(l));
        }
        catch (IOException e) 
        {
          error("writeLong failed for file: " + filename);
        }
    }

    /**
    *  Write a double value to a file.
    */
    public final synchronized void writeDouble(double d)
    {
        try
        {
          writer.write(Double.toString(d));
        }
        catch (IOException e) 
        {
          error("writeDouble failed for file: " + filename);
        }
    }

    /**
     *  Write a float value to a file.
     */
    public final synchronized void writeFloat(float f)
    {
        try
        {
          writer.write(Float.toString(f));
        }
        catch (IOException e) 
        {
          error("writeFloat failed for file: " + filename);
        }
    }
    /**
     *  Write a char value to a file.
     */
    public final synchronized void writeCharacter(char c)
    {
        try
        {
          writer.write(c);
        }
        catch (IOException e) 
        {
          error("writeCharacter failed for file: " + filename);
        }
    }

    /**
     *  Write a String value to a file.
     */
    public final synchronized void writeString(String s)
    {
        try
        {
          writer.write(s);
        }
        catch (IOException e) 
        {
          error("writeString failed for file: " + filename);
        }
    }

    /**
     *  Write a newline to a file.
     */
    public final synchronized void writeNewline()
    {
        try
        {
          writer.write("\n");
        }
        catch (IOException e) 
        {
          error("writeNewline failed for file: " + filename);
        }
    }

    /**
     * Deal with a file error
     */
    private void error(String msg)
    {
      System.out.println(msg);
      System.out.println("Unable to continue executing program.");
      System.exit(0) ;
    }

    /**
     * Instance variables to store file details.
     */
    private String filename = "";
    private BufferedWriter writer = null; 
}

