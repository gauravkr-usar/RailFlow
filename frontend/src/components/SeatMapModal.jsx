import React, { useState } from 'react';
import { X, CheckCircle, Info, ShieldAlert } from 'lucide-react';

export default function SeatMapModal({ isOpen, onClose, coachType = '3A', onSelectSeat }) {
  const [selectedSeat, setSelectedSeat] = useState(null);

  if (!isOpen) return null;

  // Generate 8 bays (typical 64 seats for 3A / 72 for Sleeper)
  // Each bay has:
  // Main cabin: Lower (LB), Middle (MB), Upper (UB) x 2 sides = 6 berths
  // Side cabin: Side Lower (SL), Side Upper (SU) = 2 berths
  const baysCount = 8;
  const bays = [];

  for (let i = 0; i < baysCount; i++) {
    const startNum = i * 8 + 1;
    bays.push({
      bayIndex: i + 1,
      mainCabins: [
        { num: startNum, type: 'Lower (LB)', pos: 'LB' },
        { num: startNum + 1, type: 'Middle (MB)', pos: 'MB' },
        { num: startNum + 2, type: 'Upper (UB)', pos: 'UB' },
        { num: startNum + 3, type: 'Lower (LB)', pos: 'LB' },
        { num: startNum + 4, type: 'Middle (MB)', pos: 'MB' },
        { num: startNum + 5, type: 'Upper (UB)', pos: 'UB' },
      ],
      sideCabins: [
        { num: startNum + 6, type: 'Side Lower (SL)', pos: 'SL' },
        { num: startNum + 7, type: 'Side Upper (SU)', pos: 'SU' },
      ]
    });
  }

  // Pre-occupied mock seats
  const occupiedSeats = [2, 5, 11, 14, 18, 23, 27, 30, 35, 42, 49, 53, 58];

  const handleSeatClick = (seat) => {
    if (occupiedSeats.includes(seat.num)) return;
    setSelectedSeat(seat);
  };

  const handleConfirm = () => {
    if (selectedSeat && onSelectSeat) {
      onSelectSeat(selectedSeat);
      onClose();
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/70 backdrop-blur-sm animate-fadeIn">
      <div className="bg-white rounded-2xl shadow-2xl max-w-4xl w-full max-h-[90vh] flex flex-col overflow-hidden border border-slate-200">
        {/* Modal Header */}
        <div className="bg-rail-navy text-white px-6 py-4 flex justify-between items-center">
          <div>
            <h3 className="text-lg font-bold flex items-center space-x-2">
              <span>Interactive Coach Seat Layout</span>
              <span className="bg-rail-orange text-white text-xs px-2.5 py-0.5 rounded-full font-mono uppercase">
                Coach {coachType}
              </span>
            </h3>
            <p className="text-xs text-slate-300 mt-0.5">
              Select your preferred berth position. System assigns automatically if available.
            </p>
          </div>
          <button
            onClick={onClose}
            className="text-slate-300 hover:text-white p-1.5 rounded-lg hover:bg-white/10 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Legend */}
        <div className="bg-slate-50 border-b border-slate-200 px-6 py-3 flex flex-wrap items-center justify-between gap-3 text-xs">
          <div className="flex items-center space-x-4">
            <span className="flex items-center space-x-1.5">
              <span className="w-3.5 h-3.5 bg-emerald-100 border border-emerald-400 rounded"></span>
              <span className="text-slate-600">Available</span>
            </span>
            <span className="flex items-center space-x-1.5">
              <span className="w-3.5 h-3.5 bg-slate-200 border border-slate-300 rounded"></span>
              <span className="text-slate-400">Booked / Reserved</span>
            </span>
            <span className="flex items-center space-x-1.5">
              <span className="w-3.5 h-3.5 bg-rail-orange border border-rail-orange-dark text-white rounded"></span>
              <span className="font-semibold text-rail-navy">Selected</span>
            </span>
          </div>
          <div className="text-[11px] text-slate-500 flex items-center space-x-1">
            <Info className="w-3.5 h-3.5 text-blue-500" />
            <span>LB: Lower | MB: Middle | UB: Upper | SL: Side Lower | SU: Side Upper</span>
          </div>
        </div>

        {/* Interactive Coach Layout Body */}
        <div className="p-6 overflow-y-auto flex-1 space-y-6 bg-slate-100/50">
          <div className="border-4 border-slate-300 rounded-3xl p-4 bg-white shadow-inner relative">
            {/* Coach Entrance indicator */}
            <div className="flex justify-between items-center text-xs font-bold text-slate-400 uppercase tracking-widest pb-3 border-b border-dashed border-slate-300 mb-4">
              <span>◄ Coach Door / Restroom</span>
              <span className="bg-slate-200 px-3 py-1 rounded-full text-slate-700">Central Aisle Corridor</span>
              <span>Coach Door / Restroom ►</span>
            </div>

            {/* Bays container */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
              {bays.map((bay) => (
                <div key={bay.bayIndex} className="bg-slate-50 p-3 rounded-xl border border-slate-200 space-y-2">
                  <div className="text-[11px] font-semibold text-slate-500 text-center uppercase tracking-wide">
                    Bay #{bay.bayIndex}
                  </div>

                  {/* Main Cabin 6 berths (3 left, 3 right) */}
                  <div className="grid grid-cols-3 gap-1.5">
                    {bay.mainCabins.map((seat) => {
                      const isOccupied = occupiedSeats.includes(seat.num);
                      const isSelected = selectedSeat?.num === seat.num;
                      return (
                        <button
                          key={seat.num}
                          disabled={isOccupied}
                          onClick={() => handleSeatClick(seat)}
                          className={`p-2 rounded-lg border text-center transition-all ${
                            isSelected
                              ? 'bg-rail-orange text-white border-rail-orange-dark shadow-md scale-105'
                              : isOccupied
                              ? 'bg-slate-200 text-slate-400 border-slate-300 cursor-not-allowed opacity-60'
                              : 'bg-emerald-50 text-slate-700 border-emerald-300 hover:border-rail-orange hover:bg-orange-50'
                          }`}
                        >
                          <div className="text-xs font-bold">{seat.num}</div>
                          <div className="text-[9px] uppercase font-mono">{seat.pos}</div>
                        </button>
                      );
                    })}
                  </div>

                  {/* Corridor space */}
                  <div className="h-2 border-y border-dashed border-slate-200 flex items-center justify-center">
                    <span className="text-[8px] text-slate-300">corridor</span>
                  </div>

                  {/* Side Cabin 2 berths */}
                  <div className="grid grid-cols-2 gap-1.5">
                    {bay.sideCabins.map((seat) => {
                      const isOccupied = occupiedSeats.includes(seat.num);
                      const isSelected = selectedSeat?.num === seat.num;
                      return (
                        <button
                          key={seat.num}
                          disabled={isOccupied}
                          onClick={() => handleSeatClick(seat)}
                          className={`p-2 rounded-lg border text-center transition-all ${
                            isSelected
                              ? 'bg-rail-orange text-white border-rail-orange-dark shadow-md scale-105'
                              : isOccupied
                              ? 'bg-slate-200 text-slate-400 border-slate-300 cursor-not-allowed opacity-60'
                              : 'bg-blue-50 text-slate-700 border-blue-200 hover:border-rail-orange hover:bg-orange-50'
                          }`}
                        >
                          <div className="text-xs font-bold">{seat.num}</div>
                          <div className="text-[9px] uppercase font-mono">{seat.pos}</div>
                        </button>
                      );
                    })}
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Modal Footer */}
        <div className="bg-white border-t border-slate-200 px-6 py-4 flex flex-col sm:flex-row justify-between items-center gap-3">
          <div className="text-sm">
            {selectedSeat ? (
              <span className="text-slate-700 flex items-center space-x-1">
                <span>Selected Berth:</span>
                <strong className="text-rail-navy font-bold text-base">
                  #{selectedSeat.num} ({selectedSeat.type})
                </strong>
              </span>
            ) : (
              <span className="text-slate-500 italic text-xs">
                Click any available green berth to select your seat preference.
              </span>
            )}
          </div>

          <div className="flex space-x-3 w-full sm:w-auto">
            <button
              onClick={onClose}
              className="px-4 py-2 border border-slate-300 rounded-lg text-slate-700 text-sm font-medium hover:bg-slate-100 transition-colors flex-1 sm:flex-none"
            >
              Cancel
            </button>
            <button
              onClick={handleConfirm}
              disabled={!selectedSeat}
              className={`px-5 py-2 rounded-lg text-sm font-semibold transition-all flex-1 sm:flex-none ${
                selectedSeat
                  ? 'bg-rail-orange text-white hover:bg-rail-orange-dark shadow'
                  : 'bg-slate-200 text-slate-400 cursor-not-allowed'
              }`}
            >
              Confirm Berth Preference
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
