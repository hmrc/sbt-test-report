import fs from 'fs';
import path from 'path';
import { spawn } from 'child_process';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const originalDataJsPath = path.join(__dirname, '/src/main/resources/assets/data.js');
const backupDataJsPath = path.join(__dirname, '/src/main/resources/assets/data.js.bak');

// start the lite-server
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

