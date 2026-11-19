import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Clock, ArrowRight, MapPin, Calendar, LayoutGrid, CheckCircle2, AlertCircle } from 'lucide-react';
import SeatMapModal from './SeatMapModal';

export default function TrainCard({ train, travelDate, sourceCode, destCode }) {
  const navigate = useNavigate();
  const [selectedClass, setSelectedClass] = useState(train.classes?.[0]?.coachType || '3A');
  const [isSeatModalOpen, setIsSeatModalOpen] = useState(false);

  const handleBookNow = (coachType) => {
    navigate(`/booking/${train.id}?class=${coachType}&date=${travelDate || ''}&from=${sourceCode || train.sourceStation?.code}&to=${destCode || train.destinationStation?.code}`);
  };

  const getStatusBadge = (status, count) => {
    if (status === 'AVAILABLE' || count > 0) {
      return (
        <span className="text-emerald-700 bg-emerald-50 border border-emerald-200 px-2 py-0.5 rounded text-xs font-bold inline-flex items-center">
          <CheckCircle2 className="w-3 h-3 mr-1 text-emerald-600" />
          AVAILABLE {count}
        </span>
      );
    } else if (typeof status === 'string' && status.startsWith('RAC')) {
      return (
        <span className="text-amber-800 bg-amber-50 border border-amber-200 px-2 py-0.5 rounded text-xs font-bold inline-flex items-center">
          <AlertCircle className="w-3 h-3 mr-1 text-amber-600" />
          {status}
        </span>
      );
    } else {
      return (
        <span className="text-rose-800 bg-rose-50 border border-rose-200 px-2 py-0.5 rounded text-xs font-bold inline-flex items-center">
          <AlertCircle className="w-3 h-3 mr-1 text-rose-600" />
          {status || 'WL 12'}
        </span>
      );
    }
  };

  return (
    <div className="bg-white rounded-2xl shadow-sm border border-slate-200 hover:shadow-md transition-shadow overflow-hidden">
      {/* Train Header Strip */}
      <div className="bg-gradient-to-r from-slate-50 to-orange-50/40 px-6 py-4 border-b border-slate-100 flex flex-wrap justify-between items-center gap-3">
        <div className="flex items-center space-x-3">
          <span className="bg-rail-navy text-white text-xs font-mono font-bold px-2.5 py-1 rounded-md">
            #{train.trainNumber}
          </span>
          <h3 className="font-extrabold text-base sm:text-lg text-slate-800 tracking-tight">
            {train.trainName}
          </h3>
          {train.trainType && (
            <span className="text-[10px] bg-rail-orange/15 text-rail-orange-dark font-bold px-2 py-0.5 rounded-full uppercase">
              {train.trainType.replace('_', ' ')}
            </span>
          )}
        </div>

        {/* Running days badge */}
        <div className="text-xs text-slate-500 flex items-center space-x-1.5">
          <Calendar className="w-3.5 h-3.5 text-slate-400" />
          <span className="font-medium">Runs:</span>
          <div className="flex space-x-0.5 text-[10px]">
            {['M', 'T', 'W', 'T', 'F', 'S', 'S'].map((day, idx) => (
              <span
                key={idx}
                className="w-4 h-4 rounded-full flex items-center justify-center font-bold bg-emerald-100 text-emerald-800"
              >
                {day}
              </span>
            ))}
          </div>
        </div>
      </div>

      {/* Train Schedule Details */}
      <div className="p-6">
        <div className="grid grid-cols-1 sm:grid-cols-3 items-center gap-4 py-2 border-b border-slate-100 pb-5">
          {/* Departure */}
          <div className="text-left">
            <div className="text-2xl font-extrabold text-slate-900">{train.departureTime}</div>
            <div className="font-semibold text-slate-700 text-sm mt-0.5">
              {train.sourceStation?.name || train.sourceStation} ({train.sourceStation?.code || sourceCode})
            </div>
            <div className="text-xs text-slate-400">Departure Station</div>
          </div>

          {/* Duration Progress bar */}
          <div className="flex flex-col items-center justify-center">
            <span className="text-xs font-semibold text-slate-500 flex items-center mb-1">
              <Clock className="w-3.5 h-3.5 mr-1 text-rail-orange" />
              {train.duration || '15h 30m'}
            </span>
            <div className="w-full flex items-center px-4">
              <div className="w-2.5 h-2.5 rounded-full bg-rail-navy"></div>
              <div className="flex-1 h-0.5 bg-gradient-to-r from-rail-navy via-rail-orange to-emerald-600 relative">
                <ArrowRight className="w-3.5 h-3.5 text-rail-orange absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2" />
              </div>
              <div className="w-2.5 h-2.5 rounded-full bg-emerald-600"></div>
            </div>
            <span className="text-[11px] text-slate-400 mt-1">
              {train.distanceKm ? `${train.distanceKm} km` : 'Non-stop / Superfast'}
            </span>
          </div>

          {/* Arrival */}
          <div className="text-left sm:text-right">
            <div className="text-2xl font-extrabold text-slate-900">{train.arrivalTime}</div>
            <div className="font-semibold text-slate-700 text-sm mt-0.5">
              {train.destinationStation?.name || train.destinationStation} ({train.destinationStation?.code || destCode})
            </div>
            <div className="text-xs text-slate-400">Destination Station</div>
          </div>
        </div>

        {/* Classes & Availability Cards Grid */}
        <div className="mt-5">
          <div className="flex items-center justify-between mb-3">
            <span className="text-xs font-bold text-slate-500 uppercase tracking-wider">
              Select Coach Class & Check Live Availability:
            </span>
            <button
              onClick={() => setIsSeatModalOpen(true)}
              className="text-xs text-rail-orange font-semibold hover:text-rail-orange-dark flex items-center space-x-1 hover:underline"
            >
              <LayoutGrid className="w-3.5 h-3.5" />
              <span>Preview Coach Seat Map</span>
            </button>
          </div>

          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-3">
            {train.classes?.map((cls) => {
              const isSelected = selectedClass === cls.coachType;
              return (
                <div
                  key={cls.coachType}
                  onClick={() => setSelectedClass(cls.coachType)}
                  className={`p-3 rounded-xl border cursor-pointer transition-all ${
                    isSelected
                      ? 'border-rail-orange bg-orange-50/50 shadow-sm ring-2 ring-rail-orange/20'
                      : 'border-slate-200 bg-white hover:border-slate-300 hover:bg-slate-50'
                  }`}
                >
                  <div className="flex justify-between items-start">
                    <span className="font-extrabold text-slate-800 text-sm">{cls.coachType}</span>
                    <span className="font-extrabold text-rail-navy text-sm">₹{cls.fare}</span>
                  </div>
                  <div className="text-[11px] text-slate-500 truncate mb-2">
                    {cls.className || cls.coachType}
                  </div>
                  <div>
                    {getStatusBadge(cls.status, cls.availableSeats)}
                  </div>
                </div>
              );
            })}
          </div>
        </div>

        {/* Action Row */}
        <div className="mt-5 pt-4 border-t border-slate-100 flex flex-col sm:flex-row justify-between items-center gap-3">
          <div className="text-xs text-slate-500 flex items-center space-x-1">
            <span className="w-2 h-2 rounded-full bg-emerald-500 inline-block"></span>
            <span>Real-time IRCTC synchronization active</span>
          </div>

          <div className="flex items-center space-x-3 w-full sm:w-auto">
            <button
              onClick={() => setIsSeatModalOpen(true)}
              className="px-4 py-2 rounded-lg border border-slate-200 text-slate-700 text-xs font-semibold hover:bg-slate-50 transition-colors"
            >
              Seat Layout
            </button>
            <button
              onClick={() => handleBookNow(selectedClass)}
              className="flex-1 sm:flex-none px-6 py-2.5 rounded-xl bg-rail-orange hover:bg-rail-orange-dark text-white font-bold text-sm shadow-md shadow-orange-500/20 active:scale-95 transition-all"
            >
              Book Now ({selectedClass})
            </button>
          </div>
        </div>
      </div>

      {/* Seat Map Modal */}
      <SeatMapModal
        isOpen={isSeatModalOpen}
        onClose={() => setIsSeatModalOpen(false)}
        coachType={selectedClass}
        onSelectSeat={(seat) => {
          console.log('User picked seat:', seat);
        }}
      />
    </div>
  );
}
