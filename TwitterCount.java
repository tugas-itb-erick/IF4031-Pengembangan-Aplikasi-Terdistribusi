/**
  Tugas 8 Pengembangan Aplikasi Terdistribusi
  13515017 - Putu Arya Pradipta
  13515107 - Roland Hartanto
 */

import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import java.util.ArrayList;

public class TwitterCount {

  public static class MapPreprocessing
    extends Mapper<Object, Text, Text, IntWritable>{

      private final static IntWritable one = new IntWritable(1);
      private Text word = new Text();
      
      public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
        StringTokenizer itr = new StringTokenizer(value.toString());
        // while (itr.hasMoreTokens()) {
        String first = itr.nextToken();
        String second = itr.nextToken();
        if (Integer.parseInt(first) < Integer.parseInt(second)) {
          word.set(first + "," + second);
        } else {
          word.set(second + "," + first);
        }
        context.write(word, one);
        // }
      }

    }
  public static class ReducerPreprocessing 
       extends Reducer<Text,IntWritable,Text,Text> {
    private Text result = new Text("∅");

    public void reduce(Text key, Iterable<IntWritable> values, 
                       Context context
                       ) throws IOException, InterruptedException {
      context.write(key, result);
    }
  }

  public static class Map1
    extends Mapper<Object, Text, IntWritable, IntWritable>{

      private IntWritable first = new IntWritable();
      private IntWritable second = new IntWritable();
      public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
        StringTokenizer itr = new StringTokenizer(value.toString());
        String firstContent = itr.nextToken();
        String[] arr = firstContent.split(",");
        first.set(Integer.parseInt(arr[0]));
        second.set(Integer.parseInt(arr[1]));
        context.write(first,second);
      }
    }

  public static class Reducer1 
    extends Reducer<IntWritable,IntWritable,Text,Text> {
      private Text result = new Text();
      private Text first = new Text();
      public void reduce(IntWritable key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
        // copy to list
        ArrayList<Integer> listval = new ArrayList<Integer>();
        for (IntWritable val : values) {
          listval.add(new Integer(val.get()));
        }

        for (Integer val1 : listval) {
          for (Integer val2 : listval) {
            if(val1 < val2){
              result.set(val1.toString() + "," + val2.toString());
              // result.set(val1.toString());
              first.set(Integer.toString(key.get()));
              context.write(first, result);
            }
          }
        }
      }
    }

  public static class Map2
    extends Mapper<Object, Text, Text, Text>{
      
      private Text dollar = new Text("$");
      private Text return1 = new Text();
      private Text return2 = new Text();
      public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
        StringTokenizer itr = new StringTokenizer(value.toString());
        String first = itr.nextToken();
        String second = itr.nextToken();
        if (second.toString().equals("∅")) {
          return1.set(first);
          context.write(return1, dollar);
        } else {
          return1.set(first);
          return2.set(second);
          context.write(return2, return1);
        }
      }
    }

  public static class Reducer2 
    extends Reducer<Text,Text,IntWritable,IntWritable> {

      private IntWritable first = new IntWritable();
      private IntWritable second = new IntWritable(0);
      public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
        boolean havedollar = false;
        for (Text val: values) {
          if (val.toString().equals("$")){
            havedollar = true;
          }
        }
        if (havedollar) {
          for (Text val: values) {
            if (!val.toString().equals("$")) {
              first.set(Integer.parseInt(val.toString()));
              context.write(first, second);
            }
          }
        }
      }
    }
  

  public static void main(String[] args) throws Exception {
    Configuration conf = new Configuration();
    String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
    if (otherArgs.length < 2) {
      System.err.println("Usage: wordcount <in> [<in>...] <out>");
      System.exit(2);
    }
    
    Job job0 = Job.getInstance(conf, "preprocess");
    job0.setJarByClass(TwitterCount.class);
    job0.setMapperClass(MapPreprocessing.class);
    job0.setReducerClass(ReducerPreprocessing.class);
    job0.setMapOutputKeyClass(Text.class);
    job0.setMapOutputValueClass(IntWritable.class);
    job0.setOutputKeyClass(Text.class);
    job0.setOutputValueClass(Text.class);
    for (int i = 0; i < otherArgs.length - 1; ++i) {
      FileInputFormat.addInputPath(job0, new Path(otherArgs[i]));
    }
    FileOutputFormat.setOutputPath(job0,
      new Path(otherArgs[otherArgs.length - 1], "out1"));
    if (!job0.waitForCompletion(true)) {
      System.exit(1);
    }

    // mapreduce 1 
    Job job1 = Job.getInstance(conf, "mapreduce 1");
    job1.setJarByClass(TwitterCount.class);
    job1.setMapperClass(Map1.class);
    job1.setReducerClass(Reducer1.class);
    job1.setMapOutputKeyClass(IntWritable.class);
    job1.setMapOutputValueClass(IntWritable.class);
    job1.setOutputKeyClass(Text.class);
    job1.setOutputValueClass(Text.class);
    FileInputFormat.addInputPath(job1, new Path(otherArgs[otherArgs.length - 1], "out1"));
    FileOutputFormat.setOutputPath(job1, new Path(otherArgs[otherArgs.length - 1], "out2"));
    System.exit(job1.waitForCompletion(true) ? 0 : 1);
    
    // // mapreduce 2
    // Job job2 = Job.getInstance(conf, "mapreduce 2");
    // job2.setJarByClass(TwitterCount.class);
    // job2.setMapperClass(Map2.class);
    // job2.setReducerClass(Reducer2.class);
    // job2.setMapOutputKeyClass(Text.class);
    // job2.setMapOutputValueClass(Text.class);
    // job2.setOutputKeyClass(IntWritable.class);
    // job2.setOutputValueClass(IntWritable.class);
    // FileInputFormat.addInputPath(job2, new Path(otherArgs[otherArgs.length - 1], "out2"));
    // FileInputFormat.addInputPath(job2, new Path(otherArgs[otherArgs.length - 1], "out1"));
    // FileOutputFormat.setOutputPath(job2, new Path(otherArgs[otherArgs.length - 1], "out3"));
    
    // System.exit(job2.waitForCompletion(true) ? 0 : 1);
  }

}