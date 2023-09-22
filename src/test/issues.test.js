/**
 * @jest-environment jsdom
 */

const {test, describe, beforeEach, expect} = require('@jest/globals');
const {createGroupedIssues, sortByImpact} = require("../main/resources/assets/js/issues.mjs");


const MD5 = require('../main/resources/assets/lib/md5.min.js');
jest.mock('../main/resources/assets/lib/md5.min.js', () => ({
    generate: jest.fn(() => "thisisamdghash")
}));

const removeSearchFromUrlParams = require("../main/resources/assets/js/browserHelper.mjs");
jest.mock('../main/resources/assets/js/browserHelper.mjs', () => jest.fn(() => new URL('http://localhost-with-no-search-param/index.html')));

const createViolation = (id, impact, help, helpUrl, html) => {
    return {
        id,
        impact,
        help,
        helpUrl,
        nodes: [
            {html}
        ]
    }
}

const createPage = (url, testEngineVersion, violations) => {
    return {
        url: url,
        testEngine: {
            version: testEngineVersion
        },
        violations
    };
}

const sameId = 'region';
const sameHtml = '<a href="#main-content" class="govuk-skip-link" data-module="govuk-skip-link">Skip to main content</a>';
const moderateViolation1 = createViolation(
    sameId,
    'moderate',
    'All page content should be contained by landmarks',
    'http://somehelpurl',
    sameHtml);

const moderateViolation2 = createViolation(
    sameId,
    'critical',
    'All page content should be contained by landmarks',
    'http://somehelpurl',
    sameHtml);

const criticalViolation3 = createViolation(
    'backlink',
    'critical',
    'All page content should be contained by landmarks',
    'http://somehelpurl',
    sameHtml);

const infoViolation4 = createViolation(
    'href',
    'info',
    'Another info help description',
    'http://somehelpurl',
    sameHtml);

const seriousViolation4 = createViolation(
    'link',
    'serious',
    'A serious violation',
    'http://somehelpurl',
    sameHtml);

describe('createGroupedIssues', () => {
    test('maps raw violations to an aggregate issue view model, with a permalink for each issue', () => {
        const axePage1 = createPage('http://axeresult-a', '4.7.2', [moderateViolation1]);

        const groupedIssues = createGroupedIssues([axePage1], MD5, removeSearchFromUrlParams);

        expect(groupedIssues.length).toBe(1)
        expect(groupedIssues).toEqual([{
            id: 'region',
            impact: 'moderate',
            dataHash: 'thisisamdghash',
            help: 'All page content should be contained by landmarks',
            helpUrl: 'http://somehelpurl',
            html: ['<a href="#main-content" class="govuk-skip-link" data-module="govuk-skip-link">Skip to main content</a>'],
            affects: ['http://axeresult-a'],
            testEngineVersion: '4.7.2',
            permaLink: 'http://localhost-with-no-search-param/index.html?search=thisisamdghash'
        }]);
    });

    test('groups violations when id and html are the same but pageUrl differs', () => {
        const axePage1 = createPage('http://axeresult-a', '4.7.2', [moderateViolation1]);
        const axePage2 = createPage('http://axeresult-b', '4.7.2', [moderateViolation2]);

        const groupedIssues = createGroupedIssues([axePage1, axePage2], MD5, removeSearchFromUrlParams);

        expect(groupedIssues.length).toBe(1)
        expect(groupedIssues[0].affects).toEqual(['http://axeresult-a', 'http://axeresult-b'])
    });

    test('groups violations when id, html and pageUrl are the same', () => {
        const axePage1 = createPage('http://axeresult-a', '4.7.2', [moderateViolation1]);
        const axePage2 = createPage('http://axeresult-a', '4.7.2', [moderateViolation2]);

        const groupedIssues = createGroupedIssues([axePage1, axePage2], MD5, removeSearchFromUrlParams);

        expect(groupedIssues.length).toBe(1)
        expect(groupedIssues[0].affects).toEqual(['http://axeresult-a'])
    });

    test('does not group when id and html differ', () => {
        const axePage1 = createPage('http://axeresult-a', '4.7.2', [moderateViolation1]);
        const axePage2 = createPage('http://axeresult-b', '4.7.2', [criticalViolation3]);

        const groupedIssues = createGroupedIssues([axePage1, axePage2], MD5, removeSearchFromUrlParams);

        expect(groupedIssues.length).toBe(2)
        expect(groupedIssues[0].affects).toEqual(['http://axeresult-a']);
        expect(groupedIssues[1].affects).toEqual(['http://axeresult-b']);
    });
});

describe('Sort by impact', () => {
    test('Sort issues in order: critical > serious > moderate > info', () => {
        const moderateAxePage   = createPage('http://axeresult-a', '4.7.2', [moderateViolation1]);
        const criticalAxePage   = createPage('http://axeresult-b', '4.7.2', [criticalViolation3]);
        const infoAxePage       = createPage('http://axeresult-b', '4.7.2', [infoViolation4]);
        const seriousAxePage    = createPage('http://axeresult-b', '4.7.2', [seriousViolation4]);

        const groupedIssues = createGroupedIssues([moderateAxePage, criticalAxePage, infoAxePage, seriousAxePage], MD5, removeSearchFromUrlParams);
        sortByImpact(groupedIssues);
        const impacts = groupedIssues.map(issue => issue.impact);

        expect(impacts).toEqual(['critical','serious','moderate','info'])
    })
});