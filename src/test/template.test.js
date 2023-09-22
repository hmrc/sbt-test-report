/**
 * @jest-environment jsdom
 */

import fs from 'fs';
import path from 'path';
import {beforeAll, describe, expect, test} from '@jest/globals';

import {fileURLToPath} from 'url';
import {populateTemplate} from "../main/resources/assets/js/template.mjs";

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const htmlFile = __dirname + '/../main/resources/index.html';
let templateHtml;

beforeAll(() => {
    document.body.innerHTML = fs.readFileSync(htmlFile, 'utf8');
    templateHtml = document.body.querySelector('template');
});

describe('blah blah blah', () => {
    test('xyz', () => {
        const issue = {
            id: 'region',
            impact: 'moderate',
            dataHash: 'eb3026fa95fda35bce525495de908954',
            help: 'All page content should be contained by landmarks',
            helpUrl: 'http://somehelpurl',
            html: ['<a href="#main-content" class="govuk-skip-link" data-module="govuk-skip-link">Skip to main content</a>'],
            affects: ['http://axeresult-a'],
            testEngineVersion: '4.7.2',
            permaLink: 'http://localhost/?search=eb3026fa95fda35bce525495de908954'
        }

        const populatedIssueElement = populateTemplate(templateHtml, issue);

        const helpUrl = populatedIssueElement.querySelector('#helpUrl');
        expect(helpUrl.getAttribute('href')).toBe('http://somehelpurl');
        expect(helpUrl.textContent).toBe('region');
    })
})