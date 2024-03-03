const fs = require('fs');
const path = require('path');

function generateIntervalData(date, intervalCount = 48) {
    // Generates a string of interval data for a day, assuming 30-minute intervals
    let data = [];
    for (let i = 0; i < intervalCount; i++) {
        // Generate random usage values; adjust the range as needed
        const usage = (Math.random() * (20.0 - 0.5) + 0.5).toFixed(2);
        data.push(`${usage}`);
    }
    data.push('A');
    data.push('');
    data.push('');
    data.push('');
    return data.join(',');
}

function writeNEM12File(filename, nmiCount = 1000, days = 28) {
    // Creates a writable stream for the output file
    const outputStream = fs.createWriteStream(filename, { encoding: 'utf8' });

    // Write the file header
    outputStream.write('100,20230101,NEM12,XXXX\n');

    let startDate = new Date(2023, 0, 1);

    for (let nmi = 1; nmi <= nmiCount; nmi++) {
        // Write NMI data details record for each NMI
        outputStream.write(`200,${nmi.toString().padStart(10, '0')},E1,,,,,kWh,30,\n`);
        for (let day = 0; day < days; day++) {

            // Assuming a simple date format YYYYMMDD for demonstration; adjust as needed
            let currentDate = new Date(startDate.getTime());
            currentDate.setDate(currentDate.getDate() + day);

            const year = currentDate.getFullYear();
            const month = String(currentDate.getMonth() + 1).padStart(2, '0'); // JS months are 0-indexed
            const d = String(currentDate.getDate()).padStart(2, '0');
            const dateStr = `${year}${month}${d}`

            const intervalData = generateIntervalData(dateStr);
            // Write interval data records
            outputStream.write(`300,${dateStr},${intervalData}\n`);
        }
    }

    // Write the file footer and close the stream
    outputStream.write('900\n', () => {
        outputStream.end();
    });

    outputStream.on('finish', () => {
        console.log('NEM12 file has been generated.');
    });
}

// Specify the filename and parameters for the size of the file
const filename = path.join(__dirname, 'large_nem12_file.csv');
const nmiCount = 1000; // Number of NMIs to include
const days = 365; // Number of days of interval data for each NMI

writeNEM12File(filename, nmiCount, days);
