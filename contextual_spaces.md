Contextual Spaces as OpenSocial Extension
=========================================

## GET THE CONTEXT
```javascript
osapi.context.get(function(context){
  context.contextId; // Id of the item in Graasp (not an IRI nor a Global-Id)
  context.contextType; // "@people" or "@spaces"
  context.containerUrl; // Url of the website that contains the gadget (not the url of the space or the user)
});

## ACTIVITY STREAMS

