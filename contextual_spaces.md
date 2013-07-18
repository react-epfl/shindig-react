Contextual Spaces as OpenSocial Extension
=========================================

## GET THE CONTEXT
```javascript
osapi.context.get().execute(function(context){
  context.contextId; // Id of the item that contains the gadget in Graasp (not an IRI nor a Global-Id)
  context.contextType; // "@people" or "@spaces" 
  context.containerUrl; // Url of the website that contains the gadget (not the url of the space or the user)
});
```

## PEOPLE

The following fields are implemented in Graasp:

| Field          | Description                              |
| -------------- | ---------------------------------------- |
| id             | Id of the user in Graasp                 |
| displayName    | Name of the user                         |
| aboutMe        | Description of the user                  |
| visibilityLevel| public/closed/hidden                     |
| thumbnailUrl   | Url to the thumbnail picture of the user |
| updated        | Date of the last update (format ISO8601) |



### GET THE VIEWER
```javascript
osapi.people.getViewer().execute(function(viewer){
  viewer.id;
  viewer.displayName;
});
```

## ACTIVITY STREAMS

