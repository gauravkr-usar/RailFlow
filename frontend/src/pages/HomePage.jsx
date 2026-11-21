import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Search, Calendar, ArrowRightLeft, ShieldCheck, Zap, RefreshCw, Award, Train, Sparkles, MapPin } from 'lucide-react';
import { trainAPI } from '../services/api';

const INITIAL_STATIONS = [
  { id: 1, code: 'NDLS', name: 'New Delhi', city: 'New Delhi', state: 'Delhi' },
  { id: 4, code: 'BSB', name: 'Varanasi Junction', city: 'Varanasi', state: 'Uttar Pradesh' },
  { id: 6, code: 'MMCT', name: 'Mumbai Central', city: 'Mumbai', state: 'Maharashtra' },
  { id: 5, code: 'HWH', name: 'Howrah Junction', city: 'Kolkata', state: 'West Bengal' },
  { id: 2, code: 'CNB', name: 'Kanpur Central', city: 'Kanpur', state: 'Uttar Pradesh' },
  { id: 3, code: 'PRYJ', name: 'Prayagraj Junction', city: 'Prayagraj', state: 'Uttar Pradesh' },
  { id: 7, code: 'BRC', name: 'Vadodara Junction', city: 'Vadodara', state: 'Gujarat' },
  { id: 8, code: 'SBC', name: 'KSR Bengaluru', city: 'Bengaluru', state: 'Karnataka' },
  { id: 9, code: 'MAS', name: 'Chennai Central', city: 'Chennai', state: 'Tamil Nadu' },
  { id: 10, code: 'PNBE', name: 'Patna Junction', city: 'Patna', state: 'Bihar' }
];

export default function HomePage() {
  const navigate = useNavigate();
  const [stations, setStations] = useState(INITIAL_STATIONS);
  const [source, setSource] = useState('NDLS');
  const [destination, setDestination] = useState('BSB');
  
  // Tomorrow's date default
  const tomorrow = new Date();
  tomorrow.setDate(tomorrow.getDate() + 1);
  const defaultDateStr = tomorrow.toISOString().split('T')[0];
  const [travelDate, setTravelDate] = useState(defaultDateStr);

  const [coachClass, setCoachClass] = useState('ALL');
  const [quota, setQuota] = useState('GENERAL');

  useEffect(() => {
    trainAPI.getStations().then((data) => {
      if (Array.isArray(data) && data.length > 0) {
        setStations(data);
      }
    }).catch(console.error);
  }, []);

  const handleSwapStations = () => {
    const temp = source;
    setSource(destination);
    setDestination(temp);
  };

  const handleSearch = (e) => {
    e.preventDefault();
    if (!source || !destination) {
      alert('Please select both source and destination stations');
      return;
    }
    if (source === destination) {
      alert('Source and destination stations cannot be the same');
      return;
    }
    navigate(`/search?source=${source}&destination=${destination}&date=${travelDate}&class=${coachClass}&quota=${quota}`);
  };

  const popularRoutes = [
    { from: 'NDLS', to: 'BSB', name: 'New Delhi ➔ Varanasi (Vande Bharat)' },
    { from: 'NDLS', to: 'MMCT', name: 'New Delhi ➔ Mumbai (Tejas Rajdhani)' },
    { from: 'NDLS', to: 'HWH', name: 'New Delhi ➔ Howrah (Rajdhani)' },
    { from: 'NDLS', to: 'CNB', name: 'New Delhi ➔ Kanpur' },
    { from: 'NDLS', to: 'PRYJ', name: 'New Delhi ➔ Prayagraj' },
    { from: 'NDLS', to: 'PNBE', name: 'New Delhi ➔ Patna' },
  ];

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col">
      {/* Hero Section */}
      <div className="relative bg-gradient-to-br from-rail-navy via-slate-900 to-rail-navy-dark text-white pt-12 pb-24 px-4 sm:px-6 lg:px-8 overflow-hidden">
        {/* Abstract Background accents */}
        <div className="absolute inset-0 opacity-10 pointer-events-none bg-[radial-gradient(#FB8C00_1px,transparent_1px)] [background-size:16px_16px]"></div>
        <div className="absolute -top-24 -right-24 w-96 h-96 bg-rail-orange/20 rounded-full blur-3xl pointer-events-none"></div>

        <div className="max-w-6xl mx-auto relative z-10">
          <div className="text-center max-w-3xl mx-auto mb-10">
            <div className="inline-flex items-center space-x-2 bg-white/10 backdrop-blur-md px-3.5 py-1.5 rounded-full text-xs font-semibold text-rail-orange mb-4 border border-white/10">
              <Sparkles className="w-4 h-4 text-amber-400" />
              <span>Next-Generation Indian Railway Booking Platform</span>
            </div>
            <h1 className="text-3xl sm:text-5xl font-extrabold tracking-tight leading-tight text-white">
              Instant Train Reservation with <span className="text-rail-orange">Zero Failure</span>
            </h1>
            <p className="mt-4 text-base sm:text-lg text-slate-300">
              Powered by atomic distributed locks, live seat inventory tracking, and automated RAC/WL progression.
            </p>
          </div>

          {/* Search Box Card */}
          <div className="bg-white rounded-3xl shadow-2xl p-6 sm:p-8 text-slate-800 border border-slate-100 relative">
            <form onSubmit={handleSearch} className="space-y-6">
              <div className="grid grid-cols-1 lg:grid-cols-12 gap-4 items-center">
                
                {/* Source Station */}
                <div className="lg:col-span-4 bg-slate-50 p-3.5 rounded-2xl border border-slate-200 focus-within:border-rail-orange focus-within:bg-white transition-all">
                  <label className="block text-[11px] font-bold text-slate-400 uppercase tracking-wider mb-1">
                    From Station
                  </label>
                  <div className="flex items-center space-x-2">
                    <MapPin className="w-5 h-5 text-rail-orange flex-shrink-0" />
                    <select
                      value={source}
                      onChange={(e) => setSource(e.target.value)}
                      className="w-full bg-transparent font-bold text-base text-slate-800 focus:outline-none cursor-pointer"
                    >
                      {stations.map((st) => (
                        <option key={st.code} value={st.code}>
                          {st.name} ({st.code}) - {st.city}
                        </option>
                      ))}
                    </select>
                  </div>
                </div>

                {/* Swap Button */}
                <div className="lg:col-span-1 flex justify-center -my-2 lg:my-0">
                  <button
                    type="button"
                    onClick={handleSwapStations}
                    className="p-3 rounded-full bg-slate-100 hover:bg-rail-orange hover:text-white transition-all text-slate-600 shadow-sm active:rotate-180 duration-200"
                    title="Swap Stations"
                  >
                    <ArrowRightLeft className="w-4 h-4" />
                  </button>
                </div>

                {/* Destination Station */}
                <div className="lg:col-span-4 bg-slate-50 p-3.5 rounded-2xl border border-slate-200 focus-within:border-rail-orange focus-within:bg-white transition-all">
                  <label className="block text-[11px] font-bold text-slate-400 uppercase tracking-wider mb-1">
                    To Station
                  </label>
                  <div className="flex items-center space-x-2">
                    <MapPin className="w-5 h-5 text-emerald-600 flex-shrink-0" />
                    <select
                      value={destination}
                      onChange={(e) => setDestination(e.target.value)}
                      className="w-full bg-transparent font-bold text-base text-slate-800 focus:outline-none cursor-pointer"
                    >
                      {stations.map((st) => (
                        <option key={st.code} value={st.code}>
                          {st.name} ({st.code}) - {st.city}
                        </option>
                      ))}
                    </select>
                  </div>
                </div>

                {/* Journey Date */}
                <div className="lg:col-span-3 bg-slate-50 p-3.5 rounded-2xl border border-slate-200 focus-within:border-rail-orange focus-within:bg-white transition-all">
                  <label className="block text-[11px] font-bold text-slate-400 uppercase tracking-wider mb-1">
                    Date of Journey
                  </label>
                  <div className="flex items-center space-x-2">
                    <Calendar className="w-5 h-5 text-rail-navy flex-shrink-0" />
                    <input
                      type="date"
                      value={travelDate}
                      min={new Date().toISOString().split('T')[0]}
                      onChange={(e) => setTravelDate(e.target.value)}
                      className="w-full bg-transparent font-bold text-base text-slate-800 focus:outline-none cursor-pointer"
                    />
                  </div>
                </div>
              </div>

              {/* Class & Quota filters + Submit button */}
              <div className="grid grid-cols-1 sm:grid-cols-3 lg:grid-cols-12 gap-4 items-center pt-2 border-t border-slate-100">
                <div className="lg:col-span-4">
                  <label className="block text-xs font-semibold text-slate-600 mb-1">Coach Class</label>
                  <select
                    value={coachClass}
                    onChange={(e) => setCoachClass(e.target.value)}
                    className="w-full bg-slate-50 border border-slate-200 rounded-xl px-3 py-2 text-sm font-semibold text-slate-700 focus:outline-none focus:border-rail-orange"
                  >
                    <option value="ALL">All Classes (1A, 2A, 3A, SL, CC)</option>
                    <option value="1A">AC First Class (1A)</option>
                    <option value="2A">AC 2 Tier (2A)</option>
                    <option value="3A">AC 3 Tier (3A)</option>
                    <option value="3E">AC 3 Economy (3E)</option>
                    <option value="SL">Sleeper (SL)</option>
                    <option value="CC">AC Chair Car (CC)</option>
                  </select>
                </div>

                <div className="lg:col-span-4">
                  <label className="block text-xs font-semibold text-slate-600 mb-1">Quota</label>
                  <select
                    value={quota}
                    onChange={(e) => setQuota(e.target.value)}
                    className="w-full bg-slate-50 border border-slate-200 rounded-xl px-3 py-2 text-sm font-semibold text-slate-700 focus:outline-none focus:border-rail-orange"
                  >
                    <option value="GENERAL">General Quota (GN)</option>
                    <option value="TATKAL">Tatkal Quota (TQ)</option>
                    <option value="LADIES">Ladies Quota (LD)</option>
                    <option value="SENIOR_CITIZEN">Senior Citizen / Lower Berth (SS)</option>
                  </select>
                </div>

                <div className="lg:col-span-4 sm:col-span-3 pt-2 sm:pt-4">
                  <button
                    type="submit"
                    className="w-full bg-gradient-to-r from-rail-orange to-rail-orange-dark hover:from-orange-600 hover:to-orange-700 text-white font-extrabold py-3.5 px-6 rounded-2xl shadow-lg shadow-orange-500/30 flex items-center justify-center space-x-2 transition-all transform active:scale-98"
                  >
                    <Search className="w-5 h-5" />
                    <span>Search Trains</span>
                  </button>
                </div>
              </div>
            </form>

            {/* Popular quick-select routes */}
            <div className="mt-6 pt-5 border-t border-slate-100 flex flex-wrap items-center gap-2">
              <span className="text-xs font-semibold text-slate-500 mr-1">Popular Routes:</span>
              {popularRoutes.map((route, i) => (
                <button
                  key={i}
                  type="button"
                  onClick={() => {
                    setSource(route.from);
                    setDestination(route.to);
                  }}
                  className="text-xs bg-slate-100 hover:bg-orange-100 hover:text-rail-orange-dark text-slate-700 px-3 py-1.5 rounded-full font-medium transition-colors border border-slate-200"
                >
                  {route.name}
                </button>
              ))}
            </div>
          </div>
        </div>
      </div>

      {/* Feature Highlights Grid */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 -mt-10 mb-16 relative z-20">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div className="bg-white rounded-2xl p-6 shadow-md border border-slate-100 flex items-start space-x-4">
            <div className="p-3 bg-orange-100 text-rail-orange rounded-xl">
              <Zap className="w-6 h-6" />
            </div>
            <div>
              <h3 className="font-bold text-slate-800 text-base">Atomic Lock Inventory</h3>
              <p className="text-xs text-slate-500 mt-1 leading-relaxed">
                Redis distributed locks guarantee exactly-once seat allocation even during peak 10:00 AM Tatkal rush.
              </p>
            </div>
          </div>

          <div className="bg-white rounded-2xl p-6 shadow-md border border-slate-100 flex items-start space-x-4">
            <div className="p-3 bg-blue-100 text-rail-navy rounded-xl">
              <RefreshCw className="w-6 h-6" />
            </div>
            <div>
              <h3 className="font-bold text-slate-800 text-base">Automatic RAC/WL Upgrades</h3>
              <p className="text-xs text-slate-500 mt-1 leading-relaxed">
                Smart FIFO waiting list cascade promotes RAC to Confirmed and WL to RAC automatically upon cancellation.
              </p>
            </div>
          </div>

          <div className="bg-white rounded-2xl p-6 shadow-md border border-slate-100 flex items-start space-x-4">
            <div className="p-3 bg-emerald-100 text-emerald-600 rounded-xl">
              <ShieldCheck className="w-6 h-6" />
            </div>
            <div>
              <h3 className="font-bold text-slate-800 text-base">Instant Refunds & PDF Tickets</h3>
              <p className="text-xs text-slate-500 mt-1 leading-relaxed">
                Automated IRCTC refund calculation with instant QR-coded printable PDF ticket generation.
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* Live Tatkal Status Banner */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 mb-16 w-full">
        <div className="bg-gradient-to-r from-amber-500/10 via-orange-500/10 to-transparent p-6 rounded-3xl border border-amber-200 flex flex-col md:flex-row items-center justify-between gap-4">
          <div className="flex items-center space-x-4">
            <div className="w-12 h-12 rounded-2xl bg-rail-orange text-white flex items-center justify-center font-bold text-xl shadow">
              <Train className="w-6 h-6" />
            </div>
            <div>
              <h4 className="font-extrabold text-slate-800 text-base">
                Daily Tatkal Booking Schedule
              </h4>
              <p className="text-xs text-slate-600 mt-0.5">
                • <strong>10:00 AM IST:</strong> AC Classes (1A, 2A, 3A, 3E, CC) &nbsp;|&nbsp;
                • <strong>11:00 AM IST:</strong> Non-AC Classes (SL, 2S)
              </p>
            </div>
          </div>
          <button
            onClick={() => navigate('/pnr-status')}
            className="px-5 py-2.5 rounded-xl bg-slate-900 text-white font-semibold text-xs hover:bg-slate-800 shadow transition-all whitespace-nowrap"
          >
            Track PNR Status ➔
          </button>
        </div>
      </div>
    </div>
  );
}
