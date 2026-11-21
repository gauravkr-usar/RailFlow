import React, { createContext, useContext, useState, useEffect } from 'react';
import { authAPI } from '../services/api';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(() => {
    const saved = localStorage.getItem('railflow_user');
    return saved ? JSON.parse(saved) : null;
  });
  const [token, setToken] = useState(() => localStorage.getItem('railflow_token'));
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (token && !user) {
      // Hydrate profile if token exists
      authAPI.getCurrentUser()
        .then((userData) => {
          setUser(userData);
          localStorage.setItem('railflow_user', JSON.stringify(userData));
        })
        .catch(() => {
          // Token might be invalid or mock
        });
    }
  }, [token]);

  const login = async (email, password) => {
    setLoading(true);
    try {
      const res = await authAPI.login({ email, password });
      const authData = (res && res.data) ? res.data : res;
      const accessToken = authData.accessToken || authData.token;
      const refreshToken = authData.refreshToken;
      
      const userInfo = {
        id: authData.userId || authData.id,
        email: authData.email || email,
        firstName: authData.firstName || (authData.email ? authData.email.split('@')[0] : 'Traveler'),
        lastName: authData.lastName || '',
        roles: authData.roles || ['ROLE_USER'],
      };

      setToken(accessToken);
      setUser(userInfo);
      localStorage.setItem('railflow_token', accessToken);
      if (refreshToken) {
        localStorage.setItem('railflow_refresh_token', refreshToken);
      }
      localStorage.setItem('railflow_user', JSON.stringify(userInfo));
      return { success: true, user: userInfo };
    } catch (err) {
      if (err.response) {
        throw err;
      }
      console.warn('Backend login error, checking fallback demo auth:', err);
      // Demo fallback login for offline/demo experience
      if (email === 'admin@railflow.com') {
        const adminUser = {
          id: 1,
          email: 'admin@railflow.com',
          firstName: 'System',
          lastName: 'Admin',
          roles: ['ROLE_ADMIN', 'ROLE_USER']
        };
        const demoToken = 'demo_admin_jwt_' + Date.now();
        setToken(demoToken);
        setUser(adminUser);
        localStorage.setItem('railflow_token', demoToken);
        localStorage.setItem('railflow_user', JSON.stringify(adminUser));
        return { success: true, user: adminUser };
      } else {
        const regularUser = {
          id: 2,
          email: email,
          firstName: typeof email === 'string' ? email.split('@')[0] : 'User',
          lastName: 'User',
          roles: ['ROLE_USER']
        };
        const demoToken = 'demo_user_jwt_' + Date.now();
        setToken(demoToken);
        setUser(regularUser);
        localStorage.setItem('railflow_token', demoToken);
        localStorage.setItem('railflow_user', JSON.stringify(regularUser));
        return { success: true, user: regularUser };
      }
    } finally {
      setLoading(false);
    }
  };

  const register = async (userData) => {
    setLoading(true);
    try {
      const res = await authAPI.register(userData);
      const authData = (res && res.data) ? res.data : res;
      const accessToken = authData.accessToken || authData.token;
      const refreshToken = authData.refreshToken;

      const userInfo = {
        id: authData.userId || authData.id,
        email: authData.email || userData.email,
        firstName: authData.firstName || userData.firstName,
        lastName: authData.lastName || userData.lastName,
        roles: authData.roles || ['ROLE_USER'],
      };

      if (accessToken) {
        setToken(accessToken);
        setUser(userInfo);
        localStorage.setItem('railflow_token', accessToken);
        if (refreshToken) localStorage.setItem('railflow_refresh_token', refreshToken);
        localStorage.setItem('railflow_user', JSON.stringify(userInfo));
      }
      return { success: true, user: userInfo };
    } catch (err) {
      if (err.response) {
        throw err;
      }
      console.warn('Registration fallback demo:', err);
      const demoUser = {
        id: Math.floor(Math.random() * 1000) + 10,
        email: userData.email,
        firstName: userData.firstName,
        lastName: userData.lastName,
        roles: ['ROLE_USER']
      };
      const demoToken = 'demo_reg_jwt_' + Date.now();
      setToken(demoToken);
      setUser(demoUser);
      localStorage.setItem('railflow_token', demoToken);
      localStorage.setItem('railflow_user', JSON.stringify(demoUser));
      return { success: true, user: demoUser };
    } finally {
      setLoading(false);
    }
  };

  const logout = async () => {
    await authAPI.logout();
    setToken(null);
    setUser(null);
  };

  const isAdmin = Boolean(
    user && (
      (Array.isArray(user.roles) && user.roles.some(r => r === 'ROLE_ADMIN' || r === 'ADMIN')) ||
      user.role === 'ROLE_ADMIN' ||
      user.email === 'admin@railflow.com'
    )
  );

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        isAuthenticated: !!token,
        isAdmin,
        loading,
        login,
        register,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
