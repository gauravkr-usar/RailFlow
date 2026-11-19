import React, { useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { Train, Lock, Mail, User, Phone, ArrowRight, ShieldCheck } from 'lucide-react';
import { authAPI } from '../services/api';
import { useAuth } from '../context/AuthContext';

export default function AuthPage() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { login: setAuthUser } = useAuth();

  const [isLogin, setIsLogin] = useState(searchParams.get('mode') !== 'register');
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    phoneNumber: ''
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      let res;
      if (isLogin) {
        res = await authAPI.login({ email: formData.email, password: formData.password });
      } else {
        res = await authAPI.register(formData);
      }

      const data = res.data || res;
      if (data.accessToken) {
        localStorage.setItem('railflow_token', data.accessToken);
        setAuthUser(data);
        navigate('/');
      } else {
        setIsLogin(true);
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Authentication failed. Please check your credentials.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-100 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
      <div className="sm:mx-auto sm:w-full sm:max-w-md">
        <div className="flex justify-center items-center space-x-2">
          <div className="w-10 h-10 rounded-xl bg-orange-600 flex items-center justify-center text-white font-black text-xl shadow-lg">
            RF
          </div>
          <span className="text-2xl font-black text-slate-900 tracking-tight">RailFlow</span>
        </div>
        <h2 className="mt-4 text-center text-2xl font-bold text-slate-900">
          {isLogin ? 'Sign in to your account' : 'Create a new RailFlow account'}
        </h2>
        <p className="mt-2 text-center text-sm text-slate-500">
          {isLogin ? "Don't have an account? " : "Already have an account? "}
          <button
            onClick={() => { setIsLogin(!isLogin); setError(null); }}
            className="font-semibold text-orange-600 hover:text-orange-500"
          >
            {isLogin ? 'Register now' : 'Sign in'}
          </button>
        </p>
      </div>

      <div className="mt-8 sm:mx-auto sm:w-full sm:max-w-md">
        <div className="bg-white py-8 px-6 shadow-sm border border-slate-200 rounded-2xl sm:px-10">
          {error && (
            <div className="mb-4 bg-red-50 border border-red-200 text-red-700 p-3 rounded-lg text-xs">
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            {!isLogin && (
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">First Name</label>
                  <input
                    type="text"
                    required
                    value={formData.firstName}
                    onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
                    className="w-full text-sm rounded-lg border-slate-300 border px-3 py-2"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">Last Name</label>
                  <input
                    type="text"
                    required
                    value={formData.lastName}
                    onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
                    className="w-full text-sm rounded-lg border-slate-300 border px-3 py-2"
                  />
                </div>
              </div>
            )}

            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">Email Address</label>
              <div className="relative">
                <input
                  type="email"
                  required
                  value={formData.email}
                  onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                  placeholder="user@railflow.com"
                  className="w-full text-sm rounded-lg border-slate-300 border pl-3 pr-3 py-2"
                />
              </div>
            </div>

            {!isLogin && (
              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">Phone Number</label>
                <input
                  type="tel"
                  required
                  value={formData.phoneNumber}
                  onChange={(e) => setFormData({ ...formData, phoneNumber: e.target.value })}
                  placeholder="+919876543210"
                  className="w-full text-sm rounded-lg border-slate-300 border px-3 py-2"
                />
              </div>
            )}

            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">Password</label>
              <input
                type="password"
                required
                minLength={8}
                value={formData.password}
                onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                placeholder="••••••••"
                className="w-full text-sm rounded-lg border-slate-300 border px-3 py-2"
              />
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full mt-4 bg-orange-600 hover:bg-orange-700 text-white font-bold py-2.5 px-4 rounded-xl shadow transition"
            >
              {loading ? 'Authenticating...' : isLogin ? 'Sign In' : 'Create Account'}
            </button>
          </form>

          {isLogin && (
            <div className="mt-6 border-t border-slate-100 pt-4 text-center">
              <span className="text-xs text-slate-400 block mb-1">Quick Demo Credentials:</span>
              <div className="text-xs font-mono bg-slate-50 p-2 rounded text-slate-600 space-y-1">
                <div>Admin: <span className="font-bold">admin@railflow.com</span> / Admin@1234</div>
                <div>User: <span className="font-bold">user@railflow.com</span> / User@1234</div>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
