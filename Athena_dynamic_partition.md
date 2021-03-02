CREATE EXTERNAL TABLE IF NOT EXISTS default.my_ticker (
    event_time string,
    ticker string,
    price float
)
PARTITIONED BY
(
 ticker_name string,
 datehour string
)
ROW FORMAT SERDE 'org.openx.data.jsonserde.JsonSerDe'
LOCATION 's3://S3BUCKET/data/'
TBLPROPERTIES
(
 "projection.enabled" = "true",
 "projection.ticker_name.type" = "injected",
 "projection.datehour.type" = "date",
 "projection.datehour.range" = "2018/01/01/00,NOW",
 "projection.datehour.format" = "yyyy/MM/dd/HH",
 "projection.datehour.interval" = "1",
 "projection.datehour.interval.unit" = "HOURS",
 "storage.location.template" = "s3://S3BUCKET/data/${ticker_name}/${datehour}"
);


CREATE EXTERNAL TABLE `my_ticker`(
  `event_time` string COMMENT 'from deserializer', 
  `ticker` string COMMENT 'from deserializer', 
  `price` float COMMENT 'from deserializer')
PARTITIONED BY ( 
  `ticker_name` string, 
  `datehour` string)
ROW FORMAT SERDE 
  'org.openx.data.jsonserde.JsonSerDe' 
STORED AS INPUTFORMAT 
  'org.apache.hadoop.mapred.TextInputFormat' 
OUTPUTFORMAT 
  'org.apache.hadoop.hive.ql.io.IgnoreKeyTextOutputFormat'
LOCATION
  's3://S3BUCKET/data'
TBLPROPERTIES (
  'projection.datehour.format'='yyyy/MM/dd/HH', 
  'projection.datehour.interval'='1', 
  'projection.datehour.interval.unit'='HOURS', 
  'projection.datehour.range'='2018/01/01/00,NOW', 
  'projection.datehour.type'='date', 
  'projection.enabled'='true', 
  'projection.ticker_name.type'='injected', 
  'storage.location.template'='s3://S3BUCKET/data/${ticker_name}/${datehour}', 
  'transient_lastDdlTime'='1600318726')