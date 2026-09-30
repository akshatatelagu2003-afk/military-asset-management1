import React from 'react';
import { User, ShieldCheck } from 'lucide-react';
import './Header.css';

const Header = ({ user }) => {
  return (
    <header className="top-header">
      <div className="header-left">
        <h1 className="header-title">Military Asset Management System</h1>
      </div>
      
      <div className="header-right">
        <div className="user-info">
          <div className="user-details">
            <span className="user-email">{user?.email}</span>
            <span className="user-role">
              <ShieldCheck size={14} className="role-icon" />
              {user?.role?.replace('_', ' ')}
            </span>
          </div>
          <div className="user-avatar">
            <User size={20} />
          </div>
        </div>
      </div>
    </header>
  );
};

export default Header;
