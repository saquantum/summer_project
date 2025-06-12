This package is the Controller layer and specifies what to be requested from front-end and what to return to the front-end.

## Code

Defines some constant integer code for actions.

#### ResponseResult

The universal format of HTTP response body JSON.

## Pages

Controls the redirection of front-end pages.

#### public void redirectHome(HttpServletResponse response)

Redirect `/` to `/index.html`.

#### public void redirectUser(HttpServletResponse response)

Redirect `/user` to `/user/index.html`.

#### public void redirectAdmin(HttpServletResponse response)

Redirect `/admin` to `/admin/index.html`.

## SpExceptionHandler

Central exception handler. Coders should throw all exceptions to the controller layer so that this handler catches all exceptions.

## BroadcastController

The interface for admin to send messages to users.

#### public ResponseEntity<?> sendNotification

Admin sends an HTTP request with JSON body formatted like `{"message": "..."}` to `/admin/notify` and sends it to all users which listen on the Stomp route `/topic/notify`.

## CrudController

Defines all front-end CRUD actions. These methods should be self-documented, so won't be discussed here, we only talked about their universal properties.

1. Their functionality: If the annotation above it says `@GetMapping`, then tries to receive HTTP GET requests to that path. Similar applies to `@PutMapping`, `@PostMapping` and `@DeleteMapping`.

2. The path they listen on: If a method is specified by annotation `@GetMapping("/asset")`, then it listens on the path `/user/api/asset` where the prefix `/user/api` is specified at the top of the class.

3. These methods return a `ResponseResult` class object and will be automatically converted to JSON as the HTTP response body by the jackson dependency.

4. These methods are RESTful: GET method only retrieves data, POST method only inserts new data, PUT method only updates data, DELETE method only deletes data.