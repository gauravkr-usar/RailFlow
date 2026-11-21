import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Search, Train, Calendar, CheckCircle2, AlertCircle, Download, ArrowRight, Radio, QrCode, FileText, Check } from 'lucide-react';
import { bookingAPI, ticketAPI } from '../services/api';

export default function PnrEnquiryPage() {
  const navigate = useNavigate();
  const [pnrInput, setPnrInput] = useState('');
  const [booking, setBooking] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleSearch = async (e) => {
    e.preventDefault();
    if (!pnrInput.trim()) return;

    setLoading(true);
    setError(null);
    setBooking(null);

    try {
      const res = await bookingAPI.getBookingByPnr(pnrInput.trim());
      setBooking(res.data || res);
    } catch (err) {
      setError('PNR not found or invalid. Please check the 10-digit PNR number.');
    } finally {
      setLoading(false);
    }
  };

  const handleDownloadPdf = (bookingData) => {
    ticketAPI.downloadTicket(bookingData);
  };

  return (
    <div className="min-h-screen bg-slate-100 py-10 px-4 sm:px-6 lg:px-8">
      <div className="max-w-4xl mx-auto space-y-6">
        <div className="text-center">
          <div className="inline-flex items-center space-x-2 bg-rail-navy text-white px-3.5 py-1 rounded-full text-xs font-semibold mb-2 shadow-sm">
            <CheckCircle2 className="w-3.5 h-3.5 text-emerald-400" />
            <span>Official IRCTC Express PNR Verification Portal</span>
          </div>
          <h1 className="text-3xl font-extrabold text-slate-900 tracking-tight">PNR Enquiry & Charting Status</h1>
          <p className="text-sm text-slate-500 mt-1">Check real-time confirmation status, chart preparation, coach position, and berth details</p>
        </div>

        {/* Input Card */}
        <div className="bg-white rounded-2xl shadow-sm border border-slate-200 p-6">
          <form onSubmit={handleSearch} className="flex flex-col sm:flex-row gap-3">
            <div className="relative flex-1">
              <input
                type="text"
                required
                maxLength={12}
                value={pnrInput}
                onChange={(e) => setPnrInput(e.target.value)}
                placeholder="Enter 10-digit PNR number (e.g. RF9284192841)"
                className="w-full pl-4 pr-10 py-3 text-base rounded-xl border border-slate-300 focus:ring-2 focus:ring-orange-500 font-mono uppercase tracking-wider"
              />
            </div>
            <button
              type="submit"
              disabled={loading}
              className="bg-orange-600 hover:bg-orange-700 text-white font-bold px-6 py-3 rounded-xl transition flex items-center justify-center space-x-2 shadow-md shadow-orange-500/20"
            >
              <Search className="w-5 h-5" />
              <span>{loading ? 'Searching...' : 'Get Live Status'}</span>
            </button>
          </form>
        </div>

        {error && (
          <div className="bg-red-50 border border-red-200 text-red-700 p-4 rounded-xl text-sm flex items-center">
            <AlertCircle className="w-5 h-5 mr-2 shrink-0" />
            <span>{error}</span>
          </div>
        )}

        {/* Result Card */}
        {booking && (
          <div className="bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden space-y-6">
            {/* Top Bar */}
            <div className="bg-gradient-to-r from-slate-900 to-rail-navy text-white p-6 flex flex-wrap justify-between items-center gap-4">
              <div>
                <span className="text-xs text-slate-400 block font-medium uppercase tracking-wider">PNR REGISTRATION</span>
                <span className="text-2xl font-mono font-bold tracking-widest text-orange-400">{booking.pnr}</span>
              </div>
              <div className="flex items-center space-x-2">
                <span className={`text-xs font-bold px-3 py-1 rounded-full ${booking.status === 'CONFIRMED' || booking.bookingStatus === 'CONFIRMED' ? 'bg-emerald-500 text-white' : 'bg-amber-500 text-white'}`}>
                  ✓ {booking.status || booking.bookingStatus || 'CONFIRMED'}
                </span>
                <span className="bg-white/10 text-slate-200 text-xs px-2.5 py-1 rounded-full font-mono">
                  Quota: GENERAL
                </span>
              </div>
            </div>

            <div className="p-6 pt-0 space-y-6">
              {/* Charting Lifecycle Progress Bar */}
              <div className="bg-slate-50 border border-slate-200 rounded-xl p-5">
                <h3 className="text-xs font-bold text-slate-500 uppercase tracking-wider mb-4 flex items-center justify-between">
                  <span>Charting & Journey Lifecycle Tracker</span>
                  <span className="text-emerald-700 bg-emerald-100 text-[11px] px-2 py-0.5 rounded font-bold">
                    Chart Prepared & Berths Locked
                  </span>
                </h3>

                <div className="grid grid-cols-1 sm:grid-cols-4 gap-3 text-xs">
                  <div className="bg-white p-3 rounded-lg border-2 border-emerald-500 shadow-xs flex items-center space-x-2.5">
                    <div className="w-6 h-6 rounded-full bg-emerald-500 text-white flex items-center justify-center font-bold text-[10px]">✓</div>
                    <div>
                      <span className="font-bold text-slate-900 block">Booking Confirmed</span>
                      <span className="text-[10px] text-slate-500">Seat Locked</span>
                    </div>
                  </div>

                  <div className="bg-white p-3 rounded-lg border-2 border-emerald-500 shadow-xs flex items-center space-x-2.5">
                    <div className="w-6 h-6 rounded-full bg-emerald-500 text-white flex items-center justify-center font-bold text-[10px]">✓</div>
                    <div>
                      <span className="font-bold text-slate-900 block">Chart Prepared</span>
                      <span className="text-[10px] text-slate-500">Final list at station</span>
                    </div>
                  </div>

                  <div className="bg-white p-3 rounded-lg border border-slate-200 flex items-center space-x-2.5">
                    <div className="w-6 h-6 rounded-full bg-rail-navy text-white flex items-center justify-center font-bold text-[10px]">3</div>
                    <div>
                      <span className="font-bold text-slate-900 block">Coach Position</span>
                      <span className="text-[10px] text-slate-500">4th from engine</span>
                    </div>
                  </div>

                  <div className="bg-white p-3 rounded-lg border border-slate-200 flex items-center space-x-2.5">
                    <div className="w-6 h-6 rounded-full bg-slate-300 text-slate-600 flex items-center justify-center font-bold text-[10px]">4</div>
                    <div>
                      <span className="font-bold text-slate-900 block">Journey Status</span>
                      <span className="text-[10px] text-slate-500">Scheduled on time</span>
                    </div>
                  </div>
                </div>
              </div>

              {/* Train & Journey details */}
              <div className="border border-slate-200 bg-white rounded-xl p-5">
                <div className="flex flex-wrap items-center justify-between gap-2 mb-4">
                  <div className="flex items-center space-x-2">
                    <span className="font-mono bg-slate-900 text-white text-xs px-2 py-0.5 rounded font-bold">
                      {booking.trainNumber || '22436'}
                    </span>
                    <span className="font-bold text-slate-900 text-lg">
                      {booking.trainName || 'Vande Bharat Express'}
                    </span>
                  </div>

                  <button
                    type="button"
                    onClick={() => navigate(`/live-status?train=${booking.trainNumber || '22436'}`)}
                    className="inline-flex items-center text-xs font-bold text-rail-navy bg-orange-50 hover:bg-orange-100 border border-orange-200 px-3 py-1.5 rounded-lg transition"
                  >
                    <Radio className="w-3.5 h-3.5 text-rail-orange mr-1.5 animate-pulse" />
                    <span>Track Live Train Status</span>
                  </button>
                </div>

                <div className="grid grid-cols-3 gap-2 text-center text-sm py-2 bg-slate-50 rounded-xl border border-slate-100">
                  <div>
                    <span className="font-bold text-slate-800 block text-base">{booking.departureTime?.substring(0, 5) || '06:00'}</span>
                    <span className="text-xs text-slate-600 font-semibold">{booking.sourceStationName || booking.sourceStationCode || 'NDLS'}</span>
                  </div>
                  <div className="text-xs text-slate-400 flex flex-col justify-center items-center">
                    <span className="font-medium text-slate-700">{booking.journeyDate}</span>
                    <ArrowRight className="w-4 h-4 text-orange-500 my-0.5" />
                    <span className="font-bold text-rail-navy">Class {booking.coachType || '3A'}</span>
                  </div>
                  <div>
                    <span className="font-bold text-slate-800 block text-base">{booking.arrivalTime?.substring(0, 5) || '14:00'}</span>
                    <span className="text-xs text-slate-600 font-semibold">{booking.destinationStationName || booking.destinationStationCode || 'BSB'}</span>
                  </div>
                </div>
              </div>

              {/* Passengers Table */}
              <div>
                <h3 className="text-sm font-bold text-slate-800 mb-3 flex items-center justify-between">
                  <span>Passenger & Seat Allotment</span>
                  <span className="text-xs text-slate-400 font-normal">
                    {booking.passengers?.length || 1} Passenger(s)
                  </span>
                </h3>
                <div className="space-y-2">
                  {booking.passengers?.map((p, idx) => (
                    <div key={idx} className="flex items-center justify-between p-4 rounded-xl border border-slate-200 bg-white hover:border-slate-300 transition">
                      <div>
                        <span className="text-sm font-bold text-slate-900 block">{p.passengerName || p.name}</span>
                        <span className="text-xs text-slate-500">{p.age} yrs • {p.gender}</span>
                      </div>
                      <div className="text-right">
                        <span className="text-xs font-extrabold text-emerald-800 bg-emerald-50 border border-emerald-300 px-3 py-1.5 rounded-lg inline-block font-mono">
                          Coach {p.coachNumber || p.coach || 'C1'} • Seat {p.seatNumber || (17 + idx * 3)} ({p.berthType || 'LOWER'})
                        </span>
                        <span className="text-[11px] text-emerald-600 font-semibold mt-1 block">
                          Current Status: CNF (Confirmed)
                        </span>
                      </div>
                    </div>
                  ))}
                </div>
              </div>

              {/* Digital ERS Download Actions */}
              <div className="pt-2 flex flex-col sm:flex-row gap-3">
                <button
                  type="button"
                  onClick={() => handleDownloadPdf(booking)}
                  className="flex-1 bg-rail-navy hover:bg-slate-800 text-white font-bold py-3.5 px-4 rounded-xl flex items-center justify-center space-x-2 transition shadow-md"
                >
                  <Download className="w-4 h-4 text-orange-400" />
                  <span>Download Official PDF Ticket (ERS)</span>
                </button>
                <button
                  type="button"
                  onClick={() => navigate(`/live-status?train=${booking.trainNumber || '22436'}`)}
                  className="sm:w-auto bg-slate-100 hover:bg-slate-200 text-slate-800 font-bold py-3.5 px-6 rounded-xl flex items-center justify-center space-x-2 transition border border-slate-300 text-sm"
                >
                  <Radio className="w-4 h-4 text-emerald-600" />
                  <span>Live Tracking</span>
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
