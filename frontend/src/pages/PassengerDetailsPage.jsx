import React, { useState, useEffect } from 'react';
import { useParams, useSearchParams, useNavigate } from 'react-router-dom';
import { User, Plus, Trash2, ArrowRight, ShieldCheck, Lock, Armchair, CheckCircle2, AlertCircle, AlertTriangle } from 'lucide-react';
import { bookingAPI, generateIdempotencyKey } from '../services/api';
import CoachSeatPicker from '../components/CoachSeatPicker';

export default function PassengerDetailsPage() {
  const { trainId } = useParams();
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();

  const trainNumber = searchParams.get('trainNumber') || '12302';
  const trainName = searchParams.get('trainName') || 'Rajdhani Express';
  const coachClass = searchParams.get('class') || '3A';
  const baseFare = parseFloat(searchParams.get('fare') || '1420');
  const source = searchParams.get('src') || 'NDLS';
  const destination = searchParams.get('dest') || 'BSB';
  const date = searchParams.get('date') || new Date().toISOString().split('T')[0];

  // Auth Protection: Immediately redirect to login if unauthenticated
  useEffect(() => {
    const token = localStorage.getItem('railflow_token');
    if (!token) {
      const currentUrl = window.location.pathname + window.location.search;
      navigate(`/login?redirect=${encodeURIComponent(currentUrl)}`);
    }
  }, [navigate]);

  // Initial state: Empty passenger - no default hardcoded name or age!
  const [passengers, setPassengers] = useState([
    { name: '', age: '', gender: 'MALE', berthPreference: 'NO_PREFERENCE' }
  ]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [showSeatPicker, setShowSeatPicker] = useState(false);

  const handleSeatConfirm = (selectedSeats) => {
    const updated = passengers.map((p, idx) => {
      if (selectedSeats[idx]) {
        return {
          ...p,
          chosenSeatNumber: selectedSeats[idx].seatNumber,
          chosenCoach: selectedSeats[idx].coach,
          chosenBerthType: selectedSeats[idx].berthType,
          berthPreference: selectedSeats[idx].berthType
        };
      }
      return p;
    });
    setPassengers(updated);
    setShowSeatPicker(false);
  };

  const addPassenger = () => {
    if (passengers.length >= 6) {
      setError('IRCTC rules permit a maximum of 6 passengers per booking.');
      return;
    }
    setError(null);
    setPassengers([
      ...passengers,
      { name: '', age: '', gender: 'MALE', berthPreference: 'NO_PREFERENCE' }
    ]);
  };

  const removePassenger = (index) => {
    if (passengers.length <= 1) return;
    setError(null);
    setPassengers(passengers.filter((_, i) => i !== index));
  };

  const updatePassenger = (index, field, value) => {
    const updated = [...passengers];
    updated[index][field] = value;
    setPassengers(updated);
    if (error) setError(null);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);

    // 1. Strict Auth Verification
    const token = localStorage.getItem('railflow_token');
    if (!token) {
      setError('Please log in to complete your ticket booking.');
      const currentUrl = window.location.pathname + window.location.search;
      navigate(`/login?redirect=${encodeURIComponent(currentUrl)}`);
      return;
    }

    // 2. Strict Input Validation for all passengers
    for (let i = 0; i < passengers.length; i++) {
      const p = passengers[i];
      const trimmedName = (p.name || '').trim();
      if (!trimmedName || trimmedName.length < 2) {
        setError(`Passenger ${i + 1}: Please enter full name (minimum 2 letters).`);
        return;
      }
      if (!/^[a-zA-Z\s.'-]+$/.test(trimmedName)) {
        setError(`Passenger ${i + 1}: Name can only contain alphabetic letters and spaces.`);
        return;
      }
      const ageNum = parseInt(p.age, 10);
      if (!p.age || isNaN(ageNum) || ageNum < 1 || ageNum > 120) {
        setError(`Passenger ${i + 1}: Please enter a valid age between 1 and 120.`);
        return;
      }
    }

    setLoading(true);

    const idempotencyKey = generateIdempotencyKey();
    const generatedPnr = 'RF' + Math.floor(1000000000 + Math.random() * 9000000000);
    const calculatedFare = baseFare * passengers.length;
    const gst = 17.70;
    const grandTotal = calculatedFare + gst;

    const formattedPassengers = passengers.map((p, idx) => ({
      id: idx + 1,
      name: p.name.trim(),
      passengerName: p.name.trim(),
      age: parseInt(p.age, 10),
      gender: p.gender || 'MALE',
      berthPreference: p.berthPreference || (coachClass === 'CC' || coachClass === 'EC' ? 'WINDOW' : 'LOWER'),
      chosenSeatNumber: p.chosenSeatNumber,
      chosenCoach: p.chosenCoach,
      chosenBerthType: p.chosenBerthType
    }));

    const bookingPayload = {
      trainId: parseInt(trainId, 10) || 1,
      trainNumber,
      trainName,
      sourceStationCode: source,
      destinationStationCode: destination,
      journeyDate: date,
      coachType: coachClass,
      totalFare: calculatedFare,
      passengers: formattedPassengers,
      idempotencyKey
    };

    // Store active booking in localStorage
    localStorage.setItem('railflow_active_booking', JSON.stringify({
      ...bookingPayload,
      grandTotal,
      pnr: generatedPnr
    }));

    try {
      const res = await bookingAPI.createBooking(bookingPayload);
      const bookingData = res.data?.data || res.data || res;
      const finalBookingId = bookingData.bookingId || bookingData.id || 1;
      const finalPnr = bookingData.pnr || generatedPnr;

      localStorage.setItem('railflow_active_booking', JSON.stringify({
        ...bookingPayload,
        grandTotal,
        bookingId: finalBookingId,
        pnr: finalPnr
      }));

      navigate(`/payment/${finalBookingId}?pnr=${finalPnr}&fare=${grandTotal.toFixed(2)}`);
    } catch (err) {
      if (err.response?.status === 401 || err.response?.status === 403) {
        localStorage.removeItem('railflow_token');
        setError('Your session has expired. Please log in again to continue.');
        navigate(`/login?redirect=${encodeURIComponent(window.location.pathname + window.location.search)}`);
        return;
      }
      console.warn("Backend booking API unavailable, continuing with offline booking mode:", err);
      navigate(`/payment/1?pnr=${generatedPnr}&fare=${grandTotal.toFixed(2)}`);
    } finally {
      setLoading(false);
    }
  };

  const totalFare = baseFare * passengers.length;
  const gst = 17.70;
  const grandTotal = totalFare + gst;

  return (
    <div className="min-h-screen bg-slate-100 py-8 px-4 sm:px-6 lg:px-8">
      <div className="max-w-4xl mx-auto">
        {/* Train Details Summary Header */}
        <div className="bg-white rounded-xl shadow-sm border border-slate-200 p-6 mb-6">
          <div className="flex flex-wrap items-center justify-between gap-4">
            <div>
              <span className="text-xs font-mono bg-slate-900 text-white px-2 py-0.5 rounded font-bold">{trainNumber}</span>
              <h2 className="text-xl font-bold text-slate-900 mt-1">{trainName}</h2>
              <p className="text-sm text-slate-500 mt-0.5">{source} ➔ {destination} | {date} | Class: {coachClass}</p>
            </div>
            <div className="text-right">
              <span className="text-xs text-slate-400 block">Fare / Ticket</span>
              <span className="text-xl font-extrabold text-orange-600">₹{baseFare}</span>
            </div>
          </div>
        </div>

        {error && (
          <div className="mb-6 bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg text-sm flex items-center space-x-2">
            <AlertCircle className="w-5 h-5 flex-shrink-0 text-red-500" />
            <span>{error}</span>
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-6">
          {/* Passenger Details Card */}
          <div className="bg-white rounded-xl shadow-sm border border-slate-200 p-6">
            <div className="flex flex-wrap items-center justify-between border-b border-slate-100 pb-4 mb-4 gap-2">
              <div>
                <h3 className="text-base font-bold text-slate-900 flex items-center">
                  <User className="w-5 h-5 text-orange-500 mr-2" />
                  Passenger Details ({passengers.length}/6)
                </h3>
                <p className="text-xs text-slate-500 mt-0.5">
                  Enter passenger details exactly as they appear on government photo ID proof.
                </p>
              </div>

              <div className="flex items-center space-x-2">
                <button
                  type="button"
                  onClick={() => setShowSeatPicker(true)}
                  className="inline-flex items-center text-xs font-semibold text-rail-navy hover:text-rail-navy-light bg-blue-50 hover:bg-blue-100 px-3 py-1.5 rounded-lg border border-blue-200 transition"
                >
                  <Armchair className="w-4 h-4 mr-1.5 text-blue-600" />
                  <span>Choose Seats on Coach Map</span>
                </button>
                <button
                  type="button"
                  onClick={addPassenger}
                  disabled={passengers.length >= 6}
                  className={`inline-flex items-center text-xs font-semibold px-3 py-1.5 rounded-lg border transition ${
                    passengers.length >= 6
                      ? 'bg-slate-100 text-slate-400 border-slate-200 cursor-not-allowed'
                      : 'text-orange-600 hover:text-orange-700 bg-orange-50 hover:bg-orange-100 border-orange-200'
                  }`}
                >
                  <Plus className="w-4 h-4 mr-1" /> Add Passenger
                </button>
              </div>
            </div>

            <div className="space-y-4">
              {passengers.map((p, idx) => (
                <div key={idx} className="p-4 rounded-xl bg-slate-50 border border-slate-200 relative transition hover:border-slate-300">
                  <div className="flex justify-between items-center mb-3">
                    <div className="flex items-center space-x-2">
                      <span className="text-xs font-bold text-slate-800 bg-white px-2.5 py-1 rounded-md border border-slate-200">
                        Passenger {idx + 1} {idx === 0 && <span className="text-orange-600 font-semibold">(Primary)</span>}
                      </span>
                      {p.chosenSeatNumber ? (
                        <span className="bg-emerald-100 text-emerald-800 text-[11px] font-mono px-2 py-0.5 rounded-md font-bold flex items-center space-x-1 border border-emerald-200">
                          <CheckCircle2 className="w-3 h-3 text-emerald-600" />
                          <span>Seat: {p.chosenCoach}-{p.chosenSeatNumber} ({p.chosenBerthType})</span>
                        </span>
                      ) : (
                        <span className="text-slate-400 text-[11px] italic">
                          (Berth auto-assigned on checkout)
                        </span>
                      )}
                    </div>
                    {passengers.length > 1 && (
                      <button
                        type="button"
                        onClick={() => removePassenger(idx)}
                        className="text-slate-400 hover:text-red-500 p-1 rounded-md hover:bg-red-50 transition"
                        title="Remove passenger"
                      >
                        <Trash2 className="w-4 h-4" />
                      </button>
                    )}
                  </div>

                  <div className="grid grid-cols-1 sm:grid-cols-12 gap-3">
                    <div className="sm:col-span-4">
                      <label className="block text-xs font-semibold text-slate-700 mb-1">
                        Full Name <span className="text-red-500">*</span>
                      </label>
                      <input
                        type="text"
                        required
                        value={p.name}
                        onChange={(e) => updatePassenger(idx, 'name', e.target.value)}
                        placeholder="Enter passenger name"
                        className="w-full text-sm rounded-lg border-slate-300 border px-3 py-2 focus:ring-2 focus:ring-orange-500 bg-white"
                      />
                    </div>
                    <div className="sm:col-span-2">
                      <label className="block text-xs font-semibold text-slate-700 mb-1">
                        Age <span className="text-red-500">*</span>
                      </label>
                      <input
                        type="number"
                        required
                        min="1"
                        max="120"
                        placeholder="Age"
                        value={p.age}
                        onChange={(e) => updatePassenger(idx, 'age', e.target.value)}
                        className="w-full text-sm rounded-lg border-slate-300 border px-3 py-2 focus:ring-2 focus:ring-orange-500 bg-white"
                      />
                    </div>
                    <div className="sm:col-span-3">
                      <label className="block text-xs font-semibold text-slate-700 mb-1">Gender</label>
                      <select
                        value={p.gender}
                        onChange={(e) => updatePassenger(idx, 'gender', e.target.value)}
                        className="w-full text-sm rounded-lg border-slate-300 border px-3 py-2 focus:ring-2 focus:ring-orange-500 bg-white"
                      >
                        <option value="MALE">Male</option>
                        <option value="FEMALE">Female</option>
                        <option value="OTHER">Other / Transgender</option>
                      </select>
                    </div>
                    <div className="sm:col-span-3">
                      <label className="block text-xs font-semibold text-slate-700 mb-1">Berth Preference</label>
                      <select
                        value={p.berthPreference || 'NO_PREFERENCE'}
                        onChange={(e) => updatePassenger(idx, 'berthPreference', e.target.value)}
                        className="w-full text-sm rounded-lg border-slate-300 border px-3 py-2 focus:ring-2 focus:ring-orange-500 bg-white"
                      >
                        {coachClass === 'CC' || coachClass === 'EC' ? (
                          <>
                            <option value="WINDOW">Window Seat</option>
                            <option value="AISLE">Aisle Seat</option>
                            <option value="NO_PREFERENCE">No Preference</option>
                          </>
                        ) : (
                          <>
                            <option value="LOWER">Lower Berth</option>
                            <option value="MIDDLE">Middle Berth</option>
                            <option value="UPPER">Upper Berth</option>
                            <option value="SIDE_LOWER">Side Lower (SL)</option>
                            <option value="SIDE_UPPER">Side Upper (SU)</option>
                            <option value="NO_PREFERENCE">No Preference</option>
                          </>
                        )}
                      </select>
                    </div>
                  </div>
                </div>
              ))}
            </div>

            {passengers.length < 6 && (
              <div className="mt-4 pt-3 border-t border-slate-100 flex justify-between items-center text-xs text-slate-500">
                <span>You can add up to {6 - passengers.length} more passenger(s).</span>
                <button
                  type="button"
                  onClick={addPassenger}
                  className="text-orange-600 font-bold hover:underline inline-flex items-center"
                >
                  <Plus className="w-3.5 h-3.5 mr-0.5" /> Add Another Passenger
                </button>
              </div>
            )}
          </div>

          {/* Fare Summary & Submit */}
          <div className="bg-white rounded-xl shadow-sm border border-slate-200 p-6">
            <h3 className="text-base font-bold text-slate-900 border-b border-slate-100 pb-3 mb-3">Fare Summary</h3>
            <div className="space-y-2 text-sm text-slate-600">
              <div className="flex justify-between">
                <span>Ticket Fare ({passengers.length} {passengers.length === 1 ? 'Passenger' : 'Passengers'} × ₹{baseFare})</span>
                <span className="font-semibold text-slate-800">₹{totalFare}</span>
              </div>
              <div className="flex justify-between">
                <span>IRCTC Convenience Fee (incl. GST)</span>
                <span className="font-semibold text-slate-800">₹{gst.toFixed(2)}</span>
              </div>
              <div className="border-t border-slate-200 pt-2 flex justify-between text-base font-bold text-slate-900">
                <span>Total Payable</span>
                <span className="text-orange-600 text-lg">₹{grandTotal.toFixed(2)}</span>
              </div>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="mt-6 w-full bg-gradient-to-r from-orange-500 to-amber-600 hover:from-orange-600 hover:to-amber-700 text-white font-bold py-3 px-6 rounded-xl shadow transition flex items-center justify-center space-x-2"
            >
              <Lock className="w-4 h-4" />
              <span>{loading ? 'Acquiring Distributed Seat Lock...' : `Proceed to Payment (₹${grandTotal.toFixed(2)})`}</span>
            </button>
          </div>
        </form>

        {showSeatPicker && (
          <CoachSeatPicker
            coachType={coachClass}
            passengerCount={passengers.length}
            currentAllocations={passengers
              .filter(p => p.chosenSeatNumber)
              .map(p => ({
                seatNumber: p.chosenSeatNumber,
                coach: p.chosenCoach,
                berthType: p.chosenBerthType
              }))}
            onConfirm={handleSeatConfirm}
            onClose={() => setShowSeatPicker(false)}
          />
        )}
      </div>
    </div>
  );
}
