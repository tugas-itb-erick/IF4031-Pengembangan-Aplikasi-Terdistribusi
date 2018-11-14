hadoop com.sun.tools.javac.Main TriangleCount.java
jar cf tc.jar TriangleCount*.class
hadoop jar tc.jar TriangleCount /user/wijayaerick/input_twit /user/wijayaerick/out1
