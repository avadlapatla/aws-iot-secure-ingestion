import json
import boto3
import random
import datetime
import requests

###curl --silent -H "x-amzn-iot-thingname:kvs_athena_1" --cert certificate.pem --key private.pem.key  --cacert ./cacert.pem > token.json

kinesis = boto3.client('kinesis')

URL = {REPLACE_WITH_IOT_CREDENTIALS_URL}
cert=('/path/client.cert', '/path/client.key')


def getCredentials():


def getReferrer():
    data = {}
    now = datetime.datetime.now()
    str_now = now.isoformat()
    data['EVENT_TIME'] = str_now
    data['TICKER'] = random.choice(['AAPL', 'AMZN', 'MSFT', 'INTC', 'TBV'])
    price = random.random() * 100
    data['PRICE'] = round(price, 2)
    return data

while True:
        data = json.dumps(getReferrer())
        print(data)
        kinesis.put_record(
                StreamName="ExampleInputStream",
                Data=data,
                PartitionKey="partitionkey")