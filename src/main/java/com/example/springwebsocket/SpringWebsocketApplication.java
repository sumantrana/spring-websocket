package com.example.springwebsocket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@SpringBootApplication
public class SpringWebsocketApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringWebsocketApplication.class, args);
    }

}

record HelloRequest(String name){}
record HelloResponse(String message){}

record AnnouncementRequest(String subject){}
record AnnouncementResponse(String message){}

@Controller
class HelloWebsocketController {

    @MessageExceptionHandler
    @SendTo("/topic/errors")
    String handleException(Exception e){
        var message = "Something went wrong while processing the request " + NestedExceptionUtils.getMostSpecificCause(e);
        return message;
    }

    @MessageMapping("/chat")
    @SendTo("/topic/greetings")
    HelloResponse sayHello(HelloRequest request ) throws Exception {

        Assert.isTrue(Character.isUpperCase(request.name().charAt(0)), () -> "Name should start with a uppercase letter");
        Thread.sleep(1000);
        return new HelloResponse("Hello!!! " + request.name());
    }

    @MessageMapping("/announcement")
    @SendTo("/topic/announcements")
    public AnnouncementResponse announce(AnnouncementRequest request){
        System.out.println("Received announcement request: " + request.subject());
        return new AnnouncementResponse("Everybody - " + request.subject());
    }

    @Configuration
    @EnableWebSocketMessageBroker
    class HelloWebsocketConfiguration implements WebSocketMessageBrokerConfigurer {

        @Override
        public void registerStompEndpoints(StompEndpointRegistry registry) {
            registry.addEndpoint("/chat").withSockJS();
            registry.addEndpoint("/announcement").withSockJS();
        }

        @Override
        public void configureMessageBroker(MessageBrokerRegistry registry) {
            registry.enableSimpleBroker("/topic");
            registry.setApplicationDestinationPrefixes("/app");
        }
    }

}
