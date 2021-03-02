This repo has instructions on how to use IOT Credential helper to provision temporary credentials and then use those credentials to ingest data into Kinesis and use Kinesis Data Analytics for Apache Flink to consume & partition the data and store it in S3. The Athena dynamic parition instructions provides an example to create dynamic parition,

Replace the below,

ACCOUTNO with AWS Account Number
S3BUCKET with S3 bucket for data
S3BUCKETAPP with Kinesis Flink code bucket
IOTURL with IOT Url from AWS IOT Console

aws iot create-thing-type --thing-type-name kvs_athena > iot-thing-type.json

aws iot create-thing --thing-name kvs_athena_1 --thing-type-name kvs_athena  > iot-thing.json
 
aws iam create-role --role-name kvs_athena --assume-role-policy-document 'file://iam-policy-document.json' > iam-role.json

aws iam put-role-policy --role-name kvs_athena --policy-name kvs_athena --policy-document 'file://iam-permission-document.json' 

aws iot create-role-alias --role-alias kvs_athena_alias --role-arn arn:aws:iam:::role/kvs_athena --credential-duration-seconds 3600 > iot-role-alias.json

aws iot create-policy --policy-name kvs_athena --policy-document 'file://iot-policy-document.json' 

aws iot create-keys-and-certificate --set-as-active --certificate-pem-outfile certificate.pem --public-key-outfile public.pem.key --private-key-outfile private.pem.key > certificate

aws iot attach-policy --policy-name kvs_athena --target $(jq --raw-output '.certificateArn' certificate)

aws iot attach-thing-principal --thing-name kvs_athena_1 --principal $(jq --raw-output '.certificateArn' certificate)

aws iot describe-endpoint --endpoint-type iot:CredentialProvider --output text > iot-credential-provider.txt

curl --silent 'https://www.amazontrust.com/repository/SFSRootCAG2.pem' --output cacert.pem
        
curl --silent -H "x-amzn-iot-thingname:kvs_athena_1" --cert certificate.pem --key private.pem.key {IOT_URL} --cacert ./cacert.pem > token.json

export AWS_ACCESS_KEY_ID=$(jq --raw-output '.credentials.accessKeyId' token.json) 
export AWS_SECRET_ACCESS_KEY=$(jq --raw-output '.credentials.secretAccessKey' token.json) 
export AWS_SESSION_TOKEN=$(jq --raw-output '.credentials.sessionToken' token.json)

aws kinesis create-stream --stream-name ExampleInputStream --shard-count 1 --region us-west-2

Start ingesting data using stock.py sample app

Create Kinesis Data Analytics for Apache Flink using the provided S3Sink example

Create Athena table using dynamic parititons to query the data automatically.