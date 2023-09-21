/**
 * @jest-environment jsdom
 */

const {test, describe, beforeEach, expect} = require('@jest/globals');
const createGroupedIssues = require("../main/resources/assets/js/createGroupedIssues.mjs");


const MD5 = require('../main/resources/assets/lib/md5.min.js');
jest.mock('../main/resources/assets/lib/md5.min.js', () => ({
    generate: jest.fn(() => "thisisamdghash")
}));

const removeSearchFromUrlParams = require("../main/resources/assets/js/browserHelper.mjs");
jest.mock('../main/resources/assets/js/browserHelper.mjs', () => jest.fn(() => new URL('http://localhost')));

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

const moderateViolation3 = createViolation(
    'backlink',
    'critical',
    'All page content should be contained by landmarks',
    'http://somehelpurl',
    sameHtml);

describe('with axe pages grouped by id, html and pageUrl', () => {
    test('grouped when id and html are the same but pageUrl differs', () => {
        const axePage1 = createPage('http://axeresult-a', '4.7.2', [moderateViolation1]);
        const axePage2 = createPage('http://axeresult-b', '4.7.2', [moderateViolation2]);

        const groupedIssues = createGroupedIssues([axePage1, axePage2], MD5, removeSearchFromUrlParams);

        expect(groupedIssues.length).toBe(1)
        expect(groupedIssues[0].affects).toEqual(['http://axeresult-a', 'http://axeresult-b'])
    });

    test('grouped when id, html and pageUrl are the same', () => {
        const axePage1 = createPage('http://axeresult-a', '4.7.2', [moderateViolation1]);
        const axePage2 = createPage('http://axeresult-a', '4.7.2', [moderateViolation2]);

        const groupedIssues = createGroupedIssues([axePage1, axePage2], MD5, removeSearchFromUrlParams);

        expect(groupedIssues.length).toBe(1)
        expect(groupedIssues[0].affects).toEqual(['http://axeresult-a'])
    });

    test('not grouped when id and html differ', () => {
        const axePage1 = createPage('http://axeresult-a', '4.7.2', [moderateViolation1]);
        const axePage2 = createPage('http://axeresult-b', '4.7.2', [moderateViolation3]);

        const groupedIssues = createGroupedIssues([axePage1, axePage2], MD5, removeSearchFromUrlParams);

        expect(groupedIssues.length).toBe(2)
        expect(groupedIssues[0].affects).toEqual(['http://axeresult-a']);
        expect(groupedIssues[1].affects).toEqual(['http://axeresult-b']);
    });
});