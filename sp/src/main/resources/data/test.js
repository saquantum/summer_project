const fs = require('fs');
const https = require('https');
const path = require('path');

function httpsGetJson(url) {
  return new Promise((resolve, reject) => {
    https.get(url, res => {
      let data = '';
      res.on('data', chunk => data += chunk);
      res.on('end', () => {
        try {
          const json = JSON.parse(data);
          resolve(json);
        } catch (e) {
          reject(e);
        }
      });
    }).on('error', reject);
  });
}

async function getPostcodeInfo(postcode) {
  const url = `https://api.postcodes.io/postcodes/${encodeURIComponent(postcode)}`;
  const json = await httpsGetJson(url);
  if (json.status === 200 && json.result) {
    return json.result;
  } else {
    throw new Error('Postcode not found');
  }
}

async function getRandomPostcode() {
  const url = 'https://api.postcodes.io/random/postcodes';
  const json = await httpsGetJson(url);
  if (json.status === 200 && json.result && json.result.postcode) {
    return json.result.postcode;
  } else {
    throw new Error('Failed to get random postcode');
  }
}

async function enrichAddress(address) {
  try {
    let info = null;
    try {
      info = await getPostcodeInfo(address.postcode);
    } catch {
      console.log(`Postcode ${address.postcode} invalid, fetching random postcode...`);
      const randomPostcode = await getRandomPostcode();
      console.log(`Using random postcode: ${randomPostcode}`);
      info = await getPostcodeInfo(randomPostcode);
      address.postcode = randomPostcode;
    }

    return {
      postcodeRegion: info.region || null,
      country: info.country || address.country,
      city: info.admin_district || address.city,
      postcodeAdminDistrict: info.admin_ward || null,
      street: address.street,
      postcodeCountry: info.country || null,
      postcode: info.postcode || address.postcode
    };
  } catch (error) {
    console.error(`Error enriching address for postcode ${address.postcode}:`, error.message);
    return address;
  }
}

async function enrichUsers(inputFile, outputFile) {
  try {
    const raw = fs.readFileSync(inputFile, 'utf-8');
    const users = JSON.parse(raw);

    for (const user of users) {
      if (user.address && user.address.postcode) {
        user.address = await enrichAddress(user.address);
      }
    }

    fs.writeFileSync(outputFile, JSON.stringify(users, null, 2), 'utf-8');
    console.log(`Enriched user data saved to ${outputFile}`);
  } catch (error) {
    console.error('Error processing users:', error);
  }
}

// 执行示例
const inputFile = path.resolve(__dirname, 'users.json');
const outputFile = path.resolve(__dirname, 'users_enriched.json');

enrichUsers(inputFile, outputFile);
