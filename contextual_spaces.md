Open Social API implementation in Graasp
=========================================

## CONTEXT

The context of an opensocial gadget is the space or the user it belongs to.

| Field          | Description                                                                        |
| -------------- | ---------------------------------------------------------------------------------- |
| contextId      | Id of the item that contains the gadget in Graasp (not an IRI nor a Global-Id)     |
| contextType    | "@people" or "@spaces"                                                             |
| containerUrl   | Url of the website that contains the gadget (not the url of the space or the user) |

Get the context :

```javascript
osapi.context.get().execute(function(context){
  context.contextId; // 1234
  context.contextType; // "@spaces" 
  context.containerUrl; // "http://graasp.epfl.ch"
});
```

## PEOPLE

The following fields are implemented in Graasp:

| Field          | Description                                                                   |
| -------------- | ----------------------------------------------------------------------------- |
| id             | Id of the user in Graasp                                                      |
| displayName    | Name of the user                                                              |
| aboutMe        | Description of the user                                                       |
| visibilityLevel| public/closed/hidden                                                          |
| thumbnailUrl   | Url to the thumbnail picture of the user (default thumbnail if not specified) |
| updated        | Date of the last update (format ISO8601)                                      |



### GET THE VIEWER OF THE GADGET
```javascript
osapi.people.getViewer().execute(function(viewer){
  viewer.id; // 5678
  viewer.displayName; // "Jean Dupont"
  viewer.aboutMe; // "I am a student in the IC faculty of EPFL"
  viewer.visibilityLevel; // "public"
  viewer.thumbnailUrl; // "http://localhost:3000/images/pics/user_thumb.png"
  viewer.updated; // 2013-07-17T08:06:11.000Z
});
```

### GET MEMBERS FROM A SPACE
**Important :** *userId* must contain the contextId (in this case, the id of the space), *personId* must contain the contextType (in this case, "@space"). Every other parameter will be ignored.

```javascript
osapi.people.get({userId: "3253", personId: "@space"}).execute(function(response){
  members = response.list;
  for(var i=0; i<members.length; i++) {
    members[i].displayName;
  }
}); // Jean Dupont, Anne Onyme, Hannon Yme ….
```

## ACTIVITY STREAMS

