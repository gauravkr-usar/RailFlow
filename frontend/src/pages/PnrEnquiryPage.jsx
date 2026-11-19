import React, { useState } from 'react';
import { Search, Train, Calendar, CheckCircle2, AlertCircle, Download, ArrowRight } from 'lucide-react';
import { bookingAPI, ticketAPI } from '../services/api';

export default function PnrEnquiryPage() {
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

  const handleDownloadPdf = (pnr) => {
    const url = ticketAPI.getPdfUrl(pnr);
    window.open(url, '_blank');
  };

  return (
    <div className="min-h-screen bg-slate-100 py-10 px-4 sm:px-6 lg:px-8">
      <div className="max-w-3xl mx-auto">
        <div className="text-center mb-8">
          <h1 className="text-3xl font-extrabold text-slate-900">PNR Enquiry & Live Status</h1>
          <p className="text-sm text-slate-500 mt-2">Check real-time confirmation status, coach, and berth details</p>
        </div>

        {/* Input Card */}
        <div className="bg-white rounded-2xl shadow-sm border border-slate-200 p-6 mb-6">
          <form onSubmit={handleSearch} className="flex flex-col sm:flex-row gap-3">
            <div className="relative flex-1">
              <input
                type="text"
                required
                maxLength={10}
                value={pnrInput}
                onChange={(e) => setPnrInput(e.target.value)}
                placeholder="Enter 10-digit PNR number"
                className="w-full pl-4 pr-10 py-3 text-base rounded-xl border border-slate-300 focus:ring-2 focus:ring-orange-500 font-mono uppercase tracking-wider"
              />
            </div>
            <button
              type="submit"
              disabled={loading}
              className="bg-orange-600 hover:bg-orange-700 text-white font-bold px-6 py-3 rounded-xl transition flex items-center justify-center space-x-2"
            >
              <Search className="w-5 h-5" />
              <span>{loading ? 'Searching...' : 'Get Status'}</span>
            </button>
          </form>
        </div>

        {error && (
          <div className="bg-red-50 border border-red-200 text-red-700 p-4 rounded-xl text-sm flex items-center mb-6">
            <AlertCircle className="w-5 h-5 mr-2 shrink-0" />
            <span>{error}</span>
          </div>
        )}

        {/* Result Card */}
        {booking && (
          <div className="bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden">
            <div className="bg-slate-900 text-white p-6 flex justify-between items-center">
              <div>
                <span className="text-xs text-slate-400 block font-medium">PNR STATUS</span>
                <span className="text-2xl font-mono font-bold tracking-widest text-orange-400">{booking.pnr}</span>
              </div>
              <span className={`text-xs font-bold px-3 py-1 rounded-full ${booking.status === 'CONFIRMED' ? 'bg-emerald-500 text-white' : 'bg-amber-500 text-white'}`}>
                {booking.status}
              </span>
            </div>

            <div className="p-6">
              {/* Train & Journey details */}
              <div className="border border-slate-100 bg-slate-50 rounded-xl p-4 mb-6">
                <div className="flex items-center space-x-2 mb-3">
                  <span className="font-mono bg-slate-900 text-white text-xs px-2 py-0.5 rounded font-bold">{booking.trainNumber}</span>
                  <span className="font-bold text-slate-800 text-base">{booking.trainName}</span>
                </div>
                <div className="grid grid-cols-3 gap-2 text-center text-sm">
                  <div>
                    <span className="font-bold text-slate-800 block">{booking.departureTime?.substring(0, 5) || '16:55'}</span>
                    <span className="text-xs text-slate-500">{booking.sourceStationName || booking.sourceStationCode}</span>
                  </div>
                  <div className="text-xs text-slate-400 flex flex-col justify-center items-center">
                    <span>{booking.journeyDate}</span>
                    <ArrowRight className="w-4 h-4 text-orange-500 my-0.5" />
                    <span>Class {booking.coachType}</span>
                  </div>
                  <div>
                    <span className="font-bold text-slate-800 block">{booking.arrivalTime?.substring(0, 5) || '23:45'}</span>
                    <span className="text-xs text-slate-500">{booking.destinationStationName || booking.destinationStationCode}</span>
                  </div>
                </div>
              </div>

              {/* Passengers Table */}
              <h3 className="text-sm font-bold text-slate-800 mb-3">Passenger Information</h3>
              <div className="space-y-2 mb-6">
                {booking.passengers?.map((p, idx) => (
                  <div key={idx} className="flex items-center justify-between p-3 rounded-lg border border-slate-200">
                    <div>
                      <span className="text-sm font-bold text-slate-800 block">{p.passengerName}</span>
                      <span className="text-xs text-slate-400">{p.age} yrs • {p.gender}</span>
                    </div>
                    <div className="text-right">
                      <span className="text-xs font-bold text-emerald-700 bg-emerald-50 border border-emerald-200 px-2.5 py-1 rounded-md block">
                        Coach {p.coachNumber} • Seat {p.seatNumber} ({p.berthType})
                      </span>
                      <span className="text-[11px] text-slate-400 mt-0.5 block">{p.status}</span>
                    </div>
                  </div>
                ))}
              </div>

              <button
                onClick={() => handleDownloadPdf(booking.pnr)}
                className="w-full bg-slate-900 hover:bg-slate-800 text-white font-semibold py-3 px-4 rounded-xl flex items-center justify-center space-x-2 transition"
              >
                <Download className="w-4 h-4 text-orange-400" />
                <span>Download Electronic Slip (PDF)</span>
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
