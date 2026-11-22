import React, { useState, useEffect, useRef } from 'react';
import { MapPin, Search, X, Train, Check, Sparkles } from 'lucide-react';
import { searchStations, getRelevantTrainsForStation } from '../utils/stationSearch';

export default function StationAutocomplete({
  label,
  value,
  onChange,
  placeholder = 'Search by station or city (e.g. New, Delhi, Giridih)',
  iconColor = 'text-rail-orange',
  showRelevantTrains = false
}) {
  const [isOpen, setIsOpen] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [results, setResults] = useState([]);
  const [selectedIndex, setSelectedIndex] = useState(0);
  const containerRef = useRef(null);
  const inputRef = useRef(null);

  // Sync display with incoming selected code
  useEffect(() => {
    if (value) {
      const match = searchStations(value, 1)[0];
      if (match) {
        setSearchTerm(`${match.name} (${match.code})`);
      } else {
        setSearchTerm(value);
      }
    }
  }, [value]);

  // Handle typing search
  const handleInputChange = (e) => {
    const val = e.target.value;
    setSearchTerm(val);
    setIsOpen(true);
    setSelectedIndex(0);

    if (val.trim().length > 0) {
      const matches = searchStations(val, 8);
      setResults(matches);
    } else {
      // Default to top major stations if empty
      setResults(searchStations('NDLS', 6));
    }
  };

  const handleSelectStation = (station) => {
    setSearchTerm(`${station.name} (${station.code})`);
    onChange(station.code, station);
    setIsOpen(false);
  };

  const handleClear = (e) => {
    e.stopPropagation();
    setSearchTerm('');
    onChange('', null);
    inputRef.current?.focus();
    setResults(searchStations('NDLS', 6));
    setIsOpen(true);
  };

  // Keyboard navigation: Arrow Up, Arrow Down, Enter, Escape
  const handleKeyDown = (e) => {
    if (!isOpen) {
      if (e.key === 'ArrowDown' || e.key === 'Enter') {
        setIsOpen(true);
      }
      return;
    }

    if (e.key === 'ArrowDown') {
      e.preventDefault();
      setSelectedIndex((prev) => (prev < results.length - 1 ? prev + 1 : 0));
    } else if (e.key === 'ArrowUp') {
      e.preventDefault();
      setSelectedIndex((prev) => (prev > 0 ? prev - 1 : results.length - 1));
    } else if (e.key === 'Enter') {
      e.preventDefault();
      if (results[selectedIndex]) {
        handleSelectStation(results[selectedIndex]);
      }
    } else if (e.key === 'Escape') {
      setIsOpen(false);
    }
  };

  // Click outside listener
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (containerRef.current && !containerRef.current.contains(event.target)) {
        setIsOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const relevantTrains = showRelevantTrains && value ? getRelevantTrainsForStation(value, searchTerm) : [];

  return (
    <div className="relative" ref={containerRef}>
      <label className="block text-[11px] font-bold text-slate-400 uppercase tracking-wider mb-1">
        {label}
      </label>
      <div className="flex items-center space-x-2">
        <MapPin className={`w-5 h-5 ${iconColor} flex-shrink-0`} />
        <div className="relative w-full">
          <input
            ref={inputRef}
            type="text"
            value={searchTerm}
            onChange={handleInputChange}
            onFocus={() => {
              setIsOpen(true);
              if (!searchTerm) {
                setResults(searchStations('NDLS', 6));
              } else {
                setResults(searchStations(searchTerm, 8));
              }
            }}
            onKeyDown={handleKeyDown}
            placeholder={placeholder}
            className="w-full bg-transparent font-bold text-base text-slate-800 placeholder-slate-400 focus:outline-none pr-6"
          />
          {searchTerm && (
            <button
              type="button"
              onClick={handleClear}
              className="absolute right-0 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 p-0.5"
            >
              <X className="w-4 h-4" />
            </button>
          )}
        </div>
      </div>

      {/* Dropdown Suggestions */}
      {isOpen && results.length > 0 && (
        <div className="absolute left-0 right-0 top-full mt-2 z-50 bg-white rounded-2xl shadow-2xl border border-slate-200 overflow-hidden divide-y divide-slate-100 max-h-80 overflow-y-auto animate-in fade-in slide-in-from-top-1 duration-150">
          <div className="px-3 py-1.5 bg-slate-50 text-[10px] font-bold text-slate-400 uppercase tracking-wider flex justify-between items-center">
            <span>Matching Stations ({results.length})</span>
            <span className="text-[10px] font-normal text-slate-400">Use ↑↓ keys + Enter</span>
          </div>
          {results.map((st, index) => {
            const isSelected = index === selectedIndex;
            return (
              <div
                key={st.code}
                onClick={() => handleSelectStation(st)}
                onMouseEnter={() => setSelectedIndex(index)}
                className={`px-4 py-3 cursor-pointer flex items-center justify-between transition ${
                  isSelected ? 'bg-orange-50/80 text-orange-950' : 'hover:bg-slate-50 text-slate-800'
                }`}
              >
                <div className="flex items-center space-x-3">
                  <div
                    className={`w-9 h-9 rounded-xl flex items-center justify-center font-mono font-black text-xs shadow-sm transition ${
                      isSelected ? 'bg-orange-600 text-white' : 'bg-slate-100 text-slate-700'
                    }`}
                  >
                    {st.code}
                  </div>
                  <div>
                    <div className="font-bold text-sm text-slate-900 flex items-center space-x-2">
                      <span>{st.name}</span>
                      <span className="text-xs font-mono font-semibold text-orange-600 bg-orange-100/60 px-1.5 py-0.2 rounded">
                        {st.code}
                      </span>
                    </div>
                    <div className="text-xs text-slate-500 mt-0.5">
                      {st.city} • <span className="text-slate-400">{st.state}</span>
                    </div>
                  </div>
                </div>

                {value === st.code && (
                  <Check className="w-4 h-4 text-emerald-600 flex-shrink-0" />
                )}
              </div>
            );
          })}
        </div>
      )}

      {/* Relevant Trains Teaser below input if requested */}
      {showRelevantTrains && relevantTrains.length > 0 && (
        <div className="mt-2 pt-2 border-t border-slate-100">
          <span className="text-[10px] font-bold text-orange-600 uppercase tracking-wider flex items-center mb-1">
            <Sparkles className="w-3 h-3 mr-1 text-amber-500" /> Top Trains from {value}:
          </span>
          <div className="flex flex-wrap gap-1.5">
            {relevantTrains.slice(0, 2).map((t) => (
              <span
                key={t.number}
                className="inline-flex items-center text-[10px] bg-slate-100 text-slate-700 px-2 py-0.5 rounded-md font-medium border border-slate-200"
              >
                <Train className="w-2.5 h-2.5 mr-1 text-slate-500" />
                {t.number} {t.name}
              </span>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
