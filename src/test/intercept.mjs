import {injectJsonData} from '../../prepare.js';

const liteServerHotReloadWaitTime = 1000; // delay needed for lite-server to hot reload

export const interceptReportMetaData = async (reportMetaDataJson, page) => {
    await injectJsonData(JSON.stringify(reportMetaDataJson));
    await page.waitForTimeout(liteServerHotReloadWaitTime);
}

export const interceptAxeAssessedPages = async (axeAssessedPagesJson, page) => {
    await injectJsonData(undefined, JSON.stringify(axeAssessedPagesJson));
    await page.waitForTimeout(liteServerHotReloadWaitTime);
}

export const resetData = async (page) => {
    await injectJsonData();
    await page.waitForTimeout(liteServerHotReloadWaitTime);
}
