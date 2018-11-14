/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package reinhard.erick.hadoop;

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

public class TriangleCount {

	public static class Mapper1 extends Mapper<Object, Text, Text, IntWritable> {
		
		private final static IntWritable one = new IntWritable(1);

		private Text word = new Text();

		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
			StringTokenizer itr = new StringTokenizer(key.toString());
			String U = itr.nextToken();
			String V = itr.nextToken();
			if (Integer.parseInt(U) > Integer.parseInt(V)) {
				word.set(U + ";" + V);
			} else {
				word.set(V + ";" + U);
			}
			context.write(word, one);
		}
	}

	public static class Reducer1 extends Reducer<Text, IntWritable, Text, Text> {
		
		private Text valOut = new Text("#");

		public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
			context.write(key, valOut);
		}
	}

	public static class Mapper2 extends Mapper<Object, Text, IntWritable, IntWritable> {
		
		private IntWritable keyOut = new IntWritable();
		
		private IntWritable valOut = new IntWritable();

		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
			StringTokenizer itr = new StringTokenizer(value.toString());
			String token = itr.nextToken();
			String[] nums = token.split(";");
			keyOut.set(Integer.parseInt(nums[0]));
			valOut.set(Integer.parseInt(nums[1]));
			context.write(keyOut, valOut);
		}
	}

	public static class Reducer2 extends Reducer<IntWritable, IntWritable, Text, Text> {
		
		private Text keyOut = new Text();
		
		private Text valOut = new Text();

		public void reduce(IntWritable key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
			ArrayList<Integer> listValues = new ArrayList<Integer>();
			values.forEach(val -> listValues.add(new Integer(val.get())));
			listValues.forEach(val1 ->
				listValues.forEach(val2 -> {
					if (val1 < val2) {
						keyOut.set(Integer.toString(key.get()));
						valOut.set(val1.toString() + ";" + val2.toString());
						context.write(keyOut, valOut);
					}
				})
			);
		}
	}

	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();

		if (otherArgs.length < 2) {
			System.err.println("Usage: wordcount <in> [<in>...] <out>");
			System.exit(2);
		}

		Job job = Job.getInstance(conf, "word count");
		job.setJarByClass(TriangleCount.class);
		job.setMapperClass(PreprocessMapper.class);
		//job.setCombinerClass(IntSumReducer.class);
		//job.setReducerClass(IntSumReducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);

		for(int i = 0; i < otherArgs.length - 1; ++i) FileInputFormat.addInputPath(job, new Path(otherArgs[i]));
		FileOutputFormat.setOutputPath(job, new Path(otherArgs[otherArgs.length - 1]));
		System.exit(job.waitForCompletion(true)? 0 : 1);
	}
}
