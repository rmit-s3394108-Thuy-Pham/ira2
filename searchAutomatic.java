import java.util.ArrayList;
import java.util.InputMismatchException;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class searchAutomatic
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
    int R;
    int E;

    if (args.length <18)
    {
      System.out.println("Please enter the right invocation as specified in README.txt");
    }
    else
    {
      typeOFsimilarityFunction = args[0];
      query_label = args[2];
      num_results = Integer.parseInt(args[4]);
      lexiconname = args[6];
      invlistsname = args[8];
      mapname = args[10];
      stoplistname = args[12];
      R = Integer.parseInt(args[14]); //user will specify R
      E = Integer.parseInt(args[16]); // user will specify E
      for (int i =17; i < args.length; i++)
      {
        String query = (args[i].replaceAll("[^a-zA-z]", " ")).toLowerCase(); // process the query term same as the way process lexicon
        String[] temp = query.split(" ");
        for (String term: temp)
        {
          queryterms.add(term);
        }
      }

      /*loading Lexicon from dish to memory and stored in hashtable data structure*/
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

      /*Loading map from disk to memory and stored in hashtable data structure, at the same time, retrieve the document weight and store in hashtable as well*/
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


      /*Reading the stoplist file and initialize stoplist hashtable */
      Hashtable<String, Integer> stoplist = new Hashtable<String, Integer>();
      String sCurrentLine;
      FileReader fr = new FileReader(stoplistname);
      BufferedReader br = new BufferedReader(fr);
      int v = 0;
      while ((sCurrentLine = br.readLine()) != null)
      {
        stoplist.put(sCurrentLine.toLowerCase(), v);
        v++;
      }
      br.close();
      fr.close();

      /*Caculate Average Document Length*/
      double AL;
      double totallength = 0;
      for (Object key: docIDanddocweight.keySet())
      {
        totallength = totallength + docIDanddocweight.get(key);
      }
      AL = totallength/(docIDanddocweight.size());

	     /*First, gather the sum of BM25 for each term in each documents, then sum all the BM25 values because each docID might include more than one query term*/
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
            BM25 = (Math.log(((N - ft) + 0.5)/(ft + 0.5))) * ((k1 + 1)*fdt)/(K + fdt);
            if (!(docIDandBM25.containsKey(map.get(docID))))
            {
              docIDandBM25.put(map.get(docID), BM25);
            }
            else
            {
              double currBM25 = docIDandBM25.get(map.get(docID));
              docIDandBM25.put(map.get(docID), currBM25 + BM25);
            }
          }
          raFile.close();
        }
      }

    /*Reversedmap for easier to retrieve DocID*/
	  Hashtable<String, Integer> reversedmap = new Hashtable<>();
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
            reversedmap.put(mapElements[1], Integer.parseInt(mapElements[0]));
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

    /*Select the docID that has top R BM25 values. Store these IDs in docIDArray*/

    MinHeap minheap = new MinHeap(docIDandBM25, R);
    String[] docIDArray = minheap.getDocIDarray();

    ArrayList<Integer> docNums = new ArrayList<>();

    for (int a = 1; a < docIDArray.length; a++)
    {
      if (docIDArray[a] != null)
      {
        int docNum = reversedmap.get(docIDArray[a]);
        docNums.add(docNum);
      }
    }


    /*Go to the latimes collection again and parse the documents that has docID in the docIDarray*/
    String collectionname = "/home/inforet/a2/latimes";
    String[] docs = readFileandsplitDoc(collectionname);
    Hashtable<String, Double> termVSft = new Hashtable<>();
    Hashtable<String, Double> termVSrt = new Hashtable<>();
    Hashtable<String, Double> termandTSV = new Hashtable<>();

    for (int num : docNums) // go through each document in top R documents
    {
      String eachDoc = docs[num];
      /*Extract the content data in the <HEADLINE> and <TEXT> tags of each doc*/
      ArrayList<String> cleanedTextremovedstopwords = new ArrayList<>(); //processed terms will be stored in arraylist of strings
      Pattern pContent = Pattern.compile("(?<=<HEADLINE>)([^\r]*)(?=</TEXT>)" );
      Matcher mContent = pContent.matcher(eachDoc);
      if (mContent.find())
      {
        String text = (((mContent.group(1).toString()).replaceAll("<.*>", "")).replaceAll("[^a-zA-z]", " ")).toLowerCase();
        //the processed text will be remove all the markup tag,
        //any punctuations and symbols that are not letter or numbers will be replace by " "
        String[] termsOfContent = text.split(" ");
        for (String term: termsOfContent)
        {
          if (!(stoplist.containsKey(term))) //remove stopwords from the list of processed terms
          {
            cleanedTextremovedstopwords.add(term); // add the content terms to the arraylist
          }
        }
      }
      else
      {
        Pattern SpContent = Pattern.compile("(?<=<TEXT>)([^\r]*)(?=</TEXT>)" );
        Matcher SmContent = SpContent.matcher(eachDoc);
        if (SmContent.find())
        {
          String text = (((SmContent.group(1).toString()).replaceAll("<.*>", "")).replaceAll("[^a-zA-z]", " ")).toLowerCase();
          //the processed text will be remove all the markup tag,
          //any punctuations and symbols that are not letter or numbers will be replace by " "
          String[] termsOfContent = text.split(" ");
          for (String term: termsOfContent)
          {
            if (!(stoplist.containsKey(term))) //remove stopwords from the list of processed terms
            {
              cleanedTextremovedstopwords.add(term); // add the content terms to the arraylist
            }
          }
        }
      }
      Hashtable<String, Double> keyvsInDocFre = new Hashtable<String, Double>();
      for (int k =0; k < cleanedTextremovedstopwords.size(); k++)//go thru each content term
      {
        String key = (cleanedTextremovedstopwords.get(k)).toString();
        if ((key.length() >0) && (!key.isEmpty())) //excludes the empty/spaces/null values
        {
          if (!(termVSrt.containsKey(key)))
          {
            double t = 1;
            termVSrt.put(key, t); // initialize thhe termVSrt hashtable. rt: how many documents in top R documents contain the term
            keyvsInDocFre.put(key, t);
          }
          else if ((termVSrt.containsKey(key)) && (!(keyvsInDocFre.containsKey(key))))
          {
            double h = 1;
            keyvsInDocFre.put(key, h);
            termVSrt.put(key, termVSrt.get(key) + 1);
          }
          else if ((termVSrt.containsKey(key)) && keyvsInDocFre.get(key) >= 1)
          {
            keyvsInDocFre.put(key, keyvsInDocFre.get(key) + 1);
          }
        }
      }

      /*Start caculate the TSV value*/
      for (Object key: termVSrt.keySet())
      {
        int offset = lexicon.get(key);
        byte[] docFreinBinary = new byte[18];
        RandomAccessFile raFile = new RandomAccessFile(invlistsname, "r");
        raFile.seek(offset*18); // each block has 18 bits
        raFile.readFully(docFreinBinary);
        double ft = (double)(Integer.parseInt((new String(docFreinBinary)), 2));
        termVSft.put(key.toString(), ft);
        double rt = termVSrt.get(key);
        double TSV;
        int N = map.size();
        double first = (Math.pow((ft/N), rt));
        double second = factorial((double)R)/(factorial(rt) * factorial(R - rt));
        TSV = first*second;
        termandTSV.put(key.toString(), TSV);
        raFile.close();
      }
    } // end of the loop going through each document in top R document

	MaxHeap maxheap = new MaxHeap(termandTSV, E); // chose the terms has lowest TSV
	String[] terms = maxheap.getTerms(); // those are 25 new terms;

	for (int x = 1; x < terms.length; x++)
  {
    String newterm = terms[x];
    double rt = termVSrt.get(newterm);
    int N = map.size();
    // int R = 10;
    int ft;
    int fileoffsetpostion = lexicon.get(newterm); //get how many blocks we need to skip
    byte[] docFreinBinary = new byte[18];
    RandomAccessFile raFile = new RandomAccessFile(invlistsname, "r");
    raFile.seek(fileoffsetpostion*18); // each block has 18 bits
    raFile.readFully(docFreinBinary);
    ft = Integer.parseInt((new String(docFreinBinary)), 2);
    double wt  = Math.log(((rt+0.5)*(N - ft - R + rt + 0.5))/((ft-rt+0.5)*(R-rt+0.5)));
    for (int i = 0; i < ft; i ++)
    {
      int fdt;
      int docID;
      double athirdBM25;
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
      athirdBM25 = (1.0/3.0)*wt*((k1 + 1)*fdt)/(K + fdt);
      if (!(docIDandBM25.containsKey(map.get(docID))))
      {
        docIDandBM25.put(map.get(docID), athirdBM25);
      }
      else
      {
        double currBM25 = docIDandBM25.get(map.get(docID));
        docIDandBM25.put(map.get(docID), currBM25 + athirdBM25);
      }
    }
  }

  MinHeap minheapFinal = new MinHeap(docIDandBM25, num_results);
  double[] heapFinal = minheapFinal.getHeap();
  String[] docIDArrayFinal = minheapFinal.getDocIDarray();
  Map<String, Double> m = new HashMap<>();
  for (int a = 1; a < heapFinal.length; a++)
  {
    m.put(docIDArrayFinal[a], heapFinal[a]);
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


/*SUPPORTING METHODS*/
public static String[] readFileandsplitDoc(String sourcefile)
{
  String text;
  String[] docs = null;
  try
  {
    FileReader instream = new FileReader(sourcefile);
    BufferedReader bufRead = new BufferedReader(instream);
    StringBuilder sb = new StringBuilder();
    try
    {
      String inputLine = bufRead.readLine();
      while (inputLine != null)
      {
        sb.append(inputLine);
        sb.append("\n");
        inputLine = bufRead.readLine();
      }
      text = sb.toString();
      docs = text.split("<DOC>");
      instream.close();
      bufRead.close();
    }
    catch(IOException e)
    {
      System.out.println(e.getMessage());
    }
  }
  catch(FileNotFoundException fnfe)
  {
    System.out.println(fnfe.getMessage());
  }
  return docs;
}


 public static double factorial(double n)
 {
   if (n == 0)
     return 1;
   else
     return(n * factorial(n-1));
 }

}
