/**
 * @jest-environment jsdom
 */

import {jest, test, describe, beforeEach, expect} from '@jest/globals';
import {metaDataHeader} from '../main/resources/assets/js/metaDataHeader.mjs';

let reportMetaDataElement;

beforeEach(() => {
    document.body.innerHTML = '<div id="metaDataHeader"/>';
    reportMetaDataElement = document.getElementById('metaDataHeader');
})

describe('with Jenkins build metadata', () => {
    test('includes link to Jenkins build', () => {
        const reportMetaData = {
            projectName: "platform-example-ui-tests",
            jenkinsBuildId: "#42",
            jenkinsBuildUrl: "https://build.tax.service.gov.uk/job/Platform-Testing/job/Examples/job/platform-example-ui-tests/42/",
            dateOfAssessment: "14th September 2023 at 12:23PM"
        }
        reportMetaData.testEnvironment = "Chrome"; // TODO: should be apart of the report meta data json

        metaDataHeader(reportMetaDataElement, reportMetaData);

        const metaData = document.querySelector('p');
        expect(metaData.textContent).toBe("Generated from build #42 (Chrome) of platform-example-ui-tests on 14th September 2023 at 12:23PM");

        const linkToBuild = document.querySelector('a:nth-child(1)');
        expect(linkToBuild.textContent).toBe('#42');
        expect(linkToBuild.href).toBe('https://build.tax.service.gov.uk/job/Platform-Testing/job/Examples/job/platform-example-ui-tests/42/');

        const linkToProject = document.querySelector('a:nth-child(2)');
        expect(linkToProject.textContent).toBe('platform-example-ui-tests');
        expect(linkToProject.href).toBe('https://github.com/hmrc/platform-example-ui-tests');

        const timeElement = document.querySelector('time');
        expect(timeElement.dateTime).toBe('14th September 2023 at 12:23PM');
    });

    test('default build URL to # if not available', () => {
        const reportMetaData = {
            projectName: "platform-example-ui-tests",
            jenkinsBuildId: "#44",
            dateOfAssessment: "1st September 2023 at 12:23PM"
        }
        reportMetaData.testEnvironment = "Chrome"; // TODO: should be apart of the report meta data json

        metaDataHeader(reportMetaDataElement, reportMetaData);

        const metaData = document.querySelector('p');
        expect(metaData.textContent).toBe("Generated from build #44 (Chrome) of platform-example-ui-tests on 1st September 2023 at 12:23PM");

        const linkToBuild = document.querySelector('a:nth-child(1)');
        expect(linkToBuild.textContent).toBe('#44');
        expect(linkToBuild.getAttribute('href')).toBe('#');

        const linkToProject = document.querySelector('a:nth-child(2)');
        expect(linkToProject.textContent).toBe('platform-example-ui-tests');
        expect(linkToProject.href).toBe('https://github.com/hmrc/platform-example-ui-tests');

        const timeElement = document.querySelector('time');
        expect(timeElement.dateTime).toBe('1st September 2023 at 12:23PM');
    });
});

describe("without Jenkins build metadata (eg. run locally)", () => {
    test('include link to project and run date', () => {
        const reportMetaData = {
            projectName: "platform-example-ui-tests",
            jenkinsBuildId: null,
            jenkinsBuildUrl: null,
            dateOfAssessment: "1st September 2023 at 12:23PM"
        }
        reportMetaData.testEnvironment = "Chrome"; // TODO: should be apart of the report meta data json

        metaDataHeader(reportMetaDataElement, reportMetaData);

        const metaData = document.querySelector('p');
        expect(metaData.textContent).toBe("Generated from platform-example-ui-tests on 1st September 2023 at 12:23PM");

        const linkToProject = document.querySelector('a');
        expect(linkToProject.textContent).toBe('platform-example-ui-tests');
        expect(linkToProject.href).toBe('https://github.com/hmrc/platform-example-ui-tests');

        const timeElement = document.querySelector('time');
        expect(timeElement.dateTime).toBe('1st September 2023 at 12:23PM');
    });
});

