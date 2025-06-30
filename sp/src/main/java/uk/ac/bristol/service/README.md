This packages stores the Service layer where background logic is handled. Most methods are self-documented.

## UserService

#### User login(User user)

Receives a User object with uid and password, returns in extra with its corresponding asset holder id, authority (isAdmin) and a jwt token.