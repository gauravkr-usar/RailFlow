import React, { useState } from 'react';
import { Check, X, Users, AlertCircle, Info } from 'lucide-react';

export default function CoachSeatPicker({ coachType = '3A', passengerCount = 1, currentAllocations = [], onConfirm, onClose }) {
  // Normalize coach code
  const coachCode = (() => {
    switch (coachType) {
      case '1A': return 'H1';
      case '2A': return 'A1';
      case '3A': return 'B1';
      case 'EC': return 'E1';
      case 'CC': return 'C1';
      case 'SL': return 'S1';
      default: return 'D1';
    }
  })();

  // Generate simulated realistic layout based on coach type
  const isChairCar = coachType === 'CC' || coachType === 'EC' || coachType === '2S';

  // Predefined occupied seats simulation (deterministic based on seat index)
  const isOccupiedInitially = (num) => {
    const occupied = [3, 4, 8, 9, 13, 14, 21, 22, 28, 35, 42, 43, 49, 56];
    return occupied.includes(num);
  };

  // Generate seat list
  const totalSeats = isChairCar ? 60 : (coachType === '2A' ? 48 : 64);
  const seats = Array.from({ length: totalSeats }, (_, i) => {
    const seatNumber = i + 1;
    let berthType = 'LOWER';
    let berthShort = 'LB';

    if (isChairCar) {
      const col = (seatNumber - 1) % 5;
      if (col === 0 || col === 4) {
        berthType = 'WINDOW';
        berthShort = 'WS';
      } else if (col === 1) {
        berthType = 'MIDDLE';
        berthShort = 'MB';
      } else {
        berthType = 'AISLE';
        berthShort = 'AS';
      }
    } else if (coachType === '2A') {
      const pos = seatNumber % 6;
      if (pos === 1 || pos === 4) {
        berthType = 'LOWER';
        berthShort = 'LB';
      } else if (pos === 2 || pos === 5) {
        berthType = 'UPPER';
        berthShort = 'UB';
      } else if (pos === 3) {
        berthType = 'SIDE LOWER';
        berthShort = 'SL';
      } else {
        berthType = 'SIDE UPPER';
        berthShort = 'SU';
      }
    } else {
      // 3A or SL (8 per bay)
      const pos = seatNumber % 8;
      if (pos === 1 || pos === 4) {
        berthType = 'LOWER';
        berthShort = 'LB';
      } else if (pos === 2 || pos === 5) {
        berthType = 'MIDDLE';
        berthShort = 'MB';
      } else if (pos === 3 || pos === 6) {
        berthType = 'UPPER';
        berthShort = 'UB';
      } else if (pos === 7) {
        berthType = 'SIDE LOWER';
        berthShort = 'SL';
      } else {
        berthType = 'SIDE UPPER';
        berthShort = 'SU';
      }
    }

    return {
      seatNumber,
      coach: coachCode,
      berthType,
      berthShort,
      isOccupied: isOccupiedInitially(seatNumber)
    };
  });

  // State for user's selected seats (up to passengerCount)
  const [selectedSeats, setSelectedSeats] = useState(
    currentAllocations.length > 0
      ? currentAllocations
      : seats.filter(s => !s.isOccupied).slice(0, passengerCount)
  );

  const toggleSeat = (seat) => {
    if (seat.isOccupied) return;

    const exists = selectedSeats.some(s => s.seatNumber === seat.seatNumber);
    if (exists) {
      setSelectedSeats(selectedSeats.filter(s => s.seatNumber !== seat.seatNumber));
    } else {
      if (selectedSeats.length >= passengerCount) {
        // Replace oldest or shift
        setSelectedSeats([...selectedSeats.slice(1), seat]);
      } else {
        setSelectedSeats([...selectedSeats, seat]);
      }
    }
  };

  const handleApply = () => {
    onConfirm(selectedSeats);
  };

  return (
    <div className="fixed inset-0 z-50 bg-slate-900/70 backdrop-blur-sm flex items-center justify-center p-3 sm:p-6 overflow-y-auto animate-in fade-in duration-200">
      <div className="bg-white rounded-2xl shadow-2xl border border-slate-200 w-full max-w-4xl max-h-[92vh] flex flex-col overflow-hidden">
        {/* Header */}
        <div className="bg-rail-navy text-white px-6 py-4 flex items-center justify-between border-b border-slate-700">
          <div>
            <div className="flex items-center space-x-2">
              <span className="font-extrabold text-lg">Interactive Coach Seat Map</span>
              <span className="bg-rail-orange text-white text-xs px-2.5 py-0.5 rounded-full font-bold">
                Coach {coachCode} ({coachType})
              </span>
            </div>
            <p className="text-xs text-slate-300 mt-0.5">
              Select exact preferred seats for <strong className="text-white">{passengerCount} passenger{passengerCount > 1 ? 's' : ''}</strong>
            </p>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 rounded-lg text-slate-300 hover:text-white hover:bg-white/10 transition"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Legend */}
        <div className="bg-slate-50 px-6 py-3 border-b border-slate-200 flex flex-wrap items-center justify-between gap-3 text-xs">
          <div className="flex flex-wrap items-center gap-4">
            <span className="flex items-center space-x-1.5 font-medium text-slate-700">
              <span className="w-4 h-4 rounded bg-white border-2 border-emerald-500 shadow-sm inline-block"></span>
              <span>Available</span>
            </span>
            <span className="flex items-center space-x-1.5 font-medium text-slate-700">
              <span className="w-4 h-4 rounded bg-orange-600 text-white flex items-center justify-center text-[10px] font-bold shadow-sm inline-block">✓</span>
              <span>Selected ({selectedSeats.length}/{passengerCount})</span>
            </span>
            <span className="flex items-center space-x-1.5 font-medium text-slate-400">
              <span className="w-4 h-4 rounded bg-slate-300 border border-slate-400 inline-block"></span>
              <span>Occupied</span>
            </span>
          </div>

          <div className="hidden sm:flex items-center space-x-3 text-[11px] text-slate-500 font-mono">
            <span>LB: Lower</span>
            <span>MB: Middle</span>
            <span>UB: Upper</span>
            <span>SL: Side Lower</span>
            <span>SU: Side Upper</span>
          </div>
        </div>

        {/* Coach Body Layout */}
        <div className="flex-1 overflow-y-auto p-6 bg-slate-100">
          <div className="max-w-2xl mx-auto bg-white rounded-2xl p-5 border-2 border-slate-300 shadow-sm">
            {/* Coach Vestibule / Engine direction */}
            <div className="flex items-center justify-between pb-3 mb-4 border-b border-dashed border-slate-300 text-xs font-bold text-slate-500 uppercase tracking-wider">
              <span>← Entry / Door A</span>
              <span className="text-rail-navy font-extrabold tracking-widest">COACH {coachCode}</span>
              <span>Entry / Door B →</span>
            </div>

            {/* Grid of Seats */}
            {isChairCar ? (
              // Chair Car Layout: 3 + 2
              <div className="space-y-2">
                {Array.from({ length: totalSeats / 5 }).map((_, rowIdx) => {
                  const rowSeats = seats.slice(rowIdx * 5, rowIdx * 5 + 5);
                  return (
                    <div key={rowIdx} className="flex items-center justify-between py-1 px-2 hover:bg-slate-50 rounded-lg">
                      <span className="text-[10px] font-mono font-bold text-slate-400 w-6">R{rowIdx + 1}</span>
                      
                      {/* Left 3 seats */}
                      <div className="flex space-x-1.5">
                        {rowSeats.slice(0, 3).map(seat => {
                          const isSel = selectedSeats.some(s => s.seatNumber === seat.seatNumber);
                          return (
                            <button
                              key={seat.seatNumber}
                              disabled={seat.isOccupied}
                              onClick={() => toggleSeat(seat)}
                              className={`w-11 h-11 rounded-lg flex flex-col items-center justify-center transition font-mono relative ${
                                seat.isOccupied
                                  ? 'bg-slate-200 text-slate-400 cursor-not-allowed border border-slate-300'
                                  : isSel
                                  ? 'bg-orange-600 text-white font-extrabold shadow-md ring-2 ring-orange-400 scale-105'
                                  : 'bg-white hover:bg-emerald-50 text-slate-800 border-2 border-emerald-400 font-bold'
                              }`}
                            >
                              <span className="text-xs leading-none">{seat.seatNumber}</span>
                              <span className="text-[9px] opacity-80 leading-none mt-0.5">{seat.berthShort}</span>
                            </button>
                          );
                        })}
                      </div>

                      {/* Aisle */}
                      <div className="w-8 text-center text-[10px] text-slate-300 font-mono select-none">
                        | |
                      </div>

                      {/* Right 2 seats */}
                      <div className="flex space-x-1.5">
                        {rowSeats.slice(3, 5).map(seat => {
                          const isSel = selectedSeats.some(s => s.seatNumber === seat.seatNumber);
                          return (
                            <button
                              key={seat.seatNumber}
                              disabled={seat.isOccupied}
                              onClick={() => toggleSeat(seat)}
                              className={`w-11 h-11 rounded-lg flex flex-col items-center justify-center transition font-mono relative ${
                                seat.isOccupied
                                  ? 'bg-slate-200 text-slate-400 cursor-not-allowed border border-slate-300'
                                  : isSel
                                  ? 'bg-orange-600 text-white font-extrabold shadow-md ring-2 ring-orange-400 scale-105'
                                  : 'bg-white hover:bg-emerald-50 text-slate-800 border-2 border-emerald-400 font-bold'
                              }`}
                            >
                              <span className="text-xs leading-none">{seat.seatNumber}</span>
                              <span className="text-[9px] opacity-80 leading-none mt-0.5">{seat.berthShort}</span>
                            </button>
                          );
                        })}
                      </div>
                    </div>
                  );
                })}
              </div>
            ) : (
              // Sleeper / AC Layout (Bays of 8 / 6)
              <div className="space-y-4">
                {Array.from({ length: totalSeats / 8 }).map((_, bayIdx) => {
                  const baySeats = seats.slice(bayIdx * 8, bayIdx * 8 + 8);
                  const mainBay = baySeats.slice(0, 6);
                  const sideBay = baySeats.slice(6, 8);

                  return (
                    <div key={bayIdx} className="bg-slate-50 border border-slate-200 rounded-xl p-3">
                      <div className="text-[10px] font-mono font-bold text-slate-400 mb-2 uppercase tracking-wider">
                        Bay {bayIdx + 1}
                      </div>
                      
                      <div className="flex items-center justify-between">
                        {/* Main Cabin 6 berths (3 on top row, 3 on bottom row) */}
                        <div className="grid grid-cols-3 gap-2">
                          {mainBay.map(seat => {
                            const isSel = selectedSeats.some(s => s.seatNumber === seat.seatNumber);
                            return (
                              <button
                                key={seat.seatNumber}
                                disabled={seat.isOccupied}
                                onClick={() => toggleSeat(seat)}
                                className={`w-12 h-12 rounded-lg flex flex-col items-center justify-center transition font-mono ${
                                  seat.isOccupied
                                    ? 'bg-slate-200 text-slate-400 cursor-not-allowed border border-slate-300'
                                    : isSel
                                    ? 'bg-orange-600 text-white font-extrabold shadow-md ring-2 ring-orange-400 scale-105'
                                    : 'bg-white hover:bg-emerald-50 text-slate-800 border-2 border-emerald-400 font-bold'
                                }`}
                              >
                                <span className="text-xs leading-none">{seat.seatNumber}</span>
                                <span className="text-[9px] opacity-80 leading-none mt-0.5">{seat.berthShort}</span>
                              </button>
                            );
                          })}
                        </div>

                        {/* Gangway */}
                        <div className="w-10 text-center text-[10px] text-slate-300 font-mono select-none">
                          aisle
                        </div>

                        {/* Side berths 2 */}
                        <div className="flex flex-col space-y-2">
                          {sideBay.map(seat => {
                            const isSel = selectedSeats.some(s => s.seatNumber === seat.seatNumber);
                            return (
                              <button
                                key={seat.seatNumber}
                                disabled={seat.isOccupied}
                                onClick={() => toggleSeat(seat)}
                                className={`w-12 h-12 rounded-lg flex flex-col items-center justify-center transition font-mono ${
                                  seat.isOccupied
                                    ? 'bg-slate-200 text-slate-400 cursor-not-allowed border border-slate-300'
                                    : isSel
                                    ? 'bg-orange-600 text-white font-extrabold shadow-md ring-2 ring-orange-400 scale-105'
                                    : 'bg-white hover:bg-emerald-50 text-slate-800 border-2 border-emerald-400 font-bold'
                                }`}
                              >
                                <span className="text-xs leading-none">{seat.seatNumber}</span>
                                <span className="text-[9px] opacity-80 leading-none mt-0.5">{seat.berthShort}</span>
                              </button>
                            );
                          })}
                        </div>
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </div>
        </div>

        {/* Footer with Selection Summary and Actions */}
        <div className="bg-white px-6 py-4 border-t border-slate-200 flex flex-col sm:flex-row items-center justify-between gap-4">
          <div className="flex items-center space-x-2 text-sm text-slate-700">
            <span className="font-semibold">Selected Berths:</span>
            {selectedSeats.length === 0 ? (
              <span className="text-slate-400 italic">None selected yet</span>
            ) : (
              <div className="flex flex-wrap gap-1.5">
                {selectedSeats.map(s => (
                  <span key={s.seatNumber} className="bg-orange-100 text-orange-800 border border-orange-200 font-mono text-xs px-2.5 py-1 rounded-md font-bold">
                    {s.coach}-{s.seatNumber} ({s.berthType})
                  </span>
                ))}
              </div>
            )}
          </div>

          <div className="flex items-center space-x-3 w-full sm:w-auto">
            <button
              type="button"
              onClick={onClose}
              className="flex-1 sm:flex-initial px-4 py-2.5 rounded-xl border border-slate-300 text-slate-700 font-semibold hover:bg-slate-50 transition text-sm"
            >
              Cancel
            </button>
            <button
              type="button"
              disabled={selectedSeats.length === 0}
              onClick={handleApply}
              className="flex-1 sm:flex-initial px-6 py-2.5 rounded-xl bg-orange-600 hover:bg-orange-700 text-white font-bold transition text-sm flex items-center justify-center space-x-1.5 shadow-md shadow-orange-500/20 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <Check className="w-4 h-4" />
              <span>Confirm Seats ({selectedSeats.length})</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
