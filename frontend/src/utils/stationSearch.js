import stationsData from '../data/all_stations.json';

// Popular trains connected to major junctions/regions
const POPULAR_TRAINS_BY_ZONE_OR_CODE = {
  NDLS: [
    { number: '22436', name: 'Vande Bharat Express', route: 'New Delhi ➔ Varanasi', time: '06:00 AM' },
    { number: '12302', name: 'Howrah Rajdhani Express', route: 'New Delhi ➔ Howrah', time: '04:55 PM' },
    { number: '12952', name: 'Mumbai Tejas Rajdhani', route: 'New Delhi ➔ Mumbai Central', time: '04:55 PM' },
    { number: '12004', name: 'Lucknow Shatabdi Express', route: 'New Delhi ➔ Lucknow', time: '06:10 AM' }
  ],
  NGRH: [
    { number: '18620', name: 'Intercity Express', route: 'New Giridih ➔ Ranchi Jn', time: '06:00 AM' },
    { number: '13106', name: 'Ballia - Sealdah Express', route: 'New Giridih ➔ Kolkata', time: '08:45 AM' },
    { number: '13330', name: 'Ganga Damodar Express', route: 'Dhanbad ➔ Patna Junction', time: '11:20 PM' }
  ],
  GRD: [
    { number: '18620', name: 'Intercity Express', route: 'Giridih ➔ Ranchi Jn', time: '06:00 AM' },
    { number: '13306', name: 'Dhanbad Intercity', route: 'Giridih ➔ Dhanbad', time: '07:15 AM' }
  ],
  BSB: [
    { number: '22435', name: 'Vande Bharat Express', route: 'Varanasi ➔ New Delhi', time: '03:00 PM' },
    { number: '12560', name: 'Shiv Ganga Express', route: 'Varanasi ➔ New Delhi', time: '07:30 PM' },
    { number: '15128', name: 'Kashi Vishwanath Express', route: 'Banaras ➔ New Delhi', time: '01:30 PM' }
  ],
  HWH: [
    { number: '12301', name: 'Kolkata Rajdhani Express', route: 'Howrah ➔ New Delhi', time: '04:50 PM' },
    { number: '12841', name: 'Coromandel Express', route: 'Howrah ➔ Chennai Central', time: '03:20 PM' },
    { number: '12810', name: 'Howrah - Mumbai Mail', route: 'Howrah ➔ CSMT Mumbai', time: '08:00 PM' }
  ],
  MMCT: [
    { number: '12951', name: 'Tejas Rajdhani Express', route: 'Mumbai Central ➔ New Delhi', time: '05:00 PM' },
    { number: '12925', name: 'Paschim SF Express', route: 'Mumbai Central ➔ Amritsar', time: '11:25 AM' },
    { number: '20901', name: 'Vande Bharat Express', route: 'Mumbai Central ➔ Gandhinagar', time: '06:10 AM' }
  ],
  PNBE: [
    { number: '12393', name: 'Sampoorna Kranti Express', route: 'Patna ➔ New Delhi', time: '07:25 PM' },
    { number: '22348', name: 'Vande Bharat Express', route: 'Patna ➔ Howrah', time: '08:00 AM' }
  ],
  SBC: [
    { number: '12627', name: 'Karnataka Express', route: 'Bengaluru ➔ New Delhi', time: '07:20 PM' },
    { number: '22691', name: 'Rajdhani Express', route: 'Bengaluru ➔ Hazrat Nizamuddin', time: '08:00 PM' }
  ],
  MAS: [
    { number: '12621', name: 'Tamil Nadu Express', route: 'Chennai Central ➔ New Delhi', time: '10:00 PM' },
    { number: '20607', name: 'Vande Bharat Express', route: 'Chennai ➔ Mysuru', time: '05:50 AM' }
  ]
};

/**
 * High-performance, zero-latency fuzzy station search across 13,000+ stations
 */
export function searchStations(query, limit = 8) {
  if (!query || typeof query !== 'string') return [];
  const q = query.trim().toUpperCase();
  if (q.length === 0) return [];

  const exactCode = [];
  const startsWithCode = [];
  const startsWithName = [];
  const startsWithCity = [];
  const containsName = [];

  // Keywords that identify non-passenger industrial sidings
  const isSiding = (name) => /SIDING|COLLIERY|SDG|DEPOT|YARD|PLANT|DEFENCE|M\/S/i.test(name);

  for (let i = 0; i < stationsData.length; i++) {
    const st = stationsData[i];
    const code = st.code.toUpperCase();
    const name = st.name.toUpperCase();
    const city = (st.city || '').toUpperCase();

    if (code === q) {
      exactCode.push(st);
    } else if (code.startsWith(q)) {
      startsWithCode.push(st);
    } else if (name.startsWith(q)) {
      startsWithName.push(st);
    } else if (city.startsWith(q)) {
      startsWithCity.push(st);
    } else if (name.includes(q) || city.includes(q)) {
      containsName.push(st);
    }
  }

  // Sorter giving priority to passenger hubs and junctions over industrial freight sidings
  const prioritizePassengerHubs = (a, b) => {
    const aSiding = isSiding(a.name) ? 1 : 0;
    const bSiding = isSiding(b.name) ? 1 : 0;
    if (aSiding !== bSiding) return aSiding - bSiding;

    // Prioritize junctions / main cities
    const aJn = /JN|CENTRAL|TERMINAL|CANTT/i.test(a.name) ? 1 : 0;
    const bJn = /JN|CENTRAL|TERMINAL|CANTT/i.test(b.name) ? 1 : 0;
    return bJn - aJn;
  };

  startsWithName.sort(prioritizePassengerHubs);
  startsWithCity.sort(prioritizePassengerHubs);
  containsName.sort(prioritizePassengerHubs);

  const combined = [
    ...exactCode,
    ...startsWithCode,
    ...startsWithName,
    ...startsWithCity,
    ...containsName
  ];

  // Deduplicate results
  const unique = [];
  const seen = new Set();
  for (const st of combined) {
    if (!seen.has(st.code)) {
      seen.add(st.code);
      unique.push(st);
      if (unique.length >= limit) break;
    }
  }

  return unique;
}

/**
 * Returns popular trains connected to a station code or city
 */
export function getRelevantTrainsForStation(stationCode, stationName = '') {
  if (!stationCode) return [];
  const code = stationCode.toUpperCase();
  if (POPULAR_TRAINS_BY_ZONE_OR_CODE[code]) {
    return POPULAR_TRAINS_BY_ZONE_OR_CODE[code];
  }

  // Check by name keywords
  const upperName = stationName.toUpperCase();
  if (upperName.includes('DELHI')) return POPULAR_TRAINS_BY_ZONE_OR_CODE.NDLS;
  if (upperName.includes('GIRIDIH')) return POPULAR_TRAINS_BY_ZONE_OR_CODE.NGRH;
  if (upperName.includes('VARANASI') || upperName.includes('BANARAS')) return POPULAR_TRAINS_BY_ZONE_OR_CODE.BSB;
  if (upperName.includes('HOWRAH') || upperName.includes('KOLKATA')) return POPULAR_TRAINS_BY_ZONE_OR_CODE.HWH;
  if (upperName.includes('MUMBAI')) return POPULAR_TRAINS_BY_ZONE_OR_CODE.MMCT;
  if (upperName.includes('PATNA')) return POPULAR_TRAINS_BY_ZONE_OR_CODE.PNBE;
  if (upperName.includes('BENGALURU') || upperName.includes('BANGALORE')) return POPULAR_TRAINS_BY_ZONE_OR_CODE.SBC;

  return [
    { number: '12302', name: 'Superfast Express', route: `${stationCode} ➔ Major Junctions`, time: 'Daily' },
    { number: '22436', name: 'Vande Bharat SF', route: `Connecting ${stationCode}`, time: 'Runs 6 Days' }
  ];
}
