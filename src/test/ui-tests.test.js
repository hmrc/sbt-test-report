const puppeteer = require('puppeteer');
const {describe, beforeEach, beforeAll, afterAll, expect, it} = require('@jest/globals');

describe('Accessibility Report', () => {
    let page;
    let browser;

    beforeAll(async () => {
        browser = await puppeteer.launch({
            headless: false
        });
    });

    beforeEach(async () => {
        page = await browser.newPage();
        await page.goto('http://localhost:3000/');
    });

    afterAll(async () => {
        await browser.close();
    })

    /*
        Search:
        Not applied
        Applied with text found
        Applied with text not found

        Checkbox:
        Not applied
        Applied with impact displayed
        Applied with impact not displayed

        Clear:
        Clear on click

        URL:
        Permalink - display issue only
    */

    describe('On initial page load', () => {
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
            expect(violations.length).toBe(4);
            expect(violations).toEqual(['critical', 'critical', 'moderate', 'info']);
        });
    });

    const searchFor = async (term) => {
        const searchInput = await page.$('#search');
        searchInput.type(term);
        await page.waitForTimeout(1000);
    }
    const getVisibleViolations = async () => {
        return await page.$$eval('li[data-hash]:not(.hidden)', els => els.map(el => el.getAttribute('data-impact')));
    }

    describe('Search', () => {
        it('should show 1 matching violation"', async () => {
            await searchFor('ARIA');

            const visibleViolations = await getVisibleViolations();

            const issueCount = await page.$eval('#issueCount', el => el.textContent);
            expect(issueCount).toBe("Displaying 1 of 4 issues identified.")

            expect(visibleViolations.length).toEqual(1);
            expect(visibleViolations).toEqual([ 'moderate' ]);
        });

        it('should show 0 violations when nothing matches"', async () => {
            await searchFor('XYZ');

            await page.waitForFunction(
                (expectedText) => {
                    const element = document.querySelector('#issueCount');
                    return element && element.textContent === expectedText;
                },
                {},
                "No issues identified.",
                {timeout: 2000}
            );

            const violations = await page.$$eval('li[data-hash]:not(.hidden)', els => els.map(el => el.getAttribute('data-impact')));
            expect(violations.length).toBe(0);
        });
    });

    describe('Checkbox filtering', () => {
        it('should show only critical violations when critical impact checkbox is selected"', async () => {
            const impactCritical = await page.$('#impact-critical');
            impactCritical.click();

            await page.waitForFunction(
                (expectedText) => {
                    const element = document.querySelector('#issueCount');
                    return element && element.textContent === expectedText;
                },
                {},
                "Displaying 2 of 4 issues identified.",
                {timeout: 2000}
            );

            const violations = await page.$$eval('li[data-hash]:not(.hidden)', els => els.map(el => el.getAttribute('data-impact')));
            expect(violations.length).toBe(2);
            expect(violations).toEqual(['critical', 'critical']);
        });
    });

   /* describe('User journey', () => {
        it('xyzc"', async () => {
            const searchInput = await page.$('#search');
            searchInput.type('XYZ');

            await page.waitForFunction(
                (expectedText) => {
                    const element = document.querySelector('#issueCount');
                    return element && element.textContent === expectedText;
                },
                {},
                "No issues identified.",
                {timeout: 2000}
            );

            const impactCritical = await page.$('#impact-critical');
            impactCritical.click();

            const clearBtn = await page.$('#clear');
            clearBtn.click();

            await page.waitForFunction(
                (expectedText) => {
                    const element = document.querySelector('#issueCount');
                    return element && element.textContent === expectedText;
                },
                {},
                "Displaying 4 of 4 issues identified.",
                {timeout: 2000}
            );

            const searchInputValue = await page.$eval('#search', el => el.value);
            expect(searchInputValue).toBe('');
        });
    });*/
});