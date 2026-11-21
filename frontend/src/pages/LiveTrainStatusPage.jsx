import React, { useState, useEffect } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { Search, Train, Clock, MapPin, CheckCircle2, ArrowRight, RefreshCw, Zap, ShieldAlert, Radio } from 'lucide-react';

export default function LiveTrainStatusPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const navigate = useNavigate();

  const initialTrain = searchParams.get('train') || '22436';
  const [trainQuery, setTrainQuery] = useState(initialTrain);
  const [selectedTrain, setSelectedTrain] = useState(initialTrain);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [lastUpdated, setLastUpdated] = useState(new Date().toLocaleTimeString());

  // Catalog of detailed train running routes
  const TRAIN_ROUTES = {
    '22436': {
      number: '22436',
      name: 'Vande Bharat Express',
      source: 'New Delhi (NDLS)',
      destination: 'Varanasi Junction (BSB)',
      speed: 130,
      currentStationIndex: 2, // between Kanpur and Prayagraj
      currentStatusText: 'Departed Kanpur Central (CNB) • Running 8 mins behind schedule',
      delayMinutes: 8,
      stations: [
        { code: 'NDLS', name: 'New Delhi', dist: 0, schArr: '--:--', schDep: '06:00', actArr: '--:--', actDep: '06:00', pf: '16', status: 'DEPARTED' },
        { code: 'CNB', name: 'Kanpur Central', dist: 440, schArr: '10:08', schDep: '10:10', actArr: '10:14', actDep: '10:18', pf: '5', status: 'DEPARTED' },
        { code: 'PRYJ', name: 'Prayagraj Junction', dist: 635, schArr: '12:08', schDep: '12:10', actArr: '12:16', actDep: '12:18', pf: '6', status: 'CURRENT' },
        { code: 'BSB', name: 'Varanasi Junction', dist: 759, schArr: '14:00', schDep: '--:--', actArr: '14:08', actDep: '--:--', pf: '1', status: 'UPCOMING' }
      ]
    },
    '12952': {
      number: '12952',
      name: 'Mumbai Tejas Rajdhani',
      source: 'New Delhi (NDLS)',
      destination: 'Mumbai Central (MMCT)',
      speed: 120,
      currentStationIndex: 3,
      currentStatusText: 'Approaching Vadodara Junction (BRC) • Right on time',
      delayMinutes: 0,
      stations: [
        { code: 'NDLS', name: 'New Delhi', dist: 0, schArr: '--:--', schDep: '16:55', actArr: '--:--', actDep: '16:55', pf: '3', status: 'DEPARTED' },
        { code: 'KOTA', name: 'Kota Junction', dist: 465, schArr: '21:30', schDep: '21:40', actArr: '21:28', actDep: '21:40', pf: '1', status: 'DEPARTED' },
        { code: 'RTM', name: 'Ratlam Junction', dist: 732, schArr: '00:30', schDep: '00:33', actArr: '00:29', actDep: '00:33', pf: '4', status: 'DEPARTED' },
        { code: 'BRC', name: 'Vadodara Junction', dist: 993, schArr: '03:40', schDep: '03:48', actArr: '03:40', actDep: '03:48', pf: '1', status: 'CURRENT' },
        { code: 'ST', name: 'Surat', dist: 1122, schArr: '05:13', schDep: '05:18', actArr: '05:13', actDep: '05:18', pf: '2', status: 'UPCOMING' },
        { code: 'BVI', name: 'Borivali', dist: 1356, schArr: '07:58', schDep: '08:00', actArr: '07:58', actDep: '08:00', pf: '7', status: 'UPCOMING' },
        { code: 'MMCT', name: 'Mumbai Central', dist: 1386, schArr: '08:35', schDep: '--:--', actArr: '08:35', actDep: '--:--', pf: '5', status: 'UPCOMING' }
      ]
    },
    '12560': {
      number: '12560',
      name: 'Shiv Ganga Express',
      source: 'New Delhi (NDLS)',
      destination: 'Banaras (BSBS)',
      speed: 110,
      currentStationIndex: 1,
      currentStatusText: 'Running at normal speed • On Time',
      delayMinutes: 0,
      stations: [
        { code: 'NDLS', name: 'New Delhi', dist: 0, schArr: '--:--', schDep: '20:05', actArr: '--:--', actDep: '20:05', pf: '12', status: 'DEPARTED' },
        { code: 'CNB', name: 'Kanpur Central', dist: 440, schArr: '01:00', schDep: '01:05', actArr: '01:00', actDep: '01:05', pf: '4', status: 'CURRENT' },
        { code: 'PRYJ', name: 'Prayagraj Junction', dist: 635, schArr: '03:45', schDep: '03:55', actArr: '03:45', actDep: '03:55', pf: '5', status: 'UPCOMING' },
        { code: 'BSBS', name: 'Banaras', dist: 755, schArr: '06:10', schDep: '--:--', actArr: '06:10', actDep: '--:--', pf: '8', status: 'UPCOMING' }
      ]
    },
    '12302': {
      number: '12302',
      name: 'Howrah Rajdhani Express',
      source: 'New Delhi (NDLS)',
      destination: 'Howrah Junction (HWH)',
      speed: 125,
      currentStationIndex: 2,
      currentStatusText: 'Passing Prayagraj Jn (PRYJ) • Running 14 mins behind schedule',
      delayMinutes: 14,
      stations: [
        { code: 'NDLS', name: 'New Delhi', dist: 0, schArr: '--:--', schDep: '16:50', actArr: '--:--', actDep: '16:50', pf: '9', status: 'DEPARTED' },
        { code: 'CNB', name: 'Kanpur Central', dist: 440, schArr: '21:32', schDep: '21:37', actArr: '21:40', actDep: '21:46', pf: '5', status: 'DEPARTED' },
        { code: 'PRYJ', name: 'Prayagraj Junction', dist: 635, schArr: '23:43', schDep: '23:45', actArr: '23:57', actDep: '23:59', pf: '4', status: 'CURRENT' },
        { code: 'DDU', name: 'Pt. Deen Dayal Upadhyaya Jn', dist: 788, schArr: '01:42', schDep: '01:52', actArr: '01:56', actDep: '02:06', pf: '2', status: 'UPCOMING' },
        { code: 'GAYA', name: 'Gaya Junction', dist: 993, schArr: '04:10', schDep: '04:13', actArr: '04:24', actDep: '04:27', pf: '1', status: 'UPCOMING' },
        { code: 'HWH', name: 'Howrah Junction', dist: 1451, schArr: '09:55', schDep: '--:--', actArr: '10:09', actDep: '--:--', pf: '9', status: 'UPCOMING' }
      ]
    }
  };

  const currentRoute = TRAIN_ROUTES[selectedTrain] || TRAIN_ROUTES['22436'];

  const handleSearch = (e) => {
    e.preventDefault();
    const clean = trainQuery.trim();
    if (TRAIN_ROUTES[clean]) {
      setSelectedTrain(clean);
      setSearchParams({ train: clean });
    } else {
      // Find matching by number or partial name
      const foundKey = Object.keys(TRAIN_ROUTES).find(k => 
        k.includes(clean) || TRAIN_ROUTES[k].name.toLowerCase().includes(clean.toLowerCase())
      );
      if (foundKey) {
        setSelectedTrain(foundKey);
        setSearchParams({ train: foundKey });
      } else {
        setSelectedTrain('22436');
      }
    }
    setLastUpdated(new Date().toLocaleTimeString());
  };

  const handleRefresh = () => {
    setIsRefreshing(true);
    setTimeout(() => {
      setIsRefreshing(false);
      setLastUpdated(new Date().toLocaleTimeString());
    }, 600);
  };

  return (
    <div className="min-h-screen bg-slate-100 py-8 px-4 sm:px-6 lg:px-8">
      <div className="max-w-5xl mx-auto space-y-6">
        {/* Title */}
        <div className="text-center">
          <div className="inline-flex items-center space-x-2 bg-rail-navy text-white px-3.5 py-1 rounded-full text-xs font-semibold mb-2 shadow-sm">
            <Radio className="w-3.5 h-3.5 text-rail-orange animate-pulse" />
            <span>GPS-Synchronized Live Satellite Tracking</span>
          </div>
          <h1 className="text-3xl font-extrabold text-slate-900 tracking-tight">Live Train Running Status</h1>
          <p className="text-sm text-slate-500 mt-1">Real-time GPS train location, arrival delays, and platform numbers</p>
        </div>

        {/* Train Search Bar */}
        <div className="bg-white rounded-2xl shadow-sm border border-slate-200 p-6">
          <form onSubmit={handleSearch} className="flex flex-col sm:flex-row gap-3">
            <div className="relative flex-1">
              <input
                type="text"
                value={trainQuery}
                onChange={(e) => setTrainQuery(e.target.value)}
                placeholder="Enter Train Number (e.g. 22436, 12952) or Name"
                className="w-full pl-4 pr-10 py-3 text-base rounded-xl border border-slate-300 focus:ring-2 focus:ring-orange-500 font-medium"
              />
            </div>
            <button
              type="submit"
              className="bg-orange-600 hover:bg-orange-700 text-white font-bold px-6 py-3 rounded-xl transition flex items-center justify-center space-x-2 shadow-md shadow-orange-500/20"
            >
              <Search className="w-5 h-5" />
              <span>Track Train</span>
            </button>
          </form>

          {/* Quick train chips */}
          <div className="flex flex-wrap items-center gap-2 mt-4 pt-4 border-t border-slate-100 text-xs">
            <span className="text-slate-400 font-medium">Popular Trains:</span>
            {Object.keys(TRAIN_ROUTES).map(num => (
              <button
                key={num}
                type="button"
                onClick={() => {
                  setSelectedTrain(num);
                  setTrainQuery(num);
                  setSearchParams({ train: num });
                }}
                className={`px-3 py-1.5 rounded-lg border font-mono transition ${
                  selectedTrain === num
                    ? 'bg-rail-navy text-white border-rail-navy font-bold'
                    : 'bg-slate-50 hover:bg-slate-100 text-slate-700 border-slate-200'
                }`}
              >
                {num} - {TRAIN_ROUTES[num].name}
              </button>
            ))}
          </div>
        </div>

        {/* Live Train Status Card */}
        <div className="bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden">
          {/* Header Bar */}
          <div className="bg-gradient-to-r from-slate-900 to-rail-navy text-white p-6">
            <div className="flex flex-wrap items-center justify-between gap-4">
              <div>
                <div className="flex items-center space-x-2">
                  <span className="bg-rail-orange text-white text-xs px-2.5 py-0.5 rounded font-mono font-bold">
                    #{currentRoute.number}
                  </span>
                  <h2 className="text-2xl font-bold tracking-tight text-white">{currentRoute.name}</h2>
                </div>
                <p className="text-xs text-slate-300 mt-1">
                  {currentRoute.source} ➔ {currentRoute.destination}
                </p>
              </div>

              <div className="flex items-center space-x-3">
                <div className="text-right">
                  <span className="text-[10px] text-slate-400 block uppercase tracking-wider">Current Speed</span>
                  <span className="text-xl font-mono font-extrabold text-emerald-400 flex items-center space-x-1">
                    <Zap className="w-4 h-4 text-emerald-400 fill-emerald-400" />
                    <span>{currentRoute.speed} km/h</span>
                  </span>
                </div>

                <button
                  type="button"
                  onClick={handleRefresh}
                  className="p-2.5 rounded-xl bg-white/10 hover:bg-white/20 text-white transition flex items-center justify-center"
                  title="Refresh GPS location"
                >
                  <RefreshCw className={`w-5 h-5 ${isRefreshing ? 'animate-spin' : ''}`} />
                </button>
              </div>
            </div>

            {/* Current Position Banner */}
            <div className="mt-4 pt-4 border-t border-slate-700 flex flex-wrap items-center justify-between gap-2 text-xs">
              <div className="flex items-center space-x-2">
                <span className="w-2.5 h-2.5 rounded-full bg-emerald-400 inline-block animate-ping"></span>
                <span className="font-semibold text-emerald-300">{currentRoute.currentStatusText}</span>
              </div>
              <div className="text-slate-400 text-[11px]">
                Last GPS ping: <strong className="text-white">{lastUpdated}</strong>
              </div>
            </div>
          </div>

          {/* Interactive Route Track */}
          <div className="p-6">
            <h3 className="text-sm font-bold text-slate-700 uppercase tracking-wider mb-6 flex items-center justify-between">
              <span>Station Timeline & Arrival Milestones</span>
              <span className={`text-xs px-2.5 py-1 rounded-full font-bold ${
                currentRoute.delayMinutes === 0
                  ? 'bg-emerald-100 text-emerald-800'
                  : 'bg-amber-100 text-amber-800'
              }`}>
                {currentRoute.delayMinutes === 0 ? '✓ Running On Time' : `⚠️ +${currentRoute.delayMinutes} mins Late`}
              </span>
            </h3>

            <div className="space-y-6 relative before:absolute before:inset-0 before:left-5 before:w-1 before:bg-slate-200 before:z-0">
              {currentRoute.stations.map((st, idx) => {
                const isDeparted = st.status === 'DEPARTED';
                const isCurrent = st.status === 'CURRENT';
                const isUpcoming = st.status === 'UPCOMING';

                return (
                  <div key={st.code} className="relative z-10 flex items-start space-x-4 pl-1">
                    {/* Track Node Indicator */}
                    <div className="shrink-0 mt-1">
                      {isDeparted && (
                        <div className="w-8 h-8 rounded-full bg-emerald-500 text-white flex items-center justify-center shadow-md">
                          <CheckCircle2 className="w-5 h-5" />
                        </div>
                      )}
                      {isCurrent && (
                        <div className="w-8 h-8 rounded-full bg-rail-navy text-rail-orange ring-4 ring-orange-200 flex items-center justify-center shadow-lg animate-bounce">
                          <Train className="w-4 h-4" />
                        </div>
                      )}
                      {isUpcoming && (
                        <div className="w-8 h-8 rounded-full bg-white border-2 border-slate-300 text-slate-400 flex items-center justify-center text-xs font-bold font-mono">
                          {idx + 1}
                        </div>
                      )}
                    </div>

                    {/* Station Information Card */}
                    <div className={`flex-1 rounded-xl p-4 border transition ${
                      isCurrent
                        ? 'bg-orange-50/60 border-orange-300 shadow-sm'
                        : isDeparted
                        ? 'bg-slate-50 border-slate-200 opacity-90'
                        : 'bg-white border-slate-200'
                    }`}>
                      <div className="flex flex-wrap items-center justify-between gap-2 mb-2">
                        <div className="flex items-center space-x-2">
                          <span className="font-mono bg-slate-900 text-white text-xs px-2 py-0.5 rounded font-bold">
                            {st.code}
                          </span>
                          <span className="font-bold text-slate-900 text-base">{st.name}</span>
                          <span className="text-xs text-slate-400">({st.dist} km)</span>
                        </div>

                        <div className="flex items-center space-x-2">
                          <span className="bg-slate-200 text-slate-700 text-xs px-2.5 py-0.5 rounded font-mono font-bold">
                            Platform {st.pf}
                          </span>
                          {isCurrent && (
                            <span className="bg-orange-600 text-white text-[11px] px-2 py-0.5 rounded-full font-bold uppercase tracking-wider">
                              Approaching Now
                            </span>
                          )}
                        </div>
                      </div>

                      {/* Timetable comparisons */}
                      <div className="grid grid-cols-2 sm:grid-cols-4 gap-2 text-xs pt-2 border-t border-slate-100 font-mono">
                        <div>
                          <span className="text-slate-400 block text-[10px]">SCH. ARRIVAL</span>
                          <span className="font-bold text-slate-700">{st.schArr}</span>
                        </div>
                        <div>
                          <span className="text-slate-400 block text-[10px]">ACT./EST. ARRIVAL</span>
                          <span className={`font-bold ${st.actArr !== st.schArr ? 'text-amber-600' : 'text-slate-900'}`}>
                            {st.actArr}
                          </span>
                        </div>
                        <div>
                          <span className="text-slate-400 block text-[10px]">SCH. DEPARTURE</span>
                          <span className="font-bold text-slate-700">{st.schDep}</span>
                        </div>
                        <div>
                          <span className="text-slate-400 block text-[10px]">ACT./EST. DEPARTURE</span>
                          <span className={`font-bold ${st.actDep !== st.schDep ? 'text-amber-600' : 'text-slate-900'}`}>
                            {st.actDep}
                          </span>
                        </div>
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>

          {/* Action Bar */}
          <div className="bg-slate-50 px-6 py-4 border-t border-slate-200 flex flex-wrap items-center justify-between gap-3">
            <span className="text-xs text-slate-500">
              Need to travel on this train? Check live seats and reserve instantly.
            </span>
            <button
              type="button"
              onClick={() => navigate(`/?source=NDLS&dest=BSB`)}
              className="bg-rail-navy hover:bg-slate-800 text-white text-xs font-bold px-4 py-2.5 rounded-xl transition flex items-center space-x-1.5"
            >
              <span>Book Seats on {currentRoute.name}</span>
              <ArrowRight className="w-3.5 h-3.5" />
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
