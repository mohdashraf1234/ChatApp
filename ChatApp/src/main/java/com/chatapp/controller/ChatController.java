package com.chatapp.controller;

import com.chatapp.model.ChatMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final Set<String> activeUsers = ConcurrentHashMap.newKeySet();

    public ChatController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        System.out.println("=== SENDING MESSAGE ===");
        System.out.println("Sender: " + chatMessage.getSender());
        System.out.println("Receiver: " + chatMessage.getReceiver());
        System.out.println("Content: " + chatMessage.getContent());
        System.out.println("Type: " + chatMessage.getType());
        
        String sender = chatMessage.getSender();
        String receiver = chatMessage.getReceiver();
        
        // Validate sender exists in active users
        if (!activeUsers.contains(sender)) {
            System.out.println("ERROR: Sender " + sender + " not in active users!");
            return;
        }

        if (receiver == null || receiver.trim().isEmpty()) {
            // Public message
            System.out.println("Sending PUBLIC message to /topic/public");
            messagingTemplate.convertAndSend("/topic/public", chatMessage);
        } else {
            // Private message
            if (!activeUsers.contains(receiver)) {
                System.out.println("ERROR: Receiver " + receiver + " not in active users!");
                System.out.println("Active users: " + activeUsers);
                return;
            }
            
            System.out.println("Sending PRIVATE message from " + sender + " to " + receiver);
            
            // Send to receiver using user destination - FIXED PATH
            messagingTemplate.convertAndSendToUser(
                receiver, 
                "/queue/private", 
                chatMessage
            );
            System.out.println("Sent to receiver: " + receiver);
            
            // Also send to sender so they can see their own private message
            messagingTemplate.convertAndSendToUser(
                sender, 
                "/queue/private", 
                chatMessage
            );
            System.out.println("Sent to sender: " + sender);
            
            // Debug: Also send as public for testing
            System.out.println("DEBUG: Also sending as public for testing");
            ChatMessage debugMessage = new ChatMessage();
//            debugMessage.setSender("SYSTEM");
          
            debugMessage.setSender(sender);
//            debugMessage.setContent("Private msg from " + sender + " to " + receiver + ": " + chatMessage.getContent());
            debugMessage.setContent(chatMessage.getContent());
            debugMessage.setType(ChatMessage.MessageType.CHAT);
            messagingTemplate.convertAndSend("/topic/public", debugMessage);
        }
        System.out.println("=== MESSAGE SENT ===");
    }

    @MessageMapping("/chat.addUser")
    public void addUser(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        String username = chatMessage.getSender();
        
        System.out.println("=== USER JOINING ===");
        System.out.println("Username: " + username);
        
        if (username != null && !username.trim().isEmpty()) {
            headerAccessor.getSessionAttributes().put("username", username);
            
            if (activeUsers.add(username)) {
                System.out.println("User added: " + username);
                System.out.println("Active users now: " + activeUsers);
                
                // Notify everyone about new user
                ChatMessage joinMessage = new ChatMessage();
                joinMessage.setType(ChatMessage.MessageType.JOIN);
                joinMessage.setSender(username);
                joinMessage.setContent(username + " joined the chat");
                
                messagingTemplate.convertAndSend("/topic/public", joinMessage);
                System.out.println("Join notification sent");
                
                // Update user list
                updateActiveUsers();
            } else {
                System.out.println("User already exists: " + username);
            }
        }
        System.out.println("=== USER JOINED ===");
    }

    @MessageMapping("/chat.leave")
    public void leaveUser(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        String username = chatMessage.getSender();
        
        System.out.println("=== USER LEAVING ===");
        System.out.println("Username: " + username);
        
        if (username != null && activeUsers.remove(username)) {
            System.out.println("User removed: " + username);
            System.out.println("Active users now: " + activeUsers);
            
            ChatMessage leaveMessage = new ChatMessage();
            leaveMessage.setType(ChatMessage.MessageType.LEAVE);
            leaveMessage.setSender(username);
            leaveMessage.setContent(username + " left the chat");
            
            messagingTemplate.convertAndSend("/topic/public", leaveMessage);
            System.out.println("Leave notification sent");
            
            updateActiveUsers();
        }
        System.out.println("=== USER LEFT ===");
    }

    private void updateActiveUsers() {
        System.out.println("=== UPDATING USER LIST ===");
        System.out.println("Current active users: " + activeUsers);
        
        ChatMessage userListMessage = new ChatMessage();
        userListMessage.setType(ChatMessage.MessageType.USERS);
        userListMessage.setContent(String.join(",", activeUsers));
        
        messagingTemplate.convertAndSend("/topic/users", userListMessage);
        System.out.println("User list updated and sent");
        System.out.println("=== USER LIST UPDATED ===");
    }
}