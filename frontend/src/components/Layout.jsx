import React, { useContext } from 'react';
import { Outlet } from 'react-router-dom';
import Sidebar from './Sidebar';
import Header from './Header';
import { AuthContext } from '../context/AuthContext';
import './Layout.css';

const Layout = () => {
  const { user, logout } = useContext(AuthContext);

  return (
    <div className="layout-container">
      <Sidebar logout={logout} role={user?.role} />
      <div className="main-content">
        <Header user={user} />
        <main className="page-content">
          <Outlet />
        </main>
      </div>
    </div>
  );
};

export default Layout;
