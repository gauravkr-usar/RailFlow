const fs = require('fs');
const path = require('path');

const rawStations = JSON.parse(fs.readFileSync('list_of_stations.json', 'utf8'));

const zoneStateMap = {
  'NR': { zone: 'Northern Railway', state: 'Delhi / Punjab / UP / Haryana', city: 'Delhi' },
  'CR': { zone: 'Central Railway', state: 'Maharashtra / MP', city: 'Mumbai' },
  'WR': { zone: 'Western Railway', state: 'Gujarat / Maharashtra / Rajasthan', city: 'Mumbai' },
  'ER': { zone: 'Eastern Railway', state: 'West Bengal / Bihar / Jharkhand', city: 'Kolkata' },
  'EC': { zone: 'East Central Railway', state: 'Bihar / Jharkhand / UP', city: 'Hajipur' },
  'SR': { zone: 'Southern Railway', state: 'Tamil Nadu / Kerala', city: 'Chennai' },
  'SC': { zone: 'South Central Railway', state: 'Telangana / Andhra Pradesh', city: 'Secunderabad' },
  'SCR': { zone: 'South Central Railway', state: 'Telangana / Andhra Pradesh', city: 'Secunderabad' },
  'SE': { zone: 'South Eastern Railway', state: 'West Bengal / Jharkhand / Odisha', city: 'Kolkata' },
  'SEC': { zone: 'South East Central Railway', state: 'Chhattisgarh / MP', city: 'Bilaspur' },
  'SW': { zone: 'South Western Railway', state: 'Karnataka', city: 'Bengaluru' },
  'SWR': { zone: 'South Western Railway', state: 'Karnataka', city: 'Bengaluru' },
  'NW': { zone: 'North Western Railway', state: 'Rajasthan / Gujarat', city: 'Jaipur' },
  'NWR': { zone: 'North Western Railway', state: 'Rajasthan / Gujarat', city: 'Jaipur' },
  'NE': { zone: 'North Eastern Railway', state: 'Uttar Pradesh / Uttarakhand', city: 'Gorakhpur' },
  'NER': { zone: 'North Eastern Railway', state: 'Uttar Pradesh / Uttarakhand', city: 'Gorakhpur' },
  'NF': { zone: 'Northeast Frontier Railway', state: 'Assam / West Bengal / NE', city: 'Guwahati' },
  'NFR': { zone: 'Northeast Frontier Railway', state: 'Assam / West Bengal / NE', city: 'Guwahati' },
  'NC': { zone: 'North Central Railway', state: 'Uttar Pradesh / MP', city: 'Prayagraj' },
  'NCR': { zone: 'North Central Railway', state: 'Uttar Pradesh / MP', city: 'Prayagraj' },
  'WC': { zone: 'West Central Railway', state: 'Madhya Pradesh / Rajasthan', city: 'Jabalpur' },
  'WCR': { zone: 'West Central Railway', state: 'Madhya Pradesh / Rajasthan', city: 'Jabalpur' },
  'ECo': { zone: 'East Coast Railway', state: 'Odisha / Andhra Pradesh', city: 'Bhubaneswar' },
  'KR': { zone: 'Konkan Railway', state: 'Goa / Maharashtra / Karnataka', city: 'Navi Mumbai' }
};

const cityDetections = [
  { keyword: 'DELHI', city: 'Delhi', state: 'Delhi' },
  { keyword: 'GIRIDIH', city: 'Giridih', state: 'Jharkhand' },
  { keyword: 'HOWRAH', city: 'Howrah', state: 'West Bengal' },
  { keyword: 'KOLKATA', city: 'Kolkata', state: 'West Bengal' },
  { keyword: 'VARANASI', city: 'Varanasi', state: 'Uttar Pradesh' },
  { keyword: 'BANARAS', city: 'Varanasi', state: 'Uttar Pradesh' },
  { keyword: 'KANPUR', city: 'Kanpur', state: 'Uttar Pradesh' },
  { keyword: 'LUCKNOW', city: 'Lucknow', state: 'Uttar Pradesh' },
  { keyword: 'PRAYAGRAJ', city: 'Prayagraj', state: 'Uttar Pradesh' },
  { keyword: 'ALLAHABAD', city: 'Prayagraj', state: 'Uttar Pradesh' },
  { keyword: 'MUMBAI', city: 'Mumbai', state: 'Maharashtra' },
  { keyword: 'BOMBAY', city: 'Mumbai', state: 'Maharashtra' },
  { keyword: 'PUNE', city: 'Pune', state: 'Maharashtra' },
  { keyword: 'NAGPUR', city: 'Nagpur', state: 'Maharashtra' },
  { keyword: 'PATNA', city: 'Patna', state: 'Bihar' },
  { keyword: 'GAYA', city: 'Gaya', state: 'Bihar' },
  { keyword: 'DHANBAD', city: 'Dhanbad', state: 'Jharkhand' },
  { keyword: 'RANCHI', city: 'Ranchi', state: 'Jharkhand' },
  { keyword: 'TATANAGAR', city: 'Jamshedpur', state: 'Jharkhand' },
  { keyword: 'JAMSHEDPUR', city: 'Jamshedpur', state: 'Jharkhand' },
  { keyword: 'BENGALURU', city: 'Bengaluru', state: 'Karnataka' },
  { keyword: 'BANGALORE', city: 'Bengaluru', state: 'Karnataka' },
  { keyword: 'CHENNAI', city: 'Chennai', state: 'Tamil Nadu' },
  { keyword: 'MADRAS', city: 'Chennai', state: 'Tamil Nadu' },
  { keyword: 'HYDERABAD', city: 'Hyderabad', state: 'Telangana' },
  { keyword: 'SECUNDERABAD', city: 'Secunderabad', state: 'Telangana' },
  { keyword: 'AHMEDABAD', city: 'Ahmedabad', state: 'Gujarat' },
  { keyword: 'SURAT', city: 'Surat', state: 'Gujarat' },
  { keyword: 'VADODARA', city: 'Vadodara', state: 'Gujarat' },
  { keyword: 'JAIPUR', city: 'Jaipur', state: 'Rajasthan' },
  { keyword: 'JODHPUR', city: 'Jodhpur', state: 'Rajasthan' },
  { keyword: 'BHOPAL', city: 'Bhopal', state: 'Madhya Pradesh' },
  { keyword: 'INDORE', city: 'Indore', state: 'Madhya Pradesh' },
  { keyword: 'GWALIOR', city: 'Gwalior', state: 'Madhya Pradesh' },
  { keyword: 'JABALPUR', city: 'Jabalpur', state: 'Madhya Pradesh' },
  { keyword: 'JALPAIGURI', city: 'Jalpaiguri', state: 'West Bengal' },
  { keyword: 'BONGAIGAON', city: 'Bongaigaon', state: 'Assam' },
  { keyword: 'GUWAHATI', city: 'Guwahati', state: 'Assam' },
  { keyword: 'SILIGURI', city: 'Siliguri', state: 'West Bengal' },
  { keyword: 'CHANDIGARH', city: 'Chandigarh', state: 'Chandigarh' },
  { keyword: 'AMRITSAR', city: 'Amritsar', state: 'Punjab' },
  { keyword: 'AGRA', city: 'Agra', state: 'Uttar Pradesh' },
  { keyword: 'GORAKHPUR', city: 'Gorakhpur', state: 'Uttar Pradesh' },
  { keyword: 'BHUBANESWAR', city: 'Bhubaneswar', state: 'Odisha' },
  { keyword: 'PURI', city: 'Puri', state: 'Odisha' },
  { keyword: 'CUTTACK', city: 'Cuttack', state: 'Odisha' },
  { keyword: 'DEHRADUN', city: 'Dehradun', state: 'Uttarakhand' },
  { keyword: 'HARIDWAR', city: 'Haridwar', state: 'Uttarakhand' },
  { keyword: 'SHIMLA', city: 'Shimla', state: 'Himachal Pradesh' },
  { keyword: 'JAMMU', city: 'Jammu', state: 'Jammu and Kashmir' },
  { keyword: 'SRINAGAR', city: 'Srinagar', state: 'Jammu and Kashmir' }
];

function toTitleCase(str) {
  if (!str) return '';
  return str.toLowerCase().split(/[\s-]+/).map(w => w.charAt(0).toUpperCase() + w.slice(1)).join(' ');
}

function escapeSql(str) {
  if (!str) return '';
  return str.replace(/'/g, "''").trim();
}

const seenCodes = new Set();
const cleanStations = [];

for (const s of rawStations) {
  const code = (s.station_code || '').trim().toUpperCase();
  let name = (s.station_name || '').trim();
  const region = (s.region_code || '').trim().toUpperCase().substring(0, 10);

  if (!code || code.length > 10 || seenCodes.has(code)) {
    continue;
  }
  seenCodes.add(code);

  let detectedCity = null;
  let detectedState = null;

  for (const det of cityDetections) {
    if (name.toUpperCase().includes(det.keyword)) {
      detectedCity = det.city;
      detectedState = det.state;
      break;
    }
  }

  if (!detectedCity) {
    if (name.includes(',')) {
      const parts = name.split(',');
      detectedCity = toTitleCase(parts[parts.length - 1].trim());
    } else {
      detectedCity = toTitleCase(name.split(' ')[0] || 'Unknown');
    }
  }

  if (!detectedState) {
    const zoneInfo = zoneStateMap[region];
    detectedState = zoneInfo ? zoneInfo.state : 'Indian Railways';
  }

  // Format station name cleanly
  const formattedName = toTitleCase(name.replace(/@/g, '').replace(/\s+/g, ' ').trim()).substring(0, 140);

  cleanStations.push({
    code,
    name: formattedName,
    city: detectedCity.substring(0, 90),
    state: detectedState.substring(0, 90),
    zone: region || 'IR'
  });
}

console.log(`Successfully processed ${cleanStations.length} unique stations.`);

// 1. Write frontend dataset
const frontendDir = path.join(__dirname, '..', 'frontend', 'src', 'data');
if (!fs.existsSync(frontendDir)) {
  fs.mkdirSync(frontendDir, { recursive: true });
}
const frontendJsonPath = path.join(frontendDir, 'all_stations.json');
fs.writeFileSync(frontendJsonPath, JSON.stringify(cleanStations));
console.log(`Wrote frontend dataset to: ${frontendJsonPath} (${(fs.statSync(frontendJsonPath).size / 1024).toFixed(1)} KB)`);

// 2. Write Flyway SQL Migration V3__import_all_stations.sql
const migrationDir = path.join(__dirname, '..', 'src', 'main', 'resources', 'db', 'migration');
const sqlPath = path.join(migrationDir, 'V3__import_all_stations.sql');

let sqlContent = `-- V3: Bulk Import of 13,000+ Indian Railway Stations\n`;
sqlContent += `-- Generated from authentic IRCTC station directory\n\n`;

const BATCH_SIZE = 250;
for (let i = 0; i < cleanStations.length; i += BATCH_SIZE) {
  const batch = cleanStations.slice(i, i + BATCH_SIZE);
  sqlContent += `INSERT INTO stations (station_code, station_name, city, state, zone_code, is_active, created_at, updated_at) VALUES\n`;
  const values = batch.map(st => {
    return `('${escapeSql(st.code)}', '${escapeSql(st.name)}', '${escapeSql(st.city)}', '${escapeSql(st.state)}', '${escapeSql(st.zone)}', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)`;
  }).join(',\n');
  sqlContent += values + `\nON CONFLICT (station_code) DO NOTHING;\n\n`;
}

fs.writeFileSync(sqlPath, sqlContent, 'utf8');
console.log(`Wrote SQL migration to: ${sqlPath} (${(fs.statSync(sqlPath).size / 1024).toFixed(1)} KB)`);
