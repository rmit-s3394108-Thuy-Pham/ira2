Student1: Nhu Anh Thuy Pham - s3394108
Student2: Amy Michelle Blume - s3623074

First, please compile the index.java by:
javac index.java 

Then, the program will be run by 2 different invocations:

(1) java index -s stoplist /home/inforet/a2/latimes 
OR
(2) java index -s stoplist latimes



With the normal query searching, please compile the search.java by:
javac search.java

Then, the program will run by invocation with format:
java search -BM25 -q <querylabel> -n <num-results> -l lexicon -i invlists -m map [-s stoplist] <queryterm1> <queryterm2> ..

i.e:
java search -BM25 -q 402 -n 20 -l lexicon -i invlists -m map -s stoplist behavioral genetics
java search -BM25 -q 401 -n 20 -l lexicon -i invlists -m map foreign minorities germany


With the automatic query expansion search, please compile the searchAutomatic.java by:
javac searchAutomatic.java

Then, the program will run by invocation with format:
java searchAutomatic -BM25 -q <querylabel> -n <num-results> -l lexicon -i invlists -m map -s stoplist -R <top R docs> -E <num of additional terms> <queryterm1> <queryterm2> ..

i.e:
java searchAutomatic -BM25 -q 401 -n 20 -l lexicon -i invlists -m map -s stoplist -R 10 -E 25 foreign minorities germany