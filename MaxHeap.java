

import java.util.*;
public class MaxHeap {

    int results;
    private double [] heap;
    private String [] terms;

    public MaxHeap(Hashtable<String, Double> hash, int results)
    {
        this.results = results;
        heap = new double [results+1];
        terms = new String [results+1];
        int i =1;
        heap[0]= Double.MAX_VALUE;
        terms[0]= "a";
        for (Object key : hash.keySet())
        {
           double TSV = hash.get(key);
           if(i < heap.length){
            heap[i] = TSV;
            terms[i] = key.toString();
            int current = i;
            while (heap[current] >= heap[parent(current)])
            {
                swap(current, parent(current));
                swapTerms(current, parent(current));
                current = parent(current);
            }
                i++;
            }
            else
            {
            if (TSV < heap[1]) {
                heap[1] = TSV;
                terms[1] = key.toString();
                maxHeapify(1);
            }
        }
    }
}

  public double[] getHeap(){
     return this.heap;
   }

  public String [] getTerms(){
      return this.terms;
  }

  public  void swapTerms(int fpos, int spos){
    String temp;
     temp = terms[fpos];
     terms[fpos] = terms[spos];
     terms[spos] = temp;
  }


   public void swap(int fpos, int spos) {
      double temp = heap[fpos];
      heap[fpos] = heap[spos];
      heap[spos] = temp;
    }

    public int parent(int pos)
    {
        return pos / 2;
    }

    public void maxHeapify(int pos) {

      int leftChild = 2 * pos;
      int rightChild = 2 * pos + 1;

      if(!isLeaf(pos))
      {
        if (rightChild == heap.length)
        {
          if (heap[pos] < heap[leftChild])
          {
            swap(pos, leftChild);
            swapTerms(pos, leftChild);
          }
        }
        else
        {
          if ( heap[pos] < heap[leftChild]  || heap[pos] < heap[rightChild])
          {

            if (heap[leftChild] > heap[rightChild])
            {
              swap(pos, leftChild);
              swapTerms(pos, leftChild);
              maxHeapify(leftChild);
            }
            else
            {
              swap(pos, rightChild);
              swapTerms(pos, rightChild);
              maxHeapify(rightChild);
            }
          }
        }
      }
    }
   public boolean isLeaf(int p)
   {
     if (p >=  (((double)(heap.length)) /  2)  &&  p <= heap.length)
     {
    return true;
     }
    return false;
  }
}
