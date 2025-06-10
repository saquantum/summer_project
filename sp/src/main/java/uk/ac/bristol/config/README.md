This package stores configurations and does not contain any logic or calculation.

## SpMVCConfigSupport

Configuration for Spring MVC.

#### protected void addResourceHandlers(ResourceHandlerRegistry registry)

Resolve static resources.

#### protected void addInterceptors(InterceptorRegistry registry)

Control the path that will be intercepted.

## WebSocketConfig

Configuration for the Stomp Web Socket.

#### public void registerStompEndpoints(StompEndpointRegistry registry)

Specifies the path that the front-end SockJS listens on: `const socket = new SockJS('/ws')` where `/ws` is specified in this method.

#### public void configureMessageBroker(MessageBrokerRegistry config)

Define the prefix of the route that Stomp Clients listens on: `this.stompClient.subscribe('/topic/notify'`