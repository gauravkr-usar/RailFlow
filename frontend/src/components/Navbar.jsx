import React, { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { Train, Search, Ticket, Calendar, Shield, LogIn, LogOut, User, Menu, X, Radio } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

export default function Navbar() {
  const { user, isAuthenticated, isAdmin, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  const handleLogout = async () => {
    await logout();
    navigate('/');
  };

  const isActive = (path) => location.pathname === path;

  return (
    <header className="sticky top-0 z-40 bg-rail-navy text-white shadow-lg border-b border-rail-navy-light">
      {/* Top micro bar with IRCTC branding flavor */}
      <div className="bg-slate-900/60 px-4 py-1 text-xs text-slate-300 flex justify-between items-center border-b border-slate-700/50">
        <div className="flex items-center space-x-4">
          <span className="flex items-center text-rail-orange font-medium">
            <span className="w-2 h-2 rounded-full bg-emerald-400 inline-block mr-1.5 animate-ping"></span>
            RailFlow IRCTC Express Network
          </span>
          <span className="hidden sm:inline text-slate-400">| 24x7 Customer Helpline: 139</span>
        </div>
        <div className="flex items-center space-x-3 text-xs">
          <span className="text-slate-300">Fast Tatkal & Premium Tatkal Booking Enabled</span>
        </div>
      </div>

      {/* Main navigation bar */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between h-16 items-center">
          {/* Logo & Brand */}
          <Link to="/" className="flex items-center space-x-3 group">
            <div className="w-10 h-10 rounded-xl bg-rail-orange flex items-center justify-center text-white shadow-md shadow-orange-500/30 group-hover:scale-105 transition-transform">
              <Train className="w-6 h-6 stroke-[2.2]" />
            </div>
            <div className="flex flex-col">
              <span className="font-extrabold text-xl tracking-tight text-white flex items-center">
                Rail<span className="text-rail-orange">Flow</span>
              </span>
              <span className="text-[10px] text-slate-300 -mt-1 tracking-wider uppercase font-semibold">
                Smart Rail Reservation
              </span>
            </div>
          </Link>

          {/* Desktop Navigation Links */}
          <nav className="hidden md:flex items-center space-x-1 lg:space-x-2">
            <Link
              to="/"
              className={`px-3 py-2 rounded-lg text-sm font-medium transition-colors flex items-center space-x-1.5 ${
                isActive('/') ? 'bg-white/10 text-rail-orange' : 'text-slate-200 hover:bg-white/5 hover:text-white'
              }`}
            >
              <Search className="w-4 h-4" />
              <span>Book Trains</span>
            </Link>

            <Link
              to="/pnr-status"
              className={`px-3 py-2 rounded-lg text-sm font-medium transition-colors flex items-center space-x-1.5 ${
                isActive('/pnr-status') ? 'bg-white/10 text-rail-orange' : 'text-slate-200 hover:bg-white/5 hover:text-white'
              }`}
            >
              <Ticket className="w-4 h-4" />
              <span>PNR Enquiry</span>
            </Link>

            <Link
              to="/live-status"
              className={`px-3 py-2 rounded-lg text-sm font-medium transition-colors flex items-center space-x-1.5 ${
                isActive('/live-status') ? 'bg-white/10 text-rail-orange' : 'text-slate-200 hover:bg-white/5 hover:text-white'
              }`}
            >
              <Radio className="w-4 h-4 text-emerald-400" />
              <span>Live Status</span>
            </Link>

            {isAuthenticated && (
              <Link
                to="/my-bookings"
                className={`px-3 py-2 rounded-lg text-sm font-medium transition-colors flex items-center space-x-1.5 ${
                  isActive('/my-bookings') ? 'bg-white/10 text-rail-orange' : 'text-slate-200 hover:bg-white/5 hover:text-white'
                }`}
              >
                <Calendar className="w-4 h-4" />
                <span>My Bookings</span>
              </Link>
            )}

            {isAdmin && (
              <Link
                to="/admin"
                className={`px-3 py-2 rounded-lg text-sm font-medium transition-colors flex items-center space-x-1.5 ${
                  isActive('/admin') ? 'bg-rail-orange text-white' : 'text-amber-400 bg-amber-400/10 hover:bg-amber-400/20'
                }`}
              >
                <Shield className="w-4 h-4" />
                <span>Admin Portal</span>
              </Link>
            )}
          </nav>

          {/* User profile / Auth buttons */}
          <div className="hidden md:flex items-center space-x-3">
            {isAuthenticated ? (
              <div className="flex items-center space-x-3">
                <div className="flex items-center space-x-2 bg-slate-800/80 px-3 py-1.5 rounded-full border border-slate-700">
                  <div className="w-7 h-7 rounded-full bg-rail-orange/20 text-rail-orange font-bold text-xs flex items-center justify-center">
                    {user?.firstName?.charAt(0) || 'U'}
                  </div>
                  <span className="text-xs font-medium text-slate-200 max-w-[120px] truncate">
                    {user?.firstName || user?.email}
                  </span>
                  {isAdmin && (
                    <span className="text-[10px] bg-red-500/20 text-red-300 px-1.5 py-0.5 rounded font-bold uppercase tracking-wider">
                      Admin
                    </span>
                  )}
                </div>

                <button
                  onClick={handleLogout}
                  title="Logout"
                  className="p-2 rounded-lg text-slate-300 hover:text-white hover:bg-white/10 transition-colors"
                >
                  <LogOut className="w-4 h-4" />
                </button>
              </div>
            ) : (
              <div className="flex items-center space-x-2">
                <Link
                  to="/login"
                  className="px-3.5 py-1.5 rounded-lg text-sm font-medium text-slate-200 hover:text-white hover:bg-white/10 transition-colors flex items-center space-x-1"
                >
                  <LogIn className="w-4 h-4" />
                  <span>Login</span>
                </Link>
                <Link
                  to="/register"
                  className="px-4 py-1.5 rounded-lg text-sm font-semibold bg-rail-orange text-white hover:bg-rail-orange-dark shadow transition-all transform active:scale-95"
                >
                  Sign Up
                </Link>
              </div>
            )}
          </div>

          {/* Mobile menu trigger */}
          <div className="md:hidden flex items-center">
            <button
              onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
              className="p-2 rounded-lg text-slate-300 hover:text-white hover:bg-white/10"
            >
              {mobileMenuOpen ? <X className="w-6 h-6" /> : <Menu className="w-6 h-6" />}
            </button>
          </div>
        </div>
      </div>

      {/* Mobile Drawer */}
      {mobileMenuOpen && (
        <div className="md:hidden bg-rail-navy-light px-4 pt-2 pb-4 space-y-2 border-t border-slate-700">
          <Link
            to="/"
            onClick={() => setMobileMenuOpen(false)}
            className="flex items-center space-x-2 py-2 text-slate-200 hover:text-white"
          >
            <Search className="w-4 h-4 text-rail-orange" />
            <span>Search & Book Trains</span>
          </Link>
          <Link
            to="/pnr-status"
            onClick={() => setMobileMenuOpen(false)}
            className="flex items-center space-x-2 py-2 text-slate-200 hover:text-white"
          >
            <Ticket className="w-4 h-4 text-rail-orange" />
            <span>PNR Enquiry</span>
          </Link>
          <Link
            to="/live-status"
            onClick={() => setMobileMenuOpen(false)}
            className="flex items-center space-x-2 py-2 text-slate-200 hover:text-white"
          >
            <Radio className="w-4 h-4 text-emerald-400" />
            <span>Live Train Status</span>
          </Link>
          {isAuthenticated && (
            <Link
              to="/my-bookings"
              onClick={() => setMobileMenuOpen(false)}
              className="flex items-center space-x-2 py-2 text-slate-200 hover:text-white"
            >
              <Calendar className="w-4 h-4 text-rail-orange" />
              <span>My Bookings</span>
            </Link>
          )}
          {isAdmin && (
            <Link
              to="/admin"
              onClick={() => setMobileMenuOpen(false)}
              className="flex items-center space-x-2 py-2 text-amber-400 font-semibold"
            >
              <Shield className="w-4 h-4" />
              <span>Admin Dashboard</span>
            </Link>
          )}
          <div className="pt-2 border-t border-slate-700/60 flex justify-between items-center">
            {isAuthenticated ? (
              <div className="flex items-center justify-between w-full">
                <span className="text-sm text-slate-300">
                  Signed in as <strong>{user?.firstName}</strong>
                </span>
                <button
                  onClick={() => {
                    handleLogout();
                    setMobileMenuOpen(false);
                  }}
                  className="text-xs bg-red-600/80 px-3 py-1.5 rounded text-white"
                >
                  Logout
                </button>
              </div>
            ) : (
              <div className="flex space-x-2 w-full pt-1">
                <Link
                  to="/login"
                  onClick={() => setMobileMenuOpen(false)}
                  className="flex-1 text-center py-2 text-sm font-medium bg-slate-800 rounded text-slate-200"
                >
                  Login
                </Link>
                <Link
                  to="/register"
                  onClick={() => setMobileMenuOpen(false)}
                  className="flex-1 text-center py-2 text-sm font-semibold bg-rail-orange text-white rounded"
                >
                  Register
                </Link>
              </div>
            )}
          </div>
        </div>
      )}
    </header>
  );
}
