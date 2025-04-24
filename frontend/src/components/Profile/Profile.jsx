import React, { useState, useEffect } from 'react';
import authService from '../../services/authService';
import { useNavigate } from 'react-router-dom';
import "./Profile.css";

function Profile() {
  const [userData, setUserData] = useState({
    userName: '',
    email: '',
    phoneNumber: '',
    age: '',
  });

  const [isEditing, setIsEditing] = useState(false);
  const [profilePicture, setProfilePicture] = useState(null);
  const [profilePictureUrl, setProfilePictureUrl] = useState('');

  const navigate = useNavigate();
  const currentUser = authService.getCurrentUser();
  const userId = currentUser?.userId;

  useEffect(() => {
    if (!currentUser) {
      navigate('/login');
      return;
    }

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

   

    const fetchProfilePicture = async () => {
      try {
        if (userId) {
          const response = await authService.getUserProfilePicture(userId);
          if (response.data.hasPicture) {
            setProfilePictureUrl(response.data.picturePath);
          }
        }
      } catch (error) {
        console.error('Error fetching profile picture', error);
      }
    };

    fetchUserData();
    fetchProfilePicture();
  }, [userId, navigate]);

  const handleFileChange = (e) => {
    setProfilePicture(e.target.files[0]);
  };

  const handleFileUpload = async () => {
    if (!profilePicture) {
      alert('Please select a file to upload.');
      return;
    }

    try {
      const response = await authService.uploadProfilePicture(userId, profilePicture);
      alert('Profile picture uploaded successfully!');
      setProfilePictureUrl(response.data.fullPath);
      setProfilePicture(null);
    } catch (error) {
      console.error('Error uploading profile picture', error);
      alert('Failed to upload profile picture.');
    }
  };

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
        id: userId 
      });

      alert('Profile updated successfully!');
      setIsEditing(false);
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

  const handlePfpClick = () => {
    
    document.getElementById("pfpUpload").click()
  }

  return (
    <div className="profile-wrapper">
      <div className="profile-container">
            <h2>Profile</h2>
            

            <div className="profile-picture-container">
              {profilePictureUrl && (
                <img
                  src={`http://localhost:8080${profilePictureUrl}`} // Helyes elérési útvonal
                  alt="Profile"
                  className="profile-picture"
                  onClick={handlePfpClick}
                />
              )}

              <label>Upload Profile Picture:</label>
              <label className='description'>First select a picture by clicking on the picture!</label>
              <input type="file" id={"pfpUpload"} onChange={handleFileChange} />
              <button onClick={handleFileUpload}>Upload</button>
            </div>

            {!isEditing ? (
              <div className="profile-view">
                <p><strong>Username:</strong> {userData.userName}</p>
                <p><strong>Email:</strong> {userData.email}</p>
                <p><strong>Phone Number:</strong> {userData.phoneNumber}</p>
                <p><strong>Age:</strong> {userData.age}</p>
                <div className="profile-buttons-row">
                  <button className="edit-button" onClick={() => setIsEditing(true)}>
                    Edit
                  </button>
                  <button className="back-button" onClick={handleBackToDashboard}>
                    Back to the dashboard
                  </button>
                </div>

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
                  <button type="button" onClick={handleCancel} className="cancel-button">
                    Cancel
                  </button>
                </div>
              </form>
            )}
          </div>
    </div>
    
  );
}

export default Profile;
