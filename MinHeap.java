import java.util.*;
public class MinHeap
{
private static double[] heap;
private static String[] docIDArray;
public int num_results;
public MinHeap(Hashtable<String, Double> hash, int num_results)
{

  this.num_results = num_results;
  this.heap = new double[num_results + 1];
  this.docIDArray = new String[num_results + 1];
  heap[0] = 0;
  docIDArray[0] = "a";
  int i = 1;
  int k = 1;
  for (Object key: hash.keySet())
  {
    double BMvalue = hash.get(key);
    if (i < heap.length)
    {
      heap[i] = BMvalue;
      docIDArray[i] = key.toString();
      int current = i;
      while (heap[current] <= heap[parent(current)])
      {
        swap(current, parent(current));
        swapID(current, parent(current));
        current = parent(current);
      }
      i = i + 1;

    }
    else
    {

      if (BMvalue > heap[1])
      {
        heap[1] = BMvalue;
        docIDArray[1] = key.toString();
        minHeapipy(1);
      }
    }
  }

}
public double[] getHeap(){
  return this.heap;
}
public String[] getDocIDarray(){
  return this.docIDArray;
}

public static int parent(int pos)
{
  return pos/2;
}

public static void swap(int fpos, int tpos)
{
  double tmp;
  tmp = heap[fpos];
  heap[fpos] = heap[tpos];
  heap[tpos] = tmp;
}
public static int rightChild(int p)
{
  return p*2 + 1;
}
public static int leftChild(int p)
{
  return p*2;
}
public static void swapID(int fpos, int tpos)
{
  String tmp;
  tmp = docIDArray[fpos];
  docIDArray[fpos] = docIDArray[tpos];
  docIDArray[tpos] = tmp;
}

public static boolean isLeaf(int p)
{
  if (p >=  (((double)(heap.length)) /  2)  &&  p <= heap.length)
  {
    return true;
  }
  return false;
}

public static void minHeapipy(int k)
{
  if (!isLeaf(k))
  {
    if (rightChild(k) == (heap.length))
    {
      if (heap[k] > heap[leftChild(k)])
      {
        swap(k, leftChild(k));
        swapID(k, leftChild(k));
      }
    }
    else
    {
      if ( heap[k] > heap[leftChild(k)]  || heap[k] > heap[rightChild(k)])
      {
        if (heap[leftChild(k)] < heap[rightChild(k)])
        {
          swap(k, leftChild(k));
          swapID(k, leftChild(k));
          minHeapipy(leftChild(k));
        }
        else
        {
          swap(k, rightChild(k));
          swapID(k, rightChild(k));
          minHeapipy(rightChild(k));
        }
      }
    }
  }

}
}
