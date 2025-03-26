import React, { useState, useEffect } from 'react';
import authService from '../services/authService';
import { useNavigate } from 'react-router-dom';

function Profile() {
  const [userData, setUserData] = useState({
    userName: '',
    email: '',
    phoneNumber: '',
    age: '',
  });
  const [isEditing, setIsEditing] = useState(false);
  const navigate = useNavigate();

  // Get current user and userId from token
  const currentUser = authService.getCurrentUser();

  const userId = currentUser?.userId; // Feltételezve, hogy a token tartalmaz userId-t

  useEffect(() => {
    if (!currentUser) {
      navigate('/login');
      return;
    }
    console.log("Profile");
    const fetchUserData = async () => {
      try {
        if (userId) {
          const response = await authService.getUserData(userId);
          setUserData(response.data);
        }
      } catch (error) {
        console.error('Error fetching user data', error);
        if (error.response?.status === 401) {
          sessionStorage.removeItem('token');
          navigate('/login');
        }
      }
    };

    fetchUserData();
  }, [ navigate]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setUserData((prevData) => ({ ...prevData, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      if (!userId) {
        throw new Error('User ID is missing');
      }

      await authService.updateUser({ 
        ...userData, 
        id: userId // userId használata a frissítéshez
      });
      
      alert('Profile updated successfully!');
      setIsEditing(false);
      
      // Frissítjük az adatokat a mentés után
      const response = await authService.getUserData(userId);
      setUserData(response.data);
    } catch (error) {
      console.error('Error updating profile', error);
      alert(error.message || 'Failed to update profile.');
      
      if (error.response?.status === 401) {
        sessionStorage.removeItem('token');
        navigate('/login');
      }
    }
  };

  const handleCancel = () => {
    setIsEditing(false);
    // Visszatöltjük az eredeti adatokat
    if (userId) {
      authService.getUserData(userId)
        .then(response => setUserData(response.data))
        .catch(error => console.error('Error reloading user data', error));
    }
  };
  const handleBackToDashboard = () => {
    navigate('/dashboard');
  };   

  if (!currentUser) {
    return <div>Redirecting to login...</div>;
  }

  return (
    <div className="profile-container">
      <h2>Profile</h2>
      {!isEditing ? (
        <div className="profile-view">
          <p><strong>Username:</strong> {userData.userName}</p>
          <p><strong>Email:</strong> {userData.email}</p>
          <p><strong>Phone Number:</strong> {userData.phoneNumber}</p>
          <p><strong>Age:</strong> {userData.age}</p>
          <button 
            className="edit-button"
            onClick={() => setIsEditing(true)}
          >
            Edit
          </button>

          <button
          className="back-button"
          onClick={handleBackToDashboard}
          >
            Back to the dashboard
          </button>
        </div>
      ) : (
        <form onSubmit={handleSubmit} className="profile-form">
          <div className="form-group">
            <label>Username:</label>
            <input
              type="text"
              name="userName"
              value={userData.userName}
              onChange={handleChange}
              required
            />
          </div>
          <div className="form-group">
            <label>Email:</label>
            <input
              type="email"
              name="email"
              value={userData.email}
              onChange={handleChange}
              required
            />
          </div>
          <div className="form-group">
            <label>Phone Number:</label>
            <input
              type="text"
              name="phoneNumber"
              value={userData.phoneNumber}
              onChange={handleChange}
              required
            />
          </div>
          <div className="form-group">
            <label>Age:</label>
            <input
              type="number"
              name="age"
              value={userData.age}
              onChange={handleChange}
              required
            />
          </div>
          <div className="form-actions">
            <button type="submit" className="save-button">Save</button>
            <button 
              type="button" 
              onClick={handleCancel}
              className="cancel-button"
            >
              Cancel
            </button>
          </div>
        </form>
      )}
    </div>
  );
}

export default Profile;