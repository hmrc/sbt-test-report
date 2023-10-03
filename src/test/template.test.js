/**
 * @jest-environment jsdom
 */

import fs from 'fs';
import path from 'path';
import {beforeAll, describe, expect, test} from '@jest/globals';

import {fileURLToPath} from 'url';
import {populateTemplate} from "../main/resources/assets/js/template.js";

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const htmlFile = __dirname + '/../main/resources/index.html';
let templateHtml;

beforeAll(() => {
    document.body.innerHTML = fs.readFileSync(htmlFile, 'utf8');
    templateHtml = document.body.querySelector('template');
});

describe('populateTemplate', () => {
    test('create a clone of the given template and populate from the given issue', () => {
        const issue = {
            id: 'region',
            impact: 'moderate',
            dataHash: 'eb3026fa95fda35bce525495de908954',
            help: 'All page content should be contained by landmarks',
            helpUrl: 'http://somehelpurl',
            html: ['<a href="#main-content" class="govuk-skip-link" data-module="govuk-skip-link">Skip to main content</a>'],
            affects: ['http://axeresult-a', 'http://axeresult-b'],
            testEngineVersion: '4.7.2',
            permaLink: 'http://localhost/?search=eb3026fa95fda35bce525495de908954'
        }

        const originalTemplateHtml = templateHtml;

        const populatedIssueElement = populateTemplate(templateHtml, issue);

        expect(templateHtml).toBe(originalTemplateHtml);

        const liElement = populatedIssueElement.querySelector('li');
        expect(liElement.getAttribute('data-impact')).toBe('moderate');
        expect(liElement.getAttribute('data-hash')).toBe('eb3026fa95fda35bce525495de908954');

        const helpUrl = populatedIssueElement.querySelector('#helpUrl');
        expect(helpUrl.getAttribute('href')).toBe('http://somehelpurl');
        expect(helpUrl.textContent).toBe('region');

        const impactTag = populatedIssueElement.querySelector('#impactTag');
        expect(impactTag.getAttribute('data-tag')).toBe('moderate');
        expect(impactTag.textContent).toBe('moderate');

        const testEngineVersion = populatedIssueElement.querySelector('#testEngineVersion');
        expect(testEngineVersion.getAttribute('data-tag')).toBe('version');
        expect(testEngineVersion.textContent).toBe('4.7.2');

        const violationHelp = populatedIssueElement.querySelector('#violationHelp');
        expect(violationHelp.textContent).toBe('All page content should be contained by landmarks');

        const htmlSnippet = populatedIssueElement.querySelector('#htmlSnippet');
        expect(htmlSnippet.textContent).toBe('<a href="#main-content" class="govuk-skip-link" data-module="govuk-skip-link">Skip to main content</a>');

        const urlViolationSummary = populatedIssueElement.querySelector('#urlViolationSummary');
        expect(urlViolationSummary.textContent).toBe('2 URLs');

        const urlViolations = populatedIssueElement.querySelector('#urlViolations');
        const urlListItems = urlViolations.querySelectorAll('li');
        expect(urlListItems.length).toBe(2);
        expect(urlListItems[0].textContent).toBe('http://axeresult-a');
        expect(urlListItems[1].textContent).toBe('http://axeresult-b');

        const violationPermaLink = populatedIssueElement.querySelector('#violationPermaLink');
        expect(violationPermaLink.getAttribute('href')).toBe('http://localhost/?search=eb3026fa95fda35bce525495de908954');
        expect(violationPermaLink.textContent).toBe('http://localhost/?search=eb3026fa95fda35bce525495de908954');
    })
})