const admin = require('firebase-admin');
const fs = require('fs');
const csv = require('csv-parser');

const serviceAccount = require('./serviceAccountKey.json');
const csvFilePath = './vehicles_expanded.csv';

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();

async function uploadData() {
  const results = [];
  console.log("Reading CSV...");

  fs.createReadStream(csvFilePath)
    .pipe(csv())
    .on('data', (data) => results.push(data))
    .on('end', async () => {
      console.log(`Parsed ${results.length} records. Uploading in chunks...`);

      const initializedMakes = new Set();
      const initializedModels = new Set();
      const CHUNK_SIZE = 50; // Process 50 rows at a time

      for (let i = 0; i < results.length; i += CHUNK_SIZE) {
        const chunk = results.slice(i, i + CHUNK_SIZE);

        await Promise.all(chunk.map(async (row) => {
          const make = (row['make'] || "").trim();
          const model = (row['model'] || "").trim();
          const year = (row['year'] || "").trim();

          if (!make || !model || !year || make.toLowerCase() === 'make') return;

          const vehicleData = {
            make: make,
            model: model,
            year: parseInt(year) || 0,
            fuel_type: (row['fuel_type'] || "").trim(),
            transmission: (row['transmission'] || "").trim(),
            mpg_city: parseFloat(row['mpg_city'] || 0),
            mpg_highway: parseFloat(row['mpg_highway'] || 0),
            kpl_city: parseFloat(row['kpl_city'] || 0),
            kpl_highway: parseFloat(row['kpl_highway'] || 0)
          };

          const safeModelId = model.replace(/\//g, '-');
          const makeRef = db.collection('makes').doc(make);
          const modelRef = makeRef.collection('models').doc(safeModelId);
          // Using year + transmission to avoid overwriting different versions of the same bike
          const yearId = year + "_" + vehicleData.transmission.replace(/\s/g, '');
          const yearRef = modelRef.collection('years').doc(yearId);

          if (!initializedMakes.has(make)) {
              await makeRef.set({ name: make }, { merge: true });
              initializedMakes.add(make);
          }
          if (!initializedModels.has(make + safeModelId)) {
              await modelRef.set({ name: model }, { merge: true });
              initializedModels.add(make + safeModelId);
          }

          return yearRef.set(vehicleData);
        }));

        console.log(`Progress: ${Math.min(i + CHUNK_SIZE, results.length)} / ${results.length} vehicles uploaded.`);
      }

      console.log("\nFINISHED! All data is in Firestore.");
      process.exit();
    });
}

uploadData();