const fs = require('fs');
const path = require("path");

const reportMetaData = require("./src/test/resources/report_meta_data.json");
const axeAssessedPages = require("./src/test/resources/axe_results.json");

const dataJsFile = __dirname + '/src/main/resources/assets/data.js';

const originalDataJsPath = path.join(__dirname, '/src/main/resources/assets/data.js');
const backupDataJsPath = path.join(__dirname, '/src/main/resources/assets/data.js.bak');

const injectJsonData = async (reportMetaDataJson, axeAssessedPagesJson) => {
    if(reportMetaDataJson === undefined) {
        reportMetaDataJson = JSON.stringify(reportMetaData);
    }

    if(axeAssessedPagesJson === undefined) {
        axeAssessedPagesJson = JSON.stringify(axeAssessedPages);
    }

    fs.copyFileSync(backupDataJsPath, originalDataJsPath);

    await fs.readFile(dataJsFile, 'utf8', async (err, data) => {
        if (err) {
            console.error('Error reading data.js:', err);
            return;
        }

        data = data
            .replace('INJECT_REPORT_METADATA', reportMetaDataJson)
            .replace('INJECT_AXE_VIOLATIONS', axeAssessedPagesJson);

        await fs.writeFile(dataJsFile, data, 'utf8', (err) => {
            if (err) {
                console.error('Error writing data.js:', err);
                return;
            }
        });
    });
}

const startServer = async () => {
    // Check if a backup of data.js exists, and if not, create one from the original
    if (!fs.existsSync(backupDataJsPath)) {
        fs.copyFileSync(originalDataJsPath, backupDataJsPath);

        await injectJsonData(JSON.stringify(reportMetaData), JSON.stringify(axeAssessedPages))
    }
}
startServer();

module.exports = injectJsonData;


