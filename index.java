import java.util.ArrayList;
import java.util.InputMismatchException;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class index
{
  /*MAIN METHOD PART*/
  public static void main(String[] args)throws IOException
  {
    String sourcefile = new String();
    String stoplistname = new String();
    /* Based on the number of command-line arguments
    to decide whether or not the program should print all content terms */
    //case1: no stopping no prinitng
    if (args.length == 1) // invocation has the format as: java index latimes
    {
      sourcefile = args[0];
      parser(readFileandsplitDoc(sourcefile));
    }
    //case2: no stopping and printing enable
    else if (args.length == 2) // invocation has the format: java index -p latimes
    {
      sourcefile = args[1];
      ArrayList<String> parsedText = parser(readFileandsplitDoc(sourcefile));
      for (int i = 0; i < parsedText.size(); i++)
      {
        String s = parsedText.get(i).toString();
        if (!(s.isEmpty()))
        {System.out.println(s);}
      }
    }
    //case3: both stopping and printing enable
    else if (args.length == 4)// invocation has the format: java index -s stoplist -p latimes
    {
      sourcefile = args[3];
      stoplistname = args[1];
      ArrayList<String> parsedandstoppedText =  removeStopWords(stoplistname, parser(readFileandsplitDoc(sourcefile)));
      for (int i = 0; i < parsedandstoppedText.size(); i++)
      {
        String s = parsedandstoppedText.get(i).toString();
        if (!(s.isEmpty()))
        {
          System.out.println(s);
        }
      }
    }
    //case4: stopping enable, no printing and create lexicon invlists map
    else if (args.length == 3)// invocation has the format: java index -s stoplist latimes
    {
      sourcefile = args[2];
      stoplistname = args[1];
      long startTime = System.nanoTime();
      createLexiInvlistsMap(sourcefile, stoplistname);
      long endTime = System.nanoTime();
      long duration = (endTime - startTime);
      System.out.println("Overall time:" + (duration/1000000));
    }
    //case5: user might enter wrong invocation
    else
    {
      System.out.println("The invocation might be in a wrong format");
    }
  }

/* SUPPORTING METHODS PART*/
public static void createLexiInvlistsMap(String sourcefile, String stoplistname)
                                          throws IOException
{
  /*Reading the stoplist file and initialize stoplist hashtable */
  Hashtable<String, Integer> stoplist = new Hashtable<String, Integer>();
  String sCurrentLine;
  FileReader fr = new FileReader(stoplistname);
  BufferedReader br = new BufferedReader(fr);
  int i = 0;
  while ((sCurrentLine = br.readLine()) != null)
  {
    stoplist.put(sCurrentLine.toLowerCase(), i);
    i++;
  }
  br.close();
  fr.close();

  /*Reading the sourcefile and gathering data for lexicon, invlist, map*/
  String[] docs = readFileandsplitDoc(sourcefile); //split the whole collection into seperate docs
  //note: the first element of the above array is empty since there's nothing before the first <DOC> tag.
  Hashtable<Integer, String> map = new Hashtable<Integer, String>();// map to keep DOCID and its unique document identifier <DOCNO>
  ArrayList<String> docnoslist = new ArrayList<>(); // arraylist to store all the unique document identifier <DOCNO>
  Hashtable<String, Integer> lexicon = new Hashtable<>();//hashtable to keep unique terms and its doc fre. key: each unique term, value: doc fre
  Hashtable<String, ArrayList<String>> keyvsPairDocIDandTF = new Hashtable<String, ArrayList<String>>();//hashtable to keep track of DOCID and TF. key: each unique term, value: arraylist of DocID and TF.

  for (int a = 1; a < docs.length; a++) //Go through each doc of the collection
  {
    /*Find the unique document identifier in the <DOCNO> tag*/
    Pattern pDocNo = Pattern.compile("<DOCNO> (\\S+) </DOCNO>", Pattern.MULTILINE);
    Matcher mDocNo = pDocNo.matcher(docs[a]);
    if (mDocNo.find())
    {
      String docno = mDocNo.group(1);
      docnoslist.add(docno); // add the DOCNO to an arraylist first
    }
    /*Extract the content data in the <HEADLINE> and <TEXT> tags of each doc*/
    ArrayList<String> cleanedTextremovedstopwords = new ArrayList<>(); //processed terms will be stored in arraylist of strings
    Pattern pContent = Pattern.compile("(?<=<HEADLINE>)([^\r]*)(?=</TEXT>)" );
    Matcher mContent = pContent.matcher(docs[a]);
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
    /*Loop through the arraylist of terms to:
    // building lexicon,
    // finding Document Frequency, DocID and Within-Doc-Frequency
    */
    Hashtable <String, Integer> keyvsInDocFre = new Hashtable<String, Integer>();
    //Hashtable to keep track of within-doc frequency for each term.
    // key: the unique content term, value: tf
    for (int k =0; k < cleanedTextremovedstopwords.size(); k++)//go thru each content term
    {
      String key = (cleanedTextremovedstopwords.get(k)).toString();
      if ((key.length() >0) && (!key.isEmpty())) //excludes the empty/spaces/null values
      {
        //case1: if the lexicon don't have the term yet
        if (!(lexicon.containsKey(key)))
        {
          lexicon.put(key, 1);
          keyvsInDocFre.put(key, 1);
        }
        // case2: if the lexicon had the term (maybe from previous doc) but the table that count Within-Doc Fre hasn't had the term
        else if ((lexicon.containsKey(key)) && (!(keyvsInDocFre.containsKey(key))))
        {
          keyvsInDocFre.put(key, 1);
          lexicon.put(key, lexicon.get(key) + 1);//update Doc Fre in lexicon table
        }
        // case3: if the term already existed in lexicon and within-docfre hashtable
        else if ((lexicon.containsKey(key)) && keyvsInDocFre.get(key) >= 1)
        {
          keyvsInDocFre.put(key, keyvsInDocFre.get(key) + 1);
        }
      }
    }

    /*Loop through the temporary hashtable that keep within-doc fre(tf) for each term
    and update the hashtable keyvsPairDocIDandTF that has key: each unique term, value: arraylist of DocID and TF */
    for (Object keyI: keyvsInDocFre.keySet())
    {
      if (!(keyvsPairDocIDandTF.containsKey(keyI)))
      {
        ArrayList<String> docIDandTF = new ArrayList<>();
        docIDandTF.add(intToBinary(a, 18));// change the docID to binary
        docIDandTF.add(intToBinary(keyvsInDocFre.get(keyI), 18)); // change the TF to binary
        keyvsPairDocIDandTF.put(keyI.toString(), docIDandTF);
      }
      else
      {
        (keyvsPairDocIDandTF.get(keyI)).add(intToBinary(a, 18));
        (keyvsPairDocIDandTF.get(keyI)).add(intToBinary(keyvsInDocFre.get(keyI), 18));
      }

    }
  }//end of looping through each doc.

  System.out.println("Lexicon size: " + lexicon.size());



  long startTime = System.nanoTime();
  /* Writing to the invlists file*/
  int fileoffsetpostion =0;
  FileWriter pwI = new FileWriter(new File("invlists"));
  for (Object key : lexicon.keySet())
  {
    String docFre = intToBinary(lexicon.get(key), 18); //obtain the doc fre for each term from the lexicon table and convert it to binary
    pwI.write(docFre);
    int m = (keyvsPairDocIDandTF.get(key)).size(); //the size of arraylist that keeps DOCID and TF
    for (int z = 0; z < m; z = z +2)
    {
      pwI.write(keyvsPairDocIDandTF.get(key).get(z));//first element is always DOCID
      pwI.write(keyvsPairDocIDandTF.get(key).get(z+1));// the following element is TF
    }
    lexicon.put(key.toString(), fileoffsetpostion); // the lexicon table now will not hold value as doc fre for each term anymore, it will hold the file offset position
    fileoffsetpostion = fileoffsetpostion + 1 + (keyvsPairDocIDandTF.get(key)).size();
    //if we encode each block of binary file as doc fre, docID and TF. This file offset position will keep track of how many blocks we need to skip to reach the position we want to read the binary
  }
  pwI.close();
  long endTime = System.nanoTime();
  long duration = (endTime - startTime);
  System.out.println("Duration for creating ivlists: "+ (duration/1000000));


  long startTimeL = System.nanoTime();
  /*Writing the lexicon file*/
  FileWriter pwL = new FileWriter(new File("lexicon"));
  for (Object key : lexicon.keySet())
  {
    pwL.write(key + "\t" + lexicon.get(key) + "\n");
    pwL.flush();
  }

  pwL.close();
  long endTimeL = System.nanoTime();
  long durationL = (endTimeL - startTimeL);
  System.out.println("Duration for printing lexicon: "+ (durationL/1000000));

  /*Initialize the map hashatble by looping through the araylist of unique document identifier <DOCNO>*/
  for (int n = 0; n < docnoslist.size(); n++)
  {
    map.put(n+1, (docnoslist.get(n)).toString()); // there won't be any doc has "0" as their DOCID. My map start from 1.
  }
  System.out.println("Map size: " + map.size());

  long startTimeM = System.nanoTime();
  /* Writing to the map file*/
  FileWriter pwM = new FileWriter(new File("map"));
  for (Object keyofMap : map.keySet())
  {
    pwM.write(keyofMap + "\t" + map.get(keyofMap) + "\n");
  }
  pwM.close();
  long endTimeM = System.nanoTime();
  long durationM = (endTimeM - startTimeM);
  System.out.println("Duration for printing map: "+ (durationM/1000000));

} // end of method createLexiInvlistsMap()

/*MORE ADDITIONAL METHODS*/
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


public static ArrayList<String> parser(String[] docs)
{
  ArrayList<String> words = new ArrayList<>();
  for  (String eachDoc : docs)
  {
    Pattern p = Pattern.compile("(?<=<HEADLINE>)([^\r]*)(?=</TEXT>)" );
    Matcher m = p.matcher(eachDoc);
    if (m.find())
    {
      String word = (((m.group(1).toString()).replaceAll("<.*>", "")).replaceAll("[^a-zA-z]", " ")).toLowerCase();
      String[] terms = word.split(" ");
      for (String term: terms)
      {
        words.add(term);
      }
    }
  }
  return words;
}

public static ArrayList<String> removeStopWords(String stoplistname, ArrayList<String> words)
{
  ArrayList<String> textremovedstopwords = new ArrayList<>();
  String sCurrentLine;
  Hashtable<String, Integer> stoplist = new Hashtable<String, Integer>();

  try
  {
    FileReader fr = new FileReader(stoplistname);
    BufferedReader br = new BufferedReader(fr);
    int i = 0;
    try
    {
      while ((sCurrentLine = br.readLine()) != null)
      {
        stoplist.put(sCurrentLine.toLowerCase(), i);
        i++;
      }
      for (String term : words)
      {
        if (!(stoplist.containsKey(term)))
        {
          textremovedstopwords.add(term);
        }
      }
      br.close();
      fr.close();
    }
    catch(IOException e)
    {
      System.out.println(e);
    }
  }

  catch(FileNotFoundException fnfe)
  {
    System.out.println(fnfe.getMessage());
  }
  return textremovedstopwords;
}

// method convert integer to fixed number of numBits
public static String intToBinary (int n, int numOfBits)
{
  String binary = "";
  for(int i = 0; i < numOfBits; ++i, n/=2) {
    switch (n % 2) {
      case 0:
      binary = "0" + binary;
      break;
      case 1:
      binary = "1" + binary;
      break;
    }
  }
  return binary;
}
}
