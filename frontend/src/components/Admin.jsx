import { useEffect, useState } from "react";
import authService from "../services/authService";
import { useNavigate } from "react-router-dom";
import "./Admin.css";

function Admin() {
  const [users, setUsers] = useState([]);
  const [filteredUsers, setFilteredUsers] = useState([]);
  const [searchTerm, setSearchTerm] = useState("");
  const [currentUser, setCurrentUser] = useState(authService.getCurrentUser());
  const [newAnnouncement, setNewAnnouncement] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    if (!currentUser) {
      navigate('/login');
      return;
    }
    fetchUsers();
  }, []);

  const fetchUsers = async () => {
    try {
      const response = await authService.getAllUsers();
      const otherUsers = response.data.filter(user => user.id !== Number(currentUser.userId));
      setUsers(otherUsers);
      setFilteredUsers(otherUsers);
    } catch (error) {
      console.error("Error fetching users", error);
    }
  };



  const handleSearch = (e) => {
    const term = e.target.value.toLowerCase();
    setSearchTerm(term);
    setFilteredUsers(users.filter(user =>
      user.userName.toLowerCase().includes(term) ||
      user.email.toLowerCase().includes(term) ||
      user.phoneNumber.toLowerCase().includes(term)
    ));
  };

  const handleBackToDashboard = () => {
    navigate('/dashboard');
  }

  const handleDelete = async (id) => {
    try {
      await authService.deleteUser(id);
      const updatedUsers = users.filter(u => u.id !== id);
      setUsers(updatedUsers);
      setFilteredUsers(updatedUsers);
      alert("User deleted successfully");
    } catch (err) {
      console.error("Error deleting user", err);
      alert("Error deleting user");
    }
  };


  const handlePostAnnouncement = async () => {
    try{
      await authService.addAnnouncement({message: newAnnouncement});
      setNewAnnouncement('');
      alert("Announcement posted successfully");
    }catch (err){
      console.error("Error posting announcement", err);
      alert("Error posting announcement");
    }
  };

  return (
    <div className="admin-container">
      <div className="admin-header">
        <h1 className="admin-title">Admin Dashboard</h1>
        <button className="back-button" onClick={handleBackToDashboard}>
              Back to Dashboard
            </button>
    
        <div className="header-sections">
          <div className="header-section">
            <p className="admin-subtitle">Manage all users</p>
              <input
                type="text"
                className="search-input"
                placeholder="Search by username, email or phone..."
                value={searchTerm}
                onChange={handleSearch}
              />
            
          </div>

          <div className="header-section">
            <p className="admin-subtitle">Post new announcement</p>
            <textarea
              value={newAnnouncement}
              onChange={(e) => setNewAnnouncement(e.target.value)}
              placeholder="Write your announcement here..."
              className="announcement-input"
              rows="4"
            />
            <button
              className="post-announcement-button"
              onClick={handlePostAnnouncement}
            >
              Post Announcement
            </button>
          </div>
      </div>
    </div>


      

      <div className="user-list2">
        {filteredUsers.length > 0 ? (
          filteredUsers.map(user => (
            <div key={user.id} className="user-card">
              <h2 className="user-name1">{user.userName}</h2>
              <p className="user-info">Email: {user.email}</p>
              <p className="user-info">Phone: {user.phoneNumber}</p>
              <p className="user-info">Age: {user.age}</p>
              <button className="delete-button" onClick={() => handleDelete(user.id)}>
                Delete User
              </button>
            </div>
          ))
        ) : (
          <p className="no-results">No users found.</p>
        )}
      </div>
    </div>
  );
}

export default Admin;
