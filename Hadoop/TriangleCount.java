/**
 * Tugas Pengembangan Aplikasi Terdistribusi Hadoop MapReduce
 * 
 * 13515011 - Reinhard Benjamin Linardi
 * 13515057 - Erick Wijaya
 */

import java.io.IOException;
import java.util.ArrayList;
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

public class TriangleCount {

	public static final Text EMPTY_VAL = new Text("X");
	public static final Text DOLLAR_VAL = new Text("$");
	public static final Text TRIANGLE = new Text("Triangle");
	public static final IntWritable ONE = new IntWritable(1);

	public static class MapperPrep extends Mapper<Object, Text, Text, Text> {
		
		private Text keyOut = new Text();

		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
			StringTokenizer itr = new StringTokenizer(value.toString());
			String first = itr.nextToken();
			String second = itr.nextToken();

			keyOut.set(first + "," + second);
			context.write(keyOut, EMPTY_VAL);
			keyOut.set(second + "," + first);
			context.write(keyOut, EMPTY_VAL);
		}
	}

	public static class ReducerPrep extends Reducer<Text, Text, Text, Text> {

		public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
			context.write(key, EMPTY_VAL);
		}
	}

	public static class Mapper1 extends Mapper<Object, Text, IntWritable, IntWritable> {
		
		private IntWritable keyOut = new IntWritable();
		private IntWritable valOut = new IntWritable();

		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
			StringTokenizer itr = new StringTokenizer(value.toString());
			String realKey = itr.nextToken();

			itr = new StringTokenizer(realKey, ",");
			int u = Integer.parseInt(itr.nextToken());
			int v = Integer.parseInt(itr.nextToken());

			if(v > u) {
				keyOut.set(u);
				valOut.set(v);
				context.write(keyOut, valOut);
			}
		}
	}

	public static class Reducer1 extends Reducer<IntWritable, IntWritable, Text, Text> {
		
		private Text keyOut = new Text();
		private Text valOut = new Text();

		private ArrayList<Integer> list = new ArrayList<>(); 

		public void reduce(IntWritable key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
			list.clear();
		
			for(IntWritable value : values) {
				list.add(value.get());
			}

			for(int idx1 = 0; idx1 < list.size() - 1; idx1++) {
				for(int idx2 = idx1 + 1; idx2 < list.size(); idx2++) {
					keyOut.set(String.valueOf(key.get()));
					valOut.set(String.valueOf(list.get(idx1)) + "," + String.valueOf(list.get(idx2)));
					context.write(keyOut, valOut);
				}
			}
		}
	}

	public static class Mapper2 extends Mapper<Object, Text, Text, Text> {

		private Text keyOut = new Text();
		private Text valueOut = new Text();
		
		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
			StringTokenizer itr = new StringTokenizer(value.toString());

			String realKey = itr.nextToken();
			String realValue = itr.nextToken();

			if (realValue.equals(EMPTY_VAL.toString())) {
				keyOut.set(realKey);
				context.write(keyOut, DOLLAR_VAL);
			} else {
				keyOut.set(realValue);
				valueOut.set(realKey);
				context.write(keyOut, valueOut);
			}
		}
	}

	public static class Reducer2 extends Reducer<Text, Text, IntWritable, IntWritable> {
		
		private IntWritable keyOut = new IntWritable();

		public void reduce(Text key, Iterable<Text> values, Context context) 
		throws IOException, InterruptedException {
			boolean hasDollar = false;
			
			for(Text val : values) {
				if (val.toString().equals(DOLLAR_VAL.toString())) {
					hasDollar = true;
					break;
				}
			}

			if(hasDollar) {
				for (Text val : values) {
					if (!val.toString().equals(DOLLAR_VAL.toString())) {
						keyOut.set(Integer.parseInt(val.toString()));
						context.write(keyOut, ONE);
					}
				}		
			}
		}
	}

	public static class MapperFinal extends Mapper<Object, Text, Text, IntWritable> {
		
		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
			context.write(TRIANGLE, ONE);
		}
	}

	public static class ReducerFinal extends Reducer<Text, IntWritable, Text, IntWritable> {

		private int sum = 0;
		private IntWritable valOut = new IntWritable();

		public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
			values.forEach(val -> {
				sum += val.get();
			});

			valOut.set(sum);
			context.write(TRIANGLE, valOut);
		}
	}

	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();

		if(otherArgs.length < 2) {
			System.err.println("Usage: TriangleCount <in> [<in>...] <out>");
			System.exit(2);
		}

		Job jobPrep = Job.getInstance(conf, "Master of Java - Preprocess");
		jobPrep.setJarByClass(TriangleCount.class);
		jobPrep.setMapperClass(MapperPrep.class);
		jobPrep.setReducerClass(ReducerPrep.class);
		jobPrep.setMapOutputKeyClass(Text.class);
		jobPrep.setMapOutputValueClass(Text.class);
		jobPrep.setOutputKeyClass(Text.class);
		jobPrep.setOutputValueClass(Text.class);
		
		for(int i = 0; i < otherArgs.length - 1; i++) {
		  	FileInputFormat.addInputPath(jobPrep, new Path(otherArgs[i]));
		}
		
		FileOutputFormat.setOutputPath(jobPrep, new Path(otherArgs[otherArgs.length - 1], "prep"));
		jobPrep.waitForCompletion(true);
		//System.exit(jobPrep.waitForCompletion(true) ? 0 : 1);

		Job job1 = Job.getInstance(conf, "Master of Java - MapReduce 1");
		job1.setJarByClass(TriangleCount.class);
		job1.setMapperClass(Mapper1.class);
		job1.setReducerClass(Reducer1.class);
		job1.setMapOutputKeyClass(IntWritable.class);
		job1.setMapOutputValueClass(IntWritable.class);
		job1.setOutputKeyClass(Text.class);
		job1.setOutputValueClass(Text.class);
		FileInputFormat.addInputPath(job1, new Path(otherArgs[otherArgs.length - 1], "prep"));
		FileOutputFormat.setOutputPath(job1, new Path(otherArgs[otherArgs.length - 1], "mapred1"));
		job1.waitForCompletion(true);
		//System.exit(job1.waitForCompletion(true) ? 0 : 1);

		Job job2 = Job.getInstance(conf, "Master of Java - MapReduce 2");
		job2.setJarByClass(TriangleCount.class);
		job2.setMapperClass(Mapper2.class);
		job2.setReducerClass(Reducer2.class);
		job2.setMapOutputKeyClass(Text.class);
		job2.setMapOutputValueClass(Text.class);
		job2.setOutputKeyClass(IntWritable.class);
		job2.setOutputValueClass(IntWritable.class);
		FileInputFormat.addInputPath(job2, new Path(otherArgs[otherArgs.length - 1], "prep"));
		FileInputFormat.addInputPath(job2, new Path(otherArgs[otherArgs.length - 1], "mapred1"));
		FileOutputFormat.setOutputPath(job2, new Path(otherArgs[otherArgs.length - 1], "mapred2"));
		job2.waitForCompletion(true);
		//System.exit(job2.waitForCompletion(true) ? 0 : 1);

		Job jobFinal = Job.getInstance(conf, "Master of Java - Final TriangleCount");
		jobFinal.setJarByClass(TriangleCount.class);
		jobFinal.setMapperClass(MapperFinal.class);
		jobFinal.setReducerClass(ReducerFinal.class);
		jobFinal.setMapOutputKeyClass(Text.class);
		jobFinal.setMapOutputValueClass(IntWritable.class);
		jobFinal.setOutputKeyClass(Text.class);
		jobFinal.setOutputValueClass(IntWritable.class);
		FileInputFormat.addInputPath(jobFinal, new Path(otherArgs[otherArgs.length - 1], "mapred2"));
		FileOutputFormat.setOutputPath(jobFinal, new Path(otherArgs[otherArgs.length - 1], "final"));
		System.exit(jobFinal.waitForCompletion(true) ? 0 : 1);
	}
}
