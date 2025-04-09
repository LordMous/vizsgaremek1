import axios from 'axios';

const API_URL = 'http://localhost:8080';

const register = (registerRequest) => {
  return axios.post(`${API_URL}/auth/register`, registerRequest);
};

const login = (loginRequest) => {
  return axios.post(`${API_URL}/auth/login`, loginRequest);
};

const getAuthHeaders = () => {
  const token = sessionStorage.getItem('token'); // localStorage helyett sessionStorage
  return {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  };
};

const getContactsByStatus = (status) => {
  return axios.get(`${API_URL}/contacts/${getCurrentUser().userId}?status=${status}`, getAuthHeaders());
};

const getUserData = (userId) => {
  return axios.get(`${API_URL}/user/${userId}`, getAuthHeaders());
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

const updateUser = (userData) => {
  return axios.put(`${API_URL}/user/${userData.id}`, userData, getAuthHeaders());
};

const getAllUsers = () => {
  return axios.get(`${API_URL}/user/list`, getAuthHeaders());
};

const updateContactStatus = (userId, contactUserId, status) => {
  return axios.put(
    `${API_URL}/contacts/${userId}?contactUserId=${contactUserId}&status=${status}`,
    null, // A body nem szükséges, mert az adatok query paraméterként kerülnek átadásra
    getAuthHeaders()
  );
};

const addContact = (userId, contactUserId) => {
  return axios.post(
    `${API_URL}/contacts/add?userId=${userId}&contactUserId=${contactUserId}`,
    null, // A body nem szükséges, mert az adatok query paraméterként kerülnek átadásra
    getAuthHeaders()
  );
};

const getCurrentUser = () => {
  const token = sessionStorage.getItem('token'); // localStorage helyett sessionStorage
  const userId = sessionStorage.getItem('userId');
  const role = sessionStorage.getItem('role');
  if (!token) return null;
  const payload = JSON.parse(atob(token.split('.')[1]));
  return {
    userName: payload.sub,
    userId: userId,
    role: role,
  };
};

const deleteContact = (userId, contactUserId) => {
  return axios.delete(`${API_URL}/contacts/${userId}?contactUserId=${contactUserId}`, getAuthHeaders());
};

const createChat = (userId, contactUserId) => {
  return axios.post(
    `${API_URL}/chat/start?userId=${userId}&contactUserId=${contactUserId}`,
    null, // A body nem szükséges, mert az adatok query paraméterként kerülnek átadásra
    getAuthHeaders()
  );
};

const deleteChat = (chatId) => {
  return axios.delete(`${API_URL}/chat/${chatId}`, getAuthHeaders());
};

const uploadProfilePicture = (userId, file) => {
  const formData = new FormData();
  formData.append('file', file);

  return axios.post(`${API_URL}/user/upload-picture/${userId}`, formData, {
    headers: {
      ...getAuthHeaders().headers,
      'Content-Type': 'multipart/form-data',
    },
  });
};

const getUserProfilePicture = (userId) => {
  return axios.get(`${API_URL}/user/${userId}/picture`, getAuthHeaders());
}

const deleteUser = (userId) => {
  return axios.delete(`${API_URL}/user/${userId}`, getAuthHeaders());
}


export default {
  register,
  login,
  getUserData,
  getChats,
  getMessages,
  sendMessage,
  getCurrentUser,
  getChatDetails,
  updateUser,
  getAllUsers,
  getContactsByStatus,
  updateContactStatus,
  addContact,
  deleteContact,
  createChat,
  deleteChat,
  uploadProfilePicture,
  getUserProfilePicture,
  deleteUser,
};
