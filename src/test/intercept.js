const injectJsonData = require('../../prepare');

const liteServerHotReloadWaitTime = 1000; // delay needed for lite-server to hot reload

const interceptReportMetaData = async (reportMetaDataJson, page) => {
    await injectJsonData(JSON.stringify(reportMetaDataJson));
    await page.waitForTimeout(liteServerHotReloadWaitTime);
}

const interceptAxeAssessedPages = async (axeAssessedPagesJson, page) => {
    await injectJsonData(undefined, JSON.stringify(axeAssessedPagesJson));
    await page.waitForTimeout(liteServerHotReloadWaitTime);
}

const resetData = async (page) => {
    await injectJsonData();
    await page.waitForTimeout(liteServerHotReloadWaitTime);
}

module.exports = {resetData, interceptReportMetaData, interceptAxeAssessedPages};
