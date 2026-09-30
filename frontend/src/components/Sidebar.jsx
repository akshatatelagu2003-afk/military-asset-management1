import React from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { LayoutDashboard, ShoppingCart, ArrowRightLeft, ClipboardList, TrendingDown, LogOut, Shield } from 'lucide-react';
import './Sidebar.css';

const Sidebar = ({ logout, role }) => {
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <aside className="sidebar">
      <div className="sidebar-header">
        <Shield className="sidebar-logo" size={32} />
        <h2 className="sidebar-title">MAMS</h2>
      </div>

      <nav className="sidebar-nav">
        <div className="nav-section">
          <p className="nav-section-title">Main Menu</p>
          <ul className="nav-list">
            <li>
              <NavLink to="/dashboard" className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}>
                <LayoutDashboard size={20} />
                <span>Dashboard</span>
              </NavLink>
            </li>
            <li>
              <NavLink to="/purchases" className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}>
                <ShoppingCart size={20} />
                <span>Purchases</span>
              </NavLink>
            </li>
            <li>
              <NavLink to="/transfers" className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}>
                <ArrowRightLeft size={20} />
                <span>Transfers</span>
              </NavLink>
            </li>
            <li>
              <NavLink to="/assignments" className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}>
                <ClipboardList size={20} />
                <span>Assignments</span>
              </NavLink>
            </li>
            <li>
              <NavLink to="/expenditures" className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}>
                <TrendingDown size={20} />
                <span>Expenditures</span>
              </NavLink>
            </li>
          </ul>
        </div>
      </nav>

      <div className="sidebar-footer">
        <button onClick={handleLogout} className="logout-button">
          <LogOut size={20} />
          <span>Logout</span>
        </button>
      </div>
    </aside>
  );
};

export default Sidebar;
