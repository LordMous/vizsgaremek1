import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import authService from '../../services/authService';
import './Register.css';

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
      alert('Registration failed: ' + (error.response?.data?.message || error.message));
    }
  };

  return (
    <div className="register-container">
      <div className="register-box">
        <p className="subtitle">Create your account</p>
        <p className="welcome">Join our community</p>
        
        <form onSubmit={handleSubmit}>

          <div className="form-group">
            <label>Username</label>
            <input 
              type="text" 
              value={userName} 
              onChange={(e) => setUserName(e.target.value)} 
              required 
              placeholder="Choose a username"
            />
          </div>

          <div className="form-group">
            <label>Email address</label>
            <input 
              type="email" 
              value={email} 
              onChange={(e) => setEmail(e.target.value)} 
              required 
              placeholder="Enter your email"
            />
          </div>
          
          <div className="form-group">
            <label>Password</label>
            <input 
              type="password" 
              value={password} 
              onChange={(e) => setPassword(e.target.value)} 
              required 
              placeholder="Create a password"
            />
          </div>
          
          <div className="form-row">
            <div className="form-group half-width">
              <label>Age</label>
              <input 
                type="number" 
                value={age} 
                onChange={(e) => setAge(e.target.value)} 
                required 
                min="13"
                max="120"
                placeholder="Age"
              />
            </div>
            
            <div className="form-group half-width">
              <label>Phone Number</label>
              <input 
                type="tel" 
                value={phoneNumber} 
                onChange={(e) => setPhoneNumber(e.target.value)} 
                required 
                placeholder="Phone number"
              />
            </div>
          </div>
          
          <button type="submit" className="register-button">Create Account</button>
        </form>
        
        <div className="divider"></div>
        
        <p className="login-link">
          Already have an account? <a href="/login">Sign in</a>
        </p>
      </div>
    </div>
  );
}

export default Register;