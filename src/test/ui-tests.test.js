import {jest} from '@jest/globals'
import {describe, beforeEach, beforeAll, afterAll, expect, it} from '@jest/globals';

import puppeteer from 'puppeteer';
import {resetData, interceptReportMetaData, interceptAxeAssessedPages} from './intercept.mjs';

jest.setTimeout(20000);

describe('Accessibility Report', () => {
    let page;
    let browser;
    const pageRefreshDelay = 1000;

    beforeAll(async () => {
        browser = await puppeteer.launch({
            headless: "new"
        });
    });

    beforeEach(async () => {
        page = await browser.newPage();
        await resetData(page);
        await page.goto('http://localhost:3000/').catch(() => {
            console.log("NOTE: Please start the server using `npm start` before running tests!");
            process.exit(1);
        });
    });

    afterAll(async () => {
        if(page) {
            await resetData(page);
        }
        await browser.close();
    })

    const searchFor = async (term) => {
        const searchInput = await page.$('#search');
        searchInput.type(term);
        await page.waitForTimeout(pageRefreshDelay);
    }

    const clickOn = async (selector) => {
        const element = await page.$(selector);
        element.click();
        await page.waitForTimeout(pageRefreshDelay);
    }

    const getVisibleViolations = async () => {
        return await page.$$eval('li[data-hash]:not(.hidden)', els => els.map(el => el.getAttribute('data-impact')));
    }

    describe('On initial page load', () => {

        it('header should display report meta data from jenkins"', async () => {
           const reportHeaderMetaData = await page.$eval('#metaDataHeader', el => el.textContent);
           expect(reportHeaderMetaData).toBe('Generated from build #42 (Chrome) of platform-example-ui-tests on 09-11-2023');
        });

        it('header should display report meta data from localhost"', async () => {
            await interceptReportMetaData({
                "projectName": "platform-example-ui-tests",
                "jenkinsBuildId": null,
                "jenkinsBuildUrl": null,
                "dateOfAssessment": "09-16-2023"
            }, page);

            const reportHeaderMetaData = await page.$eval('#metaDataHeader', el => el.textContent);
            expect(reportHeaderMetaData).toBe('Generated from platform-example-ui-tests on 09-16-2023');
        });

        it('should show 4 violations in the correct order with no filtering applied"', async () => {
            await expect(page.title()).resolves.toMatch('Accessibility assessment report');

            const issueCount = await page.$eval('#issueCount', el => el.textContent);
            expect(issueCount).toBe("Displaying 4 of 4 issues identified.");

            const searchInput = await page.$eval('#search', el => el.value);
            expect(searchInput).toBe('');

            const impactCritical = await page.$eval('#impact-critical', el => el.checked);
            const impactSerious = await page.$eval('#impact-serious', el => el.checked);
            const impactModerate = await page.$eval('#impact-moderate', el => el.checked);
            const impactInfo = await page.$eval('#impact-info', el => el.checked);
            expect(impactCritical).toBeFalsy();
            expect(impactSerious).toBeFalsy();
            expect(impactModerate).toBeFalsy();
            expect(impactInfo).toBeFalsy();

            const violations = await page.$$eval('li[data-impact]', els => els.map(el => el.getAttribute('data-impact')));
            expect(violations).toEqual(['critical', 'critical', 'moderate', 'info']);
        });
    });

    describe('Search', () => {
        it('should show 1 matching violation"', async () => {
            await searchFor('ARIA');

            const issueCount = await page.$eval('#issueCount', el => el.textContent);
            expect(issueCount).toBe("Displaying 1 of 4 issues identified.")

            const visibleViolations = await getVisibleViolations();
            expect(visibleViolations).toEqual([ 'moderate' ]);
        });

        it('should show 0 violations when nothing matches"', async () => {
            await searchFor('XYZ');

            const issueCount = await page.$eval('#issueCount', el => el.textContent);
            expect(issueCount).toBe("No issues identified.")

            const visibleViolations = await getVisibleViolations();
            expect(visibleViolations.length).toEqual(0);
        });
    });

    describe('Checkbox filtering', () => {
        it('should show only critical violations when critical impact checkbox is selected"', async () => {
            await clickOn('#impact-critical');

            const issueCount = await page.$eval('#issueCount', el => el.textContent);
            expect(issueCount).toBe("Displaying 2 of 4 issues identified.")

            const visibleViolations = await getVisibleViolations();
            expect(visibleViolations).toEqual(['critical', 'critical']);
        });
    });

    describe('User simulated interactions', () => {
        it('Permutation 1"', async () => {
            // 1. user clicks permalink - 1 result should be found
            await clickOn('#violationPermaLink');

            const permaLinkValue = await page.$eval('#violationPermaLink',
                    el => el.closest('li[data-hash]').getAttribute('data-hash'));

            const searchInputValue = await page.$eval('#search', el => el.value);
            expect(searchInputValue).toBe(permaLinkValue);

            let visibleViolations = await getVisibleViolations();
            expect(visibleViolations).toEqual(['critical']);

            // 2. user clicks on impact serious - 0 results found?
            await clickOn('#impact-serious');

            visibleViolations = await getVisibleViolations();
            expect(visibleViolations.length).toBe(0);

            // 3. user also clicks on moderate - x results found
            await clickOn('#impact-moderate');

            visibleViolations = await getVisibleViolations();
            expect(visibleViolations.length).toBe(0);

            // 4. user removes search term - x results found
            await page.$eval('#search', el => el.value = '');
            await page.focus('#search');
            await page.keyboard.press('Backspace');
            await page.waitForTimeout(pageRefreshDelay);

            visibleViolations = await getVisibleViolations();
            expect(visibleViolations).toEqual(['moderate']);

            // 5. user clicks clear - initial page results displayed
            await clickOn('#clear');

            visibleViolations = await getVisibleViolations();
            expect(visibleViolations).toEqual(['critical', 'critical', 'moderate', 'info']);
        });
    });
});