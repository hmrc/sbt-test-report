const injectJsonData = require('../../prepare');

const waitTime = 1000;

const interceptReportMetaData = async (reportMetaDataJson, page) => {
    await injectJsonData(JSON.stringify(reportMetaDataJson));
    await page.waitForTimeout(waitTime);
}

const interceptAxeAssessedPages = async (axeAssessedPagesJson, page) => {
    await injectJsonData(undefined, JSON.stringify(axeAssessedPagesJson));
    await page.waitForTimeout(waitTime);
}

const resetData = async (page) => {
    await injectJsonData();
    await page.waitForTimeout(waitTime);
}

module.exports = {resetData, interceptReportMetaData, interceptAxeAssessedPages};
