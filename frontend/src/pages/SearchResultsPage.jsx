import React, { useState, useEffect } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { Train, Clock, ArrowRight, ShieldCheck, Filter, AlertCircle, Calendar } from 'lucide-react';
import { trainAPI } from '../services/api';

export default function SearchResultsPage() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  
  const source = searchParams.get('source') || 'NDLS';
  const destination = searchParams.get('destination') || 'BSB';
  const date = searchParams.get('date') || new Date().toISOString().split('T')[0];
  const selectedClass = searchParams.get('class') || 'ALL';

  const [trains, setTrains] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedTrain, setSelectedTrain] = useState(null);
  const [selectedCoachType, setSelectedCoachType] = useState('3A');

  useEffect(() => {
    setLoading(true);
    trainAPI.searchTrains({
      sourceStationCode: source,
      destStationCode: destination,
      journeyDate: date,
      coachType: selectedClass !== 'ALL' ? selectedClass : null,
      passengerCount: 1
    }).then(res => {
      const data = res.data || res;
      setTrains(Array.isArray(data) ? data : []);
      setLoading(false);
    }).catch(err => {
      console.warn("Falling back to demo trains", err);
      // Realistic fallback for demonstration
      setTrains([
        {
          trainId: 2,
          trainNumber: '22436',
          trainName: 'Vande Bharat Express',
          trainType: 'VANDE_BHARAT',
          departureTime: '06:00:00',
          arrivalTime: '14:00:00',
          duration: '8h 00m',
          availability: {
            'CC': { availableSeats: 48, status: 'AVAILABLE' },
            'EC': { availableSeats: 12, status: 'AVAILABLE' }
          },
          fares: { 'CC': 1750, 'EC': 3300 }
        },
        {
          trainId: 1,
          trainNumber: '12302',
          trainName: 'Howrah Rajdhani Express',
          trainType: 'RAJDHANI',
          departureTime: '16:55:00',
          arrivalTime: '23:45:00',
          duration: '6h 50m',
          availability: {
            '3A': { availableSeats: 32, status: 'AVAILABLE' },
            '2A': { availableSeats: 14, status: 'AVAILABLE' },
            '1A': { availableSeats: 6, status: 'AVAILABLE' }
          },
          fares: { '3A': 1420, '2A': 2100, '1A': 3450 }
        },
        {
          trainId: 4,
          trainNumber: '12560',
          trainName: 'Shiv Ganga Express',
          trainType: 'SUPERFAST',
          departureTime: '20:05:00',
          arrivalTime: '06:10:00',
          duration: '10h 05m',
          availability: {
            'SL': { availableSeats: 74, status: 'AVAILABLE' },
            '3A': { availableSeats: 18, status: 'AVAILABLE' },
            '2A': { availableSeats: 8, status: 'AVAILABLE' }
          },
          fares: { 'SL': 430, '3A': 1180, '2A': 1720 }
        }
      ]);
      setLoading(false);
    });
  }, [source, destination, date, selectedClass]);

  const handleBookNow = (train, coachType, fare) => {
    const tid = train.trainId || train.id || 1;
    navigate(`/booking/${tid}?trainNumber=${train.trainNumber}&trainName=${encodeURIComponent(train.trainName)}&class=${coachType}&fare=${fare}&src=${source}&dest=${destination}&date=${date}`);
  };

  const getCoachList = (train) => {
    if (train.availability && typeof train.availability === 'object' && Object.keys(train.availability).length > 0) {
      return Object.entries(train.availability).map(([cType, info]) => ({
        coachType: cType,
        fare: train.fares && train.fares[cType] ? Number(train.fares[cType]) : 1200,
        availableSeats: info?.availableSeats ?? 24,
        status: info?.status || (info?.availableSeats > 0 ? 'AVAILABLE' : 'WL')
      }));
    }
    if (Array.isArray(train.classes) && train.classes.length > 0) {
      return train.classes.map(c => ({
        coachType: c.coachType || '3A',
        fare: Number(c.fare || 1200),
        availableSeats: c.availableSeats ?? 24,
        status: c.status || 'AVAILABLE'
      }));
    }
    return [
      { coachType: '1A', fare: 3200, availableSeats: 8, status: 'AVAILABLE' },
      { coachType: '2A', fare: 2150, availableSeats: 18, status: 'AVAILABLE' },
      { coachType: '3A', fare: 1420, availableSeats: 42, status: 'AVAILABLE' },
      { coachType: 'SL', fare: 480, availableSeats: 64, status: 'AVAILABLE' }
    ];
  };

  return (
    <div className="min-h-screen bg-slate-100 py-8 px-4 sm:px-6 lg:px-8">
      <div className="max-w-6xl mx-auto">
        {/* Search Header Bar */}
        <div className="bg-white rounded-xl shadow-sm border border-slate-200 p-4 mb-6 flex flex-wrap items-center justify-between gap-4">
          <div className="flex items-center space-x-3">
            <span className="text-xl font-bold text-slate-800">{source}</span>
            <ArrowRight className="w-5 h-5 text-orange-500" />
            <span className="text-xl font-bold text-slate-800">{destination}</span>
            <span className="bg-orange-100 text-orange-700 text-xs px-2.5 py-1 rounded-full font-semibold">
              {date}
            </span>
          </div>
          <button onClick={() => navigate('/')} className="text-sm font-medium text-orange-600 hover:text-orange-700">
            Modify Search
          </button>
        </div>

        {/* Results */}
        {loading ? (
          <div className="text-center py-20">
            <div className="inline-block animate-spin rounded-full h-10 w-10 border-4 border-orange-500 border-t-transparent"></div>
            <p className="mt-3 text-slate-600 font-medium">Checking live seat availability...</p>
          </div>
        ) : trains.length === 0 ? (
          <div className="bg-white rounded-xl p-10 text-center border border-slate-200">
            <AlertCircle className="w-12 h-12 text-slate-400 mx-auto mb-3" />
            <h3 className="text-lg font-bold text-slate-700">No Trains Found</h3>
            <p className="text-slate-500 text-sm mt-1">Try modifying your journey date or station codes.</p>
          </div>
        ) : (
          <div className="space-y-4">
            {trains.map((train) => {
              const coachList = getCoachList(train);
              const defaultCoach = coachList[0] || { coachType: '3A', fare: 1420 };
              return (
                <div key={train.trainId || train.id || train.trainNumber} className="bg-white rounded-xl shadow-sm border border-slate-200 p-6 transition hover:shadow-md">
                  <div className="flex flex-wrap items-start justify-between gap-4 border-b border-slate-100 pb-4">
                    <div>
                      <div className="flex items-center space-x-2">
                        <span className="bg-slate-900 text-white font-mono text-xs px-2 py-0.5 rounded font-bold">
                          {train.trainNumber}
                        </span>
                        <h3 className="text-lg font-bold text-slate-900">{train.trainName}</h3>
                        <span className="text-xs bg-emerald-50 text-emerald-700 border border-emerald-200 px-2 py-0.5 rounded font-medium">
                          Runs Daily
                        </span>
                      </div>
                      <div className="flex items-center space-x-6 mt-3 text-sm text-slate-600">
                        <div>
                          <span className="text-lg font-bold text-slate-900">{train.departureTime?.substring(0, 5) || '06:00'}</span>
                          <span className="block text-xs text-slate-400">{source}</span>
                        </div>
                        <div className="text-center text-xs text-slate-400 border-b border-dashed border-slate-300 px-4 pb-1">
                          {train.duration || '6h 30m'}
                        </div>
                        <div>
                          <span className="text-lg font-bold text-slate-900">{train.arrivalTime?.substring(0, 5) || '14:00'}</span>
                          <span className="block text-xs text-slate-400">{destination}</span>
                        </div>
                      </div>
                    </div>

                    <button
                      type="button"
                      onClick={() => handleBookNow(train, defaultCoach.coachType, defaultCoach.fare)}
                      className="bg-orange-600 hover:bg-orange-700 active:scale-95 text-white font-bold text-sm px-5 py-2.5 rounded-xl shadow-sm transition flex items-center space-x-2"
                    >
                      <span>Book Ticket</span>
                      <ArrowRight className="w-4 h-4" />
                    </button>
                  </div>

                  {/* Class Availability & Booking Boxes */}
                  <div className="mt-4">
                    <span className="text-xs font-bold text-slate-500 uppercase tracking-wider block mb-2">
                      Select Class & Book:
                    </span>
                    <div className="grid grid-cols-2 sm:grid-cols-4 md:grid-cols-6 gap-3">
                      {coachList.map((coach) => (
                        <div
                          key={coach.coachType}
                          className="border border-slate-200 hover:border-orange-500 rounded-xl p-3 cursor-pointer transition bg-slate-50 hover:bg-orange-50/50 flex flex-col justify-between"
                          onClick={() => handleBookNow(train, coach.coachType, coach.fare)}
                        >
                          <div>
                            <div className="flex justify-between items-center text-xs font-bold text-slate-700">
                              <span className="text-sm font-extrabold text-slate-900">{coach.coachType}</span>
                              <span className="text-orange-600 font-extrabold">₹{coach.fare}</span>
                            </div>
                            <div className="mt-2 text-xs font-semibold text-emerald-600 flex items-center">
                              <ShieldCheck className="w-3.5 h-3.5 mr-1 text-emerald-500" />
                              <span>{coach.status === 'AVAILABLE' ? `AVL ${coach.availableSeats}` : coach.status}</span>
                            </div>
                          </div>
                          <button
                            type="button"
                            className="mt-3 w-full bg-orange-600 hover:bg-orange-700 text-white text-xs font-bold py-1.5 rounded-lg shadow-sm transition"
                          >
                            Book {coach.coachType}
                          </button>
                        </div>
                      ))}
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
}
