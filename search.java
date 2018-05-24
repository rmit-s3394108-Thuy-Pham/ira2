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
    long startTime = System.nanoTime();
    String typeOFsimilarityFunction = new String();
    String query_label = new String();
    String lexiconname = new String();
    String invlistsname = new String();
    String mapname = new String();
    int num_results;
    String stoplistname = new String();
    ArrayList<String> queryterms = new ArrayList<>();
    Hashtable<String, Integer> lexicon = new Hashtable<>();
    Hashtable<Integer, String> map = new Hashtable<>();
    Hashtable<String, Double> docIDanddocweight = new Hashtable<>();
    Hashtable<String, Double> docIDandBM25 = new Hashtable<String, Double>();

    if (args.length <12)
    {
      System.out.println("Please enter the right invocation in this format");
    }
    else
    {
      typeOFsimilarityFunction = args[0];
      query_label = args[2];
      num_results = Integer.parseInt(args[4]);
      lexiconname = args[6];
      invlistsname = args[8];
      mapname = args[10];
      if (args[11].equals("-s"))
      {
        stoplistname = args[12];
        for (int i =13; i < args.length; i++)
        {
          String query = (args[i].replaceAll("[^a-zA-z]", " ")).toLowerCase(); // process the query term same as the way process lexicon
          String[] temp = query.split(" ");
          for (String term: temp)
          {
            queryterms.add(term);
          }
        }
      }
      else
      {
        for (int i =11; i < args.length; i++)
        {
          String query = (args[i].replaceAll("[^a-zA-z]", " ")).toLowerCase(); // process the query term same as the way process lexicon
          String[] temp = query.split(" ");
          for (String term: temp)
          {
            queryterms.add(term);
          }
        }
      }
      /*loading Lexicon file into memory and stored in a hashtable*/
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
       fr.close();
       br.close();
      }
      catch(FileNotFoundException fnfe)
      {
        System.out.println(fnfe.getMessage());
      }

      /*Loading map from disk to memory and stored in hashtable form & retrieve the document weights for each docID */
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
            if (mapElements.length == 3)
            {
              map.put(Integer.parseInt(mapElements[0]), mapElements[1]);
              docIDanddocweight.put(mapElements[1], Double.parseDouble(mapElements[2]));
            }
          }
        }
        catch (IOException e)
        {
          System.out.println(e);
        }
        fReader.close();
        bReader.close();
      }
      catch(FileNotFoundException fnfe)
      {
        System.out.println(fnfe.getMessage());
      }

      /*Caculate Average Document Length*/
      double AL;
      double totallength = 0;
      for (Object key: docIDanddocweight.keySet())
      {
        totallength = totallength + docIDanddocweight.get(key);
      }
      AL = totallength/(docIDanddocweight.size());


      /*Process each query term to gather its ft, docID, fdt */
      for (String queryterm : queryterms)
      {
        if (lexicon.containsKey(queryterm))
        {
          int N = map.size();
          int ft;
          /*To fine ft for each term we will go to the invlist file*/
          int fileoffsetpostion = lexicon.get(queryterm); //get how many blocks we need to skip in invlist file
          byte[] docFreinBinary = new byte[18];
          RandomAccessFile raFile = new RandomAccessFile(invlistsname, "r");
          raFile.seek(fileoffsetpostion*18); // each block has 18 bits
          raFile.readFully(docFreinBinary);
          ft = Integer.parseInt((new String(docFreinBinary)), 2);
          for (int i = 0; i < ft; i ++)
          {
            int fdt;
            int docID; //actually the number that represents DocID in map file
            /*Continue reading the invlist posting to find docID number and fdt*/
            byte[] docIDinBinary = new byte[18];
            raFile.readFully(docIDinBinary);
            docID = Integer.parseInt((new String(docIDinBinary)), 2);
            byte[] inDocFreinBinary = new byte[18];
            raFile.readFully(inDocFreinBinary);
            fdt = Integer.parseInt((new String(inDocFreinBinary)), 2);
            double BM25;
            double K;
            double L;
            double k1 = 1.2;
            double b = 0.75;
            L = docIDanddocweight.get(map.get(docID));
            K = k1 * ( (1 - b) + ((b*L)/AL));
            BM25 = Math.log(((N - ft) + 0.5)/(ft + 0.5)) * ((k1 + 1)*fdt)/(K + fdt);

            /*Start initialize the hashtable docIDandBM25*/
            if (!(docIDandBM25.containsKey(map.get(docID)))) //if this is the first time the doc that has docID was seen that it contains a query term
            {
              docIDandBM25.put(map.get(docID), BM25); // put its docID into the hashtable, put its term's BM25 value into the hashtable
            }
            else
            {
              double currBM25 = docIDandBM25.get(map.get(docID));
              docIDandBM25.put(map.get(docID), currBM25 + BM25); // the more queryterms the docID contains,  the higher sum of its terms' BM25 is
            }
          }
          raFile.close();
        }
      }
      /*Choosing the docID that has highest similarity values*/
      MinHeap minheap = new MinHeap(docIDandBM25, num_results);
      double[] heap = minheap.getHeap();
      String[] docIDArray = minheap.getDocIDarray();

      /*Organise those into order*/
      Map<String, Double> m = new HashMap<>();
      for (int a = 1; a < heap.length; a++)
      {
        m.put(docIDArray[a], heap[a]);
      }
      List<Map.Entry> list = new ArrayList<Map.Entry>(m.entrySet());
      Collections.sort(list, new Comparator<Map.Entry>()
      {
        public int compare(Map.Entry e1, Map.Entry e2)
        {
          Double d1 = (Double) e1.getValue();
          Double d2 = (Double) e2.getValue();
          return d2.compareTo(d1);
        }
      });
    int order = 1;
    for(Map.Entry e : list)
    {
      if (order == (list.size()) && e.getKey() == null )
      {
        break;
      }
     System.out.println(query_label + " " + e.getKey() + " " + order +" "+ e.getValue());
     order = order + 1;
    }
  }
  long endTime = System.nanoTime();
  long duration = (endTime - startTime);
  System.out.println("Running time: "+ (duration/1000000) + "ms");
  }
}
