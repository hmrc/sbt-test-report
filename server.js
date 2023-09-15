const { spawn } = require('child_process');
const fs = require('fs');
const path = require("path");

const originalDataJsPath = path.join(__dirname, '/src/main/resources/assets/data.js');
const backupDataJsPath = path.join(__dirname, '/src/main/resources/assets/data.js.bak'); // Backup file name

// Define the command to run lite-server
spawn('npx', ['lite-server']);

process.on('SIGINT', () => {
    process.exit();
});

process.on('SIGTERM', () => {
    process.exit();
});

process.on('exit', () => {
    if (fs.existsSync(backupDataJsPath)) {
        fs.copyFileSync(backupDataJsPath, originalDataJsPath);
        fs.unlinkSync(backupDataJsPath);
        console.log('Restored data.js to its original state.');
    }
});

