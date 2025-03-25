import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import authService from '../services/authService';

function Home() {
  const [userData, setUserData] = useState(null);

  useEffect(() => {
    const fetchUserData = async () => {
      try {
        const response = await authService.getUserData();
        setUserData(response.data);
      } catch (error) {
        console.error('Error fetching user data', error);
      }
    };

    fetchUserData();
  }, []);

  return (
    <div>
      <h1>Welcome to the Homepage</h1>
      {userData ? (
        <div>
          <h2>Hello, {userData.userName}</h2>
        </div>
      ) : (
        <nav>
          <ul>
            <li><Link to="/register">Register</Link></li>
            <li><Link to="/login">Login</Link></li>
          </ul>
        </nav>
      )}
    </div>
  );
}

export default Home;