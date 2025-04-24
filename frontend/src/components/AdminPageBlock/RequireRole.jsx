import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';

const RequireRole = ({ allowedRoles }) => {
  const role = sessionStorage.getItem('role');
    console.log(role)
  if (!role) {
    return <Navigate to="/login" replace />;
  }

  if (!allowedRoles.includes(role)) {
    return <Navigate to="/unauthorized" replace />;
  }

  return <Outlet />;
};

export default RequireRole;