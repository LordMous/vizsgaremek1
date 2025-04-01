import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import authService from '../services/authService';

function Home() {

  return (
    <div>
      <h1>Welcome to the Homepage</h1>
     
        <nav>
          <ul>
            <li><Link to="/register">Register</Link></li>
            <li><Link to="/login">Login</Link></li>
          </ul>
        </nav>
      
    </div>
  );
}

export default Home;