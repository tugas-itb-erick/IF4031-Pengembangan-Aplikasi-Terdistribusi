/**
	13515011 - Reinhard Benyamin L
	13515057 - Erick Wijaya
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

	public static class Mapper1 extends Mapper<Object, Text, IntWritable, IntWritable> {
		
		private IntWritable keyOut = new IntWritable();

		private IntWritable valOut = new IntWritable();

		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
			StringTokenizer itr = new StringTokenizer(key.toString());
			int u = Integer.parseInt(itr.nextToken());
			int v = Integer.parseInt(itr.nextToken());
			if (v > u) {
				keyOut.set(u);
				valOut.set(v);
			} else {
				keyOut.set(v);
				valOut.set(u);
			}
			context.write(keyOut, valOut);
		}
	}

	public static class Reducer1 extends Reducer<IntWritable, IntWritable, IntWritable, Text> {
		
		private Text valOut = new Text();

		public void reduce(IntWritable key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
			values.forEach(val1 -> {
				values.forEach(val2 -> {
					if (val1.get() < val2.get()) {
						valOut.set(val1.toString() + "," + val2.toString());
						try {
							context.write(key, valOut);
						} catch (IOException | InterruptedException e) {
							e.printStackTrace();
						}
					}
				});
			});
		}
	}

	// public static class Mapper1 extends Mapper<Object, Text, IntWritable, IntWritable> {
		
	// 	private IntWritable keyOut = new IntWritable();
		
	// 	private IntWritable valOut = new IntWritable();

	// 	public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
	// 		StringTokenizer itr = new StringTokenizer(value.toString());
	// 		String token = itr.nextToken();
	// 		String[] nums = token.split(";");
	// 		keyOut.set(Integer.parseInt(nums[0]));
	// 		valOut.set(Integer.parseInt(nums[1]));
	// 		context.write(keyOut, valOut);
	// 	}
	// }

	// public static class Reducer1 extends Reducer<IntWritable, IntWritable, Text, Text> {
		
	// 	private Text keyOut = new Text();
		
	// 	private Text valOut = new Text();

	// 	public void reduce(IntWritable key, Iterable<IntWritable> values, Context context) 
	// 	throws IOException, InterruptedException {
	// 		ArrayList<Integer> listValues = new ArrayList<Integer>();
	// 		values.forEach(val -> listValues.add(new Integer(val.get())));
	// 		for (Integer val1: listValues) {
	// 			for (Integer val2: listValues) {
	// 				if (val1 < val2) {
	// 					keyOut.set(Integer.toString(key.get()));
	// 					valOut.set(val1.toString() + ";" + val2.toString());
	// 					context.write(keyOut, valOut);
	// 				}
	// 			}
	// 		}
	// 	}
	// }

	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();

		if (otherArgs.length < 2) {
			System.err.println("Usage: wordcount <in> [<in>...] <out>");
			System.exit(2);
		}

		Job job1 = Job.getInstance(conf, "Preprocess");
		job1.setJarByClass(TriangleCount.class);
		job1.setMapperClass(Mapper1.class);
		job1.setReducerClass(Reducer1.class);
		job1.setMapOutputKeyClass(IntWritable.class);
		job1.setMapOutputValueClass(IntWritable.class);
		job1.setOutputKeyClass(IntWritable.class);
		job1.setOutputValueClass(Text.class);
		for (int i = 0; i < otherArgs.length - 1; i++) {
		  	FileInputFormat.addInputPath(job1, new Path(otherArgs[i]));
		}
		FileOutputFormat.setOutputPath(job1, new Path(otherArgs[otherArgs.length - 1], "out1"));
		if (!job1.waitForCompletion(true)) {
		  	System.exit(1);
		}

		// Job job1 = Job.getInstance(conf, "MapReduce 1");
		// job1.setJarByClass(TriangleCount.class);
		// job1.setMapperClass(Mapper1.class);
		// job1.setReducerClass(Reducer1.class);
		// job1.setMapOutputKeyClass(IntWritable.class);
		// job1.setMapOutputValueClass(IntWritable.class);
		// job1.setOutputKeyClass(Text.class);
		// job1.setOutputValueClass(Text.class);
		// FileInputFormat.addInputPath(job1, new Path(otherArgs[otherArgs.length - 1], "out1"));
		// FileOutputFormat.setOutputPath(job1, new Path(otherArgs[otherArgs.length - 1], "out2"));
		// System.exit(job1.waitForCompletion(true) ? 0 : 1);
	}
}
