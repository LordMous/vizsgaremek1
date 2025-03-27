import React, { useEffect, useState } from 'react';
import authService from '../services/authService';

function FriendsList() {
  const [friends, setFriends] = useState([]);
  const [currentUser, setCurrentUser] = useState(authService.getCurrentUser());

  useEffect(() => {
    const fetchFriends = async () => {
        try {
          const response = await authService.getContactsByStatus('ACCEPTED');
          console.log(response.data)
          setFriends(response.data);
        } catch (error) {
          console.error('Error fetching friends', error);
        }
      };

    if (currentUser) {
      fetchFriends();
    }
  }, [currentUser]);

  return (
    <div>
      <h2>Friends</h2>
      <ul>
        {friends.map(friend => (
          console.log(friend),
          <li key={friend.id}>{friend.contactUserName}</li>
        ))}
      </ul>
    </div>
  );
}

export default FriendsList;
