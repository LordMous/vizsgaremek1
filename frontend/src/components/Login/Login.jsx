import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import authService from '../../services/authService.js';
import "./Login.css"
import "../Register/Register.jsx"

function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [rememberMe, setRememberMe] = useState(false);
  const navigate = useNavigate();
  
  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const response = await authService.login({ email, password });
      console.log(response.data.role);
      if (rememberMe) {
        sessionStorage.setItem('token', response.data.token);
        sessionStorage.setItem('userId', response.data.userId);
        sessionStorage.setItem('role', response.data.role);
      } else {
        sessionStorage.setItem('token', response.data.token);
        sessionStorage.setItem('userId', response.data.userId);
        sessionStorage.setItem('role', response.data.role);
      }
      navigate('/dashboard');
      console.log()
    } catch (error) {
      alert('Invalid email or password');
    }
  };

  return (
    <div className="login-container">
      <div className="login-box">
        <p className="subtitle">Please enter your details</p>
        <p className="welcome">Welcome back</p>
        
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Email address</label>
            <input 
              type="email" 
              value={email} 
              onChange={(e) => setEmail(e.target.value)} 
              required 
            />
          </div>
          <div className="form-group">
            <label>Password</label>
            <input 
              type="password" 
              value={password} 
              onChange={(e) => setPassword(e.target.value)} 
              required 
            />
          </div>
          
          <button type="submit" className="signin-button">Sign in</button>
        </form>
        
        <div className="divider"></div>
        
        <p className="signup-link">
          Don't have an account? <a href="/register">Sign up</a>
        </p>
      </div>
    </div>
  );
}

export default Login;