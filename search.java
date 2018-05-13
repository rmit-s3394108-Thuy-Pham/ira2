import java.util.ArrayList;
import java.util.InputMismatchException;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
// need to edit for Assignment 2

public class search
{

  public static void main(String[] args)throws IOException
  {
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

      /*loading Lexicon into hashtable*/
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
            if (mapElements.length == 3)
            {
              map.put(Integer.parseInt(mapElements[0]), mapElements[1]);
              docIDanddocweight.put(mapElements[1],Double.parseDouble(mapElements[2]));
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

      /*Caculate Average Document Length*/
      double AL;
      double totallength = 0;
      for (Object key: docIDanddocweight.keySet())
      {
        totallength = totallength + docIDanddocweight.get(key);
      }
      AL = totallength/(docIDanddocweight.size());
      System.out.println("docIDanddocweight has size of: " + docIDanddocweight.size());
      System.out.println("total length: "+ AL);

      Hashtable<String, Double> docIDandBM25 = new Hashtable<String, Double>();
      for (String queryterm : queryterms)
      {
        if (lexicon.containsKey(queryterm))
        {
          int N = map.size();
          int ft;
          int fileoffsetpostion = lexicon.get(queryterm); //get how many blocks we need to skip
          byte[] docFreinBinary = new byte[18];
          RandomAccessFile raFile = new RandomAccessFile(invlistsname, "r");
          raFile.seek(fileoffsetpostion*18); // each block has 18 bits
          raFile.readFully(docFreinBinary);
          ft = Integer.parseInt((new String(docFreinBinary)), 2);
          for (int i = 0; i < ft; i ++)
          {
            int fdt;
            int docID;
            double BM25;
            double K;
            double L;
            double k1 = 1.2;
            double b = 0.75;
            byte[] docIDinBinary = new byte[18];
            raFile.readFully(docIDinBinary);
            docID = Integer.parseInt((new String(docIDinBinary)), 2);
            byte[] inDocFreinBinary = new byte[18];
            raFile.readFully(inDocFreinBinary);
            fdt = Integer.parseInt((new String(inDocFreinBinary)), 2);
            L = docIDanddocweight.get(map.get(docID));
            K = k1 * ( (1 - b) + ((b*L)/AL));
            BM25 = Math.log(((N - ft) + 0.5)/(ft + 0.5)) * ((k1 + 1)*fdt)/(K + fdt);
            if (!(docIDandBM25.containsKey(docID)))
            {
              docIDandBM25.put(map.get(docID), BM25);
            }
            else
            {
              double currBM25 = docIDandBM25.get(docID);
              docIDandBM25.put(map.get(docID), currBM25 + BM25);
            }
          }
        }
      }
      MinHeap minheap = new MinHeap(docIDandBM25, num_results);
      double[] heap = minheap.getHeap();
      String[] docIDArray = minheap.getDocIDarray();
      for (int a = 0; a < heap.length; a++)
      {
        System.out.println(heap[a] + " " + docIDArray[a]);
      }
    }
  }
}

  // public static int parent(int pos)
  // {
  //   return pos/2;
  // }
  //
  // public static void swap(int fpos, int tpos)
  // {
  //   double tmp;
  //   tmp = heap[fpos];
  //   heap[fpos] = heap[tpos];
  //   heap[tpos] = tmp;
  // }
  // public static int rightChild(int p)
  // {
  //   return p*2 + 1;
  // }
  // public static int leftChild(int p)
  // {
  //   return p*2;
  // }
  // public static void swapID(int fpos, int tpos)
  // {
  //   String tmp;
  //   tmp = docIDArray[fpos];
  //   docIDArray[fpos] = docIDArray[tpos];
  //   docIDArray[tpos] = tmp;
  // }
  //
  // public static boolean isLeaf(int p)
  // {
  //   if (p >=  (((double)(heap.length)) /  2)  &&  p <= heap.length)
  //   {
  //     return true;
  //   }
  //   return false;
  // }
  //
  // public static void minHeapipy(int k)
  // {
  //   if (!isLeaf(k))
  //   {
  //     if (rightChild(k) == (heap.length))
  //     {
  //       if (heap[k] > heap[leftChild(k)])
  //       {
  //         swap(k, leftChild(k));
  //         swapID(k, leftChild(k));
  //       }
  //     }
  //     else
  //     {
  //       if ( heap[k] > heap[leftChild(k)]  || heap[k] > heap[rightChild(k)])
  //       {
  //         if (heap[leftChild(k)] < heap[rightChild(k)])
  //         {
  //           swap(k, leftChild(k));
  //           swapID(k, leftChild(k));
  //           minHeapipy(leftChild(k));
  //         }
  //         else
  //         {
  //           swap(k, rightChild(k));
  //           swapID(k, rightChild(k));
  //           minHeapipy(rightChild(k));
  //         }
  //       }
  //     }
  //   }
  //
  // }
