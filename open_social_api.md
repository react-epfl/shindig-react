Open Social API implementation in Graasp
=========================================
**This document is a draft.**

## CONTEXT OF AN APP

The context of an opensocial app is the space or the user it belongs to.

| Field          | Description                                                                        |
| -------------- | ---------------------------------------------------------------------------------- |
| contextId      | Id of the item that contains the app in Graasp (not an IRI nor a Global-Id)     |
| contextType    | **@person** or **@space**                                                             |
| containerUrl   | Url of the website that contains the app (not the url of the space or the user) |

### GET THE CONTEXT OF THE APP (SPACE IN WHICH THE APP LIVES)

```javascript
osapi.context.get().execute(function(context){
  context.contextId; // 1234
  context.contextType; // @spaces 
  context.containerUrl; // http://graasp.epfl.ch
});
```

## PEOPLE

The following fields are implemented in Graasp:

| Field          | Description                                                                   |
| -------------- | ----------------------------------------------------------------------------- |
| id             | Id of the user in Graasp                                                      |
| displayName    | Name of the user                                                              |
| aboutMe        | Description of the user                                                       |
| visibilityLevel| **public** / **closed** / **hidden**                                                          |
| thumbnailUrl   | Url to the thumbnail picture of the user (default thumbnail if not specified) |
| updated        | Date of the last update (format ISO8601)                                      |



### GET THE VIEWER OF THE APP

Get all informations about the viewer of this app :

```javascript
osapi.people.getViewer().execute(function(viewer){
  viewer.id; // 5678
  viewer.displayName; // Jean Dupont
  viewer.aboutMe; // I am a student in the IC faculty of EPFL
  viewer.visibilityLevel; // public
  viewer.thumbnailUrl; // http://graasp.epfl.ch/images/pics/user_thumb.png
  viewer.updated; // 2013-07-17T08:06:11.000Z
});
```

### GET THE OWNER OF THE SPACE WHICH CONTAINS THE APP

Get all informations about the owner of the space which contains this app :

```javascript
osapi.people.getOwner().execute(function(owner){
  owner.id; // 5678
  owner.displayName; // Jean Dupont
 // etc...
});
```

### GET MEMBERS FROM A SPACE
*userId* should contain the contextId (in this case, the id of the space), *personId* should contain the contextType (in this case, **@space**). *groupId* will be ignored.

FILTERING and PAGINATION : It is possible to limit the number of results by adding the parameter *count*, and to change the offset with the parameter *startIndex* . *sortOrder* is not implemented. Filters are not implemented and will be ignored ( *filterBy*, *filterOp*, *filterValue*, *updatedSince*).

Get the name of everyone who has joined the space with id 3253 :

```javascript
osapi.people.get({userId: "3253", personId: "@space"}).execute(function(response){
  members = response.list;
  for(var i=0; i<members.length; i++) {
    members[i].displayName;
  }
});// Jean Dupont, Anne Onyme, Hannon Yme … 
```



### GET ALL PUBLIC USERS OF GRAASP
*userId* should be set to **@all**. Every other parameter will be ignored. Returns a list of id.

```javascript
osapi.people.get({userId: "@all"}).execute(function(response){
  users = response.list;
  for(var i=0; i<users.length; i++) {
    users[i];
  }
})// 1, 2, 3 … , 1455, 1456 
```

### GET USER(S) BY ID(S)

*userId* must be specified, as a single id or as a list of ids (1,2,3,…).

FILTERING and PAGINATION : In this case, all the OpenSocial standard request parameters are available ( *count*, *startIndex*, *sortOrder*, *filterBy*, *filterOp*, *filterValue*, *updatedSince*). See [OpenSocial standard request parameters](http://opensocial-resources.googlecode.com/svn/spec/trunk/Core-API-Server.xml#Standard-Request-Parameters) for more details.

Retrieve the name of the user with id 3253 :

```javascript
osapi.people.get({userId: "3253"}).execute(function(response){
    response.displayName; // Jean Dupont
});
```

Retrieve the names of the users with ids 3253, 3254 and 3255 :

```javascript
osapi.people.get({userId: "3253,3254,3255"}).execute(function(response){
  spaces = response.list;
  for(var i=0; i<spaces.length; i++) {
    spaces[i].displayName;
  }
});
```

### OTHER

Groups are not implemented in Graasp, therefore these are the only requests that can be performed on Users in Graasp.

## SPACES
The following fields are implemented in Graasp

| Field          | Description                                                                   |
| -------------- | ----------------------------------------------------------------------------- |
| id             | Id of the space in Graasp                                                     |
| displayName    | Name of the space                                                             |
| description    | Description of the space                                                      |
| visibilityLevel| **Everyone** / **Space members** / **Myslef**                                 |
| thumbnailUrl   | Url to the thumbnail picture of the space (default thumbnail if not specified)|
| parentType     | Type of the parent of this space ( **@person** or **@space**)                 |
| parentId       | Id of the parent of this space                                                |
| updated        | Date of the last update (format ISO8601)                                      |

### GET A LIST OF SPACES FOR A PERSON OR FOR A SPACE

*contextId* as well as the *contextType* should be specified.

#### List subspaces of a space

Retrieve all informations about subspaces of the space that has id 3253 :

```javascript
osapi.spaces.get({contextId: "3253", contextType: "@space"}).execute(function(response){
  subspaces = response.list;
  for(var i=0; i<subspaces.length; i++) {
    subspaces[i].id; // 3511
    subspaces[i].displayName; // Project reports
    subspaces[i].description; // This space contains the project reports 
    subspaces[i].visibilityLevel; // Space members
    subspaces[i].thumbnailUrl; // http://graasp.epfl.ch/images/pics/space_thumb.png
    subspaces[i].parentType; // @space
    subspaces[i].parentId; // 3253
    subspaces[i].updated; // 2013-07-16T14:31:22.000Z
  }
});//Space1, Space2 ...
```

#### List top level spaces of a user

Retrieve all informations about top level spaces of user with id 1318 :

```javascript
osapi.spaces.get({contextId: "1318", contextType: "@person"}).execute(function(response){
  subspaces = response.list;
  for(var i=0; i<subspaces.length; i++) {
    subspaces[i].id; // 3437
    subspaces[i].displayName; // Jean Dupont's Home Space
    subspaces[i].description; // This is my home space 
    subspaces[i].visibilityLevel; // Everyone
    subspaces[i].thumbnailUrl; // http://graasp.epfl.ch/images/pics/space_thumb.png
    subspaces[i].parentType; // @person
    subspaces[i].parentId; // 1318
    subspaces[i].updated; // 2013-07-16T14:31:22.000Z
  }
});
```

### GET SPACE(S) BY ID(S)

*contextId* must be specified, as a single id or as a list of ids (1,2,3,…).

Retrieve the name of the space with id 3437 :

```javascript
osapi.spaces.get({contextId: "3437"}).execute(function(response){
    response.displayName; // Jean Dupont's Home Space
});
```

Retrieve the names of the spaces with ids 3255, 3256 and 3258 :

```javascript
osapi.spaces.get({contextId: "3255,3256,3258"}).execute(function(response){
  spaces = response.list;
  for(var i=0; i<spaces.length; i++) {
    spaces[i].displayName;
  }
});
```

### GET ALL PUBLIC SPACES OF GRAASP
*contextId* should be set to **@all**. Every other parameter will be ignored. Returns a list of id.
 
```javascript
osapi.spaces.get({contextId: "@all"}).execute(function(response){
  spaces = response.list;
  for(var i=0; i<spaces.length; i++) {
    spaces[i];
  }
});
```



## ACTIVITIES (ACTIVITY STREAMS)

### COLLECTION

The structure of a Collection for activity streams differs from the one of a classical OpenSocial Collection.

| Field          | Description                                                                                                                                                                 |
| -------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| itemsPerPage   | Number of items per page, corresponds to the *count* request parameter. Default is 10 000.                                                                                  |
| startIndex     | Index of the first item of the page. Corresponds to the *startIndex* request parameter. Default is 1.                                                                       |
| filtered       | Always **true** .The results will always honor filter params in the request. The default value is 'true' if the field does not exist.                                       |
| updatedSince   | Always **true**. The results will always honor updatedSince param in the request. The default value is 'true' if the field does not exist.                                  |
| sorted         | Always **true**. The results will always honor sortOrder param in the request. The default value is 'true' if the field does not exist.                                     |
| totalResults   | The total number of contacts that would be returned if there were no startIndex or count specified. This value tells the Consumer how many total results to expect, regardless of the current pagination being used, but taking into account the current filtering options in the request.                                                                                                                              |
| entry          | An array of activity entries, one for each item matching the request.                                                                                                       |


### ACTIVITY ENTRY

| Field          | Description                                                                                                                                                                 |
| -------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| id             | Id of the activity **(This is an IRI)**                                                                                                                                     |
| actor          | User that is the initiator of this activity. This is as an **ActivityObject**.                                                                                              |
| object         | Primary object of the activity (Ex : **Space** has been visited, **User** has been invited to this space). This is as an **ActivityObject**.                                |
| target         | Target of the activity (Ex : User has been invited to **this space**). May be undefined (Ex: Space has been visited : no target here). This is as an **ActivityObject**.    |
| verb           | Identifies the action that the activity describes. ( *add, update, invite-remind, invite, request-join, join, remove, delete, access*)                                      |
| published      | When the activity was **created** in Graasp (no published notion in Graasp) (ISO8601)                                                                                       |
| updated        | Date of the last update (ISO8601)                                                                                                                                           |

### ACTIVITY OBJECT
An object is a thing, real or imaginary, which participates in an activity. It may be the entity performing the activity, or the entity on which the activity was performed. In Graasp it can be a User, a Space, an Asset, a Rating, a Link, a Tagging, a Comment, a Widget or a Favorite.

| Field          | Description                                                                                            |
| -------------- | ------------------------------------------------------------------------------------------------------ |
| id             | Id of the activity object **(This is an IRI)**                                                         |
| objectType     | *User, Space, Asset, Rating, Link, Tagging, Comment, Widget, Favorite*                                 |
| displayName    | Name of the activity object (not defined for **Rating, Link**)                                         |
| author         | Author of this object. This is a **ActivityObject**.(not defined for **User, Anonymous Rating**)       |
| image          | Image of the object. This is a **MediaLink**. (not defined for **Rating, Link**)                       |
| url            | Url of the object in Graasp (not defined for **Rating, Link**)                                         |
| summary        | Description of the object (not defined for **Rating, Link**)                                           |
| content        | Content of the object (not defined for **User, Space, Link**)                                          |
| published      | When the activity object was **created** in Graasp (no published notion in Graasp) (ISO8601)           |
| updated        | Date of the last update (ISO8601)                                                                      |

### MEDIA LINK

| Field          | Description              |
| -------------- | ------------------------ |
| url            | url of the media item.   |


### RETRIEVE ACTIVITY STREAM OF A USER OR A SPACE
#### Get the activity stream of a space

*contextId* as well as the *contextType* should be specified.

Retrieve some informations form the activity stream of the space with id 1234 :

```javascript
osapi.activitystreams.get({contextId: 1234, contextType: "@space"}).execute(function(response){
  activities = response.entry;
  for(var i=0; i<response.itemsPerPage; i++) {
    activities[i].id;
    activities[i].actor.displayName;
    activities[i].object.displayName;
    activities[i].target.displayName;
    activities[i].verb; //...
  }
});
```

#### Get the activity stream of a user

Retrieve some informations form the activity stream of the user with id 1234 :

```javascript
osapi.activitystreams.get({contextId: 5678, contextType: "@user"}).execute(function(response){
  activities = response.entry;
  for(var i=0; i<response.itemsPerPage; i++) {
    activities[i].id;
    activities[i].actor.displayName;
    activities[i].object.displayName;
    activities[i].target.displayName;
    activities[i].verb; //...
  }
});
```

### FILTER AND PAGINATE ACTIVITIES

Supported parameters : 

| Field          | Description                                                                                                                                                                              |
| -------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| count          | The number of items per page, for a paged collection.                                                                                                                                    |
| startIndex     | The index of the first result to be retrieved (for paging).                                                                                                                              |
| sortOrder      | Sort order (by update date) of the results. Default is **ascending** (oldest activities first), in order to get the last activities first, it is necessary specify it as **descending**. |
| updatedSince   | Return only entries updated since the date specified in this parameter (ISO8601)                                                                                                         |
| filterBy       | Field to filter.                                                                                                                                                                         |
| filterOp       | **contains**, **startsWith**, **equals**, **exists**                                                                                                                                     |
| filterValue    | Value of the filter                                                                                                                                                                      |
| fields         | Comma separated list of fields to include in the response. The response will contain every field if this parameter is not specified.                                                     |



**Example :** Retrieve activities corresponding to the last 10 visits of the space with id 1234 :

```javascript
osapi.activitystreams.get({contextId: 1234, contextType: "@space", count:10, sortOrder:"descending", filterBy:"verb", filterOp:"equals", filterValue:"access"}).execute(function(response){
  activities = response.entry;
  for(var i=0; i<response.itemsPerPage; i++) {
    activities[i].actor.displayName;
  }
});
```

### CREATE AN ACTIVITY ENTRY

It is possible to create a new Activity Entry from an app. The *activity* parameter must contain the new activityEntry, described in the JSON format. *userId* and *groupId* must be specified too.

```javascript
    var params = {
      userId: '@viewer',
      groupId: '@self',
      activity: {
        actor: {
          id: "graasp.epfl.ch/User/42",
        },
        verb: "add",
        object: {
          id: "graasp.epfl.ch/Space/666",
          objectType: "Space"
        }
        target: {
          id: "graasp.epfl.ch/Space/123"
          objectType: "Space"
        }
      }
    };
    osapi.activitystreams.create(params).execute(function(response){
 		response.id; //Id of the newly created activity entry
  });
```

NOTE : Supported *verb* values are : **add, update, invite, invite-remind, invite, request-join, join, remove, delete, access**

## APPS
To be continued ...
