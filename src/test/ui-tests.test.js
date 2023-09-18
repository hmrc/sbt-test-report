const puppeteer = require('puppeteer');
const {describe, beforeAll, afterAll, expect, it} = require('@jest/globals');

describe('Accessibility Report', () => {
    let page;
    let browser;

    beforeAll(async () => {
        browser = await puppeteer.launch({
            headless: false
        });
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
            expect(violations).toEqual([ 'critical', 'critical', 'moderate', 'info' ]);
        });
    });

});

/*
(async () => {
    // Launch the browser and open a new blank page
    const browser = await puppeteer.launch({
        headless: false
    });
    const page = await browser.newPage();

    // Navigate the page to a URL
    await page.goto('http://localhost:3000/');



    // Set screen size
    await page.setViewport({width: 1080, height: 1024});

    // Type into search box
    await page.type('.search-box__input', 'automate beyond recorder');

    // Wait and click on first result
    const searchResultSelector = '.search-box__link';
    await page.waitForSelector(searchResultSelector);
    await page.click(searchResultSelector);

    // Locate the full title with a unique string
    const textSelector = await page.waitForSelector(
        'text/Customize and automate'
    );
    const fullTitle = await textSelector?.evaluate(el => el.textContent);

    // Print the full title
    console.log('The title of this blog post is "%s".', fullTitle);
    await browser.close();
})();*/
