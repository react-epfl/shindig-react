Open Social API implementation in Graasp
=========================================
**This document is a draft.**

## CONTEXT

The context of an opensocial gadget is the space or the user it belongs to.

| Field          | Description                                                                        |
| -------------- | ---------------------------------------------------------------------------------- |
| contextId      | Id of the item that contains the gadget in Graasp (not an IRI nor a Global-Id)     |
| contextType    | **@person** or **@spaces**                                                             |
| containerUrl   | Url of the website that contains the gadget (not the url of the space or the user) |

### GET THE CONTEXT OF THE GADGET

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



### GET THE VIEWER OF THE GADGET

Get all informations about the viewer of this gadget :
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
  users = response.list56
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
### ACTIVITY STREAMS

| Field          | Description                                                                                                                                                                 |
| -------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| id             | Id of the activity **(This is an IRI)**                                                                                                                                     |
| actor          | User that is the initiator of this activity. This is as an **ActivityObject**.                                                                                              |
| object         | Primary object of the activity (Ex : **Space** has been visited, **User** has been invited to this space). This is as an **ActivityObject**                                 |
| target         | Target of the activity (Ex : User has been invited to **this space**). May be undefined (Ex: Space has been visited : no target here). This is as an **ActivityObject**.|
| verb           | Identifies the action that the activity describes. ( *add, update, invite-remind, invite, request-join, join, remove, delete, access*)                                       |
| published      | When the activity was **created** in Graasp (no published notion in Graasp)                                                                                                 |
| updated        | Date of the last update                                                                                                                                                     |

### ACTIVITY OBJECTS
An object is a thing, real or imaginary, which participates in an activity. It may be the entity performing the activity, or the entity on which the activity was performed. In Graasp it can be a User, a Space, an Asset, a Rating, a Link, a Tagging, a Comment, a Widget or a Favorite.

| Field          | Description                                                                                                                                                                 |
| -------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| id             | Id of the activity object **(This is an IRI)**                                                                                                                              |
| displayName    | Name of the activity object                                                                                                                                                 |
| url            | Url of the object in Graasp                                                                                                                                                 |
| summary         | Description of the object                                                                                                                                                  |
| published      | When the activity object was **created** in Graasp (no published notion in Graasp)                                                                                          |
| updated        | Date of the last update                                                                                                                                                     |

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
## APPS
To be continued ...
