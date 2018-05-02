import java.util.ArrayList;
import java.util.InputMismatchException;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class search
{
  public static void main(String[] args)throws IOException
    {
      String lexiconname = new String();
      String invlistsname = new String();
      String mapname = new String();
      ArrayList<String> queryterms = new ArrayList<>();
      Hashtable<String, Integer> lexicon = new Hashtable<>();
      Hashtable<Integer, String> map = new Hashtable<>();
      if (args.length <4)
      {
        System.out.println("Please enter the invocation in this format (or equilavent format): java search lexicon invlists map queryterm_1");
      }
      else
      {
        lexiconname = args[0];
        invlistsname = args[1];
        mapname = args[2];
        for (int i =3; i < args.length; i++)
        {
          String query = (args[i].replaceAll("[^a-zA-z]", " ")).toLowerCase(); // process the query term same as the way process lexicon
          String[] temp = query.split(" ");
          for (String term: temp)
          {
           queryterms.add(term);
          }
        }
      }
      /*loading lexicon to a hashtable again to process*/
      try
      {
        FileReader fr = new FileReader(lexiconname);
        BufferedReader br = new BufferedReader(fr);
        String sCurrentLine;
        try
        {
          sCurrentLine = br.readLine();
          while ((sCurrentLine  != null) && (!sCurrentLine.equals("\n")))
          {
            String[] lexiconElements = new String[2];
            lexiconElements = sCurrentLine.split("\t");
            if (lexiconElements.length ==2)
            {
              lexicon.put(lexiconElements[0], Integer.parseInt(lexiconElements[1]));
            }
            sCurrentLine = br.readLine();
          }
        }
        catch (IOException e)
        {
          System.out.println(e);
        }
      }
      catch(FileNotFoundException fnfe)
      {
        System.out.println(fnfe.getMessage());
      }
      /*Loading map from disk to memory*/
      try
      {
        FileReader fReader = new FileReader(mapname);
        BufferedReader bReader = new BufferedReader(fReader);
        String sLine;
        try
        {
          while  (((sLine = bReader.readLine()) != null) && (!sLine.equals("\n")))
          {
            String[] mapElements = sLine.split("\t");
            if (mapElements.length ==2)
            {
              map.put(Integer.parseInt(mapElements[0]), mapElements[1]);
            }
          }
        }
        catch (IOException e)
        {
          System.out.println(e);
        }
      }
      catch(FileNotFoundException fnfe)
      {
        System.out.println(fnfe.getMessage());
      }

      for (String queryterm : queryterms)
      {
        if (lexicon.containsKey(queryterm))
        {
          System.out.println(queryterm);
          int fileoffsetpostion = lexicon.get(queryterm); //get how many blocks we need to skip
          byte[] docFreinBinary = new byte[18];
          RandomAccessFile raFile = new RandomAccessFile(invlistsname, "r");
          raFile.seek(fileoffsetpostion*18); // each block has 18 bits
          raFile.readFully(docFreinBinary);
          int docFre = Integer.parseInt((new String(docFreinBinary)), 2);
          System.out.println(docFre);
          for (int i = 0; i < docFre; i ++)
          {
            byte[] docIDinBinary = new byte[18];
            raFile.readFully(docIDinBinary);
            int docID = Integer.parseInt((new String(docIDinBinary)), 2);
            byte[] inDocFreinBinary = new byte[18];
            raFile.readFully(inDocFreinBinary);
            int inDocFre = Integer.parseInt((new String(inDocFreinBinary)), 2);
            System.out.println(map.get(docID) + "     " + inDocFre);
          }
        }
      }
    }
}
