  import axios from 'axios';

  const API_URL = 'http://localhost:8080';

  const register = (registerRequest) => {
    return axios.post(`${API_URL}/auth/register`, registerRequest);
  };

  const login = (loginRequest) => {
    return axios.post(`${API_URL}/auth/login`, loginRequest);
  };

  const getAuthHeaders = () => {
    const token = localStorage.getItem('token');
    return {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    };
  };

  const getUserData = () => {
    return axios.get(`${API_URL}/user`, getAuthHeaders());
  };

  const getChats = () => {
    return axios.get(`${API_URL}/chat`, getAuthHeaders());
  };

  const getMessages = (chatId) => {
    return axios.get(`${API_URL}/message?chatId=${chatId}`, getAuthHeaders());
  };

  const sendMessage = (chatId, message) => {
    return axios.post(`${API_URL}/message`, { chatId, message }, getAuthHeaders());
  };

  const getChatDetails = (chatId) => {
    return axios.get(`${API_URL}/chat/${chatId}`, getAuthHeaders());
  };

  const getCurrentUser = () => {
    const token = localStorage.getItem('token');
    if (!token) return null;
    const payload = JSON.parse(atob(token.split('.')[1]));
    return { userName: payload.sub };
  };

  export default {
    register,
    login,
    getUserData,
    getChats,
    getMessages,
    sendMessage,
    getCurrentUser,
    getChatDetails,
  };