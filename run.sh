../../bin/hdfs dfs -rmr /user/wijayaerick/out1
hadoop com.sun.tools.javac.Main TriangleCount.java
jar cf tc.jar TriangleCount*.class
hadoop jar tc.jar TriangleCount $1 $2
