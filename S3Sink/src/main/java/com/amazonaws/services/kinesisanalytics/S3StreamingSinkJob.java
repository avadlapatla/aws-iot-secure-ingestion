package com.amazonaws.services.kinesisanalytics;

import java.util.Date;
import java.text.SimpleDateFormat;
import org.json.JSONObject;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.core.io.SimpleVersionedSerializer;
import org.apache.flink.streaming.api.functions.sink.filesystem.bucketassigners.SimpleVersionedStringSerializer;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.kinesis.FlinkKinesisConsumer;
import org.apache.flink.streaming.connectors.kinesis.config.ConsumerConfigConstants;
import org.apache.flink.api.common.serialization.SimpleStringEncoder;
import org.apache.flink.core.fs.Path;
import org.apache.flink.streaming.api.functions.sink.filesystem.StreamingFileSink;
import org.apache.flink.streaming.api.functions.sink.filesystem.BucketAssigner;
import org.apache.flink.streaming.api.functions.sink.filesystem.bucketassigners.DateTimeBucketAssigner;
import org.apache.flink.util.Collector;


import java.util.Properties;

import org.apache.flink.streaming.api.functions.sink.filesystem.rollingpolicies.DefaultRollingPolicy;
import org.apache.flink.streaming.api.functions.sink.filesystem.bucketassigners.DateTimeBucketAssigner;

class StockTickerBucketAssigner implements BucketAssigner<String, String>{
    
    public String convertTime(long time){
        Date date = new Date(time);
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd/HH");
        return format.format(date);
    }

    public String getTicker(String value){
      String token = "TICKER";
      JSONObject json = new JSONObject(value);
      String ticker = json.get(token).toString();
      return ticker;
    }

    @Override
    public String getBucketId(String element, BucketAssigner.Context context) {
        String eventTime = convertTime(context.currentProcessingTime());
        String token = getTicker(element);
        return token + "/" + eventTime;
    }
    
    @Override
    public SimpleVersionedSerializer<String> getSerializer() {
        return SimpleVersionedStringSerializer.INSTANCE;
  }
}



public class S3StreamingSinkJob {
    private static final String region = "us-west-2";
    private static final String inputStreamName = "ExampleInputStream";
    private static final String s3SinkPath = "s3a://ka-app-k8s/data";

    private static DataStream<String> createSourceFromStaticConfig(StreamExecutionEnvironment env) {

        Properties inputProperties = new Properties();
        inputProperties.setProperty(ConsumerConfigConstants.AWS_REGION, region);
        inputProperties.setProperty(ConsumerConfigConstants.STREAM_INITIAL_POSITION,"LATEST");
        return env.addSource(new FlinkKinesisConsumer<>(inputStreamName, new SimpleStringSchema(), inputProperties));
    }

    private static StreamingFileSink<String> createS3SinkFromStaticConfig() {

        final StreamingFileSink<String> sink = StreamingFileSink
        .forRowFormat(new Path(s3SinkPath), new SimpleStringEncoder<String>("UTF-8"))
        //.withBucketAssigner(new DateTimeBucketAssigner("yyyy-MM-dd--HH"))
        .withBucketAssigner(new StockTickerBucketAssigner())
        .withRollingPolicy(DefaultRollingPolicy.create().build())
        .build();
        return sink;
    }

    public static void main(String[] args) throws Exception {

        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        DataStream<String> input = createSourceFromStaticConfig(env);

        input.addSink(createS3SinkFromStaticConfig());

        env.execute("Flink S3 Streaming Sink Job");
    }
}