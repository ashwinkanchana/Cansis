import json
import boto3
import os
from datetime import datetime, timezone
import urllib.request
import http.client
import mimetypes


def lambda_handler(event, context):
    try:    
            url = 'ANNOUNCEMENTS_ENDPOINT'
            with urllib.request.urlopen(url) as stream:
                inputStream = stream.read()

            currentJsonString = inputStream.decode("utf-8")

            currentJson = json.loads(currentJsonString)


            dynamoDB = boto3.resource('dynamodb')
            table = dynamoDB.Table('circulars')
            response = table.get_item(
                Key={
                    'index': 1
                }
            )
            item = response['Item']

           
            previousJson = json.loads(item['json'])
        
        
            previousTitles = [listOld['title'] for listOld in previousJson['result']]
            currentTitles = [listNew['title'] for listNew in currentJson['result']]
        
            updatedTitles = list(set(currentTitles) - set(previousTitles))
        

           
            utc_dt = datetime.now(timezone.utc) 
            timestamp = str(utc_dt.astimezone())
            

            table.update_item(
                Key={
                    'index': 1
                },
                UpdateExpression='SET json = :latestJson, last_updated = :timestamp',
                ExpressionAttributeValues={
                    ':latestJson': currentJsonString,
                    ':timestamp':timestamp
                }
            )

           

            for i in updatedTitles:
                updatedNotice = {}
                for notice in currentJson['result']: 
                    if notice['title'] == i: 
                        updatedNotice = notice 
                        break
                body = updatedNotice['contents']

                payload = "{\"to\": \"/topics/announcements\",\"data\": {\"title\": \""+i+"\",\"body\" : \""+body+"\"}}"
                print('payload-',payload)
                conn = http.client.HTTPSConnection("fcm.googleapis.com")
                headers = {
                'Authorization': 'FCM_KEY_GOES_HERE',
                'Content-Type': 'application/json',
                'Content-Type': 'application/json'
                }
                conn.request("POST", "/fcm/send", payload, headers)
                res = conn.getresponse()
                data = res.read()

            res = 'Success'
    except Exception as e:
            res = 'Failure'
            print(e)
    finally:
        return {
            'statusCode': 200,
            'body': json.dumps(res)
        }
