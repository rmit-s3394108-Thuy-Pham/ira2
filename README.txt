Student1: Nhu Anh Thuy Pham - s3394108
Student2: Amy Michelle Blume - s3623074

First, please compile the index.java by:
javac index.java 

Then, the program will be run by 4 different invocations:

(1) java index /home/inforet/a1/latimes  
->>> This one will enable the parsing method, but no printing

(2) java index -p /home/inforet/a1/latimes 
->>> This one will enable the parsing method, printing all the content terms, no stopping enable

(3) java index -s stoplist -p /home/inforet/a1/latimes 
->>> This one will enable the parsing method, enable the stopping method, printing all the content terms (not including stopwords)
 
(4) java index -s stoplist /home/inforet/a1/latimes 
OR
(5) java index -s stoplist latimes
->> These ones will be the only invocation that enables the lexicon, invlists and map creation. If you want to test our program, those are recommended ones to use.


With the query searching, please compile the search.java by:
javac search.java

Then, the program will run by:
java search lexicon invlists map <queryterm_1> <queryterm_2> [...<queryterm_N>