import React, { useEffect, useState } from 'react';
import authService from '../../services/authService';

function ContactList() {
  const [users, setUsers] = useState([]);
  const [friends, setFriends] = useState([]);
  const [currentUser, setCurrentUser] = useState(authService.getCurrentUser());

  useEffect(() => {
    const fetchUsersAndFriends = async () => {
      try {
        const usersResponse = await authService.getAllUsers();
        const friendsResponse = await authService.getContactsByStatus('ACCEPTED');
  
        console.log(friendsResponse.data);
        
        setUsers(usersResponse.data.filter(user => user.id !== currentUser.id));
  
        // Barátok listája objektumként, hogy mindkét irányt kezelje
        setFriends(friendsResponse.data.map(friend => ({
          friendId: friend.userId === currentUser.id ? friend.contactUserId : friend.userId,
          friendName: friend.userId === currentUser.id ? friend.contactUserName : friend.userName
        })));
      } catch (error) {
        console.error('Error fetching users or friends', error);
      }
    };
  
    if (currentUser) {
      fetchUsersAndFriends();
    }
  }, [currentUser]);
  
  // Ellenőrzés, hogy a user barát-e
  const isFriend = (userId) => {
    return friends.some(friend => friend.friendId === userId);
  };
  
  // A barát nevének lekérése
  const getFriendName = (userId) => {
    const friend = friends.find(friend => friend.friendId === userId);
    return friend ? friend.friendName : 'Unknown';
  };

  const handleAddFriend = async (contactUserId) => {
    try {
      await authService.addContact(currentUser.id, contactUserId);
      alert('Friend request sent!');
    } catch (error) {
      console.error('Error adding friend', error);
    }
  };



  return (
    <div>
      <h2>All Users</h2>
      <ul>
        {users.map(user => (
          <li key={user.id}>
            {user.userName}{' '}
            {isFriend(user.id) ? (
              <span>(You are already friends with {getFriendName(user.id)})</span>
            ) : (
              <button onClick={() => handleAddFriend(user.id)}>Add Friend</button>
            )}
          </li>
        ))}
      </ul>
    </div>
  );
}

export default ContactList;
