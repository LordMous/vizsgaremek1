import React from 'react';
import { BrowserRouter as Router, Route, Routes} from 'react-router-dom';
import Login from './components/Login/Login';
import Register from './components/Register/Register';
import Dashboard from './components/Dashboard/Dashboard';
import Profile from './components/Profile/Profile';
import Admin from './components/Admin/Admin';
import RequireRole from './components/AdminPageBlock/RequireRole';
import UnauthorizedPage from './components/AdminPageBlock/UnauthorizedPage';

function App() {

  return (
    <Router>
      <Routes>
        <Route path="/" element={<Login />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/dashboard" element={<Dashboard />} />
        <Route path="/profile" element={<Profile />} />
        <Route path="/unauthorized" element={<UnauthorizedPage />} />
        <Route element={<RequireRole allowedRoles={['ADMIN']}/>}>
          <Route path="/admin" element={<Admin />} />
        </Route>
      </Routes>
    </Router>
  );
}

export default App;