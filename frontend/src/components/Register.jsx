import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import authService from '../services/authService';

function Register() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [userName, setUserName] = useState('');
  const [age, setAge] = useState('');
  const [phoneNumber, setPhoneNumber] = useState('');
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await authService.register({ email, password, userName, age, phoneNumber });
      navigate('/login');
    } catch (error) {
      alert('Registration failed');
    }
  };

  return (
    <div>
      <h1>Register</h1>
      <form onSubmit={handleSubmit}>
        <div>
          <label>Email:</label>
          <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
        </div>
        <div>
          <label>Password:</label>
          <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} required />
        </div>
        <div>
          <label>Username:</label>
          <input type="text" value={userName} onChange={(e) => setUserName(e.target.value)} required />
        </div>
        <div>
          <label>Age:</label>
          <input type="number" value={age} onChange={(e) => setAge(e.target.value)} required />
        </div>
        <div>
          <label>Phone Number:</label>
          <input type="text" value={phoneNumber} onChange={(e) => setPhoneNumber(e.target.value)} required />
        </div>
        <button type="submit">Register</button>
      </form>
    </div>
  );
}

export default Register;