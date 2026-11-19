import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import Navbar from './components/Navbar';
import Footer from './components/Footer';

// Pages
import HomePage from './pages/HomePage';
import SearchResultsPage from './pages/SearchResultsPage';
import PassengerDetailsPage from './pages/PassengerDetailsPage';
import PaymentCheckoutPage from './pages/PaymentCheckoutPage';
import BookingConfirmationPage from './pages/BookingConfirmationPage';
import PnrEnquiryPage from './pages/PnrEnquiryPage';
import MyBookingsPage from './pages/MyBookingsPage';
import AuthPage from './pages/AuthPage';
import AdminDashboardPage from './pages/AdminDashboardPage';

export default function App() {
  return (
    <AuthProvider>
      <Router>
        <div className="min-h-screen flex flex-col bg-slate-50 text-slate-900 font-sans antialiased">
          <Navbar />
          <main className="flex-grow">
            <Routes>
              <Route path="/" element={<HomePage />} />
              <Route path="/search" element={<SearchResultsPage />} />
              <Route path="/booking/:trainId" element={<PassengerDetailsPage />} />
              <Route path="/payment/:bookingId" element={<PaymentCheckoutPage />} />
              <Route path="/booking/confirmation/:pnr" element={<BookingConfirmationPage />} />
              <Route path="/pnr-status" element={<PnrEnquiryPage />} />
              <Route path="/my-bookings" element={<MyBookingsPage />} />
              <Route path="/login" element={<AuthPage />} />
              <Route path="/register" element={<AuthPage />} />
              <Route path="/admin" element={<AdminDashboardPage />} />
            </Routes>
          </main>
          <Footer />
        </div>
      </Router>
    </AuthProvider>
  );
}
