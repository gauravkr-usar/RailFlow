import React from 'react';
import { Train, ShieldCheck, HeartHandshake, PhoneCall, ExternalLink } from 'lucide-react';
import { Link } from 'react-router-dom';

export default function Footer() {
  return (
    <footer className="bg-slate-900 text-slate-400 text-sm mt-auto border-t border-slate-800">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-8 mb-8">
          {/* Brand Info */}
          <div className="space-y-4">
            <div className="flex items-center space-x-2">
              <div className="w-8 h-8 rounded-lg bg-rail-orange flex items-center justify-center text-white">
                <Train className="w-5 h-5" />
              </div>
              <span className="font-bold text-lg text-white">RailFlow</span>
            </div>
            <p className="text-xs text-slate-400 leading-relaxed">
              High-concurrency Smart Railway Reservation System inspired by IRCTC.
              Engineered with modern distributed architecture, atomic inventory locking, and instant PNR tracking.
            </p>
            <div className="flex items-center space-x-2 text-xs text-emerald-400">
              <ShieldCheck className="w-4 h-4" />
              <span>PCI-DSS Compliant & AES-256 Encrypted</span>
            </div>
          </div>

          {/* Quick Links */}
          <div>
            <h4 className="text-white font-semibold mb-4 text-xs tracking-wider uppercase">Book & Inquire</h4>
            <ul className="space-y-2 text-xs">
              <li>
                <Link to="/" className="hover:text-rail-orange transition-colors">Search Trains</Link>
              </li>
              <li>
                <Link to="/pnr-status" className="hover:text-rail-orange transition-colors">PNR Status Enquiry</Link>
              </li>
              <li>
                <Link to="/my-bookings" className="hover:text-rail-orange transition-colors">My Ticket Bookings</Link>
              </li>
              <li>
                <span className="text-slate-500 cursor-not-allowed">Tatkal / Premium Tatkal</span>
              </li>
              <li>
                <span className="text-slate-500 cursor-not-allowed">Train Running Status (NTES)</span>
              </li>
            </ul>
          </div>

          {/* Guidelines */}
          <div>
            <h4 className="text-white font-semibold mb-4 text-xs tracking-wider uppercase">Rules & Policies</h4>
            <ul className="space-y-2 text-xs">
              <li>
                <span className="hover:text-slate-300 cursor-pointer">Cancellation & Refund Rules</span>
              </li>
              <li>
                <span className="hover:text-slate-300 cursor-pointer">RAC / Waiting List (WL) Algorithm</span>
              </li>
              <li>
                <span className="hover:text-slate-300 cursor-pointer">Tatkal Booking Timings</span>
              </li>
              <li>
                <span className="hover:text-slate-300 cursor-pointer">Luggage Allowance Guidelines</span>
              </li>
              <li>
                <span className="hover:text-slate-300 cursor-pointer">Terms of Service & Privacy</span>
              </li>
            </ul>
          </div>

          {/* Indian Railways Notice */}
          <div className="space-y-3">
            <h4 className="text-white font-semibold mb-4 text-xs tracking-wider uppercase">Helpline & Support</h4>
            <div className="bg-slate-800/80 p-3 rounded-lg border border-slate-700/60">
              <div className="flex items-center space-x-2 text-rail-orange font-bold text-sm">
                <PhoneCall className="w-4 h-4" />
                <span>Railway Helpline: 139</span>
              </div>
              <p className="text-[11px] text-slate-400 mt-1">
                For security, medical emergency, train inquiry & catering complaints.
              </p>
            </div>
            <div className="text-xs text-slate-500">
              Project for demonstration of scalable cloud architecture & real-time seat inventory reservation.
            </div>
          </div>
        </div>

        <div className="border-t border-slate-800 pt-6 flex flex-col sm:flex-row justify-between items-center text-xs text-slate-500">
          <p>© {new Date().getFullYear()} RailFlow Systems. All rights reserved.</p>
          <p className="mt-2 sm:mt-0 flex items-center space-x-1">
            <span>Built with precision for Indian Railways travelers</span>
          </p>
        </div>
      </div>
    </footer>
  );
}
