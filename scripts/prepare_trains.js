const fs = require('fs');
const path = require('path');

console.log('Reading train datasets...');
const sf = JSON.parse(fs.readFileSync('dataset_trains/SF-TRAINS.json', 'utf8'));
const exp = JSON.parse(fs.readFileSync('dataset_trains/EXP-TRAINS.json', 'utf8'));
const allTrains = [...sf, ...exp];

console.log(`Processing ${allTrains.length} trains...`);

function cleanStationName(raw) {
  if (!raw) return { name: '', code: '' };
  const parts = raw.split(' - ');
  if (parts.length >= 2) {
    return {
      name: parts[0].trim(),
      code: parts[parts.length - 1].trim().toUpperCase()
    };
  }
  return { name: raw.trim(), code: raw.trim().toUpperCase() };
}

function determineTrainType(num, name) {
  const uName = (name || '').toUpperCase();
  if (uName.includes('VANDE BHARAT')) return 'VANDE_BHARAT';
  if (uName.includes('RAJDHANI')) return 'RAJDHANI';
  if (uName.includes('SHATABDI')) return 'SHATABDI';
  if (uName.includes('DURONTO')) return 'DURONTO';
  if (uName.includes('TEJAS')) return 'TEJAS';
  if (uName.includes('GARIB RATH')) return 'GARIB_RATH';
  if (uName.includes('JAN SHATABDI')) return 'JAN_SHATABDI';
  if (num.startsWith('12') || num.startsWith('22') || uName.includes('SF')) return 'SUPERFAST';
  return 'EXPRESS';
}

const cleanTrains = [];
const seenNumbers = new Set();

for (const t of allTrains) {
  const num = (t.trainNumber || '').trim();
  if (!num || seenNumbers.has(num)) continue;
  seenNumbers.add(num);

  const stops = (t.trainRoute || []).map(r => {
    const { name, code } = cleanStationName(r.stationName);
    return {
      c: code,
      n: name,
      arr: r.arrives === 'Source' ? null : r.arrives,
      dep: r.departs === 'Destination' ? null : r.departs,
      km: parseInt(r.distance, 10) || 0,
      d: parseInt(r.day, 10) || 1
    };
  }).filter(s => s.c && s.c.length <= 10);

  if (stops.length < 2) continue;

  cleanTrains.push({
    num,
    name: t.trainName || `Train ${num}`,
    type: determineTrainType(num, t.trainName),
    stops
  });
}

console.log(`Successfully cleaned ${cleanTrains.length} unique trains with complete stoppage routes.`);

// Output to frontend lazy data directory
const frontendDataDir = path.join(__dirname, '..', 'frontend', 'src', 'data');
if (!fs.existsSync(frontendDataDir)) {
  fs.mkdirSync(frontendDataDir, { recursive: true });
}

const outputPath = path.join(frontendDataDir, 'train_routes_data.json');
fs.writeFileSync(outputPath, JSON.stringify(cleanTrains));

const sizeMb = (fs.statSync(outputPath).size / (1024 * 1024)).toFixed(2);
console.log(`Wrote optimized train route dataset to: ${outputPath} (${sizeMb} MB)`);
