import React, { useEffect, useState, useRef } from 'react';
import { Stomp } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import authService from '../services/authService';
import { Link } from 'react-router-dom';

function Dashboard() {
  const [chats, setChats] = useState([]);
  const [selectedChat, setSelectedChat] = useState(null);
  const [messages, setMessages] = useState([]);
  const [newMessage, setNewMessage] = useState('');
  const [chatDetails, setChatDetails] = useState([]);
  const [currentUser, setCurrentUser] = useState(authService.getCurrentUser());
  const [activeTab, setActiveTab] = useState('chats');
  const [users, setUsers] = useState([]);
  const [pendingRequests, setPendingRequests] = useState([]);
  const [friends, setFriends] = useState([]);

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
      valami();
    }
  }, [currentUser]);

  useEffect(() => {
    if (chats.length > 0) {
      valami();
    }
  }, [chats]);

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
            console.log('Received WebSocket message:', receivedMessage.content);
            console.log(receivedMessage.content);
            // Ellenőrizzük, hogy az üzenet nem üres
            if (receivedMessage.content && receivedMessage.chatId === selectedChat?.id) {
                setMessages((prevMessages) => [...prevMessages, {
                  sender: receivedMessage.sender,
                  message: receivedMessage.content,
                  createdAt: new Date(),

                }]);
              
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

  //chatek a részlete
  const valami = async () => {
    try {
      const responses = await Promise.all(
        chats.map((chat) => authService.getChatDetails(chat.id))
      );
      const details = responses.map((response) => response.data);
      setChatDetails(details); // Beállítjuk az összes adatot egyszerre
      //console.log(details); // Kiírjuk az összes adatot
    } catch (error) {
      console.error('Error fetching chat details', error);
    }
  };



  useEffect(() => {
    if (currentUser) {
      fetchChats();
      fetchUsers();
      fetchPendingRequests();
      fetchFriends();
    }
  }, [currentUser]);

  useEffect(() => {
    if (selectedChat) {
      connectWebSocket();
    }
  }, [selectedChat]);

  const fetchChats = async () => {
    try {
      const response = await authService.getChats();
      setChats(response.data);
    } catch (error) {
      console.error('Error fetching chats', error);
    }
  };

  const fetchUsers = async () => {
    try {
      const response = await authService.getAllUsers();
      console.log(response.data);
      console.log(currentUser.userId);
      
      setUsers(response.data.filter(user => user.id !== Number(currentUser.userId)));
    } catch (error) {
      console.error('Error fetching users', error);
    }
  };

  const fetchPendingRequests = async () => {
    try {
      const response = await authService.getContactsByStatus('PENDING');
      
      setPendingRequests(response.data);
    } catch (error) {
      console.error('Error fetching pending requests', error);
    }
  };

  const fetchFriends = async () => {
    try {
      const response = await authService.getContactsByStatus('ACCEPTED');
      console.log(response.data);
      setFriends(response.data);
    } catch (error) {
      console.error('Error fetching friends', error);
    }
  };

  const handleAddFriend = async (contactUserId) => {
    console.log(contactUserId);
    console.log(currentUser);
    try {
      await authService.addContact(currentUser.userId, contactUserId);
      alert('Friend request sent!');
    } catch (error) {
      console.error('Error adding friend', error);
    }
  };

  const disconnectWebSocket = () => {
    if (stompClient.current && stompClient.current.connected) {
        stompClient.current.disconnect(() => {
            console.log('WebSocket disconnected');
        });
    }
};

  const handleChatClick = (chat) => {
    if (chat.id !== selectedChat?.id) {
      disconnectWebSocket();
      setSelectedChat(chat);
    }
  };

  const handleStartChat = async (friend) => {
    try {
      // Ellenőrizzük, hogy van-e már chat a baráttal
      const existingChat = chats.find(chat =>
        (chat.user1Id === currentUser.userId && chat.user2Id === friend.contactUserId) ||
        (chat.user2Id === currentUser.userId && chat.user1Id === friend.contactUserId)
      );
  
      if (existingChat) {
        // Ha van meglévő chat, azt állítjuk be
        setSelectedChat(existingChat);
      } else {
        // Ha nincs meglévő chat, létrehozunk egyet
        const response = await authService.createChat(currentUser.userId, friend.contactUserId);
        setChats([...chats, response.data]);
        setSelectedChat(response.data);
      }
  
      // Átváltunk a "Chats" fülre
      setActiveTab('chats');
    } catch (error) {
      console.error('Hiba történt a beszélgetés indításakor:', error);
      alert('Nem sikerült elindítani a beszélgetést.');
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
        // Csak küldjük el, de NEM frissítjük a messages listát azonnal
        stompClient.current.send('/app/chat', {}, JSON.stringify(message));

        setNewMessage('');
        const rawr = authService.sendMessage(selectedChat.id, newMessage);
        rawr
    }
  };

  const handleDeleteFriend = async (friend) => {
    try {
      // Az aktuális felhasználó azonosítója
      const currentUserId = currentUser.userId;
  
      // Meghatározzuk, hogy melyik mező tartozik az aktuális felhasználóhoz
      const userId = friend.userId === currentUserId ? friend.userId : friend.contactUserId;
      const contactUserId = friend.userId === currentUserId ? friend.contactUserId : friend.userId;
  
      // Törlés a backendről (ID-k helyének megcserélése)
      await authService.deleteContact(contactUserId, userId);
      alert('Barát sikeresen törölve!');
      fetchFriends(); // Frissítjük a barátok listáját
    } catch (error) {
      console.error('Hiba történt a barát törlésekor:', error);
      alert('Nem sikerült törölni a barátot.');
    }
  };

  const handleDeleteChat = async (chatId) => {
    const confirmDelete = window.confirm('Biztosan törölni szeretnéd ezt a chatet?');
    if (!confirmDelete) return;
  
    try {
      console.log('Chat törlése indult:', chatId);
      await authService.deleteChat(chatId);
      alert('Chat sikeresen törölve!');
      setChats(chats.filter(chat => chat.id !== chatId)); // Töröljük a chatet a listából
      setSelectedChat(null); // Nincs kiválasztott chat
    } catch (error) {
      alert('Nem sikerült törölni a chatet.');
    }
  };

  const handleUpdateStatus = async (contactUserId, status) => {
    try {
      await authService.updateContactStatus(currentUser.userId, contactUserId, status);
      fetchPendingRequests();
      fetchFriends();
    } catch (error) {
      console.error('Error updating contact status', error);
    }
  };

  if (!currentUser) {
    return <div>Please log in to view your dashboard.</div>;
  }

  return (
    <div>
      <header style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h1>Dashboard </h1>
        <h2>Welcome, {currentUser.userName}!</h2> 
        <nav>
          <Link to="/profile">Profile</Link>
        </nav>
      </header>
      <nav>
        <button onClick={() => setActiveTab('chats')}>Chats</button>
        <button onClick={() => setActiveTab('contacts')}>All Users</button>
        <button onClick={() => setActiveTab('pending')}>Pending Requests</button>
        <button onClick={() => setActiveTab('friends')}>Friends</button>
      </nav>
      <div>
        {activeTab === 'chats' && (
          <div style={{ display: 'flex' }}>
            <div style={{ flex: 1 }}>
              <h2>Chats</h2>
              <ul>
                {chats.map((chat) => {
                  const chatDetail = chatDetails.find(detail => detail.id === chat.id);
  
                  if (!chatDetail) {
                    return null; // Ha nincs meg a részletek között, ne jelenítsünk meg semmit
                  }
  
                  const otherUser = chatDetail.user1Name === currentUser.userName 
                    ? chatDetail.user2Name 
                    : chatDetail.user1Name;
  
                  return (
                    <li key={chat.id} onClick={() => handleChatClick(chat)}>
                      {otherUser || "Unknown User"}
                    </li>
                  );
                })}
              </ul>
            </div>
            <div style={{ flex: 2 }}>
            {selectedChat && (
  <>
    <h2>Messages</h2>
    <button
      style={{ marginBottom: '10px', color: 'red' }}
      onClick={() => handleDeleteChat(selectedChat.id)}
    >
      Chat törlése
    </button>
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
      <button type="submit">Send</button>
    </form>
  </>
)}
            </div>
          </div>
        )}
        {activeTab === 'contacts' && (
          <div>
            <h2>All Users</h2>
            <ul>
              {users.map(user => (
                <li key={user.id}>
                  {user.userName}{' '}
                  {friends.some(friend => friend.contactUserId === user.id || friend.userId === user.id) ? (
                    <span style={{ color: 'green', marginLeft: '10px' }}>
                      You are already friends with this user
                    </span>
                  ) : (
                    <button onClick={() => handleAddFriend(user.id)}>Add Friend</button>
                  )}
                </li>
              ))}
            </ul>
          </div>
        )}
        {activeTab === 'pending' && (
          <div>
            <h2>Pending Requests</h2>
            <ul>
              {pendingRequests.map(request => (
                <li key={request.id}>
                  {request.userName} 
                  <button onClick={() => handleUpdateStatus(request.userId, 'ACCEPTED')}>Accept</button>
                  <button onClick={() => handleUpdateStatus(request.userId, 'BLOCKED')}>Reject</button>
                </li>
              ))}
            </ul>
          </div>
        )}
        {activeTab === 'friends' && (
  <div>
    <h2>Friends</h2>
    <ul>
    {friends.map(friend => (
  <li key={friend.id}>
    {friend.userName === currentUser.userName ? friend.contactUserName : friend.userName}
    <button
      style={{ marginLeft: '10px', color: 'red' }}
      onClick={() => handleDeleteFriend(friend)}
    >
      Barát törlése
    </button>
    <button
      style={{ marginLeft: '10px', color: 'blue' }}
      onClick={() => handleStartChat(friend)}
    >
      Beszélgetés
    </button>
  </li>
))}
    </ul>
  </div>
)}
      </div>
    </div>
  );
}

export default Dashboard;
