1. run a single test

mvn -Dtest=DocumentHandlerTest -DfailIfNoTests=false -P social-api test
mvn -Dtest=DocumentHandlerTest#HandleGetPlural -DfailIfNoTests=false -P social-api test

2. compile without tests
mvn -Dmaven.test.skip -P graasp

3. debug
Eclipse

Commands
========

APIs to create and get assets from graasp with attachments via opensocial.
Deployed to graaspdev.epfl.ch server.

CURL examples.
1. Create asset in a space with "parentId":707 and attach file "photo_evgeny.jpg" to it.

The security token contains the id of the user st=3:3:3:3:3:3:3 on behalf of which
the action is performed, normally this token should be encrypted, but for now it is not.

curl -i -H "Content-Type: multipart/form-data;" -H "Accept: application/json;" -H "Expect:" -X POST -F 'request={"method":"documents.create",
 "params": {
   "document": {
     "displayName": "My File",
     "parentId": 707,
     "mimeType":"image/jpeg",
     "fileName":"test.jpg"
   }
 },
 "id":"documents.create"
}' -F 'file=@photo_evgeny.jpg;' "http://shindigdev.epfl.ch/rpc?method=documents.create&id=documents.create&st=3:3:3:3:3:3:3"

FULL request/response:
 # create a resource for opensocial  
 # Request:
 # parses json object that contains resource object and does things based on it
 #   data object
 #     "parentType":"@space",
 #     "parentId":"53",
 #     "description":"",
 #     "displayName":"test",
 #     "mimeType":"application/json",  # will override the form value
 #     "fileName":"measurement.tcx"    # will override the form value
 #     
 #   Multipart form
 #   form multipart
 #     "file": "dfsdsfafd",
 # 
 # Response: same as below for get


2. Retrieve asset with id=1500 and full attachment size=-1
curl -i -H "Content-Type: application/json; Accept: application/json" -X GET "http://shindigdev.epfl.ch/rest/documents/1500?st=3:3:3:3:3:3:3&size=-1"

FULL request/response:
 # Request:
 #   &size=0 # |0|- no data, |-1| - all data, |388| - specific amount
 # 
 # Response
 # {
 #     "parentType":"@space",
 #     "id":"31",
 #     "parentId":"53",
 #     "profileUrl":"http://localhost:3000/#item=asset_31",
 #     "updated":"2011-05-27T14:50:06.000Z",
 #     "objectId":31,
 #     "description":"",
 #     "thumbnailUrl":"http://localhost:3000/images/pics/asset_thumb.png",
 #     "name":"test",
 #     "displayName":"test",
 #     "mimeType":"application/json",
 #     "data": "dfsdsfafd", # base64 encoded
 #     "filterCapability":
 #     {
 #     }
 # }
