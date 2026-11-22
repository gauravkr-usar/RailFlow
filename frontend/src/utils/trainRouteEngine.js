import trainsData from '../data/train_routes_data.json';

// In-memory indexed structures
let cachedTrains = null;
let stationToTrainsMap = null;

export function loadTrainRoutes() {
  if (cachedTrains && stationToTrainsMap) {
    return { trains: cachedTrains, stationMap: stationToTrainsMap };
  }

  const trains = trainsData;
  const stationMap = {};

  for (let i = 0; i < trains.length; i++) {
    const t = trains[i];
    if (!t.stops) continue;
    for (let j = 0; j < t.stops.length; j++) {
      const code = t.stops[j].c;
      if (!stationMap[code]) {
        stationMap[code] = [];
      }
      stationMap[code].push(i);
    }
  }

  cachedTrains = trains;
  stationToTrainsMap = stationMap;
  return { trains, stationMap };
}

/**
 * Calculates human-readable duration between departure and arrival
 */
function calculateDuration(depTime, arrTime, dayDiff = 0) {
  if (!depTime || !arrTime) return '8h 30m';
  const [depH, depM] = depTime.split(':').map(Number);
  const [arrH, arrM] = arrTime.split(':').map(Number);

  if (isNaN(depH) || isNaN(arrH)) return '8h 30m';

  let totalDepMinutes = depH * 60 + (depM || 0);
  let totalArrMinutes = arrH * 60 + (arrM || 0) + (dayDiff * 24 * 60);

  if (totalArrMinutes < totalDepMinutes) {
    totalArrMinutes += 24 * 60; // Next day
  }

  const diffMinutes = totalArrMinutes - totalDepMinutes;
  const hours = Math.floor(diffMinutes / 60);
  const minutes = diffMinutes % 60;

  return `${hours}h ${minutes > 0 ? `${minutes}m` : '00m'}`;
}

/**
 * Calculates realistic IRCTC fares based on journey distance and coach type
 */
function calculateFares(distanceKm, trainType) {
  const km = Math.max(distanceKm || 450, 100);
  const isPremium = trainType === 'RAJDHANI' || trainType === 'VANDE_BHARAT' || trainType === 'TEJAS';

  if (trainType === 'VANDE_BHARAT' || trainType === 'SHATABDI') {
    return {
      'CC': Math.round(km * 1.8 + 450),
      'EC': Math.round(km * 3.4 + 900)
    };
  }

  if (trainType === 'RAJDHANI' || trainType === 'TEJAS') {
    return {
      '3A': Math.round(km * 1.35 + 650),
      '2A': Math.round(km * 2.1 + 1050),
      '1A': Math.round(km * 3.6 + 1800)
    };
  }

  // Standard Superfast / Express
  return {
    'SL': Math.round(km * 0.45 + 180),
    '3A': Math.round(km * 1.2 + 500),
    '2A': Math.round(km * 1.85 + 850),
    '1A': Math.round(km * 3.2 + 1500)
  };
}

/**
 * Generates realistic IRCTC seat availability numbers based on train and class
 */
function generateAvailability(trainNum, coachType) {
  // Deterministic seed based on train number and class
  const seed = (parseInt(trainNum.replace(/\D/g, ''), 10) || 12302) + coachType.charCodeAt(0);
  const seats = (seed % 45) + 5;
  const isWaitlist = (seed % 10) === 0;

  return {
    availableSeats: isWaitlist ? 0 : seats,
    status: isWaitlist ? `WL ${(seed % 15) + 1}` : 'AVAILABLE'
  };
}

// City co-located station clusters (e.g. if searching New Delhi, also check Delhi Junction)
const CITY_CLUSTERS = {
  NDLS: ['DLI', 'NZM', 'ANVT', 'DEE'],
  DLI: ['NDLS', 'NZM', 'ANVT', 'DEE'],
  NZM: ['NDLS', 'DLI', 'ANVT', 'DEE'],
  ANVT: ['NDLS', 'DLI', 'NZM', 'DEE'],
  HWH: ['SDAH', 'KOAA', 'SHM'],
  SDAH: ['HWH', 'KOAA'],
  MMCT: ['CSMT', 'BDTS', 'DR', 'LTT'],
  CSMT: ['MMCT', 'BDTS', 'DR', 'LTT'],
  BDTS: ['MMCT', 'CSMT', 'DR', 'LTT'],
  NGRH: ['GRD', 'DHN'],
  GRD: ['NGRH', 'DHN'],
  PNBE: ['PPTA', 'DNR', 'RJPB'],
  BSB: ['BSBS', 'DDU']
};

/**
 * Searches for real trains connecting source to destination with exact stoppage times
 */
export async function searchRealTrains(fromStationCode, toStationCode, selectedClass = 'ALL') {
  if (!fromStationCode || !toStationCode) return [];
  const fromCode = fromStationCode.trim().toUpperCase();
  const toCode = toStationCode.trim().toUpperCase();

  const { trains, stationMap } = loadTrainRoutes();
  if (!trains || !trains.length || !stationMap) return [];

  const candidateIndices = stationMap[fromCode] || [];
  const results = [];
  const seenTrainNumbers = new Set();

  const evaluateTrain = (t, srcCode, destCode) => {
    if (!t || !t.stops || seenTrainNumbers.has(t.num)) return null;

    let fromIdx = -1;
    let toIdx = -1;

    for (let i = 0; i < t.stops.length; i++) {
      if (t.stops[i].c === srcCode && fromIdx === -1) {
        fromIdx = i;
      }
      if (t.stops[i].c === destCode && fromIdx !== -1) {
        toIdx = i;
        break;
      }
    }

    if (fromIdx !== -1 && toIdx !== -1 && fromIdx < toIdx) {
      const fromStop = t.stops[fromIdx];
      const toStop = t.stops[toIdx];
      const distance = Math.max((toStop.km || 0) - (fromStop.km || 0), 120);
      const dayDiff = Math.max((toStop.d || 1) - (fromStop.d || 1), 0);
      const dep = fromStop.dep || fromStop.arr || '08:00';
      const arr = toStop.arr || toStop.dep || '20:00';
      const duration = calculateDuration(dep, arr, dayDiff);
      const fares = calculateFares(distance, t.type);

      const availability = {};
      Object.keys(fares).forEach((cType) => {
        availability[cType] = generateAvailability(t.num, cType);
      });

      seenTrainNumbers.add(t.num);
      return {
        trainId: parseInt(t.num.replace(/\D/g, ''), 10) || Math.floor(Math.random() * 9000) + 1000,
        trainNumber: t.num,
        trainName: t.name,
        trainType: t.type,
        departureTime: dep.length === 5 ? `${dep}:00` : dep,
        arrivalTime: arr.length === 5 ? `${arr}:00` : arr,
        duration,
        distance: `${distance} km`,
        fromStation: fromStop.n || srcCode,
        toStation: toStop.n || destCode,
        fromCode: srcCode,
        toCode: destCode,
        availability,
        fares
      };
    }
    return null;
  };

  // 1. Direct station search
  for (const idx of candidateIndices) {
    const res = evaluateTrain(trains[idx], fromCode, toCode);
    if (res) results.push(res);
  }

  // 2. City-cluster fallback (e.g. if searching New Delhi to Varanasi, also check Delhi Junction)
  if (results.length < 3) {
    const fromCluster = [fromCode, ...(CITY_CLUSTERS[fromCode] || [])];
    const toCluster = [toCode, ...(CITY_CLUSTERS[toCode] || [])];

    for (const altFrom of fromCluster) {
      const altIndices = stationMap[altFrom] || [];
      for (const idx of altIndices) {
        for (const altTo of toCluster) {
          if (altFrom === altTo) continue;
          const res = evaluateTrain(trains[idx], altFrom, altTo);
          if (res) results.push(res);
          if (results.length >= 10) break;
        }
        if (results.length >= 10) break;
      }
      if (results.length >= 10) break;
    }
  }

  // Sort by departure time
  results.sort((a, b) => (a.departureTime || '').localeCompare(b.departureTime || ''));
  return results;
}
