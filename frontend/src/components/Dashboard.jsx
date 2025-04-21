import React, { useEffect, useState, useRef, use } from 'react';
import { Stomp } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import authService from '../services/authService';
import { Link, useNavigate } from 'react-router-dom';
import CampaignIcon from '@mui/icons-material/Campaign';
import AddIcon from '@mui/icons-material/Add';
import './Dashboard.css'

function Dashboard() {

  let navigate = useNavigate()
  const messageRef = useRef(null)
  const subscriptionRef = useRef(null);
  const privateMessageSubscriptionRef = useRef(null);
  const fileInputRef = useRef(null);
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
  const [blockedUsers, setBlockedUsers] = useState([]);
  const stompClient = useRef(null);
  const [userPictures, setUserPictures] = useState({});
  const [announcements, setAnnouncements] = useState([]);
  
  




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
      fetchUsers();
      fetchPendingRequests();
      fetchFriends();
      fetchBlocked();
      fetchAnnouncements();
      valami();


      const token = sessionStorage.getItem('token');
      const encodedToken = encodeURIComponent(token);
      const socket = new SockJS(`http://localhost:8080/ws?token=${encodedToken}`);
    stompClient.current = Stomp.over(socket);
    stompClient.current.connect({}, () => {
      stompClient.current.subscribe('/topic/announcements', (announcement) => {
        const data = JSON.parse(announcement.body);
        console.log('Announcement received:', data);
        setAnnouncements((prev) => [data, ...prev]); // legfrissebb előre
      });
      stompClient.current.subscribe('/topic/contacts', (message) => {
        const data = JSON.parse(message.body);
        console.log('Contact request received:', data);
        fetchPendingRequests(); // Frissítjük a várakozó kéréseket
        fetchBlocked();
        fetchFriends();
      })
    })
    }
  }, [currentUser]);

  const lastMessageRef = useRef(null);

useEffect(() => {
  if (lastMessageRef.current) {
    lastMessageRef.current.scrollIntoView();
  }
}, [messages]);


  useEffect(() => {
    const loadUserPictures = async () => {
      const pictures = {};
      for (const user of users) {
        try {
          const response = await authService.getUserProfilePicture(user.id);
          pictures[user.id] = `http://localhost:8080${response.data.picturePath}`;
        } catch (error) {
          console.error(`Error fetching picture for user ${user.id}`, error);
          pictures[user.id] = '/default-profile.png'; // Egy alapértelmezett kép
        }
      }
      const current = await authService.getUserProfilePicture(currentUser.userId);
      pictures[currentUser.userId] = `http://localhost:8080${current.data.picturePath}`
      setUserPictures(pictures);
    };
  
    if (users.length > 0) {
      loadUserPictures();
    }
  }, [users]);

  useEffect(() => {
    if (chats.length > 0) {
      valami();
    }
  }, [chats]);

  useEffect(() => {
    if (selectedChat && selectedChat.type !== 'announcement') {
      connectWebSocket();
    }
  }, [selectedChat]);

  const connectWebSocket = () => {
    const token = sessionStorage.getItem('token');
  
    if (!token) {
      console.error("No token found in sessionStorage.");
      return;
    }
  
    const encodedToken = encodeURIComponent(token);
    const socket = new SockJS(`http://localhost:8080/ws?token=${encodedToken}`);
    stompClient.current = Stomp.over(socket);
  
    stompClient.current.connect({}, () => {
      console.log('✅ WebSocket connected');
  
      // Global announcement subscription (only once)
      if (!subscriptionRef.current) {
        subscriptionRef.current = stompClient.current.subscribe('/topic/announcements', (announcement) => {
          const data = JSON.parse(announcement.body);
          setAnnouncements((prev) => [data, ...prev]);
        });
      }
  
      // Always resubscribe to private messages
      if (privateMessageSubscriptionRef.current) {
        privateMessageSubscriptionRef.current.unsubscribe();
      }
  
      privateMessageSubscriptionRef.current = stompClient.current.subscribe('/user/queue/messages', (message) => {
        if (message.body) {
            const receivedMessage = JSON.parse(message.body);
            console.log("📨 New private message:", receivedMessage);
    
            if (receivedMessage.content && receivedMessage.chatId === selectedChat?.id) {
                const isFile = typeof receivedMessage.content === "string" && receivedMessage.content.includes("/files/");
                setMessages((prevMessages) => [...prevMessages, {
                    sender: receivedMessage.sender,
                    message: receivedMessage.content,
                    createdAt: receivedMessage.timestamp || new Date(),
                    type: isFile ? "file" : "text"
                }]);
            }
        }
    });
  
      fetchMessages();
    }, (error) => {
      console.error('❌ WebSocket connection failed:', error);
    });
  };
  
  

  const fetchAnnouncements = async () => {
    try {
      const response = await authService.getAnnouncements();
      setAnnouncements(response.data);
    } catch (error) {
      console.error('Error fetching announcements', error);
    }
  };

  const fetchMessages = async () => {
    try {
      const response = await authService.getMessages(selectedChat.id);
      setMessages(response.data);
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
    } catch (error) {
      console.error('Error fetching chat details', error);
    }
  };

  const handleLogout = (e) => {
    e.stopPropagation()  
    sessionStorage.removeItem('token');
    sessionStorage.removeItem('userId');
  };

  const fetchUsers = async () => {
    try {
      const response = await authService.getAllUsers();
      setUsers(response.data.filter(user => user.id !== Number(currentUser.userId)));
    } catch (error) {
      console.error('Error fetching users', error);
    }
  };

  const  deleteContact = async (contactUserId)=>{
      try {
        const response = await authService.deleteContact( contactUserId,currentUser.userId);
        alert('Friend unblocked!');
        fetchUsers();
      }catch (error) {
        alert("You can't unblock users that have blocked you!")
      }
  }

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
      setFriends(response.data);
    } catch (error) {
      console.error('Error fetching friends', error);
    }
  };

  const fetchBlocked = async () => {
    try {
      const response = await authService.getContactsByStatus('BLOCKED');
      setBlockedUsers(response.data);
    } catch (error) {
      console.error('Error fetching blocked users', error);
    }

  }

  const handleAddFriend = async (contactUserId) => {
    try {
      await authService.addContact(currentUser.userId, contactUserId);
      alert('Friend request sent!');
    } catch (error) {
      console.error('Error adding friend', error);
    }
  };

  const disconnectWebSocket = () => {
    if (privateMessageSubscriptionRef.current) {
      privateMessageSubscriptionRef.current.unsubscribe();
      privateMessageSubscriptionRef.current = null;
    }
    // Nem hívunk teljes disconnect-et, így az announcements aktív marad
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
      const friendName = friend.userName === currentUser.userName ? friend.contactUserName : friend.userName;

      const existingChat = chatDetails.find(chat =>
        (chat.user1Name === currentUser.userName && chat.user2Name === friendName) ||
        (chat.user2Name === currentUser.userName && chat.user1Name === friendName)
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

  const handleUpdateStatus = async (userId, contactUserId, status) => {
    try {
      await authService.updateContactStatus(userId, contactUserId, status);
      fetchPendingRequests();
      fetchFriends();
      fetchBlocked();
    } catch (error) {
      console.error('Error updating contact status', error);
    }
  };

  const notificationBadgeLenght = pendingRequests.filter(contact => contact.contactUserName === currentUser.userName).length

  const handleFileUpload = async (e) => {
    const file = e.target.files[0];
    if (!file || !selectedChat) return;

    const senderId = currentUser.userId;
    const chatId = selectedChat.id;

    const chatDetail = chatDetails.find(chat => chat.id === chatId);
    const receiverUserName = chatDetail.user1Name === currentUser.userName
        ? chatDetail.user2Name
        : chatDetail.user1Name;

    const receiver = users.find(u => u.userName === receiverUserName);
    if (!receiver) return alert("Receiver not found");

    try {
        const response = await authService.uploadChatFile(senderId, receiver.id, chatId, file);
        console.log("File upload response:", response.data); // Debugging line
        const filePath = response.data; // Ensure this is the correct field for the file path
        console.log("File uploaded successfully:", filePath);
        fetchMessages();

        // Send a WebSocket message to notify the receiver
        if (stompClient.current && stompClient.current.connected) {
            const message = {
                sender: currentUser.userName,
                content: filePath, // Use only the file path as the content
                chatId: chatId,
                type: "file"
            };
            stompClient.current.send('/app/chat', {}, JSON.stringify(message));
        }
    } catch (err) {
        console.error("File upload failed:", err);
        alert("Failed to send file.");
    }

    // Clear input to allow re-upload of the same file
    e.target.value = null;
};
  

  const handleAddIconClick = () => {
    fileInputRef.current?.click();
  };


  if (!currentUser) {
    return <div>Please log in to view your dashboard.</div>;
  }
  return (
    <div className="dashboard-container">
      {/* Header */}
      <header className="dashboard-header">
        <div className="header-left">
          <h1 className="app-title">ChatApp</h1>
        </div>
        <div className="header-right">
          <div className="user-info" onClick={() => {
            navigate("/profile")
          }}>
            <img 
              src={userPictures[currentUser.userId] || '/default-profile.png'} 
              alt="Profile" 
              className="profile-pic-small"
            />
            <div className="right-side">
              <span className="welcome-message">Welcome, {currentUser.userName}!</span>
              <div className="logout-wrapper">
                {currentUser.role == "ADMIN" && (
                  <>
                    <Link to={"/admin"} className="nav-link" onClick={(e) => {
                        navigate("/admin")
                        e.stopPropagation()
                      }}>
                        <button className="admin-btn">Admin</button>
                    </Link>
                  </>
                )}
                
                <Link to="/login" className="nav-link" onClick={handleLogout}>
                  <button className="logout-btn">Logout</button>
                </Link>
              </div>
            </div>
          </div>
          <nav className="header-nav">
            {/* <Link to="/profile" className="nav-link-profile">Profile</Link> */}
            {/* <Link to="/login" className="nav-link" onClick={handleLogout}>
              <button className="logout-btn">Logout</button>
            </Link> */}
          </nav>
        </div>
      </header>
  
      {/* Navigation Tabs */}
      <nav className="tab-navigation">
        <button 
          className={`tab-button ${activeTab === 'chats' ? 'active' : ''}`}
          onClick={() => setActiveTab('chats')}
        >
          Chats
        </button>
        <button 
          className={`tab-button ${activeTab === 'contacts' ? 'active' : ''}`}
          onClick={() => setActiveTab('contacts')}
        >
          All Users
        </button>
        <button 
          className={`tab-button ${activeTab === 'pending' ? 'active' : ''}`}
          onClick={() => setActiveTab('pending')}
        >
          <span className="button-span">Pending Requests 
          {notificationBadgeLenght > 0 && (
            
            <span className="notification-badge">{notificationBadgeLenght}</span>
          )}
          </span>
          
        </button>
        <button 
          className={`tab-button ${activeTab === 'friends' ? 'active' : ''}`}
          onClick={() => setActiveTab('friends')}
        >
          Friends
        </button>
      </nav>
  
      {/* Main Content */}
      <div className="main-content">
        {/* Left Side - Chat List/User List */}
        <div className="left-sidebar">
          {activeTab === 'chats' && (
            <div className="chat-list-container">
              <h2>Chats</h2>
              <ul className="chat-list">
              {announcements.length > 0 && (
                <li 
                  key="announcement" 
                  className={`chat-item ${selectedChat?.id === 'announcement' ? 'active' : ''}`}
                  onClick={() => {
                    setSelectedChat({ id: 'announcement', type: 'announcement' });
                    setMessages([...announcements]
                      .sort((a, b) => new Date(a.createdAt) - new Date(b.createdAt)) // növekvő időrend
                      .map(a => ({
                        sender: a.senderUsername,
                        message: a.message,
                        createdAt: a.createdAt,
                        isAnnouncement: true
                      }))
                    );
                  }}
                >

                  <div className="chat-info">
                    <CampaignIcon className='icon'></CampaignIcon>
                    <span className="chat-name"> Announcements</span>
                  </div>
                </li>
              )}
                {chats.map((chat) => {
                  const chatDetail = chatDetails.find(detail => detail.id === chat.id);
                  if (!chatDetail) return null;
  
                  const otherUser = chatDetail.user1Name === currentUser.userName 
                    ? chatDetail.user2Name 
                    : chatDetail.user1Name;
  
                  return (
                    <li 
                      key={chat.id} 
                      className={`chat-item ${selectedChat?.id === chat.id ? 'active' : ''}`}
                      onClick={() => handleChatClick(chat)}
                    >
                      <div className="chat-avatar">
                        {users.map(user => {  
                          if (user.userName === otherUser) {
                            return (
                              <img
                                key={`friend-img-${user.id}`}
                                src={userPictures[user.id] || '/default-profile.png'}
                                alt={`${user.userName}'s profile`}
                              />
                            )
                          }
                          return null;
                        })}
                      </div>
                      <div className="chat-info">
                        <span className="chat-name">{otherUser || "Unknown User"}</span>
                      </div>
                    </li>
                  );
                })}
              </ul>
            </div>
          )}
  
          {activeTab === 'contacts' && (
            <div className="user-list-container">
              <h2>All Users</h2>
              <ul className="user-list">
                {users.map(user => (
                  <li key={user.id} className="user-item">
                    <div className="user-avatar">
                      <img
                        src={userPictures[user.id] || 'http://localhost:8080/images/basic/basic.png'}
                        alt={`${user.userName}'s profile`}
                      />
                    </div>
                    <div className="user-info1">
                      <span className="user-name">{user.userName}</span>
                      {blockedUsers.some(blocked => blocked.userId === user.id || blocked.contactUserId === user.id) ? (
                        <span onClick={()=>{
                          try{
                            deleteContact(user.id)
                            fetchBlocked();
                          }catch (error) {
                            console.error('Error deleting contact');
                          }
                        }} className="blocked">Blocked</span>
                      ) : friends.some(friend => friend.contactUserId === user.id || friend.userId === user.id) ? (
                        <span className="friend-status">Friends</span>
                      ) : pendingRequests.some(request => request.contactUserId === user.id) ? (
                        <span className="pending">Pending</span>
                      ) : (
                        <button 
                          className="add-friend-btn"
                          onClick={() => handleAddFriend(user.id)}
                        >
                          Add Friend
                        </button>
                      )}
                    </div>
                  </li>
                ))}
              </ul>
            </div>
          )}
  
          {activeTab === 'pending' && (
            <div className="pending-list-container">
              <h2>Pending Requests</h2>
              <ul className="pending-list">
                {pendingRequests.map(request => (
                  <li key={request.id} className="request-item">
                    {request.contactUserName === currentUser.userName && (
                      <>
                        <div className="request-avatar">
                          <img
                            src={userPictures[request.userId] || '/default-profile.png'}
                            alt={`${request.contactUserName}'s profile`}
                          />
                        </div>
                        <div className="request-info">
                          <span className="request-name">{request.userName}</span>
                          <div className="request-actions">
                            <button 
                              className="accept-btn"
                              onClick={() => handleUpdateStatus( currentUser.userId,request.userId, 'ACCEPTED')}
                            >
                              Accept
                            </button>
                            <button 
                              className="reject-btn"
                              onClick={() => handleUpdateStatus( currentUser.userId,request.userId, 'BLOCKED')}
                            >
                              Reject
                            </button>
                          </div>
                        </div>
                      </>
                    )}
                  </li>
                ))}
              </ul>
            </div>
          )}
  
          {activeTab === 'friends' && (
            <div className="friends-list-container">
              <h2>Friends</h2>
              <ul className="friends-list">
                {friends.map(friend => (
                  <li key={friend.id} className="friend-item">
                    <div className="friend-avatar">
                      <img
                        src={friend.userName === currentUser.userName 
                          ? userPictures[friend.contactUserId] || '/default-profile.png'
                          : userPictures[friend.userId] || '/default-profile.png'}
                        alt={friend.userName === currentUser.userName 
                          ? `${friend.contactUserName}'s profile`
                          : `${friend.userName}'s profile`}
                      />
                    </div>
                    <div className="friend-info">
                      <span className="friend-name">
                        {friend.userName === currentUser.userName 
                          ? friend.contactUserName 
                          : friend.userName}
                      </span>
                      <div className="friend-actions">
                        <button
                          className="chat-btn"
                          onClick={() => handleStartChat(friend)}
                        >
                          Chat
                        </button>
                        <button
                          className="delete-btn"
                          style={{backgroundColor:"red", color:"white"}}
                          onClick={() => handleDeleteFriend(friend)}
                        >
                          Remove
                        </button>
                      </div>
                    </div>
                  </li>
                ))}
              </ul>
            </div>
          )}
        </div>
  
        {/* Right Side - Chat Area */}
        <div className="chat-area">
          {selectedChat ? (
            <>
              <div className="chat-header">
                {(() => {
                  const chatDetail = chatDetails.find(detail => detail.id === selectedChat.id);
                  if (!chatDetail) return null;
                  const otherUser = chatDetail.user1Name === currentUser.userName 
                    ? chatDetail.user2Name 
                    : chatDetail.user1Name;
                  
                  return (
                    <>
                      <div className="chat-partner">
                        <div className="partner-avatar">
                          {users.map(user => {  
                            if (user.userName === otherUser) {
                              return (
                                <img
                                  key={`chat-img-${user.id}`}
                                  src={userPictures[user.id] || '/default-profile.png'}
                                  alt={`${user.userName}'s profile`}
                                />
                              )
                            }
                            return null;
                          })}
                        </div>
                        <span className="partner-name">{otherUser}</span>
                      </div>
                      <button
                        className="delete-chat-btn"
                        style={{backgroundColor:"red", color:"white"}}
                        onClick={() => handleDeleteChat(selectedChat.id)}
                      >
                        Delete Chat
                      </button>
                    </>
                  );
                })()}
              </div>

              <div className="messages-container" id={"messages-container-id"}>
                {messages.length === 0 ? (
                  <div className="empty-chat">
                    <p>No messages yet. Start the conversation!</p>
                  </div>
                ) : (
                  <ul className="messages-list" id="messages-list-id" ref={messageRef}>
                    {messages.map((message, index) => (
                      <li 
                        key={index} 
                        className={`message ${
                          message.isAnnouncement
                            ? 'received'
                            : message.sender === currentUser.userName
                            ? 'sent'
                            : 'received'
                        }`}
                      ref={index === messages.length -1 ? lastMessageRef : null}

                      >
                        <div className="message-content" style={{alignItems:message.sender === currentUser.userName?"end":"start"}}>
                          {message.sender !== currentUser.userName && (
                            <span className="sender-name">{message.sender}</span>
                          )}
                          <div className="message-bubble">
                            {message.type === "file" ? (
                              <a 
                                href={`http://localhost:8080${message.message}`} 
                                download 
                                target="_blank" 
                                rel="noopener noreferrer"
                                style={{ textDecoration: 'underline', color: 'white' }}
                              >
                                {message.message.split("/").pop()}
                              </a>
                            )  : (
                              message.message
                            )}
                          </div>
                          <span className="message-time">
                            {new Date(message.createdAt).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})}
                          </span>
                        </div>
                      </li>
                    ))}
                  </ul>
                )}
              </div>
              {selectedChat.type !== 'announcement' && (
                <form className="message-form" onSubmit={handleSendMessage}>
                    <AddIcon onClick={handleAddIconClick}/>
                  <input
    ref={fileInputRef}
    type="file"
    style={{ display: 'none' }}
    onChange={handleFileUpload}
  />
                <input
                  type="text"
                  value={newMessage}
                  onChange={(e) => setNewMessage(e.target.value)}
                  placeholder="Type a message..."
                  required
                />
                <button type="submit" className="send-btn">Send</button>
              </form>
              )}
              
            </>
          ) : (
            <div className="no-chat-selected">
              <div className="welcome-illustration">
                <i className="fas fa-comments"></i>
              </div>
              <h2>Welcome to ChatApp</h2>
              <p>Select a chat to start messaging or find new friends</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

export default Dashboard;
