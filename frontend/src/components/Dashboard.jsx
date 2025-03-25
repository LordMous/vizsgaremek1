import React, { useEffect, useState, useRef } from 'react';
import { Stomp } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import authService from '../services/authService';
import { useMemo } from 'react';

function Dashboard() {
  const [chats, setChats] = useState([]);
  const [selectedChat, setSelectedChat] = useState(null);
  const [messages, setMessages] = useState([]);
  const [newMessage, setNewMessage] = useState('');

  const currentUser = useMemo(() => authService.getCurrentUser(), []);
  
  const stompClient = useRef(null);

  useEffect(() => {
    const fetchChats = async () => {
      try {
        const response = await authService.getChats();
        setChats(response.data);
      } catch (error) {
        console.error('Error fetching chats', error);
      }
    };

    if (currentUser) {
      fetchChats();
    }
  }, [currentUser]);

  useEffect(() => {
    if (selectedChat) {
      connectWebSocket();
    }
  }, [selectedChat]);

  const connectWebSocket = () => {
    const socket = new SockJS('http://localhost:8080/ws');
    stompClient.current = Stomp.over(socket);
    console.log('Connecting to WebSocket...');
    stompClient.current.connect({}, () => {
      console.log('Connected to WebSocket');
  
      // Feliratkozás a privát üzenetek csatornájára
      stompClient.current.subscribe('/user/queue/messages', (message) => {
        if (message.body) {
          const receivedMessage = JSON.parse(message.body);
          console.log('Received WebSocket message:', receivedMessage);
          // Csak akkor frissítjük az üzeneteket, ha az üzenet az aktuális chathez tartozik
          if (receivedMessage.chatId === selectedChat?.id) {
            setMessages((prevMessages) => [...prevMessages, receivedMessage]);
          }
        }
      });
  
      // Az aktuális chat üzeneteinek betöltése
      fetchMessages();
    }, (error) => {
      console.error('WebSocket error:', error);
    });
  };

  const fetchMessages = async () => {
    try {
      const response = await authService.getMessages(selectedChat.id);
      setMessages(response.data);
      console.log('Fetched messages:', response.data);
    } catch (error) {
      console.error('Error fetching messages', error);
    }
  };

  const handleChatClick = (chat) => {
    if (chat.id !== selectedChat?.id) {
      setSelectedChat(chat);
    }
  };

  const handleSendMessage = (e) => {
    e.preventDefault();
    if (stompClient.current && stompClient.current.connected && newMessage.trim() !== '') {
      const message = {
        sender: currentUser.userName,
        content: newMessage,
        chatId: selectedChat.id,
      };
  
      stompClient.current.send('/app/chat', {}, JSON.stringify(message));
  
      // Azonnal hozzáadjuk az üzenetet a listához
      setMessages((prevMessages) => [
        ...prevMessages,
        { sender: currentUser.userName, message: newMessage, chatId: selectedChat.id },
      ]);
  
      setNewMessage('');
    }
  };

  if (!currentUser) {
    return <div>Please log in to view your dashboard.</div>;
  }

  return (
    <div>
      <h1>Dashboard</h1>
      <div style={{ display: 'flex' }}>
        <div style={{ flex: 1 }}>
          <h2>Chats</h2>
          <ul>
            {chats.map((chat) => (
              <li key={chat.id} onClick={() => handleChatClick(chat)}>
                Chat with {chat.user1 && chat.user1.userName === currentUser.userName ? chat.user2?.userName : chat.user1?.userName}
              </li>
            ))}
          </ul>
        </div>
        <div style={{ flex: 2 }}>
          {selectedChat && (
            <>
              <h2>Messages</h2>
              <ul>
                {messages.map((message, index) => (
                  <li key={index}>
                    <strong>{message.sender}:</strong> {message.message}
                  </li>
                ))}
              </ul>
              <form onSubmit={handleSendMessage}>
                <input
                  type="text"
                  value={newMessage}
                  onChange={(e) => setNewMessage(e.target.value)}
                  required
                />
                <button onClick={() => {
                  authService.sendMessage(selectedChat.id, newMessage);
                  
                }} type="submit">Send</button>
              </form>
            </>
          )}
        </div>
      </div>
    </div>
  );
}

export default Dashboard;