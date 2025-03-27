import React, { useEffect, useState } from 'react';
import authService from '../services/authService';

function PendingRequests() {
  const [pendingRequests, setPendingRequests] = useState([]);
  const [currentUser, setCurrentUser] = useState(authService.getCurrentUser());
  console.log(currentUser)

  useEffect(() => {
    const fetchPendingRequests = async () => {
        try {
          const response = await authService.getContactsByStatus('PENDING');
          console.log(response.data)
          setPendingRequests(response.data);
        } catch (error) {
          console.error('Error fetching pending requests', error);
        }
      };

    if (currentUser) {
      fetchPendingRequests();
    }
  }, [currentUser]);


  const handleUpdateStatus = async (contactUserId, status) => {
    console.log(currentUser)
    try {
      await authService.updateContactStatus(currentUser.userId,contactUserId,  status);
      setPendingRequests(prev => prev.filter(req => req.contactUserId !== contactUserId));
    } catch (error) {
      console.error('Error updating contact status', error);
    }
  };

  return (
    <div>
      <h2>Pending Requests</h2>
      <ul>
        {pendingRequests.map(request => (
          <li key={request.id}>
            {request.contactUserName} 
            <button onClick={() => handleUpdateStatus(request.contactUserId, 'ACCEPTED')}>Accept</button>
            <button onClick={() => handleUpdateStatus(request.contactUserId, 'BLOCKED')}>Reject</button>
          </li>
        ))}
      </ul>
    </div>
  );
}

export default PendingRequests;
